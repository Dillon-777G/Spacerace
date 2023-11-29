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
 * Container for SINEX data.
 * @author Bryan Cazabonne
 */
public class TutorialSinex {

    /** Path for sinex station positions file. */
    private String stationPositions;

    /** Path for sinex station eccentricities file. */
    private String stationEccentricities;

    /**
     * Get the path for sinex station positions file.
     * @return path for sinex station positions file
     */
    public String getStationPositions() {
        return stationPositions;
    }

    /**
     * Set the path for sinex station positions file.
     * @param stationPositions the path to set
     */
    public void setStationPositions(final String stationPositions) {
        this.stationPositions = stationPositions;
    }

    /**
     * Get the path for sinex station eccentricities file.
     * @return the path for sinex station eccentricities file.
     */
    public String getStationEccentricities() {
        return stationEccentricities;
    }

    /**
     * Set the path for sinex station eccentricities file.
     * @param stationEccentricities the path to set
     */
    public void setStationEccentricities(final String stationEccentricities) {
        this.stationEccentricities = stationEccentricities;
    }

}
