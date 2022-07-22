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

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.logging.Formatter;
import java.util.logging.Level;

import org.glassfish.main.jul.cfg.GlassFishLoggingConstants;

import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.DEFAULT_BUFFER_CAPACITY;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.DEFAULT_BUFFER_TIMEOUT;
import static org.glassfish.main.jul.handler.GlassFishLogHandlerProperty.DEFAULT_ROTATION_LIMIT_MB;

/**
 * Configuration for {@link GlassFishLogHandler}
 *
 * @author David Matejcek
 */
public class GlassFishLogHandlerConfiguration implements Cloneable {
    private Level level = Level.INFO;
    private Charset encoding = StandardCharsets.UTF_8;

    private boolean enabled = true;
    private File logFile;
    /** Count of flushed records in one batch, not a frequency at all */
    private int flushFrequency;
    private int maxArchiveFiles;

    private int bufferCapacity = DEFAULT_BUFFER_CAPACITY;
    private int bufferTimeout = DEFAULT_BUFFER_TIMEOUT;

    private boolean rotationOnDateChange;
    private int rotationTimeLimitMinutes;
    private long rotationSizeLimitBytes = DEFAULT_ROTATION_LIMIT_MB * GlassFishLoggingConstants.BYTES_PER_MEGABYTES;
    private boolean compressionOnRotation;

    private boolean redirectStandardStreams;

    private Formatter formatterConfiguration;

    /**
     * @return minimal acceptable level of the record to be handled. Default is {@link Level#INFO}
     */
    public Level getLevel() {
        return level;
    }


    /**
     * @param level - minimal acceptable level of the record to be handled.
     *            Default is {@link Level#INFO}
     */
    public void setLevel(final Level level) {
        this.level = level;
    }


    /**
     * @return used charset, default is {@value StandardCharsets#UTF_8}
     */
    public Charset getEncoding() {
        return encoding;
    }


    /**
     * @param encoding - used charset, default is {@value StandardCharsets#UTF_8}
     */
    public void setEncoding(final Charset encoding) {
        this.encoding = encoding;
    }


    public boolean isEnabled() {
        return enabled;
    }


    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }


    public File getLogFile() {
        return logFile;
    }


    public void setLogFile(final File logFile) {
        this.logFile = logFile;
    }


    public boolean isRedirectStandardStreams() {
        return redirectStandardStreams;
    }


    public void setRedirectStandardStreams(final boolean logStandardStreams) {
        this.redirectStandardStreams = logStandardStreams;
    }


    public int getFlushFrequency() {
        return flushFrequency;
    }


    public void setFlushFrequency(final int flushFrequency) {
        this.flushFrequency = flushFrequency;
    }


    public int getBufferCapacity() {
        return bufferCapacity;
    }


    public void setBufferCapacity(final int bufferCapacity) {
        this.bufferCapacity = bufferCapacity;
    }


    public int getBufferTimeout() {
        return bufferTimeout;
    }


    public void setBufferTimeout(final int bufferTimeout) {
        this.bufferTimeout = bufferTimeout;
    }


    public long getRotationSizeLimitBytes() {
        return rotationSizeLimitBytes;
    }


    public void setRotationSizeLimitMB(final long megabytes) {
        this.rotationSizeLimitBytes = megabytes * GlassFishLoggingConstants.BYTES_PER_MEGABYTES;
    }


    public void setRotationSizeLimitBytes(final long bytes) {
        this.rotationSizeLimitBytes = bytes;
    }


    public boolean isCompressionOnRotation() {
        return compressionOnRotation;
    }


    public void setCompressionOnRotation(final boolean compressionOnRotation) {
        this.compressionOnRotation = compressionOnRotation;
    }


    public boolean isRotationOnDateChange() {
        return rotationOnDateChange;
    }


    public void setRotationOnDateChange(final boolean rotationOnDateChange) {
        this.rotationOnDateChange = rotationOnDateChange;
    }


    /**
     * @return minutes
     */
    public int getRotationTimeLimitMinutes() {
        return rotationTimeLimitMinutes;
    }


    /**
     * @param rotationTimeLimitValue minutes
     */
    public void setRotationTimeLimitMinutes(final int rotationTimeLimitValue) {
        this.rotationTimeLimitMinutes = rotationTimeLimitValue;
    }


    public int getMaxArchiveFiles() {
        return maxArchiveFiles;
    }


    public void setMaxArchiveFiles(final int maxHistoryFiles) {
        this.maxArchiveFiles = maxHistoryFiles;
    }


    public Formatter getFormatterConfiguration() {
        return formatterConfiguration;
    }


    public void setFormatterConfiguration(final Formatter formatterConfiguration) {
        this.formatterConfiguration = formatterConfiguration;
    }


    @Override
    public GlassFishLogHandlerConfiguration clone() {
        try {
            return (GlassFishLogHandlerConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone failed.", e);
        }
    }
}
