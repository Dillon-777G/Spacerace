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
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.hipparchus.Field;
import org.hipparchus.analysis.differentiation.DSFactory;
import org.hipparchus.analysis.differentiation.DerivativeStructure;
import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeFieldIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853FieldIntegrator;
import org.hipparchus.random.GaussianRandomGenerator;
import org.hipparchus.random.RandomGenerator;
import org.hipparchus.random.UncorrelatedRandomVectorGenerator;
import org.hipparchus.random.Well19937a;
import org.hipparchus.stat.descriptive.DescriptiveStatistics;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.FieldKeplerianOrbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.FieldSpacecraftState;
import org.orekit.propagation.numerical.FieldNumericalPropagator;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeStampedFieldPVCoordinates;

/** Orekit tutorial for field mode propagation.
 * <p>This tutorial shows the interest of the field propagation in particular focusing
 *  on the utilisation of the DerivativeStructure in Orekit.<p>
 * @author Andrea Antolino
 */

public class FieldPropagation {

    /** Private constructor for utility class. */
    private FieldPropagation() {
        // empty
    }

    /** Program entry point.
     * @param args program arguments (unused here)
     */
    public static void main(final String[] args) {
        try {
            // the goal of this example is to make a Montecarlo simulation giving an error on the semiaxis,
            // the inclination and the RAAN. The interest of doing it with Orekit based on the
            // DerivativeStructure is that instead of doing a large number of propagation around the initial
            // point we will do a single propagation of the initial state, and thanks to the Taylor expansion
            // we will see the evolution of the std deviation of the position, which is divided in the
            // CrossTrack, the LongTrack and the Radial error.

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

            // output file in user's home directory
            final File errorFile = new File(home, "error.txt");
            System.out.println("Output file is in : " + errorFile.getAbsolutePath());
            final PrintWriter PW = new PrintWriter(errorFile, StandardCharsets.UTF_8.name());

            PW.printf("time \t\tCrossTrackErr \tLongTrackErr  \tRadialErr \tTotalErr%n");

            //setting the parameters of the simulation
            //Order of derivation of the DerivativeStructures
            final int params = 3;
            final int order = 3;
            final DSFactory factory = new DSFactory(params, order);

            //number of samples of the montecarlo simulation
            final int montecarlo_size = 100;

            //nominal values of the Orbital parameters
            final double a_nominal    = 7.278E6;
            final double e_nominal    = 1e-3;
            final double i_nominal    = FastMath.toRadians(98.3);
            final double pa_nominal   = FastMath.PI / 2;
            final double raan_nominal = 0.0;
            final double ni_nominal   = 0.0;

            //mean of the gaussian curve for each of the errors around the nominal values
            //{a, i, RAAN}
            final double[] mean = {
                0, 0, 0
            };

            //standard deviation of the gaussian curve for each of the errors around the nominal values
            //{dA, dI, dRaan}
            final double[] dAdIdRaan = {
                5, FastMath.toRadians(1e-3), FastMath.toRadians(1e-3)
            };

            //time of integration
            final double final_Dt = 1 * 60 * 60;
            //number of steps per orbit
            final double num_step_orbit = 10;


            final DerivativeStructure a_0    = factory.variable(0, a_nominal);
            final DerivativeStructure e_0    = factory.constant(e_nominal);
            final DerivativeStructure i_0    = factory.variable(1, i_nominal);
            final DerivativeStructure pa_0   = factory.constant(pa_nominal);
            final DerivativeStructure raan_0 = factory.variable(2, raan_nominal);
            final DerivativeStructure ni_0   = factory.constant(ni_nominal);

            //sometimes we will need the field of the DerivativeStructure to build new instances
            final Field<DerivativeStructure> field = a_0.getField();
            //sometimes we will need the zero of the DerivativeStructure to build new instances
            final DerivativeStructure zero = field.getZero();

            //initializing the FieldAbsoluteDate with only the field it will generate the day J2000
            final FieldAbsoluteDate<DerivativeStructure> date_0 = new FieldAbsoluteDate<>(field);

            //initialize a basic frame
            final Frame frame = FramesFactory.getEME2000();

            //initialize the orbit
            final DerivativeStructure mu = factory.constant(3.9860047e14);

            final FieldKeplerianOrbit<DerivativeStructure> KO = new FieldKeplerianOrbit<>(a_0, e_0, i_0, pa_0, raan_0, ni_0, PositionAngle.ECCENTRIC, frame, date_0, mu);

            //step of integration (how many times per orbit we take the mesures)
            final double int_step = KO.getKeplerianPeriod().getReal() / num_step_orbit;


            //random generator to conduct an
            final long number = 23091991;
            final RandomGenerator RG = new Well19937a(number);
            final GaussianRandomGenerator NGG = new GaussianRandomGenerator(RG);
            final UncorrelatedRandomVectorGenerator URVG = new UncorrelatedRandomVectorGenerator(mean, dAdIdRaan, NGG);
            final double[][] rand_gen = new double[montecarlo_size][3];
            for (int jj = 0; jj < montecarlo_size; jj++) {
                rand_gen[jj] = URVG.nextVector();
            }
            //
            final FieldSpacecraftState<DerivativeStructure> SS_0 = new FieldSpacecraftState<>(KO);
            //adding force models
            final ForceModel fModel_Sun  = new ThirdBodyAttraction(CelestialBodyFactory.getSun());
            final ForceModel fModel_Moon = new ThirdBodyAttraction(CelestialBodyFactory.getMoon());
            final ForceModel fModel_HFAM =
                            new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(IERSConventions.IERS_2010, true),
                                                                  GravityFieldFactory.getNormalizedProvider(18, 18));

            //setting an hipparchus field integrator
            final OrbitType type = OrbitType.CARTESIAN;
            final double[][] tolerance = NumericalPropagator.tolerances(0.001, KO.toOrbit(), type);
            final AdaptiveStepsizeFieldIntegrator<DerivativeStructure> integrator =
                            new DormandPrince853FieldIntegrator<>(field, 0.001, 200, tolerance[0], tolerance[1]);

            integrator.setInitialStepSize(60);

            //setting of the field propagator, we used the numerical one in order to add the third body attraction
            //and the holmes featherstone force models
            final FieldNumericalPropagator<DerivativeStructure> numProp = new FieldNumericalPropagator<>(field, integrator);

            numProp.setOrbitType(type);
            numProp.setInitialState(SS_0);
            numProp.addForceModel(fModel_Sun);
            numProp.addForceModel(fModel_Moon);
            numProp.addForceModel(fModel_HFAM);
            //calculate and print the error on every fixed step on the file error.txt
            //we defined the StepHandler to do that giving him the random number generator,
            //the size of the montecarlo simulation and the initial date
            numProp.getMultiplexer().add(zero.add(int_step),
                                         currentState -> {
                    final TimeStampedFieldPVCoordinates<DerivativeStructure> PV_t =
                                    (TimeStampedFieldPVCoordinates<DerivativeStructure>) currentState.getPVCoordinates();

                    //getting the propagated position and velocity(to find the cross track and long track error)
                    final FieldVector3D<DerivativeStructure> P_t = PV_t.getPosition();
                    final FieldVector3D<DerivativeStructure> V_t = PV_t.getVelocity().normalize();
                    final FieldVector3D<DerivativeStructure> M_t = PV_t.getMomentum().normalize();
                    final FieldVector3D<DerivativeStructure> N_t = FieldVector3D.crossProduct(V_t, M_t);

                    final DerivativeStructure x_t = P_t.getX();
                    final DerivativeStructure y_t = P_t.getY();
                    final DerivativeStructure z_t = P_t.getZ();
                    final DescriptiveStatistics stat_CT = new DescriptiveStatistics();
                    final DescriptiveStatistics stat_LT = new DescriptiveStatistics();
                    final DescriptiveStatistics stat_R = new DescriptiveStatistics();
                    final DescriptiveStatistics stat_dist = new DescriptiveStatistics();

                    for (int jj = 0; jj < montecarlo_size; jj++) {
                        //Generation of the random error around the nominal values
                        final double da = rand_gen[jj][0];
                        final double di = rand_gen[jj][1];
                        final double dRAAN = rand_gen[jj][2];
                        //evaluating thanks to taylor the propagation of the nominal values with error
                        // x_e = f(a-n, i-n, raan-n) + df/da(a-n, i-n, raan-n) * da + df/di(a-n, i-n, raan-n) * di + ... etc.
                        //TAYLOR'S EXPANSION
                        final double x_e = x_t.taylor(da, di, dRAAN);
                        final double y_e = y_t.taylor(da, di, dRAAN);
                        final double z_e = z_t.taylor(da, di, dRAAN);

                        final Vector3D P_e = new Vector3D(x_e, y_e, z_e);

                        stat_CT.addValue(Vector3D.dotProduct(P_e.subtract(P_t.toVector3D()), M_t.toVector3D()));
                        stat_LT.addValue(Vector3D.dotProduct(P_e.subtract(P_t.toVector3D()), V_t.toVector3D()));
                        stat_R.addValue(Vector3D.dotProduct(P_e.subtract(P_t.toVector3D()), N_t.toVector3D()));
                        stat_dist.addValue(P_e.subtract(P_t.toVector3D()).getNorm());
                    }

                    //printing all the standard deviations on the file error.txt
                    PW.format(Locale.US, "%f\t%f\t%f\t%f\t%f%n",
                              currentState.getDate().durationFrom(date_0).getReal() / 3600,
                              stat_CT.getStandardDeviation(),
                              stat_LT.getStandardDeviation(),
                              stat_R.getStandardDeviation(),
                              stat_dist.getStandardDeviation());

                });
            //
            final long start = System.nanoTime();

            final FieldSpacecraftState<DerivativeStructure> finalState = numProp.propagate(date_0.shiftedBy(final_Dt));

            final long stop = System.nanoTime();

            System.out.println((stop - start) / 1e6 + " ms");
            System.out.println(finalState.getDate());

            PW.close();
        } catch (IOException | OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

}
