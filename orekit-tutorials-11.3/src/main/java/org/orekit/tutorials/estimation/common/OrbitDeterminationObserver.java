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
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresProblem;
import org.hipparchus.util.FastMath;
import org.orekit.estimation.leastsquares.BatchLSEstimator;
import org.orekit.estimation.leastsquares.BatchLSObserver;
import org.orekit.estimation.measurements.AngularAzEl;
import org.orekit.estimation.measurements.EstimatedMeasurement;
import org.orekit.estimation.measurements.EstimationsProvider;
import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.estimation.measurements.PV;
import org.orekit.estimation.measurements.Range;
import org.orekit.estimation.measurements.RangeRate;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.radiation.RadiationSensitive;
import org.orekit.orbits.Orbit;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.ParameterDriver;
import org.orekit.utils.ParameterDriversList;

/** Observer for the OrbitDetermination scheme. */
public class OrbitDeterminationObserver implements BatchLSObserver {

    /** Format of the beginning of the header line. */
    private static final String FORMAT_HEADER = "iteration evaluations      ΔP(m)        ΔV(m/s)           RMS          nb Range    nb Range-rate  nb Angular     nb PV  ";

    /** Format of the beginning of the first values' line. */
    private static final String FORMAT_0 = "    %2d         %2d                                 %16.12f     %s       %s     %s     %s";

    /** Format of the beginning of the other values' line. */
    private static final String FORMAT_L  = "    %2d         %2d      %13.6f %12.9f %16.12f     %s       %s     %s     %s";

    /** Separator between parameters. */
    private static final String SEP       = "  ";

    /** Format for parameters' names. */
    private static final String PAR_STR   = SEP + "%22s";

    /** Format for parameters' values. */
    private static final String PAR_VAL   = SEP + "%22.9f";

    /** String format (needed to avoid Checkstyle errors...). */
    private static final String STR       = "%s";

    /** New line format (needed to avoid Checkstyle errors...). */
    private static final String NEW_L     = "%n";

    /** Short name for SRP reflection coefficient. */
    private static final String SRP_CR    = "SRP-Cr";

    /** Short name for SRP absorption coefficient. */
    private static final String SRP_CA    = "SRP-Ca";

    /** Short name for drag coefficient. */
    private static final String DRAG_CD   = "Drag-Cd";

    /** Short name for drag sensitive lift ratio. */
    private static final String DRAG_CL   = "Drag-Cl";

    /** Previous PV value. */
    private PVCoordinates previousPV;

    /** Log stream for printing the outputs. */
    private final PrintStream logStream;

