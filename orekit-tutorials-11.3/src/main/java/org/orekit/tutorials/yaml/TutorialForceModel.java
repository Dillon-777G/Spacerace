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

import java.util.List;

import org.hipparchus.geometry.euclidean.threed.Vector3D;

/**
 * Initial data to initialize the main Orekit force models.
 * <p>
 * Data are read from a YAML file.
 * </p>
 * @author Bryan Cazabonne
 */
public class TutorialForceModel {

    /** Central body gravity. */
    private TutorialGravity gravity;

    /** Third body attraction. */
    private List<TutorialThirdBody> thirdBody;

    /** Atmospheric drag. */
    private TutorialDrag drag;

    /** Solar radiation pressure. */
    private TutorialSolarRadiationPressure solarRadiationPressure;

    /** Post-Newtonian correction force due to general relativity. */
    private TutorialRelativity relativity;

    /** Ocean tides. */
    private TutorialOceanTides oceanTides;

    /** Polynomial accelerations. */
    private List<TutorialPolynomialAcceleration> polynomialAcceleration;

    /** Maneuvers. */
    private List<TutorialManeuver> maneuvers;

    /**
     * Get the central body gravity force model.
     * @return the central body gravity force model
     */
    public TutorialGravity getGravity() {
        return gravity;
    }

    /**
     * Set the central body gravity force model.
     * @param gravity central body gravity force model
     */
    public void setGravity(final TutorialGravity gravity) {
        this.gravity = gravity;
    }

    /**
     * Get the list of third body attraction force models.
     * @return the list of third body attraction force models
     */
    public List<TutorialThirdBody> getThirdBody() {
        return thirdBody;
    }

    /**
     * Set the list of third body attraction force models.
     * @param thirdBody list of third body attraction force models
     */
    public void setThirdBody(final List<TutorialThirdBody> thirdBody) {
        this.thirdBody = thirdBody;
    }

    /**
     * Get the drag force model.
     * @return the drag force model
     */
    public TutorialDrag getDrag() {
        return drag;
    }

    /**
     * Set the drag force model.
     * @param drag drag force model
     */
    public void setDrag(final TutorialDrag drag) {
        this.drag = drag;
    }

    /**
     * Get the solar radiation pressure force model.
     * @return the solar radiation pressure force model
     */
    public TutorialSolarRadiationPressure getSolarRadiationPressure() {
        return solarRadiationPressure;
    }

    /**
     * Set the solar radiation pressure force model.
     * @param solarRadiationPressure olar radiation pressure force model
     */
    public void setSolarRadiationPressure(final TutorialSolarRadiationPressure solarRadiationPressure) {
        this.solarRadiationPressure = solarRadiationPressure;
    }

    /**
     * Get the relativity force model.
     * @return the relativity force model
     */
    public TutorialRelativity getRelativity() {
        return relativity;
    }

    /**
     * Set the relativity force model.
     * @param relativity relativity force model
     */
    public void setRelativity(final TutorialRelativity relativity) {
        this.relativity = relativity;
    }

    /**
     * Get the ocean tides force model.
     * @return the ocean tides force model
     */
    public TutorialOceanTides getOceanTides() {
        return oceanTides;
    }

    /**
     * Set the ocean tides force model.
     * @param oceanTides ocean tides force model
     */
    public void setOceanTides(final TutorialOceanTides oceanTides) {
        this.oceanTides = oceanTides;
    }

    /**
     * Get the list of polynomial acceleration force models.
     * @return the list of polynomial acceleration force models
     */
    public List<TutorialPolynomialAcceleration> getPolynomialAcceleration() {
        return polynomialAcceleration;
    }

    /**
     * Set the list of polynomial acceleration force models.
     * @param polynomialAcceleration list of polynomial acceleration force model
     */
    public void setPolynomialAcceleration(final List<TutorialPolynomialAcceleration> polynomialAcceleration) {
        this.polynomialAcceleration = polynomialAcceleration;
    }

    /**
     * Get the list of maneuvers.
     * @return the list of maneuvers
     */
    public List<TutorialManeuver> getManeuvers() {
        return maneuvers;
    }

    /**
     * Set the list of maneuvers.
     * @param maneuvers the list of maneuvers
     */
    public void setManeuvers(final List<TutorialManeuver> maneuvers) {
        this.maneuvers = maneuvers;
    }

    /** Central body gravity. */
    public static class TutorialGravity {

        /** Maximal degree. */
        private int degree;

        /** Maximal order. */
        private int order;

        /**
         * Get the maximal degree.
         * @return the maximal degree
         */
        public int getDegree() {
            return degree;
        }

        /**
         * Set the maximal degree.
         * @param degree maximal degree
         */
        public void setDegree(final int degree) {
            this.degree = degree;
        }

        /**
         * Get the maximal order.
         * @return the maximal order
         */
        public int getOrder() {
            return order;
        }

