/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.resources.custom;

import com.sun.enterprise.server.logging.logviewer.backend.LogFilter;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.LogManager;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Vector;
import jakarta.ws.rs.QueryParam;

/**
 * REST resource to get Log Names simple wrapper around internal LogFilter class
 *
 * @author ludovic Champenois
 */
public class LogNamesResource {

    protected ServiceLocator habitat = Globals.getDefaultBaseServiceLocator();

    @GET
    @Produces({ MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON })
    public String getLogNamesJSON(@QueryParam("instanceName") String instanceName) throws IOException {
        return getLogNames(instanceName, "json");
    }

    @GET
    @Produces({ MediaType.APPLICATION_XML })
    public String getLogNamesJXML(@QueryParam("instanceName") String instanceName) throws IOException {
        return getLogNames(instanceName, "xml");
    }

    private String getLogNames(String instanceName, String type) throws IOException {

        if (habitat.getService(LogManager.class) == null) {
            //the logger service is not install, so we cannot rely on it.
            //return an error
            throw new IOException("The GlassFish LogManager Service is not available. Not installed?");
        }

        LogFilter logFilter = habitat.getService(LogFilter.class);

        return convertQueryResult(logFilter.getInstanceLogFileNames(instanceName), type);

    }

    private String quoted(String s) {
        return "\"" + s + "\"";
    }

    private String convertQueryResult(Vector v, String type) {
        StringBuilder sb = new StringBuilder();
        String sep = "";
        if (type.equals("json")) {
            sb.append("{\"InstanceLogFileNames\": [");
        } else {
            sb.append("<InstanceLogFileNames>\n");
        }

        // extract every record
        for (int i = 0; i < v.size(); ++i) {
            String name = (String) v.get(i);

            if (type.equals("json")) {
                sb.append(sep);
                sb.append(quoted(name));
                sep = ",";
            } else {
                sb.append("<" + name + "/>");

            }

        }
        if (type.equals("json")) {
            sb.append("]}\n");
        } else {
            sb.append("\n</InstanceLogFileNames>\n");

        }

        return sb.toString();
    }
}
