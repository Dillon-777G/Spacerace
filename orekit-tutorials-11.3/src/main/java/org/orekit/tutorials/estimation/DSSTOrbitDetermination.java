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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.util.FastMath;
import org.orekit.attitudes.AttitudeProvider;
import org.orekit.bodies.CelestialBody;
import org.orekit.bodies.OneAxisEllipsoid;
import org.orekit.errors.OrekitException;
import org.orekit.forces.drag.DragSensitive;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.UnnormalizedSphericalHarmonicsProvider;
import org.orekit.forces.radiation.RadiationSensitive;
import org.orekit.models.earth.atmosphere.Atmosphere;
import org.orekit.orbits.EquinoctialOrbit;
import org.orekit.orbits.Orbit;
import org.orekit.orbits.OrbitType;
import org.orekit.propagation.PropagationType;
import org.orekit.propagation.conversion.DSSTPropagatorBuilder;
import org.orekit.propagation.conversion.ODEIntegratorBuilder;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTAtmosphericDrag;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTForceModel;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTSolarRadiationPressure;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTTesseral;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTThirdBody;
import org.orekit.propagation.semianalytical.dsst.forces.DSSTZonal;
import org.orekit.time.AbsoluteDate;
import org.orekit.tutorials.estimation.common.AbstractOrbitDetermination;
import org.orekit.tutorials.estimation.common.TutorialOrbitDetermination;
import org.orekit.tutorials.yaml.TutorialForceModel.TutorialGravity;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;
import org.orekit.utils.ParameterDriver;

/** Orekit tutorial for orbit determination using DSST theory.
 * <p>
 * The tutorial is very close to the one for the orbit determination
 * using a numerical propagator.
 * </p> <p>
 * The tutorial performs orbit determination considering only
 * mean elements.
 * </p>
 * @author Luc Maisonobe
 * @author Bryan Cazabonne
 */
public class DSSTOrbitDetermination extends AbstractOrbitDetermination<DSSTPropagatorBuilder> {

    /** Gravity field. */
    private UnnormalizedSphericalHarmonicsProvider gravityField;

