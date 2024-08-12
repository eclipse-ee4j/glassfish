/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.provider;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.util.Collection;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.glassfish.admin.rest.Constants;

/**
 *
 * @author jdlee
 */
@Produces(Constants.MEDIA_TYPE_JSON)
public class CollectionWriter extends BaseProvider<Collection> {
    public CollectionWriter() {
        super(Collection.class, Constants.MEDIA_TYPE_JSON_TYPE);
    }

    @Override
    public String getContent(Collection proxy) {
        try {
            JSONArray array = new JSONArray(proxy);
            return array.toString(getFormattingIndentLevel());
        } catch (JSONException ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
