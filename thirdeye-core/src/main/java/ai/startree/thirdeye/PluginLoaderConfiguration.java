/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package ai.startree.thirdeye;

import com.google.inject.Singleton;

@Singleton
public class PluginLoaderConfiguration {

  private String pluginsPath = "plugins";

  public String getPluginsPath() {
    return pluginsPath;
  }

  public PluginLoaderConfiguration setPluginsPath(final String pluginsPath) {
    this.pluginsPath = pluginsPath;
    return this;
  }
}