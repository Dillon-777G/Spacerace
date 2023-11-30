/* Copyright 2002-2020 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.QRDecomposer;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.optim.nonlinear.vector.leastsquares.GaussNewtonOptimizer;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresOptimizer;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LevenbergMarquardtOptimizer;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.Precision;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataFilter;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DataSource;
import org.orekit.data.DirectoryCrawler;
import org.orekit.data.GzipFilter;
import org.orekit.data.UnixCompressFilter;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.estimation.leastsquares.BatchLSEstimator;
import org.orekit.estimation.measurements.AngularAzEl;
import org.orekit.estimation.measurements.EstimatedMeasurement;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.estimation.measurements.PV;
import org.orekit.estimation.measurements.Range;
import org.orekit.estimation.measurements.RangeRate;
import org.orekit.estimation.measurements.modifiers.AngularRadioRefractionModifier;
import org.orekit.estimation.measurements.modifiers.Bias;
import org.orekit.estimation.measurements.modifiers.DynamicOutlierFilter;
import org.orekit.estimation.measurements.modifiers.OnBoardAntennaRangeModifier;
import org.orekit.estimation.measurements.modifiers.OutlierFilter;
import org.orekit.estimation.measurements.modifiers.RangeIonosphericDelayModifier;
import org.orekit.estimation.measurements.modifiers.RangeRateIonosphericDelayModifier;
import org.orekit.estimation.measurements.modifiers.RangeTroposphericDelayModifier;
import org.orekit.estimation.measurements.modifiers.ShapiroRangeModifier;
import org.orekit.estimation.sequential.ConstantProcessNoise;
import org.orekit.estimation.sequential.KalmanEstimator;
import org.orekit.estimation.sequential.KalmanEstimatorBuilder;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.drag.IsotropicDrag;
import org.orekit.forces.radiation.IsotropicRadiationSingleCoefficient;
import org.orekit.forces.radiation.RadiationSensitive;
import org.orekit.frames.EOPHistory;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.gnss.HatanakaCompressFilter;
import org.orekit.gnss.MeasurementType;
import org.orekit.gnss.ObservationData;
import org.orekit.gnss.ObservationDataSet;
import org.orekit.gnss.RinexObservationLoader;
import org.orekit.gnss.SatelliteSystem;
import org.orekit.models.AtmosphericRefractionModel;
import org.orekit.models.earth.EarthITU453AtmosphereRefraction;
import org.orekit.models.earth.atmosphere.Atmosphere;
import org.orekit.models.earth.atmosphere.DTM2000;
import org.orekit.models.earth.atmosphere.data.MarshallSolarActivityFutureEstimation;
import org.orekit.models.earth.displacement.StationDisplacement;
import org.orekit.models.earth.ionosphere.EstimatedIonosphericModel;
import org.orekit.models.earth.ionosphere.IonosphericMappingFunction;
import org.orekit.models.earth.ionosphere.IonosphericModel;
import org.orekit.models.earth.ionosphere.KlobucharIonoCoefficientsLoader;
import org.orekit.models.earth.ionosphere.KlobucharIonoModel;
import org.orekit.models.earth.ionosphere.SingleLayerModelMappingFunction;
import org.orekit.models.earth.troposphere.DiscreteTroposphericModel;
import org.orekit.models.earth.troposphere.EstimatedTroposphericModel;
import org.orekit.models.earth.troposphere.GlobalMappingFunctionModel;
import org.orekit.models.earth.troposphere.MappingFunction;
import org.orekit.models.earth.troposphere.NiellMappingFunctionModel;
import org.orekit.models.earth.troposphere.SaastamoinenModel;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.CircularOrbit;
import org.orekit.orbits.EquinoctialOrbit;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.conversion.DormandPrince853IntegratorBuilder;
import org.orekit.propagation.conversion.ODEIntegratorBuilder;
import org.orekit.propagation.conversion.OrbitDeterminationPropagatorBuilder;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.tutorials.yaml.TutorialBatchLSEstimator;
import org.orekit.tutorials.yaml.TutorialBody;
import org.orekit.tutorials.yaml.TutorialEstimatedParameter;
import org.orekit.tutorials.yaml.TutorialForceModel;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialDrag;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialOceanTides;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialPolynomialAcceleration;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialSolarRadiationPressure;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialThirdBody;
import org.orekit.tutorials.yaml.TutorialIntegrator;
import org.orekit.tutorials.yaml.TutorialIonosphere;
import org.orekit.tutorials.yaml.TutorialKalman;
import org.orekit.tutorials.yaml.TutorialMeasurements;
import org.orekit.tutorials.yaml.TutorialMeasurements.TutorialAzEl;
import org.orekit.tutorials.yaml.TutorialMeasurements.TutorialPV;
import org.orekit.tutorials.yaml.TutorialMeasurements.TutorialRange;
import org.orekit.tutorials.yaml.TutorialMeasurements.TutorialRangeRate;
import org.orekit.tutorials.yaml.TutorialOptimizationEngine.TutorialLevenbergMarquardt;
import org.orekit.tutorials.yaml.TutorialOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialCartesianOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialCircularOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialEquinoctialOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialKeplerianOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialTLE;
import org.orekit.tutorials.yaml.TutorialPropagator;
import org.orekit.tutorials.yaml.TutorialSpacecraft;
import org.orekit.tutorials.yaml.TutorialStation;
import org.orekit.tutorials.yaml.TutorialStation.TutorialObservationType;
import org.orekit.tutorials.yaml.TutorialTroposphere;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.ParameterDriver;
import org.orekit.utils.ParameterDriversList;
import org.orekit.utils.ParameterDriversList.DelegatingDriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/** Base class for Orekit orbit determination tutorials.
 * @param <T> type of the propagator builder
 * @author Luc Maisonobe
 * @author Bryan Cazabonne
 */
public abstract class AbstractOrbitDeterminationEngine<T extends OrbitDeterminationPropagatorBuilder> {

    /** Suffix for azimuth bias. */
    static final String AZIMUTH_BIAS_SUFFIX = "/az bias";

    /** Suffix for elevation bias. */
    static final String ELEVATION_BIAS_SUFFIX = "/el bias";

    /** Prefix for IERS convention. */
    private static final String IERS = "IERS_";

    /** Suffix for the log file. */
    private static final String LOG_SUFFIX = "-log.out";

    /** Suffix for range bias. */
    private static final String RANGE_BIAS_SUFFIX = "/range bias";

    /** Suffix for range rate bias. */
    private static final String RANGE_RATE_BIAS_SUFFIX = "/range rate bias";

    /** Prefix for estimated orbit. */
    private static final String ESTIMATED_ORBIT = "Estimated orbit: ";

    /** Prefix for orbital parameters changes. */
    private static final String ORBITAL_CHANGES = "Estimated orbital parameters changes: ";

    /** Prefix for propagation parameters changes. */
    private static final String PROPAGATION_CHANGES = "Estimated propagation parameters changes: ";

    /** Prefix for measurements parameters changes. */
    private static final String MEASUREMENTS_CHANGES = "Estimated measurements parameters changes: ";

    /** Prefix for evaluations. */
    private static final String NUMBER_OF_EVALUATIONS = "Number of evaluations: ";

    /** Prefix for iterations. */
    private static final String NUMBER_OF_ITERATIONS = "Number of iterations: ";

    /** Key to print the wall clock run time. */
    private static final String WALL_CLOCK_RUN_TIME = "wall clock run time (s): ";

    /** Null double values are equal to 0.0 in YAML file. */
    private static final double NULL_DOUBLE = 0.0;

    /** Null integer values are equal to 0 in YAML files. */
    private static final int NULL_INT = 0;

    /** Create a gravity field from input parameters.
     * @param inputData input data
     * @throws NoSuchElementException if input parameters are missing
     */
    protected abstract void createGravityField(TutorialOrbitDetermination inputData)
        throws NoSuchElementException;

    /** Get the central attraction coefficient.
     * @return central attraction coefficient
     */
    protected abstract double getMu();

    /** Create a propagator builder from input parameters.
     * <p>
     * The advantage of using the DSST instead of the numerical
     * propagator is that it is possible to use greater values
     * for the minimum and maximum integration steps.
     * </p>
     * @param referenceOrbit reference orbit from which real orbits will be built
     * @param builder first order integrator builder
     * @param positionScale scaling factor used for orbital parameters normalization
     * (typically set to the expected standard deviation of the position)
     * @return propagator builder
     */
    protected abstract T createPropagatorBuilder(Orbit referenceOrbit,
                                                 ODEIntegratorBuilder builder,
                                                 double positionScale);

    /** Set satellite mass.
     * @param propagatorBuilder propagator builder
     * @param mass initial mass
     */
    protected abstract void setMass(T propagatorBuilder, double mass);

    /** Set gravity force model.
     * @param propagatorBuilder propagator builder
     * @param body central body
     * @return drivers for the force model
     */
    protected abstract List<ParameterDriver> setGravity(T propagatorBuilder, OneAxisEllipsoid body);

    /** Set third body attraction force model.
     * @param propagatorBuilder propagator builder
     * @param conventions IERS conventions to use
     * @param body central body
     * @param degree degree of the tide model to load
     * @param order order of the tide model to load
     * @return drivers for the force model
     */
    protected abstract List<ParameterDriver> setOceanTides(T propagatorBuilder, IERSConventions conventions,
                                                           OneAxisEllipsoid body, int degree, int order);

    /** Set third body attraction force model.
     * @param propagatorBuilder propagator builder
     * @param conventions IERS conventions to use
     * @param body central body
     * @param solidTidesBodies third bodies generating solid tides
     * @return drivers for the force model
     */
    protected abstract List<ParameterDriver> setSolidTides(T propagatorBuilder, IERSConventions conventions,
                                                           OneAxisEllipsoid body, CelestialBody[] solidTidesBodies);

    /** Set third body attraction force model.
     * @param propagatorBuilder propagator builder
     * @param thirdBody third body
     * @return drivers for the force model
     */
    protected abstract List<ParameterDriver> setThirdBody(T propagatorBuilder, CelestialBody thirdBody);

