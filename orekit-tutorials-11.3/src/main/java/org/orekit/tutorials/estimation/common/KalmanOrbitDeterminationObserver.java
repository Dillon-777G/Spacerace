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

import java.io.PrintStream;
import java.util.Locale;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.util.FastMath;
import org.orekit.estimation.measurements.AngularAzEl;
import org.orekit.estimation.measurements.EstimatedMeasurement;
import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.estimation.measurements.PV;
import org.orekit.estimation.measurements.Range;
import org.orekit.estimation.measurements.RangeRate;
import org.orekit.estimation.sequential.KalmanEstimation;
import org.orekit.estimation.sequential.KalmanObserver;
import org.orekit.time.AbsoluteDate;
import org.orekit.utils.ParameterDriversList.DelegatingDriver;

/** Observer for the KalmanNumericalOrbitDetermination scheme. */
public class KalmanOrbitDeterminationObserver implements KalmanObserver {

    /** Format for parameters' names. */
    private static final String PAR_STR = "\t%20s";

    /** Format for parameters' values. */
    private static final String PAR_VAL = "\t%20.9f";

    /** Format for parameters' covariances. */
    private static final String PAR_COV = "\t%20.9e";

    /** Date of the first measurement.*/
    private AbsoluteDate t0;

    /** Log stream for printing the outputs. */
    private final PrintStream logStream;

    /** Log stream for printing range data. */
    private final RangeLog rangeLog;

    /** Log stream for printing range rate data. */
    private final RangeRateLog rangeRateLog;

    /** Log stream for printing azimuth data. */
    private final AzimuthLog   azimuthLog;

    /** Log stream for printing elevation data. */
    private final ElevationLog elevationLog;

    /** Log stream for printing position data. */
    private final PositionLog  positionLog;

    /** Log stream for printing velocity data. */
    private final VelocityLog  velocityLog;

