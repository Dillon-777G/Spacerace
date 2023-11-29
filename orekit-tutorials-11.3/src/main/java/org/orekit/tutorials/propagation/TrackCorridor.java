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

import org.hipparchus.geometry.euclidean.threed.Line;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.LofOffset;
import org.orekit.bodies.BodyShape;
import org.orekit.bodies.GeodeticPoint;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.FramesFactory;
import org.orekit.frames.LOFType;
import org.orekit.frames.Transform;
import org.orekit.orbits.CircularOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.analytical.EcksteinHechlerPropagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScale;
import org.orekit.time.TimeScalesFactory;
import org.orekit.tutorials.yaml.TutorialOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialCircularOrbit;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialTLE;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/** Orekit tutorial for track corridor display.
 * @author Luc Maisonobe
 */
public class TrackCorridor {

    /** Private constructor for utility class. */
    private TrackCorridor() {
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
            final File input  = new File(TrackCorridor.class.getResource("/track-corridor.yaml").toURI().getPath());
            final File output = new File(input.getParentFile(), "track-corridor.csv");

            new TrackCorridor().run(input, output, ",");

            System.out.println("corridor saved as file " + output);

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
        final TutorialTrackCorridor inputData = mapper.readValue(input, TutorialTrackCorridor.class);

        final TimeScale utc = TimeScalesFactory.getUTC();

        final Propagator propagator;
        if (inputData.getTle() != null) {
            final TutorialTLE tleData = inputData.getTle();
            propagator = createPropagator(tleData.getLine1(),
                                          tleData.getLine2());
        } else {
            final TutorialOrbit         orbitData    = inputData.getOrbit();
            final TutorialCircularOrbit circularData = orbitData.getOrbitType().getCircular();
            propagator = createPropagator(new AbsoluteDate(orbitData.getDate(), utc),
                                          circularData.getA(),
                                          circularData.getEx(),
                                          circularData.getEy(),
                                          FastMath.toRadians(circularData.getI()),
                                          FastMath.toRadians(circularData.getRaan()),
                                          FastMath.toRadians(circularData.getAlphaV()));
        }

        // simulation properties
        final AbsoluteDate start = new AbsoluteDate(inputData.getStartDate(), utc);
        final double duration    = inputData.getDuration();
        final double step        = inputData.getStep();
        final double angle       = FastMath.toRadians(inputData.getAngularOffset());

        // set up a handler to gather all corridor points
        final CorridorHandler handler = new CorridorHandler(angle);
        propagator.getMultiplexer().add(step, handler);

        // perform propagation, letting the step handler populate the corridor
        propagator.propagate(start, start.shiftedBy(duration));

        // retrieve the built corridor
        final List<CorridorPoint> corridor = handler.getCorridor();

        // create a 7 columns csv file representing the corridor in the user home directory, with
        // date in column 1 (in ISO-8601 format)
        // left limit latitude in column 2 and left limit longitude in column 3
        // center track latitude in column 4 and center track longitude in column 5
        // right limit latitude in column 6 and right limit longitude in column 7
        final DecimalFormat format = new DecimalFormat("#00.00000", new DecimalFormatSymbols(Locale.US));
        try (PrintStream stream = new PrintStream(output, StandardCharsets.UTF_8.name())) {
            for (CorridorPoint p : corridor) {
                stream.println(p.date + separator +
                               format.format(FastMath.toDegrees(p.left.getLatitude()))    + separator +
                               format.format(FastMath.toDegrees(p.left.getLongitude()))   + separator +
                               format.format(FastMath.toDegrees(p.center.getLatitude()))  + separator +
                               format.format(FastMath.toDegrees(p.center.getLongitude())) + separator +
                               format.format(FastMath.toDegrees(p.right.getLatitude()))   + separator +
                               format.format(FastMath.toDegrees(p.right.getLongitude())));
            }
        }

    }

    /** Create an orbit propagator for a circular orbit.
     * @param a  semi-major axis (m)
     * @param ex e cos(ω), first component of circular eccentricity vector
     * @param ey e sin(ω), second component of circular eccentricity vector
     * @param i inclination (rad)
     * @param raan right ascension of ascending node (Ω, rad)
     * @param alpha  an + ω, mean latitude argument (rad)
     * @param date date of the orbital parameters
     * @return an orbit propagator
     */
    private Propagator createPropagator(final AbsoluteDate date,
                                        final double a, final double ex, final double ey,
                                        final double i, final double raan,
                                        final double alpha) {

        // create orbit
        final Orbit initialOrbit = new CircularOrbit(a, ex, ey, i, raan, alpha, PositionAngle.MEAN,
                                                     FramesFactory.getEME2000(), date,
                                                     Constants.EIGEN5C_EARTH_MU);

        // create propagator
        final Propagator propagator =
                new EcksteinHechlerPropagator(initialOrbit,
                                              new LofOffset(initialOrbit.getFrame(), LOFType.TNW),
                                              Constants.EIGEN5C_EARTH_EQUATORIAL_RADIUS,
                                              Constants.EIGEN5C_EARTH_MU,  Constants.EIGEN5C_EARTH_C20,
                                              Constants.EIGEN5C_EARTH_C30, Constants.EIGEN5C_EARTH_C40,
                                              Constants.EIGEN5C_EARTH_C50, Constants.EIGEN5C_EARTH_C60);

        return propagator;

    }

    /** Create an orbit propagator for a TLE orbit.
     * @param line1 firs line of the TLE
     * @param line2 second line of the TLE
     * @return an orbit propagator
     */
    private Propagator createPropagator(final String line1, final String line2) {

        // create pseudo-orbit
        final TLE tle = new TLE(line1, line2);

        // create propagator
        final Propagator propagator = TLEPropagator.selectExtrapolator(tle);

        return propagator;

    }

