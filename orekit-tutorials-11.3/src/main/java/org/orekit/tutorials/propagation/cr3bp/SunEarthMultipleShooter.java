/* Copyright 2002-2017 CS GROUP
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.ode.ODEIntegrator;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.attitudes.Attitude;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.forces.gravity.NewtonianAttraction;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.ThirdBodyAttractionEpoch;
import org.orekit.frames.Frame;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.EpochDerivativesEquations;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.TimeScalesFactory;
import org.orekit.utils.AbsolutePVCoordinates;
import org.orekit.utils.MultipleShooter;
import org.orekit.utils.PVCoordinates;

/** Correction of a trajectory using multiple shooting method.
 *
 * A trajectory is computed and split in several patch points.
 * One patch point is perturbated, and then the trajectory is corrected
 *
 * In this problem, the choice has been made to fix the epoch and the,
 * position of the first patch point, and the position of the last patch point.
 *
 * @author William Desprats
 *
 */

public class SunEarthMultipleShooter {

    /** Private constructor for utility class. */
    private SunEarthMultipleShooter() {
        // empty
    }

    /** Program entry point.
     * @param args program arguments
     */
    public static void main(final String[] args) {

        // configure Orekit
        // ----------------
        final File home       = new File(System.getProperty("user.home"));
        final File orekitData = new File(home, "orekit-data");
        if (!orekitData.exists()) {
            System.err.format(Locale.US, "Failed to find %s folder%n",
                              orekitData.getAbsolutePath());
            System.err.format(Locale.US, "You need to download %s from the %s page and unzip it in %s for this tutorial to work%n",
                              "orekit-data.zip", "https://www.orekit.org/forge/projects/orekit/files",
                              home.getAbsolutePath());
            System.exit(1);
        }
        final DataProvidersManager manager = DataContext.getDefault().getDataProvidersManager();
        manager.addProvider(new DirectoryCrawler(orekitData));

        // Time settings
        // -------------
        final AbsoluteDate initialDate =
                        new AbsoluteDate(2000, 01, 01, 0, 0, 00.000,
                                         TimeScalesFactory.getUTC());
        final double arcDuration = 10000;

        final PVCoordinates firstGuess = new PVCoordinates(new Vector3D(1.25E10, 1.450E11, -7.5E9),
                                                           new Vector3D(-30000.0, 2500.0, -3500.0));

        // Integration parameters
        // ---------------------------
        // Adaptive stepsize boundaries
        final double minStep = 1;
        final double maxstep = 30000;

        // Integrator tolerances
        final double positionTolerance = 1000;
        final double velocityTolerance = 1;
        final double massTolerance = 1.0e-6;
        final double[] vecAbsoluteTolerances = {positionTolerance, positionTolerance, positionTolerance, velocityTolerance, velocityTolerance, velocityTolerance, massTolerance };
        final double[] vecRelativeTolerances =
                        new double[vecAbsoluteTolerances.length];

        // Integrator definition
        final AdaptiveStepsizeIntegrator integrator =
                        new DormandPrince853Integrator(minStep, maxstep,
                                                       vecAbsoluteTolerances,
                                                       vecRelativeTolerances);

        // Load Celestial bodies
        // ---------------------
        final CelestialBody   sun   = CelestialBodyFactory.getSun();
        final CelestialBody   earth = CelestialBodyFactory.getEarthMoonBarycenter();
        final Frame primaryFrame    = sun.getInertiallyOrientedFrame();

        // Trajectory definition
        // ---------------------
        final int narcs = 5;
        final List<SpacecraftState> correctedList = new ArrayList<>(narcs + 1);
        final AbsolutePVCoordinates firstGuessAPV = new AbsolutePVCoordinates(primaryFrame, initialDate, firstGuess);
        final List<SpacecraftState> firstGuessList2 = generatePatchPointsEphemeris(sun, earth, firstGuessAPV, arcDuration, narcs, integrator);
        final List<NumericalPropagator> propagatorList  = initializePropagators(sun, earth, integrator, narcs);
        final List<EpochDerivativesEquations> additionalEquations = addAdditionalProviders(propagatorList);

        for (int i = 0; i < narcs + 1; i++) {
            final SpacecraftState sp = firstGuessList2.get(i);
            correctedList.add(new SpacecraftState(sp.getAbsPVA(), sp.getAttitude()));
        }

        // Perturbation on a patch point
        // -----------------------------

        final int nP = 1; // Perturbated patch point
        final Vector3D deltaP = new Vector3D(-50000, 1000, 0);
        final Vector3D deltaV = new Vector3D(0.1, 0, 1.0);
        final double deltaEpoch = 1000;

        System.out.format(Locale.US, "Pertubation on point n° %d:%n", nP);
        System.out.format(Locale.US, "DeltaP  [m]  = (%5.0f, %4.0f, %1.0f)%n", deltaP.getX(), deltaP.getY(), deltaP.getZ());
        System.out.format(Locale.US, "DeltaV [m/s] = (%4.3f, %4.3f, %4.3f) %n", deltaV.getX(), deltaV.getY(), deltaV.getZ());
        System.out.format(Locale.US, "Delta Epoch [s] = %5.0f %n", deltaEpoch);

        final SpacecraftState firstGuessSP = correctedList.get(nP);
        final AttitudeProvider attPro = propagatorList.get(nP).getAttitudeProvider();

        // Small change of the a patch point
        final Vector3D newPos = firstGuessSP.getAbsPVA().getPosition().add(deltaP);
        final Vector3D newVel = firstGuessSP.getAbsPVA().getVelocity().add(deltaV);
        final AbsoluteDate newDate = firstGuessSP.getDate().shiftedBy(deltaEpoch);
        final AbsolutePVCoordinates absPva = new AbsolutePVCoordinates(firstGuessSP.getFrame(), newDate, newPos, newVel);
        final Attitude attitude = attPro.getAttitude(absPva, newDate, absPva.getFrame());
        final SpacecraftState newSP = new SpacecraftState(absPva, attitude);
        correctedList.set(1, newSP);

        final double tolerance = 1.0;
        final int    maxIter   = 10;

        final MultipleShooter multipleShooting = new MultipleShooter(correctedList, propagatorList, additionalEquations, arcDuration, tolerance, maxIter);
        multipleShooting.setPatchPointComponentFreedom(1, 0, false);
        multipleShooting.setPatchPointComponentFreedom(1, 1, false);
        multipleShooting.setPatchPointComponentFreedom(1, 2, false);
        multipleShooting.setPatchPointComponentFreedom(1, 3, false);
        multipleShooting.setPatchPointComponentFreedom(narcs + 1, 0, false);
        multipleShooting.setPatchPointComponentFreedom(narcs + 1, 1, false);
        multipleShooting.setPatchPointComponentFreedom(narcs + 1, 2, false);
        multipleShooting.setEpochFreedom(1, false);

        multipleShooting.compute();

        System.out.format(Locale.US, "Differences between unperpertubed trajectory and %ncorrected trajectory after perturbation:%n");
        for (int i = 0; i < correctedList.size(); i++) {
            final Vector3D initialP = firstGuessList2.get(i).getAbsPVA().getPosition();
            final Vector3D initialV = firstGuessList2.get(i).getAbsPVA().getVelocity();
            final Vector3D correctedP = correctedList.get(i).getAbsPVA().getPosition();
            final Vector3D correctedV = correctedList.get(i).getAbsPVA().getVelocity();
            System.out.format(Locale.US, "Point n° %d %n", i);
            System.out.format(Locale.US, "Position Error: %10.6f [m]%n", Vector3D.distance(initialP, correctedP));
            System.out.format(Locale.US, "Velocity Error: %10.6f [m/s]%n", Vector3D.distance(initialV, correctedV));

        }
    }

