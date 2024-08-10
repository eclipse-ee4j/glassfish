/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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
 * HASessionStoreValve.java
 *
 * Created on June 27, 2002, 6:42 PM
 */

package org.glassfish.web.ha.session.management;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Globals;
import org.apache.catalina.Manager;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.valves.ValveBase;
import org.glassfish.ha.common.HACookieManager;

/**
 *
 * @author  lwhite
 * @author Rajiv Mordani
 */
public class HASessionStoreValve extends ValveBase {

    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static final Logger _logger
        = HAStoreBase._logger;

    /** Creates a new instance of HASessionStoreValve */
    public HASessionStoreValve() {
        super();
    }

    /**
     * invoke call-back; nothing to do on the way in
     * @param request
     * @param response
     */
    public int invoke(org.apache.catalina.Request request, org.apache.catalina.Response response) throws java.io.IOException, jakarta.servlet.ServletException {
        //FIXME this is for 7.0PE style valves
        //left here if the same optimization is done to the valve architecture
        String sessionId = null;
        ReplicationWebEventPersistentManager manager;
        StandardContext  context;


        HttpServletRequest httpServletrequest = (HttpServletRequest)request.getRequest();
        HttpSession session = httpServletrequest.getSession(false);
        if (session != null) {
            sessionId = session.getId();

            if (sessionId != null) {
                context = (StandardContext) request.getContext();
                manager = (ReplicationWebEventPersistentManager)context.getManager();


                String oldJreplicaValue = null;

                Cookie[] cookies = httpServletrequest.getCookies();
                if (cookies != null) {
                    for (Cookie cookie: cookies) {
                        if (cookie.getName().equalsIgnoreCase(Globals.JREPLICA_COOKIE_NAME)) {
                            oldJreplicaValue = cookie.getValue();
                        }
                    }
                    String replica = manager.getReplicaFromPredictor(sessionId, oldJreplicaValue);
                    if (replica != null) {
                        Session sess = request.getSessionInternal(false);
                        if (sess != null) {
                            sess.setNote(Globals.JREPLICA_SESSION_NOTE, replica);
                        }
                    }
                }
            }
        }


        return INVOKE_NEXT;
        // return 0;
    }

    /**
     * A post-request processing implementation that does the valveSave.
     * @param request
     * @param response
     */
    public void postInvoke(Request request, Response response)
        throws IOException, ServletException {
        //FIXME this is for 7.0PE style valves
        //left here if the same optimization is done to the valve architecture
        doPostInvoke(request, response);
    }


    /**
     * A post-request processing implementation that does the valveSave.
     * @param request
     * @param response
     */
    private void doPostInvoke(Request request, Response response)
        throws IOException, ServletException {
        if(_logger.isLoggable(Level.FINEST)) {
            _logger.finest("IN HASessionStoreValve>>postInvoke()");
        }
        String sessionId = null;
        Session session;
        StandardContext context;
        Manager manager;
        HttpServletRequest hreq =
            (HttpServletRequest) request.getRequest();
        HttpSession hsess = hreq.getSession(false);
        if (hsess != null) {
            sessionId = hsess.getId();
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("IN HASessionStoreValve:postInvoke:sessionId=" +
                               sessionId);
            }
        }
        if (sessionId != null) {
            context = (StandardContext) request.getContext();
            manager = context.getManager();
            session = manager.findSession(sessionId);
            if(_logger.isLoggable(Level.FINEST)) {
                _logger.finest("IN HASessionStoreValve:postInvoke:session=" +
                               session);
            }
            if (session != null) {
                WebEventPersistentManager pMgr =
                        (WebEventPersistentManager) manager;
                pMgr.doValveSave(session);
            }
        }
        HACookieManager.reset();
    }

}
