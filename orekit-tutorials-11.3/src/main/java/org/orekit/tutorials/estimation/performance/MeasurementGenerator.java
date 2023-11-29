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
package org.orekit.tutorials.estimation.performance;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.SortedSet;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.random.CorrelatedRandomVectorGenerator;
import org.hipparchus.random.GaussianRandomGenerator;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.Well19937a;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.NadirPointing;
import org.orekit.attitudes.YawCompensation;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.errors.OrekitException;
import org.orekit.errors.OrekitMessages;
import org.orekit.estimation.measurements.GroundStation;
import org.orekit.estimation.measurements.ObservableSatellite;
import org.orekit.estimation.measurements.ObservedMeasurement;
import org.orekit.estimation.measurements.PV;
import org.orekit.estimation.measurements.Range;
import org.orekit.estimation.measurements.generation.ContinuousScheduler;
import org.orekit.estimation.measurements.generation.EventBasedScheduler;
import org.orekit.estimation.measurements.generation.Generator;
import org.orekit.estimation.measurements.generation.MeasurementBuilder;
import org.orekit.estimation.measurements.generation.PVBuilder;
import org.orekit.estimation.measurements.generation.RangeBuilder;
import org.orekit.estimation.measurements.generation.SignSemantic;
import org.orekit.estimation.measurements.modifiers.OnBoardAntennaRangeModifier;
import org.orekit.estimation.measurements.modifiers.RangeIonosphericDelayModifier;
import org.orekit.estimation.measurements.modifiers.RangeTroposphericDelayModifier;
import org.orekit.estimation.measurements.modifiers.ShapiroRangeModifier;
import org.orekit.forces.ForceModel;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.drag.IsotropicDrag;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.OceanTides;
import org.orekit.forces.gravity.Relativity;
import org.orekit.forces.gravity.SolidTides;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.maneuvers.ConstantThrustManeuver;
import org.orekit.forces.maneuvers.propulsion.BasicConstantThrustPropulsionModel;
import org.orekit.forces.maneuvers.trigger.DateBasedManeuverTriggers;
import org.orekit.forces.radiation.IsotropicRadiationSingleCoefficient;
import org.orekit.forces.radiation.RadiationSensitive;
import org.orekit.forces.radiation.SolarRadiationPressure;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.gnss.Frequency;
import org.orekit.models.earth.atmosphere.Atmosphere;
import org.orekit.models.earth.atmosphere.DTM2000;
import org.orekit.models.earth.atmosphere.data.MarshallSolarActivityFutureEstimation;
import org.orekit.models.earth.displacement.StationDisplacement;
import org.orekit.models.earth.ionosphere.EstimatedIonosphericModel;
import org.orekit.models.earth.ionosphere.IonosphericMappingFunction;
import org.orekit.models.earth.ionosphere.IonosphericModel;
import org.orekit.models.earth.ionosphere.SingleLayerModelMappingFunction;
import org.orekit.models.earth.troposphere.EstimatedTroposphericModel;
import org.orekit.models.earth.troposphere.GlobalMappingFunctionModel;
import org.orekit.models.earth.troposphere.MappingFunction;
import org.orekit.models.earth.troposphere.NiellMappingFunctionModel;
import org.orekit.models.earth.troposphere.SaastamoinenModel;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.conversion.DormandPrince853IntegratorBuilder;
import org.orekit.propagation.conversion.NumericalPropagatorBuilder;
import org.orekit.propagation.events.ElevationDetector;
import org.orekit.propagation.events.handlers.ContinueOnEvent;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FixedStepSelector;
import org.orekit.time.TimeScalesFactory;
import org.orekit.tutorials.estimation.common.AttitudeMode;
import org.orekit.tutorials.yaml.TutorialBody;
import org.orekit.tutorials.yaml.TutorialEstimatedParameter;
import org.orekit.tutorials.yaml.TutorialForceModel;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialDrag;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialGravity;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialManeuver;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialOceanTides;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialSolarRadiationPressure;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialThirdBody;
import org.orekit.tutorials.yaml.TutorialIntegrator;
import org.orekit.tutorials.yaml.TutorialIonosphere;
import org.orekit.tutorials.yaml.TutorialMeasurements;
import org.orekit.tutorials.yaml.TutorialMeasurements.TutorialPV;
import org.orekit.tutorials.yaml.TutorialMeasurements.TutorialRange;
import org.orekit.tutorials.yaml.TutorialOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialCartesianOrbit;
import org.orekit.tutorials.yaml.TutorialPropagator;
import org.orekit.tutorials.yaml.TutorialSpacecraft;
import org.orekit.tutorials.yaml.TutorialStation;
import org.orekit.tutorials.yaml.TutorialTroposphere;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.ParameterDriver;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Class for measurement generation.
 * <p>
 * In case a user does not have measurements to perform the performance
 * testing, he can use this class to generate measurements. Measurement
 * generation configuration is done by <i>measurement-generation.yaml</i>
 * file. Please don't forget to set <code>generateMeasurements</code> key
 * in <i>performance-testing.yaml</i> file to <code>true</code> if you
 * want to use generated measurements for performance analysis.
 * </p><p>
 * Current version considers only generation for pseudorange and PV measurements.
 * </p>
 * @author Bryan Cazabonne
 */
