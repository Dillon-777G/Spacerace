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

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.RotationConvention;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.Transform;
import org.orekit.frames.UpdatableFrame;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

/** Orekit tutorial for advanced frames support.
 * <p>This tutorial shows a smart usage of frames and transforms.</p>
 * @author Pascal Parraud
 */
public class Frames2 {

    /** Private constructor for utility class. */
    private Frames2() {
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

            // Considering the following Computing/Measurement date in UTC time scale
            final TimeScale utc = TimeScalesFactory.getUTC();
            final AbsoluteDate date = new AbsoluteDate(2008, 10, 01, 12, 00, 00.000, utc);

            // The Center of Gravity frame has its origin at the satellite center of gravity (CoG)
            // and its axes parallel to EME2000. It is derived from EME2000 frame at any moment
            // by an unknown transform which depends on the current position and the velocity.
            // Let's initialize this transform by the identity transform.
            final UpdatableFrame cogFrame = new UpdatableFrame(FramesFactory.getEME2000(), Transform.IDENTITY, "LOF", false);

            // The satellite frame, with origin also at the CoG, depends on attitude.
            // For the sake of this tutorial, we consider a simple inertial attitude here
            final Transform cogToSat = new Transform(date, new Rotation(0.6, 0.48, 0, 0.64, false));
            final Frame satFrame = new Frame(cogFrame, cogToSat, "sat", false);

            // Finally, the GPS antenna frame can be defined from the satellite frame by 2 transforms:
            // a translation and a rotation
            final Transform translateGPS = new Transform(date, new Vector3D(0, 0, 1));
            final Transform rotateGPS    = new Transform(date, new Rotation(new Vector3D(0, 1, 3),
                                                                            FastMath.toRadians(10),
                                                                            RotationConvention.VECTOR_OPERATOR));
            final Frame gpsFrame         = new Frame(satFrame, new Transform(date, translateGPS, rotateGPS), "GPS", false);


            // Let's get the satellite position and velocity in ITRF as measured by GPS antenna at this moment:
            final Vector3D position = new Vector3D(-6142438.668, 3492467.560, -25767.25680);
            final Vector3D velocity = new Vector3D(505.8479685, 942.7809215, 7435.922231);
            System.out.format(Locale.US, "GPS antenna position in ITRF:    %12.3f %12.3f %12.3f%n",
                              position.getX(), position.getY(), position.getZ());
            System.out.format(Locale.US, "GPS antenna velocity in ITRF:    %12.7f %12.7f %12.7f%n",
                              velocity.getX(), velocity.getY(), velocity.getZ());

            // The transform from GPS frame to ITRF frame at this moment is defined by
            // a translation and a rotation. The translation is directly provided by the
            // GPS measurement above. The rotation is extracted from the existing tree, where
            // we know all rotations are already up to date, even if one translation is still
            // unknown. We combine the extracted rotation and the measured translation by
            // applying the rotation first because the position/velocity vector are given in
            // ITRF frame not in GPS antenna frame:
            final Transform measuredTranslation = new Transform(date, position, velocity);
            final Transform formerTransform     = gpsFrame.getTransformTo(FramesFactory.getITRF(IERSConventions.IERS_2010, true), date);
            final Transform preservedRotation   = new Transform(date,
                                                                formerTransform.getRotation(),
                                                                formerTransform.getRotationRate());
            final Transform gpsToItrf           = new Transform(date, preservedRotation, measuredTranslation);

            // So we can now update the transform from EME2000 to CoG frame
            cogFrame.updateTransform(gpsFrame, FramesFactory.getITRF(IERSConventions.IERS_2010, true), gpsToItrf, date);

            // And we can get the position and velocity of satellite CoG in EME2000 frame
            final PVCoordinates origin  = PVCoordinates.ZERO;
            final Transform cogToItrf   = cogFrame.getTransformTo(FramesFactory.getITRF(IERSConventions.IERS_2010, true), date);
            final PVCoordinates satItrf = cogToItrf.transformPVCoordinates(origin);
            System.out.format(Locale.US, "Satellite   position in ITRF:    %12.3f %12.3f %12.3f%n",
                              satItrf.getPosition().getX(), satItrf.getPosition().getY(), satItrf.getPosition().getZ());
            System.out.format(Locale.US, "Satellite   velocity in ITRF:    %12.7f %12.7f %12.7f%n",
                              satItrf.getVelocity().getX(), satItrf.getVelocity().getY(), satItrf.getVelocity().getZ());

            final Transform cogToEme2000   = cogFrame.getTransformTo(FramesFactory.getEME2000(), date);
            final PVCoordinates satEME2000 = cogToEme2000.transformPVCoordinates(origin);
            System.out.format(Locale.US, "Satellite   position in EME2000: %12.3f %12.3f %12.3f%n",
                              satEME2000.getPosition().getX(), satEME2000.getPosition().getY(), satEME2000.getPosition().getZ());
            System.out.format(Locale.US, "Satellite   velocity in EME2000: %12.7f %12.7f %12.7f%n",
                              satEME2000.getVelocity().getX(), satEME2000.getVelocity().getY(), satEME2000.getVelocity().getZ());

        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }
    }

}
