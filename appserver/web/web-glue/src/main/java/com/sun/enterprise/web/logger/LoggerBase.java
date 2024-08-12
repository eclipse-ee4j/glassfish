/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.web.logger;

import jakarta.servlet.ServletException;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.CharArrayWriter;
import java.io.PrintWriter;

import org.apache.catalina.Container;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Logger;

/**
 * Convenience base class for <b>Logger</b> implementations.  The only
 * method that must be implemented is
 * <code>write(String msg, int verbosity)</code>, plus any property
 * setting and lifecycle methods required for configuration.
 *
 */

abstract class LoggerBase implements Logger {

    // ----------------------------------------------------- Instance Variables

    /**
     * The Container with which this Logger has been associated.
     */
    protected Container container = null;

    /**
     * The descriptive information about this implementation.
     */
    protected static final String info =
        "com.sun.enterprise.web.logger.LoggerBase/1.0";

    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    // ------------------------------------------------------------- Properties

    /**
     * Return the Container with which this Logger has been associated.
     */
    public Container getContainer() {
        return (container);
    }

    /**
     * Set the Container with which this Logger has been associated.
     *
     * @param container The associated Container
     */
    public void setContainer(Container container) {

        Container oldContainer = this.container;
        this.container = container;
        support.firePropertyChange("container", oldContainer, this.container);
    }

    /**
     * Return descriptive information about this Logger implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    public String getInfo() {
        return (info);
    }

    /**
     * Logger interface method (ignored)
     */
    public int getVerbosity() {
        //Ignored
        return -1;
    }

    /**
     * Logger interface method (ignored)
     *
     * @param verbosity The new verbosity level
     */
    public void setVerbosity(int verbosity) {
        //Ignored
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Writes the specified message to a servlet log file, usually an event
     * log.  The name and type of the servlet log is specific to the
     * servlet container.
     *
     * @param msg A <code>String</code> specifying the message to be
     *  written to the log file
     */
    public void log(String msg) {
        write(msg, DEBUG);
    }

    /**
     * Writes the specified exception, and message, to a servlet log file.
     * The implementation of this method should call
     * <code>log(msg, exception)</code> instead.  This method is deprecated
     * in the ServletContext interface, but not deprecated here to avoid
     * many useless compiler warnings.  This message will be logged
     * unconditionally.
     *
     * @param exception An <code>Exception</code> to be reported
     * @param msg The associated message string
     */
    public void log(Exception exception, String msg) {
        log(msg, exception);
    }

    /**
     * Writes an explanatory message and a stack trace for a given
     * <code>Throwable</code> exception to the servlet log file.  The name
     * and type of the servlet log file is specific to the servlet container,
     * usually an event log.  This message will be logged unconditionally.
     *
     * @param msg A <code>String</code> that describes the error or
     *  exception
     * @param throwable The <code>Throwable</code> error or exception
     */
    public void log(String msg, Throwable throwable) {
        write(msg, throwable, ERROR);
    }

    /**
     * Writes the specified message to the servlet log file, usually an event
     * log, if the logger is set to a verbosity level equal to or higher than
     * the specified value for this message.
     *
     * @param message A <code>String</code> specifying the message to be
     *  written to the log file
     * @param verbosity Verbosity level of this message
     */
    public void log(String message, int verbosity) {
        write(message, verbosity);
    }

    /**
     * Writes the specified message and exception to the servlet log file,
     * usually an event log, if the logger is set to a verbosity level equal
     * to or higher than the specified value for this message.
     *
     * @param message A <code>String</code> that describes the error or
     *  exception
     * @param throwable The <code>Throwable</code> error or exception
     * @param verbosity Verbosity level of this message
     */
    public void log(String message, Throwable throwable, int verbosity) {
        write(message, throwable, verbosity);
    }

    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    protected void write(String msg, Throwable throwable, int verbosity) {
        CharArrayWriter buf = new CharArrayWriter();
        PrintWriter writer = new PrintWriter(buf);
        writer.println(msg);
        throwable.printStackTrace(writer);
        Throwable rootCause = null;
        if (throwable instanceof LifecycleException)
            rootCause = ((LifecycleException) throwable).getCause();
        else if (throwable instanceof ServletException)
            rootCause = ((ServletException) throwable).getRootCause();
        if (rootCause != null) {
            writer.println("----- Root Cause -----");
            rootCause.printStackTrace(writer);
        }
        write(buf.toString(), verbosity);
    }

    /**
     * Logs the given message at the given verbosity level.
     */
    protected abstract void write(String msg, int verbosity);
}
