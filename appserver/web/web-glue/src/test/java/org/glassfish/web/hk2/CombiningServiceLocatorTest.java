/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.web.hk2;


import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevelController;
import org.glassfish.hk2.runlevel.RunLevelServiceUtilities;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 *
 * @author Ondro Mihalyi
 */
public class CombiningServiceLocatorTest {

    TestInfo testInfo;

    @BeforeEach
    void init(TestInfo testInfo) {
        this.testInfo = testInfo;
    }

    @Test
    void testGetServiceWithLocalContext() {
        ServiceLocator parentLocator = parentLocatorWithLocalScopeServiceAndPublicServices(ServiceWithLocalScope.class, PublicService.class, ChildService.class);
        ServiceLocator childLocator = childLocatorWithServices(parentLocator, ChildService.class);

        ServiceWithLocalScope serviceFromParent = parentLocator.getService(ServiceWithLocalScope.class);
        assertEquals(parentLocator, serviceFromParent.getLocator());

        assertThrows(MultiException.class, () -> childLocator.getService(ServiceWithLocalScope.class), "Child locator shouldn't return service local to parent");

        CombiningServiceLocator combiningLocator = new CombiningServiceLocator(childLocator, parentLocator);
        ServiceWithLocalScope parentLocalService = combiningLocator.getService(ServiceWithLocalScope.class);

        Assertions.assertNotNull(parentLocalService, "Service with context local to the parent locator available from the combining locator");
        assertEquals(parentLocator, serviceFromParent.getLocator(), "The service local to parent should be asociated to the parent locator, not to the combining locator");

        PublicService publicService = childLocator.getService(PublicService.class);

        Assertions.assertNotNull(publicService);
        assertEquals(parentLocator, publicService.getLocator(), "Services created by parent locator should be associated to the parent locator, not to the child locator");

        ChildService childService = childLocator.getService(ChildService.class);

        Assertions.assertNotNull(childService);
        assertEquals(childLocator, childService.getLocator(), "Services available both in child and parent locators should be associated to the child locator which takes precedence");
    }

    @Test
    void testGetAllServicesWithLocalContext() {
        ServiceLocator parentLocator = parentLocatorWithLocalScopeServiceAndPublicServices(ServiceWithLocalScope.class, PublicService.class);
        ServiceLocator childLocator = childLocatorWithServices(parentLocator, PublicService.class);
        CombiningServiceLocator combiningLocator = new CombiningServiceLocator(childLocator, parentLocator);

        assertEquals(1, parentLocator.getAllServices(ServiceWithLocalScope.class).size(),
                "Parent locator should contain 1 " + ServiceWithLocalScope.class.getSimpleName() + " service");

        assertThrows(MultiException.class, () -> childLocator.getAllServices(ServiceWithLocalScope.class),
                "Child locator shouldn't have access to " + ServiceWithLocalScope.class.getSimpleName() + " service");

        assertEquals(1, combiningLocator.getAllServices(ServiceWithLocalScope.class).size(), "Combined locator should contain 1 " + ServiceWithLocalScope.class.getSimpleName() + " service");

        assertEquals(1, parentLocator.getAllServices(PublicService.class).size(),
                "Parent locator should contain 1 " + PublicService.class.getSimpleName() + " service");

        assertEquals(2, childLocator.getAllServices(PublicService.class).size(),
                "Child locator should contain 2 " + PublicService.class.getSimpleName() + " services (one from parent, one from child)");

        assertEquals(2, combiningLocator.getAllServices(PublicService.class).size(),
                "Combined locator should contain 2 " + PublicService.class.getSimpleName() + " services (one from parent, one from child)");

    }

    private ServiceLocator parentLocatorWithLocalScopeServiceAndPublicServices(Class<ServiceWithLocalScope> localServiceClass, Class<?>... publicServiceClasses) {
        ServiceLocator locator = LocatorHelper.create(createLocatorName("parent"), configuration -> {
            configuration.bind(BuilderHelper.createDescriptorFromClass(localServiceClass));
            for (Class<?> cls : publicServiceClasses) {
                configuration.bind(BuilderHelper.createDescriptorFromClass(cls));
            }
        });
        proceedToRunLevel(0, locator);
        return locator;
    }

    private ServiceLocator childLocatorWithServices(ServiceLocator parentLocator, Class<?>... classes) {
        return LocatorHelper.create(createLocatorName("child"), parentLocator, configuration -> {
            for (Class<?> cls : classes) {
                configuration.bind(BuilderHelper.createDescriptorFromClass(cls));
            }
        });
    }

    private String createLocatorName(String suffix) {
        return testInfo.getTestClass().get().getSimpleName() + "::" + testInfo.getTestMethod().get().getName() + "-" + suffix;
    }
    private void proceedToRunLevel(int level, ServiceLocator parentLocator) {
        RunLevelServiceUtilities.enableRunLevelService(parentLocator);
        RunLevelController runLevelService = parentLocator.getService(RunLevelController.class);
        runLevelService.proceedTo(level);
    }

}
