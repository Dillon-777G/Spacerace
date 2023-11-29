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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.stat.descriptive.StreamingStatistics;
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
import org.orekit.estimation.measurements.EstimatedMeasurement;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.estimation.measurements.Position;
import org.orekit.estimation.sequential.ConstantProcessNoise;
import org.orekit.estimation.sequential.CovarianceMatrixProvider;
import org.orekit.estimation.sequential.KalmanEstimation;
import org.orekit.estimation.sequential.KalmanObserver;
import org.orekit.estimation.sequential.SemiAnalyticalKalmanEstimator;
import org.orekit.estimation.sequential.SemiAnalyticalKalmanEstimatorBuilder;
import org.orekit.files.ilrs.CPF;
import org.orekit.files.ilrs.CPF.CPFCoordinate;
import org.orekit.files.ilrs.CPF.CPFEphemeris;
import org.orekit.files.ilrs.CPFParser;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.drag.IsotropicDrag;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.SphericalHarmonicsProvider;
import org.orekit.forces.gravity.potential.UnnormalizedSphericalHarmonicsProvider;
import org.orekit.forces.radiation.IsotropicRadiationSingleCoefficient;
import org.orekit.forces.radiation.RadiationSensitive;
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
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.PropagationType;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.conversion.ClassicalRungeKuttaIntegratorBuilder;
import org.orekit.propagation.conversion.DSSTPropagatorBuilder;
import org.orekit.propagation.conversion.ODEIntegratorBuilder;
import org.orekit.propagation.conversion.OrbitDeterminationPropagatorBuilder;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTAtmosphericDrag;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTForceModel;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTNewtonianAttraction;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTSolarRadiationPressure;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTTesseral;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTThirdBody;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTZonal;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.tutorials.estimation.common.AttitudeMode;
import org.orekit.tutorials.estimation.common.TutorialOrbitDetermination;
import org.orekit.tutorials.yaml.TutorialBody;
import org.orekit.tutorials.yaml.TutorialForceModel;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialDrag;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialGravity;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialSolarRadiationPressure;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialThirdBody;
import org.orekit.tutorials.yaml.TutorialIntegrator;
import org.orekit.tutorials.yaml.TutorialKalman;
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
 * Tutorial to present the Extended Semi-analytical Kalman Filter.
 * <p>
 * Because the DSST orbit propagator uses large step size to perform the numerical
 * integration of the equations of motion for the mean equinoctial elements
 * (e.g., half-day for GEO satellites), itvssical Extended Kalman Filter orbit determination.
 * The classical Extended Kalman Filter algorithm needs to re-initialize the orbital state
 * at each observation epoch. However, the time difference between two observations
 * is usually much smaller than the DSST step size. In order to take advantage of the
 * DSST theory within a recursive filter orbit determination, Orekit implements
 * the Extended Semi-analytical Kalman Filter.
 * <p>
 * This tutorial presents the functional validation test performed in the second reference
 * below. It corresponds to a 5-days orbit determination of Lageos 2 satellites. Lageos'
 * positions are used as observations during the estimation process.
 *
 * @see "Folcik Z., Orbit Determination Using Modern Filters/Smoothers and Continuous Thrust Modeling,
 *       Master of Science Thesis, Department of Aeronautics and Astronautics, MIT, June, 2008."
 *
 * @see "Cazabonne B., Bayard J., Journot M., and Cefola P. J., A Semi-analytical Approach for Orbit
 *       Determination based on Extended Kalman Filter, AAS Paper 21-614, AAS/AIAA Astrodynamics
 *       Specialist Conference, Big Sky, August 2021."
 *
 * @author Bryan Cazabonne
 *
 */
public class ExtendedSemianalyticalKalmanFilter {

    /** f16.9 floating value. */
    private static final String E16_9 = "%16.9e";

    /** Return. */
    private static final String RETURN = "%n";

    /** Header for covariance matrix. */
    private static final String COV_HEADER = "Estimated covariance matrix in equinoctial orbital elements";

    /** Header. */
    private static final String HEADER = "%-25s\t%16s\t%16s\t%16s";

