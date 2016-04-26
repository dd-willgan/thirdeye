/**
 * Copyright (C) 2014-2015 LinkedIn Corp. (pinot-core@linkedin.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.linkedin.thirdeye.hadoop.derivedcolumn.transformation;

import static com.linkedin.thirdeye.hadoop.derivedcolumn.transformation.DerivedColumnTransformationPhaseConstants.DERIVED_COLUMN_TRANSFORMATION_PHASE_INPUT_PATH;
import static com.linkedin.thirdeye.hadoop.derivedcolumn.transformation.DerivedColumnTransformationPhaseConstants.DERIVED_COLUMN_TRANSFORMATION_PHASE_OUTPUT_PATH;
import static com.linkedin.thirdeye.hadoop.derivedcolumn.transformation.DerivedColumnTransformationPhaseConstants.DERIVED_COLUMN_TRANSFORMATION_PHASE_OUTPUT_SCHEMA;
import static com.linkedin.thirdeye.hadoop.derivedcolumn.transformation.DerivedColumnTransformationPhaseConstants.DERIVED_COLUMN_TRANSFORMATION_PHASE_THIRDEYE_CONFIG;
import static com.linkedin.thirdeye.hadoop.derivedcolumn.transformation.DerivedColumnTransformationPhaseConstants.DERIVED_COLUMN_TRANSFORMATION_PHASE_TOPK_PATH;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.linkedin.thirdeye.hadoop.config.MetricSpec;
import com.linkedin.thirdeye.hadoop.config.MetricType;
import com.linkedin.thirdeye.hadoop.config.ThirdEyeConstants;
import com.linkedin.thirdeye.hadoop.config.TopKDimensionToMetricsSpec;
import com.linkedin.thirdeye.hadoop.config.TopkWhitelistSpec;
import com.linkedin.thirdeye.hadoop.config.ThirdEyeConfig;
import com.linkedin.thirdeye.hadoop.topk.TopKDimensionValues;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.SchemaBuilder.BaseFieldTypeBuilder;
import org.apache.avro.SchemaBuilder.FieldAssembler;
import org.apache.avro.SchemaBuilder.RecordBuilder;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.mapred.AvroKey;
import org.apache.avro.mapreduce.AvroJob;
import org.apache.avro.mapreduce.AvroKeyInputFormat;
import org.apache.avro.mapreduce.AvroKeyOutputFormat;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This phase will add a new column for every column that has topk/whitelist config
 * The new column added will be called column_raw (containing all values) and
 * column will contain only topk+whitelist values
 */
public class DerivedColumnTransformationPhaseJob extends Configured {
  private static final Logger LOGGER = LoggerFactory.getLogger(DerivedColumnTransformationPhaseJob.class);

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private String name;
  private Properties props;

  /**
   * @param name
   * @param props
   */
  public DerivedColumnTransformationPhaseJob(String name, Properties props) {
    super(new Configuration());
    this.name = name;
    this.props = props;
  }

