/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.connectors.config;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;

import org.glassfish.hk2.api.ServiceLocator;
import org.junit.Test;
import static org.junit.Assert.assertTrue;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.Dom;

import java.util.List;
import java.util.logging.Logger;


/**
 * Test the Document allModelsImplementing API.
 */
public class AllModelsImplementingTest extends ConfigApiTest {

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void checkResources() throws ClassNotFoundException {

        ServiceLocator habitat = getHabitat();
        Resources resources = habitat.<Domain>getService(Domain.class).getResources();
        Dom dom = Dom.unwrap(resources);
        List <ConfigModel> models = dom.document.getAllModelsImplementing(Resource.class);
        for (ConfigModel model : models) {
            Logger.getAnonymousLogger().fine(model.targetTypeName);
        }
        assertTrue(models.size()>0);

    }

}
