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
package org.orekit.tutorials.propagation;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.AbstractIntegrator;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.drag.IsotropicDrag;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.gravity.potential.UnnormalizedSphericalHarmonicsProvider;
import org.orekit.forces.radiation.IsotropicRadiationSingleCoefficient;
import org.orekit.forces.radiation.RadiationSensitive;
import org.orekit.forces.radiation.SolarRadiationPressure;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.models.earth.atmosphere.Atmosphere;
import org.orekit.models.earth.atmosphere.HarrisPriester;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.CircularOrbit;
import org.orekit.orbits.EquinoctialOrbit;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.PropagationType;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.propagation.semianalytical.dsst.DSSTPropagator;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTAtmosphericDrag;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTSolarRadiationPressure;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTTesseral;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTThirdBody;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTZonal;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.tutorials.yaml.TutorialBody;
import org.orekit.tutorials.yaml.TutorialForceModel;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialDrag;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialGravity;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialSolarRadiationPressure;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialThirdBody;
import org.orekit.tutorials.yaml.TutorialIntegrator;
import org.orekit.tutorials.yaml.TutorialOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialCartesianOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialCircularOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialEquinoctialOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialKeplerianOrbit;
import org.orekit.tutorials.yaml.TutorialPropagator;
import org.orekit.tutorials.yaml.TutorialSpacecraft;
import org.orekit.utils.Constants;
import org.orekit.utils.DoubleArrayDictionary;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/** Orekit tutorial for semi-analytical extrapolation using the DSST.
 *  <p>
 *  The parameters are read from the input file dsst-propagation.yaml located in the user's
 *  home directory (see commented example at src/tutorial/ressources/dsst-propagation.yaml).
 *  The results are written to the ouput file dsst-propagation.out in the same directory.
 *  </p>
 *  <p>
 *  Comparison between the DSST propagator and the numerical propagator can be optionally
 *  performed. Numerical results are  written to the ouput file numerical-propagation.out.
 *  </p>
 *
 *  @author Romain Di Costanzo
 *  @author Pascal Parraud
 */
public class DSSTPropagation {

    /** Null integer value read in the YAML file. */
    private static final int NULL_INT = 0;

    /** Null double value read in the YAML file. */
    private static final double NULL_DOUBLE = 0.0;

    /** Warning message. */
    private final String WARNING = "WARNING:";

    /** Private constructor for utility class. */
    private DSSTPropagation() {
        // empty
    }

