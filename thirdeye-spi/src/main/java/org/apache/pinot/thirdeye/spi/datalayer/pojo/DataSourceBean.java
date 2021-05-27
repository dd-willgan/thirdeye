/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pinot.thirdeye.spi.datalayer.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.pinot.thirdeye.spi.datalayer.dto.AbstractDTO;

/**
 * This class defines the config of a single datasource used in thirdeye
 * Eg: PinotThirdeyeDataSource
 */
public class DataSourceBean extends AbstractDTO {

  private String name;
  private String classRef;
  private Map<String, Object> properties = new HashMap<>();
  private List<DataSourceMetaBean> metaList = new ArrayList<>();

  public String getName() {
    return name;
  }

  public DataSourceBean setName(final String name) {
    this.name = name;
    return this;
  }

  public String getClassRef() {
    return classRef;
  }

  public DataSourceBean setClassRef(final String classRef) {
    this.classRef = classRef;
    return this;
  }

  public Map<String, Object> getProperties() {
    return properties;
  }

  public DataSourceBean setProperties(
      final Map<String, Object> properties) {
    this.properties = properties;
    return this;
  }

  public List<DataSourceMetaBean> getMetaList() {
    return metaList;
  }

  public DataSourceBean setMetaList(
      final List<DataSourceMetaBean> metaList) {
    this.metaList = metaList;
    return this;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }
}