public class MeasurementGenerator {

    /** Null value for double parameter. */
    private static final double NULL_DOUBLE = 0.0;

    /** Central attraction coefficient. */
    private double mu;

    /** Flag for end of measurement generation. */
    private boolean finished;

    /**
     * Constructor.
     */
    public MeasurementGenerator() {
        this.finished = false;
    }

    /**
     * Run the program.
     * @param input input file
     * @throws IOException if input file cannot be read
     */
    public void run(final File input) throws IOException {

        // read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialMeasurementGeneration inputData = mapper.readValue(input, TutorialMeasurementGeneration.class);

        // gravity field
        final TutorialGravity gravity = inputData.getPropagator().getForceModels().getGravity();
        final int degree = gravity.getDegree();
        final int order  = FastMath.min(degree, gravity.getOrder());
        final NormalizedSphericalHarmonicsProvider gravityField = GravityFieldFactory.getNormalizedProvider(degree, order);
        mu = gravityField.getMu();

        // IERS conventions
        final IERSConventions conventions = IERSConventions.valueOf("IERS_" + inputData.getBody().getIersConventionYear());

        // earth
        final OneAxisEllipsoid earth = buildBody(inputData);
        // orbit
        final Orbit orbit = buildOrbit(inputData);

        // spacecraft data
        final TutorialSpacecraft spacecraft = inputData.getSpacecraft();

        // measurement generator
        final Generator generator = new Generator();
        final ObservableSatellite satellite = generator.addPropagator(buildPropagator(orbit, earth, conventions,
                                                                                      gravityField, inputData.getPropagator(),
                                                                                      spacecraft));
        final ParameterDriver clockOffsetDriver = satellite.getClockOffsetDriver();
        if (spacecraft.getClockOffset() != null) {
            // on-board clock offset
            final TutorialEstimatedParameter clockOffset = spacecraft.getClockOffset();
            clockOffsetDriver.setReferenceValue(clockOffset.getInitialValue());
            clockOffsetDriver.setValue(clockOffset.getInitialValue());
            clockOffsetDriver.setMinValue(clockOffset.getMinValue());
            clockOffsetDriver.setMaxValue(clockOffset.getMaxValue());
            clockOffsetDriver.setSelected(clockOffset.isEstimated());
        }

        final TutorialMeasurements measurementsData = inputData.getMeasurements();
        if (measurementsData.getRange() != null) {
            for (TutorialStation stationData : measurementsData.getStations()) {
                final GroundStation station = buildStation(stationData, earth, conventions, new StationDisplacement[0]);
                generator.addScheduler(new EventBasedScheduler<>(getRangeBuilder(new Well19937a(0x01e226dd859c2c9dl), measurementsData, stationData, spacecraft, station, satellite),
                                new FixedStepSelector(inputData.getStep(), TimeScalesFactory.getUTC()),
                                generator.getPropagator(satellite),
                                new ElevationDetector(station.getBaseFrame()).
                                withConstantElevation(FastMath.toRadians(5.0)).
                                withHandler(new ContinueOnEvent<>()),
                                SignSemantic.FEASIBLE_MEASUREMENT_WHEN_POSITIVE));
            }
        } else if (measurementsData.getPv() != null) {
            generator.addScheduler(new ContinuousScheduler<>(getPVBuilder(new Well19937a(0x01e226dd859c2c9dl), measurementsData, satellite),
                            new FixedStepSelector(inputData.getStep(), TimeScalesFactory.getUTC())));

        } else {
            System.out.println("ERROR");
            return;
        }

        // Start and end dates
        final AbsoluteDate t0 = new AbsoluteDate(inputData.getStartDate(), TimeScalesFactory.getUTC());
        final AbsoluteDate t1 = new AbsoluteDate(inputData.getEndDate(), TimeScalesFactory.getUTC());

        // Generate measurements
        final long start = System.currentTimeMillis();
        System.out.println("start measurement generation");
        System.out.println("generate measurements ...");
        final SortedSet<ObservedMeasurement<?>> measurements = generator.generate(t0, t1);
        System.out.println("measurement generation finished!");

        final long end = System.currentTimeMillis();
        System.out.println("measurement generation execution time (s): " + (0.001 * (end - start)));
        System.out.println("total measurement number: " + measurements.size());

        // Save measurement into an output file
        final File output = new File(input.getParentFile(), inputData.getOutputBaseName());
        final PrintStream outputStream = new PrintStream(output, StandardCharsets.UTF_8.name());
        String lineFormat = "";
        System.out.println("writing measurement file...");
        for (ObservedMeasurement<?> measurement : measurements) {

            if (measurementsData.getRange() != null) {
                lineFormat = "%-24s\t%-5s\t%-10s\t%20.9f%n";
                final Range range = (Range) measurement;
                outputStream.format(Locale.US, lineFormat,
                                    range.getDate(), "RANGE",
                                    range.getStation().getBaseFrame().getName(),
                                    range.getObservedValue()[0]);
            } else {
                lineFormat = "%-24s\t%-5s\t%-10s\t%15.9f\t%15.9f\t%15.9f\t%12.9f\t%12.9f\t%12.9f%n";
                final PV pv = (PV) measurement;
                outputStream.format(Locale.US, lineFormat,
                                    pv.getDate().toStringWithoutUtcOffset(TimeScalesFactory.getUTC(), 3), "PV",
                                    "-----",
                                    pv.getObservedValue()[0] * 1.e-3, pv.getObservedValue()[1] * 1.e-3, pv.getObservedValue()[2] * 1.e-3,
                                    pv.getObservedValue()[3] * 1.e-3, pv.getObservedValue()[4] * 1.e-3, pv.getObservedValue()[5] * 1.e-3);
            }
        }
        outputStream.close();
        System.out.println("measurement generation results saved as file " + output);
        finished = true;
    }