    /** Program entry point.
     * @param args program arguments
     */
    public static void main(final String[] args) {
        try {

            // configure Orekit
            final File home       = new File(System.getProperty("user.home"));
            final File orekitData = new File(home, "orekit-data");
            if (!orekitData.exists()) {
                System.err.format(Locale.US, "Failed to find %s folder%n",
                                  orekitData.getAbsolutePath());
                System.err.format(Locale.US, "You need to download %s from %s, unzip it in %s and rename it 'orekit-data' for this tutorial to work%n",
                                  "orekit-data-master.zip", "https://gitlab.orekit.org/orekit/orekit-data/-/archive/master/orekit-data-master.zip",
                                  home.getAbsolutePath());
                System.exit(1);
            }
            final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
            manager.addProvider(new DirectoryCrawler(orekitData));

            // input/output (in user's home directory)
            final File input  = new File(DSSTPropagation.class.getResource("/dsst-propagation.yaml").toURI().getPath());
            final File output = new File(input.getParentFile(), "dsst-propagation.out");

            new DSSTPropagation().run(input, output);

        } catch (URISyntaxException | IOException | OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    /** Run the program.
     * @param input input file
     * @param output output file
     * @throws IOException if input file cannot be read
     */
    private void run(final File input, final File output)
            throws IOException {

        // read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialDSSTPropagation inputData = mapper.readValue(input, TutorialDSSTPropagation.class);

        // check mandatory input parameters
        if (inputData.getOrbit().getDate() == null) {
            throw new IOException("Orbit date is not defined.");
        }
        if (inputData.getDurationInDays() == NULL_INT) {
            throw new IOException("Propagation duration is not defined.");
        }

        // All dates in UTC
        final TimeScale utc = TimeScalesFactory.getUTC();

        final double rotationRate = Constants.WGS84_EARTH_ANGULAR_VELOCITY;

        // Propagator data
        final TutorialPropagator propagatorData = inputData.getPropagator();

        final int degree = propagatorData.getForceModels().getGravity().getDegree();
        final int order  = FastMath.min(degree, propagatorData.getForceModels().getGravity().getOrder());

        // Potential coefficients providers
        final UnnormalizedSphericalHarmonicsProvider unnormalized =
                GravityFieldFactory.getConstantUnnormalizedProvider(degree, order);
        final NormalizedSphericalHarmonicsProvider normalized =
                GravityFieldFactory.getConstantNormalizedProvider(degree, order);

        // Central body attraction coefficient (m³/s²)
        final double mu = unnormalized.getMu();

        // Earth frame definition
        final TutorialBody bodyData = inputData.getBody();
        final Frame earthFrame;
        if (bodyData.getFrameName() == null) {
            earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
        } else {
            earthFrame = bodyData.getEarthFrame();
        }

        // Orbit definition
        final Orbit orbit = createOrbit(inputData, utc, mu);

        // DSST propagator definition
        final TutorialSpacecraft spacecraftData = inputData.getSpacecraft();
        double mass = 1000.0;
        if (spacecraftData != null && spacecraftData.getMass() != NULL_DOUBLE) {
            mass = spacecraftData.getMass();
        }
        final PropagationType initialType = inputData.isInitialOrbitIsOsculating() ?
                                                               PropagationType.OSCULATING : PropagationType.MEAN;
        final PropagationType outputType = inputData.isOutputOrbitIsOsculating() ?
                                                               PropagationType.OSCULATING : PropagationType.MEAN;
        List<String> shortPeriodCoefficients = null;
        if (inputData.getOutputShortPeriodCoefficients() != null) {
            shortPeriodCoefficients = inputData.getOutputShortPeriodCoefficients();
            if (shortPeriodCoefficients.size() == 1 && shortPeriodCoefficients.get(0).equalsIgnoreCase("all")) {
                // special case, we use the empty list to represent all possible (unknown) keys
                // we don't use Collections.emptyList() because we want the list to be populated later on
                shortPeriodCoefficients = new ArrayList<String>();
            } else if (shortPeriodCoefficients.size() == 1 && shortPeriodCoefficients.get(0).equalsIgnoreCase("none")) {
                // special case, we use null to select no coefficients at all
                shortPeriodCoefficients = null;
            } else {
                // general case, we have an explicit list of coefficients names
                Collections.sort(shortPeriodCoefficients);
            }
            if (shortPeriodCoefficients != null && outputType != PropagationType.OSCULATING) {
                System.out.println();
                System.out.println(WARNING);
                System.out.println("Short periodic coefficients can be output only if output orbit is osculating.");
                System.out.println("No coefficients will be computed here.\n");
            }
        }
        final TutorialIntegrator integratorData = propagatorData.getIntegrator();
        double fixedStepSize = -1.;
        double minStep       =  6000.0;
        double maxStep       = 86400.0;
        double dP            =     1.0;
        if (integratorData.getFixedStep() != NULL_DOUBLE) {
            fixedStepSize = integratorData.getFixedStep();
        } else {
            if (integratorData.getMinStep() != NULL_DOUBLE) {
                minStep = integratorData.getMinStep();
            }
            if (integratorData.getMaxStep() != NULL_DOUBLE) {
                maxStep = integratorData.getMaxStep();
            }
            if (integratorData.getPositionError() != NULL_DOUBLE) {
                dP = integratorData.getPositionError();
            }
        }
        final DSSTPropagator dsstProp = createDSSTProp(orbit, mass,
                                                       initialType, outputType,
                                                       fixedStepSize, minStep, maxStep, dP,
                                                       shortPeriodCoefficients);

        if (inputData.getFixedNumberOfInterpolationPoints() != NULL_INT) {
            if (inputData.getMaxTimeGapBetweenInterpolationPoints() != NULL_DOUBLE) {
                throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                          "cannot specify both fixed.number.of.interpolation.points" +
                                          " and max.time.gap.between.interpolation.points");
            }
            dsstProp.setInterpolationGridToFixedNumberOfPoints(inputData.getFixedNumberOfInterpolationPoints());
        } else if (inputData.getMaxTimeGapBetweenInterpolationPoints() != NULL_DOUBLE) {
            dsstProp.setInterpolationGridToMaxTimeGap(inputData.getMaxTimeGapBetweenInterpolationPoints());
        } else {
            dsstProp.setInterpolationGridToFixedNumberOfPoints(3);
        }

        // Set Force models
        setForceModel(inputData, unnormalized, earthFrame, rotationRate, dsstProp);

        // Simulation properties
        final AbsoluteDate start = orbit.getDate();
        double duration = 0.;
        if (inputData.getDurationInDays() != NULL_DOUBLE) {
            duration = inputData.getDurationInDays() * Constants.JULIAN_DAY;
        }
        final double outStep = inputData.getOutputStep();
        final boolean displayKeplerian = true;
        final boolean displayEquinoctial = true;
        final boolean displayCartesian = true;

        // DSST Propagation
        final EphemerisGenerator dsstGenerator = dsstProp.getEphemerisGenerator();
        final double dsstOn = System.currentTimeMillis();
        dsstProp.propagate(start, start.shiftedBy(duration));
        final double dsstOff = System.currentTimeMillis();
        System.out.println("DSST execution time (without large file write) : " + (dsstOff - dsstOn) / 1000.);
        System.out.println("writing file...");
        final BoundedPropagator dsstEphem = dsstGenerator.getGeneratedEphemeris();
        dsstEphem.getMultiplexer().add(outStep, new OutputHandler(output,
                                                           displayKeplerian, displayEquinoctial, displayCartesian,
                                                           shortPeriodCoefficients));
        dsstEphem.propagate(start, start.shiftedBy(duration));
        System.out.println("DSST results saved as file " + output);

        // Check if we want to compare numerical to DSST propagator (default is false)
        if (inputData.isNumericalComparison()) {

            if (outputType == PropagationType.MEAN) {
                System.out.println();
                System.out.println(WARNING);
                System.out.println("The DSST propagator considers a mean orbit while the numerical will consider an osculating one.");
                System.out.println("The comparison will be meaningless.\n");
            }

            // Numerical propagator definition
            final NumericalPropagator numProp = createNumProp(orbit, mass);

            // Set Force models
            setForceModel(inputData, normalized, earthFrame, numProp);

            // Numerical Propagation without output
            final EphemerisGenerator numGenerator = numProp.getEphemerisGenerator();
            final double numOn = System.currentTimeMillis();
            numProp.propagate(start, start.shiftedBy(duration));
            final double numOff = System.currentTimeMillis();
            System.out.println("Numerical execution time (including output): " + (numOff - numOn) / 1000.);

            // Add output
            final BoundedPropagator numEphemeris = numGenerator.getGeneratedEphemeris();
            final File numOutput = new File(input.getParentFile(), "numerical-propagation.out");
            numEphemeris.getMultiplexer().add(outStep, new OutputHandler(numOutput,
                                                                  displayKeplerian, displayEquinoctial, displayCartesian,
                                                                  null));
            System.out.println("Writing file, this may take some time ...");
            numEphemeris.propagate(numEphemeris.getMaxDate());
            System.out.println("Numerical results saved as file " + numOutput);

        }

    }

    /** Create an orbit from input parameters.
     * @param inputData input data for the tutorial
     * @param scale  time scale
     * @param mu     central attraction coefficient
     * @return orbit
     * @throws NoSuchElementException if input parameters are missing
     * @throws IOException if input parameters are invalid
     */
    private Orbit createOrbit(final TutorialDSSTPropagation inputData,
                              final TimeScale scale, final double mu)
        throws NoSuchElementException, IOException {

        // Orbit data
        final TutorialOrbit     orbitData = inputData.getOrbit();
        final TutorialOrbitType orbitType = orbitData.getOrbitType();

        final Frame frame;
        if (orbitData.getFrameName() == null) {
            frame = FramesFactory.getEME2000();
        } else {
            frame = orbitData.getInertialFrame();
        }

        // Orbit definition
        final Orbit orbit;
        if (orbitType.getKeplerian() != null) {
            final TutorialKeplerianOrbit keplerian = orbitType.getKeplerian();
            orbit = new KeplerianOrbit(keplerian.getA(),
                                       keplerian.getE(),
                                       FastMath.toRadians(keplerian.getI()),
                                       FastMath.toRadians(keplerian.getPa()),
                                       FastMath.toRadians(keplerian.getRaan()),
                                       FastMath.toRadians(keplerian.getV()),
                                       PositionAngle.valueOf(keplerian.getPositionAngle()),
                                       frame,
                                       new AbsoluteDate(orbitData.getDate(), scale),
                                       mu
                                      );
        } else if (orbitType.getEquinoctial() != null) {
            final TutorialEquinoctialOrbit equinoctial = orbitType.getEquinoctial();
            orbit = new EquinoctialOrbit(equinoctial.getA(),
                                         equinoctial.getEx(),
                                         equinoctial.getEy(),
                                         equinoctial.getHx(),
                                         equinoctial.getHy(),
                                         FastMath.toRadians(equinoctial.getLv()),
                                         PositionAngle.valueOf(equinoctial.getPositionAngle()),
                                         frame,
                                         new AbsoluteDate(orbitData.getDate(), scale),
                                         mu
                                        );
        } else if (orbitType.getCircular() != null) {
            final TutorialCircularOrbit circular = orbitType.getCircular();
            orbit = new CircularOrbit(circular.getA(),
                                      circular.getEx(),
                                      circular.getEy(),
                                      FastMath.toRadians(circular.getI()),
                                      FastMath.toRadians(circular.getRaan()),
                                      FastMath.toRadians(circular.getAlphaV()),
                                      PositionAngle.valueOf(circular.getPositionAngle()),
                                      frame,
                                      new AbsoluteDate(orbitData.getDate(), scale),
                                      mu
                                     );
        } else if (orbitType.getCartesian() != null) {
            final TutorialCartesianOrbit cartesian = orbitType.getCartesian();
            final double[] pos = {cartesian.getX(),
                                  cartesian.getY(),
                                  cartesian.getZ()};
            final double[] vel = {cartesian.getVx(),
                                  cartesian.getVy(),
                                  cartesian.getVz()};
            orbit = new CartesianOrbit(new PVCoordinates(new Vector3D(pos), new Vector3D(vel)),
                                       frame,
                                       new AbsoluteDate(orbitData.getDate(), scale),
                                       mu
                                      );
        } else {
            throw new IOException("Orbit definition is incomplete.");
        }

        return orbit;

    }

    /** Set up the DSST Propagator.
     *
     *  @param orbit initial orbit
     *  @param mass S/C mass (kg)
     *  @param initialType type of initial orbital elements
     *  @param outputType if we want to output osculating parameters
     *  @param fixedStepSize step size for fixed step integrator (s)
     *  @param minStep minimum step size, if step is not fixed (s)
     *  @param maxStep maximum step size, if step is not fixed (s)
     *  @param dP position tolerance for step size control, if step is not fixed (m)
     *  @param shortPeriodCoefficients list of short periodic coefficients
     *  to output (null means no coefficients at all, empty list means all
     *  possible coefficients)
     *  @return DSST propagator
     */
    private DSSTPropagator createDSSTProp(final Orbit orbit,
                                          final double mass,
                                          final PropagationType initialType,
                                          final PropagationType outputType,
                                          final double fixedStepSize,
                                          final double minStep,
                                          final double maxStep,
                                          final double dP,
                                          final List<String> shortPeriodCoefficients) {
        final AbstractIntegrator integrator;
        if (fixedStepSize > 0.) {
            integrator = new ClassicalRungeKuttaIntegrator(fixedStepSize);
        } else {
            final double[][] tol = DSSTPropagator.tolerances(dP, orbit);
            integrator = new DormandPrince853Integrator(minStep, maxStep, tol[0], tol[1]);
            ((AdaptiveStepsizeIntegrator) integrator).setInitialStepSize(10. * minStep);
        }

        final DSSTPropagator dsstProp = new DSSTPropagator(integrator, outputType);
        dsstProp.setInitialState(new SpacecraftState(orbit, mass), initialType);
        dsstProp.setSelectedCoefficients(shortPeriodCoefficients == null ?
                                         null : new HashSet<String>(shortPeriodCoefficients));

        return dsstProp;
    }

    /** Create the numerical propagator.
     *  @param orbit initial orbit
     *  @param mass S/C mass (kg)
     *  @return numerical propagator
     */
    private NumericalPropagator createNumProp(final Orbit orbit, final double mass) {
        final double[][] tol = NumericalPropagator.tolerances(1.0, orbit, orbit.getType());
        final double minStep = 1.e-3;
        final double maxStep = 1.e+3;
        final AdaptiveStepsizeIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tol[0], tol[1]);
        integrator.setInitialStepSize(100.);

        final NumericalPropagator numProp = new NumericalPropagator(integrator);
        numProp.setInitialState(new SpacecraftState(orbit, mass));

        return numProp;
    }

