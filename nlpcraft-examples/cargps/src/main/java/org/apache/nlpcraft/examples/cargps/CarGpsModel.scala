/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nlpcraft.examples.cargps

import org.apache.nlpcraft.model._

/**
 * See 'README.md' file in the same folder for running and testing instructions.
 */
class CarGpsModel extends NCModelFileAdapter("cargps_model.yaml") {
    /**
     *
     * @return
     */
    @NCIntentRef("int:navigate")
    @NCIntentSampleRef("samples/cargps_navigate_samples.txt")
    def onNavigation(): NCResult = {
        NCResult.text(s"")
    }

    /**
     *
     * @return
     */
    @NCIntentRef("int:cancel")
    @NCIntentSampleRef("samples/cargps_cancel_samples.txt")
    def onCancel(): NCResult = {
        NCResult.text(s"")
    }

    /**
     *
     * @return
     */
    @NCIntentRef("int:add:waypoint")
    @NCIntentSampleRef("samples/cargps_add_waypoint_samples.txt")
    def onAddWaypoint(): NCResult = {
        NCResult.text(s"")
    }

    /**
     *
     * @return
     */
    @NCIntentRef("int:remove:waypoint")
    @NCIntentSampleRef("samples/cargps_remove_waypoint_samples.txt")
    def onRemoveWaypoint(): NCResult = {
        NCResult.text(s"")
    }
}
