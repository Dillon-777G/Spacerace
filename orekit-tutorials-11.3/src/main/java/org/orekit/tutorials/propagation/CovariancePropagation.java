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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Locale;

import org.hipparchus.linear.RealMatrix;
import org.hipparchus.ode.ODEIntegrator;
import org.hipparchus.ode.nonstiff.DormandPrince853Integrator;
import org.orekit.data.DataContext;
import org.orekit.data.DataProvidersManager;
import org.orekit.data.DirectoryCrawler;
import org.orekit.files.ccsds.definitions.CenterName;
import org.orekit.files.ccsds.ndm.WriterBuilder;
import org.orekit.files.ccsds.ndm.odm.CartesianCovariance;
import org.orekit.files.ccsds.ndm.odm.opm.Opm;
import org.orekit.files.ccsds.ndm.odm.opm.OpmData;
import org.orekit.forces.ForceModel;
import org.orekit.forces.gravity.HolmesFeatherstoneAttractionModel;
import org.orekit.forces.gravity.potential.GravityFieldFactory;
import org.orekit.forces.gravity.potential.NormalizedSphericalHarmonicsProvider;
import org.orekit.frames.Frame;
import org.orekit.frames.FramesFactory;
import org.orekit.orbits.OrbitType;
import org.orekit.orbits.PositionAngle;
import org.orekit.propagation.MatricesHarvester;
import org.orekit.propagation.Propagator;
import org.orekit.propagation.SpacecraftState;
import org.orekit.propagation.StateCovariance;
import org.orekit.propagation.StateCovarianceMatrixProvider;
import org.orekit.propagation.numerical.NumericalPropagator;
import org.orekit.time.AbsoluteDate;
import org.orekit.tutorials.utils.NdmUtils;
import org.orekit.utils.Constants;
import org.orekit.utils.IERSConventions;

/**
 * Orekit tutorial for covariance matrix propagation.
 * <p>
 * This tutorial uses the Orekit's additional state provider for covariance
 * propagation. SIt presents how accessing a covariance matrix from the provider
 * and an input spacecraft state. It also present how converting the covariance
 * frame and orbit type.
 * </p>
 * @author Bryan Cazabonne
 */
public class CovariancePropagation {

    /** String for "Initial cartesian covariance matrix at ". */
    private static final String INITIAL_CART_COVARIANCE = "Initial cartesian covariance matrix at ";

    /** String for "Propagated cartesian covariance matrix at ". */
    private static final String PROPAGATED_CART_COVARIANCE = "Propagated cartesian covariance matrix at ";

    /** String for "Propagated keplerian covariance matrix at ". */
    private static final String PROPAGATED_KEP_COVARIANCE = "Propagated keplerian covariance matrix at ";

    /** String for " expressed in ". */
    private static final String EXPRESSED_IN = " expressed in ";

    /** String for " frame". */
    private static final String FRAME = " frame";

    /** f16.9 floating value. */
    private static final String E16_9 = "%16.9e";

    /** Private constructor for utility class. */
    private CovariancePropagation() {
        // empty
    }

    /**
     * Program entry point.
     * @param args program arguments (unused here)
     */
    public static void main(final String[] args) {

        try {

            // Configure orekit-data
            final File home = configureOrekit();

             // Run the program
            new CovariancePropagation().run(home);

        } catch (IOException | URISyntaxException e) {
            // Nothing to do
        }

    }

    /**
     * Run the program.
     * @param home home directory
     */
    private void run(final File home) throws URISyntaxException, IOException {

        // Read the input OPM to extract input state vector and covariance
        final String fileName  = "estimated.xml";
        final String inputPath = CovariancePropagation.class.getClassLoader().getResource("covariance/" + fileName).toURI().getPath();
        final File   inputFile = new File(inputPath);
        final Opm    opm       = NdmUtils.parseOPM(Constants.WGS84_EARTH_MU, Propagator.DEFAULT_MASS, inputFile);

        // Build the initial conditions
        // ------

        // Initial state
        final SpacecraftState stateInit = new SpacecraftState(opm.generateCartesianOrbit());

        // CCSDS-like Cartesian covariance matrix
        final CartesianCovariance ccsdsCovariance =  opm.getData().getCovarianceBlock();

        // Covariance matrix content
        final RealMatrix   covInitMatrix = ccsdsCovariance.getCovarianceMatrix();

        // Covariance date
        final AbsoluteDate covDate = ccsdsCovariance.getEpoch();

        // Covariance frame (here we know from the file it is an inertial frame and not a LOF frame)
        final Frame covFrame = ccsdsCovariance.getReferenceFrame().asFrame();

        // Build an Orekit StateCovariance object (PositionAngle type can be anything here)
        final StateCovariance covInit = new StateCovariance(covInitMatrix, covDate, covFrame, OrbitType.CARTESIAN, PositionAngle.MEAN);
        System.out.println(INITIAL_CART_COVARIANCE + covDate.toString() + EXPRESSED_IN + covFrame.getName() + FRAME);
        printCovariance(covInitMatrix);

        // Create the numerical propagator
        final NumericalPropagator propagator = buildPropagator(stateInit);

        // Set up computation of State Transition Matrix and Jacobians matrix with respect to parameters
        final String stmAdditionalName = "stm";
        final MatricesHarvester harvester = propagator.setupMatricesComputation(stmAdditionalName, null, null);

        // Set up covariance matrix provider and add it to the propagator
        final StateCovarianceMatrixProvider provider = new StateCovarianceMatrixProvider("covariance", stmAdditionalName, harvester,
                                                                                         propagator.getOrbitType(), propagator.getPositionAngleType(),
                                                                                         covInit);
        propagator.addAdditionalStateProvider(provider);

        // Perform propagation
        final AbsoluteDate targetEpoch = stateInit.getDate().shiftedBy(Constants.JULIAN_DAY);
        System.out.println("Propagate the covariance to " + targetEpoch.toString() + " ...");
        final SpacecraftState finalState = propagator.propagate(targetEpoch);

        // Write propagated state and covariance into a new OPM file
        final RealMatrix covProp = provider.getStateCovariance(finalState).getMatrix();
        System.out.println("Write propagated OPM");
        System.out.println("");
        final WriterBuilder ndmWriterBuilder = new WriterBuilder().withConventions(IERSConventions.IERS_2010);
        writeFile(home, opm, ndmWriterBuilder, finalState, covProp);

        // Print the propagated covariance matrix in cartesian elements and propagation frame
        System.out.println(PROPAGATED_CART_COVARIANCE + targetEpoch.toString() + EXPRESSED_IN + finalState.getFrame().getName() + FRAME);
        printCovariance(covProp);

        // Print the propagated covariance matrix in cartesian elements and TOD frame
        final RealMatrix covPropGcrf = provider.getStateCovariance(finalState, FramesFactory.getTOD(false)).getMatrix();
        System.out.println(PROPAGATED_CART_COVARIANCE + targetEpoch.toString() + EXPRESSED_IN + "TOD" + FRAME);
        printCovariance(covPropGcrf);

        // Print the propagated covariance matrix in keplerian elements and propagation frame
        final RealMatrix covPropKep = provider.getStateCovariance(finalState, OrbitType.KEPLERIAN, PositionAngle.MEAN).getMatrix();
        System.out.println(PROPAGATED_KEP_COVARIANCE + targetEpoch.toString() + EXPRESSED_IN + finalState.getFrame().getName() + FRAME);
        printCovariance(covPropKep);
    }

