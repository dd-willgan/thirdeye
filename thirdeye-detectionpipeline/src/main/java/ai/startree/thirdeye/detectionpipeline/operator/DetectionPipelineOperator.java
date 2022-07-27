/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.detectionpipeline.operator;

import ai.startree.thirdeye.detectionpipeline.utils.EpochTimeConverter;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean;
import ai.startree.thirdeye.spi.datalayer.dto.PlanNodeBean.OutputBean;
import ai.startree.thirdeye.spi.detection.TimeConverter;
import ai.startree.thirdeye.spi.detection.v2.DetectionPipelineResult;
import ai.startree.thirdeye.spi.detection.v2.Operator;
import ai.startree.thirdeye.spi.detection.v2.OperatorContext;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DetectionPipelineOperator forms the root of the detection class hierarchy. It represents a wireframe
 * for implementing (intermittently stateful) executable pipelines on top of it.
 */
public abstract class DetectionPipelineOperator implements Operator {

  protected static final String PROP_TYPE = "type";
  private static final Logger LOG = LoggerFactory.getLogger(DetectionPipelineOperator.class);
  private static final TimeConverter TIME_CONVERTER = new EpochTimeConverter(TimeUnit.MILLISECONDS.toString());

  protected PlanNodeBean planNode;
  protected Interval detectionInterval;
  protected Map<String, DetectionPipelineResult> resultMap = new HashMap<>();
  protected Map<String, DetectionPipelineResult> inputMap;
  protected Map<String, String> outputKeyMap = new HashMap<>();

  protected DetectionPipelineOperator() {
  }

  protected static Map<String, Object> getComponentSpec(final Map<String, Object> params) {
    final Map<String, Object> componentSpec = new HashMap<>();
    if (params == null || params.isEmpty()) {
      return componentSpec;
    }
    final String prefix = "component.";
    params.forEach((key, value) -> {
      if (key.startsWith(prefix)) {
        componentSpec.put(key.substring(prefix.length()), value);
      }
    });
    return componentSpec;
  }

  @Override
  public void init(final OperatorContext context) {
    planNode = context.getPlanNode();
    detectionInterval = context.getDetectionInterval();

    resultMap = new HashMap<>();
    inputMap = context.getInputsMap();
    if (context.getPlanNode().getOutputs() != null) {
      for (final OutputBean outputBean : context.getPlanNode().getOutputs()) {
        outputKeyMap.put(outputBean.getOutputKey(), outputBean.getOutputName());
      }
    }
  }

  /**
   * Returns a detection result for the time range between {@code startTime} and {@code endTime}.
   *
   * @return detection result
   */
  @Override
  public abstract void execute()
      throws Exception;

  public PlanNodeBean getPlanNode() {
    return planNode;
  }

  public Interval getDetectionInterval() {
    return detectionInterval;
  }

  protected void setOutput(String key, final DetectionPipelineResult output) {
    if (outputKeyMap.containsKey(key)) {
      key = outputKeyMap.get(key);
    }
    resultMap.put(key, output);
  }

  @Override
  public void setProperty(final String key, final Object value) {
    planNode.getParams().put(key, value);
  }

  @Override
  public DetectionPipelineResult getOutput(final String key) {
    return resultMap.get(key);
  }

  @Override
  public Map<String, DetectionPipelineResult> getOutputs() {
    return resultMap;
  }

  @Override
  public void setInput(final String key, final DetectionPipelineResult input) {
    inputMap.put(key, input);
  }
}