    /** Constructor.
     * @param initialGuess initial guess orbit
     * @param logStream log stream for printing the outputs
     * @param estimator estimator to observe
     */
    public OrbitDeterminationObserver(final Orbit initialGuess,
                                      final PrintStream logStream,
                                      final BatchLSEstimator estimator) {
        this.previousPV = initialGuess.getPVCoordinates();
        this.logStream  = logStream;
        String header = FORMAT_HEADER;
        header = addParametersNames(header,
                                    estimator.getOrbitalParametersDrivers(true),
                                    estimator.getPropagatorParametersDrivers(true),
                                    estimator.getMeasurementsParametersDrivers(true));
        System.out.format(Locale.US, header);
        if (logStream != null) {
            logStream.format(Locale.US, header);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void evaluationPerformed(final int iterationsCount, final int evaluationsCount,
                                   final Orbit[] orbits,
                                   final ParameterDriversList estimatedOrbitalParameters,
                                   final ParameterDriversList estimatedPropagatorParameters,
                                   final ParameterDriversList estimatedMeasurementsParameters,
                                   final EstimationsProvider  evaluationsProvider,
                                   final LeastSquaresProblem.Evaluation lspEvaluation) {
        final PVCoordinates currentPV = orbits[0].getPVCoordinates();
        final EvaluationCounter<Range>       rangeCounter     = new EvaluationCounter<Range>();
        final EvaluationCounter<RangeRate>   rangeRateCounter = new EvaluationCounter<RangeRate>();
        final EvaluationCounter<AngularAzEl> angularCounter   = new EvaluationCounter<AngularAzEl>();
        final EvaluationCounter<PV>          pvCounter        = new EvaluationCounter<PV>();
        for (int i = 0; i < evaluationsProvider.getNumber(); i++) {
            final EstimatedMeasurement<?> estimatedMeasurement = evaluationsProvider.getEstimatedMeasurement(i);
            final ObservedMeasurement<?>  observed             = estimatedMeasurement.getObservedMeasurement();
            final String measurementType = observed.getMeasurementType();
            if (measurementType.equals(Range.MEASUREMENT_TYPE)) {
                @SuppressWarnings("unchecked")
                final EstimatedMeasurement<Range> evaluation = (EstimatedMeasurement<Range>) estimatedMeasurement;
                rangeCounter.add(evaluation);
            } else if (measurementType.equals(RangeRate.MEASUREMENT_TYPE)) {
                @SuppressWarnings("unchecked")
                final EstimatedMeasurement<RangeRate> evaluation = (EstimatedMeasurement<RangeRate>) estimatedMeasurement;
                rangeRateCounter.add(evaluation);
            } else if (measurementType.equals(AngularAzEl.MEASUREMENT_TYPE)) {
                @SuppressWarnings("unchecked")
                final EstimatedMeasurement<AngularAzEl> evaluation = (EstimatedMeasurement<AngularAzEl>) estimatedMeasurement;
                angularCounter.add(evaluation);
            } else if (measurementType.equals(PV.MEASUREMENT_TYPE)) {
                @SuppressWarnings("unchecked")
                final EstimatedMeasurement<PV> evaluation = (EstimatedMeasurement<PV>) estimatedMeasurement;
                pvCounter.add(evaluation);
            }
        }
        // Print evaluation lines
        if (evaluationsCount == 1) {
            // Prepare first line
            String firstLine = String.format(Locale.US, FORMAT_0,
                                            iterationsCount, evaluationsCount,
                                            lspEvaluation.getRMS(),
                                            rangeCounter.format(8), rangeRateCounter.format(8),
                                            angularCounter.format(8), pvCounter.format(8));
            // Add parameter drivers' values
            firstLine = addParametersValues(firstLine,
                                           estimatedOrbitalParameters,
                                           estimatedPropagatorParameters,
                                           estimatedMeasurementsParameters);
            // Print first line
            System.out.format(firstLine);
            if (logStream != null) {
                logStream.format(firstLine);
            }
        } else {

            // Prepare line
            String line = String.format(Locale.US, FORMAT_L,
                                        iterationsCount, evaluationsCount,
                                        Vector3D.distance(previousPV.getPosition(), currentPV.getPosition()),
                                        Vector3D.distance(previousPV.getVelocity(), currentPV.getVelocity()),
                                        lspEvaluation.getRMS(),
                                        rangeCounter.format(8), rangeRateCounter.format(8),
                                        angularCounter.format(8), pvCounter.format(8));

            // Add parameter drivers' values
            line = addParametersValues(line,
                                       estimatedOrbitalParameters,
                                       estimatedPropagatorParameters,
                                       estimatedMeasurementsParameters);
            // Print line
            System.out.format(line);
            if (logStream != null) {
                logStream.format(line);
            }
        }
        previousPV = currentPV;
    }

    /** Add the parameters' names to a line.
     * @param in the string to write in
     * @param estimatedOrbitalParameters the estimated orbital parameters list
     * @param estimatedPropagatorParameters the estimated propagation parameters list
     * @param estimatedMeasurementsParameters the estimated measurements parameters list
     * @return the string with the parameters added
     */
    private String addParametersNames(final String in,
                                      final ParameterDriversList estimatedOrbitalParameters,
                                      final ParameterDriversList estimatedPropagatorParameters,
                                      final ParameterDriversList estimatedMeasurementsParameters) {
        // Initialize out string
        String out = in;

        // Add orbital drivers
        for (ParameterDriver driver: estimatedOrbitalParameters.getDrivers())
        {
            out = String.format(STR + PAR_STR, out, driver.getName());
        }

        // Add propagator parameters
        for (ParameterDriver driver: estimatedPropagatorParameters.getDrivers())
        {
            String driverName = driver.getName();

            // Replace with shorter name, if applicable
            driverName = driverName.replace(RadiationSensitive.REFLECTION_COEFFICIENT, SRP_CR);
            driverName = driverName.replace(RadiationSensitive.ABSORPTION_COEFFICIENT, SRP_CA);
            driverName = driverName.replace(DragSensitive.DRAG_COEFFICIENT, DRAG_CD);
            driverName = driverName.replace(DragSensitive.LIFT_RATIO, DRAG_CL);

            out = String.format(STR + PAR_STR, out, driverName);
        }

        // Add measurements parameters
        for (ParameterDriver driver: estimatedMeasurementsParameters.getDrivers())
        {
            out = String.format(STR + PAR_STR, out, driver.getName());
        }
        // Finalize line
        out = String.format(Locale.US, out + NEW_L);
        return out;
    }

    /** Add the parameters' values to a line.
     * @param in the string to write in
     * @param estimatedOrbitalParameters the estimated orbital parameters list
     * @param estimatedPropagatorParameters the estimated propagation parameters list
     * @param estimatedMeasurementsParameters the estimated measurements parameters list
     * @return the string with the propagation parameters added
     */
    private String addParametersValues(final String in,
                                       final ParameterDriversList estimatedOrbitalParameters,
                                       final ParameterDriversList estimatedPropagatorParameters,
                                       final ParameterDriversList estimatedMeasurementsParameters)
    {
        // Initialize out string
        String out = in;
        for (ParameterDriver driver: estimatedOrbitalParameters.getDrivers())
        {
            out = String.format(Locale.US, STR + PAR_VAL, out, driver.getValue());
        }

        for (ParameterDriver driver: estimatedPropagatorParameters.getDrivers())
        {
            out = String.format(Locale.US, STR + PAR_VAL, out, driver.getValue());
        }

        double driverValue;
        for (ParameterDriver driver: estimatedMeasurementsParameters.getDrivers())
        {
            // rad to deg conversion for angular biases
            if (driver.getName().contains(AbstractOrbitDetermination.AZIMUTH_BIAS_SUFFIX) ||
                driver.getName().contains(AbstractOrbitDetermination.ELEVATION_BIAS_SUFFIX))
            {
                driverValue = FastMath.toDegrees(driver.getValue());
            }
            else
            {
                driverValue = driver.getValue();
            }
            out = String.format(Locale.US, STR + PAR_VAL, out, driverValue);
        }
        // Finalize line
        out = String.format(Locale.US, out + NEW_L);
        return out;
    }
}
