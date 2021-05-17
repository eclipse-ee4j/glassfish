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

package org.glassfish.embeddable.web.config;

/**
 * Class used for configuring VirtualServer instances.
 *
 * @see org.glassfish.embeddable.web.VirtualServer
 */
public class VirtualServerConfig {

    private boolean ssoEnabled;
    private boolean accessLoggingEnabled;
    private String defaultWebXml;
    private String contextXmlDefault;
    private boolean allowLinking;
    private String allowRemoteAddress;
    private String denyRemoteAddress;
    private String allowRemoteHost;
    private String denyRemoteHost;
    private String hostNames = "${com.sun.aas.hostName}";

    /**
     * Enables or disables Single-Sign-On.
     *
     * @param ssoEnabled true if Single-Sign-On is to be enabled, false
     * otherwise
     */
    public void setSsoEnabled(boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    /**
     * Checks if Single-Sign-On is enabled or disabled.
     *
     * @return true if Single-Sign-On is enabled, false otherwise
     */
    public boolean isSsoEnabled() {
        return ssoEnabled;
    }

    /**
     * Enables or disables access logging.
     *
     * @param accessLoggingEnabled true if access logging is to be enabled,
     * false otherwise
     */
    public void setAccessLoggingEnabled(boolean accessLoggingEnabled) {
        this.accessLoggingEnabled = accessLoggingEnabled;
    }

    /**
     * Checks if access logging is enabled or disabled.
     *
     * @return true if access logging is enabled, false otherwise
     */
    public boolean isAccessLoggingEnabled() {
        return accessLoggingEnabled;
    }

    /**
     * Sets the location of the default web.xml configuration file.
     *
     * @param defaultWebXml the location of the default web.xml configuration
     * file
     */
    public void setDefaultWebXml(String defaultWebXml) {
        this.defaultWebXml = defaultWebXml;
    }

    /**
     * Gets the location of the default web.xml configuration file.
     *
     * @return the location of the default web.xml configuration file, or
     * <tt>null</tt> if <tt>setDefaultWebXml</tt> was never called on this
     * <tt>VirtualServerConfig</tt>
     */
    public String getDefaultWebXml() {
        return defaultWebXml;
    }

    /**
     * Sets the location of the default context.xml configuration file.
     *
     * @param contextXmlDefault the location of the default context.xml
     * configuration file.
     */
    public void setContextXmlDefault(String contextXmlDefault) {
        this.contextXmlDefault = contextXmlDefault;
    }

    /**
     * Gets the location of the default context.xml configuration file.
     *
     * @return the location of the default context.xml configuration file,
     * or <tt>null</tt> if <tt>setContextXmlDefault</tt> was never called
     * on this <tt>VirtualServerConfig</tt>
     */
    public String getContextXmlDefault() {
        return contextXmlDefault;
    }

    /**
     * Enables or disables the serving of resources that are symbolic links.
     *
     * @param allowLinking true if resources that are symbolic links are
     * to be served, false otherwise
     */
    public void setAllowLinking(boolean allowLinking) {
        this.allowLinking = allowLinking;
    }

    /**
     * Checks if resources that are symbolic links will be served.
     *
     * @return true if resources that are symbolic links will be served,
     * false otherwise
     */
    public boolean isAllowLinking() {
        return allowLinking;
    }

    /**
     * Sets the comma-separated list of regular expression patterns that
     * the remote client's IP address is compared to.
     *
     * <p>If this property is specified, the remote address must match for
     * this request to be accepted. If this property is not specified,
     * all requests are accepted unless the remote address matches a
     * <tt>denyRemoteAddress</tt> pattern.
     *
     * @param allowRemoteAddress the comma-separated list of regular
     * expression patterns that the remote client's IP address is compared
     * to
     */
    public void setAllowRemoteAddress(String allowRemoteAddress) {
        this.allowRemoteAddress = allowRemoteAddress;
    }

    /**
     * Gets the comma-separated list of regular expression patterns that
     * the remote client's IP address is compared to.
     *
     * @return the comma-separated list of regular expression patterns that
     * the remote client's IP address is compared to, or <tt>null</tt>
     * if <tt>setAllowRemoteAddress</tt> was never called on this
     * <tt>VirtualServerConfig</tt>
     */
    public String getAllowRemoteAddress() {
        return allowRemoteAddress;
    }

    /**
     * Sets the comma-separated list of regular expression patterns that
     * the remote client's IP address is compared to.
     *
     * <p>If this property is specified, the remote address must not match
     * for this request to be accepted. If this property is not specified,
     * request acceptance is governed solely by the allowRemoteAddress
     * property.
     *
     * @param denyRemoteAddress the comma-separated list of regular
     * expression patterns that the remote client's IP address is
     * compared to
     */
    public void setDenyRemoteAddress(String denyRemoteAddress) {
        this.denyRemoteAddress = denyRemoteAddress;
    }

    /**
     * Gets the comma-separated list of regular expression patterns that
     * the remote client's IP address is compared to.
     *
     * @return the comma-separated list of regular expression patterns that
     * the remote client's IP address is compared to, or <tt>null</tt>
     * if <tt>setDenyRemoteAddress</tt> was never called on this
     * <tt>VirtualServerConfig</tt>
     */
    public String getDenyRemoteAddress() {
        return denyRemoteAddress;
    }

    /**
     * Sets the comma-separated list of regular expression patterns
     * that the remote client's hostname (as returned by
     * java.net.Socket.getInetAddress().getHostName()) is compared to.
     *
     * <p>If this property is specified, the remote hostname must match
     * for this request to be accepted. If this property is not specified,
     * all requests are accepted unless the remote hostname matches a
     * <tt>denyRemoteHost</tt> pattern.
     *
     * @param allowRemoteHost the comma-separated list of regular
     * expression patterns that the remote client's hostname (as returned
     * by java.net.Socket.getInetAddress().getHostName()) is compared to
     */
    public void setAllowRemoteHost(String allowRemoteHost) {
        this.allowRemoteHost = allowRemoteHost;
    }

    /**
     * Gets the comma-separated list of regular expression patterns
     * that the remote client's hostname (as returned by
     * java.net.Socket.getInetAddress().getHostName()) is compared to.
     *
     * @return the comma-separated list of regular expression patterns
     * that the remote client's hostname (as returned by
     * java.net.Socket.getInetAddress().getHostName()) is compared to,
     * or <tt>null</tt> if <tt>setAllowRemoteHost</tt> was never called
     * on this <tt>VirtualServerConfig</tt>
     */
    public String getAllowRemoteHost() {
        return allowRemoteHost;
    }

    /**
     * Sets the comma-separated list of regular expression patterns that
     * the remote client's hostname (as returned by
     * java.net.Socket.getInetAddress().getHostName()) is compared to.
     *
     * <p>If this property is specified, the remote hostname must not
     * match for this request to be accepted. If this property is not
     * specified, request acceptance is governed solely by the
     * <tt>allowRemoteHost</tt> property.
     *
     * @param denyRemoteHost the comma-separated list of regular
     * expression patterns that the remote client's hostname
     * (as returned by java.net.Socket.getInetAddress().getHostName())
     * is compared to
     */
    public void setDenyRemoteHost(String denyRemoteHost) {
        this.denyRemoteHost = denyRemoteHost;
    }

    /**
     * Gets the comma-separated list of regular expression patterns that
     * the remote client's hostname (as returned by
     * java.net.Socket.getInetAddress().getHostName()) is compared to.
     *
     * @return the comma-separated list of regular expression patterns that
     * the remote client's hostname (as returned by
     * java.net.Socket.getInetAddress().getHostName()) is compared to,
     * or <tt>null</tt> if <tt>setDenyRemoteHost</tt> was never called
     * on this <tt>VirtualServerConfig</tt>
     */
    public String getDenyRemoteHost() {
        return denyRemoteHost;
    }

    /**
     * Sets the host names that will be assigned to any
     * <tt>VirtualServer</tt> configured via this
     * <tt>VirtualServerConfig</tt> separated by commas.
     *
     * @param hostNames the host names
     */
    public void setHostNames(String hostNames) {
        this.hostNames = hostNames;
    }

    /**
     * Gets the host names assigned to any <tt>VirtualServer</tt> configured
     * via this <tt>VirtualServerConfig</tt>.
     *
     * @return the host names
     */
    public String getHostNames() {
        return hostNames;
    }
}