    /** Data line. */
    private static final String DATA_LINE = "%-25s\t%16.9f\t%16.9f\t%16.9f";

    /** Suffix for the log file. */
    private static final String LOG_SUFFIX = "-log.out";

    /** Null double values are equal to 0.0 in YAML file. */
    private static final double NULL_DOUBLE = 0.0;

    /**
     * Main method.
     * @param args nothing is needed
     * @throws IOException if observations file cannot be read properly
     * @throws URISyntaxException if URI syntax of SP3 file is wrong
     */
    public static void main(final String[] args) throws URISyntaxException, IOException {

        // input in tutorial resources directory
        final String inputPath = GNSSOrbitDetermination.class.getClassLoader().
                                 getResource("eskf/eskf-orbit-determination.yaml").toURI().getPath();

        // Run the program
        new ExtendedSemianalyticalKalmanFilter().run(new File(inputPath));

    }

    /**
     * Run the program.
     * @param input input file
     * @throws IOException if observations file cannot be read properly
     * @throws URISyntaxException if URI syntax of CPF file is wrong
     */
    private void run(final File input) throws URISyntaxException, IOException {

        // Configure Orekit
        final File home = initializeOrekit();

        // Read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialOrbitDetermination inputData = mapper.readValue(input, TutorialOrbitDetermination.class);

        // Log file (output file)
        final String baseName;
        final PrintStream logStream;
        if (inputData.getOutputBaseName() != null) {
            baseName  = inputData.getOutputBaseName();
            logStream = new PrintStream(new File(home, baseName + LOG_SUFFIX), StandardCharsets.UTF_8.name());
        } else {
            baseName  = null;
            logStream = null;
        }

        try {

            // Observations
            final List<CPFEphemeris> observations = initializeObservations(inputData, input);

            // Central body
            final OneAxisEllipsoid centralBody = initializeBody(inputData);

            // Gravity field
            final UnnormalizedSphericalHarmonicsProvider gravityField = initializeGravityField(inputData);

            // Initial orbit
            final Orbit initialOrbit = initializeOrbit(inputData, observations, gravityField);

            // Initialize propagator
            final OrbitDeterminationPropagatorBuilder propagator = initializePropagator(inputData, initialOrbit, centralBody, gravityField);

            // Measurements
            final List<ObservedMeasurement<?>> measurements = initializeMeasurements(inputData, observations, initialOrbit);

            // Covariance
            final CovarianceMatrixProvider provider = buildCovarianceProvider(inputData, initialOrbit);

            // Create estimator and run it
            final Observer observer = initializeEstimator(propagator, measurements, provider, logStream);

            // Print some results
            System.out.println("");
            System.out.println("FINAL RESULTS");
            System.out.println("=============");
            // Print the statistics
            observer.printXStatistics();
            observer.printYStatistics();
            observer.printZStatistics();
            observer.printCovarianceMatrix();

        } finally {
            if (logStream != null) {
                logStream.close();
            }
        }

    }

