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
package org.orekit.tutorials.data;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.orekit.bodies.CelestialBodies;
import org.orekit.data.DataContext;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.forces.gravity.potential.GravityFields;
import org.orekit.frames.Frame;
import org.orekit.frames.Frames;
import org.orekit.frames.Transform;
import org.orekit.models.earth.GeoMagneticFields;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.DateComponents;
import org.orekit.time.OffsetModel;
import org.orekit.time.TimeComponents;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScales;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

/** Orekit tutorial for data contexts.
 * @author Luc Maisonobe
 */
public class Context implements DataContext {

    /** Program entry point.
     * @param args program arguments (unused here)
     */
    public static void main(final String[] args) {
        try {

            // configure the reference context
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
            DataContext.
                getDefault().
                getDataProvidersManager().
                addProvider(new DirectoryCrawler(orekitData));

            // create the local data context implemented by this class
            final DataContext context = new Context();

            // a few test days
            final List<DateComponents> days = Arrays.asList(new DateComponents(2003, 11, 23),
                                                            new DateComponents(2010,  7, 11),
                                                            new DateComponents(2017,  8, 21));

            // compare date conversions for the two contexts
            final TimeScale contextUTC   = context.getTimeScales().getUTC();
            final TimeScale referenceUTC = DataContext.getDefault().getTimeScales().getUTC();
            System.out.format(Locale.US, "time scales differences between made up data context and default data context%n");
            for (DateComponents day : days) {
                final AbsoluteDate contextDate   = new AbsoluteDate(day, TimeComponents.H00, contextUTC);
                final AbsoluteDate referenceDate = new AbsoluteDate(day, TimeComponents.H00, referenceUTC);
                System.out.format(Locale.US, "UTC offsets on %s: %8.5f%n",
                                  day, contextDate.durationFrom(referenceDate));
            }

            // compare frames conversions for the two contexts
            final Frame contextITRF   = context.getFrames().getITRF(IERSConventions.IERS_2010, false);
            final Frame referenceITRF = DataContext.getDefault().getFrames().getITRF(IERSConventions.IERS_2010, false);
            System.out.format(Locale.US, "%nframes differences between made up data context and default data context%n");
            final AbsoluteDate t0 = new AbsoluteDate(days.get(1), contextUTC);
            for (double dt = 0; dt < Constants.JULIAN_DAY; dt += 3600) {
                final AbsoluteDate date      = t0.shiftedBy(dt);
                final Transform    transform = contextITRF.getTransformTo(referenceITRF, date);
                System.out.format(Locale.US, "frames offsets on %s: %6.3f m%n",
                                  date, transform.getRotation().getAngle() * Constants.WGS84_EARTH_EQUATORIAL_RADIUS);
            }

        } catch (OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    /** {@inheritDoc} */
    @Override
    public TimeScales getTimeScales() {
        // set up only the offsets for years 2009 to 2017 and zero EOP
        return TimeScales.of(Arrays.asList(new OffsetModel(new DateComponents(2009, 1, 1), 34),
                                           new OffsetModel(new DateComponents(2012, 1, 1), 35),
                                           new OffsetModel(new DateComponents(2015, 1, 1), 36)),
            (convention, timescale) -> Collections.emptyList());
    }

    /** {@inheritDoc} */
    @Override
    public Frames getFrames() {
        return Frames.of(getTimeScales(), getCelestialBodies());
    }

    /** {@inheritDoc} */
    @Override
    public CelestialBodies getCelestialBodies() {
        // just us the lazy loaded bodies
        return DataContext.getDefault().getCelestialBodies();
    }

    /** {@inheritDoc} */
    @Override
    public GravityFields getGravityFields() {
        // just us the lazy loaded gravity fields
        return DataContext.getDefault().getGravityFields();
    }

    /** {@inheritDoc} */
    @Override
    public GeoMagneticFields getGeoMagneticFields() {
        // just us the lazy loaded geomagnetic fields
        return DataContext.getDefault().getGeoMagneticFields();
    }

}