    /** Set DSST propagator force models.
     *  @param inputData input data for the tutorial
     *  @param unnormalized spherical harmonics provider
     *  @param earthFrame Earth rotating frame
     *  @param rotationRate central body rotation rate (rad/s)
     *  @param dsstProp DSST propagator
     */
    private void setForceModel(final TutorialDSSTPropagation inputData,
                               final UnnormalizedSphericalHarmonicsProvider unnormalized,
                               final Frame earthFrame, final double rotationRate,
                               final DSSTPropagator dsstProp) {

        // Force model data
        final TutorialForceModel forceModelData = inputData.getPropagator().getForceModels();
        final double ae = unnormalized.getAe();
        final double mu = unnormalized.getMu();

        // Central Body Force Model with un-normalized coefficients
        dsstProp.addForceModel(new DSSTZonal(unnormalized));
        dsstProp.addForceModel(new DSSTTesseral(earthFrame, rotationRate, unnormalized));

        // 3rd body
        if (forceModelData.getThirdBody() != null) {
            for (TutorialThirdBody thirdBody : forceModelData.getThirdBody()) {
                dsstProp.addForceModel(new DSSTThirdBody(CelestialBodyFactory.getBody(thirdBody.getName()), mu));
            }
        }

        // Drag
        if (forceModelData.getDrag() != null) {
            final TutorialDrag drag = forceModelData.getDrag();
            final OneAxisEllipsoid earth = new OneAxisEllipsoid(ae, Constants.WGS84_EARTH_FLATTENING, earthFrame);
            final Atmosphere atm = new HarrisPriester(CelestialBodyFactory.getSun(), earth, 6);
            dsstProp.addForceModel(new DSSTAtmosphericDrag(atm, drag.getCd().getInitialValue(),
                                                           drag.getArea(), mu));
        }

        // Solar Radiation Pressure
        if (forceModelData.getSolarRadiationPressure() != null) {
            final TutorialSolarRadiationPressure srp = forceModelData.getSolarRadiationPressure();
            dsstProp.addForceModel(new DSSTSolarRadiationPressure(srp.getCr().getInitialValue(),
                                                                  srp.getArea(),
                                                                  CelestialBodyFactory.getSun(), ae, mu));
        }

    }

