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
package org.orekit.tutorials.gnss;

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
import org.orekit.errors.OrekitException;
import org.orekit.gnss.HatanakaCompressFilter;
import org.orekit.gnss.ObservationData;
import org.orekit.gnss.ObservationDataSet;
import org.orekit.gnss.RinexObservationLoader;
import org.orekit.gnss.SatelliteSystem;

/**
 * Orekit tutorial for the reading of Rinex observation file.
 * <p>
 * The purpose of the tutorial is to print the measurements contained
 * in a Rinex file for a given GNSS satellite.
 * </p>
 * @author Bryan Cazabonne
 */
public class RinexObservationFile {

    /**
     * Private constructor for utility class.
     */
    private RinexObservationFile() {
       // empty constructor
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

            // input in tutorial resources directory
            final String inputPath = RinexObservationFile.class.getClassLoader().getResource("gnss-od/badg0440.16o.Z").toURI().getPath();
            final File file = new File(inputPath);

            // set up filtering for measurements files
            DataSource nd = new DataSource(file.getName(), () -> new FileInputStream(new File(file.getParentFile(), file.getName())));
            for (final DataFilter filter : Arrays.asList(new GzipFilter(),
                                                         new UnixCompressFilter(),
                                                         new HatanakaCompressFilter())) {
                nd = filter.filter(nd);
            }

            // read the Rinex file and print the measurements for GPS-01
            final boolean         print  = true;
            final SatelliteSystem system = SatelliteSystem.GPS;
            final int             prn    = 1;
            readRinex(print, nd, system, prn);

        }  catch (OrekitException | URISyntaxException | IOException e) {
            System.err.println(e.getLocalizedMessage());
        }
    }

    /**
     * Read the measurements contained inside a Rinex file.
     * @param print true to print the measurements
     * @param source data source containing measurements
     * @param system satellite system
     * @param prn PRN code of the satellite
     * @throws IOException
     */
    private static void readRinex(final boolean print, final DataSource source,
                                  final SatelliteSystem system, final int prn) throws IOException {

        System.out.println("Measurements for satellite " + system + "-" + prn + " contained in Rinex file " + source.getName());
        System.out.println("\n");
        System.out.println("          Time                    System     PRN     SNR indicator     Measurement Type       Value");

        // initialize the Rinex loader
        final RinexObservationLoader loader = new RinexObservationLoader(source);

        // loop on observation data set
        for (final ObservationDataSet observationDataSet : loader.getObservationDataSets()) {

            // check if the measurements correspond to the wanted satellite
            if (observationDataSet.getSatelliteSystem() == system &&
                            observationDataSet.getPrnNumber() == prn) {
                // loop on observations
                for (final ObservationData od : observationDataSet.getObservationData()) {
                    // check if it exists a measurement
                    if (!Double.isNaN(od.getValue())) {
                        if (print) {
                            System.out.format(Locale.US, " %s            %s       %s           %s             %15s     %9.3f%n",
                                              observationDataSet.getDate(),
                                              system, prn, od.getSignalStrength(),
                                              od.getObservationType().getMeasurementType(), od.getValue());
                        }
                    }
                }
            }

        }

    }

}
