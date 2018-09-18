/*
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

package org.glassfish.admin.rest;

import java.util.Locale;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.CompositeUtil;
import org.glassfish.admin.rest.model.BaseModel;
import org.glassfish.admin.rest.utils.JsonUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jdlee
 */
public class JsonUtilTest {
    @Test
    public void testArrayEncoding() throws JSONException {
        Locale locale = null;
        BaseModel model = CompositeUtil.instance().getModel(BaseModel.class);
        model.setStringArray(new String[] {"one", "two"});
        JSONObject json = (JSONObject)JsonUtil.getJsonObject(model);
        Assert.assertNotNull(json);
        Object o = json.get("stringArray");
        Assert.assertTrue(o instanceof JSONArray);
        JSONArray array = (JSONArray)o;
        Assert.assertEquals(array.length(), 2);
        Assert.assertTrue(contains(array, "one"));
        Assert.assertTrue(contains(array, "two"));

        BaseModel model2 = CompositeUtil.instance().unmarshallClass(locale, BaseModel.class, json);
        Assert.assertNotNull(model2);
        Assert.assertNotNull(model2.getStringArray());
        Assert.assertEquals(2, model2.getStringArray().length);
    }

    private boolean contains(JSONArray array, String text) throws JSONException {
        for (int i = 0, len = array.length(); i < len; i++) {
            if (text.equals(array.get(i))) {
                return true;
            }
        }

        return false;
    }
}
