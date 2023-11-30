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
package org.orekit.tutorials.estimation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.time.AbsoluteDate;
import org.orekit.tutorials.estimation.performance.MeasurementGenerator;

/** <b>Orekit tutorial for maneuver estimation</b>.
 * <p>
 * This tutorial is divided into two main parts.<br>
 * The first part makes use of the tutorial {@link MeasurementGenerator} to generate PV measurements in the expected format.
 * The generator is configured via the <i>maneuver-estimation-generate-measurements.yaml</i>
 * file which, for the purpose of this tutorial, contains three maneuvers by default.<br>
 * The second part focuses on the maneuver estimation using a Batch Least-Square.
 * The default configuration is provided in the <i>maneuver-estimation.yaml</i> file.<br>
 * The estimated parameters are as follows:
 * <ul>
 * <li>The start/stop dates of the first maneuver,</li>
 * <li>The median date and duration of the second and third maneuvers,</li>
 * <li>The thrust for all maneuvers,
 * <li>The drag and reflectivity coefficients.
 * </ul>
 * The idea here is to generate measurements with "reference" orbit and maneuvers.<br>
 * Then launch the estimation starting from a shifted orbit and shifted maneuvers (in thrust and dates).<br>
 * And finally check that at the end of the OD process, the results converge towards the reference values.
 * <p>
 * Note that <b>only two alternative combinations are allowed for dates estimation</b>:
 * <ol>
 * <li>Start and stop dates,</li>
 * <li><b>OR</b> median date and duration.</li>
 * </ol>
 * When estimating both date parameters (e.g. start and stop dates / median date and duration), their initial guesses should be chosen carefully to be
 * representative of the maneuver (e.g. the overall duration should be consistent / start and stop dates
 * should not be pushed too early or later in time).
 * <p>
 * Also, the estimated dates require an entry for their minimum and maximum value in the
 * yaml configuration file. However these values are ignored by the algorithm which sets them
 * to {@link AbsoluteDate#PAST_INFINITY} and {@link AbsoluteDate#FUTURE_INFINITY}, respectively.
 */
public class ManeuverEstimation extends NumericalOrbitDetermination {

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


            // Read measurements generation settings
            final String measGenInputPath = ManeuverEstimation.class.getClassLoader().
                            getResource("maneuver-estimation/maneuver-estimation-generate-measurements.yaml").toURI().getPath();

            // Run the measurement generator which will generate the measurements.aer file
            new MeasurementGenerator().run(new File(measGenInputPath));


            // input in tutorial resources directory
            final String inputPath = ManeuverEstimation.class.getClassLoader().
                            getResource("maneuver-estimation/maneuver-estimation.yaml").toURI().getPath();

            new ManeuverEstimation().run(new File(inputPath));


        } catch (URISyntaxException | IOException | OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }


}
