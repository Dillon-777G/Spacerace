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
package org.orekit.tutorials.estimation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.QRDecomposer;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.optim.nonlinear.vector.leastsquares.GaussNewtonOptimizer;
import org.hipparchus.optim.nonlinear.vector.leastsquares.LeastSquaresProblem.Evaluation;
import org.hipparchus.optim.nonlinear.vector.leastsquares.SequentialGaussNewtonOptimizer;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataFilter;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DataSource;
import org.orekit.data.DirectoryCrawler;
import org.orekit.data.GzipFilter;
import org.orekit.data.UnixCompressFilter;
import org.orekit.estimation.leastsquares.BatchLSEstimator;
import org.orekit.estimation.leastsquares.SequentialBatchLSEstimator;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.estimation.measurements.Position;
import org.orekit.files.ccsds.definitions.BodyFacade;
import org.orekit.files.ccsds.definitions.CenterName;
import org.orekit.files.ccsds.definitions.FrameFacade;
import org.orekit.files.ccsds.definitions.TimeSystem;
import org.orekit.files.ccsds.ndm.WriterBuilder;
import org.orekit.files.ccsds.ndm.odm.CartesianCovariance;
import org.orekit.files.ccsds.ndm.odm.oem.OemMetadata;
import org.orekit.files.ccsds.ndm.odm.oem.StreamingOemWriter;
import org.orekit.files.ccsds.ndm.odm.opm.Opm;
import org.orekit.files.ccsds.ndm.odm.opm.OpmData;
import org.orekit.files.ccsds.utils.generation.Generator;
import org.orekit.files.ccsds.utils.generation.XmlGenerator;
import org.orekit.files.ilrs.CPF;
import org.orekit.files.ilrs.CPF.CPFCoordinate;
import org.orekit.files.ilrs.CPF.CPFEphemeris;
import org.orekit.files.ilrs.CPFParser;
import org.orekit.forces.ForceModel;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.drag.IsotropicDrag;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.NewtonianAttraction;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.gravity.potential.SphericalHarmonicsProvider;
import org.orekit.forces.radiation.IsotropicRadiationSingleCoefficient;
import org.orekit.forces.radiation.RadiationSensitive;
import org.orekit.forces.radiation.SolarRadiationPressure;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.gnss.HatanakaCompressFilter;
import org.orekit.models.earth.atmosphere.Atmosphere;
import org.orekit.models.earth.atmosphere.NRLMSISE00;
import org.orekit.models.earth.atmosphere.data.MarshallSolarActivityFutureEstimation;
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
import org.orekit.propagation.conversion.ClassicalRungeKuttaIntegratorBuilder;
import org.orekit.propagation.conversion.DormandPrince853IntegratorBuilder;
import org.orekit.propagation.conversion.NumericalPropagatorBuilder;
import org.orekit.propagation.conversion.ODEIntegratorBuilder;
import org.orekit.propagation.conversion.OrbitDeterminationPropagatorBuilder;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.tutorials.estimation.common.AttitudeMode;
import org.orekit.tutorials.estimation.common.OrbitDeterminationObserver;
import org.orekit.tutorials.estimation.common.TutorialOrbitDetermination;
import org.orekit.tutorials.utils.NdmUtils;
import org.orekit.tutorials.yaml.TutorialBatchLSEstimator;
import org.orekit.tutorials.yaml.TutorialBody;
import org.orekit.tutorials.yaml.TutorialForceModel;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialDrag;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialGravity;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialSolarRadiationPressure;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialThirdBody;
import org.orekit.tutorials.yaml.TutorialIntegrator;
import org.orekit.tutorials.yaml.TutorialMeasurements.TutorialPosition;
import org.orekit.tutorials.yaml.TutorialOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialCartesianOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialCircularOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialEquinoctialOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialKeplerianOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialTLE;
import org.orekit.tutorials.yaml.TutorialPropagator;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.ParameterDriver;
import org.orekit.utils.TimeStampedPVCoordinates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * The purpose of the tutorial is to presents the
 * Sequential-Batch Least Squares estimation.
 * <p>
 * An initial covariance and first guess are obtained from
 * a classical batch least squares estimation. The sequential-batch
 * estimation is perform using these data and a new set of measurements.
 * A new OPM is generated at the end of the estimation and an OEM
 * is also generated.
 * </p>
 * @author Bryan Cazabonne
 */
