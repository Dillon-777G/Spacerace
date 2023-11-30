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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.hipparchus.exception.LocalizedCoreFormats;
import org.hipparchus.stat.descriptive.StreamingStatistics;
import org.orekit.errors.OrekitException;
import org.orekit.estimation.measurements.EstimatedMeasurement;
import org.orekit.estimation.measurements.ObservedMeasurement;

/** Local class for measurement-specific log.
 * @param T type of mesurement
 * @author Luc Maisonobe
 */
abstract class MeasurementLog<T extends ObservedMeasurement<T>> {

    /** Separator. */
    protected static final String SEP = "  ";

    /** New line. */
    protected static final String NEW_LINE = "%n";

    /** Date format. */
    protected static final String DATE_FORMAT = "%-25s";

    /** Station format. */
    protected static final String STATION_FORMAT = "%-10s";

    /** Number tag format. */
    protected static final String NB_TAG_FORMAT = "%20s";

    /** Number format. */
    protected static final String NB_FORMAT = "%20.9f";

    /** Header format for single data with station name. */
    protected static final String STATION_HEADER_FORMAT = DATE_FORMAT + SEP + STATION_FORMAT + SEP + // Date & station
                    NB_TAG_FORMAT + SEP + NB_TAG_FORMAT + SEP + NB_TAG_FORMAT + NEW_LINE; // Data: estimated, observed, residual

    /** Line format for single data with station name. */
    protected static final String STATION_LINE_FORMAT = DATE_FORMAT + SEP + STATION_FORMAT + SEP + // Date & station
                    NB_FORMAT + SEP + NB_FORMAT + SEP + NB_FORMAT + NEW_LINE; // Data: estimated, observed, residual


    /** Residuals. */
    private final SortedSet<EstimatedMeasurement<T>> evaluations;

    /** Measurements name. */
    private final String name;

    /** Output file. */
    private final File file;

    /** Output stream. */
    private final PrintStream stream;

    /** Simple constructor.
     * @param home home directory
     * @param baseName output file base name (may be null)
     * @param name measurement name
     * @exception IOException if output file cannot be created
     */
    MeasurementLog(final File home, final String baseName, final String name) throws IOException {
        this.evaluations = new TreeSet<EstimatedMeasurement<T>>(Comparator.naturalOrder());
        this.name        = name;
        if (baseName == null) {
            this.file    = null;
            this.stream  = null;
        } else {
            this.file    = new File(home, baseName + "-" + name + "-residuals.out");
            this.stream  = new PrintStream(file, StandardCharsets.UTF_8.name());
        }
    }

    /** Get the output stream.
     * @return output stream
     */
    protected PrintStream getStream() {
        return stream;
    }

    /** Display a header.
     */
    abstract void displayHeader();

    /** Display an evaluation residual.
     * @param evaluation evaluation to consider
     */
    abstract void displayResidual(EstimatedMeasurement<T> evaluation);

    /** Compute residual value.
     * @param evaluation evaluation to consider
     * @return residual value
     */
    abstract double residual(EstimatedMeasurement<T> evaluation);

    /** Add an evaluation.
     * @param evaluation evaluation to add
     */
    void add(final EstimatedMeasurement<T> evaluation) {
        evaluations.add(evaluation);
    }

    /** Display summary statistics in the general log file.
     * @param logStream log stream
     */
    public void displaySummary(final PrintStream logStream) {
        if (!evaluations.isEmpty()) {

            // compute statistics
            final StreamingStatistics stats = new StreamingStatistics();
            for (final EstimatedMeasurement<T> evaluation : evaluations) {
                stats.addValue(residual(evaluation));
            }

            // display statistics
            logStream.println("Measurements type: " + name);
            logStream.println("   number of measurements: " + stats.getN());
            logStream.println("   residuals min  value  : " + stats.getMin());
            logStream.println("   residuals max  value  : " + stats.getMax());
            logStream.println("   residuals mean value  : " + stats.getMean());
            logStream.println("   residuals σ           : " + stats.getStandardDeviation());

        }
    }

    /** Display detailed residuals.
     */
    public void displayResiduals() {
        if (file != null && !evaluations.isEmpty()) {
            displayHeader();
            for (final EstimatedMeasurement<T> evaluation : evaluations) {
                displayResidual(evaluation);
            }
        }
    }

    /** Close the measurement-specific log file.
     * <p>
     * The file is deleted if it contains no data.
     * </p>
     */
    public void close() {
        if (stream != null) {
            stream.close();
            if (evaluations.isEmpty()) {
                // delete unused file
                if (!file.delete()) {
                    throw new OrekitException(LocalizedCoreFormats.SIMPLE_MESSAGE,
                                              "cannot delete " + file.getAbsolutePath());
                }
            }
        }
    }

}
