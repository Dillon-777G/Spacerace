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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.estimation.measurements.EstimatedMeasurement;
import org.orekit.estimation.measurements.PV;

/** Logger for position measurements.
 * @author Luc Maisonobe
 */
class PositionLog extends MeasurementLog<PV> {

    /** Header format. */
    private static final String HEADER_FORMAT = DATE_FORMAT + SEP + // Date
                    NB_TAG_FORMAT + SEP + NB_TAG_FORMAT + SEP + NB_TAG_FORMAT + SEP + // Estimated
                    NB_TAG_FORMAT + SEP + NB_TAG_FORMAT + SEP + NB_TAG_FORMAT + SEP + // Observed
                    NB_TAG_FORMAT + NEW_LINE; // Residual

    /** Line format. */
    private static final String LINE_FORMAT = DATE_FORMAT + SEP + // Date
                    NB_FORMAT + SEP + NB_FORMAT + SEP + NB_FORMAT + SEP + // Estimated
                    NB_FORMAT + SEP + NB_FORMAT + SEP + NB_FORMAT + SEP + // Observed
                    NB_FORMAT + NEW_LINE; // Residual

    /** Simple constructor.
     * @param home home directory
     * @param baseName output file base name (may be null)
     * @exception IOException if output file cannot be created
     */
    PositionLog(final File home, final String baseName) throws IOException {
        super(home, baseName, "position");
    }

    /** {@inheritDoc} */
    @Override
    void displayHeader() {
        getStream().format(Locale.US, HEADER_FORMAT,
                           "Epoch_UTC",
                           "Estimated_X", "Estimated_Y", "Estimated_Z",
                           "Observed_X", "Observed_Y", "Observed_Z",
                           "DP_m");
    }

    /** {@inheritDoc} */
    @Override
    void displayResidual(final EstimatedMeasurement<PV> evaluation) {
        final double[] theoretical = evaluation.getEstimatedValue();
        final double[] observed    = evaluation.getObservedMeasurement().getObservedValue();
        getStream().format(Locale.US, LINE_FORMAT,
                           evaluation.getDate().toString(),
                           theoretical[0], theoretical[1], theoretical[2],
                           observed[0], observed[1], observed[2],
                           residual(evaluation));
    }

    /** {@inheritDoc} */
    @Override
    double residual(final EstimatedMeasurement<PV> evaluation) {
        final double[] theoretical = evaluation.getEstimatedValue();
        final double[] observed    = evaluation.getObservedMeasurement().getObservedValue();
        return Vector3D.distance(new Vector3D(theoretical[0], theoretical[1], theoretical[2]),
                                 new Vector3D(observed[0],    observed[1],    observed[2]));
    }

}