    /**
     * Get the flag for measurement generation processing.
     * @return true if measurement generation is finished
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Build an orbit propagator from input data.
     * @param initialOrbit initial orbit
     * @param body central body
     * @param conventions IERS convention
     * @param gravityField gravity field
     * @param propagator propagator data
     * @param spacecraft spacecraft data
     * @return a configured orbit propagator
     */
    private Propagator buildPropagator(final Orbit initialOrbit,
                                       final OneAxisEllipsoid body,
                                       final IERSConventions conventions,
                                       final NormalizedSphericalHarmonicsProvider gravityField,
                                       final TutorialPropagator propagator,
                                       final TutorialSpacecraft spacecraft) {

        // initialize propagator builder
        final TutorialIntegrator integrator  = propagator.getIntegrator();
        final TutorialForceModel forceModels = propagator.getForceModels();

        final NumericalPropagatorBuilder builder = new NumericalPropagatorBuilder(initialOrbit,
                                                                                  new DormandPrince853IntegratorBuilder(integrator.getMinStep(),
                                                                                                                        integrator.getMaxStep(),
                                                                                                                        integrator.getPositionError()),
                                                                                  PositionAngle.MEAN,
                                                                                  0.001);

        // mass
        final double mass;
        if (spacecraft != null && spacecraft.getMass() != NULL_DOUBLE) {
            mass = spacecraft.getMass();
        } else {
            mass = 1000.0;
        }
        builder.setMass(mass);

        // attitude mode
        final AttitudeMode mode;
        if (spacecraft != null && spacecraft.getAttitudeMode() != null) {
            mode = AttitudeMode.valueOf(spacecraft.getAttitudeMode());
        } else {
            mode = AttitudeMode.DEFAULT_LAW;
        }
        builder.setAttitudeProvider(mode.getProvider(initialOrbit.getFrame(), body));

        // gravity
        builder.addForceModel(new HolmesFeatherstoneAttractionModel(body.getBodyFrame(), gravityField));

        // third body attraction with solid tides force model
        final List<CelestialBody> solidTidesBodies = new ArrayList<>();
        for (TutorialThirdBody thirdBody : forceModels.getThirdBody()) {
            builder.addForceModel(new ThirdBodyAttraction(CelestialBodyFactory.getBody(thirdBody.getName())));
            if (thirdBody.isWithSolidTides()) {
                solidTidesBodies.add(CelestialBodyFactory.getBody(thirdBody.getName()));
            }
        }
        if (!solidTidesBodies.isEmpty()) {
            builder.addForceModel(new SolidTides(body.getBodyFrame(),
                                                 gravityField.getAe(), mu,
                                                 gravityField.getTideSystem(), conventions,
                                                 TimeScalesFactory.getUT1(conventions, true),
                                                 solidTidesBodies.toArray(new CelestialBody[solidTidesBodies.size()])));
        }

        // ocean tides force model
        if (forceModels.getOceanTides() != null) {
            final TutorialOceanTides oceanTides  = forceModels.getOceanTides();
            final int                oceanDegree = oceanTides.getDegree();
            final int                oceanOrder  = oceanTides.getOrder();
            if (oceanDegree > 0 && oceanOrder > 0) {
                builder.addForceModel(new OceanTides(body.getBodyFrame(),
                                                     gravityField.getAe(), mu,
                                                     oceanDegree, oceanOrder, conventions,
                                                     TimeScalesFactory.getUT1(conventions, true)));
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
            final ForceModel dragModel  = new DragForce(atmosphere, new IsotropicDrag(area, cd));
            builder.addForceModel(dragModel);
            final List<ParameterDriver> drivers = dragModel.getParametersDrivers();
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
            final ForceModel srpModel = new SolarRadiationPressure(CelestialBodyFactory.getSun(),
                                                                   body.getEquatorialRadius(),
                                                                   new IsotropicRadiationSingleCoefficient(area, cr));
            builder.addForceModel(srpModel);
            final List<ParameterDriver> drivers = srpModel.getParametersDrivers();
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
            builder.addForceModel(new Relativity(mu));
        }


        // Maneuvers
        if (forceModels.getManeuvers() != null) {
            for (TutorialManeuver maneuver : forceModels.getManeuvers()) {

                final String name = maneuver.getName();
                final double thrust = maneuver.getThrust().getInitialValue();
                final double isp = maneuver.getIsp();
                final Vector3D direction = maneuver.getDirection();

                // Compute values of firingDate and duration from parameters to estimate
                AbsoluteDate firingDate = new AbsoluteDate();
                double duration = 0.0;
                if ( maneuver.getStartDate() != null && maneuver.getStopDate() != null) {
                    firingDate = maneuver.getStartDate().getInitialValue();
                    duration = maneuver.getStopDate().getInitialValue().durationFrom(firingDate);
                } else if ( maneuver.getMedianDate() != null && maneuver.getDuration() != null) {
                    duration = maneuver.getDuration().getInitialValue();
                    firingDate = maneuver.getMedianDate().getInitialValue().shiftedBy(-duration / 2);
                } else {
                    throw new OrekitException(OrekitMessages.INCONSISTENT_SELECTION, maneuver.getStartDate(), maneuver.getStopDate(),
                                              maneuver.getMedianDate(), maneuver.getDuration());
                }

                final BasicConstantThrustPropulsionModel constantThrustPropulsionModel = new BasicConstantThrustPropulsionModel(thrust, isp, direction, name);
                final ConstantThrustManeuver constThrustMnv = new ConstantThrustManeuver(builder.getAttitudeProvider(),
                                                                                         new DateBasedManeuverTriggers(name, firingDate, duration),
                                                                                         constantThrustPropulsionModel);
                builder.addForceModel(constThrustMnv);
                final List<ParameterDriver> drivers = constThrustMnv.getParametersDrivers();

                if (maneuver.getStartDate() != null && maneuver.getStartDate().isEstimated()) {
                    for (final ParameterDriver driver : drivers) {
                        if (driver.getName().equals(name + "_START")) {
                            driver.setSelected(true);
                            driver.setValue(maneuver.getStartDate().getInitialValue().durationFrom(builder.getInitialOrbitDate()));
                            driver.setMaxValue(maneuver.getStartDate().getMaxValue().durationFrom(builder.getInitialOrbitDate()));
                            driver.setMinValue(maneuver.getStartDate().getMinValue().durationFrom(builder.getInitialOrbitDate()));
                        }
                    }
                }

                if (maneuver.getStopDate() != null && maneuver.getStopDate().isEstimated()) {
                    for (final ParameterDriver driver : drivers) {
                        if (driver.getName().equals(name + "_STOP")) {
                            driver.setSelected(true);
                            driver.setValue(maneuver.getStopDate().getInitialValue().durationFrom(builder.getInitialOrbitDate()));
                            driver.setMaxValue(maneuver.getStopDate().getMaxValue().durationFrom(builder.getInitialOrbitDate()));
                            driver.setMinValue(maneuver.getStopDate().getMinValue().durationFrom(builder.getInitialOrbitDate()));
                        }
                    }
                }

                if (maneuver.getMedianDate() != null && maneuver.getMedianDate().isEstimated()) {
                    for (final ParameterDriver driver : drivers) {
                        if (driver.getName().equals(name + "_MEDIAN")) {
                            driver.setSelected(true);
                        }
                    }
                }

                if (maneuver.getDuration() != null && maneuver.getDuration().isEstimated()) {
                    for (final ParameterDriver driver : drivers) {
                        if (driver.getName().equals(name + "_DURATION")) {
                            driver.setSelected(true);
                            driver.setMinValue(maneuver.getDuration().getMinValue());
                            driver.setMaxValue(maneuver.getDuration().getMaxValue());
                        }
                    }
                }

                if (maneuver.getThrust() != null && maneuver.getThrust().isEstimated()) {
                    for (final ParameterDriver driver : drivers) {
                        if (driver.getName().equals(name + "thrust")) {
                            driver.setSelected(true);
                            driver.setMinValue(maneuver.getThrust().getMinValue());
                            driver.setMaxValue(maneuver.getThrust().getMaxValue());
                        }
                    }
                }

                if (maneuver.getFlowRate() != null && maneuver.getFlowRate().isEstimated()) {
                    for (final ParameterDriver driver : drivers) {
                        if (driver.getName().equals(name + "flow rate")) {
                            driver.setSelected(true);
                            driver.setMinValue(maneuver.getFlowRate().getMinValue());
                            driver.setMaxValue(maneuver.getFlowRate().getMaxValue());
                        }
                    }
                }
            }
        }

        // attitude mode
        builder.setAttitudeProvider(new YawCompensation(initialOrbit.getFrame(), new NadirPointing(initialOrbit.getFrame(), body)));

        return builder.buildPropagator(builder.getSelectedNormalizedParameters());
    }

    /**
     * Build a new ground station.
     * @param stationData station data
     * @param earth central body
     * @param conventions IERS conventions
     * @param displacements station displacements
     * @return a configured ground station
     */
    private GroundStation buildStation(final TutorialStation stationData,
                                       final OneAxisEllipsoid earth, final IERSConventions conventions,
                                       final StationDisplacement... displacements) {
        // geodetic point
        final GeodeticPoint gp = new GeodeticPoint(FastMath.toRadians(stationData.getLatitude()),
                                                   FastMath.toRadians(stationData.getLongitude()),
                                                   stationData.getAltitude());
        // station
        final GroundStation station = new GroundStation(new TopocentricFrame(earth, gp, stationData.getName()),
                                                        TimeScalesFactory.getUT1(conventions, true).getEOPHistory(),
                                                        displacements);
        // station clock offset
        final TutorialEstimatedParameter clockOffset = stationData.getObservationTypes().get(0).getClockOffset();
        station.getClockOffsetDriver().setReferenceValue(clockOffset.getInitialValue());
        station.getClockOffsetDriver().setValue(clockOffset.getInitialValue());
        station.getClockOffsetDriver().setMinValue(clockOffset.getMinValue());
        station.getClockOffsetDriver().setMaxValue(clockOffset.getMaxValue());
        station.getClockOffsetDriver().setSelected(clockOffset.isEstimated());

        return station;
    }

    /**
     * Create an orbit from input parameters.
     * @param inputData input data
     * @return a configured orbit
     */
    private Orbit buildOrbit(final TutorialMeasurementGeneration inputData) {

        // orbit data
        final TutorialOrbit     orbit = inputData.getOrbit();
        final TutorialOrbitType data  = orbit.getOrbitType();

        // inertial frame
        final Frame frame = orbit.getInertialFrame() == null ? FramesFactory.getEME2000() : orbit.getInertialFrame();

        // Cartesian orbit
        final TutorialCartesianOrbit cartesian = data.getCartesian();
        final double[] pos = {cartesian.getX(), cartesian.getY(), cartesian.getZ()};
        final double[] vel = {cartesian.getVx(), cartesian.getVy(), cartesian.getVz()};

        return new CartesianOrbit(new PVCoordinates(new Vector3D(pos), new Vector3D(vel)),
                                  frame,
                                  new AbsoluteDate(orbit.getDate(),
                                                   TimeScalesFactory.getUTC()),
                                  mu);

    }

    /** Create central body from input parameters.
     * @param inputData input data
     * @return central body
     * @throws NoSuchElementException if input parameters are missing
     */
    private OneAxisEllipsoid buildBody(final TutorialMeasurementGeneration inputData) {

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

    /**
     * Get the range measurement builder.
     * @param random underlying random generator to use
     * @param measurementData input measurement data
     * @param stationData input station data
     * @param spacecraftData spacecraft data
     * @param groundStation current ground station
     * @param satellite observable satellite
     * @return a configured builder for range measurement
     * @throws IOException if tropospheric mapping function is invalid
     */
    private MeasurementBuilder<Range> getRangeBuilder(final RandomGenerator random,
                                                      final TutorialMeasurements measurementData,
                                                      final TutorialStation stationData,
                                                      final TutorialSpacecraft spacecraftData,
                                                      final GroundStation groundStation,
                                                      final ObservableSatellite satellite) throws IOException {
        // range measurement data
        final TutorialRange rangeData = measurementData.getRange();

        // builder
        final RealMatrix covariance = MatrixUtils.createRealDiagonalMatrix(new double[] { rangeData.getSigma() * rangeData.getSigma() });
        final MeasurementBuilder<Range> rb =
                        new RangeBuilder(random == null ? null : new CorrelatedRandomVectorGenerator(covariance,
                                                                                                     1.0e-10,
                                                                                                     new GaussianRandomGenerator(random)),
                                                        groundStation, true, rangeData.getSigma(), 1.0, satellite);

        // troposphere
        if (measurementData.getTroposphere() != null) {

            // troposphere data
            final TutorialTroposphere troposphere = measurementData.getTroposphere();

            if (troposphere.isEstimatedModel()) {

                // mapping function
                final MappingFunction mapping;
                if (troposphere.getMappingFunction().equals("NMF")) {
                    mapping = new NiellMappingFunctionModel();
                } else if (troposphere.getMappingFunction().equals("GMF")) {
                    mapping = new GlobalMappingFunctionModel();
                } else {
                    throw new IOException("Invalid tropospheric mapping function");
                }

                final TutorialEstimatedParameter zenithDelay = troposphere.getZenithDelay();
                final EstimatedTroposphericModel model = new EstimatedTroposphericModel(mapping, zenithDelay.getInitialValue());
                final ParameterDriver driver = model.getParametersDrivers().get(0);
                driver.setSelected(zenithDelay.isEstimated());
                driver.setName(stationData.getName() + " " + EstimatedTroposphericModel.TOTAL_ZENITH_DELAY);

                rb.addModifier(new RangeTroposphericDelayModifier(model));

            } else {
                final SaastamoinenModel model = SaastamoinenModel.getStandardModel();
                rb.addModifier(new RangeTroposphericDelayModifier(model));
            }

        }

        // ionosphere
        if (measurementData.getIonosphere() != null) {
            // ionosphere data
            final TutorialIonosphere ionosphereData = measurementData.getIonosphere();
            if (ionosphereData.isEstimatedModel()) {
                // Estimated ionospheric model
                final IonosphericMappingFunction mapping = new SingleLayerModelMappingFunction(ionosphereData.getIonosphericLayer());
                final IonosphericModel ionosphericModel  = new EstimatedIonosphericModel(mapping, ionosphereData.getVtec().getInitialValue());
                final ParameterDriver  ionosphericDriver = ionosphericModel.getParametersDrivers().get(0);
                ionosphericDriver.setSelected(ionosphereData.getVtec().isEstimated());
                ionosphericDriver.setName(stationData.getName() + " " + EstimatedIonosphericModel.VERTICAL_TOTAL_ELECTRON_CONTENT);
                rb.addModifier(new RangeIonosphericDelayModifier(ionosphericModel, Frequency.G01.getMHzFrequency() * 1.0e6));
            } else {
                // do nothing
            }
        }

        // shapiro
        if (measurementData.isWithShapiro()) {
            rb.addModifier(new ShapiroRangeModifier(mu));
        }

        // on-board antenna offset
        if (spacecraftData.getAntennaOffset() != null) {
            rb.addModifier(new OnBoardAntennaRangeModifier(new Vector3D(spacecraftData.getAntennaOffset())));
        }

        return rb;
    }

    /**
     * Get the PV measurement builder.
     * @param random underlying random generator to use
     * @param measurementData input measurement data
     * @param satellite observable satellite
     * @return a configured builder for PV measurement
     */
    private MeasurementBuilder<PV> getPVBuilder(final RandomGenerator random,
                                                final TutorialMeasurements measurementData,
                                                final ObservableSatellite satellite) throws IOException {
        // range measurement data
        final TutorialPV pvData = measurementData.getPv();

        // builder
        final RealMatrix covariance = MatrixUtils.createRealDiagonalMatrix(new double[] {
            pvData.getSigmaPos() * pvData.getSigmaPos(), pvData.getSigmaPos() * pvData.getSigmaPos(), pvData.getSigmaPos() * pvData.getSigmaPos(),
            pvData.getSigmaVel() * pvData.getSigmaVel(), pvData.getSigmaVel() * pvData.getSigmaVel(), pvData.getSigmaVel() * pvData.getSigmaVel()
        });

        final MeasurementBuilder<PV> pvb =
                        new PVBuilder(random == null ? null : new CorrelatedRandomVectorGenerator(covariance,
                                                                                                  1.0e-10,
                                                                                                  new GaussianRandomGenerator(random)),
                                                     pvData.getSigmaPos(), pvData.getSigmaVel(), 1.0, satellite);

        return pvb;
    }

    /** Tutorial input data. */
    public static class TutorialMeasurementGeneration {

        /** Start date for measurement generation. */
        private String startDate;

        /** End date for measurement generation. */
        private String endDate;

        /** Step for measurement generation. */
        private double step;

        /** Orbit data. */
        private TutorialOrbit orbit;

        /** Spacecraft data. */
        private TutorialSpacecraft spacecraft;

        /** Propagator data. */
        private TutorialPropagator propagator;

        /** Central body data. */
        private TutorialBody body;

        /** Measurement data. */
        private TutorialMeasurements measurements;

        /** Name of the output measurement file. */
        private String outputBaseName;

        /**
         * Get the start date for measurement generation.
         * @return the start date for measurement generation.
         */
        public String getStartDate() {
            return startDate;
        }

        /**
         * Set the start date for measurement generation.
         * @param startDate start date for measurement generation
         */
        public void setStartDate(final String startDate) {
            this.startDate = startDate;
        }

        /**
         * Get the end date for measurement generation.
         * @return the end date for measurement generation
         */
        public String getEndDate() {
            return endDate;
        }

        /**
         * Set the end date for measurement generation.
         * @param endDate end date for measurement generation
         */
        public void setEndDate(final String endDate) {
            this.endDate = endDate;
        }

        /**
         * Get the step for the measurement generation.
         * @return the step for the measurement generation (s)
         */
        public double getStep() {
            return step;
        }

        /**
         * Set the step for the measurement generation.
         * @param step step for the measurement generation (s)
         */
        public void setStep(final double step) {
            this.step = step;
        }

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
         * Get the propagator data.
         * @return the propagator data
         */
        public TutorialPropagator getPropagator() {
            return propagator;
        }

        /**
         * Set the propagator data.
         * @param propagator propagator data
         */
        public void setPropagator(final TutorialPropagator propagator) {
            this.propagator = propagator;
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
         * Get the measurement data.
         * @return the measurement data
         */
        public TutorialMeasurements getMeasurements() {
            return measurements;
        }

        /**
         * Set the measurement data.
         * @param measurements measurement data
         */
        public void setMeasurements(final TutorialMeasurements measurements) {
            this.measurements = measurements;
        }

        /**
         * Get the name of the output measurement file.
         * @return the name of the output measurement file
         */
        public String getOutputBaseName() {
            return outputBaseName;
        }

        /**
         * Set the name of the output measurement file.
         * @param outputBaseName name of the output measurement file
         */
        public void setOutputBaseName(final String outputBaseName) {
            this.outputBaseName = outputBaseName;
        }

    }

}