    /** Constructor.
     * @param logStream log stream for printing the outputs
     * @param rangeLog log stream for printing range data
     * @param rangeRateLog log stream for printing range rate data
     * @param azimuthLog log stream for printing azimuth data
     * @param elevationLog log stream for printing elevation data
     * @param positionLog log stream for printing position data
     * @param velocityLog log stream for printing velocity data
     */
    public KalmanOrbitDeterminationObserver(final PrintStream logStream, final RangeLog rangeLog,
                                            final RangeRateLog rangeRateLog, final AzimuthLog azimuthLog,
                                            final ElevationLog elevationLog, final PositionLog  positionLog,
                                            final VelocityLog velocityLog) {
        this.logStream = logStream;
        this.rangeLog = rangeLog;
        this.rangeRateLog = rangeRateLog;
        this.azimuthLog = azimuthLog;
        this.elevationLog = elevationLog;
        this.positionLog = positionLog;
        this.velocityLog = velocityLog;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("unchecked")
    @Override
    public void evaluationPerformed(final KalmanEstimation estimation) {

        // Current measurement number, date and status
        final EstimatedMeasurement<?> estimatedMeasurement = estimation.getCorrectedMeasurement();
        final int currentNumber        = estimation.getCurrentMeasurementNumber();
        final AbsoluteDate currentDate = estimatedMeasurement.getDate();
        final EstimatedMeasurement.Status currentStatus = estimatedMeasurement.getStatus();

        // Current estimated measurement
        final ObservedMeasurement<?>  observedMeasurement  = estimatedMeasurement.getObservedMeasurement();

        // Measurement type & Station name
        String measType    = "";
        String stationName = "";

        // Register the measurement in the proper measurement logger
        logEvaluation(estimatedMeasurement);

        // Register the measurement in the proper measurement logger
        final String measurementType = observedMeasurement.getMeasurementType();
        if (measurementType.equals(Range.MEASUREMENT_TYPE)) {
            measType    = "RANGE";
            stationName =  ((EstimatedMeasurement<Range>) estimatedMeasurement).getObservedMeasurement().
                            getStation().getBaseFrame().getName();
        } else if (measurementType.equals(RangeRate.MEASUREMENT_TYPE)) {
            measType    = "RANGE_RATE";
            stationName =  ((EstimatedMeasurement<RangeRate>) estimatedMeasurement).getObservedMeasurement().
                            getStation().getBaseFrame().getName();
        } else if (measurementType.equals(AngularAzEl.MEASUREMENT_TYPE)) {
            measType    = "AZ_EL";
            stationName =  ((EstimatedMeasurement<AngularAzEl>) estimatedMeasurement).getObservedMeasurement().
                            getStation().getBaseFrame().getName();
        } else if (measurementType.equals(PV.MEASUREMENT_TYPE)) {
            measType    = "PV";
        }

        // Print data on terminal
        // ----------------------

        // Header
        if (currentNumber == 1) {
            // Set t0 to first measurement date
            t0 = currentDate;

            // Print header
            final String formatHeader = "%-4s\t%-25s\t%15s\t%-10s\t%-10s\t%-20s\t%20s\t%20s";
            String header = String.format(Locale.US, formatHeader,
                                          "Nb", "Epoch", "Dt[s]", "Status", "Type", "Station",
                                          "DP Corr", "DV Corr");
            // Orbital drivers
            for (DelegatingDriver driver : estimation.getEstimatedOrbitalParameters().getDrivers()) {
                header += String.format(Locale.US, PAR_STR, driver.getName());
                header += String.format(Locale.US, PAR_STR, "D" + driver.getName());
            }

            // Propagation drivers
            for (DelegatingDriver driver : estimation.getEstimatedPropagationParameters().getDrivers()) {
                header += String.format(Locale.US, PAR_STR, driver.getName());
                header += String.format(Locale.US, PAR_STR, "D" + driver.getName());
            }

            // Measurements drivers
            for (DelegatingDriver driver : estimation.getEstimatedMeasurementsParameters().getDrivers()) {
                header += String.format(Locale.US, PAR_STR, driver.getName());
                header += String.format(Locale.US, PAR_STR, "D" + driver.getName());
            }

            // Print header
            System.out.println(header);
        }

        // Print current measurement info in terminal
        String line = "";
        // Line format
        final String lineFormat = "%4d\t%-25s\t%15.3f\t%-10s\t%-10s\t%-20s\t%20.9e\t%20.9e";

        // Orbital correction = DP & DV between predicted orbit and estimated orbit
        final Vector3D predictedP = estimation.getPredictedSpacecraftStates()[0].getPVCoordinates().getPosition();
        final Vector3D predictedV = estimation.getPredictedSpacecraftStates()[0].getPVCoordinates().getVelocity();
        final Vector3D estimatedP = estimation.getCorrectedSpacecraftStates()[0].getPVCoordinates().getPosition();
        final Vector3D estimatedV = estimation.getCorrectedSpacecraftStates()[0].getPVCoordinates().getVelocity();
        final double DPcorr       = Vector3D.distance(predictedP, estimatedP);
        final double DVcorr       = Vector3D.distance(predictedV, estimatedV);

        final StringBuffer buffer = new StringBuffer();
        line = String.format(Locale.US, lineFormat,
                             currentNumber, currentDate.toString(),
                             currentDate.durationFrom(t0), currentStatus.toString(),
                             measType, stationName,
                             DPcorr, DVcorr);
        buffer.append(line);

        // Handle parameters printing (value and error)
        int jPar = 0;
        final RealMatrix Pest = estimation.getPhysicalEstimatedCovarianceMatrix();
        // Orbital drivers
        for (DelegatingDriver driver : estimation.getEstimatedOrbitalParameters().getDrivers()) {
            buffer.append(String.format(Locale.US, PAR_VAL, driver.getValue()));
            buffer.append(String.format(Locale.US, PAR_COV, FastMath.sqrt(Pest.getEntry(jPar, jPar))));
            jPar++;
        }
        // Propagation drivers
        for (DelegatingDriver driver : estimation.getEstimatedPropagationParameters().getDrivers()) {
            buffer.append(line += String.format(Locale.US, PAR_VAL, driver.getValue()));
            buffer.append(line += String.format(Locale.US, PAR_COV, FastMath.sqrt(Pest.getEntry(jPar, jPar))));
            jPar++;
        }
        // Measurements drivers
        for (DelegatingDriver driver : estimation.getEstimatedMeasurementsParameters().getDrivers()) {
            buffer.append(line += String.format(Locale.US, PAR_VAL, driver.getValue()));
            buffer.append(line += String.format(Locale.US, PAR_COV, FastMath.sqrt(Pest.getEntry(jPar, jPar))));
            jPar++;
        }

        // Print the line
        System.out.println(buffer.toString());
        if (logStream != null) {
            logStream.format(buffer.toString());
        }
    }

    /** Log evaluations.
     * @param evaluation measurement
     */
    private void logEvaluation(final EstimatedMeasurement<?> evaluation) {
        final String measurementType = evaluation.getObservedMeasurement().getMeasurementType();
        if (measurementType.equals(Range.MEASUREMENT_TYPE)) {
            @SuppressWarnings("unchecked")
            final EstimatedMeasurement<Range> ev = (EstimatedMeasurement<Range>) evaluation;
            if (rangeLog != null) {
                rangeLog.add(ev);
            }
        } else if (measurementType.equals(RangeRate.MEASUREMENT_TYPE)) {
            @SuppressWarnings("unchecked")
            final EstimatedMeasurement<RangeRate> ev = (EstimatedMeasurement<RangeRate>) evaluation;
            if (rangeRateLog != null) {
                rangeRateLog.add(ev);
            }
        } else if (measurementType.equals(AngularAzEl.MEASUREMENT_TYPE)) {
            @SuppressWarnings("unchecked")
            final EstimatedMeasurement<AngularAzEl> ev = (EstimatedMeasurement<AngularAzEl>) evaluation;
            if (azimuthLog != null) {
                azimuthLog.add(ev);
            }
            if (elevationLog != null) {
                elevationLog.add(ev);
            }
        } else if (measurementType.equals(PV.MEASUREMENT_TYPE)) {
            @SuppressWarnings("unchecked")
            final EstimatedMeasurement<PV> ev = (EstimatedMeasurement<PV>) evaluation;
            if (positionLog != null) {
                positionLog.add(ev);
            }
            if (velocityLog != null) {
                velocityLog.add(ev);
            }
        }
    }

}
