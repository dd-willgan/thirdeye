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
package ai.startree.thirdeye.plugins.datasource.auto.onboard;

import ai.startree.thirdeye.plugins.datasource.pinot.PinotThirdEyeDataSource;
import ai.startree.thirdeye.spi.datalayer.bao.DatasetConfigManager;
import ai.startree.thirdeye.spi.datalayer.bao.MetricConfigManager;
import ai.startree.thirdeye.spi.datalayer.dto.DataSourceMetaBean;
import ai.startree.thirdeye.spi.datasource.AutoOnboard;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a service to onboard datasets automatically to thirdeye from pinot
 * The run method is invoked periodically by the AutoOnboardService, and it checks for new tables in
 * pinot, to add to thirdeye
 * It also looks for any changes in dimensions or metrics to the existing tables
 */
public class AutoOnboardPinotMetadataSource extends AutoOnboard {

  private static final Logger LOG = LoggerFactory.getLogger(AutoOnboardPinotMetadataSource.class);

  private final PinotControllerRestClient pinotControllerRestClient;
  private final String dataSourceName;

  public AutoOnboardPinotMetadataSource(DataSourceMetaBean dataSourceMeta) {
    super(dataSourceMeta);
    pinotControllerRestClient = new PinotControllerRestClient(dataSourceMeta, "pinot");
    dataSourceName = MapUtils.getString(dataSourceMeta.getProperties(), "name",
        PinotThirdEyeDataSource.class.getSimpleName());
  }

  public AutoOnboardPinotMetadataSource(DataSourceMetaBean dataSourceMeta,
      final PinotControllerRestClient pinotControllerRestClient,
      final DatasetConfigManager datasetConfigManager,
      final MetricConfigManager metricConfigManager) {
    super(dataSourceMeta);
    this.pinotControllerRestClient = pinotControllerRestClient;
    this.datasetConfigManager = datasetConfigManager;
    this.metricConfigManager = metricConfigManager;

    dataSourceName = MapUtils.getString(dataSourceMeta.getProperties(), "name",
        PinotThirdEyeDataSource.class.getSimpleName());
  }

  public void run() {
    try {
      LOG.info("Checking all pinot tables");
      final PinotDatasetOnboarder pinotDatasetOnboarder = new PinotDatasetOnboarder(
          pinotControllerRestClient,
          datasetConfigManager,
          metricConfigManager);
      pinotDatasetOnboarder.onboardAll(dataSourceName);
    } catch (Exception e) {
      LOG.error("Exception in loading datasets", e);
    }
  }

  @Override
  public void runAdhoc() {
    LOG.info("Triggering adhoc run for AutoOnboard Pinot data source");
    run();
  }
}
