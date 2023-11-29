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
/**
 * This package provides elements for testing performance
 * of Orekit Orbit Determination features.
 * <p>
 * {@link org.orekit.tutorials.estimation.performance.MeasurementGenerator MeasurementGenerator}
 * is a class used to generate measurement in the case a user does not have measurement for testing performance.
 * Please note that user-defined measurements file can be used.
 * The measurement generation is initialized using <i>measurement-generation.yaml</i> file in
 * <b>src/main/resources/performance-testing</b> folder.
 * </p><p>
 * {@link org.orekit.tutorials.estimation.performance.OrbitDeterminationEngine OrbitDeterminationEngine}
 * is the main class to configure the orbit determination. This class is initialized using <i>testing-X.yaml</i>
 * file in <b>src/main/resources/performance-testing</b> folder.
 * </p><p>
 * {@link org.orekit.tutorials.estimation.performance.PerformanceTesting PerformanceTesting}
 * is a runnable class allowing the user performing the performance testing of the orbit determination.
 * This class is initialized using the very simple file <i>performance-analysis.yaml</i>
 * in <b>src/main/resources/performance-testing</b> folder.
 * </p>
 * @author Bryan Cazabonne
 */
package org.orekit.tutorials.estimation.performance;
