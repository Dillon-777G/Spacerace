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
package org.orekit.tutorials.propagation.cr3bp;

import java.io.File;
import java.util.Locale;

import org.hipparchus.ode.events.Action;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.CR3BPFactory;
import org.orekit.bodies.CR3BPSystem;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
import org.orekit.orbits.HaloOrbit;
import org.orekit.orbits.LibrationOrbitFamily;
import org.orekit.orbits.RichardsonExpansion;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.events.EventDetector;
import org.orekit.propagation.events.handlers.EventHandler;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.propagation.numerical.cr3bp.CR3BPForceModel;
import org.orekit.propagation.sampling.OrekitFixedStepHandler;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.LagrangianPoints;
import org.orekit.utils.PVCoordinates;

/**
 * Orekit tutorial for the computation of a northern Halo Orbit around Earth-Moon L1.
 * @author Vincent Mouraux
 */
public class EarthMoonHaloOrbit {

    /** Distance between the two primaries in the circular restricted system [m]. */
    private static double dDim;

    /** Orbital Period in the circular restricted system [s]. */
    private static double tDim;

    /** Checkpoints. */
    private static double outputStep;

    /** Simple counter. */
    private static double compteur = 0;

    /**
     * Private constructor for utility class.
     */
    private EarthMoonHaloOrbit() {
        // empty
    }

    public static void main(final String[] args) {

        // configure Orekit data provider
        final File home = new File(System.getProperty("user.home"));
        final File orekitData = new File(home, "orekit-data");
        if (!orekitData.exists()) {
            System.err.format(Locale.US, "Failed to find %s folder%n",
                              orekitData.getAbsolutePath());
            System.err
                .format(Locale.US,
                        "You need to download %s from the %s page and unzip it in %s for this tutorial to work%n",
                        "orekit-data.zip",
                        "https://www.orekit.org/forge/projects/orekit/files",
                        home.getAbsolutePath());
            System.exit(1);
        }
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));

        // Get the Earth-Moon Circular Restricted system
        final CR3BPSystem syst = CR3BPFactory.getEarthMoonCR3BP();

        // Define a Northern Halo orbit around Earth-Moon L1 with a Z-amplitude
        // of 8 000 km
        final HaloOrbit h =
            new HaloOrbit(new RichardsonExpansion(syst, LagrangianPoints.L1), 8E6,
                          LibrationOrbitFamily.NORTHERN);

        // Get the CR3BP Rotating Frame centered on Earth-Moon Barycenter
        final Frame Frame = syst.getRotatingFrame();

        // Time settings
        final AbsoluteDate initialDate =
            new AbsoluteDate(1996, 06, 25, 0, 0, 00.000,
                             TimeScalesFactory.getUTC());

        // Get the characteristic distance of the system, distance between m1
        // and m2
        dDim = syst.getDdim();

        // Get the characteristic time of the system, orbital period of m2
        tDim = syst.getTdim();

        System.out.println("         Northern Halo Orbit around Earth-Moon L1\n");

        final double orbitalPeriod = h.getOrbitalPeriod();
        System.out.println("Orbital Period of the expected Halo orbit: " + orbitalPeriod * tDim / 2 / FastMath.PI / 86400 + " days\n");

        final double integrationTime = orbitalPeriod;
        outputStep = 0.01;

        // Integration parameters
        // These parameters are used for the Dormand-Prince integrator, a
        // variable step integrator,
        // these limits prevent the integrator to spend too much time when the
        // equations are too stiff,
        // as well as the reverse situation.
        final double minStep = 1E-10;
        final double maxstep = 1E-3;

        // tolerances for integrators
        // Used by the integrator to estimate its variable integration step
        final double positionTolerance = 1.0E-6;
        final double velocityTolerance = 1.0E-6;
        final double massTolerance = 1.0e-6;
        final double[] vecAbsoluteTolerances = {
            positionTolerance, positionTolerance, positionTolerance,
            velocityTolerance, velocityTolerance, velocityTolerance,
            massTolerance};
        final double[] vecRelativeTolerances = new double[vecAbsoluteTolerances.length];

        // Defining the numerical integrator that will be used by the propagator
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(minStep, maxstep,
                                           vecAbsoluteTolerances,
                                           vecRelativeTolerances);


        // Differential correction on the first guess necessary to find a point that will lead to an Halo Orbit.
        h.applyDifferentialCorrection();

        // Return a PVCoordinates leading to the Halo Orbit
        final PVCoordinates initialConditions = h.getInitialPV();

        // Define a clean propagator for the final propagation
        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        // The following steps are the same as in a standard propagation in CR3BP
        final AbsolutePVCoordinates initialAbsPV =
            new AbsolutePVCoordinates(Frame, initialDate, initialConditions);

        final SpacecraftState initialState = new SpacecraftState(initialAbsPV);

        // Event detector settings
        final double maxcheck = 10;
        final double threshold = 1E-10;

        // Event detector definition
        final EventDetector sphereCrossing =
            new CR3BPSphereCrossingDetector(6378E3, 1737E3, syst, maxcheck, threshold).withHandler(new SphereCrossingHandler());

        propagator.setOrbitType(null);
        propagator.setIgnoreCentralAttraction(true);
        propagator.addForceModel(new CR3BPForceModel(syst));
        propagator.setInitialState(initialState);
        propagator.addEventDetector(sphereCrossing);
        propagator.getMultiplexer().add(outputStep, new TutorialStepHandler());

        final SpacecraftState finalState =
            propagator.propagate(initialDate.shiftedBy(integrationTime));

        System.out.println("\nInitial position: " + initialState.getPVCoordinates().getPosition().scalarMultiply(dDim));
        System.out.println("Final position: " + finalState.getPVCoordinates().getPosition().scalarMultiply(dDim));
    }

    private static class TutorialStepHandler implements OrekitFixedStepHandler {

        /** {@inheritDoc}. */
        @Override
        public void init(final SpacecraftState s0, final AbsoluteDate t,
                         final double step) {
            System.out.format(Locale.US, "%s %s %s %s %n", "date",
                              "                              X",
                              "                     Y",
                              "                       Z");
        }

        /** {@inheritDoc}. */
        @Override
        public void handleStep(final SpacecraftState currentState) {
            try {
                final double duration = 0.5 * compteur * outputStep * tDim / FastMath.PI;
                compteur++;

                final AbsoluteDate d =
                    currentState.getDate().shiftedBy(duration);
                final double px =
                    currentState.getPVCoordinates().getPosition()
                        .getX() * dDim;
                final double py =
                    currentState.getPVCoordinates().getPosition()
                        .getY() * dDim;
                final double pz =
                    currentState.getPVCoordinates().getPosition()
                        .getZ() * dDim;

                System.out.format(Locale.US, "%s  %18.12f  %18.12f  %18.12f%n",
                                  d, px, py, pz);

            } catch (OrekitException oe) {
                System.err.println(oe.getMessage());
            }
        }
    }

    /**
     * Static class for event detection.
     */
    private static class SphereCrossingHandler implements EventHandler<CR3BPSphereCrossingDetector> {

        /** {@inheritDoc}. */
        @Override
        public Action eventOccurred(final SpacecraftState s,
                                    final CR3BPSphereCrossingDetector detector,
                                    final boolean increasing) {
            System.out.println("You intersected one of the two primaries so the propagation has been stopped");
            return Action.STOP;
        }
    }
}
