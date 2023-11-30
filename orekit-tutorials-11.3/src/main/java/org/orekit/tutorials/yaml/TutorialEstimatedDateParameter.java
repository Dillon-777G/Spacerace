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

import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;

/**
 * Utility class for estimated date parameters during a maneuver estimation process.
 * <p>
 * Dates are read from a YAML file. Initial, minimum and maximum dates for the parameter
 * are stored into an AbsoluteDate array.
 * </p>
 * <p>NOTE: the minimum and maximum dates are automatically set by the algorithm to NEGATIVE and POSITIVE
 * infinity respectively, regardless the values provided in the input file.</p>
 */
public class TutorialEstimatedDateParameter {

    /** AbsoluteDate array containing the initial, minimum and maximum dates for the parameter. */
    private AbsoluteDate[] values;

    /** Flag for the date parameter estimation. */
    private boolean isEstimated;

    /**
     * Constructor.
     */
    public TutorialEstimatedDateParameter() {
        // Initialize empty array
        this.values = new AbsoluteDate[0];
    }

    /**
     * Get the initial, minimum and maximum dates for the parameter.
     * @return an AbsoluteDate array containing the initial, minimum and maximum dates for the parameter
     */
    public AbsoluteDate[] getValues() {
        return values.clone();
    }

    /**
     * Set the initial, minimum and maximum dates for the parameter.
     * @param values array containing the initial, minimum and maximum dates for the parameter
     */
    public void setValues(final String[] values) {
        this.values = new AbsoluteDate[values.length];
        for (int i = 0; i < values.length; i++) {
            this.values[i] = new AbsoluteDate(values[i], TimeScalesFactory.getUTC());
        }
    }

    /**
     * Get the initial value for the estimated date parameter.
     * @return the initial value for the estimated date parameter
     */
    public AbsoluteDate getInitialValue() {
        return values[0];
    }

    /**
     * Get the minimum allowed value for the date parameter.
     * @return the minimum allowed value for the date parameter
     */
    public AbsoluteDate getMinValue() {
        return values[1];
    }

    /**
     * Get the maximum allowed value for the date parameter.
     * @return the maximum allowed value for the date parameter
     */
    public AbsoluteDate getMaxValue() {
        return values[2];
    }

    /**
     * Get the flag for the date parameter estimation.
     * @return true if the date parameter is estimated
     */
    public boolean isEstimated() {
        return isEstimated;
    }

    /**
     * Set the flag for the date parameter estimation.
     * @param estimated true if the date parameter is estimated
     */
    public void setIsEstimated(final boolean estimated) {
        this.isEstimated = estimated;
    }

}
