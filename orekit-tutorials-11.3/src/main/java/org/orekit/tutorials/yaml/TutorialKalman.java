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
 * Initial data for the kalman filter estimator.
 * <p>
 * Data are read from a YAML file.
 * </p> <p>
 * Data contain the needed elements to initialize the
 * Kalman filter (i.e. initial covariance and process noise
 * matrices for the cartesian, propagation and measurement
 * parameters).
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialKalman {

    /** Initial diagonal elements of the cartesian covariance matrix. */
    private double[] cartesianOrbitalP;

    /** Initial diagonal elements of the cartesian process noise matrix. */
    private double[] cartesianOrbitalQ;

    /** Initial diagonal elements of the propagation covariance matrix. */
    private double[] propagationP;

    /** Initial diagonal elements of the propagation process noise matrix. */
    private double[] propagationQ;

    /** Initial diagonal elements of the measurement covariance matrix. */
    private double[] measurementP;

    /** Initial diagonal elements of the measurement process noise matrix. */
    private double[] measurementQ;

    /**
     * Constructor.
     */
    public TutorialKalman() {
        // initialise empty array
        this.cartesianOrbitalP = new double[0];
        this.cartesianOrbitalQ = new double[0];
        this.propagationP      = new double[0];
        this.propagationQ      = new double[0];
        this.measurementP      = new double[0];
        this.measurementQ      = new double[0];
    }

    /**
     * Get the initial diagonal elements of the cartesian covariance matrix.
     * @return the initial diagonal elements of the cartesian covariance matrix
     */
    public double[] getCartesianOrbitalP() {
        return cartesianOrbitalP.clone();
    }

    /**
     * Set the initial diagonal elements of the cartesian covariance matrix.
     * @param cartesianOrbitalP initial diagonal elements of the cartesian covariance matrix
     */
    public void setCartesianOrbitalP(final double[] cartesianOrbitalP) {
        this.cartesianOrbitalP = cartesianOrbitalP.clone();
    }

    /**
     * Get the initial diagonal elements of the cartesian process noise matrix.
     * @return the initial diagonal elements of the cartesian process noise matrix
     */
    public double[] getCartesianOrbitalQ() {
        return cartesianOrbitalQ.clone();
    }

    /**
     * Set the initial diagonal elements of the cartesian process noise matrix.
     * @param cartesianOrbitalQ initial diagonal elements of the cartesian process noise matrix
     */
    public void setCartesianOrbitalQ(final double[] cartesianOrbitalQ) {
        this.cartesianOrbitalQ = cartesianOrbitalQ.clone();
    }

    /**
     * Get the initial diagonal elements of the propagation covariance matrix.
     * @return the initial diagonal elements of the propagation covariance matrix
     */
    public double[] getPropagationP() {
        return propagationP.clone();
    }

    /**
     * Set the initial diagonal elements of the propagation covariance matrix.
     * @param propagationP initial diagonal elements of the propagation covariance matrix
     */
    public void setPropagationP(final double[] propagationP) {
        this.propagationP = propagationP.clone();
    }

    /**
     * Get the initial diagonal elements of the propagation process noise matrix.
     * @return the initial diagonal elements of the propagation process noise matrix
     */
    public double[] getPropagationQ() {
        return propagationQ.clone();
    }

    /**
     * Set the initial diagonal elements of the propagation process noise matrix.
     * @param propagationQ initial diagonal elements of the propagation process noise matrix
     */
    public void setPropagationQ(final double[] propagationQ) {
        this.propagationQ = propagationQ.clone();
    }

    /**
     * Get the initial diagonal elements of the measurement covariance matrix.
     * @return the initial diagonal elements of the measurement covariance matrix
     */
    public double[] getMeasurementP() {
        return measurementP.clone();
    }

    /**
     * Set the initial diagonal elements of the measurement covariance matrix.
     * @param measurementP initial diagonal elements of the measurement covariance matrix
     */
    public void setMeasurementP(final double[] measurementP) {
        this.measurementP = measurementP.clone();
    }

    /**
     * Get the initial diagonal elements of the measurement process noise matrix.
     * @return the initial diagonal elements of the measurement process noise matrix
     */
    public double[] getMeasurementQ() {
        return measurementQ.clone();
    }

    /**
     * Set the initial diagonal elements of the measurement process noise matrix.
     * @param measurementQ initial diagonal elements of the measurement process noise matrix
     */
    public void setMeasurementQ(final double[] measurementQ) {
        this.measurementQ = measurementQ.clone();
    }

}
