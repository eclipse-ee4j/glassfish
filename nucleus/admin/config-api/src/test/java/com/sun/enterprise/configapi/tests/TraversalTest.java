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

package com.sun.enterprise.configapi.tests;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.Dom;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;
import java.util.logging.Logger;

import com.sun.enterprise.config.serverbeans.Domain;

/**
 * Traverse a config tree using the hk2 model APIs.
 *
 * @author Jerome Dochez
 */
public class TraversalTest extends ConfigApiTest {

    static Logger logger = Logger.getAnonymousLogger();

    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void traverse() {
        ServiceLocator habitat = super.getHabitat();
        Domain domain = Domain.class.cast(habitat.<Domain>getService(Domain.class));
        introspect(0, Dom.unwrap(domain));
    }


    @Ignore
    private void introspect(int indent, Dom proxy) {
        indent = indent + 1;
        Set<String> ss = proxy.getAttributeNames();
        String id = "";
        for (int i = 0; i < indent; i++) {
            id = id + "    ";
        }
        logger.fine(id + "--------" + proxy.model.key);
        for (String a : ss) {

            logger.fine(id + a + "=" + proxy.attribute(a));
        }


        Set<String> elem = proxy.getElementNames();

        for (String bb : elem) {


            logger.fine(id + "<" + bb + ">");
            org.jvnet.hk2.config.ConfigModel.Property prop = proxy.model.getElement(bb);
            if (prop != null && proxy.model.getElement(bb).isLeaf()) {
                logger.fine(proxy.leafElement(bb));
            } else {
                introspect(indent, proxy.element(bb));
            }
            logger.fine(id + "</" + bb + ">");
            logger.fine("    ");

        }
    }
}
