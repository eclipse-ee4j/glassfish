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

package com.sun.enterprise.admin.remote;

import com.sun.enterprise.universal.glassfish.AdminCommandResponse;
import com.sun.enterprise.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author bnevins
 */
class ManifestManager implements ResponseManager {
    ManifestManager(InputStream inStream, Logger logger) throws RemoteException, IOException {
        this.logger = logger;
        response = new AdminCommandResponse(inStream);
    }

    public Map<String, String> getMainAtts() {
        return response.getMainAtts();
    }

    public void process() throws RemoteException {
        logger.finer("PROCESSING MANIFEST...");

        // remember these are "succeed-fast".  They will throw a
        // RemoteSuccessException if they succeed...
        processGeneratedManPage();
        processManPage();
        processGeneric();

        // No RemoteSuccessException was thrown -- this is now an Error!!
        throw new RemoteFailureException("Could not process");
    }

    private void processManPage() throws RemoteSuccessException {
        String manPage = response.getValue(AdminCommandResponse.MANPAGE);

        if (!ok(manPage))
            return;

        throw new RemoteSuccessException(manPage);
    }

    private void processGeneratedManPage() throws RemoteException {
        if (!response.isGeneratedHelp())
            return;
        GeneratedManPageManager mgr = new GeneratedManPageManager(response);
        mgr.process();
    }

    private void processGeneric() throws RemoteSuccessException, RemoteFailureException {
        StringBuilder sb = new StringBuilder();
        String msg = response.getMainMessage();
        if (ok(msg)) {
            sb.append(msg);
        }

        boolean useMainChildrenAttr = Boolean.valueOf(response.getMainAtts().get("use-main-children-attribute"));

        if (useMainChildrenAttr) {
            sb = processMainChildrenAttribute(response.getMainAtts(), sb);
        } else {
            processOneLevel("", null, response.getMainAtts(), sb);
        }

        if (response.wasFailure()) {
            final String cause = response.getCause();
            if (ok(cause)) {
                if (logger.isLoggable(Level.FINER)) {
                    if (sb.length() > 0)
                        sb.append(EOL);
                    sb.append(cause);
                }
                throw new RemoteFailureException(sb.toString(), cause);
            }
            throw new RemoteFailureException(sb.toString());
        }

        throw new RemoteSuccessException(sb.toString());
    }

    // this is just HORRIBLE -- but that's the way it is presented from the
    // server.  I imagine tons of bug reports on this coming up...
    private void processOneLevel(String prefix, String key, Map<String, String> atts, StringBuilder sb) {

        if (atts == null)
            return;

        // we probably should not show props to the user
        // processProps(prefix, atts, sb);
        processChildren(prefix, key, atts, sb);
    }

    private void processChildren(String prefix, String parent, Map<String, String> atts, StringBuilder sb) {

        Map<String, Map<String, String>> kids = response.getChildren(atts);

        if (kids == null || kids.isEmpty())
            return;

        String childrenType = atts.get(AdminCommandResponse.CHILDREN_TYPE);
        int index = (parent == null) ? 0 : parent.length() + 1;

        for (Map.Entry<String, Map<String, String>> entry : kids.entrySet()) {
            String container = entry.getKey();

            if (sb.length() > 0)
                sb.append(EOL);
            if (ok(childrenType)) {
                sb.append(prefix).append(childrenType).append(" : ");
            }
            try {
                sb.append(java.net.URLDecoder.decode(container.substring(index), "UTF-8"));
            } catch (Exception e) {
                sb.append(container.substring(index));
            }
            processOneLevel(prefix + TAB, container, entry.getValue(), sb);
        }
    }

    /* Issue 5918 Keep output sorted. Grab "children" from main attributes
    * which has the original order of output returned from server-side
    */
    private StringBuilder processMainChildrenAttribute(Map<String, String> atts, StringBuilder sb) {
        String allChildren = atts.get("children");
        if (ok(allChildren)) {
            String[] children = allChildren.split(";");
            for (String child : children) {
                if (sb.length() > 0)
                    sb.append(EOL);
                sb.append(decode(child));
            }
        }
        return sb;
    }

    private boolean ok(String s) {
        return s != null && s.length() > 0 && !s.equals("null");
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        } catch (IllegalArgumentException e1) {
            return value;
        }
    }

    private Logger logger;
    private AdminCommandResponse response;
    private static final String EOL = StringUtils.NEWLINE;
    private static final String TAB = "    ";
}
