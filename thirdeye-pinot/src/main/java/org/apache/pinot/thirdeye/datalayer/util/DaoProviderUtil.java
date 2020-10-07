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

package org.apache.pinot.thirdeye.datalayer.util;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import java.io.File;
import javax.validation.Validation;
import org.apache.pinot.thirdeye.datalayer.DataSourceBuilder;
import org.apache.pinot.thirdeye.datalayer.ThirdEyePersistenceModule;
import org.apache.pinot.thirdeye.datalayer.bao.jdbc.AbstractManagerImpl;
import org.apache.pinot.thirdeye.datalayer.dto.AbstractDTO;
import org.apache.pinot.thirdeye.datalayer.util.PersistenceConfig.DatabaseConfiguration;
import org.apache.tomcat.jdbc.pool.DataSource;

public abstract class DaoProviderUtil {

  private static Injector injector;

  public static void init(File localConfigFile) {
    final PersistenceConfig configuration = readPersistenceConfig(localConfigFile);
    final DatabaseConfiguration dbConfig = configuration.getDatabaseConfiguration();

    init(new DataSourceBuilder().build(dbConfig));
  }

  public static void init(DataSource dataSource) {
    injector = Guice.createInjector(new ThirdEyePersistenceModule(dataSource));
  }

  public static <T extends AbstractManagerImpl<? extends AbstractDTO>> T getInstance(Class<T> c) {
    return injector.getInstance(c);
  }

  public static PersistenceConfig readPersistenceConfig(File configFile) {
    YamlConfigurationFactory<PersistenceConfig> factory = new YamlConfigurationFactory<>(
        PersistenceConfig.class,
        Validation.buildDefaultValidatorFactory().getValidator(),
        Jackson.newObjectMapper(),
        "");
    try {
      return factory.build(configFile);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
