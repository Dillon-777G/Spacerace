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
package org.orekit.tutorials.yaml;

/**
 * Initial data to initialize the spacecraft state.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialSpacecraft {

    /** Spacecraft mass (kg). */
    private double mass;

    /** Satellite name. */
    private String name;

    /** Satellite id. */
    private String id;

    /** Name of the attitude mode. */
    private String attitudeMode;

    /** Coordinates of the on-board antenna phase center in spacecraft frame (m). */
    private double[] antennaOffset;

    /** Estimated On-board clock offset. */
    private TutorialEstimatedParameter clockOffset;

    /** Estimated On-board range bias. */
    private TutorialEstimatedParameter bias;

    /**
     * Constructor.
     */
    public TutorialSpacecraft() {
        // initialise empty array
        antennaOffset = new double[0];
    }

    /**
     * Get the spacecraft mass.
     * @return the spacecraft mass (kg)
     */
    public double getMass() {
        return mass;
    }

    /**
     * Set the spacecraft mass.
     * @param mass spacecraft mass(kg)
     */
    public void setMass(final double mass) {
        this.mass = mass;
    }

    /**
     * Get the satellite name.
     * @return the satellite name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the satellite name.
     * @param name satellite name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Get the satellite ID.
     * @return the satellite ID
     */
    public String getId() {
        return id;
    }

    /**
     * Set the satellite ID.
     * @param id satellite ID
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * Get the name of the attitude mode.
     * @return the name of the attitude mode
     */
    public String getAttitudeMode() {
        return attitudeMode;
    }

    /**
     * Set the name of the attitude mode.
     * @param attitudeMode name of the attitude mode
     */
    public void setAttitudeMode(final String attitudeMode) {
        this.attitudeMode = attitudeMode;
    }

    /**
     * Get the coordinates of the on-board antenna phase center in spacecraft frame.
     * @return the coordinates of the on-board antenna phase center in spacecraft frame [X, Y, Z].
     */
    public double[] getAntennaOffset() {
        return antennaOffset.clone();
    }

    /**
     * Set the coordinates of the on-board antenna phase center in spacecraft frame.
     * @param antennaOffset coordinates of the on-board antenna phase center in spacecraft frame [X, Y, Z]
     */
    public void setAntennaOffset(final double[] antennaOffset) {
        this.antennaOffset = antennaOffset.clone();
    }

    /**
     * Get the estimated On-board clock offset.
     * @return the estimated On-board clock offset
     */
    public TutorialEstimatedParameter getClockOffset() {
        return clockOffset;
    }

    /**
     * Set the estimated On-board clock offset.
     * @param clockOffset estimated On-board clock offset
     */
    public void setClockOffset(final TutorialEstimatedParameter clockOffset) {
        this.clockOffset = clockOffset;
    }

    /**
     * Get the estimated On-board range bias.
     * @return the estimated On-board range bias
     */
    public TutorialEstimatedParameter getBias() {
        return bias;
    }

    /**
     * Set the estimated On-board range bias.
     * @param bias estimated On-board range bias
     */
    public void setBias(final TutorialEstimatedParameter bias) {
        this.bias = bias;
    }

}