public class SequentialBatchLeastSquares {

    /** String for estimation time. */
    private static final String ESTIMATION_TIME = "Estimation time (s): ";

    /** Null double values are equal to 0.0 in YAML file. */
    private static final double NULL_DOUBLE = 0.0;

    /** Null integer values are equal to 0 in YAML files. */
    private static final int NULL_INT = 0;

    /** Name for the estimated OPM. */
    private static final String OUTPUT_OPM = "Opm.xml";

    /** Name for the generated OEM. */
    private static final String OUTPUT_OEM = "Oem.xml";

    /**
     * Main method.
     * @param args nothing is needed
     * @throws IOException if observations file cannot be read properly
     * @throws URISyntaxException if URI syntax of SP3 file is wrong
     */
    public static void main(final String[] args) throws URISyntaxException, IOException {

        // input in tutorial resources directory
        final String inputPath = SequentialBatchLeastSquares.class.getClassLoader().
                                 getResource("sequential-batch/sequential-batch.yaml").toURI().getPath();

        // Run the program
        new SequentialBatchLeastSquares().run(new File(inputPath));

    }

    /**
     * Run the program.
     * @param input input file
     * @throws IOException if observations file cannot be read properly
     * @throws URISyntaxException if URI syntax of SP3 file is wrong
     */
    private void run(final File input) throws URISyntaxException, IOException {

        // Configure Orekit
        final File home = initializeOrekit();

        // Read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialSequentialBatch data = mapper.readValue(input, TutorialSequentialBatch.class);

        // Orbit determination data
        final TutorialOrbitDetermination inputData = data.getOrbitDetermination();

        // Gravity field
        final NormalizedSphericalHarmonicsProvider gravityField = initializeGravityField(inputData);

        // Central body
        final OneAxisEllipsoid centralBody = initializeBody(inputData);

        // Initial orbit
        final Orbit initialOrbit = initializeOrbit(inputData, gravityField);

        // Initialize propagator
        final ODEIntegratorBuilder integrator = initializeIntegrator(inputData);
        final OrbitDeterminationPropagatorBuilder propagator = initializePropagator(inputData, integrator, initialOrbit, centralBody, gravityField);

        // Measurements for the batch least squares orbit determination
        String fileName = inputData.getMeasurements().getMeasurementFiles().get(0);
        List<ObservedMeasurement<?>> measurements = initializeMeasurements(inputData, fileName, input, initialOrbit);

        // Create estimator for the batch least squares estimation
        System.out.println("First estimation: Batch Least Squares");
        BatchLSEstimator estimator = initializeEstimator(inputData, propagator, initialOrbit, measurements);

        // Estimation on the first set of measurements
        double t0 = System.currentTimeMillis();
        final Propagator estimatedLS      = estimator.estimate()[0];
        final Orbit      estimatedOrbitLS = estimatedLS.getInitialState().getOrbit();
        double t1 = System.currentTimeMillis();
        System.out.println(ESTIMATION_TIME + (t1 - t0) * 1.0e-3);
        System.out.println("");

        // Measurements for the sequential-batch least squares orbit determination
        fileName = inputData.getMeasurements().getMeasurementFiles().get(1);
        measurements = initializeMeasurements(inputData, fileName, input, estimatedOrbitLS);

        // Create estimator for the sequential-batch least squares estimation
        System.out.println("Second estimation: Sequential-Batch Least Squares");
        estimator = initializeSequentialEstimator(inputData, propagator, estimatedOrbitLS, estimator.getOptimum(), measurements);

        // Estimation on the second set of measurements
        t0 = System.currentTimeMillis();
        final Propagator estimatedSeqLS      = estimator.estimate()[0];
        final Orbit      estimatedOrbitSeqLS = estimatedSeqLS.getInitialState().getOrbit();
        t1 = System.currentTimeMillis();
        System.out.println(ESTIMATION_TIME + (t1 - t0) * 1.0e-3);
        System.out.println("");

        // Create an OPM based on the estimated orbit and covariance
        final WriterBuilder ndmWriterBuilder = new WriterBuilder().withConventions(IERSConventions.IERS_2010);
        System.out.println("Write estimated OPM");
        writeOpmFile(home, inputData, ndmWriterBuilder,
                     new SpacecraftState(estimatedOrbitSeqLS, inputData.getSpacecraft().getMass()),
                     estimator.getPhysicalCovariances(1.0e-10).getSubMatrix(0, 5, 0, 5));

        // Create an OEM based on the estimated orbit
        System.out.println("Generate OEM with a duration of (days): " + data.getDurationInDays());
        writeOemFile(home, inputData, data.getOutputStep(),
                     data.getDurationInDays() * Constants.JULIAN_DAY,
                     ndmWriterBuilder, estimatedSeqLS);

    }

