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

import java.util.Set;

/**
 * Class used to configure SSL processing parameters
 *
 * @author Rajiv Mordani
 */
public class SslConfig {

    private String keyStore;
    private String trustStore;
    private char[] keyPassword;
    private char[] trustPassword;
    private int timeoutMilliSeconds;
    private Set<SslType> algorithms;
    private String certNickname;

    /**
     * Create an instance of <tt>SslConfig</tt>.
     *
     * @param key the location of the keystore file
     * @param trust the location of the truststore file
     */
    public SslConfig(String key, String trust) {
        this.keyStore = key;
        this.trustStore = trust;
    }

    /**
     * Sets the location of the keystore file
     *
     * @param keyStore The location of the keystore file
     */
    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    /**
     * Sets the location of the truststore file
     *
     * @param trustStore The location of the truststore file
     */
    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    /**
     * Sets the password of the keystore file
     *
     * @param keyPassword The password of the keystore file
     */
    public void setKeyPassword(char[] keyPassword) {
        this.keyPassword = keyPassword;
    }

    /**
     * Sets the password of the truststore file
     *
     * @param trustPassword The password of the truststore file
     */
    public void setTrustPassword(char[] trustPassword) {
        this.trustPassword = trustPassword;
    }

    /**
     * Sets the timeout within which there must be activity from the client
     *
     * @param timeoutMilliSeconds The timeout in milliseconds
     */
    public void setHandshakeTimeout(int timeoutMilliSeconds) {
        this.timeoutMilliSeconds = timeoutMilliSeconds;
    }

    /**
     * Sets the algorithm
     *
     * @param algorithms
     */
    public void setAlgorithms(Set<SslType> algorithms) {
        this.algorithms = algorithms;
    }

    /**
     * Gets the location of the keystore file
     *
     * @return the location of the keystore file
     */
    public String getKeyStore() {
        return this.keyStore;
    }

    /**
     * Gets the truststore file location
     *
     * @return the location of the truststore file
     */
    public String getTrustStore() {
        return this.trustStore;
    }

    /**
     * Gets the password of the keystore file
     *
     * @return the password of the keystore file
     */
    public char[] getKeyPassword() {
        return this.keyPassword;
    }

    /**
     * Gets the password of the truststore file
     *
     * @return the password of the truststore file
     */
    public char[] getTrustPassword() {
        return this.trustPassword;
    }

    /**
     * Gets the timeout within which there must be activity from the client
     *
     * @return the timeout in milliseconds
     */
    public int getHandshakeTimeout() {
        return this.timeoutMilliSeconds;
    }

    /**
     * Sets the algorithm
     *
     * @return the algorithm
     */
    public Set<SslType> getAlgorithms() {
        return this.algorithms;
    }

    /**
     * Gets the nickname of the server certificate in the certificate database
     *
     * @return the certNickname
     */
    public String getCertNickname() {
       return this.certNickname;
    }

    /**
     * Sets the certNickname
     *
     */
    public void setCertNickname(String value) {
        this.certNickname = value;
    }

}
