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
 * Optimization engine used by the estimator.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialOptimizationEngine {

    /** Levenberg-Marquardt optimizer. */
    private TutorialLevenbergMarquardt levenbergMarquardt;

    /** Gauss-Newton optimizer. */
    private TutorialGaussNewton gaussNewton;

    /**
     * Get the Levenberg-Marquardt optimization engine.
     * @return the Levenberg-Marquardt optimization engine
     */
    public TutorialLevenbergMarquardt getLevenbergMarquardt() {
        return levenbergMarquardt;
    }

    /**
     * Set the Levenberg-Marquardt optimization engine.
     * @param levenbergMarquardt Levenberg-Marquardt optimization engine
     */
    public void setLevenbergMarquardt(final TutorialLevenbergMarquardt levenbergMarquardt) {
        this.levenbergMarquardt = levenbergMarquardt;
    }

    /**
     * Get the Gauss-Newton optimization engine.
     * @return the Gauss-Newton optimization engine
     */
    public TutorialGaussNewton getGaussNewton() {
        return gaussNewton;
    }

    /**
     * Set the Gauss-Newton optimization engine.
     * @param gaussNewton Gauss-Newton optimization engine
     */
    public void setGaussNewton(final TutorialGaussNewton gaussNewton) {
        this.gaussNewton = gaussNewton;
    }

    /** Levenberg-Marquardt optimizer. */
    public static class TutorialLevenbergMarquardt {

        /** Initial step for the optimizer. */
        private double initialStep;

        /**
         * Get the optimizer's initial step.
         * @return the optimizer's initial step
         */
        public double getInitialStep() {
            return initialStep;
        }

        /**
         * Set the optimizer's initial step.
         * @param initialStep optimizer's initial step
         */
        public void setInitialStep(final double initialStep) {
            this.initialStep = initialStep;
        }

    }

    /** Gauss-Newton optimizer. */
    public static class TutorialGaussNewton {
        // No parameters required by the implementation
    }

}
