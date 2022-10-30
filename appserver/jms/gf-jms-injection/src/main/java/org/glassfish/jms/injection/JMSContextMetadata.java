/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jms.injection;

import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSPasswordCredential;
import jakarta.jms.JMSSessionMode;

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.internal.api.RelativePathResolver;

import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_COMPONENT;

/**
 * Serializable object which holds the information about the JMSContext that was specified at the injection point.
 */
public class JMSContextMetadata implements Serializable {

    static final SimpleJndiName DEFAULT_CONNECTION_FACTORY = new SimpleJndiName(
        JNDI_CTX_JAVA_COMPONENT + "DefaultJMSConnectionFactory");
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(InjectableJMSContext.JMS_INJECTION_LOGGER);

    private final SimpleJndiName lookup;
    private final int sessionMode;
    private final String userName;
    private final String password;
    private String fingerPrint;

    JMSContextMetadata(JMSConnectionFactory jmsConnectionFactoryAnnot, JMSSessionMode sessionModeAnnot, JMSPasswordCredential credentialAnnot) {
        if (jmsConnectionFactoryAnnot == null) {
            lookup = null;
        } else {
            lookup = new SimpleJndiName(jmsConnectionFactoryAnnot.value().trim());
        }

        if (sessionModeAnnot == null) {
            sessionMode = JMSContext.AUTO_ACKNOWLEDGE;
        } else {
            sessionMode = sessionModeAnnot.value();
        }

        if (credentialAnnot == null) {
            userName = null;
            password = null;
        } else {
            userName = credentialAnnot.userName();
            password = getUnAliasedPwd(credentialAnnot.password());
        }
    }

    public SimpleJndiName getLookup() {
        return lookup;
    }

    public int getSessionMode() {
        return sessionMode;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("JMSContextMetadata[");
        sb.append("lookup=").append(lookup);
        sb.append(", sessionMode=").append(sessionMode);
        sb.append(", username=").append(userName);
        sb.append(", password=");
        if (password != null) {
            sb.append("xxxxxx");
        } else {
            sb.append("null");
        }
        sb.append(" [fingerPrint[").append(getFingerPrint());
        sb.append("]]");
        return sb.toString();
    }

    public String getFingerPrint() {
        if (fingerPrint == null) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("MD5");
                byte delimer = (byte) '|';
                messageDigest.update(delimer);
                SimpleJndiName cf = lookup;
                if (lookup == null) {
                    cf = DEFAULT_CONNECTION_FACTORY;
                }
                messageDigest.update(cf.toString().getBytes("ISO-8859-1"));
                messageDigest.update(delimer);
                messageDigest.update((byte) sessionMode);
                messageDigest.update(delimer);
                if (userName != null) {
                    messageDigest.update(userName.getBytes("ISO-8859-1"));
                }
                messageDigest.update(delimer);
                if (password != null) {
                    messageDigest.update(password.getBytes("ISO-8859-1"));
                }
                messageDigest.update(delimer);
                byte[] result = messageDigest.digest();
                StringBuilder buff = new StringBuilder();
                for (byte element : result) {
                    String byteStr = Integer.toHexString(element & 0xFF);
                    if (byteStr.length() < 2) {
                        buff.append('0');
                    }
                    buff.append(byteStr);
                }
                fingerPrint = buff.toString();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't make digest of JMSContextMetadata content", e);
            }
        }
        return fingerPrint;
    }

    private boolean isPasswordAlias(String password) {
        if (password != null && password.startsWith("${ALIAS=")) {
            return true;
        }

        return false;
    }

    private String getUnAliasedPwd(String password) {
        if (password != null && isPasswordAlias(password)) {
            try {
                String unalisedPwd = RelativePathResolver.getRealPasswordFromAlias(password);
                if (unalisedPwd != null && !unalisedPwd.isEmpty()) {
                    return unalisedPwd;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to unalias password.", e);
            }
        }

        return password;
    }
}
