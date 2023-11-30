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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.hipparchus.Field;
import org.hipparchus.analysis.differentiation.Gradient;
import org.hipparchus.geometry.euclidean.threed.FieldVector3D;
import org.hipparchus.linear.MatrixUtils;
import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.nonstiff.AdaptiveStepsizeIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.hipparchus.util.FastMath;
import org.orekit.bodies.CelestialBodyFactory;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.forces.ForceModel;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.IsotropicDrag;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.models.earth.atmosphere.HarrisPriester;
import org.orekit.orbits.CartesianOrbit;
import org.orekit.orbits.FieldCartesianOrbit;
import org.orekit.orbits.KeplerianOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.FieldSpacecraftState;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.time.FieldAbsoluteDate;
import org.orekit.utils.Constants;
import org.orekit.utils.FieldPVCoordinates;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.TimeStampedFieldPVCoordinates;

/**
 * Gradient Computation.
 * <p>
 * Computes the Jacobian containing the partial derivatives
 * of the acceleration with respect the the spacecraft coordinates.
 * </p>
 * @author Bryan Cazabonne
 */
public class GradientComputation {

    /** Private constructor for utility class. */
    private GradientComputation() {
        // empty
    }

    /** Program entry point.
     * @param args program arguments (unused here)
     */
    public static void main(final String[] args) {

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

        // Initialize a basic frame
        final Frame frame = FramesFactory.getEME2000();

        // Initialize a basic epoch
        final AbsoluteDate epoch = AbsoluteDate.J2000_EPOCH;

        // Nominal values of the Orbital parameters
        final double a    = Constants.WGS84_EARTH_EQUATORIAL_RADIUS + 450.0e3;
        final double e    = 1e-3;
        final double i    = FastMath.toRadians(98.3);
        final double pa   = 0.5 * FastMath.PI;
        final double raan = 0.0;
        final double ni   = 0.0;
        final KeplerianOrbit kep = new KeplerianOrbit(a, e, i, pa, raan, ni, PositionAngle.ECCENTRIC, frame, epoch, Constants.WGS84_EARTH_MU);
        final CartesianOrbit car = (CartesianOrbit) OrbitType.CARTESIAN.convertType(kep);

        // Force models
        final double dragCoeff    = 1.0;
        final double crossSection = 1.0;
        final List<ForceModel> forceModels = new ArrayList<>();
        final ForceModel fModel_HFAM =
                        new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(IERSConventions.IERS_2010, true),
                                                              GravityFieldFactory.getNormalizedProvider(18, 18));
        forceModels.add(fModel_HFAM);
        final ForceModel fModel_Drag = new DragForce(new HarrisPriester(CelestialBodyFactory.getSun(),
                                                                        new OneAxisEllipsoid(Constants.WGS84_EARTH_EQUATORIAL_RADIUS,
                                                                                             Constants.WGS84_EARTH_FLATTENING,
                                                                                             FramesFactory.getITRF(IERSConventions.IERS_2010, true))),
                                                     new IsotropicDrag(crossSection, dragCoeff));
        forceModels.add(fModel_Drag);

        // Compute the Jacobian for the initial state
        final RealMatrix Jac0 = computeJacobian(forceModels, car);
        System.out.println("Jacobian with respect to initial coordinates");
        printMatrix(Jac0);

        // Setting a numerical propagator
        final double minStep = 0.001;
        final double maxstep = 1000.0;
        final double positionTolerance = 10.0;
        final OrbitType propagationType = OrbitType.CARTESIAN;
        final double[][] tolerances =
                NumericalPropagator.tolerances(positionTolerance, car, propagationType);
        final AdaptiveStepsizeIntegrator integrator =
                new DormandPrince853Integrator(minStep, maxstep, tolerances[0], tolerances[1]);

        final NumericalPropagator propagator = new NumericalPropagator(integrator);
        propagator.setOrbitType(propagationType);
        propagator.setOrbitType(propagationType);
        propagator.setInitialState(new SpacecraftState(car));
        propagator.addForceModel(fModel_HFAM);
        propagator.addForceModel(fModel_Drag);

        // Propagate
        final SpacecraftState finalState = propagator.propagate(epoch.shiftedBy(3600.0));