    /**
     * Initialize initial guess.
     * @param inputData tutorial input data
     * @param gravityField gravity field (used for the central attraction coefficient)
     * @return the configured orbit
     */
    private static Orbit initializeOrbit(final TutorialOrbitDetermination inputData,
                                         final SphericalHarmonicsProvider gravityField) {

        // Input orbit data
        final TutorialOrbit orbit = inputData.getOrbit();

        // Central attration coefficient
        final double mu = gravityField.getMu();

        // Input orbit data were defined
        final TutorialOrbitType data = orbit.getOrbitType();

        // Inertial frame
        final Frame frame = orbit.getInertialFrame() == null ? FramesFactory.getEME2000() : orbit.getInertialFrame();

        // Obit definition
        if (data.getKeplerian() != null) {
            // Input data are Keplerian elements
            final TutorialKeplerianOrbit keplerian = data.getKeplerian();
            return new KeplerianOrbit(keplerian.getA(), keplerian.getE(),
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
            // Input data are Equinoctial elements
            final TutorialEquinoctialOrbit equinoctial = data.getEquinoctial();
            return new EquinoctialOrbit(equinoctial.getA(),
                                        equinoctial.getEx(), equinoctial.getEy(),
                                        equinoctial.getHx(), equinoctial.getHy(),
                                        FastMath.toRadians(equinoctial.getLv()),
                                        PositionAngle.valueOf(equinoctial.getPositionAngle()),
                                        frame,
                                        new AbsoluteDate(orbit.getDate(),
                                                         TimeScalesFactory.getUTC()),
                                        mu);
        } else if (data.getCircular() != null) {
            // Input data are Circular elements
            final TutorialCircularOrbit circular = data.getCircular();
            return new CircularOrbit(circular.getA(),
                                     circular.getEx(), circular.getEy(),
                                     FastMath.toRadians(circular.getI()),
                                     FastMath.toRadians(circular.getRaan()),
                                     FastMath.toRadians(circular.getAlphaV()),
                                     PositionAngle.valueOf(circular.getPositionAngle()),
                                     frame,
                                     new AbsoluteDate(orbit.getDate(),
                                                      TimeScalesFactory.getUTC()),
                                     mu);
        } else if (data.getTle() != null) {
            // Input data is a TLE
            final TutorialTLE tleData = data.getTle();
            final String line1 = tleData.getLine1();
            final String line2 = tleData.getLine2();
            final TLE tle = new TLE(line1, line2);

            final TLEPropagator propagator = TLEPropagator.selectExtrapolator(tle);

            final AbsoluteDate initDate = tle.getDate();
            final SpacecraftState initialState = propagator.getInitialState();

            //Transformation from TEME to frame.
            return new CartesianOrbit(initialState.getPVCoordinates(frame),
                                      frame, initDate, mu);

        } else {
            // Input data are Cartesian elements
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

    /**
     * Initialize the propagator builder.
     * @param inputData input data
     * @param integrator integrator builder
     * @param orbit initial orbit
     * @param centralBody central body
     * @param gravityField gravity field
     * @return a configured propagator builder
     */
    private static OrbitDeterminationPropagatorBuilder initializePropagator(final TutorialOrbitDetermination inputData,
                                                                            final ODEIntegratorBuilder integrator,
                                                                            final Orbit orbit,
                                                                            final OneAxisEllipsoid centralBody,
                                                                            final SphericalHarmonicsProvider gravityField) {

        // Attitude mode
        final AttitudeMode mode;
        if (inputData.getSpacecraft().getAttitudeMode() != null) {
            mode = AttitudeMode.valueOf(inputData.getSpacecraft().getAttitudeMode());
        } else {
            mode = AttitudeMode.DEFAULT_LAW;
        }

        // Initialize the numerical builder
        final NumericalPropagatorBuilder numPropagator = new NumericalPropagatorBuilder(orbit, integrator, PositionAngle.MEAN, 1.0);

        // Add force models to the numerical propagator
        final TutorialForceModel forceModelData = inputData.getPropagator().getForceModels();
        // Drag
        if (forceModelData.getDrag() != null) {

            // Drag data
            final TutorialDrag drag        = forceModelData.getDrag();
            final double       cd          = drag.getCd().getInitialValue();
            final double       area        = drag.getArea();
            final boolean      cdEstimated = drag.getCd().isEstimated();

            // Atmosphere model
            final MarshallSolarActivityFutureEstimation msafe =
                            new MarshallSolarActivityFutureEstimation(MarshallSolarActivityFutureEstimation.DEFAULT_SUPPORTED_NAMES,
                                                                      MarshallSolarActivityFutureEstimation.StrengthLevel.AVERAGE);
            final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
            manager.feed(msafe.getSupportedNames(), msafe);
            final Atmosphere atmosphere = new NRLMSISE00(msafe, CelestialBodyFactory.getSun(), centralBody);

            // Drag force (Assuming spherical satellite)
            final ForceModel force = new DragForce(atmosphere, new IsotropicDrag(area, cd));
            for (final ParameterDriver driver : force.getParametersDrivers()) {
                if (driver.getName().equals(DragSensitive.DRAG_COEFFICIENT)) {
                    if (cdEstimated) {
                        driver.setSelected(true);
                    }
                }
            }

            // Add the force model
            numPropagator.addForceModel(force);

        }

        // Third bodies
        for (TutorialThirdBody thirdBody : forceModelData.getThirdBody()) {
            final CelestialBody body = CelestialBodyFactory.getBody(thirdBody.getName());
            numPropagator.addForceModel(new ThirdBodyAttraction(body));
        }

        // Solar radiation pressure
        if (forceModelData.getSolarRadiationPressure() != null) {

            // Solar radiation pressure data
            final TutorialSolarRadiationPressure srp = forceModelData.getSolarRadiationPressure();
            final double  cr                         = srp.getCr().getInitialValue();
            final double  area                       = srp.getArea();
            final boolean cREstimated                = srp.getCr().isEstimated();

            // Satellite model (spherical)
            final RadiationSensitive spacecraft = new IsotropicRadiationSingleCoefficient(area, cr);

            // Solar radiation pressure
            final ForceModel force = new SolarRadiationPressure(CelestialBodyFactory.getSun(), gravityField.getAe(), spacecraft);
            for (final ParameterDriver driver : force.getParametersDrivers()) {
                if (driver.getName().equals(RadiationSensitive.REFLECTION_COEFFICIENT)) {
                    if (cREstimated) {
                        driver.setSelected(true);
                    }
                }
            }

            // Add the force model
            numPropagator.addForceModel(force);

        }

        // Potential
        numPropagator.addForceModel(new HolmesFeatherstoneAttractionModel(centralBody.getBodyFrame(), (NormalizedSphericalHarmonicsProvider) gravityField));

        // Newton
        numPropagator.addForceModel(new NewtonianAttraction(gravityField.getMu()));

        // Update
        numPropagator.setAttitudeProvider(mode.getProvider(orbit.getFrame(), centralBody));
        numPropagator.setMass(inputData.getSpacecraft().getMass());
        numPropagator.resetOrbit(orbit);

        // Return
        return numPropagator;

    }

    /**
     * Initialize the list of measurements.
     * @param inputData input data
     * @param fileName file name
     * @param input input file
     * @param orbit initial orbit used for the frame
     * @return the list of measurements
     */
    private static List<ObservedMeasurement<?>> initializeMeasurements(final TutorialOrbitDetermination inputData, final String fileName,
                                                                       final File input, final Orbit orbit) throws IOException {

        // Tutorial measurement data
        final TutorialPosition measurementData = inputData.getMeasurements().getPosition();

        // Satellite
        final ObservableSatellite satellite = new ObservableSatellite(0);

        // Initialize an empty list of measurements
        final List<ObservedMeasurement<?>> measurements = new ArrayList<>();

        // Initialize list
        final List<CPFEphemeris> ephemeris = new ArrayList<>();

        // set up filtering for measurements files
        DataSource nd = new DataSource(fileName, () -> new FileInputStream(new File(input.getParentFile(), fileName)));
        for (final DataFilter filter : Arrays.asList(new GzipFilter(),
                                                     new UnixCompressFilter(),
                                                     new HatanakaCompressFilter())) {
            nd = filter.filter(nd);
        }

        // Return the CPF ephemeris for the wanted satellite
        final CPF cpfFile = new CPFParser().parse(nd);
        ephemeris.add(cpfFile.getSatellites().get(cpfFile.getHeader().getIlrsSatelliteId()));

        // Loop on ephemeris
        for (final CPFEphemeris currentEphemeris : ephemeris) {

            // Loop on measurements
            for (final CPFCoordinate coordinate : currentEphemeris.getCoordinates()) {

                // Position in inertial frames
                final TimeStampedPVCoordinates pvInertial = currentEphemeris.getFrame().getTransformTo(orbit.getFrame(), coordinate.getDate()).
                                                                                        transformPVCoordinates(coordinate);

                // Initialize measurement
                final Position measurement = new Position(coordinate.getDate(), pvInertial.getPosition(), measurementData.getSigmaPos(), measurementData.getWeight(), satellite);

                // Add the measurement to the list
                measurements.add(measurement);

            }

        }

        // Return the filled list
        return measurements;

    }

    /**
     * Initialize the estimator used for the orbit determination.
     * @param inputData input data
     * @param propagator orbit propagator
     * @param initialGuess intial guess of the orbit determination
     * @param measurements measurements
     * @return a configured estimator
     */
    private static BatchLSEstimator initializeEstimator(final TutorialOrbitDetermination inputData,
                                                        final OrbitDeterminationPropagatorBuilder propagator,
                                                        final Orbit initialGuess,
                                                        final List<ObservedMeasurement<?>> measurements) {

        // Estimator data
        final TutorialBatchLSEstimator estimatorData = inputData.getEstimator();

        // Optimizer
        final GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(new QRDecomposer(1e-11), false);

        // Estimator
        final BatchLSEstimator estimator = new BatchLSEstimator(optimizer, propagator);

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

        estimator.setParametersConvergenceThreshold(convergenceThreshold);
        estimator.setMaxIterations(maxIterations);
        estimator.setMaxEvaluations(maxEvaluations);

        // Add the measurements to the estimator
        for (ObservedMeasurement<?> measurement : measurements) {
            estimator.addMeasurement(measurement);
        }

        // Add observer
        estimator.setObserver(new OrbitDeterminationObserver(initialGuess, null, estimator));

        // Return the configured estimator
        return estimator;

    }

    /**
     * Initialize the sequential-batch estimator used for the orbit determination.
     * @param inputData input data
     * @param propagator orbit propagator
     * @param initialGuess intial guess of the orbit determination
     * @param previousEstimation previous least squares estimation
     * @param measurements measurements
     * @return a configured estimator
     */
    private static SequentialBatchLSEstimator initializeSequentialEstimator(final TutorialOrbitDetermination inputData,
                                                                            final OrbitDeterminationPropagatorBuilder propagator,
                                                                            final Orbit initialGuess,
                                                                            final Evaluation previousEstimation,
                                                                            final List<ObservedMeasurement<?>> measurements) {

        // Estimator data
        final TutorialBatchLSEstimator estimatorData = inputData.getEstimator();

        // Optimizer
        final SequentialGaussNewtonOptimizer optimizer = new SequentialGaussNewtonOptimizer().withEvaluation(previousEstimation);

        // Estimator
        final SequentialBatchLSEstimator estimator = new SequentialBatchLSEstimator(optimizer, propagator);

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

        estimator.setParametersConvergenceThreshold(convergenceThreshold);
        estimator.setMaxIterations(maxIterations);
        estimator.setMaxEvaluations(maxEvaluations);

        // Add the measurements to the estimator
        for (ObservedMeasurement<?> measurement : measurements) {
            estimator.addMeasurement(measurement);
        }

        // Add observer
        estimator.setObserver(new OrbitDeterminationObserver(initialGuess, null, estimator));

        // Return the configured estimator
        return estimator;

    }

    /**
     * Initialize the spherical harmonics provider.
     * @param inputData input data
     * @return a configured spherical harmonics provider
     */
    private static NormalizedSphericalHarmonicsProvider initializeGravityField(final TutorialOrbitDetermination inputData) {
        // Gravity data
        final TutorialGravity gravity = inputData.getPropagator().getForceModels().getGravity();
        final int degree = gravity.getDegree();
        final int order  = FastMath.min(degree, gravity.getOrder());
        // Return the unnormalized provider
        return GravityFieldFactory.getNormalizedProvider(degree, order);
    }

    /**
     * Initialize the central body (i.e. the Earth).
     * @param inputData input data
     * @return a configured central body
     */
    private static OneAxisEllipsoid initializeBody(final TutorialOrbitDetermination inputData) {

        // Body data
        final TutorialBody body = inputData.getBody();

        // Body Frame
        final Frame bodyFrame;
        if (body.getFrameName() != null) {
            bodyFrame = body.getEarthFrame();
        } else {
            bodyFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        }

        // Equatorial radius
        final double equatorialRadius;
        if (body.getEquatorialRadius() != NULL_DOUBLE) {
            equatorialRadius = body.getEquatorialRadius();
        } else {
            equatorialRadius = Constants.WGS84_EARTH_EQUATORIAL_RADIUS;
        }

        // Flattening
        final double flattening;
        if (body.getInverseFlattening() != NULL_DOUBLE) {
            flattening = 1.0 / body.getInverseFlattening();
        } else {
            flattening = Constants.WGS84_EARTH_FLATTENING;
        }

        // Return the configured body
        return new OneAxisEllipsoid(equatorialRadius, flattening, bodyFrame);

    }

    /**
     * Initialize integrator data.
     * @param inputData input data
     * @return a configured numerical integrator
     */
    private static ODEIntegratorBuilder initializeIntegrator(final TutorialOrbitDetermination inputData) {

        // Propagator configuration
        final TutorialPropagator propagatorData = inputData.getPropagator();

        // Initialize numerical integrator
        final TutorialIntegrator integratorData = propagatorData.getIntegrator();

        // Fixed step
        if (integratorData.getFixedStep() != NULL_DOUBLE) {
            // RK4 integrator
            return new ClassicalRungeKuttaIntegratorBuilder(integratorData.getFixedStep());
        } else {
            // Variable step integrator
            return new DormandPrince853IntegratorBuilder(integratorData.getMinStep(),
                                                         integratorData.getMaxStep(),
                                                         integratorData.getPositionError());
        }

    }

    /**
     * Write the output OPM file.
     * @param home home directory
     * @param inputData input data
     * @param ndmWriterBuilder builder for NDM files
     * @param estimated estimated orbit
     * @param covEstimated estimated covariance
     */
    private void writeOpmFile(final File home, final TutorialOrbitDetermination inputData,
                              final WriterBuilder ndmWriterBuilder, final SpacecraftState estimated,
                              final RealMatrix covEstimated) {

        // Covariance data
        final CartesianCovariance covariance = NdmUtils.createCartesianCovariance(estimated.getDate(),
                                                                                  estimated.getFrame(),
                                                                                  covEstimated);

        // Create the data
        final OpmData data = new OpmData(NdmUtils.createStateVector(estimated.getPVCoordinates()),
                                         NdmUtils.createKeplerianElements(estimated.getOrbit()),
                                         null,
                                         covariance,
                                         new ArrayList<>(), // Empty list of maneuvers
                                         null,
                                         estimated.getMass());

        // Opm
        final Opm opm = new Opm(NdmUtils.createHeader(),
                                NdmUtils.createOpmSegments(NdmUtils.createCommonMetadata(CenterName.EARTH,
                                                                                         inputData.getSpacecraft().getName(),
                                                                                         inputData.getSpacecraft().getId(),
                                                                                         estimated.getFrame()),
                                                           data),
                                IERSConventions.IERS_2010, DataContext.getDefault(), Constants.WGS84_EARTH_MU);

        // Write the file
        NdmUtils.writeOPM(ndmWriterBuilder.buildOpmWriter(), opm, new File(home, OUTPUT_OPM));

    }

    /**
     * Write the output OEM file.
     * @param home home directory
     * @param inputData input data
     * @param step step for OEm generation (s)
     * @param duration duration of the OEM generation (s)
     * @param propagator estimated propagator
     * @param ndmWriterBuilder builder for NDM files
     */
    private void writeOemFile(final File home, final TutorialOrbitDetermination inputData,
                              final double step, final double duration,
                              final WriterBuilder ndmWriterBuilder, final Propagator propagator)
        throws IOException {

        // Output file
        final File oemFile = new File(home, OUTPUT_OEM);

        // OEM meta data
        final OemMetadata metadataTemplate = new OemMetadata(4);
        metadataTemplate.setObjectName(inputData.getSpacecraft().getName());
        metadataTemplate.setObjectID(inputData.getSpacecraft().getId());
        metadataTemplate.setCenter(new BodyFacade("EARTH", CelestialBodyFactory.getCelestialBodies().getEarth()));
        metadataTemplate.setReferenceFrame(FrameFacade.map(propagator.getInitialState().getFrame()));
        metadataTemplate.setTimeSystem(TimeSystem.UTC);

        // Set the OEM writer to the propagator
        try (BufferedWriter     fileWriter = Files.newBufferedWriter(Paths.get(oemFile.getAbsolutePath()), StandardCharsets.UTF_8);
             Generator          generator  = new XmlGenerator(fileWriter, XmlGenerator.DEFAULT_INDENT, oemFile.getName(), true);
             StreamingOemWriter sw         = new StreamingOemWriter(generator,
                                                                    ndmWriterBuilder.buildOemWriter(),
                                                                    NdmUtils.createHeader(), metadataTemplate)) {

            // Let the propagator generate the ephemeris
            propagator.getMultiplexer().clear();
            propagator.getMultiplexer().add(step, sw.newSegment());
            propagator.propagate(propagator.getInitialState().getDate().shiftedBy(duration));

        }

    }

    /**
     * Initialize Orekit data.
     * @return home directory
     */
    private static File initializeOrekit() {
        // Configure path
        final File home       = new File(System.getProperty("user.home"));
        final File orekitData = new File(home, "orekit-data");
        // Define data provider
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));
        return home;
    }

    /**
     * Input data for the sequential-batch orbit determination tutorial.
     * <p>
     * Data are read from a YAML file.
     * </p>
     * @author Bryan Cazabonne
     */
    public static class TutorialSequentialBatch {

        /** Orbit determination data. */
        private TutorialOrbitDetermination orbitDetermination;

        /** OEM generation duration (days). */
        private double durationInDays;

        /** Time step for OEM generation (s). */
        private double outputStep;

        /**
         * Get the simulation duration.
         * @return the simulation duration (days)
         */
        public double getDurationInDays() {
            return durationInDays;
        }

        /**
         * Set the simulation duration.
         * @param durationInDays simulation duration (days)
         */
        public void setDurationInDays(final double durationInDays) {
            this.durationInDays = durationInDays;
        }

        /**
         * Get the time step for OEM generation.
         * @return the time step for OEM generation (s)
         */
        public double getOutputStep() {
            return outputStep;
        }

        /**
         * Set the time step for OEM generation.
         * @param outputStep time step for OEM generation (s)
         */
        public void setOutputStep(final double outputStep) {
            this.outputStep = outputStep;
        }

        /**
         * Get the orbit determination data.
         * @return the orbit determination data
         */
        public TutorialOrbitDetermination getOrbitDetermination() {
            return orbitDetermination;
        }

        /**
         * Set the orbit determination data.
         * @param orbitDetermination the orbit determination data to set
         */
        public void setOrbitDetermination(final TutorialOrbitDetermination orbitDetermination) {
            this.orbitDetermination = orbitDetermination;
        }

    }

}
