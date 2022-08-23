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

import ai.startree.thirdeye.spi.datalayer.dto.EnumerationItemDTO;
import ai.startree.thirdeye.spi.detection.model.DetectionResult;

public class WrappedDetectionPipelineResult extends DetectionResult {

  private final EnumerationItemDTO enumerationItem;

  public WrappedDetectionPipelineResult(final EnumerationItemDTO enumerationItem,
      final DetectionResult delegate) {
    super(delegate);
    this.enumerationItem = enumerationItem;
  }

  @Override
  public EnumerationItemDTO getEvaluationItem() {
    return enumerationItem;
  }
}