    /**
     * Initialize the Position/Velocity observations.
     * @param inputData input data
     * @param input input file
     * @return the ephemeris contained in the input files
     * @throws IOException if observations file cannot be read properly
     * @throws URISyntaxException if URI syntax is wrong
     */
    private static List<CPFEphemeris> initializeObservations(final TutorialOrbitDetermination inputData,
                                                             final File input)
        throws URISyntaxException, IOException {

        // Initialize list
        final List<CPFEphemeris> ephemeris = new ArrayList<>();

        // Loop on measurement files
        for (final String fileName : inputData.getMeasurements().getMeasurementFiles()) {

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

        }

        // Return the list
        return ephemeris;
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
     * Initialize the spherical harmonics provider.
     * @param inputData input data
     * @return a configured spherical harmonics provider
     */
    private static UnnormalizedSphericalHarmonicsProvider initializeGravityField(final TutorialOrbitDetermination inputData) {
        // Gravity data
        final TutorialGravity gravity = inputData.getPropagator().getForceModels().getGravity();
        final int degree = gravity.getDegree();
        final int order  = FastMath.min(degree, gravity.getOrder());
        // Return the unnormalized provider
        return GravityFieldFactory.getUnnormalizedProvider(degree, order);
    }

    /**
     * Initialize initial guess.
     * <p>
     * Initial guess corresponds to the first orbit in the CPF file.
     * It is converted in EME2000 frame.
     * </p>
     * @param inputData tutorial input data
     * @param ephemeris CPF ephemeris
     * @param gravityField gravity field (used for the central attraction coefficient)
     * @return the configured orbit
     */
    private static Orbit initializeOrbit(final TutorialOrbitDetermination inputData,
                                         final List<CPFEphemeris> ephemeris,
                                         final SphericalHarmonicsProvider gravityField) {

        // Input orbit data
        final TutorialOrbit orbit = inputData.getOrbit();

        // Central attration coefficient
        final double mu = gravityField.getMu();

        // Verify if an input orbit has been defined
        // If not, we use the first orbit entry in the CPF file
        if (orbit != null) {

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

        } else {

            // We use the first entry of the CPF ephemeris

            // Frame
            final Frame orbitFrame = FramesFactory.getEME2000();

            // We suppose (as written in the input file) that CPF ephemeris are chronologically ordered
            final CPFEphemeris first = ephemeris.get(0);

            // Bounded propagator from the CPF file
            final BoundedPropagator bounded = first.getPropagator();

            // Initial date
            final AbsoluteDate initialDate = bounded.getMinDate();

            // Initial orbit
            final TimeStampedPVCoordinates pvInitITRF = bounded.getPVCoordinates(initialDate, first.getFrame());
            final TimeStampedPVCoordinates pvInitInertial = first.getFrame().getTransformTo(orbitFrame, initialDate).
                                                                                 transformPVCoordinates(pvInitITRF);

            // Return orbit (in J2000 frame)
            return new CartesianOrbit(new TimeStampedPVCoordinates(pvInitInertial.getDate(),
                                                                   new Vector3D(pvInitInertial.getPosition().getX(),
                                                                                pvInitInertial.getPosition().getY(),
                                                                                pvInitInertial.getPosition().getZ()),
                                                                   new Vector3D(pvInitInertial.getVelocity().getX(),
                                                                                pvInitInertial.getVelocity().getY(),
                                                                                pvInitInertial.getVelocity().getZ())),
                                      orbitFrame, gravityField.getMu());

        }

    }

    /**
     * Initialize the propagator builder.
     * @param inputData input data
     * @param orbit initial guess
     * @param centralBody central body
     * @param gravityField gravity field
     * @return a configured propagator builder
     */
    private static OrbitDeterminationPropagatorBuilder initializePropagator(final TutorialOrbitDetermination inputData,
                                                                            final Orbit orbit,
                                                                            final OneAxisEllipsoid centralBody,
                                                                            final UnnormalizedSphericalHarmonicsProvider gravityField) {

        // Propagator configuration
        final TutorialPropagator propagatorData = inputData.getPropagator();

        // Initialize numerical integrator
        final TutorialIntegrator integratorData = propagatorData.getIntegrator();
        final ODEIntegratorBuilder integrator = new ClassicalRungeKuttaIntegratorBuilder(integratorData.getFixedStep());

        // Initialize the builder
        final OrbitDeterminationPropagatorBuilder builder;

        // Convert initial orbit in equinoctial elements
        final EquinoctialOrbit equinoctial = (EquinoctialOrbit) OrbitType.EQUINOCTIAL.convertType(orbit);

        // Initialize the numerical builder
        final DSSTPropagatorBuilder propagator = new DSSTPropagatorBuilder(equinoctial, integrator, integratorData.getPositionError(), PropagationType.MEAN, PropagationType.OSCULATING);

        // Add the force models to the DSST propagator
        addDSSTForceModels(propagatorData.getForceModels(), propagator, centralBody, gravityField);

        // Attitude mode
        final AttitudeMode mode;
        if (inputData.getSpacecraft().getAttitudeMode() != null) {
            mode = AttitudeMode.valueOf(inputData.getSpacecraft().getAttitudeMode());
        } else {
            mode = AttitudeMode.DEFAULT_LAW;
        }
        propagator.setAttitudeProvider(mode.getProvider(orbit.getFrame(), centralBody));

        // Mass
        propagator.setMass(inputData.getSpacecraft().getMass());

        // Set
        builder = propagator;

        // Reset the orbit
        builder.resetOrbit(equinoctial);

        // Return the fully configured propagator builder
        return builder;

    }

    /**
     * Add the force models to the DSST propagator.
     * @param forceModelData force model data
     * @param propagator propagator
     * @param centralBody central body
     * @param gravityField gravity field
     */
    private static void addDSSTForceModels(final TutorialForceModel forceModelData,
                                           final DSSTPropagatorBuilder propagator,
                                           final OneAxisEllipsoid centralBody,
                                           final UnnormalizedSphericalHarmonicsProvider gravityField) {

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

            // Drag force
            // Assuming spherical satellite
            final DSSTForceModel force = new DSSTAtmosphericDrag(new DragForce(atmosphere, new IsotropicDrag(area, cd)), gravityField.getMu());
            for (final ParameterDriver driver : force.getParametersDrivers()) {
                if (driver.getName().equals(DragSensitive.DRAG_COEFFICIENT)) {
                    if (cdEstimated) {
                        driver.setSelected(true);
                    }
                }
            }

            // Add the force model
            propagator.addForceModel(force);

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
            final DSSTForceModel force = new DSSTSolarRadiationPressure(CelestialBodyFactory.getSun(), gravityField.getAe(), spacecraft, gravityField.getMu());
            for (final ParameterDriver driver : force.getParametersDrivers()) {
                if (driver.getName().equals(RadiationSensitive.REFLECTION_COEFFICIENT)) {
                    if (cREstimated) {
                        driver.setSelected(true);
                    }
                }
            }

            // Add the force model
            propagator.addForceModel(force);

        }

        // Third bodies
        for (TutorialThirdBody thirdBody : forceModelData.getThirdBody()) {
            final CelestialBody body = CelestialBodyFactory.getBody(thirdBody.getName());
            propagator.addForceModel(new DSSTThirdBody(body, gravityField.getMu()));
        }

        // Potential
        propagator.addForceModel(new DSSTTesseral(centralBody.getBodyFrame(), Constants.WGS84_EARTH_ANGULAR_VELOCITY, gravityField));
        propagator.addForceModel(new DSSTZonal(gravityField));

        // Newton
        propagator.addForceModel(new DSSTNewtonianAttraction(gravityField.getMu()));

    }