    /**
     * Write the output OPM file.
     * @param home home directory
     * @param initial input OPM (used for metadata)
     * @param ndmWriterBuilder builder for NDM files
     * @param propagated propagated spacecraft state
     * @param covProp propagated covariance
     */
    private void writeFile(final File home, final Opm initial, final WriterBuilder ndmWriterBuilder,
                           final SpacecraftState propagated, final RealMatrix covProp) {

        // Covariance data
        final CartesianCovariance covariance = NdmUtils.createCartesianCovariance(propagated.getDate(),
                                                                                  propagated.getFrame(),
                                                                                  covProp);

        // Create the data
        final OpmData data = new OpmData(NdmUtils.createStateVector(propagated.getPVCoordinates()),
                                         NdmUtils.createKeplerianElements(propagated.getOrbit()),
                                         null,
                                         covariance,
                                         new ArrayList<>(), // Empty list of maneuvers
                                         null,
                                         propagated.getMass());

        // Opm
        final Opm opm = new Opm(NdmUtils.createHeader(),
                                NdmUtils.createOpmSegments(NdmUtils.createCommonMetadata(CenterName.EARTH,
                                                                                         initial.getMetadata().getObjectName(),
                                                                                         initial.getMetadata().getObjectID(),
                                                                                         propagated.getFrame()),
                                                           data),
                                IERSConventions.IERS_2010, DataContext.getDefault(), Constants.WGS84_EARTH_MU);

        // Write the file
        NdmUtils.writeOPM(ndmWriterBuilder.buildOpmWriter(), opm, new File(home, "propagatedOpm.xml"));

    }

    /**
     * Build a numerical propagator.
     * <p>
     * Only J2 effect is considered in the dynamical model
     * </p>
     * @param stateInit input spacecraft state
     * @return a configured numerical propagator
     */
    private NumericalPropagator buildPropagator(final SpacecraftState stateInit) {

        // Integrator
        final double    minStep           = 0.001;
        final double    maxStep           = 1000.0;
        final double    positionTolerance = 10.0;
        final OrbitType orbitType         = OrbitType.CARTESIAN;
        final double[][] tol = NumericalPropagator.tolerances(positionTolerance, stateInit.getOrbit(), orbitType);
        final ODEIntegrator integrator = new DormandPrince853Integrator(minStep, maxStep, tol[0], tol[1]);

        // Propagator
        final NumericalPropagator propagator = new NumericalPropagator(integrator);
        propagator.setInitialState(stateInit);
        propagator.setOrbitType(orbitType);

        // Add a force model
        final NormalizedSphericalHarmonicsProvider provider = GravityFieldFactory.getNormalizedProvider(2, 0);
        final ForceModel holmesFeatherstone = new HolmesFeatherstoneAttractionModel(FramesFactory.getITRF(IERSConventions.IERS_2010, true), provider);
        propagator.addForceModel(holmesFeatherstone);

        // Return
        return propagator;

    }

    /**
     * Print a covariance matrix.
     * @param covariance covariance matrix to print
     */
    private void printCovariance(final RealMatrix covariance) {

        // Create a string builder
        final StringBuilder covToPrint = new StringBuilder();
        for (int row = 0; row < covariance.getRowDimension(); row++) {
            for (int column = 0; column < covariance.getColumnDimension(); column++) {
                covToPrint.append(String.format(Locale.US, E16_9, covariance.getEntry(row, column)));
                covToPrint.append(" ");
            }
            covToPrint.append("\n");
        }

        // Print
        System.out.println(covToPrint);

    }

    /**
     * Configure orekit-data.
     * @return home directory
     */
    private static File configureOrekit() {
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
        return home;
    }

}
