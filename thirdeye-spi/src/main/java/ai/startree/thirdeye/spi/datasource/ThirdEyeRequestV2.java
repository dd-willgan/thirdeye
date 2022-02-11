/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye.spi.datasource;

import ai.startree.thirdeye.spi.detection.v2.DataTable;
import java.util.Map;
import java.util.Objects;

/**
 * Request object containing all information for a {@link ThirdEyeDataSource} to retrieve {@link
 * DataTable}.
 */
public class ThirdEyeRequestV2 {

  private final String table;
  private final String query;
  private final Map<String, String> properties;

  public ThirdEyeRequestV2(final String table, final String query,
      final Map<String, String> properties) {
    this.table = table;
    this.query = query;
    this.properties = properties;
  }

  public String getTable() {
    return table;
  }

  public String getQuery() {
    return query;
  }

  public Map<String, String> getProperties() {
    return properties;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ThirdEyeRequestV2 that = (ThirdEyeRequestV2) o;
    return Objects.equals(table, that.table) && Objects.equals(query, that.query)
        && Objects.equals(properties, that.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(table, query, properties);
  }

  @Override
  public String toString() {
    return "ThirdEyeRequestV2{" +
        "table='" + table + '\'' +
        ", query='" + query + '\'' +
        ", properties=" + properties +
        '}';
  }
}