    private static List<SpacecraftState> generatePatchPointsEphemeris(final CelestialBody primary, final CelestialBody secondary,
                                                                      final AbsolutePVCoordinates initialAPV, final double arcDuration,
                                                                      final int narcs, final ODEIntegrator integrator) {

        final List<SpacecraftState> firstGuessList = new ArrayList<>(narcs + 1);

        final double integrationTime = arcDuration;

        // Creating the initial spacecraftstate that will be given to the propagator

        final SpacecraftState initialState2 = new SpacecraftState(initialAPV);

        firstGuessList.add(initialState2);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);

        propagator.addForceModel(new NewtonianAttraction(primary.getGM()));
        propagator.addForceModel(new ThirdBodyAttraction(secondary));
        propagator.setOrbitType(null);


        propagator.setInitialState(initialState2);

        AbsoluteDate currentDate = initialAPV.getDate();
        for (int i = 0; i < narcs; i++) {

            currentDate = currentDate.shiftedBy(integrationTime);
            final SpacecraftState sp = propagator.propagate(currentDate);

            System.out.println(sp.getPVCoordinates());

            firstGuessList.add(sp);
        }
        return firstGuessList;
    }

    private static List<NumericalPropagator> initializePropagators(final CelestialBody primary, final CelestialBody secondary, final ODEIntegrator integrator,
                                                                   final int propagationNumber) {
        final List<NumericalPropagator> propagatorList = new ArrayList<>(propagationNumber);

        // Definition of the propagator
        for (int i = 0; i < propagationNumber; i++) {

            final NumericalPropagator propagator = new NumericalPropagator(integrator);

            propagator.addForceModel(new NewtonianAttraction(primary.getGM()));
            propagator.addForceModel(new ThirdBodyAttractionEpoch(secondary));

            propagator.setOrbitType(null);
            propagatorList.add(propagator);
        }
        return propagatorList;
    }

    private static List<EpochDerivativesEquations> addAdditionalProviders(final List<NumericalPropagator> propagatorList) {
        final int narcs = propagatorList.size();
        final List<EpochDerivativesEquations> additionalProviders = new ArrayList<>(narcs);
        for (int i = 0; i < narcs; i++) {
            additionalProviders.add(new EpochDerivativesEquations("derivatives", propagatorList.get(i)));
        }
        return additionalProviders;

    }
}