    /** Program entry point.
     * @param args program arguments (unused here)
     */
    public static void main(final String[] args) {
        try {

            // input in tutorial resources directory
            final String inputPath = DSSTOrbitDetermination.class.getClassLoader().
                                     getResource("dsst-od/dsst-orbit-determination.yaml").toURI().getPath();
            new DSSTOrbitDetermination().run(new File(inputPath));

        } catch (URISyntaxException | IOException | OrekitException e) {
            System.err.println(e.getLocalizedMessage());
            System.exit(1);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void createGravityField(final TutorialOrbitDetermination inputData)
        throws NoSuchElementException {
        final TutorialGravity gravity = inputData.getPropagator().getForceModels().getGravity();
        final int degree = gravity.getDegree();
        final int order  = FastMath.min(degree, gravity.getOrder());
        gravityField = GravityFieldFactory.getUnnormalizedProvider(degree, order);
    }

    /** {@inheritDoc} */
    @Override
    protected double getMu() {
        return gravityField.getMu();
    }

    /** {@inheritDoc} */
    @Override
    protected DSSTPropagatorBuilder createPropagatorBuilder(final Orbit referenceOrbit,
                                                            final ODEIntegratorBuilder builder,
                                                            final double positionScale) {
        final EquinoctialOrbit equiOrbit = (EquinoctialOrbit) OrbitType.EQUINOCTIAL.convertType(referenceOrbit);
        return new DSSTPropagatorBuilder(equiOrbit, builder, positionScale,
                                         PropagationType.OSCULATING, PropagationType.OSCULATING);
    }

    /** {@inheritDoc} */
    @Override
    protected void setMass(final DSSTPropagatorBuilder propagatorBuilder,
                                final double mass) {
        propagatorBuilder.setMass(mass);
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setGravity(final DSSTPropagatorBuilder propagatorBuilder,
                                               final OneAxisEllipsoid body) {

        // tesseral terms
        final DSSTForceModel tesseral = new DSSTTesseral(body.getBodyFrame(),
                                                         Constants.WGS84_EARTH_ANGULAR_VELOCITY, gravityField);
        propagatorBuilder.addForceModel(tesseral);

        // zonal terms
        final DSSTForceModel zonal = new DSSTZonal(gravityField);
        propagatorBuilder.addForceModel(zonal);

        // gather all drivers
        final List<ParameterDriver> drivers = new ArrayList<>();
        drivers.addAll(tesseral.getParametersDrivers());
        drivers.addAll(tesseral.getParametersDrivers());
        return drivers;

    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setOceanTides(final DSSTPropagatorBuilder propagatorBuilder,
                                                  final IERSConventions conventions,
                                                  final OneAxisEllipsoid body,
                                                  final int degree, final int order) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Ocean tides not implemented in DSST");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setSolidTides(final DSSTPropagatorBuilder propagatorBuilder,
                                                  final IERSConventions conventions,
                                                  final OneAxisEllipsoid body,
                                                  final CelestialBody[] solidTidesBodies) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                  "Solid tides not implemented in DSST");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setThirdBody(final DSSTPropagatorBuilder propagatorBuilder,
                                                 final CelestialBody thirdBody) {
        final DSSTForceModel thirdBodyModel = new DSSTThirdBody(thirdBody, gravityField.getMu());
        propagatorBuilder.addForceModel(thirdBodyModel);
        return thirdBodyModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setDrag(final DSSTPropagatorBuilder propagatorBuilder,
                                            final Atmosphere atmosphere, final DragSensitive spacecraft) {
        final DSSTForceModel dragModel = new DSSTAtmosphericDrag(atmosphere, spacecraft, gravityField.getMu());
        propagatorBuilder.addForceModel(dragModel);
        return dragModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setSolarRadiationPressure(final DSSTPropagatorBuilder propagatorBuilder, final CelestialBody sun,
                                                              final double equatorialRadius, final RadiationSensitive spacecraft) {
        final DSSTForceModel srpModel = new DSSTSolarRadiationPressure(sun, equatorialRadius,
                                                                       spacecraft, gravityField.getMu());
        propagatorBuilder.addForceModel(srpModel);
        return srpModel.getParametersDrivers();
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setRelativity(final DSSTPropagatorBuilder propagatorBuilder) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Relativity not implemented in DSST");
    }

    /** {@inheritDoc} */
    @Override
    protected List<ParameterDriver> setPolynomialAcceleration(final DSSTPropagatorBuilder propagatorBuilder,
                                                              final String name, final Vector3D direction, final int degree) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                        "Polynomial acceleration not implemented in DSST");
    }

    @Override
    protected List<ParameterDriver> setManeuver(final DSSTPropagatorBuilder propagatorBuilder, final AbsoluteDate fireDate,
                                                final double duration, final double thrust, final double isp, final Vector3D direction, final String name) {
        throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                  "Maneuvers not implemented for DSSTOrbitDetermination");
    }

    /** {@inheritDoc} */
    @Override
    protected void setAttitudeProvider(final DSSTPropagatorBuilder propagatorBuilder,
                                       final AttitudeProvider attitudeProvider) {
        propagatorBuilder.setAttitudeProvider(attitudeProvider);
    }

    /** {@inheritDoc} */
    @Override
    protected void compareWithReference(final Orbit estimatedOrbit) {
        // Reference results
        final Vector3D refPos = new Vector3D(-2747606.680868164, 22572091.30648564, 13522761.402325712);
        final Vector3D refVel = new Vector3D(-2729.5151218788005, 1142.6629459030657, -2523.9055974487947);

        // Print results
        System.out.println("Comparison with reference orbit: ");
        System.out.println("ΔP(m) = " + Vector3D.distance(refPos, estimatedOrbit.getPVCoordinates().getPosition()));
        System.out.println("ΔV(m/s) = " + Vector3D.distance(refVel, estimatedOrbit.getPVCoordinates().getVelocity()));
    }
}
