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

package org.orekit.tutorials.estimation.common;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import org.orekit.estimation.measurements.EstimatedMeasurement;
import org.orekit.estimation.measurements.Range;

/** Logger for range measurements.
 * @author Luc Maisonobe
 */
class RangeLog extends MeasurementLog<Range> {

    /** Simple constructor.
     * @param home home directory
     * @param baseName output file base name (may be null)
     * @exception IOException if output file cannot be created
     */
    RangeLog(final File home, final String baseName) throws IOException {
        super(home, baseName, "range");
    }

    /** {@inheritDoc} */
    @Override
    void displayHeader() {
        getStream().format(Locale.US, STATION_HEADER_FORMAT,
                           "Epoch_UTC", "Station",
                           "Estimated_Range_m", "Observed_Range_m", "Range_Residual_m");
    }

    /** {@inheritDoc} */
    @Override
    void displayResidual(final EstimatedMeasurement<Range> evaluation) {
        final double[] theoretical = evaluation.getEstimatedValue();
        final double[] observed    = evaluation.getObservedMeasurement().getObservedValue();
        getStream().format(Locale.US, STATION_LINE_FORMAT,
                           evaluation.getDate().toString(),
                           evaluation.getObservedMeasurement().getStation().getBaseFrame().getName(),
                           theoretical[0], observed[0], residual(evaluation));
    }

    /** {@inheritDoc} */
    @Override
    double residual(final EstimatedMeasurement<Range> evaluation) {
        return evaluation.getEstimatedValue()[0] - evaluation.getObservedMeasurement().getObservedValue()[0];
    }

}