    /** Set numerical propagator force models.
     *
     *  @param inputData  input data for the tutorial
     *  @param normalized spherical harmonics provider
     *  @param earthFrame Earth rotating frame
     *  @param numProp numerical propagator
     */
    private void setForceModel(final TutorialDSSTPropagation inputData,
                               final NormalizedSphericalHarmonicsProvider normalized,
                               final Frame earthFrame,
                               final NumericalPropagator numProp) {

        // Force model data
        final TutorialForceModel forceModelData = inputData.getPropagator().getForceModels();

        final double ae = normalized.getAe();

        final TutorialGravity gravity = forceModelData.getGravity();
        final int degree = gravity.getDegree();
        final int order  = gravity.getOrder();

        if (order > degree) {
            throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                      "Potential order cannot be higher than potential degree");
        }

        // Central Body (normalized coefficients)
        numProp.addForceModel(new HolmesFeatherstoneAttractionModel(earthFrame, normalized));

        // 3rd body
        if (forceModelData.getThirdBody() != null) {
            for (TutorialThirdBody thirdBody : forceModelData.getThirdBody()) {
                numProp.addForceModel(new ThirdBodyAttraction(CelestialBodyFactory.getBody(thirdBody.getName())));
            }
        }

        // Drag
        if (forceModelData.getDrag() != null) {
            final TutorialDrag drag = forceModelData.getDrag();
            final OneAxisEllipsoid earth = new OneAxisEllipsoid(ae, Constants.WGS84_EARTH_FLATTENING, earthFrame);
            final Atmosphere atm = new HarrisPriester(CelestialBodyFactory.getSun(), earth, 6);
            final DragSensitive ssc = new IsotropicDrag(drag.getArea(), drag.getCd().getInitialValue());
            numProp.addForceModel(new DragForce(atm, ssc));
        }

