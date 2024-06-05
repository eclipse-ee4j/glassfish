/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.glassfish.admin.rest.resources.TemplateCommandPostResource;
import org.glassfish.api.admin.ParameterMap;
import org.jvnet.hk2.config.Dom;

/**
 *
 * @author jasonlee
 */
public class SetDomainConfigResource extends TemplateCommandPostResource {
    public SetDomainConfigResource() {
        super("SetDomainConfigResource", "set", "POST", "commandAction", "set", false);
    }

    public void setEntity(Dom p) {
        // ugly no-op hack. For now.
    }

    @POST
    @Produces({ "text/html", MediaType.APPLICATION_JSON + ";qs=0.5", MediaType.APPLICATION_XML + ";qs=0.5" })
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.APPLICATION_FORM_URLENCODED })
    public Response setDomainConfig(HashMap<String, String> data) {

        final Iterator<Entry<String, String>> iterator = data.entrySet().iterator();
        if (iterator.hasNext()) {
            ParameterMap fixed = new ParameterMap();
            Map.Entry entry = iterator.next();
            fixed.add("DEFAULT", entry.getKey() + "=" + entry.getValue());

            return super.executeCommandLegacyFormat(fixed);
        }

        throw new RuntimeException("You must supply exactly one configuration option."); //i18n
    }

}