    /** Set drag force model.
     * @param propagatorBuilder propagator builder
     * @param atmosphere atmospheric model
     * @param spacecraft spacecraft model
     * @return drivers for the force model
     */
    protected abstract List<ParameterDriver> setDrag(T propagatorBuilder, Atmosphere atmosphere, DragSensitive spacecraft);

    /** Set solar radiation pressure force model.
     * @param propagatorBuilder propagator builder
     * @param sun Sun model
     * @param equatorialRadius central body equatorial radius (for shadow computation)
     * @param spacecraft spacecraft model
     * @return drivers for the force model
     */
    protected abstract List<ParameterDriver> setSolarRadiationPressure(T propagatorBuilder, CelestialBody sun,
                                                                       double equatorialRadius, RadiationSensitive spacecraft);

    /** Set relativity force model.
     * @param propagatorBuilder propagator builder
     * @return drivers for the force model
     */
    protected abstract List<ParameterDriver> setRelativity(T propagatorBuilder);

    /** Set polynomial acceleration force model.
     * @param propagatorBuilder propagator builder
     * @param name name of the acceleration
     * @param direction normalized direction of the acceleration
     * @param degree polynomial degree
     * @return drivers for the force model
     */
    protected abstract List<ParameterDriver> setPolynomialAcceleration(T propagatorBuilder, String name,
                                                                       Vector3D direction, int degree);

    /** Set attitude provider.
     * @param propagatorBuilder propagator builder
     * @param attitudeProvider attitude provider
     */
    protected abstract void setAttitudeProvider(T propagatorBuilder, AttitudeProvider attitudeProvider);

    /** Compare the estimated orbit with a reference orbit.
     * @param estimatedOrbit estimated orbit
     * @throws IOException Input file cannot be opened
     */
    protected abstract void compareWithReference(Orbit estimatedOrbit) throws IOException;

    /** Run the program.
     * <p>
     * This method uses a batch least squares algorithms
     * to perform the orbit determination.
     * </p>
     * @param input input file
     * @throws IOException if input files cannot be read
     */
    protected void run(final File input) throws IOException {

        final long t0 = System.currentTimeMillis();

        // initialize Orekit data
        final File home = initializeOrekitData(input);

        // read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialOrbitDetermination inputData = mapper.readValue(input, TutorialOrbitDetermination.class);

        // log file
        final String baseName;
        final PrintStream logStream;
        if (inputData.getOutputBaseName() != null) {
            baseName  = inputData.getOutputBaseName();
            logStream = new PrintStream(new File(home, baseName + LOG_SUFFIX), StandardCharsets.UTF_8.name());
        } else {
            baseName  = null;
            logStream = null;
        }

        final RangeLog     rangeLog     = new RangeLog(home, baseName);
        final RangeRateLog rangeRateLog = new RangeRateLog(home, baseName);
        final AzimuthLog   azimuthLog   = new AzimuthLog(home, baseName);
        final ElevationLog elevationLog = new ElevationLog(home, baseName);
        final PositionLog  positionLog  = new PositionLog(home, baseName);
        final VelocityLog  velocityLog  = new VelocityLog(home, baseName);

        try {

            // gravity field
            createGravityField(inputData);

            // Orbit initial guess
            final Orbit initialGuess = createOrbit(inputData, getMu());

            // IERS conventions
            final IERSConventions conventions = IERSConventions.valueOf(IERS + inputData.getBody().getIersConventionYear());

            // central body
            final OneAxisEllipsoid body = createBody(inputData);

            // propagator builder
            final T propagatorBuilder = configurePropagatorBuilder(inputData, conventions, body, initialGuess);

            // estimator
            final BatchLSEstimator estimator = createEstimator(inputData, propagatorBuilder);

            final Map<String, StationData>    stations                 = createStationsData(inputData, conventions, body);
            final PVData                      pvData                   = createPVData(inputData);
            final ObservableSatellite         satellite                = createObservableSatellite(inputData);
            final Bias<Range>                 satRangeBias             = createSatRangeBias(inputData);
            final OnBoardAntennaRangeModifier satAntennaRangeModifier  = createSatAntennaRangeModifier(inputData);
            final ShapiroRangeModifier        shapiroRangeModifier     = createShapiroRangeModifier(inputData);
            final Weights                     weights                  = createWeights(inputData);
            final OutlierFilter<Range>        rangeOutliersManager     = createRangeOutliersManager(inputData, false);
            final OutlierFilter<RangeRate>    rangeRateOutliersManager = createRangeRateOutliersManager(inputData, false);
            final OutlierFilter<AngularAzEl>  azElOutliersManager      = createAzElOutliersManager(inputData, false);
            final OutlierFilter<PV>           pvOutliersManager        = createPVOutliersManager(inputData, false);

            // measurements
            final List<ObservedMeasurement<?>> measurements = new ArrayList<>();
            for (final String fileName : inputData.getMeasurements().getMeasurementFiles()) {

                // set up filtering for measurements files
                DataSource nd = new DataSource(fileName, () -> new FileInputStream(new File(input.getParentFile(), fileName)));
                for (final DataFilter filter : Arrays.asList(new GzipFilter(),
                                                             new UnixCompressFilter(),
                                                             new HatanakaCompressFilter())) {
                    nd = filter.filter(nd);
                }

                if (Pattern.matches(RinexObservationLoader.DEFAULT_RINEX_2_SUPPORTED_NAMES, nd.getName()) ||
                    Pattern.matches(RinexObservationLoader.DEFAULT_RINEX_3_SUPPORTED_NAMES, nd.getName())) {
                    // the measurements come from a Rinex file
                    measurements.addAll(readRinex(nd,
                                                  inputData.getSpacecraft().getName(),
                                                  stations, satellite, satRangeBias, satAntennaRangeModifier, weights,
                                                  rangeOutliersManager, rangeRateOutliersManager, shapiroRangeModifier));
                } else {
                    // the measurements come from an Orekit custom file
                    measurements.addAll(readMeasurements(nd,
                                                         stations, pvData, satellite,
                                                         satRangeBias, satAntennaRangeModifier, weights,
                                                         rangeOutliersManager,
                                                         rangeRateOutliersManager,
                                                         azElOutliersManager,
                                                         pvOutliersManager));
                }

            }
            for (ObservedMeasurement<?> measurement : measurements) {
                estimator.addMeasurement(measurement);
            }

            // Set the observer and estimate orbit
            estimator.setObserver(new OrbitDeterminationObserver(initialGuess,
                                                                 logStream,
                                                                 estimator));
            final Orbit estimated = estimator.estimate()[0].getInitialState().getOrbit();

            // Compare with reference
            compareWithReference(estimated);

            // Compute some statistics
            for (final Map.Entry<ObservedMeasurement<?>, EstimatedMeasurement<?>> entry : estimator.getLastEstimations().entrySet()) {

                // Send to dedicated log depending on type
                final String measurementType = entry.getKey().getMeasurementType();
                if (measurementType.equals(Range.MEASUREMENT_TYPE)) {
                    @SuppressWarnings("unchecked")
                    final EstimatedMeasurement<Range> evaluation = (EstimatedMeasurement<Range>) entry.getValue();
                    rangeLog.add(evaluation);
                } else if (measurementType.equals(RangeRate.MEASUREMENT_TYPE)) {
                    @SuppressWarnings("unchecked")
                    final EstimatedMeasurement<RangeRate> evaluation = (EstimatedMeasurement<RangeRate>) entry.getValue();
                    rangeRateLog.add(evaluation);
                } else if (measurementType.equals(AngularAzEl.MEASUREMENT_TYPE)) {
                    @SuppressWarnings("unchecked")
                    final EstimatedMeasurement<AngularAzEl> evaluation = (EstimatedMeasurement<AngularAzEl>) entry.getValue();
                    azimuthLog.add(evaluation);
                    elevationLog.add(evaluation);
                } else if (measurementType.equals(PV.MEASUREMENT_TYPE)) {
                    @SuppressWarnings("unchecked")
                    final EstimatedMeasurement<PV> evaluation = (EstimatedMeasurement<PV>) entry.getValue();
                    positionLog.add(evaluation);
                    velocityLog.add(evaluation);
                }
            }

            System.out.println(ESTIMATED_ORBIT + estimated);
            if (logStream != null) {
                logStream.println(ESTIMATED_ORBIT + estimated);
            }

            final ParameterDriversList orbitalParameters      = estimator.getOrbitalParametersDrivers(true);
            final ParameterDriversList propagatorParameters   = estimator.getPropagatorParametersDrivers(true);
            final ParameterDriversList measurementsParameters = estimator.getMeasurementsParametersDrivers(true);
            int length = 0;
            for (final ParameterDriver parameterDriver : orbitalParameters.getDrivers()) {
                length = FastMath.max(length, parameterDriver.getName().length());
            }
            for (final ParameterDriver parameterDriver : propagatorParameters.getDrivers()) {
                length = FastMath.max(length, parameterDriver.getName().length());
            }
            for (final ParameterDriver parameterDriver : measurementsParameters.getDrivers()) {
                length = FastMath.max(length, parameterDriver.getName().length());
            }
            displayParametersChanges(System.out, ORBITAL_CHANGES,
                                     false, length, orbitalParameters);
            if (logStream != null) {
                displayParametersChanges(logStream, ORBITAL_CHANGES,
                                         false, length, orbitalParameters);
            }
            displayParametersChanges(System.out, PROPAGATION_CHANGES,
                                     true, length, propagatorParameters);
            if (logStream != null) {
                displayParametersChanges(logStream, PROPAGATION_CHANGES,
                                         true, length, propagatorParameters);
            }
            displayParametersChanges(System.out, MEASUREMENTS_CHANGES,
                                     true, length, measurementsParameters);
            if (logStream != null) {
                displayParametersChanges(logStream, MEASUREMENTS_CHANGES,
                                         true, length, measurementsParameters);
            }

            System.out.println(NUMBER_OF_ITERATIONS + estimator.getIterationsCount());
            System.out.println(NUMBER_OF_EVALUATIONS + estimator.getEvaluationsCount());
            rangeLog.displaySummary(System.out);
            rangeRateLog.displaySummary(System.out);
            azimuthLog.displaySummary(System.out);
            elevationLog.displaySummary(System.out);
            positionLog.displaySummary(System.out);
            velocityLog.displaySummary(System.out);
            if (logStream != null) {
                logStream.println(NUMBER_OF_ITERATIONS + estimator.getIterationsCount());
                logStream.println(NUMBER_OF_EVALUATIONS + estimator.getEvaluationsCount());
                rangeLog.displaySummary(logStream);
                rangeRateLog.displaySummary(logStream);
                azimuthLog.displaySummary(logStream);
                elevationLog.displaySummary(logStream);
                positionLog.displaySummary(logStream);
                velocityLog.displaySummary(logStream);
            }

            rangeLog.displayResiduals();
            rangeRateLog.displayResiduals();
            azimuthLog.displayResiduals();
            elevationLog.displayResiduals();
            positionLog.displayResiduals();
            velocityLog.displayResiduals();

        } finally {
            if (logStream != null) {
                logStream.close();
            }
            rangeLog.close();
            rangeRateLog.close();
            azimuthLog.close();
            elevationLog.close();
            positionLog.close();
            velocityLog.close();
        }

        final long t1 = System.currentTimeMillis();
        System.out.println(WALL_CLOCK_RUN_TIME + (0.001 * (t1 - t0)));

    }

