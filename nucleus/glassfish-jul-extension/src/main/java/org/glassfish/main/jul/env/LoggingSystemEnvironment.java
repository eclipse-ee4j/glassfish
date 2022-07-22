/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.main.jul.env;

import java.io.PrintStream;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

/**
 * This class holds informations detected on the logging system startup, so whatever you will
 * change, this class should always have access to original values, interesting for the logging
 * system:
 * <ul>
 * <li>STDOUT
 * <li>STDERR
 * </ul>
 * It holds also other informations about the environment required for logs.
 *
 * @author David Matejcek
 */
public final class LoggingSystemEnvironment {

    private static final PrintStream ORIGINAL_STD_ERR = System.err;
    private static final PrintStream ORIGINAL_STD_OUT = System.out;

    // These values are global for this JVM
    // Why they are not volatile? Because it affects JIT optimizations
    // and they are not so critical.
    private static String productId;
    private static boolean releaseParametersEarly = true;
    private static boolean resolveLevelWithIncompleteConfiguration;

    private LoggingSystemEnvironment() {
        // hidden
    }


    /**
     * Call this method before you do any changes in global JVM objects like {@link System#out}!
     */
    public static synchronized void initialize() {
        // this is a rather psychological trick to force developer to touch this class.
    }


    /**
     * @return the STDOUT {@link PrintStream} used at startup.
     */
    public static PrintStream getOriginalStdErr() {
        return ORIGINAL_STD_ERR;
    }


    /**
     * @return the STDOUT {@link PrintStream} used at startup.
     */
    public static PrintStream getOriginalStdOut() {
        return ORIGINAL_STD_OUT;
    }


    /**
     * Sets original values of the STDOUT and STDERR print streams back.
     */
    public static void resetStandardOutputs() {
        logSetter("Output streams reset to JVM defaults.");
        System.setOut(ORIGINAL_STD_OUT);
        System.setErr(ORIGINAL_STD_ERR);
    }

    /**
     * @return the name of the product. Can be null if not explicitly set.
     */
    public static String getProductId() {
        return productId;
    }


    /**
     * @param productId the name of the product. It is null by default.
     */
    public static void setProductId(final String productId) {
        logSetter("productId: " + productId);
        LoggingSystemEnvironment.productId = productId;
    }


    /**
     * @return if true, parameters are forgotten after they were used in the message.
     */
    public static boolean isReleaseParametersEarly() {
        return releaseParametersEarly;
    }


    /**
     * Note: This method is used internally.
     *
     * @param releaseParametersEarly if true, parameters are forgotten after they were used in the message.
     */
    public static void setReleaseParametersEarly(boolean releaseParametersEarly) {
        logSetter("releaseParametersEarly: " + releaseParametersEarly);
        LoggingSystemEnvironment.releaseParametersEarly = releaseParametersEarly;
    }


    /**
     * Note: This method is used internally.
     *
     * @param resolveLevelWithIncompleteConfiguration If true, log record level threshold is
     *            resolved even if the logging is not completely configured.
     */
    public static void setResolveLevelWithIncompleteConfiguration(boolean resolveLevelWithIncompleteConfiguration) {
        logSetter("resolveLevelWithIncompleteConfiguration: " + resolveLevelWithIncompleteConfiguration);
        LoggingSystemEnvironment.resolveLevelWithIncompleteConfiguration = resolveLevelWithIncompleteConfiguration;
    }


    /**
     * @return if true, log record level threshold is resolved even if the logging is not completely
     *         configured.
     */
    public static boolean isResolveLevelWithIncompleteConfiguration() {
        return resolveLevelWithIncompleteConfiguration;
    }


    /**
     * Reason for this method? Like {@link System} class setters, it's quite important to have a clue
     * which part of the JVM changed the setting.
     */
    private static void logSetter(final String message) {
        GlassFishLoggingTracer.stacktrace(LoggingSystemEnvironment.class, message);
    }
}
