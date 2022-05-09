/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.metric;

import ai.startree.thirdeye.spi.datalayer.Predicate;
import ai.startree.thirdeye.spi.datalayer.dto.DatasetConfigDTO;
import ai.startree.thirdeye.spi.datalayer.dto.MetricConfigDTO;
import ai.startree.thirdeye.spi.detection.TimeGranularity;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

/**
 * Selector for time series and aggregate values of a specific metric, independent of
 * data source.
 */
public final class MetricSlice {

  private final @NonNull MetricConfigDTO metricConfigDTO;
  private final Interval interval;
  private final List<Predicate> predicates;
  @Deprecated
  // use predicates - remove this asap - kept for compatibility with deprecated classes
  private final Multimap<String, String> filters;
  private final @NonNull DatasetConfigDTO datasetConfigDTO;

  protected MetricSlice(final @NonNull MetricConfigDTO metricConfigDTO,
      final @NonNull Interval interval,
      final List<Predicate> predicates, Multimap<String, String> filters,
      final @NonNull DatasetConfigDTO datasetConfigDTO) {
    this.metricConfigDTO = metricConfigDTO;
    this.interval = interval;
    this.predicates = predicates;
    this.filters = filters;
    this.datasetConfigDTO = datasetConfigDTO;
  }

  public static MetricSlice from(final @NonNull MetricConfigDTO metricConfigDTO,
      final Interval interval, final @NonNull DatasetConfigDTO datasetConfigDTO) {
    return new MetricSlice(metricConfigDTO,
        interval,
        List.of(),
        ArrayListMultimap.create(),
        datasetConfigDTO);
  }

  public static MetricSlice from(final @NonNull MetricConfigDTO metricConfigDTO,
      final Interval interval,
      final List<Predicate> predicates, final @NonNull DatasetConfigDTO datasetConfigDTO) {
    return new MetricSlice(metricConfigDTO,
        interval,
        predicates,
        ArrayListMultimap.create(),
        datasetConfigDTO);
  }

  public static MetricSlice from(final @NonNull MetricConfigDTO metricConfigDTO,
      final Interval interval,
      final List<Predicate> predicates, TimeGranularity granularity,
      final @NonNull DatasetConfigDTO datasetConfigDTO) {
    // todo cyril set granularity
    return new MetricSlice(metricConfigDTO,
        interval,
        predicates,
        ArrayListMultimap.create(),
        datasetConfigDTO);
  }

  @Deprecated
  public static MetricSlice from(final long metricId, long start, long end) {
    return new MetricSlice((MetricConfigDTO) new MetricConfigDTO().setId(metricId),
        new Interval(start, end, DateTimeZone.UTC),
        List.of(),
        ArrayListMultimap.create(),
        new DatasetConfigDTO());
  }

  @Deprecated
  public static MetricSlice from(final long metricId, long start, long end,
      Multimap<String, String> filters) {
    return new MetricSlice((MetricConfigDTO) new MetricConfigDTO().setId(metricId),
        new Interval(start, end, DateTimeZone.UTC),
        List.of(),
        filters,
        new DatasetConfigDTO());
  }

  @Deprecated
  public static MetricSlice from(final long metricId, long start, long end,
      Multimap<String, String> filters, TimeGranularity granularity) {
    DatasetConfigDTO datasetConfigDTO = new DatasetConfigDTO();
    datasetConfigDTO.setNonAdditiveBucketSize(granularity.getSize());
    datasetConfigDTO.setNonAdditiveBucketUnit(granularity.getUnit());
    return new MetricSlice((MetricConfigDTO) new MetricConfigDTO().setId(metricId),
        new Interval(start, end, DateTimeZone.UTC),
        List.of(),
        filters,
        new DatasetConfigDTO());
  }

  @Deprecated
  public long getMetricId() {
    return metricConfigDTO.getId();
  }

  @Deprecated
  public long getStartMillis() {
    return interval.getStartMillis();
  }

  @Deprecated
  public long getEndMillis() {
    return interval.getEndMillis();
  }

  public @NonNull MetricConfigDTO getMetricConfigDTO() {
    return metricConfigDTO;
  }

  public @NonNull DatasetConfigDTO getDatasetConfigDTO() {
    return datasetConfigDTO;
  }

  public DateTime getStart() {
    return interval.getStart();
  }

  public DateTime getEnd() {
    return interval.getEnd();
  }

  public Interval getInterval() {
    return interval;
  }

  public List<Predicate> getPredicates() {
    return predicates;
  }

  @Deprecated
  // use predicates
  public Multimap<String, String> getFilters() {
    return filters;
  }

  @Deprecated
  public @Nullable TimeGranularity getGranularity() {
    return datasetConfigDTO.bucketTimeGranularity();
  }

  @Deprecated
  public @Nullable String getDatasetName() {
    return datasetConfigDTO.getDataset();
  }

  public MetricSlice withStart(DateTime start) {
    return new MetricSlice(metricConfigDTO,
        interval.withStart(start),
        predicates,
        filters,
        datasetConfigDTO);
  }

  @Deprecated
  public MetricSlice withStart(long start) {
    return new MetricSlice(metricConfigDTO,
        interval.withStartMillis(start),
        predicates,
        filters,
        datasetConfigDTO);
  }

  public MetricSlice withEnd(DateTime end) {
    return new MetricSlice(metricConfigDTO,
        interval.withEnd(end),
        predicates,
        filters,
        datasetConfigDTO);
  }

  @Deprecated
  public MetricSlice withEnd(long end) {
    return new MetricSlice(metricConfigDTO,
        interval.withEndMillis(end),
        predicates,
        filters,
        datasetConfigDTO);
  }

  public MetricSlice withPredicates(List<Predicate> predicates) {
    return new MetricSlice(metricConfigDTO, interval, predicates, filters, datasetConfigDTO);
  }

  public MetricSlice withMetricConfigDto(MetricConfigDTO metricConfigDTO) {
    return new MetricSlice(metricConfigDTO, interval, predicates, filters, datasetConfigDTO);
  }

  public MetricSlice withDatasetConfigDto(DatasetConfigDTO datasetConfigDto) {
    return new MetricSlice(metricConfigDTO, interval, predicates, filters, datasetConfigDto);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MetricSlice that = (MetricSlice) o;
    return Objects.equals(metricConfigDTO, that.metricConfigDTO) &&
        Objects.equals(interval, that.interval) &&
        Objects.equals(predicates, that.predicates) &&
        Objects.equals(filters, that.filters) &&
        Objects.equals(datasetConfigDTO, that.datasetConfigDTO);
  }

  @Override
  public int hashCode() {
    return Objects.hash(metricConfigDTO, interval, predicates, filters, datasetConfigDTO);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("metricId", metricConfigDTO.getId())
        .add("start", interval.getStart())
        .add("end", interval.getEnd())
        .add("predicates", predicates)
        .add("filters", filters)
        .add("dataset", datasetConfigDTO.getDataset())
        .toString();
  }
}