/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.remote.writer;

import com.sun.enterprise.admin.remote.ParamsWithPayload;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.glassfish.api.admin.ParameterMap;

/**
 * Writes ParameterMap into the POST
 *
 * @author mmares
 * @author Marek Potociar (marek.potociar at oracle.com)
 */
public class ParameterMapFormProprietaryWriter implements ProprietaryWriter {

    @Override
    public void writeTo(Object entity, HttpURLConnection urlConnection) throws IOException {
        ParameterMap pm;
        if (entity instanceof ParameterMap) {
            pm = (ParameterMap) entity;
        } else if (entity instanceof ParamsWithPayload) {
            pm = ((ParamsWithPayload) entity).getParameters();
        } else {
            pm = new ParameterMap();
        }
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, List<String>> entry : pm.entrySet()) {
            for (String value : entry.getValue()) {
                if (sb.length() > 0) {
                    sb.append('&');
                }
                sb.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                if (value != null) {
                    sb.append('=');
                    sb.append(URLEncoder.encode(value, "UTF-8"));
                }
            }
        }
        urlConnection.getOutputStream().write(sb.toString().getBytes("UTF-8"));
    }

    @Override
    public boolean isWriteable(Object entity) {
        if (entity instanceof ParameterMap) {
            return true;
        }
        if (entity instanceof ParamsWithPayload) {
            ParamsWithPayload pwp = (ParamsWithPayload) entity;
            return pwp.getPayloadOutbound() == null || pwp.getPayloadOutbound().size() == 0;
        }
        return false;
    }

}