        // Compute the Jacobian for the final state
        final RealMatrix JacEnd = computeJacobian(forceModels, finalState.getOrbit());
        System.out.println("Jacobian with respect to final coordinates");
        printMatrix(JacEnd);

    }

    /**
     * Compute the Jacobian of the partial derivatives of the acceleration with respect to spacecraft coordinates.
     * @param forceModels list of force models
     * @param orbit orbit containing spacecraft coordinates
     * @return the Jacobian matrix
     */
    private static RealMatrix computeJacobian(final List<ForceModel> forceModels, final Orbit orbit) {

        // Convert Orbit to its "Field" representation.
        final FieldSpacecraftState<Gradient> state = convert(orbit);

        // Initialize array containing Jacobian
        final double[][] jacobian = new double[3][6];

        // Loop on force models
        for (final ForceModel model : forceModels) {

            // Compute acceleration and extract derivatives
            final FieldVector3D<Gradient> acceleration = model.acceleration(state, model.getParameters(state.getA().getField()));
            final double[] derivativesX = acceleration.getX().getGradient();
            final double[] derivativesY = acceleration.getY().getGradient();
            final double[] derivativesZ = acceleration.getZ().getGradient();

            // Fill Jacobian
            addToRow(derivativesX, 0, jacobian);
            addToRow(derivativesY, 1, jacobian);
            addToRow(derivativesZ, 2, jacobian);

        }

        // Return the matrix
        return MatrixUtils.createRealMatrix(jacobian);

    }

    /**
     * Fill Jacobians rows.
     * @param derivatives derivatives of a component of acceleration
     * @param index component index (0 for x, 1 for y, 2 for z, etc.)
     * @param matrix Jacobian of acceleration with respect to spacecraft position/velocity
     */
    private static void addToRow(final double[] derivatives, final int index,
                                 final double[][] matrix) {
        for (int i = 0; i < 6; i++) {
            matrix[index][i] += derivatives[i];
        }
    }

    /**
     * Convert an orbit to a "Field" spacecraft state.
     * <p>
     * "Field" objects are used to allow automatic differentiation computing
     * partial derivatives. Parameters used as derivatives are contained in
     * the "Gradient definition".
     * </p>
     * @param orbit input orbit
     * @return a "Fiel" spacecraft state
     */
    private static FieldSpacecraftState<Gradient> convert(final Orbit orbit) {

        // Convert to cartesian if needed
        final CartesianOrbit car = (CartesianOrbit) OrbitType.CARTESIAN.convertType(orbit);

        // Gradient definition (parameters used for derivatives)
        final int freeParameters = 6;
        final Gradient xG    = Gradient.variable(freeParameters, 0, car.getPVCoordinates().getPosition().getX());
        final Gradient yG    = Gradient.variable(freeParameters, 1, car.getPVCoordinates().getPosition().getY());
        final Gradient zG    = Gradient.variable(freeParameters, 2, car.getPVCoordinates().getPosition().getZ());
        final Gradient vxG   = Gradient.variable(freeParameters, 3, car.getPVCoordinates().getVelocity().getX());
        final Gradient vyG   = Gradient.variable(freeParameters, 4, car.getPVCoordinates().getVelocity().getY());
        final Gradient vzG   = Gradient.variable(freeParameters, 5, car.getPVCoordinates().getVelocity().getZ());

        // Sometimes we will need the field of the Gradient to build new instances
        final Field<Gradient> field = xG.getField();

        // Initializing the FieldAbsoluteDate using the epoch definition
        final FieldAbsoluteDate<Gradient> date = new FieldAbsoluteDate<>(field, orbit.getDate());

        // Initialize the "Field" orbit
        final Gradient mu = Gradient.constant(freeParameters, car.getMu());
        final FieldCartesianOrbit<Gradient> CO = new FieldCartesianOrbit<Gradient>(new TimeStampedFieldPVCoordinates<>(date,
                                                                                                                       new FieldPVCoordinates<>(new FieldVector3D<>(xG, yG, zG),
                                                                                                                                                new FieldVector3D<>(vxG, vyG, vzG))),
                                                                                   orbit.getFrame(),
                                                                                   mu);

        // Return
        return new FieldSpacecraftState<>(CO);

    }

    /**
     * Print a matrix.
     * @param matrix matrix to print
     */
    private static void printMatrix(final RealMatrix matrix) {

        // Create a string builder
        final StringBuilder covToPrint = new StringBuilder();
        for (int row = 0; row < matrix.getRowDimension(); row++) {
            for (int column = 0; column < matrix.getColumnDimension(); column++) {
                covToPrint.append(String.format(Locale.US, "%16.9e", matrix.getEntry(row, column)));
                covToPrint.append(" ");
            }
            covToPrint.append("\n");
        }

        // Print
        System.out.println(covToPrint);

    }

}
