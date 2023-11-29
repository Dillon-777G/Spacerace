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

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.attitudes.LofOffset;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.maneuvers.ImpulseManeuver;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.propagation.events.EnablingPredicate;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.EventEnablingPredicateFilter;
import org.orekit.propagation.events.NodeDetector;
import org.orekit.propagation.events.handlers.StopOnIncreasing;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;

/**
 * Orekit tutorial for simple impulsive maneuver.
 *
 * <p>This tutorial shows a basic usage for performing
 * an impulsive inclination maneuver at node.</p>
 * @author Luc Maisonobe
 */
public class ImpulseAtNode {

    /** Private constructor for utility class. */
    private ImpulseAtNode() {
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

            // set up an initial orbit, with 50 degrees inclination
            final Frame eme2000 = FramesFactory.getEME2000();
            final Orbit initialOrbit =
                            new KeplerianOrbit(8000000.0, 0.01,
                                               FastMath.toRadians(50.0), // ← this is initial inclination
                                               FastMath.toRadians(140.0),
                                               FastMath.toRadians(12.0),
                                               FastMath.toRadians(-60.0), PositionAngle.MEAN,
                                               eme2000,
                                               new AbsoluteDate(new DateComponents(2008, 6, 23),
                                                                new TimeComponents(14, 0, 0),
                                                                TimeScalesFactory.getUTC()),
                                               Constants.EIGEN5C_EARTH_MU);

            // the maneuver will be defined in spacecraft frame
            // we need to ensure the Z axis is aligned with orbital momentum
            // so we select an attitude aligned with LVLH Local Orbital frame
            final AttitudeProvider attitudeProvider = new LofOffset(eme2000, LOFType.LVLH);

            // we want to perform a series of 3 inclination reduction maneuvers,
            // as they modify only inclination, they must occur at node
            // but not all nodes are suitable, we want ascending nodes, with a ΔV along -Z
            // the maneuvers are triggered when Action.STOP events occur (and are filtered out)
            final NodeDetector ascendingNodeStopper =
                            new NodeDetector(FramesFactory.getEME2000()).
                            withMaxCheck(300.0).
                            withThreshold(1.0e-6).
                            withHandler(new StopOnIncreasing<>());

            // we allow only maneuvers on the first 3 orbits
            final AbsoluteDate lastAllowedDate =
                            initialOrbit.getDate().shiftedBy(3 * initialOrbit.getKeplerianPeriod());
            final EnablingPredicate<EventDetector> predicate = (state, detector, g) -> state.getDate().isBefore(lastAllowedDate);
            final EventDetector trigger =
                            new EventEnablingPredicateFilter<>(ascendingNodeStopper, predicate);

            // create the maneuver, using ascending node detector as a trigger
            final ImpulseManeuver<EventDetector> maneuver =
                            new ImpulseManeuver<>(trigger,
                                                  new Vector3D(0.0, 0.0, -122.25), // ← 122.25 m/s along -Z
                                                  350.0);

            // wrap-up everything in a propagator
            // note that ImpulseManeuver is a event detector, not a force model!
            // this allows it to be used for all propagators, including analytical ones
            // like the Keplerian propagator used here
            final KeplerianPropagator propagator = new KeplerianPropagator(initialOrbit, attitudeProvider);
            propagator.addEventDetector(maneuver);

            // progress monitoring: we should see inclination remain constant as we
            // cross descending nodes (i.e. switch from Northern to Southern
            // hemisphere), and change as we cross the first three ascending nodes
            propagator.getMultiplexer().add(900.0, state -> {
                final Vector3D pos = state.getPVCoordinates(eme2000).getPosition();
                System.out.format(Locale.US, "%s %s hemisphere inclination = %5.3f%n",
                                  state.getDate(),
                                  pos.getZ() > 0 ? "Northern" : "Southern",
                                  FastMath.toDegrees(state.getOrbit().getI()));
            });

            // run simulation
            propagator.propagate(initialOrbit.getDate().shiftedBy(5 * initialOrbit.getKeplerianPeriod()));

            System.exit(0);

        } catch (OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }

    }

}
