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
package org.orekit.tutorials.estimation.common;

import org.hipparchus.exception.DummyLocalizable;
import org.hipparchus.exception.Localizable;
import org.orekit.tutorials.yaml.TutorialBatchLSEstimator;
import org.orekit.tutorials.yaml.TutorialBody;
import org.orekit.tutorials.yaml.TutorialKalman;
import org.orekit.tutorials.yaml.TutorialMeasurements;
import org.orekit.tutorials.yaml.TutorialOrbit;
import org.orekit.tutorials.yaml.TutorialPropagator;
import org.orekit.tutorials.yaml.TutorialSpacecraft;

/**
 * Initial data to initialize an orbit determination tutorial.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialOrbitDetermination {

    /** Error message for unknown frame. */
    public static final Localizable UNKNOWN_FRAME =
        new DummyLocalizable("unknown frame {0}");

    /** Error message for not Earth frame. */
    public static final Localizable NOT_EARTH_FRAME =
        new DummyLocalizable("frame {0} is not an Earth frame");

    /** Data used to initialize the batch least square estimator. */
    private TutorialBatchLSEstimator estimator;

    /** Data used to initialize the Kalman filter. */
    private TutorialKalman kalman;

    /** Data used to initialize the central body. */
    private TutorialBody body;

    /** Data used to initialize the initial orbit. */
    private TutorialOrbit orbit;

    /** Data used to initialize the spacecraft (mass, antenna offset, clock offset and attitude mode). */
    private TutorialSpacecraft spacecraft;

    /** Data used to initialize the orbit propagator (integrator and force models). */
    private TutorialPropagator propagator;

    /** Data used to initialize the measurements (troposphere, ionosphere, stations, etc.). */
    private TutorialMeasurements measurements;

    /** Base name of the output files (log and residuals), no files created if empty. */
    private String outputBaseName;

    /** Flag for the OPM generation. */
    private boolean isOpmGenerated;

    /**
     * Get the batch least square estimator data.
     * @return the estimator data
     */
    public TutorialBatchLSEstimator getEstimator() {
        return estimator;
    }

    /**
     * Set the batch least square estimator data.
     * @param estimator estimator data
     */
    public void setEstimator(final TutorialBatchLSEstimator estimator) {
        this.estimator = estimator;
    }

    /**
     * Get the kalman filter data.
     * @return the kalman filter data
     */
    public TutorialKalman getKalman() {
        return kalman;
    }

    /**
     * Set the kalman filter data.
     * @param kalman kalman filter data
     */
    public void setKalman(final TutorialKalman kalman) {
        this.kalman = kalman;
    }

    /**
     * Get the body data.
     * @return the body data.
     */
    public TutorialBody getBody() {
        return body;
    }

    /**
     * Set the body data.
     * @param body body data
     */
    public void setBody(final TutorialBody body) {
        this.body = body;
    }

    /**
     * Get the orbit data.
     * @return the orbit data
     */
    public TutorialOrbit getOrbit() {
        return orbit;
    }

    /**
     * Set the orbit data.
     * @param orbit orbit data
     */
    public void setOrbit(final TutorialOrbit orbit) {
        this.orbit = orbit;
    }

    /**
     * Get the spacecraft data.
     * @return the spacecraft data
     */
    public TutorialSpacecraft getSpacecraft() {
        return spacecraft;
    }

    /**
     * Set the spacecraft data.
     * @param spacecraft spacecraft data
     */
    public void setSpacecraft(final TutorialSpacecraft spacecraft) {
        this.spacecraft = spacecraft;
    }

    /**
     * Get the propagator data.
     * @return the propagator data
     */
    public TutorialPropagator getPropagator() {
        return propagator;
    }

    /**
     * Set the propagator data.
     * @param propagator propagator data
     */
    public void setPropagator(final TutorialPropagator propagator) {
        this.propagator = propagator;
    }

    /**
     * Get the measurements data.
     * @return the measurements data
     */
    public TutorialMeasurements getMeasurements() {
        return measurements;
    }

    /**
     * Set the measurement data.
     * @param measurements measurement data
     */
    public void setMeasurements(final TutorialMeasurements measurements) {
        this.measurements = measurements;
    }

    /**
     * Get the base name of the output files (log and residuals).
     * @return the base name of the output files (log and residuals)
     */
    public String getOutputBaseName() {
        return outputBaseName;
    }

    /**
     * Set the base name of the output files (log and residuals).
     * @param outputBaseName base name of the output files (log and residuals)
     */
    public void setOutputBaseName(final String outputBaseName) {
        this.outputBaseName = outputBaseName;
    }

    /**
     * Get the flag for the estimated OPM generation.
     * @return true if the estimated OPM is generated
     */
    public boolean isOpmGenerated() {
        return isOpmGenerated;
    }

    /**
     * Set the flag for the estimated OPM generation.
     * @param generated true if the estimated OPM is generated
     */
    public void setIsOpmGenerated(final boolean generated) {
        this.isOpmGenerated = generated;
    }

}
