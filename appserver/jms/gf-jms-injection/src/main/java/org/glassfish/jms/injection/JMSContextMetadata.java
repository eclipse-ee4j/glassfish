/*
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

import java.io.Serializable;
import java.security.MessageDigest;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.jms.JMSConnectionFactory;
import jakarta.jms.JMSContext;
import jakarta.jms.JMSPasswordCredential;
import jakarta.jms.JMSSessionMode;
import org.glassfish.internal.api.RelativePathResolver;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * Serializable object which holds the information about the JMSContext
 * that was specified at the injection point.
 */
public class JMSContextMetadata implements Serializable {
    private static final Logger logger = Logger.getLogger(InjectableJMSContext.JMS_INJECTION_LOGGER);
    private final static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(JMSContextMetadata.class);
    public final static String DEFAULT_CONNECTION_FACTORY = "java:comp/DefaultJMSConnectionFactory";

    private final String lookup;
    private final int sessionMode;
    private final String userName;
    private final String password;
    private String fingerPrint;

    JMSContextMetadata(JMSConnectionFactory jmsConnectionFactoryAnnot, JMSSessionMode sessionModeAnnot, JMSPasswordCredential credentialAnnot) {
        if (jmsConnectionFactoryAnnot == null) {
            lookup = null;
        } else {
            lookup = jmsConnectionFactoryAnnot.value().trim();
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

    public String getLookup() {
       return lookup;
    }

    public int getSessionMode(){
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
        StringBuffer sb = new StringBuffer();
        sb.append("JMSContextMetadata[");
        sb.append("lookup=").append(lookup);
        sb.append(", sessionMode=").append(sessionMode);
        sb.append(", username=").append(userName);
        sb.append(", password=");
        if (password != null)
            sb.append("xxxxxx");
        else
            sb.append("null");
        sb.append(" [fingerPrint[").append(getFingerPrint());
        sb.append("]]");
        return sb.toString();
    }

    public String getFingerPrint() {
        if (fingerPrint == null) {
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                byte delimer = (byte) '|';
                md.update(delimer);
                String cf = lookup;
                if (lookup == null)
                    cf = DEFAULT_CONNECTION_FACTORY;
                md.update(cf.getBytes("ISO-8859-1"));
                md.update(delimer);
                md.update((byte) sessionMode);
                md.update(delimer);
                if (userName != null)
                    md.update(userName.getBytes("ISO-8859-1"));
                md.update(delimer);
                if (password != null)
                    md.update(password.getBytes("ISO-8859-1"));
                md.update(delimer);
                byte[] result = md.digest();
                StringBuffer buff = new StringBuffer();
                for(int i=0; i<result.length; i++) {
                    String byteStr = Integer.toHexString(result[i] & 0xFF);
                    if(byteStr.length() < 2)
                        buff.append('0');
                    buff.append(byteStr);
                }
                fingerPrint = buff.toString();
            } catch (Exception e) {
                throw new RuntimeException("Couldn't make digest of JMSContextMetadata content", e);
            }
        }
        return fingerPrint;
    }

    private boolean isPasswordAlias(String password){
        if (password != null && password.startsWith("${ALIAS="))
            return true;
        return false;
    }

    private String getUnAliasedPwd(String password) {
        if (password != null && isPasswordAlias(password)) {
            try {
                String unalisedPwd = RelativePathResolver.getRealPasswordFromAlias(password);
                if (unalisedPwd != null && !"".equals(unalisedPwd))
                    return unalisedPwd;
            } catch (Exception e) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, localStrings.getLocalString("decrypt.password.fail",
                               "Failed to unalias password for the reason: {0}."), e.toString());
                }
            }
        }
        return password;
    }
}
