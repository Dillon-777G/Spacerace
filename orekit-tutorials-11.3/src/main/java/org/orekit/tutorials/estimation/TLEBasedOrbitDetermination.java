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
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.radiation.RadiationSensitive;
import org.orekit.frames.Frame;
import org.orekit.models.earth.atmosphere.Atmosphere;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.analytical.tle.TLE;
import org.orekit.propagation.analytical.tle.TLEConstants;
import org.orekit.propagation.analytical.tle.TLEPropagator;
import org.orekit.propagation.conversion.ODEIntegratorBuilder;
import org.orekit.propagation.conversion.TLEPropagatorBuilder;
import org.orekit.time.AbsoluteDate;
import org.orekit.tutorials.estimation.common.AbstractOrbitDetermination;
import org.orekit.tutorials.estimation.common.TutorialOrbitDetermination;
import org.orekit.tutorials.yaml.TutorialOrbitType.TutorialTLE;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.PVCoordinates;
import org.orekit.utils.ParameterDriver;

/**
 * The purpose of the tutorial is to present a TLE based orbit determination.
 * <p>
 * Position data from a SP3 file are used as observations for the orbit
 * determination process.
 * The dynamical model used for the satellite is the SGP4/SDP4 model.
 * An input TLE (from Celestrak) is used as initial guess. The purpose
 * is to generate a new TLE, with a better accuracy thanks to the
 * estimation process.
 * </p>
 * @author Bryan Cazabonne
 *
 */
public class TLEBasedOrbitDetermination extends AbstractOrbitDetermination<TLEPropagatorBuilder> {

    /** Initial TLE. */
    private TLE templateTLE;

    /** Estimated TLE. */
    private TLE estimatedTLE;

    /** Frame of the orbit determination. */
    private Frame frame;

    /**
     * Program entry point.
     * @param args program arguments (unused here)
     */
    public static void main(final String[] args) {
        try {

            // Input in tutorial resources directory
            final String inputPath = GNSSOrbitDetermination.class.getClassLoader().
                                     getResource("tle/tle-orbit-determination-GPS07.yaml").toURI().getPath();

            // Run
            new TLEBasedOrbitDetermination().run(new File(inputPath));

        } catch (URISyntaxException | IOException | OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }

    }

    /** {@inheritDoc} */
    @Override
    protected void init(final TutorialOrbitDetermination inputData) {

        // Frame
        frame = inputData.getOrbit().getInertialFrame();

        // Template TLE
        final TutorialTLE tleData = inputData.getOrbit().getOrbitType().getTle();
        final String line1 = tleData.getLine1();
        final String line2 = tleData.getLine2();
        templateTLE = new TLE(line1, line2);

        // Print the initial TLE
        System.out.println("Input TLE:");
        System.out.println(templateTLE.toString());
        System.out.println("");

    }

    /** {@inheritDoc} */
    @Override
    protected void finish(final Propagator[] estimated) {

        // Estimate TLE propagator
        final TLEPropagator estimatedTlePropagator = (TLEPropagator) estimated[0];
        estimatedTLE = estimatedTlePropagator.getTLE();

        // Print the estimated TLE
        System.out.println("");
        System.out.println("Estimated TLE:");
        System.out.println(estimatedTLE.toString());
        System.out.println("");

    }

    /** {@inheritDoc} */
    @Override
    protected void createGravityField(final TutorialOrbitDetermination inputData)
        throws NoSuchElementException {
        // TLE OD does not need gravity field
    }

    /** {@inheritDoc} */
    @Override
    protected double getMu() {
        return TLEConstants.MU;
    }

    /** {@inheritDoc} */
    @Override
    protected TLEPropagatorBuilder createPropagatorBuilder(final Orbit referenceOrbit,
                                                           final ODEIntegratorBuilder builder,
                                                           final double positionScale) {
        return new TLEPropagatorBuilder(templateTLE, PositionAngle.MEAN,
                                        positionScale);
    }

    /** {@inheritDoc} */
    @Override
    protected void setMass(final TLEPropagatorBuilder propagatorBuilder,
                           final double mass) {
        // TLE OD does not need to set mass
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setGravity(final TLEPropagatorBuilder propagatorBuilder,
                                               final OneAxisEllipsoid body) {
        return Collections.emptyList();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setOceanTides(final TLEPropagatorBuilder propagatorBuilder,
                                                  final IERSConventions conventions, final OneAxisEllipsoid body,
                                                  final int degree, final int order) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Ocean tides not implemented in TLE Propagator");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setSolidTides(final TLEPropagatorBuilder propagatorBuilder,
                                                  final IERSConventions conventions, final OneAxisEllipsoid body,
                                                  final CelestialBody[] solidTidesBodies) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Solid tides not implemented in TLE Propagator");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setThirdBody(final TLEPropagatorBuilder propagatorBuilder,
                                                 final CelestialBody thirdBody) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Third body not implemented in TLE Propagator");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setDrag(final TLEPropagatorBuilder propagatorBuilder,
                                            final Atmosphere atmosphere,
                                            final DragSensitive spacecraft) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Drag not implemented in TLE Propagator");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setSolarRadiationPressure(final TLEPropagatorBuilder propagatorBuilder,
                                                              final CelestialBody sun, final double equatorialRadius,
                                                              final RadiationSensitive spacecraft) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "SRP not implemented in TLE Propagator");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setRelativity(final TLEPropagatorBuilder propagatorBuilder) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Relativity not implemented in TLE Propagator");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setPolynomialAcceleration(final TLEPropagatorBuilder propagatorBuilder,
                                                              final String name,
                                                              final Vector3D direction,
                                                              final int degree) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Polynomial acceleration not implemented in TLE Propagator");
    }

    /** {@inheritDoc} */
    @Override
    protected void setAttitudeProvider(final TLEPropagatorBuilder propagatorBuilder,
                                       final AttitudeProvider attitudeProvider) {
        propagatorBuilder.setAttitudeProvider(attitudeProvider);
    }

    /** {@inheritDoc} */
    @Override
    protected void compareWithReference(final Orbit estimatedOrbit)
        throws IOException {
        // Reference results at TLE epoch and in TEME frame
        final Vector3D refPos = new Vector3D(1.3931486005985674E7, -2.2866200308614843E7, -730.5222800564768);
        final Vector3D refVel = new Vector3D(1869.502095268089, 1120.6318571146398, 3164.7668022992707);

        // Print results
        System.out.println("Comparison with reference orbit from SP3 file");
        System.out.println("=============================================");

        // Initial TLE
        final PVCoordinates pvInit = TLEPropagator.selectExtrapolator(templateTLE).getPVCoordinates(templateTLE.getDate(), frame);
        System.out.println("Initial TLE from external provider:");
        System.out.println("ΔP_init(m) = " + Vector3D.distance(refPos,   pvInit.getPosition()));
        System.out.println("ΔV_init(m/s) = " + Vector3D.distance(refVel, pvInit.getVelocity()));

        // Estimated TLE
        System.out.println("Estimated:");
        System.out.println("ΔP_final(m) = " + Vector3D.distance(refPos, estimatedOrbit.getPVCoordinates().getPosition()));
        System.out.println("ΔV_final(m/s) = " + Vector3D.distance(refVel, estimatedOrbit.getPVCoordinates().getVelocity()));
        System.out.println("");

    }

    @Override
    protected List<ParameterDriver> setManeuver(final TLEPropagatorBuilder propagatorBuilder, final AbsoluteDate fireDate,
                                                final double duration, final double thrust, final double isp, final Vector3D direction, final String name) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                  "Maneuvers not implemented for TLEBasedOrbitDetermination");
    }

}
