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
import java.util.Locale;

import org.hipparchus.ode.AbstractIntegrator;
import org.hipparchus.ode.nonstiff.ClassicalRungeKuttaIntegrator;
import org.hipparchus.util.FastMath;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.BoundedPropagator;
import org.orekit.propagation.EphemerisGenerator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

/** Orekit tutorial for ephemeris mode propagation.
 * <p>This tutorial shows a basic usage of the ephemeris mode in conjunction with a numerical propagator.<p>
 * @author Pascal Parraud
 */
public class EphemerisMode {

    /** Private constructor for utility class. */
    private EphemerisMode() {
        // empty
    }

    /** Program entry point.
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


            // Initial orbit parameters
            final double a = 24396159; // semi major axis in meters
            final double e = 0.72831215; // eccentricity
            final double i = FastMath.toRadians(7); // inclination
            final double omega = FastMath.toRadians(180); // perigee argument
            final double raan = FastMath.toRadians(261); // right ascension of ascending node
            final double lM = 0; // mean anomaly

            // Inertial frame
            final Frame inertialFrame = FramesFactory.getEME2000();

            // Initial date in UTC time scale
            final TimeScale utc = TimeScalesFactory.getUTC();
            final AbsoluteDate initialDate = new AbsoluteDate(2004, 01, 01, 23, 30, 00.000, utc);

            // gravitation coefficient
            final double mu =  3.986004415e+14;

            // Orbit construction as Keplerian
            final Orbit initialOrbit = new KeplerianOrbit(a, e, i, omega, raan, lM, PositionAngle.MEAN,
                                                          inertialFrame, initialDate, mu);

            // Initialize state
            final SpacecraftState initialState = new SpacecraftState(initialOrbit);

            // Numerical propagation with no perturbation (only Keplerian movement)
            // Using a very simple integrator with a fixed step: classical Runge-Kutta
            final double stepSize = 10;  // the step is ten seconds
            final AbstractIntegrator integrator = new ClassicalRungeKuttaIntegrator(stepSize);
            final NumericalPropagator propagator = new NumericalPropagator(integrator);

            // Set the propagator to ephemeris mode
            final EphemerisGenerator generator = propagator.getEphemerisGenerator();

            // Initialize propagation
            propagator.setInitialState(initialState);

            // Propagation with storage of the results in an integrated ephemeris
            final SpacecraftState finalState = propagator.propagate(initialDate.shiftedBy(6000));

            System.out.format(Locale.US, "Numerical propagation:%n  Final date: %s%n  %s%n",
                              finalState.getDate(), finalState.getOrbit());

            // Getting the integrated ephemeris
            final BoundedPropagator ephemeris = generator.getGeneratedEphemeris();

            System.out.format(Locale.US, "%nEphemeris defined from %s to %s%n",
                              ephemeris.getMinDate(), ephemeris.getMaxDate());

            System.out.format(Locale.US, "%nEphemeris propagation:%n");
            final String format = "  %s  %s%n";
            AbsoluteDate intermediateDate = initialDate.shiftedBy(3000);
            SpacecraftState intermediateState = ephemeris.propagate(intermediateDate);
            System.out.format(Locale.US, format, intermediateState.getDate(), intermediateState.getOrbit());

            intermediateDate = finalState.getDate();
            intermediateState = ephemeris.propagate(intermediateDate);
            System.out.format(Locale.US, format, intermediateState.getDate(), intermediateState.getOrbit());

            intermediateDate = initialDate.shiftedBy(-1000);
            System.out.format(Locale.US, "%nAttempting to propagate to date %s which is OUT OF RANGE%n",
                              intermediateDate);
            System.out.println("This propagation attempt should fail, " +
                               "so an error message shoud appear below, " +
                               "this is expected and shows that errors are handled correctly");
            intermediateState = ephemeris.propagate(intermediateDate);

            // these two print should never happen as an exception should have been triggered
            System.out.println("  date :  " + intermediateState.getDate());
            System.out.println("  " + intermediateState.getOrbit());

        } catch (OrekitException oe) {
            System.err.println(oe.getMessage(Locale.US));
        }
    }
}
