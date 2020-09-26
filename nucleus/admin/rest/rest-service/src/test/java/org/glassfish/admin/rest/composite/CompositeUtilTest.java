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

package org.glassfish.admin.rest.composite;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.customvalidators.ReferenceConstraint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;
import jakarta.validation.ConstraintViolation;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.metadata.AttributeReference;
import org.glassfish.admin.rest.model.BaseModel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jdlee
 */
public class CompositeUtilTest {
    private static String json =
            "{\"name\":\"testModel\",\"count\":123, \"related\":[{\"id\":\"rel1\", \"description\":\"description 1\"},{\"id\":\"rel2\", \"description\":\"description 2\"}]}";

    @Test
    public void modelGeneration() {
        BaseModel model = CompositeUtil.instance().getModel(BaseModel.class);
        Assert.assertNotNull(model);
    }

    @Test
    public void readInJson() throws Exception {
        Locale locale = null;
        JSONObject o = new JSONObject(json);
        BaseModel model = CompositeUtil.instance().unmarshallClass(locale, BaseModel.class, o);

        Assert.assertEquals(model.getName(), "testModel");
        Assert.assertEquals(model.getRelated().size(), 2);
        Assert.assertTrue(model.getRelated().get(0).getDescription().startsWith("description "));
    }

    @Test
    public void testBeanValidationSupport() {
        Locale locale = null;
        final CompositeUtil cu = CompositeUtil.instance();
        BaseModel model = cu.getModel(BaseModel.class);
        model.setName(null); // Redundant, but here for emphasis
        model.setSize(16); // Must be between 10 and 15, inclusive
        model.setConfigRef(null); // Not null. Validation pulled in from the ConfigBean

        Set<ConstraintViolation<BaseModel>> violations = cu.validateRestModel(locale, model);
        Assert.assertEquals(3, violations.size());
    }

    @Test
    public void testAttributeReferenceProcessing() throws Exception {
        final CompositeUtil cu = CompositeUtil.instance();
        BaseModel model = cu.getModel(BaseModel.class);
        
        final Method clusterMethod = Cluster.class.getMethod("getConfigRef");
        final Method modelMethod = model.getClass().getDeclaredMethod("getConfigRef");

        Annotation[] fromCluster = clusterMethod.getAnnotations();
        Annotation[] fromRestModel = modelMethod.getAnnotations();

        Assert.assertEquals(fromCluster.length, fromRestModel.length);
        Assert.assertEquals(clusterMethod.getAnnotation(ReferenceConstraint.RemoteKey.class).message(),
                            modelMethod.getAnnotation(ReferenceConstraint.RemoteKey.class).message());
    }

    @Test
    public void testDirtyFieldDetection() throws JSONException {
        Locale locale = null;
        JSONObject o = new JSONObject(json);
        BaseModel model = CompositeUtil.instance().unmarshallClass(locale, BaseModel.class, o);
        RestModel rmi = (RestModel)model;

        Assert.assertTrue(rmi.isSet("name"));
        Assert.assertTrue(rmi.isSet("count"));
        Assert.assertTrue(rmi.isSet("related"));
        Assert.assertFalse(rmi.isSet("size"));
    }
}