        /**
         * Set the maximal order.
         * @param order maximal order
         */
        public void setOrder(final int order) {
            this.order = order;
        }

    }

    /** Third body attraction. */
    public static class TutorialThirdBody {

        /** Name of the celestial body. */
        private String name;

        /** Flag for the computation of the body's solid tides. */
        private boolean withSolidTides;

        /**
         * Get the name of the celestial body.
         * @return the name of the celestial body
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name of the celestial body.
         * @param name name of the celestial body
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * Get the flag for the computation of the body's solid tides.
         * @return true if body's solid tides are computed
         */
        public boolean isWithSolidTides() {
            return withSolidTides;
        }

        /**
         * Set the flag for the computation of the body's solid tides.
         * @param withSolidTides true if body's solid tides are computed
         */
        public void setWithSolidTides(final boolean withSolidTides) {
            this.withSolidTides = withSolidTides;
        }

    }

    /** Atmospheric drag. */
    public static class TutorialDrag {

        /** Surface (m²). */
        private double area;

        /** Estimated drag coefficient. */
        private TutorialEstimatedParameter cd;

        /**
         * Get the cross section.
         * @return the cross section (m²)
         */
        public double getArea() {
            return area;
        }

        /**
         * Set the cross section.
         * @param area cross section (m²)
         */
        public void setArea(final double area) {
            this.area = area;
        }

        /**
         * Get the estimated drag coefficient.
         * @return the estimated drag coefficient
         */
        public TutorialEstimatedParameter getCd() {
            return cd;
        }

        /**
         * Set the estimated drag coefficient.
         * @param cd estimated drag coefficient
         */
        public void setCd(final TutorialEstimatedParameter cd) {
            this.cd = cd;
        }

    }

    /** Solar radiation pressure. */
    public static class TutorialSolarRadiationPressure {

        /** Surface (m²). */
        private double area;

        /** Estimated reflection coefficient. */
        private TutorialEstimatedParameter cr;

        /**
         * Get the cross section.
         * @return the cross section (m²)
         */
        public double getArea() {
            return area;
        }

        /**
         * Set the cross section.
         * @param area cross section (m²)
         */
        public void setArea(final double area) {
            this.area = area;
        }

        /**
         * Get the estimated reflection coefficient.
         * @return the estimated reflection coefficient
         */
        public TutorialEstimatedParameter getCr() {
            return cr;
        }

        /**
         * Set the estimated reflection coefficient.
         * @param cr estimated reflection coefficient
         */
        public void setCr(final TutorialEstimatedParameter cr) {
            this.cr = cr;
        }

    }

    /** Post-Newtonian correction force due to general relativity. */
    public static class TutorialRelativity {

        /** Flag to use the relativity force model. */
        private boolean isUsed;

        /**
         * Get the flag to use the relativity force model.
         * @return true if the relativity is used
         */
        public boolean isUsed() {
            return isUsed;
        }

        /**
         * Set the flag to use the relativity force model.
         * @param isUsed true if the relativity is used
         */
        public void setIsUsed(final boolean isUsed) {
            this.isUsed = isUsed;
        }

    }

    /** Ocean tides. */
    public static class TutorialOceanTides {

        /** Maximal degree. */
        private int degree;

        /** Maximal order. */
        private int order;

        /**
         * Get the maximal degree.
         * @return the maximal degree
         */
        public int getDegree() {
            return degree;
        }

        /**
         * Set the maximal degree.
         * @param degree maximal degree
         */
        public void setDegree(final int degree) {
            this.degree = degree;
        }

        /**
         * Get the maximal order.
         * @return the maximal order
         */
        public int getOrder() {
            return order;
        }

        /**
         * Set the maximal order.
         * @param order maximal order
         */
        public void setOrder(final int order) {
            this.order = order;
        }

    }

    /** Polynomial acceleration. */
    public static class TutorialPolynomialAcceleration {

        /**  Prefix to use for parameter drivers. */
        private String name;

        /** Acceleration direction in defining frame (X, Y, Z). */
        private double[] directions;

        /** Acceleration coefficients. */
        private double[] coefficients;

        /** Flag for acceleration coefficients estimation. */
        private boolean isEstimated;

        /**
         * Constructor.
         */
        public TutorialPolynomialAcceleration() {
            // initialise empty array
            this.directions  = new double[0];
            this.coefficients = new double[0];
        }

        /**
         * Get the prefix to use for parameter drivers.
         * @return the prefix to use for parameter drivers
         */
        public String getName() {
            return name;
        }

        /**
         * Set the prefix to use for parameter drivers.
         * @param name prefix to use for parameter drivers
         */
        public void setName(final String name) {
            this.name = name;
        }

        /**
         * Get the acceleration direction in defining frame.
         * @return the acceleration direction in defining frame (X, Y, Z)
         */
        public double[] getDirections() {
            return directions.clone();
        }

