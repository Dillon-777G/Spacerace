/* Copyright 2002-2022 CS GROUP
 * Licensed to CS GROUP (CS) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * CS licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.orekit.tutorials.yaml;

import org.orekit.models.earth.displacement.OceanLoading;
import org.orekit.models.earth.displacement.TidalDisplacement;

/**
 * Initial data to initialize the station displacement.
 * <p>
 * Data are read from a YAML file. Station displacement are:
 * </p>
 * <ul>
 * <li>{@link TidalDisplacement Tidal displacement}: displacement of reference points due to tidal effects.</li>
 * <li>{@link OceanLoading Ocean loading}: displacement of reference points due to ocean loading.</li>
 * </ul>
 * @author Bryan Cazabonne
 */
public class TutorialStationDisplacement {

    /** Flag for the computation of the tidal displacement. */
    private boolean withTidalCorrection;

    /** Flag for permanent deformation. */
    private boolean removePermanentDeformation;

    /** Flag for the computation of the ocean loading correction. */
    private boolean withOceanLoadingCorrection;

    /**
     * Get the flag for the computation of the tidal displacement.
     * @return true if tidal displacement are computed
     */
    public boolean isWithTidalCorrection() {
        return withTidalCorrection;
    }

    /**
     * Set the flag for the computation of the tidal displacement.
     * @param withTidalCorrection true if tidal displacement are computed
     */
    public void setWithTidalCorrection(final boolean withTidalCorrection) {
        this.withTidalCorrection = withTidalCorrection;
    }

    /**
     * Get the flag for permanent deformation.
     * @return if true, the station coordinates are considered <em>mean tide</em>
     *         and already include the permanent deformation, hence it should be
     *         removed from the displacement to avoid considering it twice;
     *         if false, the station coordinates are considered <em>conventional tide free</em>
     *         so the permanent deformation must be included in the displacement
     */
    public boolean removePermanentDeformation() {
        return removePermanentDeformation;
    }

    /**
     * Set the flag for permanent deformation.
     * @param removePermanentDeformation if true, the station coordinates are considered <em>mean tide</em>
     *        and already include the permanent deformation, hence it should be
     *        removed from the displacement to avoid considering it twice;
     *        if false, the station coordinates are considered <em>conventional tide free</em>
     *        so the permanent deformation must be included in the displacement
     */
    public void setRemovePermanentDeformation(final boolean removePermanentDeformation) {
        this.removePermanentDeformation = removePermanentDeformation;
    }

    /**
     * Get the flag for the computation of ocean loading correction.
     * @return true if ocean loading correction is computed
     */
    public boolean isWithOceanLoadingCorrection() {
        return withOceanLoadingCorrection;
    }

    /**
     * Set the flag for the computation of ocean loading correction.
     * @param withOceanLoadingCorrection true if ocean loading correction is computed
     */
    public void setWithOceanLoadingCorrection(final boolean withOceanLoadingCorrection) {
        this.withOceanLoadingCorrection = withOceanLoadingCorrection;
    }

}
