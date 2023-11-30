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

import org.hipparchus.geometry.euclidean.threed.Rotation;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.events.Action;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.InertialProvider;
import org.orekit.bodies.CR3BPFactory;
import org.orekit.bodies.CR3BPSystem;
import org.orekit.bodies.CelestialBody;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.errors.OrekitException;
import org.orekit.frames.Frame;
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
 * Simple tutorial giving an example of propagation in the Circular Restricted 3 Body problem with Orekit.
 * @author Vincent Mouraux
 */
public class PropagationInCR3BP {

    /** Return. */
    private static final String RETURN = "\n";

    /** Double return. */
    private static final String DOUBLE_RETURN = "\n \n";

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
    private PropagationInCR3BP() {
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

        System.out
            .print("          Earth-Moon CR3BP System tutorial \n          Trajectory around L4 stable point            \n \n");

        // Time settings
        final AbsoluteDate initialDate =
            new AbsoluteDate(1996, 06, 25, 0, 0, 0.000,
                             TimeScalesFactory.getUTC());

        // Get the Circular Restricted Three-Body Problem System Sun-Earth from
        // the factory
        final CR3BPSystem syst = CR3BPFactory.getEarthMoonCR3BP();

        // Get the characteristic distance of the system, distance between m1
        // and m2
        dDim = syst.getDdim();

        // Get the characteristic time of the system, orbital period of m2
        tDim = syst.getTdim();

        // Get the mass ratio of the system
        final double mu = syst.getMassRatio();

        System.out
            .print("Distance in meters between primary bodies: " + dDim + RETURN);
        System.out
        .print("Orbital velocity of the primary bodies: " + syst.getVdim() + RETURN);
        System.out
        .print("Orbital period of the primary bodies: " + tDim + RETURN);
        System.out.print("Mass ratio of the system: " + mu + DOUBLE_RETURN);

        // Get the position of each Lagrangian points in this CR3BP System
        final Vector3D L1 = syst.getLPosition(LagrangianPoints.L1);
        final Vector3D L2 = syst.getLPosition(LagrangianPoints.L2);
        final Vector3D L3 = syst.getLPosition(LagrangianPoints.L3);
        final Vector3D L4 = syst.getLPosition(LagrangianPoints.L4);
        final Vector3D L5 = syst.getLPosition(LagrangianPoints.L5);
        System.out.print("Earth-Moon L1 position: " + L1 + RETURN);
        System.out.print("Earth-Moon L2 position: " + L2 + RETURN);
        System.out.print("Earth-Moon L3 position: " + L3 + RETURN);
        System.out.print("Earth-Moon L4 position: " + L4 + RETURN);
        System.out.print("Earth-Moon L5 position: " + L5 + DOUBLE_RETURN);

        // Get the Rotating Frame in which both primaries are orbiting around
        // their common barycenter.
        final Frame rotatingFrame = syst.getRotatingFrame();

        // Compare the true celestial bodies position to the hypothetical
        // position in the circular restricted system
        final CelestialBody primaryBody = syst.getPrimary();
        final CelestialBody secondaryBody = syst.getSecondary();
        final Vector3D sunPos =
            primaryBody.getPVCoordinates(initialDate, rotatingFrame)
                .getPosition();
        final Vector3D earthPos =
            secondaryBody.getPVCoordinates(initialDate, rotatingFrame)
                .getPosition();

        System.out
            .print("Earth position in the Rotating Frame: " + sunPos + RETURN);
        System.out
            .print("True Moon position in the Rotating Frame using JPL Data at this time: " +
                   earthPos + DOUBLE_RETURN);

        // Starting point of the spacecraft, has to be in the Rotating Frame as
        // it will be linked to it in the next step
        // WARNING: PVCoordinates have to be in the normalized system, here the propagation starts approximately at 3000 km from L4
        // e.g in Earth-Moon CR3BP System, 1 distance unit = 384 400 km
        final PVCoordinates initialConditions =
            new PVCoordinates(new Vector3D(L4.getX(), L4.getY() + 1E-3, 0.0),
                              new Vector3D(0.0, 0.0, 0.0));

        // PVCoordinates linked to a Frame and a Date
        final AbsolutePVCoordinates initialAbsPV =
            new AbsolutePVCoordinates(rotatingFrame, initialDate,
                                      initialConditions);

        // Creating the initial spacecraft state that will be given to the
        // propagator by using AbsolutePVCoordinates and a standard attitude
        // which is not relying on spacecraft velocity
        final Attitude attitude =
            new InertialProvider(new Rotation(1, 0, 0, 0, false))
                .getAttitude(initialAbsPV, initialAbsPV.getDate(),
                             initialAbsPV.getFrame());

        final SpacecraftState initialState =
            new SpacecraftState(initialAbsPV, attitude);

        // calculation, treal = Tdim * t / (2 * pi) , e.g in Earth-Moon CR3BP
        // System, 1T = 4.35 days
        final double integrationTime = 40;
        outputStep = 0.1;

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
        final double[] vecRelativeTolerances =
            new double[vecAbsoluteTolerances.length];

        // Defining the numerical integrator that will be used by the propagator
        final AdaptiveStepsizeIntegrator integrator =
            new DormandPrince853Integrator(minStep, maxstep,
                                           vecAbsoluteTolerances,
                                           vecRelativeTolerances);

        // Creating the numerical propagator
        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        // Event detector settings
        final double maxcheck = 10;
        final double threshold = 1E-10;

        // Event detector definition, this detector will stop the propagation if the trajectory enters a sphere around one of the primaries
        // User can defines the size of the sphere
        final EventDetector sphereCrossing =
            new CR3BPSphereCrossingDetector(6378E3, 1737E3, syst, maxcheck, threshold).withHandler(new SphereCrossingHandler());

        // Non Keplerian propagation
        propagator.setOrbitType(null);

        // the real GM has to be ignored since this propagation is non Keplerian
        propagator.setIgnoreCentralAttraction(true);

        // Add our specific force model to the propagation, it has to be
        // propagated in the rotating frame
        propagator.addForceModel(new CR3BPForceModel(syst));

        // Add our event detector to the propagation
        propagator.addEventDetector(sphereCrossing);

        // Initializing propagation
        propagator.setInitialState(initialState);

        propagator.getMultiplexer().add(outputStep,
                                 new TutorialStepHandler());

        // Propagate during 40T which is approximately half a year
        final SpacecraftState finalState =
            propagator.propagate(initialDate.shiftedBy(integrationTime));
        System.out.println("\nInitial position: " + initialState.getPVCoordinates().getPosition().scalarMultiply(dDim));
        System.out.println("Final position: " + finalState.getPVCoordinates().getPosition().scalarMultiply(dDim));

        compteur = 0;
    }

    private static class TutorialStepHandler implements OrekitFixedStepHandler {

       /**
        * Constructor.
        */
        private TutorialStepHandler() {
            // empty
        }

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
                // Time is normalized with tDim/(2*pi) in CR3BP computation
                final double duration = compteur * outputStep * tDim / 2 / FastMath.PI;
                compteur++;

                // Current unnormalized propagation date
                final AbsoluteDate d =
                    currentState.getDate().shiftedBy(duration);

                // Current normalized spacecraft position
                final double x =
                    currentState.getPVCoordinates().getPosition().getX();
                final double y =
                    currentState.getPVCoordinates().getPosition().getY();
                final double z =
                    currentState.getPVCoordinates().getPosition().getZ();

                // Position is normalized with lDim in CR3BP computation
                // (Velocity with vDim)
                final double px = x * dDim;
                final double py = y * dDim;
                final double pz = z * dDim;

                System.out.format(Locale.US, "%s  %18.12f  %18.12f  %18.12f%n",
                                  d, px, py, pz);

            } catch (OrekitException oe) {
                System.err.println(oe.getMessage());
            }
        }
    }

    /**
     *  Static class for event detection.
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
