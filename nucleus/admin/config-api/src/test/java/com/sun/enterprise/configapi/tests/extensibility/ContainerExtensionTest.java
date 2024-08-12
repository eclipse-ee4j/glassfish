/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.configapi.tests.extensibility;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.configapi.tests.example.AnApplicationExtension;
import com.sun.enterprise.configapi.tests.example.RandomContainer;
import com.sun.enterprise.configapi.tests.example.RandomElement;
import com.sun.enterprise.configapi.tests.example.RandomExtension;

import jakarta.inject.Inject;

import java.util.List;

import org.glassfish.api.admin.config.Container;
import org.glassfish.config.api.test.ConfigApiJunit5Extension;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.tests.utils.junit.DomainXml;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Jerome Dochez
 */
@ExtendWith(ConfigApiJunit5Extension.class)
@DomainXml("Extensibility.xml")
public class ContainerExtensionTest {

    @Inject
    private ServiceLocator locator;

    @Test
    public void existenceTest() {
        Config config = locator.<Domain>getService(Domain.class).getConfigs().getConfig().get(0);
        List<Container> containers = config.getContainers();
        assertThat(containers, hasSize(2));
        RandomContainer container = (RandomContainer) containers.get(0);
        assertEquals("random", container.getName());
        assertEquals("1243", container.getNumberOfRuntime());
        RandomElement element = container.getRandomElement();
        assertNotNull(element);
        assertEquals("foo", element.getAttr1());
    }

    @Test
    public void extensionTest() {
        Config config = locator.<Domain>getService(Domain.class).getConfigs().getConfig().get(0);
        RandomExtension extension = config.getExtensionByType(RandomExtension.class);
        assertNotNull(extension);
        assertEquals("foo", extension.getSomeAttribute());
    }

    @Test
    public void applicationExtensionTest() {
        Application a = locator.getService(Application.class);
        List<AnApplicationExtension> taes = a.getExtensionsByType(AnApplicationExtension.class);
        assertThat(taes, hasSize(2));
    }
}