  public static class DerivedColumnTransformationPhaseMapper
      extends Mapper<AvroKey<GenericRecord>, NullWritable, AvroKey<GenericRecord>, NullWritable> {

    private Schema outputSchema;
    private ThirdEyeConfig thirdeyeConfig;
    private DerivedColumnTransformationPhaseConfig config;
    private List<String> dimensionsNames;
    private List<String> metricNames;
    private TopKDimensionValues topKDimensionValues;
    private Map<String, Set<String>> topKDimensionsMap;
    private String timeColumnName;

    @Override
    public void setup(Context context) throws IOException, InterruptedException {
      LOGGER.info("DerivedColumnTransformationPhaseJob.DerivedColumnTransformationPhaseMapper.setup()");
      Configuration configuration = context.getConfiguration();
      FileSystem fs = FileSystem.get(configuration);

      thirdeyeConfig = OBJECT_MAPPER.readValue(configuration.get(DERIVED_COLUMN_TRANSFORMATION_PHASE_THIRDEYE_CONFIG.toString()), ThirdEyeConfig.class);
      config = DerivedColumnTransformationPhaseConfig.fromThirdEyeConfig(thirdeyeConfig);
      dimensionsNames = config.getDimensionNames();
      metricNames = config.getMetricNames();
      timeColumnName = config.getTimeColumnName();

      outputSchema = new Schema.Parser().parse(configuration.get(DERIVED_COLUMN_TRANSFORMATION_PHASE_OUTPUT_SCHEMA.toString()));

      Path topKPath = new Path(configuration.get(DERIVED_COLUMN_TRANSFORMATION_PHASE_TOPK_PATH.toString()));
      topKDimensionValues = new TopKDimensionValues();
      if (fs.exists(topKPath)) {
        FSDataInputStream topkValuesStream = fs.open(topKPath);
        topKDimensionValues = OBJECT_MAPPER.readValue(topkValuesStream, TopKDimensionValues.class);
        topkValuesStream.close();
      }
      topKDimensionsMap = topKDimensionValues.getTopKDimensions();
    }


    @Override
    public void map(AvroKey<GenericRecord> key, NullWritable value, Context context)
        throws IOException, InterruptedException {

      // input record
      GenericRecord inputRecord = key.datum();

      // output record
      GenericRecord outputRecord = new Record(outputSchema);

      // dimensions
      for (String dimension : dimensionsNames) {
        String dimensionName = dimension;
        String dimensionValue = getDimensionFromRecord(inputRecord, dimension);
        // add column for topk + whitelist
        if (topKDimensionsMap.containsKey(dimensionName)) {
          Set<String> topKDimensionValues = topKDimensionsMap.get(dimensionName);
          if (topKDimensionValues != null && topKDimensionValues.contains(dimensionValue)) {
            outputRecord.put(dimensionName, dimensionValue);
          } else {
            outputRecord.put(dimensionName, ThirdEyeConstants.OTHER);
          }
          dimensionName = dimension + ThirdEyeConstants.RAW_DIMENSION_SUFFIX;
        }
        outputRecord.put(dimensionName, dimensionValue);
      }

      // metrics
      for (String metric : metricNames) {
        outputRecord.put(metric, getMetricFromRecord(inputRecord, metric));
      }

      // time
      outputRecord.put(timeColumnName, getMetricFromRecord(inputRecord, timeColumnName));

      AvroKey<GenericRecord> outputKey = new AvroKey<GenericRecord>(outputRecord);
      context.write(outputKey , NullWritable.get());
    }

    private String getDimensionFromRecord(GenericRecord record, String dimensionName) {
      String dimensionValue = (String) record.get(dimensionName);
      if (dimensionValue == null) {
        dimensionValue = ThirdEyeConstants.EMPTY_STRING;
      }
      return dimensionValue;
    }

    private Number getMetricFromRecord(GenericRecord record, String metricName) {
      Number metricValue = (Number) record.get(metricName);
      if (metricValue == null) {
        metricValue = ThirdEyeConstants.EMPTY_NUMBER;
      }
      return metricValue;
    }


    @Override
    public void cleanup(Context context) throws IOException, InterruptedException {

    }
  }

  public Job run() throws Exception {
    Job job = Job.getInstance(getConf());
    job.setJobName(name);
    job.setJarByClass(DerivedColumnTransformationPhaseJob.class);

    Configuration configuration = job.getConfiguration();
    FileSystem fs = FileSystem.get(configuration);

    // Input Path
    String inputPathDir = getAndSetConfiguration(configuration, DERIVED_COLUMN_TRANSFORMATION_PHASE_INPUT_PATH);
    LOGGER.info("Input path dir: " + inputPathDir);
    for (String inputPath : inputPathDir.split(",")) {
      LOGGER.info("Adding input:" + inputPath);
      Path input = new Path(inputPath);
      FileInputFormat.addInputPath(job, input);
    }

    // Topk path
    String topkPath = getAndSetConfiguration(configuration, DERIVED_COLUMN_TRANSFORMATION_PHASE_TOPK_PATH);
    LOGGER.info("Topk path : " + topkPath);

    // Output path
    Path outputPath = new Path(getAndSetConfiguration(configuration, DERIVED_COLUMN_TRANSFORMATION_PHASE_OUTPUT_PATH));
    LOGGER.info("Output path dir: " + outputPath.toString());
    if (fs.exists(outputPath)) {
      fs.delete(outputPath, true);
    }
    FileOutputFormat.setOutputPath(job, outputPath);

    // ThirdEye Config
    ThirdEyeConfig thirdeyeConfig = ThirdEyeConfig.fromProperties(props);
    job.getConfiguration().set(DERIVED_COLUMN_TRANSFORMATION_PHASE_THIRDEYE_CONFIG.toString(),
        OBJECT_MAPPER.writeValueAsString(thirdeyeConfig));
    LOGGER.info("ThirdEyeConfig {}", thirdeyeConfig);

    // New schema
    Schema outputSchema = newSchema(thirdeyeConfig);
    job.getConfiguration().set(DERIVED_COLUMN_TRANSFORMATION_PHASE_OUTPUT_SCHEMA.toString(), outputSchema.toString());

    // Map config
    job.setMapperClass(DerivedColumnTransformationPhaseMapper.class);
    job.setInputFormatClass(AvroKeyInputFormat.class);
    job.setMapOutputKeyClass(AvroKey.class);
    job.setMapOutputValueClass(NullWritable.class);
    AvroJob.setOutputKeySchema(job, outputSchema);
    job.setOutputFormatClass(AvroKeyOutputFormat.class);

    job.setNumReduceTasks(0);

    job.waitForCompletion(true);

    return job;
  }


