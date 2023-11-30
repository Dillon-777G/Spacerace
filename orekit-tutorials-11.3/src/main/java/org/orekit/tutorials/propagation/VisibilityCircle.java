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
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hipparchus.util.FastMath;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.TopocentricFrame;
import org.orekit.tutorials.yaml.TutorialStation;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/** Orekit tutorial for computing visibility circles.
 * @author Luc Maisonobe
 */
public class VisibilityCircle {

    /** Private constructor for utility class. */
    private VisibilityCircle() {
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

            // input/out
            final File input  = new File(VisibilityCircle.class.getResource("/visibility-circle.yaml").toURI().getPath());
            final File output = new File(input.getParentFile(), "visibility-circle.csv");

            new VisibilityCircle().run(input, output, ",");

            System.out.println("visibility circle saved as file " + output);

        } catch (URISyntaxException | IOException | OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    /** Run the program.
     * @param input input file
     * @param output output file
     * @param separator separator for lists
     * @throws IOException if input file cannot be read
     */
    private void run(final File input, final File output, final String separator) throws IOException {

        // read input parameters
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();
        final TutorialVisibilityCircle inputData = mapper.readValue(input, TutorialVisibilityCircle.class);

        final double minElevation = FastMath.toRadians(inputData.getMinElevation());
        final double radius       = Constants.WGS84_EARTH_EQUATORIAL_RADIUS + inputData.getSpacecraftAltitude();
        final int    points       = inputData.getPointsNumber();

        // station properties
        final TutorialStation stationData = inputData.getStation();
        final double latitude  = FastMath.toRadians(stationData.getLatitude());
        final double longitude = FastMath.toRadians(stationData.getLongitude());
        final double altitude  = stationData.getAltitude();
        final String name      = stationData.getName();

        // compute visibility circle
        final List<GeodeticPoint> circle =
                computeCircle(latitude, longitude, altitude, name, minElevation, radius, points);

        // create a 2 columns csv file representing the visibility circle
        // in the user home directory, with latitude in column 1 and longitude in column 2
        final DecimalFormat format = new DecimalFormat("#00.00000", new DecimalFormatSymbols(Locale.US));
        final PrintStream csvFile = new PrintStream(output, StandardCharsets.UTF_8.name());
        for (GeodeticPoint p : circle) {
            csvFile.println(format.format(FastMath.toDegrees(p.getLatitude())) + "," +
                            format.format(FastMath.toDegrees(p.getLongitude())));
        }
        csvFile.close();

    }

    /** Compute visibility circle on ground.
     * @param latitude station latitude
     * @param longitude station longitude
     * @param altitude station altitude
     * @param name station name
     * @param minElevation minimum elevation for defining visibility above ground
     * @param radius assumed satellite distance to Earth center
     * @param points number of points to compute
     * @return visibility circle as a list of ground points
     */
    private static List<GeodeticPoint> computeCircle(final double latitude, final double longitude, final double altitude,
                                                     final String name, final double minElevation,
                                                     final double radius, final int points) {

        // define Earth shape, using WGS84 model
        final BodyShape earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                                     Constants.WGS84_EARTH_FLATTENING,
                                                     FramesFactory.getITRF(IERSConventions.IERS_2010, false));

        // define an array of ground stations
        final TopocentricFrame station =
                new TopocentricFrame(earth, new GeodeticPoint(latitude, longitude, altitude), name);

        // compute the visibility circle
        final List<GeodeticPoint> circle = new ArrayList<GeodeticPoint>();
        for (int i = 0; i < points; ++i) {
            final double azimuth = i * (2.0 * FastMath.PI / points);
            circle.add(station.computeLimitVisibilityPoint(radius, azimuth, minElevation));
        }

        // return the computed points
        return circle;

    }

    /**
     * Input data for the visibility circle tutorial.
     * <p>
     * Data are read from a YAML file.
     * </p>
     * @author Bryan Cazabonne
     */
    public static class TutorialVisibilityCircle {

        /** Ground station. */
        private TutorialStation station;

        /** Minimum elevation for defining visibility above ground. */
        private double minElevation;

        /** Assumed satellite altitude. */
        private double spacecraftAltitude;

        /** Number of points to compute. */
        private int pointsNumber;

        /**
         * Get the ground station.
         * @return the ground station
         */
        public TutorialStation getStation() {
            return station;
        }

        /**
         * Set the ground station.
         * @param station ground station
         */
        public void setStation(final TutorialStation station) {
            this.station = station;
        }

        /**
         * Get the minimum elevation for defining visibility above ground.
         * @return the minimum elevation for defining visibility above ground (°)
         */
        public double getMinElevation() {
            return minElevation;
        }

        /**
         * Set the minimum elevation for defining visibility above ground.
         * @param minElevation minimum elevation for defining visibility above ground (°)
         */
        public void setMinElevation(final double minElevation) {
            this.minElevation = minElevation;
        }

        /**
         * Get the satellite altitude.
         * @return the satellite altitude (m)
         */
        public double getSpacecraftAltitude() {
            return spacecraftAltitude;
        }

        /**
         * Set the satellite altitude.
         * @param spacecraftAltitude satellite altitude (m)
         */
        public void setSpacecraftAltitude(final double spacecraftAltitude) {
            this.spacecraftAltitude = spacecraftAltitude;
        }

        /**
         * Get the number of points to compute.
         * @return the number of points to compute
         */
        public int getPointsNumber() {
            return pointsNumber;
        }

        /**
         * Set the number of points to compute.
         * @param pointsNumber number of points to compute
         */
        public void setPointsNumber(final int pointsNumber) {
            this.pointsNumber = pointsNumber;
        }

    }

}
