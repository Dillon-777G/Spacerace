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
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

import org.orekit.data.DataContext;
import org.orekit.data.DataFilter;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DataSource;
import org.orekit.data.DirectoryCrawler;
import org.orekit.data.GzipFilter;
import org.orekit.data.UnixCompressFilter;
import org.orekit.gnss.HatanakaCompressFilter;
import org.orekit.gnss.navigation.RinexNavigation;
import org.orekit.gnss.navigation.RinexNavigationParser;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.gnss.GNSSPropagator;
import org.orekit.propagation.analytical.gnss.GNSSPropagatorBuilder;
import org.orekit.propagation.analytical.gnss.data.GPSNavigationMessage;
import org.orekit.time.AbsoluteDate;

/**
 * Orekit tutorial for GNSS orbit propagation based on navigation messages.
 *
 * @author Bryan Cazabonne
 */
public class GNSSPropagation {

    /** Name for position/velocity coordinates. */
    private static final String PV = "PV coordinates at ";

    /** Private constructor for utility class. */
    private GNSSPropagation() {
        // empty
    }

    /**
     * Program entry point.
     * @param args program arguments (unused here)
     */
    public static void main(final String[] args) {

        try {

            // Configure orekit-data
            configureOrekit();

            // Run the program
            new GNSSPropagation().run();

        } catch (IOException | URISyntaxException e) {
            // Nothing to do
        }

    }

    private void run() throws IOException, URISyntaxException {

        // Read navigation file
        final String         name = "GPS_Rinex304.n";
        final RinexNavigation file = readNavigationFile(name);

        // Access the navigation message of GPS 03 satellite
        final String               satId   = "G03";
        final GPSNavigationMessage message = getNavigationMessage(satId, file);

        // Build a GNSS orbit propagator
        final GNSSPropagator propagator = new GNSSPropagatorBuilder(message).build();
        final AbsoluteDate initialEpoch = message.getDate();

        // PV coordinates for navigation message epoch
        final SpacecraftState initialState = propagator.propagate(initialEpoch);
        System.out.println("");
        System.out.println(PV + initialState.getPVCoordinates());

        // Propagate to another epoch
        final AbsoluteDate    finalEpoch = initialEpoch.shiftedBy(86400.0);
        final SpacecraftState state      = propagator.propagate(finalEpoch);
        System.out.println(PV + state.getPVCoordinates());

    }

    /**
     * Read a Rinex navigation file.
     *
     * @param fileName file name
     * @return a parsed Rinex navigation file
     */
    private static RinexNavigation readNavigationFile(final String fileName) throws URISyntaxException, IOException {

        // Input in tutorial resources directory
        final String inputPath = GNSSPropagation.class.getClassLoader().getResource("tutorial-gnss/" + fileName).toURI().getPath();
        final File file = new File(inputPath);

        // Set up filtering for measurements files
        DataSource source = new DataSource(file.getName(), () -> new FileInputStream(new File(file.getParentFile(), file.getName())));
        for (final DataFilter filter : Arrays.asList(new GzipFilter(),
                                                     new UnixCompressFilter(),
                                                     new HatanakaCompressFilter())) {
            source = filter.filter(source);
        }

        // Read the Rinex navigation file
        System.out.println("Read navigation file " + fileName + "\n");
        return new RinexNavigationParser().parse(source);

    }

    /**
     * Get the navigation message for the given satellite ID.
     * @param satId satellite ID
     * @param file navigation file
     * @return the corresponding navigation message
     */
    private static GPSNavigationMessage getNavigationMessage(final String satId,
                                                             final RinexNavigation file) {

        // Access the navigation message
        final GPSNavigationMessage message = file.getGPSNavigationMessages(satId).get(0);

        // Print few data
        System.out.println("Navigation data for satellite " + satId + " :");
        System.out.println("===========================================\n");
        System.out.println("Epoch (UTC):                               " + message.getDate());
        System.out.println("Semi-Major Axis (m):                       " + message.getSma());
        System.out.println("Eccentricity:                              " + message.getE());
        System.out.println("Inclination Angle at Reference Time (rad): " + message.getI0());
        System.out.println("Rate of Inclination Angle (rad/s):         " + message.getIDot());
        System.out.println("Longitude of Ascending Node (rad):         " + message.getOmega0());
        System.out.println("Rate of Right Ascension (rad/s):           " + message.getOmegaDot());
        System.out.println("Argument of Perigee (rad):                 " + message.getPa());
        System.out.println("Mean Anomaly (rad):                        " + message.getM0());
        System.out.println("Af0 (s):                                   " + message.getAf0());
        System.out.println("Af1 (s/s):                                 " + message.getAf1());
        System.out.println("Af2 (s/s²):                                " + message.getAf2());

        // Return
        return message;

    }

    /**
     * Configure orekit-data.
     */
    private static void configureOrekit() {
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
    }

}
