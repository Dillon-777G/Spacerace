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
 * Initial data to initialize a tropospheric delay model.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialTroposphere {

    /** Flag to know if tropospheric model is estimated of not. */
    private boolean isEstimatedModel;

    /** Estimated tropospheric parameter. */
    private TutorialEstimatedParameter zenithDelay;

    /** Name of the tropospheric mapping function (NMF or GMF). */
    private String mappingFunction;

    /** Flag to know if an empirical model is used to compute station Temperature and Pressure. */
    private boolean withWeatherModel;

    /** Medium date used to initialize the time span tropospheric model. */
    private String correctionDate;

    /**
     * Get the flag to know if tropospheric model is estimated of not.
     * @return true if troposheric delay is computed with an estimated model
     */
    public boolean isEstimatedModel() {
        return isEstimatedModel;
    }

    /**
     * Set the flag to know if tropospheric model is estimated of not.
     * @param isEstimatedModel true if troposheric delay is computed with an estimated model
     */
    public void setIsEstimatedModel(final boolean isEstimatedModel) {
        this.isEstimatedModel = isEstimatedModel;
    }

    /**
     * Get the estimated troposheric parameter.
     * @return the estimated troposheric parameter.
     */
    public TutorialEstimatedParameter getZenithDelay() {
        return zenithDelay;
    }

    /**
     * Set the estimated troposheric parameter.
     * @param zenithDelay estimated troposheric parameter
     */
    public void setZenithDelay(final TutorialEstimatedParameter zenithDelay) {
        this.zenithDelay = zenithDelay;
    }

    /**
     * Get the name of the tropospheric mapping function.
     * @return the name of the tropospheric mapping function (NMF or GMF)
     */
    public String getMappingFunction() {
        return mappingFunction;
    }

    /**
     * Set the name of the tropospheric mapping function.
     * @param mappingFunction name of the tropospheric mapping function (NMF or GMF)
     */
    public void setMappingFunction(final String mappingFunction) {
        this.mappingFunction = mappingFunction;
    }

    /**
     * Get the flag to know if an empirical model is used
     * to compute station Temperature and Pressure.
     * @return true if an empirical model is used
     */
    public boolean isWithWeatherModel() {
        return withWeatherModel;
    }

    /**
     * Set the flag to know if an empirical model is used
     * to compute station Temperature and Pressure.
     * @param withWeatherModel true if an empirical model is used
     */
    public void setWithWeatherModel(final boolean withWeatherModel) {
        this.withWeatherModel = withWeatherModel;
    }

    /**
     * Get the medium date used to initialize the time span tropospheric model.
     * @return the medium date used to initialize the time span tropospheric model
     */
    public String getCorrectionDate() {
        return correctionDate;
    }

    /**
     * Set the medium date used to initialize the time span tropospheric model.
     * @param correctionDate medium date used to initialize the time span tropospheric model
     */
    public void setCorrectionDate(final String correctionDate) {
        this.correctionDate = correctionDate;
    }

}
