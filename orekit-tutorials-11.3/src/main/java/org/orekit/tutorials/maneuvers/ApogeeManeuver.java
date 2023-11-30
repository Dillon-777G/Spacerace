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
package org.orekit.tutorials.maneuvers;

import java.io.File;
import java.util.Locale;

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.attitudes.InertialProvider;
import org.orekit.attitudes.LofOffset;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.maneuvers.Maneuver;
import org.orekit.forces.maneuvers.propulsion.BasicConstantThrustPropulsionModel;
import org.orekit.forces.maneuvers.propulsion.PropulsionModel;
import org.orekit.forces.maneuvers.trigger.DateBasedManeuverTriggers;
import org.orekit.forces.maneuvers.trigger.ManeuverTriggers;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

/**
 * Orekit tutorial for an apogee maneuver.
 *
 * @author Luc Maisonobe
 */
public class ApogeeManeuver {

    /** Private constructor for utility class. */
    private ApogeeManeuver() {
        // empty
    }

    /**
     * Program entry point.
     * @param args program arguments (unused here)
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

            // set up initial GTO orbit
            final Frame eme2000 = FramesFactory.getEME2000();
            final AbsoluteDate date = new AbsoluteDate(new DateComponents(2004, 01, 01),
                                                       new TimeComponents(23, 30, 00.000),
                                                       TimeScalesFactory.getUTC());
            final Orbit orbit =
                            new KeplerianOrbit(24396159, 0.72831215, FastMath.toRadians(7),
                                               FastMath.toRadians(180), FastMath.toRadians(261),
                                               FastMath.toRadians(0), PositionAngle.TRUE,
                                               eme2000, date,
                                               Constants.EIGEN5C_EARTH_MU);
            final SpacecraftState initialState = new SpacecraftState(orbit, 2500.0);

            // prepare numerical propagator
            final OrbitType orbitType = OrbitType.EQUINOCTIAL;
            final double[][] tol = NumericalPropagator.tolerances(1.0, orbit, orbitType);
            final AdaptiveStepsizeIntegrator integrator =
                            new DormandPrince853Integrator(0.001, 1000, tol[0], tol[1]);
            integrator.setInitialStepSize(60);
            final NumericalPropagator propagator = new NumericalPropagator(integrator);
            propagator.setOrbitType(orbitType);
            propagator.setInitialState(initialState);
            propagator.setAttitudeProvider(new LofOffset(eme2000, LOFType.VNC));

            // set up an attitude law dedicated to the maneuver
            // where the +X axis (direction of acceleration of the thruster)
            // points towards a specific direction
            final Vector3D direction = new Vector3D(FastMath.toRadians(-7.4978),
                                                    FastMath.toRadians(351));
            final AttitudeProvider attitudeOverride =
                            new InertialProvider(new Rotation(direction, Vector3D.PLUS_I),
                                                 eme2000);

            // maneuver will start at a known date and stop after a known duration
            final AbsoluteDate firingDate = new AbsoluteDate(new DateComponents(2004, 1, 2),
                                                             new TimeComponents(4, 15, 34.080),
                                                             TimeScalesFactory.getUTC());
            final double duration = 3653.99;
            final ManeuverTriggers triggers = new DateBasedManeuverTriggers(firingDate, duration);

            // maneuver has constant thrust
            final double thrust = 420;
            final double isp    = 318;
            final PropulsionModel propulsionModel =
                            new BasicConstantThrustPropulsionModel(thrust, isp,
                                                                   Vector3D.PLUS_I,
                                                                   "apogee-engine");

            // build maneuver and add it to the propagator as a new force model
            propagator.addForceModel(new Maneuver(attitudeOverride, triggers, propulsionModel));

            // progress monitoring
            propagator.getMultiplexer().add(120.0, state ->
                System.out.format(Locale.US, "%s a = %12.3f m, e = %11.9f, m = %8.3f kg%n",
                                  state.getDate(), state.getA(), state.getE(), state.getMass())
            );

            // propagate orbit, including maneuver
            propagator.propagate(firingDate.shiftedBy(-900), firingDate.shiftedBy(duration + 900));

            System.exit(0);

        } catch (OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }

    }

}
