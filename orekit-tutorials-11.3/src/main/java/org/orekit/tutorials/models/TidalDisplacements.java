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

package org.orekit.tutorials.models;

import java.io.File;
import java.util.Locale;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.BodiesElements;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.data.FundamentalNutationArguments;
import org.orekit.errors.OrekitException;
import org.orekit.frames.EOPHistory;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.models.earth.displacement.TidalDisplacement;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

/** Orekit tutorial for computing stations tidal displacements.
 * @author Luc Maisonobe
 */
public class TidalDisplacements {

    /** Private constructor for utility class.
     */
    private TidalDisplacements() {
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

            final IERSConventions conventions = IERSConventions.IERS_2010;
            final boolean removePermanentDeformation = true;
            final TidalDisplacement tidalDisplacement = new TidalDisplacement(Constants.EIGEN5C_EARTH_EQUATORIAL_RADIUS,
                                                                              Constants.JPL_SSD_SUN_EARTH_PLUS_MOON_MASS_RATIO,
                                                                              Constants.JPL_SSD_EARTH_MOON_MASS_RATIO,
                                                                              CelestialBodyFactory.getSun(),
                                                                              CelestialBodyFactory.getMoon(),
                                                                              conventions,
                                                                              removePermanentDeformation);
            final EOPHistory eop = FramesFactory.getEOPHistory(conventions, false);
            final TimeScale  ut1 = TimeScalesFactory.getUT1(eop);
            final FundamentalNutationArguments arguments = conventions.getNutationArguments(ut1);

            final Frame earthFrame = FramesFactory.getITRF(conventions, false);
            final OneAxisEllipsoid earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                                                Constants.WGS84_EARTH_FLATTENING,
                                                                earthFrame);
            final TopocentricFrame topo = new TopocentricFrame(earth,
                                                               new GeodeticPoint(FastMath.toRadians(43.57732),
                                                                                 FastMath.toRadians(1.49443),
                                                                                 200),
                                                               "bike shed");
            final Vector3D referencePoint = topo.getTransformTo(earthFrame, (AbsoluteDate) null).
                                            transformPosition(Vector3D.ZERO);

            final AbsoluteDate t0 = new AbsoluteDate(2019, 10, 31, TimeScalesFactory.getUTC());
            for (double dt = 0; dt < Constants.JULIAN_DAY; dt += 60.0) {

                // compute displacement for current date
                final AbsoluteDate date = t0.shiftedBy(dt);
                final BodiesElements elements = arguments.evaluateAll(date);
                final Vector3D displacement = tidalDisplacement.displacement(elements, earthFrame, referencePoint);

                // display displacement in East-North-Up frame
                System.out.format(Locale.US, "%s %9.6f %9.6f %9.6f%n",
                                  date,
                                  Vector3D.dotProduct(displacement, topo.getEast()),
                                  Vector3D.dotProduct(displacement, topo.getNorth()),
                                  Vector3D.dotProduct(displacement, topo.getZenith()));

            }

        } catch (OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

}
