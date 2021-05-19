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

package org.glassfish.webservices;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.WSWebServiceContext;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.web.WebModule;
import org.glassfish.api.invocation.InvocationManager;

import jakarta.xml.ws.EndpointReference;
import jakarta.xml.ws.handler.MessageContext;
import java.security.Principal;
import java.util.Set;
import java.util.Iterator;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.ejb.api.EJBInvocation;
import org.glassfish.internal.api.Globals;

/**
 * <p><b>NOT THREAD SAFE: mutable instance variables</b>
 */
public final class WebServiceContextImpl implements WSWebServiceContext {

    public static final ThreadLocal msgContext = new ThreadLocal();

    public static final ThreadLocal principal = new ThreadLocal();

    private WSWebServiceContext jaxwsContextDelegate;

    private static final String JAXWS_SERVLET = "org.glassfish.webservices.JAXWSServlet";

    private String servletName;

    private SecurityService  secServ;

    public WebServiceContextImpl() {
        if (Globals.getDefaultHabitat() != null) {
            secServ = Globals.get(org.glassfish.webservices.SecurityService.class);
        }
    }

    public void setContextDelegate(WSWebServiceContext wsc) {
        this.jaxwsContextDelegate = wsc;
    }

    public MessageContext getMessageContext() {
        return this.jaxwsContextDelegate.getMessageContext();
    }

    public void setMessageContext(MessageContext ctxt) {
        msgContext.set(ctxt);
    }

    public WSWebServiceContext getContextDelegate(){
        return jaxwsContextDelegate;
    }

    /*
     * this may still be required for EJB endpoints
     *
     */
    public void setUserPrincipal(Principal p) {
        principal.set(p);
    }

    public Principal getUserPrincipal() {
        // This could be an EJB endpoint; check the threadlocal variable
        Principal p = (Principal) principal.get();
        if (p != null) {
            return p;
        }
        // This is a servlet endpoint
        p = this.jaxwsContextDelegate.getUserPrincipal();
        //handling for WebService with WS-Security
        if (p == null && secServ != null) {
            WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
            InvocationManager mgr = wscImpl.getInvocationManager();
            boolean isWeb = ComponentInvocation.ComponentInvocationType.SERVLET_INVOCATION.
                    equals(mgr.getCurrentInvocation().getInvocationType()) ? true : false;
            p = secServ.getUserPrincipal(isWeb);
        }
        return p;
    }

    public boolean isUserInRole(String role) {
        WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
        ComponentInvocation.ComponentInvocationType EJBInvocationType = ComponentInvocation.ComponentInvocationType.EJB_INVOCATION;
        InvocationManager mgr = wscImpl.getInvocationManager();
        if ((mgr!=null) && (EJBInvocationType.equals(mgr.getCurrentInvocation().getInvocationType()))) {
            Object obj = mgr.getCurrentInvocation();
            boolean res = false;
            if (obj instanceof EJBInvocation) {
                EJBInvocation inv = (EJBInvocation) obj;
                res = inv.isCallerInRole(role);
            }
           return res;
        }
        // This is a servlet endpoint
        boolean ret = this.jaxwsContextDelegate.isUserInRole(role);
        //handling for webservice with WS-Security
        if (!ret && secServ != null) {

            if (mgr.getCurrentInvocation().getContainer() instanceof WebModule) {
                Principal p = getUserPrincipal();
                ret = secServ.isUserInRole((WebModule)mgr.getCurrentInvocation().getContainer(), p, servletName, role);
            }

        }
        return ret;
    }

    // TODO BM need to fix this after checking with JAXWS spec
    public EndpointReference getEndpointReference(Class clazz, org.w3c.dom.Element... params) {
        return this.jaxwsContextDelegate.getEndpointReference(clazz, params);
    }

    public EndpointReference getEndpointReference(org.w3c.dom.Element... params) {
        return this.jaxwsContextDelegate.getEndpointReference(params);
    }

    public Packet getRequestPacket() {
        return this.jaxwsContextDelegate.getRequestPacket();
    }

    void setServletName(Set webComponentDescriptors) {
        Iterator it = webComponentDescriptors.iterator();
        String endpointName = null;
        while (it.hasNext()) {
            WebComponentDescriptor desc = (WebComponentDescriptor)it.next();
            String name = desc.getCanonicalName();
            if (JAXWS_SERVLET.equals(desc.getWebComponentImplementation())) {
                endpointName = name;
            }
            if (desc.getSecurityRoleReferences().hasMoreElements()) {
                servletName = name;
                break;
            }
        }
        if (servletName == null) {
            servletName = endpointName;
        }
    }

}
