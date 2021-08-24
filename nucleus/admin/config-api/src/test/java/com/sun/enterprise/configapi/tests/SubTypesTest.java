/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Domain;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Dom;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.junit.jupiter.api.Assertions.assertAll;


/**
 * User: Jerome Dochez
 * Date: Mar 20, 2008
 * Time: 2:44:48 PM
 */
public class SubTypesTest extends ConfigApiTest {

    // not testing all the sub types, just a few to be sure it works ok.
    private static final Class<?>[] expectedClassNames = {
        com.sun.enterprise.config.serverbeans.Applications.class,
        com.sun.enterprise.config.serverbeans.Configs.class,
        com.sun.enterprise.config.serverbeans.Clusters.class
    };


    @Override
    public String getFileName() {
        return "DomainTest";
    }

    @Test
    public void testSubTypesOfDomain() throws Exception {
        Domain domain = super.getHabitat().getService(Domain.class);
        Class<?>[] subTypes = ConfigSupport.getSubElementsTypes((ConfigBean) Dom.unwrap(domain));
        assertAll(
            () -> assertThat(subTypes.length, equalTo(12)),
            () -> assertThat(Set.of(subTypes), hasItems(expectedClassNames))
        );
    }
}
