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

package org.apache.pinot.thirdeye.anomaly.utils;

import com.codahale.metrics.Counter;
import com.codahale.metrics.MetricRegistry;

public class ThirdeyeMetricsUtil {

  private static final MetricRegistry metricRegistry = new MetricRegistry();

  public static final Counter taskCounter =
      metricRegistry.counter("taskCounter");
  public static final Counter taskSuccessCounter =
      metricRegistry.counter("taskSuccessCounter");
  public static final Counter taskExceptionCounter =
      metricRegistry.counter("taskExceptionCounter");
  public static final Counter taskDurationCounter =
      metricRegistry.counter("taskDurationCounter");
  public static final Counter detectionTaskCounter =
      metricRegistry.counter("detectionTaskCounter");
  public static final Counter detectionTaskSuccessCounter =
      metricRegistry.counter("detectionTaskSuccessCounter");
  public static final Counter detectionTaskExceptionCounter =
      metricRegistry.counter("detectionTaskExceptionCounter");
  public static final Counter dataQualityTaskCounter =
      metricRegistry.counter("dataQualityTaskCounter");
  public static final Counter dataQualityTaskSuccessCounter =
      metricRegistry.counter("dataQualityTaskSuccessCounter");
  public static final Counter dataQualityTaskExceptionCounter =
      metricRegistry.counter("dataQualityTaskExceptionCounter");
  public static final Counter alertTaskCounter =
      metricRegistry.counter("alertTaskCounter");
  public static final Counter alertTaskSuccessCounter =
      metricRegistry.counter("alertTaskSuccessCounter");
  public static final Counter alertTaskExceptionCounter =
      metricRegistry.counter("alertTaskExceptionCounter");
  public static final Counter dbCallCounter =
      metricRegistry.counter("dbCallCounter");
  public static final Counter dbExceptionCounter =
      metricRegistry.counter("dbExceptionCounter");
  public static final Counter dbReadCallCounter =
      metricRegistry.counter("dbReadCallCounter");
  public static final Counter dbReadByteCounter =
      metricRegistry.counter("dbReadByteCounter");
  public static final Counter dbReadDurationCounter =
      metricRegistry.counter("dbReadDurationCounter");
  public static final Counter dbWriteCallCounter =
      metricRegistry.counter("dbWriteCallCounter");
  public static final Counter dbWriteByteCounter =
      metricRegistry.counter("dbWriteByteCounter");
  public static final Counter dbWriteDurationCounter =
      metricRegistry.counter("dbWriteDurationCounter");
  public static final Counter datasourceCallCounter =
      metricRegistry.counter("datasourceCallCounter");
  public static final Counter datasourceDurationCounter =
      metricRegistry.counter("datasourceDurationCounter");
  public static final Counter datasourceExceptionCounter =
      metricRegistry.counter("datasourceExceptionCounter");
  public static final Counter couchbaseCallCounter =
      metricRegistry.counter("couchbaseCallCounter");
  public static final Counter couchbaseWriteCounter =
      metricRegistry.counter("couchbaseWriteCounter");
  public static final Counter couchbaseExceptionCounter =
      metricRegistry.counter("couchbaseExceptionCounter");
  public static final Counter rcaPipelineCallCounter =
      metricRegistry.counter("rcaPipelineCallCounter");
  public static final Counter rcaPipelineDurationCounter =
      metricRegistry.counter("rcaPipelineDurationCounter");
  public static final Counter rcaPipelineExceptionCounter =
      metricRegistry.counter("rcaPipelineExceptionCounter");
  public static final Counter rcaFrameworkCallCounter =
      metricRegistry.counter("rcaFrameworkCallCounter");
  public static final Counter rcaFrameworkDurationCounter =
      metricRegistry.counter("rcaFrameworkDurationCounter");
  public static final Counter rcaFrameworkExceptionCounter =
      metricRegistry.counter("rcaFrameworkExceptionCounter");
  public static final Counter cubeCallCounter =
      metricRegistry.counter("cubeCallCounter");
  public static final Counter cubeDurationCounter =
      metricRegistry.counter("cubeDurationCounter");
  public static final Counter cubeExceptionCounter =
      metricRegistry.counter("cubeExceptionCounter");
  public static final Counter detectionRetuneCounter =
      metricRegistry.counter("detectionRetuneCounter");
  public static final Counter triggerEventCounter =
      metricRegistry.counter("triggerEventCounter");
  public static final Counter processedTriggerEventCounter =
      metricRegistry.counter("processedTriggerEventCounter");
  public static final Counter eventScheduledTaskCounter =
      metricRegistry.counter("eventScheduledTaskCounter");
  public static final Counter eventScheduledTaskFallbackCounter =
      metricRegistry.counter("eventScheduledTaskFallbackCounter");
  public static final Counter emailAlertsSucesssCounter =
      metricRegistry.counter("emailAlertsSucesssCounter");
  public static final Counter emailAlertsFailedCounter =
      metricRegistry.counter("emailAlertsFailedCounter");
  public static final Counter jiraAlertsSuccessCounter =
      metricRegistry.counter("jiraAlertsSuccessCounter");
  public static final Counter jiraAlertsFailedCounter =
      metricRegistry.counter("jiraAlertsFailedCounter");
  public static final Counter jiraAlertsNumTicketsCounter =
      metricRegistry.counter("jiraAlertsNumTicketsCounter");
  public static final Counter jiraAlertsNumCommentsCounter =
      metricRegistry.counter("jiraAlertsNumCommentsCounter");

  private ThirdeyeMetricsUtil() {
  }
}
