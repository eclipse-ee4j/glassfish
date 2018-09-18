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

import org.codehaus.jettison.json.JSONException;
import org.glassfish.admin.rest.composite.metadata.RestResourceMetadata;
import org.glassfish.admin.rest.composite.resource.DummiesResource;
import org.glassfish.admin.rest.composite.resource.DummyResource;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 * @author jdlee
 */
public class ResourceMetadataTest  {
    @Test
    public void testMetadata() throws JSONException {
        RestResourceMetadata rrmd = new RestResourceMetadata(new DummiesResource());
        Assert.assertNotNull(rrmd);
        Assert.assertEquals(rrmd.getResourceMethods().size(), 3);
        Assert.assertEquals(rrmd.getSubResources().size(), 1);

        rrmd = new RestResourceMetadata(new DummyResource());
        Assert.assertNotNull(rrmd);
        Assert.assertEquals(rrmd.getResourceMethods().size(), 3);
        Assert.assertEquals(rrmd.getSubResources().size(), 0);
    }
}