    /** Run the program.
     * <p>
     * This method uses an Extended Kalman Filter
     * to perform the orbit determination.
     * </p>
     * @param input input file
     * @throws IOException if input files cannot be read
     */
    protected void runKalman(final File input) throws IOException {

        final long t0 = System.currentTimeMillis();

        // initialize Orekit data
        final File home = initializeOrekitData(input);

        // read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialOrbitDetermination inputData = mapper.readValue(input, TutorialOrbitDetermination.class);

        // log file
        final String baseName;
        final PrintStream logStream;
        if (inputData.getOutputBaseName() != null) {
            baseName  = inputData.getOutputBaseName();
            logStream = new PrintStream(new File(home, baseName + LOG_SUFFIX), StandardCharsets.UTF_8.name());
        } else {
            baseName  = null;
            logStream = null;
        }

        final RangeLog     rangeLog     = new RangeLog(home, baseName);
        final RangeRateLog rangeRateLog = new RangeRateLog(home, baseName);
        final AzimuthLog   azimuthLog   = new AzimuthLog(home, baseName);
        final ElevationLog elevationLog = new ElevationLog(home, baseName);
        final PositionLog  positionLog  = new PositionLog(home, baseName);
        final VelocityLog  velocityLog  = new VelocityLog(home, baseName);

        try {

            // gravity field
            createGravityField(inputData);

            // Orbit initial guess
            final Orbit initialGuess = createOrbit(inputData, getMu());

            // IERS conventions
            final IERSConventions conventions = IERSConventions.valueOf(IERS + inputData.getBody().getIersConventionYear());

            // central body
            final OneAxisEllipsoid body = createBody(inputData);

            // propagator builder
            final T propagatorBuilder = configurePropagatorBuilder(inputData, conventions, body, initialGuess);

            final Map<String, StationData>    stations                 = createStationsData(inputData, conventions, body);
            final PVData                      pvData                   = createPVData(inputData);
            final ObservableSatellite         satellite                = createObservableSatellite(inputData);
            final Bias<Range>                 satRangeBias             = createSatRangeBias(inputData);
            final OnBoardAntennaRangeModifier satAntennaRangeModifier  = createSatAntennaRangeModifier(inputData);
            final ShapiroRangeModifier        shapiroRangeModifier     = createShapiroRangeModifier(inputData);
            final Weights                     weights                  = createWeights(inputData);
            final OutlierFilter<Range>        rangeOutliersManager     = createRangeOutliersManager(inputData, true);
            final OutlierFilter<RangeRate>    rangeRateOutliersManager = createRangeRateOutliersManager(inputData, true);
            final OutlierFilter<AngularAzEl>  azElOutliersManager      = createAzElOutliersManager(inputData, true);
            final OutlierFilter<PV>           pvOutliersManager        = createPVOutliersManager(inputData, true);

            // measurements
            final List<ObservedMeasurement<?>> measurements = new ArrayList<>();
            for (final String fileName : inputData.getMeasurements().getMeasurementFiles()) {

                // set up filtering for measurements files
                DataSource nd = new DataSource(fileName, () -> new FileInputStream(new File(input.getParentFile(), fileName)));
                for (final DataFilter filter : Arrays.asList(new GzipFilter(),
                                                             new UnixCompressFilter(),
                                                             new HatanakaCompressFilter())) {
                    nd = filter.filter(nd);
                }

                if (Pattern.matches(RinexObservationLoader.DEFAULT_RINEX_2_SUPPORTED_NAMES, nd.getName()) ||
                    Pattern.matches(RinexObservationLoader.DEFAULT_RINEX_3_SUPPORTED_NAMES, nd.getName())) {
                    // the measurements come from a Rinex file
                    measurements.addAll(readRinex(nd,
                                                  inputData.getSpacecraft().getName(),
                                                  stations, satellite, satRangeBias, satAntennaRangeModifier, weights,
                                                  rangeOutliersManager, rangeRateOutliersManager, shapiroRangeModifier));
                } else {
                    // the measurements come from an Orekit custom file
                    measurements.addAll(readMeasurements(nd,
                                                         stations, pvData, satellite,
                                                         satRangeBias, satAntennaRangeModifier, weights,
                                                         rangeOutliersManager,
                                                         rangeRateOutliersManager,
                                                         azElOutliersManager,
                                                         pvOutliersManager));
                }

            }

            // Building the Kalman filter:
            // - Gather the estimated measurement parameters in a list
            // - Prepare the initial covariance matrix and the process noise matrix
            // - Build the Kalman filter
            // --------------------------------------------------------------------

            // Kalman filter data
            final TutorialKalman kalmanData = inputData.getKalman();

            // Build the list of estimated measurements
            final ParameterDriversList estimatedMeasurementsParameters = new ParameterDriversList();
            for (ObservedMeasurement<?> measurement : measurements) {
                final List<ParameterDriver> drivers = measurement.getParametersDrivers();
                for (ParameterDriver driver : drivers) {
                    if (driver.isSelected()) {
                        // Add the driver
                        estimatedMeasurementsParameters.add(driver);
                    }
                }
            }
            // Sort the list lexicographically
            estimatedMeasurementsParameters.sort();

            // Initial orbital Cartesian covariance matrix
            // These covariances are derived from the deltas between initial and reference orbits

            // Initial cartesian covariance matrix and process noise matrix
            final RealMatrix cartesianOrbitalP = MatrixUtils.createRealDiagonalMatrix(kalmanData.getCartesianOrbitalP());
            final RealMatrix cartesianOrbitalQ = MatrixUtils.createRealDiagonalMatrix(kalmanData.getCartesianOrbitalQ());

            // Initial propagation covariance matrix and process noise matrix
            final RealMatrix propagationP = (kalmanData.getPropagationP().length) != 0 ?
                                                                MatrixUtils.createRealDiagonalMatrix(kalmanData.getPropagationP()) : null;
            final RealMatrix propagationQ = (kalmanData.getPropagationQ().length) != 0 ?
                                                                MatrixUtils.createRealDiagonalMatrix(kalmanData.getPropagationQ()) : null;

            // Initial measurement covariance matrix and process noise matrix
            final RealMatrix measurementP = (kalmanData.getMeasurementP().length) != 0 ?
                                                                MatrixUtils.createRealDiagonalMatrix(kalmanData.getMeasurementP()) : null;
            final RealMatrix measurementQ = (kalmanData.getMeasurementQ().length) != 0 ?
                                                                MatrixUtils.createRealDiagonalMatrix(kalmanData.getMeasurementQ()) : null;

            // Orbital covariance matrix initialization
            // Jacobian of the orbital parameters w/r to Cartesian
            final double[][] dYdC = new double[6][6];
            initialGuess.getJacobianWrtCartesian(propagatorBuilder.getPositionAngle(), dYdC);
            final RealMatrix Jac = MatrixUtils.createRealMatrix(dYdC);
            final RealMatrix orbitalP = Jac.multiply(cartesianOrbitalP.multiply(Jac.transpose()));

            // Orbital process noise matrix
            final RealMatrix orbitalQ = Jac.multiply(cartesianOrbitalQ.multiply(Jac.transpose()));

            // Build the full covariance matrix and process noise matrix
            final int nbPropag = (propagationP != null) ? propagationP.getRowDimension() : 0;
            final RealMatrix initialP = MatrixUtils.createRealMatrix(6 + nbPropag,
                                                                     6 + nbPropag);
            final RealMatrix Q = MatrixUtils.createRealMatrix(6 + nbPropag,
                                                              6 + nbPropag);
         // Orbital part
            initialP.setSubMatrix(orbitalP.getData(), 0, 0);
            Q.setSubMatrix(orbitalQ.getData(), 0, 0);

            // Orbital part
            initialP.setSubMatrix(orbitalP.getData(), 0, 0);
            Q.setSubMatrix(orbitalQ.getData(), 0, 0);

            // Propagation part
            if (propagationP != null) {
                initialP.setSubMatrix(propagationP.getData(), 6, 6);
                Q.setSubMatrix(propagationQ.getData(), 6, 6);
            }

            // Build the Kalman
            final KalmanEstimatorBuilder kalmanBuilder = new KalmanEstimatorBuilder().
                            addPropagationConfiguration(propagatorBuilder, new ConstantProcessNoise(initialP, Q));
            if (measurementP != null) {
                // Measurement part
                kalmanBuilder.estimatedMeasurementsParameters(estimatedMeasurementsParameters, new ConstantProcessNoise(measurementP, measurementQ));
            }
            final KalmanEstimator kalman = kalmanBuilder.build();
            kalman.setObserver(new KalmanOrbitDeterminationObserver(logStream, rangeLog, rangeRateLog,
                                                                    azimuthLog, elevationLog, positionLog,
                                                                    velocityLog));

            // Process the list measurements
            final Orbit estimated = kalman.processMeasurements(measurements)[0].getInitialState().getOrbit();

            // Compare with reference
            compareWithReference(estimated);

            System.out.println(ESTIMATED_ORBIT + estimated);
            if (logStream != null) {
                logStream.println(ESTIMATED_ORBIT + estimated);
            }

            final ParameterDriversList orbitalParameters      = kalman.getOrbitalParametersDrivers(true);
            final ParameterDriversList propagatorParameters   = kalman.getPropagationParametersDrivers(true);
            final ParameterDriversList measurementsParameters = kalman.getEstimatedMeasurementsParameters();
            int length = 0;
            for (final ParameterDriver parameterDriver : orbitalParameters.getDrivers()) {
                length = FastMath.max(length, parameterDriver.getName().length());
            }
            for (final ParameterDriver parameterDriver : propagatorParameters.getDrivers()) {
                length = FastMath.max(length, parameterDriver.getName().length());
            }
            for (final ParameterDriver parameterDriver : measurementsParameters.getDrivers()) {
                length = FastMath.max(length, parameterDriver.getName().length());
            }
            displayParametersChanges(System.out, ORBITAL_CHANGES,
                                     false, length, orbitalParameters);
            if (logStream != null) {
                displayParametersChanges(logStream, ORBITAL_CHANGES,
                                         false, length, orbitalParameters);
            }
            displayParametersChanges(System.out, PROPAGATION_CHANGES,
                                     true, length, propagatorParameters);
            if (logStream != null) {
                displayParametersChanges(logStream, PROPAGATION_CHANGES,
                                         true, length, propagatorParameters);
            }
            displayParametersChanges(System.out, MEASUREMENTS_CHANGES,
                                     true, length, measurementsParameters);
            if (logStream != null) {
                displayParametersChanges(logStream, MEASUREMENTS_CHANGES,
                                         true, length, measurementsParameters);
            }

            rangeLog.displaySummary(System.out);
            rangeRateLog.displaySummary(System.out);
            azimuthLog.displaySummary(System.out);
            elevationLog.displaySummary(System.out);
            positionLog.displaySummary(System.out);
            velocityLog.displaySummary(System.out);
            if (logStream != null) {
                rangeLog.displaySummary(logStream);
                rangeRateLog.displaySummary(logStream);
                azimuthLog.displaySummary(logStream);
                elevationLog.displaySummary(logStream);
                positionLog.displaySummary(logStream);
                velocityLog.displaySummary(logStream);
            }

            rangeLog.displayResiduals();
            rangeRateLog.displayResiduals();
            azimuthLog.displayResiduals();
            elevationLog.displayResiduals();
            positionLog.displayResiduals();
            velocityLog.displayResiduals();

        } finally {
            if (logStream != null) {
                logStream.close();
            }
            rangeLog.close();
            rangeRateLog.close();
            azimuthLog.close();
            elevationLog.close();
            positionLog.close();
            velocityLog.close();
        }

        final long t1 = System.currentTimeMillis();
        System.out.println(WALL_CLOCK_RUN_TIME + (0.001 * (t1 - t0)));

    }

