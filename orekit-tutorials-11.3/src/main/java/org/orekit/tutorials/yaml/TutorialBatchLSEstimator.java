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
 * Initial data for the batch least squares orbit determination estimator.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialBatchLSEstimator {

    /** Scaling factor for orbital parameters normalization (m). */
    private double orbitalParametersPositionScale;

    /** Maximum number of iterations before an exception is thrown. */
    private int maxIterations;

    /** Maximum number of evaluations before an exception is thrown. */
    private int maxEvaluations;

    /** Convergence threshold. */
    private double convergenceThreshold;

    /** Optimization engine used by the estimator. */
    private TutorialOptimizationEngine optimizationEngine;

    /**
     * Get the scaling factor for orbital parameters normalization.
     * @return the scaling factor for orbital parameters normalization (m)
     */
    public double getOrbitalParametersPositionScale() {
        return orbitalParametersPositionScale;
    }

    /**
     * Set the scaling factor for orbital parameters normalization.
     * @param orbitalParametersPositionScale scaling factor for orbital parameters normalization (m)
     */
    public void setOrbitalParametersPositionScale(final double orbitalParametersPositionScale) {
        this.orbitalParametersPositionScale = orbitalParametersPositionScale;
    }

    /**
     * Get the maximum number of iterations before an exception is thrown.
     * @return the maximum number of iterations before an exception is thrown
     */
    public int getMaxIterations() {
        return maxIterations;
    }

    /**
     * Set the maximum number of iterations before an exception is thrown.
     * @param maxIterations maximum number of iterations before an exception is thrown
     */
    public void setMaxIterations(final int maxIterations) {
        this.maxIterations = maxIterations;
    }

    /**
     * Get the maximum number of evaluations before an exception is thrown.
     * @return the maximum number of iterations before an exception is thrown
     */
    public int getMaxEvaluations() {
        return maxEvaluations;
    }

    /**
     * Set the maximum number of evaluations before an exception is thrown.
     * @param maxEvaluations maximum number of iterations before an exception is thrown
     */
    public void setMaxEvaluations(final int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;
    }

    /**
     * Get the convergence threshold.
     * @return the convergence threshold
     */
    public double getConvergenceThreshold() {
        return convergenceThreshold;
    }

    /**
     * Set the convergence threshold.
     * @param convergenceThreshold convergence threshold
     */
    public void setConvergenceThreshold(final double convergenceThreshold) {
        this.convergenceThreshold = convergenceThreshold;
    }

    /**
     * Get the optimization engine.
     * @return the optimization engine
     */
    public TutorialOptimizationEngine getOptimizationEngine() {
        return optimizationEngine;
    }

    /**
     * Set the optimization engine.
     * @param optimitationEngine optimization engine
     */
    public void setOptimizationEngine(final TutorialOptimizationEngine optimitationEngine) {
        this.optimizationEngine = optimitationEngine;
    }

}
