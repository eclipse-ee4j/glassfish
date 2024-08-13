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

/*
 * InvocationTrace.java
 *
 * Created on November 22, 2004, 4:35 PM
 */

package org.glassfish.webservices.monitoring;

import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.webservices.LogUtils;
import org.glassfish.webservices.SOAPMessageContext;

/**
 * An invocation trace contains the timestamp os a particular
 * message invocation, the stringified SOAP request and
 * response or the SOAP Faults if the invocation resulted in one.
 * <p><b>NOT THREAD SAFE: mutable instance variables</b>
 *
 * @author Jerome Dochez
 */
public class MessageTraceImpl implements MessageTrace {

    private Endpoint source;
    private String soapMessage=null;
    private TransportInfo transportInfo=null;

    /** Creates a new instance of InvocationTrace */
    public MessageTraceImpl() {

    }

    /**
     * Return the SOAPMessage as a string including the SOAPHeaders or not
     * @param includeHeaders the soap headers.
     * @return the soap message
     */
    public String getMessage(boolean includeHeaders) {

        if (soapMessage!=null) {
            if (includeHeaders) {
                return soapMessage;
            }

            Pattern p = Pattern.compile("<env:Body>.*</env:Body>");
            Matcher m = p.matcher(soapMessage);
            if (m.find()) {
                return soapMessage.substring(m.start(),m.end());
            } else {
                return soapMessage;
            }
        }
        return null;
    }

    /**
     * Return the endpoint where this message originated from
     */
    public Endpoint getEndpoint() {
        return source;
    }

    public void setMessageContext(SOAPMessageContext soapMessageCtx) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            soapMessageCtx.getMessage().writeTo(baos);
        } catch(Exception e) {
            WebServiceEngineImpl.sLogger.log(Level.WARNING, LogUtils.CANNOT_LOG_SOAPMSG, e.getMessage());
        }

        soapMessage = baos.toString();
    }


    public void setEndpoint(Endpoint source) {
        this.source = source;
    }

    public TransportInfo getTransportInfo() {
        return transportInfo;
    }

    public void setTransportInfo(TransportInfo info) {
        transportInfo = info;
    }
}