    /** Step handler storing corridor points. */
    private static class CorridorHandler implements OrekitFixedStepHandler {

        /** Earth model. */
        private final BodyShape earth;

        /** Radial offset from satellite to some distant point at specified angular offset. */
        private final double deltaR;

        /** Cross-track offset from satellite to some distant point at specified angular offset. */
        private final double deltaC;

        /** Corridor. */
        private final List<CorridorPoint> corridor;

        /** simple constructor.
         * @param angle angular offset of corridor boundaries
         */
        CorridorHandler(final double angle) {

            // set up Earth model
            earth = new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                         Constants.WGS84_EARTH_FLATTENING,
                                         FramesFactory.getITRF(IERSConventions.IERS_2010, false));

            // set up position offsets, using Earth radius as an arbitrary distance
            deltaR = Constants.WGS84_EARTH_EQUATORIAL_RADIUS * FastMath.cos(angle);
            deltaC = Constants.WGS84_EARTH_EQUATORIAL_RADIUS * FastMath.sin(angle);

            // prepare an empty corridor
            corridor = new ArrayList<TrackCorridor.CorridorPoint>();

        }

        /** {@inheritDoc} */
        public void handleStep(final SpacecraftState currentState) {

            // compute sub-satellite track
            final AbsoluteDate  date    = currentState.getDate();
            final PVCoordinates pvInert = currentState.getPVCoordinates();
            final Transform     t       = currentState.getFrame().getTransformTo(earth.getBodyFrame(), date);
            final Vector3D      p       = t.transformPosition(pvInert.getPosition());
            final Vector3D      v       = t.transformVector(pvInert.getVelocity());
            final GeodeticPoint center  = earth.transform(p, earth.getBodyFrame(), date);

            // compute left and right corridor points
            final Vector3D      nadir      = p.normalize().negate();
            final Vector3D      crossTrack = p.crossProduct(v).normalize();
            final Line          leftLine   = new Line(p, new Vector3D(1.0, p, deltaR, nadir,  deltaC, crossTrack), 1.0e-10);
            final GeodeticPoint left       = earth.getIntersectionPoint(leftLine, p, earth.getBodyFrame(), date);
            final Line          rightLine  = new Line(p, new Vector3D(1.0, p, deltaR, nadir, -deltaC, crossTrack), 1.0e-10);
            final GeodeticPoint right      = earth.getIntersectionPoint(rightLine, p, earth.getBodyFrame(), date);

            // add the corridor points
            corridor.add(new CorridorPoint(date, left, center, right));

        }

        /** Get the corridor.
         * @return build corridor
         */
        public List<CorridorPoint> getCorridor() {
            return corridor;
        }

    }

    /** Container for corridor points. */
    private static class CorridorPoint {

        /** Point date. */
        private final AbsoluteDate date;

        /** Left limit. */
        private final GeodeticPoint left;

        /** Central track point. */
        private final GeodeticPoint center;

        /** Right limit. */
        private final GeodeticPoint right;

        /** Simple constructor.
         * @param date point date
         * @param left left limit
         * @param center central track point
         * @param right right limit
         */
        CorridorPoint(final AbsoluteDate date, final GeodeticPoint left,
                      final GeodeticPoint center, final GeodeticPoint right) {
            this.date   = date;
            this.left   = left;
            this.center = center;
            this.right  = right;
        }

    }

    /**
     * Input data for the TrackCorridor tutorial.
     * <p>
     * Data are read from a YAML file.
     * </p>
     * @author Bryan Cazabonne
     */
    public static class TutorialTrackCorridor {

        /** TLE data. */
        private TutorialTLE tle;

        /** Orbit data. */
        private TutorialOrbit orbit;

        /** Simulation start date. */
        private String startDate;

        /** Simulation duration (s). */
        private double duration;

        /** Time step between each points (s). */
        private double step;

        /** Angular offset (half width of the corridor, as seen from spacecraft, in degrees). */
        private double angularOffset;

        /**
         * Get the TLE data.
         * @return the TLE data
         */
        public TutorialTLE getTle() {
            return tle;
        }

        /**
         * Set the TLE data.
         * @param tle TLE data
         */
        public void setTle(final TutorialTLE tle) {
            this.tle = tle;
        }

        /**
         * Get the orbit data (circular orbit).
         * @return the orbit data
         */
        public TutorialOrbit getOrbit() {
            return orbit;
        }

        /**
         * Set the orbit data (circular orbit).
         * @param orbit orbit data
         */
        public void setOrbit(final TutorialOrbit orbit) {
            this.orbit = orbit;
        }

        /**
         * Get the simulation start date.
         * @return the simulation start date
         */
        public String getStartDate() {
            return startDate;
        }

        /**
         * Set the simulation start date.
         * @param startDate simulation start date
         */
        public void setStartDate(final String startDate) {
            this.startDate = startDate;
        }

        /**
         * Get the simulation duration.
         * @return the simulation duration (s)
         */
        public double getDuration() {
            return duration;
        }

        /**
         * Set the simulation duration.
         * @param duration simulation duration (s)
         */
        public void setDuration(final double duration) {
            this.duration = duration;
        }

        /**
         * Get the time step between each points.
         * @return the time step between each points (s)
         */
        public double getStep() {
            return step;
        }

        /**
         * Set the time step between each points.
         * @param step time step between each points (s)
         */
        public void setStep(final double step) {
            this.step = step;
        }

        /**
         * Get the angular offset.
         * @return the angular offset (°)
         */
        public double getAngularOffset() {
            return angularOffset;
        }

        /**
         * Set the angular offset.
         * @param angularOffset angular offset (°)
         */
        public void setAngularOffset(final double angularOffset) {
            this.angularOffset = angularOffset;
        }

    }

}
