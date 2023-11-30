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
 * Initial data to initialize the measurements used during the orbit determination process.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialMeasurements {

    /** List of measurement files. */
    private List<String> measurementFiles;

    /** Sinex files containing station positions and eccentricities. */
    private TutorialSinex sinex;

    /** Flag for station position estimation. */
    private boolean withStationPositionEstimated;

    /** Tropospheric model. */
    private TutorialTroposphere troposphere;

    /** Ionospheric model. */
    private TutorialIonosphere ionosphere;

    /** Flag for the computation of the Shapiro effect. */
    private boolean withShapiro;

    /** Station displacement. */
    private TutorialStationDisplacement stationDisplacement;

    /** Used stations. */
    private List<TutorialStation> stations;

    /** Detection limit for outliers. */
    private int outlierRejectionMultiplier;

    /** Number of iterations before with filter is not applied. */
    private int outlierRejectionStartingIteration;

    /** Range measurement. */
    private TutorialRange range;

    /** Range rate measurement. */
    private TutorialRangeRate rangeRate;

    /** Azimuth/Elevation measurement. */
    private TutorialAzEl azEl;

    /** Phase measurement. */
    private TutorialPhase phase;

    /** Position/Velocity measurement. */
    private TutorialPV pv;

    /** Position only measurement. */
    private TutorialPosition position;

    /**
     * Get the list of measurement files.
     * @return the list of measurement files
     */
    public List<String> getMeasurementFiles() {
        return measurementFiles;
    }

    /**
     * Set the list of measurement files.
     * @param measurementFiles list of measurement files
     */
    public void setMeasurementFiles(final List<String> measurementFiles) {
        this.measurementFiles = measurementFiles;
    }


    /**
     * Get the sinex files containing station positions and eccentricities.
     * @return sinex files
     */
    public TutorialSinex getSinex() {
        return sinex;
    }

    /**
     * Set the sinex files containing station positions and eccentricities.
     * @param sinex the sinex file object to set
     */
    public void setSinex(final TutorialSinex sinex) {
        this.sinex = sinex;
    }

    /**
     * Get the flag for station position estimation.
     * @return true if station position is estimated
     */
    public boolean isWithStationPositionEstimated() {
        return withStationPositionEstimated;
    }

    /**
     * Set the flag for station position estimation.
     * @param withStationPositionEstimated true if station position is estimated
     */
    public void setWithStationPositionEstimated(final boolean withStationPositionEstimated) {
        this.withStationPositionEstimated = withStationPositionEstimated;
    }

    /**
     * Get the tropospheric model.
     * @return the tropospheric model
     */
    public TutorialTroposphere getTroposphere() {
        return troposphere;
    }

    /**
     * Set the tropospheric model.
     * @param troposphere tropospheric model
     */
    public void setTroposphere(final TutorialTroposphere troposphere) {
        this.troposphere = troposphere;
    }

    /**
     * Get the ionospheric model.
     * @return ionospheric model
     */
    public TutorialIonosphere getIonosphere() {
        return ionosphere;
    }

    /**
     * Set the ionospheric model.
     * @param ionosphere ionospheric model
     */
    public void setIonosphere(final TutorialIonosphere ionosphere) {
        this.ionosphere = ionosphere;
    }

    /**
     * Get the flag for the computation of the Shapiro effect.
     * @return true if Shapiro effect has to be computed
     */
    public boolean isWithShapiro() {
        return withShapiro;
    }

    /**
     * Set the flag for the computation of the Shapiro effect.
     * @param withShapiro true if Shapiro effect has to be computed
     */
    public void setWithShapiro(final boolean withShapiro) {
        this.withShapiro = withShapiro;
    }

    /**
     * Get the station displacement data.
     * @return the station displacement data
     */
    public TutorialStationDisplacement getStationDisplacement() {
        return stationDisplacement;
    }

    /**
     * Set the station displacement data.
     * @param stationDisplacement station displacement data
     */
    public void setStationDisplacement(final TutorialStationDisplacement stationDisplacement) {
        this.stationDisplacement = stationDisplacement;
    }

    /**
     * Get the list of used stations.
     * @return the list of used stations
     */
    public List<TutorialStation> getStations() {
        return stations;
    }

    /**
     * Set the list of used stations.
     * @param stations list of used stations
     */
    public void setStations(final List<TutorialStation> stations) {
        this.stations = stations;
    }


    /**
     * Get the detection limit for outliers.
     * @return the detection limit for outliers
     */
    public int getOutlierRejectionMultiplier() {
        return outlierRejectionMultiplier;
    }

    /**
     * Set the detection limit for outliers.
     * @param outlierRejectionMultiplier detection limit for outliers
     */
    public void setOutlierRejectionMultiplier(final int outlierRejectionMultiplier) {
        this.outlierRejectionMultiplier = outlierRejectionMultiplier;
    }

    /**
     * Get the number of iterations before with filter is not applied.
     * @return the number of iterations before with filter is not applied
     */
    public int getOutlierRejectionStartingIteration() {
        return outlierRejectionStartingIteration;
    }

    /**
     * Set the number of iterations before with filter is not applied.
     * @param outlierRejectionStartingIteration starting iteration for outliers
     */
    public void setOutlierRejectionStartingIteration(final int outlierRejectionStartingIteration) {
        this.outlierRejectionStartingIteration = outlierRejectionStartingIteration;
    }

    /**
     * Get the range measurement data.
     * @return the range measurement data
     */
    public TutorialRange getRange() {
        return range;
    }

    /**
     * Set the range measurement data.
     * @param range range measurement data
     */
    public void setRange(final TutorialRange range) {
        this.range = range;
    }

    /**
     * Get the range rate measurement data.
     * @return the range rate measurement data
     */
    public TutorialRangeRate getRangeRate() {
        return rangeRate;
    }

    /**
     * Set the range rate measurement data.
     * @param rangeRate range rate measurement data
     */
    public void setRangeRate(final TutorialRangeRate rangeRate) {
        this.rangeRate = rangeRate;
    }

    /**
     * Get the azimuth/elevation measurement data.
     * @return the azimuth/elevation measurement data
     */
    public TutorialAzEl getAzEl() {
        return azEl;
    }

    /**
     * Set the azimuth/elevation measurement data.
     * @param azEl azimuth/elevation measurement data
     */
    public void setAzEl(final TutorialAzEl azEl) {
        this.azEl = azEl;
    }

    /**
     * Get the phase measurement data.
     * @return the phase measurement data
     */
    public TutorialPhase getPhase() {
        return phase;
    }

    /**
     * Set the phase measurement data.
     * @param phase phase measurement data
     */
    public void setPhase(final TutorialPhase phase) {
        this.phase = phase;
    }

    /**
     * Get the position/velocity measurement data.
     * @return the position/velocity measurement data
     */
    public TutorialPV getPv() {
        return pv;
    }

    /**
     * Set the position/velocity measurement data.
     * @param pv position/velocity measurement data
     */
    public void setPv(final TutorialPV pv) {
        this.pv = pv;
    }

    /**
     * Get the position only measurement data.
     * @return the position only measurement data
     */
    public TutorialPosition getPosition() {
        return position;
    }

    /**
     * Set the position only measurement data.
     * @param position position only measurement data
     */
    public void setPosition(final TutorialPosition position) {
        this.position = position;
    }

    /** Range measurement. */
    public static class TutorialRange {

        /** Theoretical standard deviation. */
        private double sigma;

        /** Base weight. */
        private double weight;

        /**
         * Get the theoretical standard deviation of the measurement.
         * @return the theoretical standard deviation of the measurement
         */
        public double getSigma() {
            return sigma;
        }

        /**
         * Set the theoretical standard deviation of the measurement.
         * @param sigma theoretical standard deviation of the measurement
         */
        public void setSigma(final double sigma) {
            this.sigma = sigma;
        }

        /**
         * Get the measurement base weight.
         * @return the measurement base weight
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Set the measurement base weight.
         * @param weight measurement base weight
         */
        public void setWeight(final double weight) {
            this.weight = weight;
        }

    }

    /** Range rate measurement. */
    public static class TutorialRangeRate {

        /** Theoretical standard deviation. */
        private double sigma;

        /** Base weight. */
        private double weight;

        /**
         * Get the theoretical standard deviation of the measurement.
         * @return the theoretical standard deviation of the measurement
         */
        public double getSigma() {
            return sigma;
        }

        /**
         * Set the theoretical standard deviation of the measurement.
         * @param sigma theoretical standard deviation of the measurement
         */
        public void setSigma(final double sigma) {
            this.sigma = sigma;
        }

        /**
         * Get the measurement base weight.
         * @return the measurement base weight
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Set the measurement base weight.
         * @param weight measurement base weight
         */
        public void setWeight(final double weight) {
            this.weight = weight;
        }

    }

    /** Azimuth/Elevation measurement. */
    public static class TutorialAzEl {

        /** Theoretical standard deviation. */
        private double sigma;

        /** Base weight. */
        private double weight;

        /** Flag for the computation of the refraction correction. */
        private boolean withRefractionCorrection;

        /**
         * Get the theoretical standard deviation of the measurement.
         * @return the theoretical standard deviation of the measurement
         */
        public double getSigma() {
            return sigma;
        }

        /**
         * Set the theoretical standard deviation of the measurement.
         * @param sigma theoretical standard deviation of the measurement
         */
        public void setSigma(final double sigma) {
            this.sigma = sigma;
        }

        /**
         * Get the measurement base weight.
         * @return the measurement base weight
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Set the measurement base weight.
         * @param weight measurement base weight
         */
        public void setWeight(final double weight) {
            this.weight = weight;
        }

        /**
         * Get the flag for the computation of the refraction correction.
         * @return true if the refraction correction has to be computed
         */
        public boolean isWithRefractionCorrection() {
            return withRefractionCorrection;
        }

        /**
         * Set the flag for the computation of the refraction correction.
         * @param withRefractionCorrection true if the refraction correction has to be computed
         */
        public void setWithRefractionCorrection(final boolean withRefractionCorrection) {
            this.withRefractionCorrection = withRefractionCorrection;
        }

    }

    /** Position/Velocity measurement. */
    public static class TutorialPV {

        /** Theoretical standard deviation for position data. */
        private double sigmaPos;

        /** Theoretical standard deviation for velocity data. */
        private double sigmaVel;

        /** Base weight. */
        private double weight;

        /**
         * Get the theoretical standard deviation for position data.
         * @return the theoretical standard deviation for position data
         */
        public double getSigmaPos() {
            return sigmaPos;
        }

        /**
         * Set the theoretical standard deviation for position data.
         * @param sigmaPos theoretical standard deviation for position data
         */
        public void setSigmaPos(final double sigmaPos) {
            this.sigmaPos = sigmaPos;
        }

        /**
         * Get the theoretical standard deviation for velocity data.
         * @return the theoretical standard deviation for velocity data
         */
        public double getSigmaVel() {
            return sigmaVel;
        }

        /**
         * Set the theoretical standard deviation for velocity data.
         * @param sigmaVel theoretical standard deviation for velocity data
         */
        public void setSigmaVel(final double sigmaVel) {
            this.sigmaVel = sigmaVel;
        }

        /**
         * Get the measurement base weight.
         * @return the measurement base weight
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Set the measurement base weight.
         * @param weight measurement base weight
         */
        public void setWeight(final double weight) {
            this.weight = weight;
        }

    }

    /** Position only measurement. */
    public static class TutorialPosition {

        /** Theoretical standard deviation for position data. */
        private double sigmaPos;

        /** Base weight. */
        private double weight;

        /**
         * Get the theoretical standard deviation for position data.
         * @return the theoretical standard deviation for position data
         */
        public double getSigmaPos() {
            return sigmaPos;
        }

        /**
         * Set the theoretical standard deviation for position data.
         * @param sigmaPos theoretical standard deviation for position data
         */
        public void setSigmaPos(final double sigmaPos) {
            this.sigmaPos = sigmaPos;
        }

        /**
         * Get the measurement base weight.
         * @return the measurement base weight
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Set the measurement base weight.
         * @param weight measurement base weight
         */
        public void setWeight(final double weight) {
            this.weight = weight;
        }

    }

    /** Phase measurement. */
    public static class TutorialPhase {

        /** Theoretical standard deviation. */
        private double sigma;

        /** Base weight. */
        private double weight;

        /**
         * Get the theoretical standard deviation of the measurement.
         * @return the theoretical standard deviation of the measurement
         */
        public double getSigma() {
            return sigma;
        }

        /**
         * Set the theoretical standard deviation of the measurement.
         * @param sigma theoretical standard deviation of the measurement
         */
        public void setSigma(final double sigma) {
            this.sigma = sigma;
        }

        /**
         * Get the measurement base weight.
         * @return the measurement base weight
         */
        public double getWeight() {
            return weight;
        }

        /**
         * Set the measurement base weight.
         * @param weight measurement base weight
         */
        public void setWeight(final double weight) {
            this.weight = weight;
        }

    }

}
