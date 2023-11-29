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
import java.util.List;
import java.util.NoSuchElementException;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.forces.ForceModel;
import org.orekit.forces.drag.DragForce;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.OceanTides;
import org.orekit.forces.gravity.Relativity;
import org.orekit.forces.gravity.SolidTides;
import org.orekit.forces.gravity.ThirdBodyAttraction;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.forces.radiation.RadiationSensitive;
import org.orekit.forces.radiation.SolarRadiationPressure;
import org.orekit.models.earth.atmosphere.Atmosphere;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.conversion.NumericalPropagatorBuilder;
import org.orekit.propagation.conversion.ODEIntegratorBuilder;
import org.orekit.time.TimeScalesFactory;
import org.orekit.tutorials.estimation.common.AbstractOrbitDeterminationEngine;
import org.orekit.tutorials.estimation.common.TutorialOrbitDetermination;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialGravity;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.ParameterDriver;

/**
 * Orbit determination engine for performance testing.
 * @author Bryan Cazabonne
 */
public class OrbitDeterminationEngine extends AbstractOrbitDeterminationEngine<NumericalPropagatorBuilder> {

    /** Input file. */
    private File input;

    /** Gravity field. */
    private NormalizedSphericalHarmonicsProvider gravityField;

    /** Flag for orbit determination engine. */
    private final boolean isKalman;

    /**
     * Constructor.
     * @param isKalman true is orbit determination is performed using a Kalman Filter
     */
    public OrbitDeterminationEngine(final boolean isKalman) {
        this.isKalman = isKalman;
    }

    /**
     * Run the orbit determination.
     * @param inputFile in orbit determination file
     * @throws IOException if file cannot be read properly
     */
    public void runOrbitDetermination(final File inputFile) throws IOException {
        this.input = inputFile;
        if (isKalman) {
            // perform orbit determination with a Kalman Filter
            super.runKalman(inputFile);
        } else {
            // perform orbit determination with a batch least squares algorithm
            super.run(inputFile);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void createGravityField(final TutorialOrbitDetermination inputData)
        throws NoSuchElementException {
        final TutorialGravity gravity = inputData.getPropagator().getForceModels().getGravity();
        final int degree = gravity.getDegree();
        final int order  = FastMath.min(degree, gravity.getOrder());
        gravityField = GravityFieldFactory.getNormalizedProvider(degree, order);
    }

    /** {@inheritDoc} */
    @Override
    protected double getMu() {
        return gravityField.getMu();
    }

    /** {@inheritDoc} */
    @Override
    protected NumericalPropagatorBuilder createPropagatorBuilder(final Orbit referenceOrbit,
                                                                 final ODEIntegratorBuilder builder,
                                                                 final double positionScale) {
        return new NumericalPropagatorBuilder(referenceOrbit, builder, PositionAngle.MEAN,
                                              positionScale);
    }

    /** {@inheritDoc} */
    @Override
    protected void setMass(final NumericalPropagatorBuilder propagatorBuilder,
                           final double mass) {
        propagatorBuilder.setMass(mass);
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setGravity(final NumericalPropagatorBuilder propagatorBuilder,
                                               final OneAxisEllipsoid body) {
        final ForceModel gravityModel = new HolmesFeatherstoneAttractionModel(body.getBodyFrame(), gravityField);
        propagatorBuilder.addForceModel(gravityModel);
        return gravityModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setOceanTides(final NumericalPropagatorBuilder propagatorBuilder,
                                                  final IERSConventions conventions, final OneAxisEllipsoid body,
                                                  final int degree, final int order) {
        final ForceModel tidesModel = new OceanTides(body.getBodyFrame(),
                                                     gravityField.getAe(), gravityField.getMu(),
                                                     degree, order, conventions,
                                                     TimeScalesFactory.getUT1(conventions, true));
        propagatorBuilder.addForceModel(tidesModel);
        return tidesModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setSolidTides(final NumericalPropagatorBuilder propagatorBuilder,
                                                  final IERSConventions conventions, final  OneAxisEllipsoid body,
                                                  final CelestialBody[] solidTidesBodies) {
        final ForceModel tidesModel = new SolidTides(body.getBodyFrame(),
                                                     gravityField.getAe(), gravityField.getMu(),
                                                     gravityField.getTideSystem(), conventions,
                                                     TimeScalesFactory.getUT1(conventions, true),
                                                     solidTidesBodies);
        propagatorBuilder.addForceModel(tidesModel);
        return tidesModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setThirdBody(final NumericalPropagatorBuilder propagatorBuilder,
                                                 final CelestialBody thirdBody) {
        final ForceModel thirdBodyModel = new ThirdBodyAttraction(thirdBody);
        propagatorBuilder.addForceModel(thirdBodyModel);
        return thirdBodyModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setDrag(final NumericalPropagatorBuilder propagatorBuilder,
                                            final Atmosphere atmosphere, final DragSensitive spacecraft) {
        final ForceModel dragModel = new DragForce(atmosphere, spacecraft);
        propagatorBuilder.addForceModel(dragModel);
        return dragModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setSolarRadiationPressure(final NumericalPropagatorBuilder propagatorBuilder,
                                                              final CelestialBody sun, final double equatorialRadius,
                                                              final RadiationSensitive spacecraft) {
        final ForceModel srpModel = new SolarRadiationPressure(sun, equatorialRadius, spacecraft);
        propagatorBuilder.addForceModel(srpModel);
        return srpModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setRelativity(final NumericalPropagatorBuilder propagatorBuilder) {
        final ForceModel relativityModel = new Relativity(gravityField.getMu());
        propagatorBuilder.addForceModel(relativityModel);
        return relativityModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setPolynomialAcceleration(final NumericalPropagatorBuilder propagatorBuilder,
                                                              final String name, final Vector3D direction,
                                                              final int degree) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Polynomial acceleration not implemented for GNSS orbit determination");
    }

    /** {@inheritDoc} */
    @Override
    protected void setAttitudeProvider(final NumericalPropagatorBuilder propagatorBuilder,
                                       final AttitudeProvider attitudeProvider) {
        propagatorBuilder.setAttitudeProvider(attitudeProvider);
    }

    /** {@inheritDoc}.
     * @throws IOException if input file cannot be read
     */
    @Override
    protected void compareWithReference(final Orbit estimatedOrbit) throws IOException {
        // Reference results
        final Vector3D refPos0 = new Vector3D(-2747606.680868164, 22572091.30648564, 13522761.402325712);
        final Vector3D refVel0 = new Vector3D(-2729.5151218788005, 1142.6629459030657, -2523.9055974487947);

        // Check if orbit determination is performed with a Kalman Filter
        Vector3D refPos = refPos0;
        Vector3D refVel = refVel0;
        if (isKalman) {
            // Run the reference until Kalman last date
            final Orbit refOrbit = runReference(input, refPos0, refVel0, null,
                                                estimatedOrbit.getDate());
            refPos = refOrbit.getPVCoordinates().getPosition();
            refVel = refOrbit.getPVCoordinates().getVelocity();
        }

        // Print results
        System.out.println("Comparison with reference orbit: ");
        System.out.println("ΔP(m) = " + Vector3D.distance(refPos, estimatedOrbit.getPVCoordinates().getPosition()));
        System.out.println("ΔV(m/s) = " + Vector3D.distance(refVel, estimatedOrbit.getPVCoordinates().getVelocity()));
    }

}
