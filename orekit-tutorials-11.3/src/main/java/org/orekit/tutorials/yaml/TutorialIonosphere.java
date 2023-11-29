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

/**
 * Initial data to initialize an ionospheric delay model.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialIonosphere {

    /** Flag to know if the ionospheric model is estimated of not. */
    private boolean isEstimatedModel;

    /** Estimated ionospheric parameter. */
    private TutorialEstimatedParameter vtec;

    /** Height of the ionospheric layer (m). */
    private double ionosphericLayer;

    /**
     * Get the flag to know if the ionospheric model is estimated of not.
     * @return true if the ionospheric delay is computed with an estimated model
     */
    public boolean isEstimatedModel() {
        return isEstimatedModel;
    }

    /**
     * Set the flag to know if the ionospheric model is estimated of not.
     * @param isEstimatedModel true if the ionospheric delay is computed with an estimated model
     */
    public void setIsEstimatedModel(final boolean isEstimatedModel) {
        this.isEstimatedModel = isEstimatedModel;
    }

    /**
     * Get the estimated ionospheric parameter.
     * @return the estimated ionospheric parameter.
     */
    public TutorialEstimatedParameter getVtec() {
        return vtec;
    }

    /**
     * Set the estimated ionospheric parameter.
     * @param vtec estimated ionospheric parameter
     */
    public void setVtec(final TutorialEstimatedParameter vtec) {
        this.vtec = vtec;
    }

    /**
     * Get the height of the ionospheric layer.
     * @return the height of the ionospheric layer (m)
     */
    public double getIonosphericLayer() {
        return ionosphericLayer;
    }

    /**
     * Set the height of the ionospheric layer.
     * @param ionosphericLayer height of the ionospheric layer (m)
     */
    public void setIonosphericLayer(final double ionosphericLayer) {
        this.ionosphericLayer = ionosphericLayer;
    }

}