  public Schema newSchema(ThirdEyeConfig thirdeyeConfig) {
    Schema outputSchema = null;

    Set<String> transformDimensionSet = new HashSet<>();
    TopkWhitelistSpec topkWhitelist = thirdeyeConfig.getTopKWhitelist();

    // gather topk + whitelist columns
    if (topkWhitelist != null) {
      List<TopKDimensionToMetricsSpec> topKDimensionToMetricsSpecs = topkWhitelist.getTopKDimensionToMetricsSpec();
      if (topKDimensionToMetricsSpecs != null) {
        for (TopKDimensionToMetricsSpec topKDimensionToMetricsSpec : topKDimensionToMetricsSpecs) {
          transformDimensionSet.add(topKDimensionToMetricsSpec.getDimensionName());
        }
      }
      Map<String, String> whitelist = topkWhitelist.getWhitelist();
      if (whitelist != null) {
        transformDimensionSet.addAll(whitelist.keySet());
      }
    }
    RecordBuilder<Schema> recordBuilder = SchemaBuilder.record(thirdeyeConfig.getCollection());
    FieldAssembler<Schema> fieldAssembler = recordBuilder.fields();

    // add new column for topk + whitelist columns
    for (String dimension : thirdeyeConfig.getDimensionNames()) {
      fieldAssembler = fieldAssembler.name(dimension).type().nullable().stringType().noDefault();
      if (transformDimensionSet.contains(dimension)) {
        fieldAssembler = fieldAssembler.name(dimension + ThirdEyeConstants.RAW_DIMENSION_SUFFIX).type().nullable().stringType().noDefault();
      }
    }

    for (MetricSpec metricSpec : thirdeyeConfig.getMetrics()) {
      String metric = metricSpec.getName();
      MetricType metricType = metricSpec.getType();
      BaseFieldTypeBuilder<Schema> baseFieldTypeBuilder = fieldAssembler.name(metric).type().nullable();

      switch (metricType) {
        case SHORT:
        case INT:
          fieldAssembler = baseFieldTypeBuilder.intType().noDefault();
          break;
        case FLOAT:
          fieldAssembler = baseFieldTypeBuilder.floatType().noDefault();
          break;
        case DOUBLE:
          fieldAssembler = baseFieldTypeBuilder.doubleType().noDefault();
          break;
        case LONG:
        default:
          fieldAssembler = baseFieldTypeBuilder.longType().noDefault();
      }
    }

    String timeColumnName = thirdeyeConfig.getTime().getColumnName();
    fieldAssembler = fieldAssembler.name(timeColumnName).type().longType().noDefault();

    outputSchema = fieldAssembler.endRecord();
    LOGGER.info("New schema {}", outputSchema.toString(true));

    return outputSchema;
  }

  private String getAndSetConfiguration(Configuration configuration,
      DerivedColumnTransformationPhaseConstants constant) {
    String value = getAndCheck(constant.toString());
    configuration.set(constant.toString(), value);
    return value;
  }

  private String getAndCheck(String propName) {
    String propValue = props.getProperty(propName);
    if (propValue == null) {
      throw new IllegalArgumentException(propName + " required property");
    }
    return propValue;
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      throw new IllegalArgumentException("usage: config.properties");
    }

    Properties props = new Properties();
    props.load(new FileInputStream(args[0]));
    DerivedColumnTransformationPhaseJob job = new DerivedColumnTransformationPhaseJob("derived_column_transformation_job", props);
    job.run();
  }

}
