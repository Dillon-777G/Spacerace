/* Copyright 2002-2020 CS Systèmes d'Information
 * Licensed to CS Systèmes d'Information (CS) under one or more
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
package org.orekit.tutorials.estimation.performance;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Locale;

import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * Main class for Orbit Determination performance testing.
 * <p>
 * Testing configuration is done by <i>performance-testing.yaml</i>
 * file. In this file, a user can specify:
 * <ul>
 * <li> If input measurements must be generated using {@link MeasurementGenerator} class </li>
 * <li> The Orbit Determination engine (i.e. Kalman Filter or Batch Least Squares) </li>
 * </ul>
 * @author Bryan Cazabonne
 */
public class PerformanceTesting {

    /** Folder name. */
    private static final String FOLDER = "/performance-testing/";

    /**
     * Constructor.
     */
    private PerformanceTesting() {
        // empty constructor
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

            // run the program
            new PerformanceTesting().run(new File(PerformanceTesting.class.getResource(FOLDER + "performance-testing.yaml").toURI()));

        } catch (OrekitException | IOException | URISyntaxException | InterruptedException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }

    }

    /**
     * Run the performance testing.
     * @param input input file
     * @throws IOException if input file cannot be read
     * @throws URISyntaxException if the exception occurs
     * @throws InterruptedException if the exception occurs
     */
    private void run(final File input) throws IOException, URISyntaxException, InterruptedException {

        // read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialPerformanceTesting inputData = mapper.readValue(input, TutorialPerformanceTesting.class);

        // read existing measurement or generate measurements
        if (inputData.generateMeasurements() && inputData.getMeasurementGenerationInputFile() != null) {
            // run the measurement generator
            new MeasurementGenerator().run(new File(PerformanceTesting.class.getResource(FOLDER + inputData.getMeasurementGenerationInputFile()).toURI()));
        }

        // run the orbit determination process
        System.out.println("===== RUN ORBIT DETERMINATION =====");
        if (inputData.isKalman()) {
            // run Kalman Filter
            System.out.println("Run Kalman Filter");
            new OrbitDeterminationEngine(true).runOrbitDetermination(new File(PerformanceTesting.class.getResource(FOLDER + inputData.getOrbitDeterminationInputFile()).toURI()));
        } else {
            // run Batch Least Squares algorithm
            System.out.println("Run a Batch Least Squares algorithm");
            new OrbitDeterminationEngine(false).runOrbitDetermination(new File(PerformanceTesting.class.getResource(FOLDER + inputData.getOrbitDeterminationInputFile()).toURI()));
        }

    }

    /**
     * Input data for orbit determination performance testing.
     * <p>
     * Data are read from a YAML file.
     * </p>
     * @author Bryan Cazabonne
     */
    public static class TutorialPerformanceTesting {

        /** Flag for measurement generation. */
        private boolean generateMeasurements;

        /** Flag for orbit determination engine (i.e. Kalman filer or least squares). */
        private boolean isKalman;

        /** Name of the input file for measurement generation. */
        private String measurementGenerationInputFile;

        /** Name of the input file for orbit determination. */
        private String orbitDeterminationInputFile;

        /**
         * Get the flag for measurement generation.
         * @return true if measurements have to be generated
         */
        public boolean generateMeasurements() {
            return generateMeasurements;
        }

        /**
         * Set the flag for measurements generation.
         * @param generateMeasurements true if measurements have to be generated
         */
        public void setGenerateMeasurements(final boolean generateMeasurements) {
            this.generateMeasurements = generateMeasurements;
        }

        /**
         * Get the flag for orbit determination engine.
         * @return true if Kalman filter is used,
         *         false if a batch least squares algorithm is used
         */
        public boolean isKalman() {
            return isKalman;
        }

        /**
         * Set the flag for orbit determination engine.
         * @param isKalman true if Kalman filter is used,
         *                 false if a batch least squares algorithm is used
         */
        public void setIsKalman(final boolean isKalman) {
            this.isKalman = isKalman;
        }

        /**
         * Get the name of the input YAML file for measurement generation.
         * @return the name of the YAML input file for measurement generation
         */
        public String getMeasurementGenerationInputFile() {
            return measurementGenerationInputFile;
        }

        /**
         * Set the name of the input YAML file for measurement generation.
         * @param measurementGenerationInputFile name of the input YAML file for measurement generation
         */
        public void setMeasurementGenerationInputFile(final String measurementGenerationInputFile) {
            this.measurementGenerationInputFile = measurementGenerationInputFile;
        }

        /**
         * Get the name of the input YAML file for orbit determination.
         * @return the name of the input YAML file for orbit determination
         */
        public String getOrbitDeterminationInputFile() {
            return orbitDeterminationInputFile;
        }

        /**
         * Set the name of the input YAML file for orbit determination.
         * @param orbitDeterminationInputFile name of the input YAML file for orbit determination
         */
        public void setOrbitDeterminationInputFile(final String orbitDeterminationInputFile) {
            this.orbitDeterminationInputFile = orbitDeterminationInputFile;
        }

    }

}
