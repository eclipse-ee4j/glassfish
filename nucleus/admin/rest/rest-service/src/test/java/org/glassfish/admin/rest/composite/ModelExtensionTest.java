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

package org.glassfish.admin.rest.composite;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.glassfish.admin.rest.composite.metadata.Default;
import org.glassfish.admin.rest.composite.metadata.DefaultsGenerator;
import org.glassfish.admin.rest.composite.metadata.RestMethodMetadata;
import org.glassfish.admin.rest.composite.metadata.RestResourceMetadata;
import org.glassfish.admin.rest.model.BaseModel;
import org.glassfish.admin.rest.model.ModelExt1;
import org.glassfish.admin.rest.model.ModelExt2;
import org.glassfish.admin.rest.model.RelatedModel;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jdlee
 */
@Test
public class ModelExtensionTest {
    @Test
    public void testNestedModels() {
        BaseModel model = CompositeUtil.instance().getModel(BaseModel.class);
        List<RelatedModel> related = model.getRelated();
        Assert.assertNull(related);

        RelatedModel rm = CompositeUtil.instance().getModel(RelatedModel.class);
        rm.setId("1");
        rm.setDescription("test");
        related = new ArrayList<RelatedModel>();
        related.add(rm);
        model.setRelated(related);

        related = model.getRelated();
        Assert.assertEquals(related.size(), 1);
    }

    @Test
    public void testModelExtension() {
        BaseModel model = CompositeUtil.instance().getModel(BaseModel.class);
        Assert.assertTrue(ModelExt1.class.isAssignableFrom(model.getClass()));
        Assert.assertTrue(ModelExt2.class.isAssignableFrom(model.getClass()));
    }

    public void testModelInheritance() throws JSONException {
        Model1 m1 = CompositeUtil.instance().getModel(Model1.class);
        Model2 m2 = CompositeUtil.instance().getModel(Model2.class);

        Assert.assertNotNull(m1);
        Assert.assertNotNull(m2);

        RestResourceMetadata rrmd = new RestResourceMetadata(new TestResource());
        final List<RestMethodMetadata> getMethods = rrmd.getResourceMethods().get("GET");
        JSONObject name = getJsonObject(getMethods.get(0).toJson(), "response.properties.name");

        Assert.assertNotNull(name, "'name' should not be null. Inherited methods are not showing up in generated class");
        Assert.assertNotNull(name.get("default"), "The field 'name' should have a default value.");
    }

    // Works with no dot?
    private JSONObject getJsonObject(JSONObject current, String dottedName) {
        Assert.assertNotNull(dottedName);
        String[] parts = dottedName.split("\\.");

        for (String part : parts) {
            try {
                current = (JSONObject)current.get(part);
            } catch (JSONException e) {
                current = null;
                break;
            }
        }

        return current;
    }

    public static class TestResource extends LegacyCompositeResource {
        @GET
        public Model2 getModel() {
            Model2 m2 = CompositeUtil.instance().getModel(Model2.class);

            return m2;
        }
    }

    public static interface Model1 extends RestModel {
        @Default(generator=ModelDefaultGenerator.class)
        String getName();
        void setName(String name);
    }

    public static interface Model2 extends Model1 {
        @Default(generator=ModelDefaultGenerator.class)
        String getName2();
        void setName2(String name);
    }

    public static class ModelDefaultGenerator implements DefaultsGenerator {

        @Override
        public Object getDefaultValue(String propertyName) {
            return "defaultData";
        }

    }
}