    /**
     * Use the physical models in the input file.
     * Incorporate the initial reference values
     * And run the propagation until the last measurement to get the reference orbit at the same date
     * as the Kalman filter
     * @param input Input configuration file
     * @param refPosition Initial reference position
     * @param refVelocity Initial reference velocity
     * @param refPropagationParameters Reference propagation parameters
     * @param finalDate The final date to usefinal dateame date as the Kalman filter
     * @return reference orbit at the same data as the Kalman Filter
     * @throws IOException Input file cannot be opened
     */
    protected Orbit runReference(final File input,
                                 final Vector3D refPosition, final Vector3D refVelocity,
                                 final ParameterDriversList refPropagationParameters,
                                 final AbsoluteDate finalDate) throws IOException {

        // read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialOrbitDetermination inputData = mapper.readValue(input, TutorialOrbitDetermination.class);

        // Gravity field
        createGravityField(inputData);

        // Orbit initial guess
        final TutorialOrbit orbitData = inputData.getOrbit();
        final Orbit initialRefOrbit = new CartesianOrbit(new PVCoordinates(refPosition, refVelocity),
                                                        orbitData.getInertialFrame(),
                                                        new AbsoluteDate(orbitData.getDate(),
                                                                         TimeScalesFactory.getUTC()),
                                                         getMu());

        // IERS conventions
        final IERSConventions conventions = IERSConventions.valueOf(IERS + inputData.getBody().getIersConventionYear());

        // Central body
        final OneAxisEllipsoid body = createBody(inputData);

        // Propagator builder
        final T propagatorBuilder = configurePropagatorBuilder(inputData, conventions, body, initialRefOrbit);

        // Force the selected propagation parameters to their reference values
        if (refPropagationParameters != null) {
            for (DelegatingDriver refDriver : refPropagationParameters.getDrivers()) {
                for (DelegatingDriver driver : propagatorBuilder.getPropagationParametersDrivers().getDrivers()) {
                    if (driver.getName().equals(refDriver.getName())) {
                        driver.setValue(refDriver.getValue());
                    }
                }
            }
        }

        // Build the reference propagator
        final Propagator propagator =
                        propagatorBuilder.buildPropagator(propagatorBuilder.
                                                          getSelectedNormalizedParameters());

        // Propagate until last date and return the orbit
        return propagator.propagate(finalDate).getOrbit();

    }

    /** Display parameters changes.
     * @param out output stream
     * @param header header message
     * @param sort if true, parameters will be sorted lexicographically
     * @param length max length of parameters names
     * @param parameters parameters list
     */
    private void displayParametersChanges(final PrintStream out, final String header, final boolean sort,
                                          final int length, final ParameterDriversList parameters) {

        final List<ParameterDriver> list = new ArrayList<>(parameters.getDrivers());
        if (sort) {
            // sort the parameters lexicographically
            Collections.sort(list, new Comparator<ParameterDriver>() {
                /** {@inheritDoc} */
                @Override
                public int compare(final ParameterDriver pd1, final ParameterDriver pd2) {
                    return pd1.getName().compareTo(pd2.getName());
                }

            });
        }

        out.println(header);
        int index = 0;
        for (final ParameterDriver parameter : list) {
            if (parameter.isSelected()) {
                final double factor;
                if (parameter.getName().endsWith(AZIMUTH_BIAS_SUFFIX) ||
                    parameter.getName().endsWith(ELEVATION_BIAS_SUFFIX)) {
                    factor = FastMath.toDegrees(1.0);
                } else {
                    factor = 1.0;
                }
                final double initial = parameter.getReferenceValue();
                final double value   = parameter.getValue();
                out.format(Locale.US, "  %2d %s", ++index, parameter.getName());
                for (int i = parameter.getName().length(); i < length; ++i) {
                    out.format(Locale.US, " ");
                }
                out.format(Locale.US, "  %+.12f  (final value:  % .12f)%n",
                           factor * (value - initial), factor * value);
            }
        }

    }