    /**
     * Initialize the list of measurements.
     * @param inputData input data
     * @param ephemeris CPF ephemeris
     * @param orbit initial guess (used for orbit determination epoch)
     * @return the list of measurements
     */
    private static List<ObservedMeasurement<?>> initializeMeasurements(final TutorialOrbitDetermination inputData,
                                                                       final List<CPFEphemeris> ephemeris,
                                                                       final Orbit orbit) {

        // Tutorial measurement data
        final TutorialPosition measurementData = inputData.getMeasurements().getPosition();

        // Satellite
        final ObservableSatellite satellite = new ObservableSatellite(0);

        // Initialize an empty list of measurements
        final List<ObservedMeasurement<?>> measurements = new ArrayList<>();

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
     * Initialize the estimator used for the orbit determination and run the estimation.
     * @param propagator orbit propagator
     * @param measurements list of measurements
     * @param provider covariance matrix provider
     * @param logStream output log file
     * @return the kalman observer
     */
    private static Observer initializeEstimator(final OrbitDeterminationPropagatorBuilder propagator,
                                                final List<ObservedMeasurement<?>> measurements,
                                                final CovarianceMatrixProvider provider,
                                                final PrintStream logStream) {

        // Initialize builder
        final SemiAnalyticalKalmanEstimatorBuilder builder = new SemiAnalyticalKalmanEstimatorBuilder();

        // Add the propagation configuration
        builder.addPropagationConfiguration((DSSTPropagatorBuilder) propagator, provider);

        // Build filter
        final SemiAnalyticalKalmanEstimator estimator = builder.build();

        // Add observer
        final Observer observer = new Observer(logStream);
        estimator.setObserver(observer);

        // Estimation
        estimator.processMeasurements(measurements);

        // Return the observer
        return observer;

    }

    /**
     * Build the covariance matrix provider.
     * @param inputData input data
     * @param initialOrbit initial orbit
     * @return the covariance matrix provider
     */
    private static CovarianceMatrixProvider buildCovarianceProvider(final TutorialOrbitDetermination inputData,
                                                                    final Orbit initialOrbit) {

        // Kalman filter data
        final TutorialKalman kalmanData = inputData.getKalman();

        // Initial cartesian covariance matrix and process noise matrix
        final RealMatrix cartesianOrbitalP = MatrixUtils.createRealDiagonalMatrix(kalmanData.getCartesianOrbitalP());
        final RealMatrix cartesianOrbitalQ = MatrixUtils.createRealDiagonalMatrix(kalmanData.getCartesianOrbitalQ());

        // Initial propagation covariance matrix and process noise matrix
        final RealMatrix propagationP = (kalmanData.getPropagationP().length) != 0 ?
                                                            MatrixUtils.createRealDiagonalMatrix(kalmanData.getPropagationP()) : null;
        final RealMatrix propagationQ = (kalmanData.getPropagationQ().length) != 0 ?
                                                            MatrixUtils.createRealDiagonalMatrix(kalmanData.getPropagationQ()) : null;


        // Jacobian of the orbital parameters w/r to Cartesian
        final Orbit orbit = OrbitType.EQUINOCTIAL.convertType(initialOrbit);
        final double[][] dYdC = new double[6][6];
        orbit.getJacobianWrtCartesian(PositionAngle.TRUE, dYdC);
        final RealMatrix Jac = MatrixUtils.createRealMatrix(dYdC);

        // Equinoctial initial covariance matrix
        final RealMatrix orbitalP = Jac.multiply(cartesianOrbitalP.multiply(Jac.transpose()));

        // Build the full covariance matrix and process noise matrix
        final int nbPropag = (propagationP != null) ? propagationP.getRowDimension() : 0;
        final RealMatrix initialP = MatrixUtils.createRealMatrix(6 + nbPropag,
                                                                 6 + nbPropag);
        final RealMatrix Q = MatrixUtils.createRealMatrix(6 + nbPropag,
                                                          6 + nbPropag);

        // Orbital part
        initialP.setSubMatrix(orbitalP.getData(), 0, 0);
        Q.setSubMatrix(cartesianOrbitalQ.getData(), 0, 0);

        // Propagation part
        if (propagationP != null) {
            initialP.setSubMatrix(propagationP.getData(), 6, 6);
            Q.setSubMatrix(propagationQ.getData(), 6, 6);
        }

        // Return
        return new ConstantProcessNoise(initialP, Q);
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

    /** Observer for Kalman estimation. */
    public static class Observer implements KalmanObserver {

        /** X Pos. residuals. */
        private static final String X_RES = "X Pos. residuals";

        /** Y Pos. residuals. */
        private static final String Y_RES = "Y Pos. residuals";

        /** Z Pos. residuals. */
        private static final String Z_RES = "Z Pos. residuals ";

        /** Residuals line. */
        private static final String RESIDUAL_LINE = "   Min value (m):  %10.6e" + RETURN +
                                                    "   Max value (m):  %10.6e" + RETURN +
                                                    "   Mean value (m): %10.6e" + RETURN +
                                                    "   STD (m):        %10.6e" + RETURN;

        /** Statistics on X position residuals. */
        private StreamingStatistics statX;

        /** Statistics on Y position residuals. */
        private StreamingStatistics statY;

        /** Statistics on Z position residuals. */
        private StreamingStatistics statZ;

        /** Kalman estimation. */
        private KalmanEstimation kalmanEstimation;

        /** Log file. */
        private PrintStream logStream;

        /** Constructor.
         * @param logStream LOG file
         */
        public Observer(final PrintStream logStream) {
            this.logStream = logStream;
            statX = new StreamingStatistics();
            statY = new StreamingStatistics();
            statZ = new StreamingStatistics();
            final String header = String.format(Locale.US, HEADER, "Epoch", "X residual (m)", "Y residual (m)", "Z residual (m)");
            if (logStream != null) {
                logStream.format(header);
                logStream.format(RETURN);
            }
            System.out.println(header);
        }

        /** {@inheritDoc} */
        @Override
        public void evaluationPerformed(final KalmanEstimation estimation) {

            // Estimated and observed measurements
            final EstimatedMeasurement<?> estimatedMeasurement = estimation.getCorrectedMeasurement();

            // Check
            if (estimatedMeasurement.getObservedMeasurement().getMeasurementType().equals(Position.MEASUREMENT_TYPE)) {

                if (estimatedMeasurement.getStatus() == EstimatedMeasurement.Status.REJECTED) {
                    System.out.println(estimatedMeasurement.getDate() + "  REJECTED");
                } else {
                    final double[] estimated = estimatedMeasurement.getEstimatedValue();
                    final double[] observed  = estimatedMeasurement.getObservedValue();

                    // Calculate residuals
                    final double resX  = estimated[0] - observed[0];
                    final double resY  = estimated[1] - observed[1];
                    final double resZ  = estimated[2] - observed[2];
                    statX.addValue(resX);
                    statY.addValue(resY);
                    statZ.addValue(resZ);

                    // Add measurement line
                    final String line = String.format(Locale.US, DATA_LINE, estimatedMeasurement.getDate(), resX, resY, resZ);
                    if (logStream != null) {
                        logStream.format(line);
                        logStream.format(RETURN);
                    }
                    System.out.println(line);

                }

            }

            this.kalmanEstimation = estimation;

        }

        /**
         * Print the statistics on the X coordinate residuals.
         */
        public void printXStatistics() {
            System.out.println("");
            System.out.println(X_RES);
            final String line = String.format(Locale.US, RESIDUAL_LINE, statX.getMin(), statX.getMax(), statX.getMean(), statX.getStandardDeviation());
            System.out.println(line);
            if (logStream != null) {
                logStream.format(RETURN);
                logStream.format(X_RES + RETURN);
                logStream.format(line + RETURN);
            }
        }

        /**
         * Print the statistics on the Y coordinate residuals.
         */
        public void printYStatistics() {
            System.out.println("");
            System.out.println(Y_RES);
            final String line = String.format(Locale.US, RESIDUAL_LINE, statY.getMin(), statY.getMax(), statY.getMean(), statY.getStandardDeviation());
            System.out.println(line);
            if (logStream != null) {
                logStream.format(RETURN);
                logStream.format(Y_RES + RETURN);
                logStream.format(line + RETURN);
            }
        }

        /**
         * Print the statistics on the Z coordinate residuals.
         */
        public void printZStatistics() {
            System.out.println("");
            System.out.println(Z_RES);
            final String line = String.format(Locale.US, RESIDUAL_LINE, statZ.getMin(), statZ.getMax(), statZ.getMean(), statZ.getStandardDeviation());
            System.out.println(line);
            if (logStream != null) {
                logStream.format(RETURN);
                logStream.format(Z_RES + RETURN);
                logStream.format(line + RETURN);
            }
        }

        /**
         * Print the physical estimated covariance matrix.
         */
        public void printCovarianceMatrix() {

            // Covariance matrix
            final RealMatrix cov = kalmanEstimation.getPhysicalEstimatedCovarianceMatrix();

            // Fill the covariance matrix
            System.out.println("");
            System.out.println(COV_HEADER);
            final StringBuilder covToPrint = new StringBuilder();
            for (int row = 0; row < cov.getRowDimension(); row++) {
                for (int column = 0; column < cov.getColumnDimension(); column++) {
                    covToPrint.append(String.format(Locale.US, E16_9, cov.getEntry(row, column)));
                    covToPrint.append(" ");
                }
                covToPrint.append("\n");
            }

            // Print
            System.out.println(covToPrint);
            if (logStream != null) {
                logStream.format(RETURN);
                logStream.format(COV_HEADER);
                logStream.format(RETURN);
                logStream.format(covToPrint.toString());
            }

        }

    }

}
