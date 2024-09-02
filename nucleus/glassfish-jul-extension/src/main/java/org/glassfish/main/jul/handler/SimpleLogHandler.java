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

package org.glassfish.main.jul.handler;

import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.StreamHandler;

import org.glassfish.main.jul.cfg.LogProperty;
import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.formatter.OneLineFormatter;


/**
 * The simplest possible log handler.
 * <p>
 * Similar to {@link java.util.logging.ConsoleHandler} except
 * <ul>
 * <li>can be configured to use STDOUT instead of STDERR
 * <li>uses {@link OneLineFormatter} by default
 * </ul>
 *
 * @author David Matejcek
 */
public class SimpleLogHandler extends StreamHandler {

    /**
     * Configures the instance with properties prefixed by the name of this class.
     */
    public SimpleLogHandler() {
        final HandlerConfigurationHelper helper = HandlerConfigurationHelper.forHandlerClass(getClass());
        final PrintStream outputStream;
        if (helper.getBoolean(SimpleLogHandlerProperty.USE_ERROR_STREAM, true)) {
            outputStream = LoggingSystemEnvironment.getOriginalStdErr();
        } else {
            outputStream = LoggingSystemEnvironment.getOriginalStdOut();
        }
        setOutputStream(new UncloseablePrintStream(outputStream,
            helper.getCharset(SimpleLogHandlerProperty.ENCODING, Charset.defaultCharset())));
        setFormatter(helper.getFormatter(OneLineFormatter.class));
    }


    /**
     * Configures the instance with properties prefixed by the name of this class
     * and sets the explicit {@link PrintStream}
     *
     * @param printStream
     */
    public SimpleLogHandler(final PrintStream printStream) {
        super(printStream, new OneLineFormatter());
    }


    /**
     * Publishes the record and calls {@link #flush()}
     */
    @Override
    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    /**
     * Executes {@link #flush()}
     */
    @Override
    public void close() {
        flush();
    }


    /**
     * {@link Handler#close()} closes also stream it used for the output, but we don't want to close
     * STDOUT and STDERR
     */
    private static final class UncloseablePrintStream extends PrintStream {

        private UncloseablePrintStream(PrintStream out, Charset encoding) {
            super(out, false, encoding);
        }

        @Override
        public void close() {
            // don't close
        }
    }


    /**
     * Configuration property set of this handler.
     */
    public enum SimpleLogHandlerProperty implements LogProperty {

        /** Use STDERR or STDOUT? Default is true (STDERR). */
        USE_ERROR_STREAM("useErrorStream"),
        /** Class of the {@link Formatter} used with this handler */
        FORMATTER(HandlerConfigurationHelper.FORMATTER.getPropertyName()),
        /** Minimal level accepted by this handler, default is {@link Level#ALL} */
        LEVEL("level"),
        /** Additional filter class used to filter log records. Default is null. */
        FILTER("filter"),
        /** Output stream encoding. Default is null */
        ENCODING("encoding"),
        ;

        private final String propertyName;

        SimpleLogHandlerProperty(final String propertyName) {
            this.propertyName = propertyName;
        }


        @Override
        public String getPropertyName() {
            return propertyName;
        }

        /**
         * @return full name using the {@link SimpleLogHandler} class.
         */
        public String getPropertyFullName() {
            return getPropertyFullName(SimpleLogHandler.class);
        }
    }
}
