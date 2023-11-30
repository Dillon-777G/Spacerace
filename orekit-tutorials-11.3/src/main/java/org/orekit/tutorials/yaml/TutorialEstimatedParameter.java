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
 * Utility class for estimated parameters during an orbit determination process.
 * <p>
 * Data are read from a YAML file. Initial, minimum and maximum value of the parameter
 * are stored into an array. Conventions are:
 * </p>
 * <table border="1" cellpadding="5">
 * <caption>Estimated parameter</caption>
 * <tr style="background-color: #ccccff;">
 * <th>Element</th><th>Value</th>
 * <tr><td style="background-color: #eeeeff;">0</td><td>Initial value</td>
 * <tr><td style="background-color: #eeeeff;">1</td><td>Minimum value</td>
 * <tr><td style="background-color: #eeeeff;">2</td><td>Maximum value</td>
 * </table>
 * @author Bryan Cazabonne
 */
public class TutorialEstimatedParameter {

    /** Initial, minimum and maximum value of the parameter. */
    private double[] values;

    /** Flag for the parameter estimation. */
    private boolean isEstimated;

    /**
     * Constructor.
     */
    public TutorialEstimatedParameter() {
        // initialise empty array
        this.values = new double[0];
    }

    /**
     * Get the initial, minimum and maximum value of the parameter.
     * @return an array containing the initial, minimum and maximum value of the parameter
     */
    public double[] getValues() {
        return values.clone();
    }

    /**
     * Set the initial, minimum and maximum value of the parameter.
     * @param values array containing the initial, minimum and maximum value of the parameter
     */
    public void setValues(final double[] values) {
        this.values = values.clone();
    }

    /**
     * Get the initial value for the estimated parameter.
     * @return the initial value for the estimated parameter
     */
    public double getInitialValue() {
        return values[0];
    }

    /**
     * Get the minimum allowed value for the parameter.
     * @return the minimum allowed value for the parameter
     */
    public double getMinValue() {
        return values[1];
    }

    /**
     * Get the maximum allowed value for the parameter.
     * @return the maximum allowed value for the parameter
     */
    public double getMaxValue() {
        return values[2];
    }

    /**
     * Get the flag for the parameter estimation.
     * @return true if the parameter is estimated
     */
    public boolean isEstimated() {
        return isEstimated;
    }

    /**
     * Set the flag for the parameter estimation.
     * @param estimated true if the parameter is estimated
     */
    public void setIsEstimated(final boolean estimated) {
        this.isEstimated = estimated;
    }

}
