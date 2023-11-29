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
package org.orekit.tutorials.time;

import java.io.File;
import java.util.Locale;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;

/** Orekit tutorial for dates support.
 * <p>This tutorial shows basic date usage.</p>
 * @author Luc Maisonobe
 */
public class Time1 {

    /** Private constructor for utility class. */
    private Time1() {
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

            // get the UTC and TAI time scales
            final TimeScale utc = TimeScalesFactory.getUTC();
            final TimeScale tai = TimeScalesFactory.getTAI();

            // create a start date from its calendar components in UTC time scale
            final AbsoluteDate start = new AbsoluteDate(2005, 12, 31, 23, 59, 50, utc);

            // create an end date 20 seconds after the start date
            final double duration = 20.0;
            final AbsoluteDate end = start.shiftedBy(duration);

            // output header line
            System.out.println("        UTC date                  TAI date");

            // loop from start to end using a one minute step
            // (a leap second was introduced this day, so the display should show
            //  the rare case of an UTC minute with more than 60 seconds)
            final double step = 0.5;
            for (AbsoluteDate date = start; date.compareTo(end) < 0; date = date.shiftedBy(step)) {
                System.out.println(date.toString(utc) + "   " + date.toString(tai));
            }

        } catch (OrekitException oe) {
            System.err.println(oe.getLocalizedMessage());
        }
    }
}