        // Solar Radiation Pressure
        if (forceModelData.getSolarRadiationPressure() != null) {
            final TutorialSolarRadiationPressure srp = forceModelData.getSolarRadiationPressure();
            final double cR = srp.getCr().getInitialValue();
            final RadiationSensitive ssc = new IsotropicRadiationSingleCoefficient(srp.getArea(), cR);
            numProp.addForceModel(new SolarRadiationPressure(CelestialBodyFactory.getSun(), ae, ssc));
        }
    }

    /** Specialized step handler catching the state at each step. */
    private static class OutputHandler implements OrekitFixedStepHandler {

        /** Format for delta T. */
        private static final String DT_FORMAT                             = "%20.9f";

        /** Format for 5 elements. */
        private static final String FIVE_ELEMENTS_FORMAT             = " %23.16e %23.16e %23.16e %23.16e %23.16e";

        /** Format for 6 elements. */
        private static final String SIX_ELEMENTS_FORMAT             = " %23.16e %23.16e %23.16e %23.16e %23.16e %23.16e";

        /** Output file. */
        private final File outputFile;

        /** Indicator for Keplerian elements output. */
        private final boolean outputKeplerian;

        /** Indicator for equinoctial elements output. */
        private final boolean outputEquinoctial;

        /** Indicator for Cartesian elements output. */
        private final boolean outputCartesian;

        /** Start date of propagation. */
        private AbsoluteDate start;

        /** Indicator for first step. */
        private boolean isFirst;

        /** Stream for output. */
        private PrintStream outputStream;

        /** Number of columns already declared in the header. */
        private int nbColumns;

        /** Sorted list of short period coefficients to display. */
        private List<String> shortPeriodCoefficients;

        /** Simple constructor.
         * @param outputFile output file
         * @param outputKeplerian if true, the file should contain Keplerian elements
         * @param outputEquinoctial if true, the file should contain equinoctial elements
         * @param outputCartesian if true, the file should contain Cartesian elements
         *  @param shortPeriodCoefficients list of short periodic coefficients
         *  to output (null means no coefficients at all, empty list means all
         *  possible coefficients)
         */
        private OutputHandler(final File outputFile,
                              final boolean outputKeplerian, final boolean outputEquinoctial,
                              final boolean outputCartesian, final List<String> shortPeriodCoefficients) {
            this.outputFile              = outputFile;
            this.outputKeplerian         = outputKeplerian;
            this.outputEquinoctial       = outputEquinoctial;
            this.outputCartesian         = outputCartesian;
            this.shortPeriodCoefficients = shortPeriodCoefficients;
            this.isFirst                 = true;
        }

        /** {@inheritDoc} */
        public void init(final SpacecraftState s0, final AbsoluteDate t, final double step) {
            try {
                nbColumns           = 0;
                outputStream        = new PrintStream(outputFile, StandardCharsets.UTF_8.name());
                final String sma    = "semi major axis a (km)";
                describeNextColumn("time from start (s)");
                if (outputKeplerian) {
                    describeNextColumn(sma);
                    describeNextColumn("eccentricity e");
                    describeNextColumn("inclination i (deg)");
                    describeNextColumn("right ascension of ascending node raan (deg)");
                    describeNextColumn("perigee argument (deg)");
                    describeNextColumn("mean anomaly M (deg)");
                }
                if (outputEquinoctial) {
                    if (!outputKeplerian) {
                        describeNextColumn(sma);
                    }
                    describeNextColumn("eccentricity vector component ey/h");
                    describeNextColumn("eccentricity vector component ex/k");
                    describeNextColumn("inclination vector component hy/p");
                    describeNextColumn("inclination vector component hx/q");
                    describeNextColumn("mean longitude argument L (deg)");
                }
                if (outputCartesian) {
                    describeNextColumn("position along X (km)");
                    describeNextColumn("position along Y (km)");
                    describeNextColumn("position along Z (km)");
                    describeNextColumn("velocity along X (km/s)");
                    describeNextColumn("velocity along Y (km/s)");
                    describeNextColumn("velocity along Z (km/s)");
                }
                start   = s0.getDate();
                isFirst = true;
            } catch (IOException ioe) {
                throw new OrekitException(ioe, LocalizedCoreFormats.SIMPLE_MESSAGE, ioe.getLocalizedMessage());
            }
        }

        /** Describe next column.
         * @param description column description
         */
        private void describeNextColumn(final String description) {
            outputStream.format("# %3d %s%n", ++nbColumns, description);
        }

        /** {@inheritDoc} */
        public void handleStep(final SpacecraftState s) {
            if (isFirst) {
                if (shortPeriodCoefficients != null) {
                    if (shortPeriodCoefficients.isEmpty()) {
                        // we want all available coefficients,
                        // they correspond to the additional states
                        for (final DoubleArrayDictionary.Entry entry : s.getAdditionalStatesValues().getData()) {
                            shortPeriodCoefficients.add(entry.getKey());
                        }
                        Collections.sort(shortPeriodCoefficients);
                    }
                    for (final String coefficientName : shortPeriodCoefficients) {
                        describeNextColumn(coefficientName + " (a)");
                        describeNextColumn(coefficientName + " (h)");
                        describeNextColumn(coefficientName + " (k)");
                        describeNextColumn(coefficientName + " (p)");
                        describeNextColumn(coefficientName + " (q)");
                        describeNextColumn(coefficientName + " (L)");
                    }
                }
                isFirst = false;
            }
            outputStream.format(Locale.US, DT_FORMAT, s.getDate().durationFrom(start));
            if (outputKeplerian) {
                final KeplerianOrbit ko = (KeplerianOrbit) OrbitType.KEPLERIAN.convertType(s.getOrbit());
                outputStream.format(Locale.US, SIX_ELEMENTS_FORMAT,
                                    ko.getA() / 1000.,
                                    ko.getE(),
                                    FastMath.toDegrees(ko.getI()),
                                    FastMath.toDegrees(MathUtils.normalizeAngle(ko.getRightAscensionOfAscendingNode(), FastMath.PI)),
                                    FastMath.toDegrees(MathUtils.normalizeAngle(ko.getPerigeeArgument(), FastMath.PI)),
                                    FastMath.toDegrees(MathUtils.normalizeAngle(ko.getAnomaly(PositionAngle.MEAN), FastMath.PI)));
                if (outputEquinoctial) {
                    outputStream.format(Locale.US, FIVE_ELEMENTS_FORMAT,
                                        ko.getEquinoctialEy(), // h
                                        ko.getEquinoctialEx(), // k
                                        ko.getHy(),            // p
                                        ko.getHx(),            // q
                                        FastMath.toDegrees(MathUtils.normalizeAngle(ko.getLM(), FastMath.PI)));
                }
            } else if (outputEquinoctial) {
                outputStream.format(Locale.US, SIX_ELEMENTS_FORMAT,
                                    s.getOrbit().getA(),
                                    s.getOrbit().getEquinoctialEy(), // h
                                    s.getOrbit().getEquinoctialEx(), // k
                                    s.getOrbit().getHy(),            // p
                                    s.getOrbit().getHx(),            // q
                                    FastMath.toDegrees(MathUtils.normalizeAngle(s.getOrbit().getLM(), FastMath.PI)));
            }
            if (outputCartesian) {
                final PVCoordinates pv = s.getPVCoordinates();
                outputStream.format(Locale.US, SIX_ELEMENTS_FORMAT,
                                    pv.getPosition().getX() * 0.001,
                                    pv.getPosition().getY() * 0.001,
                                    pv.getPosition().getZ() * 0.001,
                                    pv.getVelocity().getX() * 0.001,
                                    pv.getVelocity().getY() * 0.001,
                                    pv.getVelocity().getZ() * 0.001);
            }
            if (shortPeriodCoefficients != null) {
                for (final String coefficientName : shortPeriodCoefficients) {
                    final double[] coefficient = s.getAdditionalState(coefficientName);
                    outputStream.format(Locale.US, SIX_ELEMENTS_FORMAT,
                                        coefficient[0],
                                        coefficient[2], // beware, it is really 2 (ey/h), not 1 (ex/k)
                                        coefficient[1], // beware, it is really 1 (ex/k), not 2 (ey/h)
                                        coefficient[4], // beware, it is really 4 (hy/p), not 3 (hx/q)
                                        coefficient[3], // beware, it is really 3 (hx/q), not 4 (hy/p)
                                        coefficient[5]);
                }
            }
            outputStream.format(Locale.US, "%n");
        }

        /** {@inheritDoc} */
        public void finish(final SpacecraftState finalState) {
            outputStream.close();
            outputStream = null;
        }

    }

    /**
     * Input data for the DSST propagation tutorial.
     * <p>
     * Data are read from a YAML file.
     * </p>
     * @author Bryan Cazabonne
     */
    public static class TutorialDSSTPropagation {

        /** Orbit data. */
        private TutorialOrbit orbit;

        /** Central body data. */
        private TutorialBody body;

        /** Orbit propagator data. */
        private TutorialPropagator propagator;

        /** Spacecraft definition. */
        private TutorialSpacecraft spacecraft;

        /** Flag for initial orbit type (mean or osculating). */
        private boolean initialOrbitIsOsculating;

        /** Flag for output orbit type (mean or osculating). */
        private boolean outputOrbitIsOsculating;

        /** Simulation duration (days). */
        private double durationInDays;

        /** Time step between printed elements (s). */
        private double outputStep;

        /** List of short period coefficients to output. */
        private List<String> outputShortPeriodCoefficients;

        /** Interpolation grid specification. */
        private double maxTimeGapBetweenInterpolationPoints;

        /** Interpolation grid specification. */
        private int fixedNumberOfInterpolationPoints;

        /** Flag for comparison with numerical propagator. */
        private boolean numericalComparison;

        /**
         * Get the orbit data.
         * @return the orbit data
         */
        public TutorialOrbit getOrbit() {
            return orbit;
        }

        /**
         * Set the orbit data.
         * @param orbit orbit data
         */
        public void setOrbit(final TutorialOrbit orbit) {
            this.orbit = orbit;
        }

        /**
         * Get the body data.
         * @return the body data
         */
        public TutorialBody getBody() {
            return body;
        }

        /**
         * Set the body data.
         * @param body body data
         */
        public void setBody(final TutorialBody body) {
            this.body = body;
        }

        /**
         * Get the orbit propagator data.
         * @return the orbit propagator data
         */
        public TutorialPropagator getPropagator() {
            return propagator;
        }

        /**
         * Set the orbit propagator data.
         * @param propagator orbit propagator data
         */
        public void setPropagator(final TutorialPropagator propagator) {
            this.propagator = propagator;
        }

        /**
         * Get the spacecraft data.
         * @return the spacecraft data
         */
        public TutorialSpacecraft getSpacecraft() {
            return spacecraft;
        }

        /**
         * Set the spacecraft data.
         * @param spacecraft spacecraft data
         */
        public void setSpacecraft(final TutorialSpacecraft spacecraft) {
            this.spacecraft = spacecraft;
        }

        /**
         * Get the flag for initial orbit type.
         * @return true if initial orbit is defined with osculating elements
         */
        public boolean isInitialOrbitIsOsculating() {
            return initialOrbitIsOsculating;
        }

        /**
         * Set the flag for initial orbit type.
         * @param initialOrbitIsOsculating true if initial orbit is defined with osculating elements
         */
        public void setInitialOrbitIsOsculating(final boolean initialOrbitIsOsculating) {
            this.initialOrbitIsOsculating = initialOrbitIsOsculating;
        }

        /**
         * Get the flag for output orbit type.
         * @return true if output orbit is defined with osculating elements
         */
        public boolean isOutputOrbitIsOsculating() {
            return outputOrbitIsOsculating;
        }

        /**
         * Set the flag for output orbit type.
         * @param outputOrbitIsOsculating true if output orbit is defined with osculating elements
         */
        public void setOutputOrbitIsOsculating(final boolean outputOrbitIsOsculating) {
            this.outputOrbitIsOsculating = outputOrbitIsOsculating;
        }

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
         * Get the time step between printed elements.
         * @return the time step between printed elements (s)
         */
        public double getOutputStep() {
            return outputStep;
        }

        /**
         * Set the time step between printed elements.
         * @param outputStep time step between printed elements (s)
         */
        public void setOutputStep(final double outputStep) {
            this.outputStep = outputStep;
        }

        /**
         * Get the list of short period coefficients to output.
         * @return the list of short period coefficients to output.
         */
        public List<String> getOutputShortPeriodCoefficients() {
            return outputShortPeriodCoefficients;
        }

        /**
         * Set the list of short period coefficients to output.
         * @param outputShortPeriodCoefficients list of short period coefficients to output
         */
        public void setOutputShortPeriodCoefficients(final List<String> outputShortPeriodCoefficients) {
            this.outputShortPeriodCoefficients = outputShortPeriodCoefficients;
        }

        /**
         * Get the interpolation grid specification.
         * @return the interpolation grid specification
         */
        public double getMaxTimeGapBetweenInterpolationPoints() {
            return maxTimeGapBetweenInterpolationPoints;
        }

        /**
         * Set the interpolation grid specification.
         * @param maxTimeGapBetweenInterpolationPoints interpolation grid specification
         */
        public void setMaxTimeGapBetweenInterpolationPoints(final double maxTimeGapBetweenInterpolationPoints) {
            this.maxTimeGapBetweenInterpolationPoints = maxTimeGapBetweenInterpolationPoints;
        }

        /**
         * Get the interpolation grid specification.
         * @return the interpolation grid specification
         */
        public int getFixedNumberOfInterpolationPoints() {
            return fixedNumberOfInterpolationPoints;
        }

        /**
         * Set the interpolation grid specification.
         * @param fixedNumberOfInterpolationPoints interpolation grid specification
         */
        public void setFixedNumberOfInterpolationPoints(final int fixedNumberOfInterpolationPoints) {
            this.fixedNumberOfInterpolationPoints = fixedNumberOfInterpolationPoints;
        }

        /**
         * Get the flag for the comparison with the numerical orbit propagator.
         * @return if true, DSST results are compared with the numerical orbit propagator
         */
        public boolean isNumericalComparison() {
            return numericalComparison;
        }

        /**
         * Set the flag for the comparison with the numerical orbit propagator.
         * @param numericalComparison if true, DSST results are compared with the numerical orbit propagator
         */
        public void setNumericalComparison(final boolean numericalComparison) {
            this.numericalComparison = numericalComparison;
        }

    }

}
