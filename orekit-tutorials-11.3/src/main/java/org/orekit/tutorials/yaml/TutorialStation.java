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

import java.util.List;

/**
 * Initial data used to initialize station data
 * (name, coordinates, observation types and measurement data).
 * <p>
 * Data are read from a YAML file.
 * </p>
 * <table border="1" cellpadding="5">
 * <caption>Station coordinates</caption>
 * <tr style="background-color: #ccccff;">
 * <th>Element</th><th>Coordinate</th><th>Unit</th>
 * <tr><td style="background-color: #eeeeff;">0</td><td> Latitude</td><td>°</td>
 * <tr><td style="background-color: #eeeeff;">1</td><td>Longitude</td><td>°</td>
 * <tr><td style="background-color: #eeeeff;">2</td><td> Altitude</td><td>m</td>
 * </table>
 * @author Bryan Cazabonne
 */
public class TutorialStation {

    /** Get the station name. */
    private String name;

    /** Station coordinated (angles in degrees and altitude in meters). */
    private double[] coordinates;

    /** List of observation types supported by the station. */
    private List<TutorialObservationType> observationTypes;

    /** Estimated range measurement bias for the ground station. */
    private TutorialEstimatedParameter rangeBias;

    /** Estimated range rate measurement bias for the ground station. */
    private TutorialEstimatedParameter rangeRateBias;

    /** Estimated azimuth/elevation measurement bias for the ground station. */
    private TutorialEstimatedParameter azElBias;

    /** Estimated phase measurement bias for the ground station. */
    private TutorialEstimatedParameter phaseBias;

    /** Estimated position/velocity measurement bias for the ground station. */
    private TutorialEstimatedParameter pvBias;

    /**
     * Constructor.
     */
    public TutorialStation() {
        // initialise empty array
        this.coordinates = new double[0];
    }

    /**
     * Get the station name.
     * @return the station name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the station name.
     * @param name station name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the array containing the station coordinates.
     * <p>
     * Order of the elements is given in the {@link TutorialStation class documentation}.
     * </p>
     * @return the array containing the station coordinates.
     */
    public double[] getCoordinates() {
        return coordinates.clone();
    }

    /**
     * Set the array containing the station coordinates.
    * <p>
     * Order of the elements is given in the {@link TutorialStation class documentation}.
     * </p>
     * @param coordinates array containing the station coordinates.
     */
    public void setCoordinates(final double[] coordinates) {
        this.coordinates = coordinates.clone();
    }

    /**
     * Get the list of observation types.
     * @return the list of observation types
     */
    public List<TutorialObservationType> getObservationTypes() {
        return observationTypes;
    }

    /**
     * Set the list of observation types.
     * @param observationTypes list of observation types
     */
    public void setObservationTypes(final List<TutorialObservationType> observationTypes) {
        this.observationTypes = observationTypes;
    }

    /**
     * Get the estimated range measurement bias.
     * @return the estimated range measurement bias
     */
    public TutorialEstimatedParameter getRangeBias() {
        return rangeBias;
    }

    /**
     * Set the estimated range measurement bias.
     * @param rangeBias range estimated measurement bias
     */
    public void setRangeBias(final TutorialEstimatedParameter rangeBias) {
        this.rangeBias = rangeBias;
    }

    /**
     * Get the estimated range rate measurement bias.
     * @return the estimated range rate measurement bias
     */
    public TutorialEstimatedParameter getRangeRateBias() {
        return rangeRateBias;
    }

    /**
     * Set the estimated range rate measurement bias.
     * @param rangeRateBias range rate estimated measurement bias
     */
    public void setRangeRateBias(final TutorialEstimatedParameter rangeRateBias) {
        this.rangeRateBias = rangeRateBias;
    }

    /**
     * Get the estimated azimuth/elevation measurement bias.
     * @return the estimated azimuth/elevation measurement bias
     */
    public TutorialEstimatedParameter getAzElBias() {
        return azElBias;
    }

    /**
     * Set the estimated azimuth/elevation measurement bias.
     * @param azElBias azimuth/elevation estimated measurement bias
     */
    public void setAzElBias(final TutorialEstimatedParameter azElBias) {
        this.azElBias = azElBias;
    }

    /**
     * Get the estimated position/velocity measurement bias.
     * @return the estimated position/velocity measurement bias
     */
    public TutorialEstimatedParameter getPvBias() {
        return pvBias;
    }

    /**
     * Set the estimated position/velocity measurement bias.
     * @param pvBias position/velocity estimated measurement bias
     */
    public void setPvBias(final TutorialEstimatedParameter pvBias) {
        this.pvBias = pvBias;
    }

    /**
     * Get the estimated phase measurement bias.
     * @return the estimated phase measurement bias
     */
    public TutorialEstimatedParameter getPhaseBias() {
        return phaseBias;
    }

    /**
     * Set the estimated phase measurement bias.
     * @param phaseBias phase estimated measurement bias
     */
    public void setPhaseBias(final TutorialEstimatedParameter phaseBias) {
        this.phaseBias = phaseBias;
    }

    /**
     * Get the station latitude as read in the YAML file.
     * @return the station latitude as read in the YAML file (°)
     */
    public double getLatitude() {
        return coordinates[0];
    }

    /**
     * Get the station longitude as read in the YAML file.
     * @return the station longitude as read in the YAML file (°)
     */
    public double getLongitude() {
        return coordinates[1];
    }

    /**
     * Get the station altitude as read in the YAML file.
     * @return the station altitude as read in the YAML file (m)
     */
    public double getAltitude() {
        return coordinates[2];
    }

    /** Observation types for the measurements. */
    public static class TutorialObservationType {

        /** Name of the observation type. */
        private String name;

        /** Estimated station clock offset. */
        private TutorialEstimatedParameter clockOffset;

        /**
         * Get the name of the observation type.
         * @return the name of the observation type
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name of the observation type.
         * @param name name of the observation type
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * Get the estimated station clock offset.
         * @return the estimated station clock offset
         */
        public TutorialEstimatedParameter getClockOffset() {
            return clockOffset;
        }

        /**
         * Set the estimated clock offset.
         * @param clockOffset estimated clock offset
         */
        public void setClockOffset(final TutorialEstimatedParameter clockOffset) {
            this.clockOffset = clockOffset;
        }

    }

}