    /** Create a propagator builder from input parameters.
     * <p>
     * The advantage of using the DSST instead of the numerical
     * propagator is that it is possible to use greater values
     * for the minimum and maximum integration steps.
     * </p>
     * @param inputData input data
     * @param conventions IERS conventions to use
     * @param body central body
     * @param orbit first orbit estimate
     * @return propagator builder
     * @throws NoSuchElementException if input parameters are missing
     */
    private T configurePropagatorBuilder(final TutorialOrbitDetermination inputData,
                                         final IERSConventions conventions,
                                         final OneAxisEllipsoid body,
                                         final Orbit orbit)
        throws NoSuchElementException {

        // propagator data
        final TutorialPropagator propagator = inputData.getPropagator();

        // integrator data
        final TutorialIntegrator integrator = propagator.getIntegrator();
        final double minStep;
        if (integrator.getMinStep() != NULL_DOUBLE) {
            minStep = integrator.getMinStep();
        } else {
            minStep = 6000.0;
        }

        final double maxStep;
        if (integrator.getMaxStep() != NULL_DOUBLE) {
            maxStep = integrator.getMaxStep();
        } else {
            maxStep = 86400;
        }

        final double dP;
        if (integrator.getPositionError() != NULL_DOUBLE) {
            dP = integrator.getPositionError();
        } else {
            dP = 10.0;
        }

        final double positionScale;
        if (inputData.getEstimator() != null && inputData.getEstimator().getOrbitalParametersPositionScale() != NULL_DOUBLE) {
            positionScale = inputData.getEstimator().getOrbitalParametersPositionScale();
        } else {
            positionScale = dP;
        }

        final T propagatorBuilder = createPropagatorBuilder(orbit,
                                                            new DormandPrince853IntegratorBuilder(minStep, maxStep, dP),
                                                            positionScale);

        // initial mass
        final double mass;
        if (inputData.getSpacecraft().getMass() != NULL_DOUBLE) {
            mass = inputData.getSpacecraft().getMass();
        } else {
            mass = 1000.0;
        }
        setMass(propagatorBuilder, mass);

        setGravity(propagatorBuilder, body);

        // force models data
        final TutorialForceModel forceModels = propagator.getForceModels();

        // third body attraction with solid tides force model
        final List<CelestialBody> solidTidesBodies = new ArrayList<>();
        for (TutorialThirdBody thirdBody : forceModels.getThirdBody()) {
            setThirdBody(propagatorBuilder, CelestialBodyFactory.getBody(thirdBody.getName()));
            if (thirdBody.isWithSolidTides()) {
                solidTidesBodies.add(CelestialBodyFactory.getBody(thirdBody.getName()));
            }
        }
        if (!solidTidesBodies.isEmpty()) {
            setSolidTides(propagatorBuilder, conventions, body,
                          solidTidesBodies.toArray(new CelestialBody[solidTidesBodies.size()]));
        }

        // ocean tides force model
        if (forceModels.getOceanTides() != null) {
            final TutorialOceanTides oceanTides = forceModels.getOceanTides();
            final int                degree     = oceanTides.getDegree();
            final int                order      = oceanTides.getOrder();
            if (degree > 0 && order > 0) {
                setOceanTides(propagatorBuilder, conventions, body, degree, order);
            }
        }

        // drag
        if (forceModels.getDrag() != null) {
            final TutorialDrag drag        = forceModels.getDrag();
            final double       cd          = drag.getCd().getInitialValue();
            final double       area        = drag.getArea();
            final boolean      cdEstimated = drag.getCd().isEstimated();

            final MarshallSolarActivityFutureEstimation msafe =
                            new MarshallSolarActivityFutureEstimation(MarshallSolarActivityFutureEstimation.DEFAULT_SUPPORTED_NAMES,
                                                                      MarshallSolarActivityFutureEstimation.StrengthLevel.AVERAGE);
            final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
            manager.feed(msafe.getSupportedNames(), msafe);
            final Atmosphere atmosphere = new DTM2000(msafe, CelestialBodyFactory.getSun(), body);
            final List<ParameterDriver> drivers = setDrag(propagatorBuilder, atmosphere, new IsotropicDrag(area, cd));
            if (cdEstimated) {
                for (final ParameterDriver driver : drivers) {
                    if (driver.getName().equals(DragSensitive.DRAG_COEFFICIENT)) {
                        driver.setSelected(true);
                    }
                }
            }
        }

        // solar radiation pressure
        if (forceModels.getSolarRadiationPressure() != null) {
            final TutorialSolarRadiationPressure srp = forceModels.getSolarRadiationPressure();
            final double  cr          = srp.getCr().getInitialValue();
            final double  area        = srp.getArea();
            final boolean cREstimated = srp.getCr().isEstimated();
            final List<ParameterDriver> drivers = setSolarRadiationPressure(propagatorBuilder, CelestialBodyFactory.getSun(),
                                                                            body.getEquatorialRadius(),
                                                                            new IsotropicRadiationSingleCoefficient(area, cr));
            if (cREstimated) {
                for (final ParameterDriver driver : drivers) {
                    if (driver.getName().equals(RadiationSensitive.REFLECTION_COEFFICIENT)) {
                        driver.setSelected(true);
                    }
                }
            }
        }

        // post-Newtonian correction force due to general relativity
        if (forceModels.getRelativity() != null) {
            setRelativity(propagatorBuilder);
        }

        // extra polynomial accelerations
        if (forceModels.getPolynomialAcceleration() != null &&
                        !forceModels.getPolynomialAcceleration().isEmpty()) {
            // loop on polynomial acceleration models
            for (TutorialPolynomialAcceleration tpa : forceModels.getPolynomialAcceleration()) {
                final String   name         = tpa.getName();
                final Vector3D direction    = tpa.getAccelerationDirection();
                final double[] coefficients = tpa.getCoefficients();
                final boolean  estimated    = tpa.isEstimated();

                final List<ParameterDriver> drivers = setPolynomialAcceleration(propagatorBuilder, name, direction, coefficients.length - 1);
                for (int k = 0; k < coefficients.length; ++k) {
                    final String coefficientName = name + "[" + k + "]";
                    for (final ParameterDriver driver : drivers) {
                        if (driver.getName().equals(coefficientName)) {
                            driver.setValue(coefficients[k]);
                            driver.setSelected(estimated);
                        }
                    }
                }
            }
        }

        // attitude mode
        final AttitudeMode mode;
        if (inputData.getSpacecraft().getAttitudeMode() != null) {
            mode = AttitudeMode.valueOf(inputData.getSpacecraft().getAttitudeMode());
        } else {
            mode = AttitudeMode.NADIR_POINTING_WITH_YAW_COMPENSATION;
        }
        setAttitudeProvider(propagatorBuilder, mode.getProvider(orbit.getFrame(), body));

        return propagatorBuilder;

    }

    /** Create central body from input parameters.
     * @param inputData input data
     * @return central body
     * @throws NoSuchElementException if input parameters are missing
     */
    private OneAxisEllipsoid createBody(final TutorialOrbitDetermination inputData)
        throws NoSuchElementException {

        // body data
        final TutorialBody body = inputData.getBody();

        final Frame bodyFrame;
        if (body.getFrameName() != null) {
            bodyFrame = body.getEarthFrame();
        } else {
            bodyFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        }

        final double equatorialRadius;
        if (body.getEquatorialRadius() != NULL_DOUBLE) {
            equatorialRadius = body.getEquatorialRadius();
        } else {
            equatorialRadius = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        }

        final double flattening;
        if (body.getInverseFlattening() != NULL_DOUBLE) {
            flattening = 1.0 / body.getInverseFlattening();
        } else {
            flattening = Constants.WGS84_EARTH_FLATTENING;
        }

        return new OneAxisEllipsoid(equatorialRadius, flattening, bodyFrame);
    }