        /**
         * Set the acceleration direction in defining frame.
         * @param directions acceleration direction in defining frame (X, Y, Z)
         */
        public void setDirections(final double[] directions) {
            this.directions = directions.clone();
        }

        /**
         * Get the array of acceleration coefficients.
         * @return the array of acceleration coefficients
         */
        public double[] getCoefficients() {
            return coefficients.clone();
        }

        /**
         * Set the array of acceleration coefficients.
         * @param coefficients array of acceleration coefficients
         */
        public void setCoefficients(final double[] coefficients) {
            this.coefficients = coefficients.clone();
        }

        /**
         * Get the flag for acceleration coefficients estimation.
         * @return true is acceleration coefficients are estimated
         */
        public boolean isEstimated() {
            return isEstimated;
        }

        /**
         * Set the flag for acceleration coefficients estimation.
         * @param isEstimated true is acceleration coefficients are estimated
         */
        public void setIsEstimated(final boolean isEstimated) {
            this.isEstimated = isEstimated;
        }

        /**
         * Get the acceleration direction in defining frame.
         * @return the acceleration direction in defining frame
         */
        public Vector3D getAccelerationDirection() {
            final double[] direction = getDirections();
            return new Vector3D(direction);
        }

    }

    /** Maneuver. */
    public static class TutorialManeuver {

        /** Estimated start date. */
        private TutorialEstimatedDateParameter startDate;

        /** Estimated stop date. */
        private TutorialEstimatedDateParameter stopDate;

        /** Estimated median date. */
        private TutorialEstimatedDateParameter medianDate;

        /** Estimated thrust. */
        private TutorialEstimatedParameter thrust;

        /** Estimated duration. */
        private TutorialEstimatedParameter duration;

        /** Estimated flow rate. */
        private TutorialEstimatedParameter flowRate;

        /** Isp. */
        private double isp;

        /** Direction. */
        private Vector3D direction;

        /** Name. */
        private String name;

        /**
         * Get the estimated start date.
         * @return the estimated start date
         */
        public TutorialEstimatedDateParameter getStartDate() {
            return startDate;
        }

        /**
         * Set the estimated start date.
         * @param startDate the estimated start date
         */
        public void setStartDate(final TutorialEstimatedDateParameter startDate) {
            this.startDate = startDate;
        }

        /**
         * Get the estimated stop date.
         * @return the estimated stop date
         */
        public TutorialEstimatedDateParameter getStopDate() {
            return stopDate;
        }

        /**
         * Set the estimated stop date.
         * @param stopDate the estimated stop date
         */
        public void setStopDate(final TutorialEstimatedDateParameter stopDate) {
            this.stopDate = stopDate;
        }

        /**
         * Get the estimated median date.
         * @return the estimated median date
         */
        public TutorialEstimatedDateParameter getMedianDate() {
            return medianDate;
        }

        /**
         * Set the estimated median date.
         * @param medianDate the estimated median date
         */
        public void setMedianDate(final TutorialEstimatedDateParameter medianDate) {
            this.medianDate = medianDate;
        }

        /**
         * Get the estimated duration.
         * @return the estimated duration
         */
        public TutorialEstimatedParameter getDuration() {
            return duration;
        }

        /**
         * Set the estimated duration.
         * @param duration the estimated duration
         */
        public void setDuration(final TutorialEstimatedParameter duration) {
            this.duration = duration;
        }

        /**
         * Get the estimated thrust.
         * @return the estimated thrust
         */
        public TutorialEstimatedParameter getThrust() {
            return thrust;
        }

        /**
         * Set the estimated thrust.
         * @param thrust the estimated thrust
         */
        public void setThrust(final TutorialEstimatedParameter thrust) {
            this.thrust = thrust;
        }

        /**
         * Get the estimated flow rate.
         * @return the estimated flow rate
         */
        public TutorialEstimatedParameter getFlowRate() {
            return flowRate;
        }

        /**
         * Set the estimated flow rate.
         * @param flowRate the estimated flow rate
         */
        public void setFlowRate(final TutorialEstimatedParameter flowRate) {
            this.flowRate = flowRate;
        }

        /**
         * Get the Isp.
         * @return the Isp
         */
        public double getIsp() {
            return isp;
        }

        /**
         * Set the Isp.
         * @param isp the Isp
         */
        public void setIsp(final double isp) {
            this.isp = isp;
        }

        /**
         * Get the thust direction.
         * @return the thust direction
         */
        public Vector3D getDirection() {
            return direction;
        }

        /**
         * Set the thust direction.
         * @param direction the thust direction
         */
        public void setDirection(final double[] direction) {
            this.direction = new Vector3D(direction[0], direction[1], direction[2]);
        }

        /**
         * Get the name.
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * Set the name.
         * @param name the name
         */
        public void setName(final String name) {
            this.name = name;
        }
    }
}
