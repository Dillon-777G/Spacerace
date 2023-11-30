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

package org.orekit.tutorials.frames;

import java.io.File;
import java.util.Locale;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.analytical.KeplerianPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

/** Orekit tutorial for basic frames support.
 * <p>This tutorial shows a simple usage of frames and transforms.</p>
 * @author Pascal Parraud
 */
public class Frames1 {

    /** Private constructor for utility class. */
    private Frames1() {
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

            //  Initial state definition : date, orbit
            final TimeScale utc = TimeScalesFactory.getUTC();
            final AbsoluteDate initialDate = new AbsoluteDate(2008, 10, 01, 0, 0, 00.000, utc);
            final double mu =  3.986004415e+14; // gravitation coefficient
            final Frame inertialFrame = FramesFactory.getEME2000(); // inertial frame for orbit definition
            final Vector3D posisat = new Vector3D(-6142438.668, 3492467.560, -25767.25680);
            final Vector3D velosat = new Vector3D(505.8479685, 942.7809215, 7435.922231);
            final PVCoordinates pvsat = new PVCoordinates(posisat, velosat);
            final Orbit initialOrbit = new CartesianOrbit(pvsat, inertialFrame, initialDate, mu);

            // Propagator : consider a simple Keplerian motion
            final Propagator kepler = new KeplerianPropagator(initialOrbit);

            // Earth and frame
            final Frame earthFrame = FramesFactory.getITRF(IERSConventions.IERS_2010, true);
            final BodyShape earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                                         Constants.WGS84_EARTH_FLATTENING,
                                                         earthFrame);

            // Station
            final double longitude = FastMath.toRadians(45.);
            final double latitude  = FastMath.toRadians(25.);
            final double altitude  = 0.;
            final GeodeticPoint station = new GeodeticPoint(latitude, longitude, altitude);
            final TopocentricFrame staF = new TopocentricFrame(earth, station, "station");

            System.out.println("          time           doppler (m/s)");

            // Stop date
            final AbsoluteDate finalDate = initialDate.shiftedBy(6000);

            // Loop
            AbsoluteDate extrapDate = initialDate;
            while (extrapDate.compareTo(finalDate) <= 0)  {

                // We can simply get the position and velocity of spacecraft in station frame at any time
                final PVCoordinates pvInert   = kepler.propagate(extrapDate).getPVCoordinates();
                final PVCoordinates pvStation = inertialFrame.getTransformTo(staF, extrapDate).transformPVCoordinates(pvInert);

                // And then calculate the Doppler signal
                final double doppler = Vector3D.dotProduct(pvStation.getPosition(), pvStation.getVelocity()) / pvStation.getPosition().getNorm();

                System.out.format(Locale.US, "%s   %9.3f%n", extrapDate, doppler);

                extrapDate = extrapDate.shiftedBy(600);

            }

        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }
    }

}