    /** Create an orbit from input parameters.
     * @param inputData input data
     * @param mu central attraction coefficient
     * @return orbit
     * @throws NoSuchElementException if input parameters are missing
     */
    private Orbit createOrbit(final TutorialOrbitDetermination inputData, final double mu)
        throws NoSuchElementException {

        // orbit data
        final TutorialOrbit     orbit = inputData.getOrbit();
        final TutorialOrbitType data  = orbit.getOrbitType();

        // inertial frame
        final Frame frame = orbit.getInertialFrame() == null ? FramesFactory.getEME2000() : orbit.getInertialFrame();

        // orbit definition
        if (data.getKeplerian() != null) {
            final TutorialKeplerianOrbit keplerian = data.getKeplerian();
            return new KeplerianOrbit(keplerian.getA(),
                                      keplerian.getE(),
                                      FastMath.toRadians(keplerian.getI()),
                                      FastMath.toRadians(keplerian.getPa()),
                                      FastMath.toRadians(keplerian.getRaan()),
                                      FastMath.toRadians(keplerian.getV()),
                                      PositionAngle.valueOf(keplerian.getPositionAngle()),
                                      frame,
                                      new AbsoluteDate(orbit.getDate(),
                                                     TimeScalesFactory.getUTC()),
                                      mu);
        } else if (data.getEquinoctial() != null) {
            final TutorialEquinoctialOrbit equinoctial = data.getEquinoctial();
            return new EquinoctialOrbit(equinoctial.getA(),
                                        equinoctial.getEx(),
                                        equinoctial.getEy(),
                                        equinoctial.getHx(),
                                        equinoctial.getHy(),
                                        FastMath.toRadians(equinoctial.getLv()),
                                        PositionAngle.valueOf(equinoctial.getPositionAngle()),
                                        frame,
                                        new AbsoluteDate(orbit.getDate(),
                                                         TimeScalesFactory.getUTC()),
                                        mu);
        } else if (data.getCircular() != null) {
            final TutorialCircularOrbit circular = data.getCircular();
            return new CircularOrbit(circular.getA(),
                                     circular.getEx(),
                                     circular.getEy(),
                                     FastMath.toRadians(circular.getI()),
                                     FastMath.toRadians(circular.getRaan()),
                                     FastMath.toRadians(circular.getAlphaV()),
                                     PositionAngle.valueOf(circular.getPositionAngle()),
                                     frame,
                                     new AbsoluteDate(orbit.getDate(),
                                                      TimeScalesFactory.getUTC()),
                                     mu);
        } else if (data.getTle() != null) {
            final TutorialTLE tleData = data.getTle();
            final String line1 = tleData.getLine1();
            final String line2 = tleData.getLine2();
            final TLE tle = new TLE(line1, line2);

            final TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);

            final AbsoluteDate initDate = tle.getDate();
            final SpacecraftState initialState = propagator.getInitialState();


            //Transformation from TEME to frame.
            return new CartesianOrbit(initialState.getPVCoordinates(frame),
                                      frame,
                                      initDate,
                                      mu);


        } else {
            final TutorialCartesianOrbit cartesian = data.getCartesian();
            final double[] pos = {cartesian.getX(), cartesian.getY(), cartesian.getZ()};
            final double[] vel = {cartesian.getVx(), cartesian.getVy(), cartesian.getVz()};

            return new CartesianOrbit(new PVCoordinates(new Vector3D(pos), new Vector3D(vel)),
                                      frame,
                                      new AbsoluteDate(orbit.getDate(),
                                                       TimeScalesFactory.getUTC()),
                                      mu);
        }
    }

    /** Set up range bias due to transponder delay.
     * @param inputData input data
     * @return range bias (may be null if bias is fixed to zero)
     */
    private Bias<Range> createSatRangeBias(final TutorialOrbitDetermination inputData) {

        // spacecraft parameters
        final TutorialSpacecraft spacecraft = inputData.getSpacecraft();

        // transponder delay bias
        final TutorialEstimatedParameter onBoardBias = spacecraft.getBias();

        if (onBoardBias != null && (FastMath.abs(onBoardBias.getInitialValue()) >= Precision.SAFE_MIN || onBoardBias.isEstimated())) {
            // bias is either non-zero or will be estimated,
            // we really need to create a modifier for this
            final Bias<Range> bias = new Bias<Range>(new String[] { "transponder delay bias", },
                                                     new double[] { onBoardBias.getInitialValue() },
                                                     new double[] { 1.0 },
                                                     new double[] { onBoardBias.getMinValue() },
                                                     new double[] { onBoardBias.getMaxValue() });
            bias.getParametersDrivers().get(0).setSelected(onBoardBias.isEstimated());
            return bias;
        } else {
            // fixed zero bias, we don't need any modifier
            return null;
        }
    }

    /** Set up range modifier taking on-board antenna offset.
     * @param inputData input data
     * @return range modifier (may be null if antenna offset is zero or undefined)
     */
    private OnBoardAntennaRangeModifier createSatAntennaRangeModifier(final TutorialOrbitDetermination inputData) {
        // spacecraft data
        final TutorialSpacecraft spacecraft = inputData.getSpacecraft();
        final Vector3D offset;
        if (spacecraft.getAntennaOffset().length != 0) {
            final double[] antennaOffset = spacecraft.getAntennaOffset();
            offset = new Vector3D(antennaOffset);
        } else {
            offset = Vector3D.ZERO;
        }
        return offset.getNorm() > 0 ? new OnBoardAntennaRangeModifier(offset) : null;
    }

    /** Set up range modifier taking shapiro effect.
     * @param inputData input data
     * @return range modifier (may be null if antenna offset is zero or undefined)
     */
    private ShapiroRangeModifier createShapiroRangeModifier(final TutorialOrbitDetermination inputData) {
        // range measurement data
        final TutorialMeasurements measurements = inputData.getMeasurements();
        return measurements.isWithShapiro() ? new ShapiroRangeModifier(getMu()) : null;
    }

    /** Set up stations.
     * @param inputData input data
     * @param conventions IERS conventions to use
     * @param body central body
     * @return name to station data map
          * @throws NoSuchElementException if input parameters are missing
     */
    private Map<String, StationData> createStationsData(final TutorialOrbitDetermination inputData,
                                                        final IERSConventions conventions,
                                                        final OneAxisEllipsoid body)
        throws NoSuchElementException {

        // map of stations
        final Map<String, StationData> stations = new HashMap<>();

        // measurement data
        final TutorialMeasurements measurementData = inputData.getMeasurements();

        final EOPHistory eopHistory = FramesFactory.findEOP(body.getBodyFrame());

        // loop on station data
        for (TutorialStation stationData : measurementData.getStations()) {

            // loop on observation type
            for (TutorialObservationType observationType : stationData.getObservationTypes()) {

                // station name
                final String stationName = stationData.getName() + observationType.getName();

                // displacements
                final StationDisplacement[] displacements  = new StationDisplacement[0];

                // the station itself
                final GeodeticPoint position = new GeodeticPoint(FastMath.toRadians(stationData.getLatitude()),
                                                                 FastMath.toRadians(stationData.getLongitude()),
                                                                 stationData.getAltitude());
                final TopocentricFrame topo = new TopocentricFrame(body, position, stationName);
                final GroundStation station = new GroundStation(topo, eopHistory, displacements);
                final TutorialEstimatedParameter clockOffset = observationType.getClockOffset();
                station.getClockOffsetDriver().setReferenceValue(clockOffset.getInitialValue());
                station.getClockOffsetDriver().setValue(clockOffset.getInitialValue());
                station.getClockOffsetDriver().setMinValue(clockOffset.getMinValue());
                station.getClockOffsetDriver().setMaxValue(clockOffset.getMaxValue());
                station.getClockOffsetDriver().setSelected(clockOffset.isEstimated());
                station.getEastOffsetDriver().setSelected(measurementData.isWithStationPositionEstimated());
                station.getNorthOffsetDriver().setSelected(measurementData.isWithStationPositionEstimated());
                station.getZenithOffsetDriver().setSelected(measurementData.isWithStationPositionEstimated());

                // range
                final TutorialRange rangeData = measurementData.getRange();
                final double rangeSigma = rangeData == null ? NULL_DOUBLE : rangeData.getSigma();
                final Bias<Range> rangeBias;
                if (rangeData != null && stationData.getRangeBias() != null &&
                                (FastMath.abs(stationData.getRangeBias().getInitialValue()) >= Precision.SAFE_MIN || stationData.getRangeBias().isEstimated())) {
                    final TutorialEstimatedParameter bias = stationData.getRangeBias();
                    rangeBias = new Bias<Range>(new String[] { stationName + RANGE_BIAS_SUFFIX, },
                                                new double[] { bias.getInitialValue() },
                                                new double[] { rangeSigma },
                                                new double[] { bias.getMinValue() },
                                                new double[] { bias.getMaxValue() });
                    rangeBias.getParametersDrivers().get(0).setSelected(bias.isEstimated());
                } else {
                    // bias fixed to zero, we don't need to create a modifier for this
                    rangeBias  = null;
                }

                // range rate
                final TutorialRangeRate rangeRateData = measurementData.getRangeRate();
                final double rangeRateSigma = rangeRateData == null ? NULL_DOUBLE : rangeRateData.getSigma();
                final Bias<RangeRate> rangeRateBias;
                if (rangeRateData != null && stationData.getRangeRateBias() != null &&
                                (FastMath.abs(stationData.getRangeRateBias().getInitialValue()) >= Precision.SAFE_MIN || stationData.getRangeRateBias().isEstimated())) {
                    final TutorialEstimatedParameter bias = stationData.getRangeRateBias();
                    rangeRateBias = new Bias<RangeRate>(new String[] { stationName + RANGE_RATE_BIAS_SUFFIX },
                                                        new double[] { bias.getInitialValue() },
                                                        new double[] { rangeRateSigma },
                                                        new double[] { bias.getMinValue() },
                                                        new double[] { bias.getMaxValue() });
                    rangeRateBias.getParametersDrivers().get(0).setSelected(bias.isEstimated());
                } else {
                    // bias fixed to zero, we don't need to create a modifier for this
                    rangeRateBias  = null;
                }

                // angular biases (must be converted in radians)
                final TutorialAzEl azElData = measurementData.getAzEl();
                final double[] azElSigma = azElData == null ? new double[2] : new double[] { FastMath.toRadians(azElData.getSigma()), FastMath.toRadians(azElData.getSigma()) };
                final Bias<AngularAzEl> azELBias;
                if (azElData != null && stationData.getAzElBias() != null &&
                                (FastMath.abs(FastMath.toRadians(stationData.getAzElBias().getInitialValue()))   >= Precision.SAFE_MIN || stationData.getAzElBias().isEstimated())) {
                    final TutorialEstimatedParameter bias = stationData.getAzElBias();
                    azELBias = new Bias<AngularAzEl>(new String[] { stationName + AZIMUTH_BIAS_SUFFIX,
                                                                    stationName + ELEVATION_BIAS_SUFFIX },
                                                     new double[] { FastMath.toRadians(bias.getInitialValue()), FastMath.toRadians(bias.getInitialValue()) },
                                                     azElSigma,
                                                     new double[] { FastMath.toRadians(bias.getMinValue()), FastMath.toRadians(bias.getMinValue()) },
                                                     new double[] { FastMath.toRadians(bias.getMaxValue()), FastMath.toRadians(bias.getMaxValue()) });
                    azELBias.getParametersDrivers().get(0).setSelected(bias.isEstimated());
                    azELBias.getParametersDrivers().get(1).setSelected(bias.isEstimated());
                } else {
                    // bias fixed to zero, we don't need to create a modifier for this
                    azELBias  = null;
                }

                // refraction correction
                final AngularRadioRefractionModifier refractionCorrection;
                if (azElData != null && azElData.isWithRefractionCorrection()) {
                    final double                     altitude        = station.getBaseFrame().getPoint().getAltitude();
                    final AtmosphericRefractionModel refractionModel = new EarthITU453AtmosphereRefraction(altitude);
                    refractionCorrection = new AngularRadioRefractionModifier(refractionModel);
                } else {
                    refractionCorrection = null;
                }

                // tropospheric correction
                final RangeTroposphericDelayModifier rangeTroposphericCorrection;
                if (measurementData.getTroposphere() != null) {

                    // troposphere data
                    final TutorialTroposphere troposphere = measurementData.getTroposphere();

                    // mapping function
                    final MappingFunction mappingModel;
                    if (troposphere.getMappingFunction().equals("NMF")) {
                        mappingModel = new NiellMappingFunctionModel();
                    } else if (troposphere.getMappingFunction().equals("GMF")) {
                        mappingModel = new GlobalMappingFunctionModel();
                    } else {
                        mappingModel = null;;
                    }

                    final DiscreteTroposphericModel troposphericModel;
                    if (troposphere.isEstimatedModel() && mappingModel != null) {
                        // Estimated tropospheric model
                        final TutorialEstimatedParameter zenithDelay = troposphere.getZenithDelay();
                        final EstimatedTroposphericModel model = new EstimatedTroposphericModel(mappingModel, zenithDelay.getInitialValue());
                        final ParameterDriver driver = model.getParametersDrivers().get(0);
                        driver.setSelected(zenithDelay.isEstimated());
                        driver.setName(stationData.getName() + " " + EstimatedTroposphericModel.TOTAL_ZENITH_DELAY);
                        troposphericModel = model;
                    } else {
                        // Empirical tropospheric model
                        troposphericModel = SaastamoinenModel.getStandardModel();
                    }

                    rangeTroposphericCorrection = new  RangeTroposphericDelayModifier(troposphericModel);
                } else {
                    rangeTroposphericCorrection = null;
                }

                // ionospheric correction
                final IonosphericModel ionosphericModel;
                if (measurementData.getIonosphere() != null) {
                    // ionosphere data
                    final TutorialIonosphere ionosphereData = measurementData.getIonosphere();
                    if (ionosphereData.isEstimatedModel()) {
                        // Estimated ionospheric model
                        final IonosphericMappingFunction mapping = new SingleLayerModelMappingFunction(ionosphereData.getIonosphericLayer());
                        ionosphericModel  = new EstimatedIonosphericModel(mapping, ionosphereData.getVtec().getInitialValue());
                        final ParameterDriver  ionosphericDriver = ionosphericModel.getParametersDrivers().get(0);
                        ionosphericDriver.setSelected(ionosphereData.getVtec().isEstimated());
                        ionosphericDriver.setName(stationName.substring(0, 5) + EstimatedIonosphericModel.VERTICAL_TOTAL_ELECTRON_CONTENT);
                    } else {
                        final TimeScale utc = TimeScalesFactory.getUTC();
                        // Klobuchar model
                        final KlobucharIonoCoefficientsLoader loader = new KlobucharIonoCoefficientsLoader();
                        loader.loadKlobucharIonosphericCoefficients(new AbsoluteDate(inputData.getOrbit().getDate(), utc).getComponents(utc).getDate());
                        ionosphericModel = new KlobucharIonoModel(loader.getAlpha(), loader.getBeta());
                    }
                } else {
                    ionosphericModel = null;
                }

                stations.put(stationName,
                             new StationData(station,
                                             rangeSigma,     rangeBias,
                                             rangeRateSigma, rangeRateBias,
                                             azElSigma,      azELBias,
                                             refractionCorrection, rangeTroposphericCorrection,
                                             ionosphericModel));

            }

        }

        return stations;
    }

    /** Set up weights.
     * @param inputData input data
     * @return base weights
     * @throws NoSuchElementException if input parameters are missing
     */
    private Weights createWeights(final TutorialOrbitDetermination inputData)
        throws NoSuchElementException {
        // measurement data
        final TutorialMeasurements measurements = inputData.getMeasurements();
        final double rangeWeight     = measurements.getRange()     == null ? NULL_DOUBLE : measurements.getRange().getWeight();
        final double rangeRateWeight = measurements.getRangeRate() == null ? NULL_DOUBLE : measurements.getRangeRate().getWeight();
        final double azElWeight      = measurements.getAzEl()      == null ? NULL_DOUBLE : measurements.getAzEl().getWeight();
        final double pvWeight        = measurements.getPv()        == null ? NULL_DOUBLE : measurements.getPv().getWeight();
        return new Weights(rangeWeight,
                           rangeRateWeight,
                           new double[] { azElWeight, azElWeight },
                           pvWeight);
    }

    /** Set up outliers manager for range measurements.
     * @param inputData input data
     * @param isDynamic if true, the filter should have adjustable standard deviation
     * @return outliers manager (null if none configured)
     */
    private OutlierFilter<Range> createRangeOutliersManager(final TutorialOrbitDetermination inputData, final boolean isDynamic) {
        // measurement data
        final TutorialMeasurements measurements = inputData.getMeasurements();
        if (measurements.getOutlierRejectionMultiplier() != NULL_INT &&
                        measurements.getOutlierRejectionStartingIteration() != NULL_INT) {
            return isDynamic ?
                   new DynamicOutlierFilter<>(measurements.getOutlierRejectionStartingIteration(),
                                                   measurements.getOutlierRejectionMultiplier()) :
                   new OutlierFilter<>(measurements.getOutlierRejectionStartingIteration(),
                                            measurements.getOutlierRejectionMultiplier());
        } else {
            return null;
        }
    }

    /** Set up outliers manager for range-rate measurements.
     * @param inputData input data
     * @param isDynamic if true, the filter should have adjustable standard deviation
     * @return outliers manager (null if none configured)
     */
    private OutlierFilter<RangeRate> createRangeRateOutliersManager(final TutorialOrbitDetermination inputData, final boolean isDynamic) {
        // measurement data
        final TutorialMeasurements measurements = inputData.getMeasurements();
        if (measurements.getOutlierRejectionMultiplier() != NULL_INT &&
                        measurements.getOutlierRejectionStartingIteration() != NULL_INT) {
            return isDynamic ?
                   new DynamicOutlierFilter<>(measurements.getOutlierRejectionStartingIteration(),
                                                       measurements.getOutlierRejectionMultiplier()) :
                   new OutlierFilter<>(measurements.getOutlierRejectionStartingIteration(),
                                                measurements.getOutlierRejectionMultiplier());
        } else {
            return null;
        }
    }

    /** Set up outliers manager for azimuth-elevation measurements.
     * @param inputData input data
     * @param isDynamic if true, the filter should have adjustable standard deviation
     * @return outliers manager (null if none configured)
     */
    private OutlierFilter<AngularAzEl> createAzElOutliersManager(final TutorialOrbitDetermination inputData, final boolean isDynamic) {
        // measurement data
        final TutorialMeasurements measurements = inputData.getMeasurements();
        if (measurements.getOutlierRejectionMultiplier() != NULL_INT &&
                        measurements.getOutlierRejectionStartingIteration() != NULL_INT) {
            return isDynamic ?
                   new DynamicOutlierFilter<>(measurements.getOutlierRejectionStartingIteration(),
                                                         measurements.getOutlierRejectionMultiplier()) :
                   new OutlierFilter<>(measurements.getOutlierRejectionStartingIteration(),
                                                  measurements.getOutlierRejectionMultiplier());
        } else {
            return null;
        }
    }

    /** Set up outliers manager for PV measurements.
     * @param inputData input data
     * @param isDynamic if true, the filter should have adjustable standard deviation
     * @return outliers manager (null if none configured)
     */
    private OutlierFilter<PV> createPVOutliersManager(final TutorialOrbitDetermination inputData, final boolean isDynamic) {
        // measurement data
        final TutorialMeasurements measurements = inputData.getMeasurements();
        if (measurements.getOutlierRejectionMultiplier() != NULL_INT &&
                        measurements.getOutlierRejectionStartingIteration() != NULL_INT) {
            return isDynamic ?
                   new DynamicOutlierFilter<>(measurements.getOutlierRejectionStartingIteration(),
                                                measurements.getOutlierRejectionMultiplier()) :
                   new OutlierFilter<>(measurements.getOutlierRejectionStartingIteration(),
                                         measurements.getOutlierRejectionMultiplier());
        } else {
            return null;
        }
    }

    /** Set up PV data.
     * @param inputData input data
     * @return PV data
     * @throws NoSuchElementException if input parameters are missing
     */
    private PVData createPVData(final TutorialOrbitDetermination inputData)
        throws NoSuchElementException {
        final TutorialPV pv = inputData.getMeasurements().getPv();
        return pv == null ? null : new PVData(pv.getSigmaPos(), pv.getSigmaVel());
    }

    /** Set up satellite data.
     * @param inputData input data
     * @return satellite data
     * @throws NoSuchElementException if input parameters are missing
     */
    private ObservableSatellite createObservableSatellite(final TutorialOrbitDetermination inputData)
        throws NoSuchElementException {
        // spacecraft data
        final TutorialSpacecraft spacecraft = inputData.getSpacecraft();
        final ObservableSatellite obsSat = new ObservableSatellite(0);
        final ParameterDriver clockOffsetDriver = obsSat.getClockOffsetDriver();
        if (spacecraft.getClockOffset() != null) {
            // on-board clock offser
            final TutorialEstimatedParameter clockOffset = spacecraft.getClockOffset();
            clockOffsetDriver.setReferenceValue(clockOffset.getInitialValue());
            clockOffsetDriver.setValue(clockOffset.getInitialValue());
            clockOffsetDriver.setMinValue(clockOffset.getMinValue());
            clockOffsetDriver.setMaxValue(clockOffset.getMaxValue());
            clockOffsetDriver.setSelected(clockOffset.isEstimated());
        }
        return obsSat;
    }

    /** Set up estimator.
     * @param inputData input data
     * @param propagatorBuilder propagator builder
     * @return estimator
     * @throws NoSuchElementException if input parameters are missing
     */
    private BatchLSEstimator createEstimator(final TutorialOrbitDetermination inputData,
                                             final OrbitDeterminationPropagatorBuilder propagatorBuilder)
        throws NoSuchElementException {

        // estimator data
        final TutorialBatchLSEstimator estimatorData = inputData.getEstimator();

        final LeastSquaresOptimizer optimizer;
        if (estimatorData.getOptimizationEngine().getLevenbergMarquardt() != null) {
            // we want to use a Levenberg-Marquardt optimization engine
            final TutorialLevenbergMarquardt levenbergMarquardt = estimatorData.getOptimizationEngine().getLevenbergMarquardt();
            final double initialStepBoundFactor = levenbergMarquardt.getInitialStep() == NULL_DOUBLE ? 100.0 : levenbergMarquardt.getInitialStep();
            optimizer = new LevenbergMarquardtOptimizer().withInitialStepBoundFactor(initialStepBoundFactor);
        } else {
            // we want to use a Gauss-Newton optimization engine
            optimizer = new GaussNewtonOptimizer(new QRDecomposer(1e-11), false);
        }

        final double convergenceThreshold;
        if (estimatorData.getConvergenceThreshold() != NULL_DOUBLE) {
            convergenceThreshold = estimatorData.getConvergenceThreshold();
        } else {
            convergenceThreshold = 1.0e-3;
        }
        final int maxIterations;
        if (estimatorData.getMaxIterations() != NULL_INT) {
            maxIterations = estimatorData.getMaxIterations();
        } else {
            maxIterations = 10;
        }
        final int maxEvaluations;
        if (estimatorData.getMaxEvaluations() != NULL_INT) {
            maxEvaluations = estimatorData.getMaxEvaluations();
        } else {
            maxEvaluations = 20;
        }

        final BatchLSEstimator estimator = new BatchLSEstimator(optimizer, propagatorBuilder);
        estimator.setParametersConvergenceThreshold(convergenceThreshold);
        estimator.setMaxIterations(maxIterations);
        estimator.setMaxEvaluations(maxEvaluations);

        return estimator;

    }

    /** Read a measurements file.
     * @param source data source containing measurements
     * @param stations name to stations data map
     * @param pvData PV measurements data
     * @param satellite satellite reference
     * @param satRangeBias range bias due to transponder delay
     * @param satAntennaRangeModifier modifier for on-board antenna offset
     * @param weights base weights for measurements
     * @param rangeOutliersManager manager for range measurements outliers (null if none configured)
     * @param rangeRateOutliersManager manager for range-rate measurements outliers (null if none configured)
     * @param azElOutliersManager manager for azimuth-elevation measurements outliers (null if none configured)
     * @param pvOutliersManager manager for PV measurements outliers (null if none configured)
     * @return measurements list
     * @exception IOException if measurement file cannot be read
     */
    private List<ObservedMeasurement<?>> readMeasurements(final DataSource source,
                                                          final Map<String, StationData> stations,
                                                          final PVData pvData,
                                                          final ObservableSatellite satellite,
                                                          final Bias<Range> satRangeBias,
                                                          final OnBoardAntennaRangeModifier satAntennaRangeModifier,
                                                          final Weights weights,
                                                          final OutlierFilter<Range> rangeOutliersManager,
                                                          final OutlierFilter<RangeRate> rangeRateOutliersManager,
                                                          final OutlierFilter<AngularAzEl> azElOutliersManager,
                                                          final OutlierFilter<PV> pvOutliersManager)
        throws IOException {

        final List<ObservedMeasurement<?>> measurements = new ArrayList<>();
        try (Reader reader = source.getOpener().openReaderOnce();
             BufferedReader br = new BufferedReader(reader)) {
            int lineNumber = 0;
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                ++lineNumber;
                line = line.trim();
                if (line.length() > 0 && !line.startsWith("#")) {
                    final String[] fields = line.split("\\s+");
                    if (fields.length < 2) {
                        throw new OrekitException(OrekitMessages.UNABLE_TO_PARSE_LINE_IN_FILE,
                                                  lineNumber, source.getName(), line);
                    }
                    switch (fields[1]) {
                        case "RANGE" :
                            final Range range = new RangeParser().parseFields(fields, stations, pvData, satellite,
                                                                              satRangeBias, weights,
                                                                              line, lineNumber, source.getName(), true);
                            if (satAntennaRangeModifier != null) {
                                range.addModifier(satAntennaRangeModifier);
                            }
                            if (rangeOutliersManager != null) {
                                range.addModifier(rangeOutliersManager);
                            }
                            addIfNonZeroWeight(range, measurements);
                            break;
                        case "RANGE_RATE" :
                            final RangeRate rangeRate = new RangeRateParser().parseFields(fields, stations, pvData, satellite,
                                                                                          satRangeBias, weights,
                                                                                          line, lineNumber, source.getName(), true);
                            if (rangeRateOutliersManager != null) {
                                rangeRate.addModifier(rangeRateOutliersManager);
                            }
                            addIfNonZeroWeight(rangeRate, measurements);
                            break;
                        case "AZ_EL" :
                            final AngularAzEl angular = new AzElParser().parseFields(fields, stations, pvData, satellite,
                                                                                     satRangeBias, weights,
                                                                                     line, lineNumber, source.getName(), false);
                            if (azElOutliersManager != null) {
                                angular.addModifier(azElOutliersManager);
                            }
                            addIfNonZeroWeight(angular, measurements);
                            break;
                        case "PV" :
                            final PV pv = new PVParser().parseFields(fields, stations, pvData, satellite,
                                                                     satRangeBias, weights,
                                                                     line, lineNumber, source.getName(), true);
                            if (pvOutliersManager != null) {
                                pv.addModifier(pvOutliersManager);
                            }
                            addIfNonZeroWeight(pv, measurements);
                            break;
                        default :
                            throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                                      "unknown measurement type " + fields[1] +
                                                      " at line " + lineNumber +
                                                      " in file " + source.getName() +
                                                      "\n" + line);
                    }
                }
            }
        }

        if (measurements.isEmpty()) {
            throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                      "not measurements read from file " + source.getName());
        }

        return measurements;

    }

    /** Read a RINEX measurements file.
     * @param source named data containing measurements
     * @param satId satellite we are interested in
     * @param stations name to stations data map
     * @param satellite satellite reference
     * @param satRangeBias range bias due to transponder delay
     * @param satAntennaRangeModifier modifier for on-board antenna offset
     * @param weights base weights for measurements
     * @param rangeOutliersManager manager for range measurements outliers (null if none configured)
     * @param rangeRateOutliersManager manager for range-rate measurements outliers (null if none configured)
     * @param shapiroRangeModifier shapiro range modifier (null if none configured)
     * @return measurements list
     * @exception IOException if measurement file cannot be read
     */
    private List<ObservedMeasurement<?>> readRinex(final DataSource source, final String satId,
                                                   final Map<String, StationData> stations,
                                                   final ObservableSatellite satellite,
                                                   final Bias<Range> satRangeBias,
                                                   final OnBoardAntennaRangeModifier satAntennaRangeModifier,
                                                   final Weights weights,
                                                   final OutlierFilter<Range> rangeOutliersManager,
                                                   final OutlierFilter<RangeRate> rangeRateOutliersManager,
                                                   final ShapiroRangeModifier shapiroRangeModifier)
        throws IOException {
        final String notConfigured = " not configured";
        final List<ObservedMeasurement<?>> measurements = new ArrayList<>();
        final SatelliteSystem system = SatelliteSystem.parseSatelliteSystem(satId);
        final int prnNumber;
        switch (system) {
            case GPS:
            case GLONASS:
            case GALILEO:
                prnNumber = Integer.parseInt(satId.substring(1));
                break;
            case SBAS:
                prnNumber = Integer.parseInt(satId.substring(1)) + 100;
                break;
            default:
                prnNumber = -1;
        }
        final RinexObservationLoader loader = new RinexObservationLoader(source);
        for (final ObservationDataSet observationDataSet : loader.getObservationDataSets()) {
            if (observationDataSet.getSatelliteSystem() == system    &&
                observationDataSet.getPrnNumber()       == prnNumber) {
                for (final ObservationData od : observationDataSet.getObservationData()) {
                    final double snr = od.getSignalStrength();
                    if (!Double.isNaN(od.getValue()) && (snr == 0 || snr >= 4)) {
                        if (od.getObservationType().getMeasurementType() == MeasurementType.PSEUDO_RANGE) {
                            // this is a measurement we want
                            final String stationName = observationDataSet.getHeader().getMarkerName() + "/" + od.getObservationType();
                            final StationData stationData = stations.get(stationName);
                            if (stationData == null) {
                                throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                                          stationName + notConfigured);
                            }
                            final Range range = new Range(stationData.getStation(), false, observationDataSet.getDate(),
                                                          od.getValue(), stationData.getRangeSigma(),
                                                          weights.getRangeBaseWeight(), satellite);
                            if (stationData.getIonosphericModel() != null) {
                                final RangeIonosphericDelayModifier ionoModifier = new RangeIonosphericDelayModifier(stationData.getIonosphericModel(),
                                                                                                                     od.getObservationType().getFrequency(system).getMHzFrequency() * 1.0e6);
                                range.addModifier(ionoModifier);
                            }
                            if (satAntennaRangeModifier != null) {
                                range.addModifier(satAntennaRangeModifier);
                            }
                            if (shapiroRangeModifier != null) {
                                range.addModifier(shapiroRangeModifier);
                            }
                            if (stationData.getRangeBias() != null) {
                                range.addModifier(stationData.getRangeBias());
                            }
                            if (satRangeBias != null) {
                                range.addModifier(satRangeBias);
                            }
                            if (stationData.getRangeTroposphericCorrection() != null) {
                                range.addModifier(stationData.getRangeTroposphericCorrection());
                            }
                            if (rangeOutliersManager != null) {
                                range.addModifier(rangeOutliersManager);
                            }
                            addIfNonZeroWeight(range, measurements);

                        } else if (od.getObservationType().getMeasurementType() == MeasurementType.DOPPLER) {
                            // this is a measurement we want
                            final String stationName = observationDataSet.getHeader().getMarkerName() + "/" + od.getObservationType();
                            final StationData stationData = stations.get(stationName);
                            if (stationData == null) {
                                throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                                          stationName + notConfigured);
                            }
                            final RangeRate rangeRate = new RangeRate(stationData.getStation(), observationDataSet.getDate(),
                                                                      od.getValue(), stationData.getRangeRateSigma(),
                                                                      weights.getRangeRateBaseWeight(), false, satellite);
                            if (stationData.getIonosphericModel() != null) {
                                final RangeRateIonosphericDelayModifier ionoModifier = new RangeRateIonosphericDelayModifier(stationData.getIonosphericModel(),
                                                                                                                             od.getObservationType().getFrequency(system).getMHzFrequency() * 1.0e6,
                                                                                                                             false);
                                rangeRate.addModifier(ionoModifier);
                            }
                            if (stationData.getRangeRateBias() != null) {
                                rangeRate.addModifier(stationData.getRangeRateBias());
                            }
                            if (rangeRateOutliersManager != null) {
                                rangeRate.addModifier(rangeRateOutliersManager);
                            }
                            addIfNonZeroWeight(rangeRate, measurements);
                        }
                    }
                }
            }
        }

        return measurements;

    }

    /** Add a measurement to a list if it has non-zero weight.
     * @param measurement measurement to add
     * @param measurements measurements list
     */
    private static void addIfNonZeroWeight(final ObservedMeasurement<?> measurement, final List<ObservedMeasurement<?>> measurements) {
        double sum = 0;
        for (double w : measurement.getBaseWeight()) {
            sum += FastMath.abs(w);
        }
        if (sum > Precision.SAFE_MIN) {
            // we only consider measurements with non-zero weight
            measurements.add(measurement);
        }
    }

    /** Initialize the Orekit data.
     * @param input input file
     * @return home home directory
     * @throws IOException if input files cannot be read
     */
    private static File initializeOrekitData(final File input) throws IOException {

        // configure Orekit
        final File home       = new File(System.getProperty("user.home"));
        final File orekitData = new File(home, "orekit-data");
        if (!orekitData.exists()) {
            System.err.format(Locale.US, "Failed to find %s folder%n",
                              orekitData.getAbsolutePath());
            System.err.format(Locale.US, "You need to download %s from %s, unzip it in %s and rename it 'orekit-data' for this tutorial to work%n",
                              "orekit-data-master.zip", "https://gitlab.orekit.org/orekit/orekit-data/-/archive/master/orekit-data-master.zip",
                              home.getAbsolutePath());
            throw new IOException("Failed to load Orekit data");
        }
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
        manager.addProvider(new DirectoryCrawler(input.getParentFile()));
        return home;
    }

}
