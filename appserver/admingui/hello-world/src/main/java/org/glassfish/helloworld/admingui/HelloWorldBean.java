/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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
package org.glassfish.helloworld.admingui;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 * CDI backing bean for the Hello World JSF page in GlassFish Admin Console.
 * This demonstrates how to create modern Jakarta Faces pages within the legacy
 * Admin Console framework. The key aspects:
 * 1. Uses CDI (@Named, @RequestScoped) instead of JSF managed beans
 * 2. Works seamlessly with the existing console infrastructure
 * 3. Can be injected with HK2 services via CDI producers if needed
 * 4. Follows standard JSF patterns for data binding and actions
 * The bean is automatically discovered by CDI due to the implicit bean archive
 * (no beans.xml needed) and made available to JSF via the @Named annotation.
 */
@Named
@RequestScoped
public class HelloWorldBean {

    /**
     * Returns the main greeting message displayed on the page.
     * This method is called via EL expression #{helloWorldBean.message}
     * in the XHTML template. The property name follows JavaBean conventions
     * (getMessage() -> message property).
     * @return the greeting message
     */
    public String getMessage() {
        return "Hello World from GlassFish Admin Console!";
    }

    /**
     * Returns a description of what this page demonstrates.
     * Called via EL expression #{helloWorldBean.description} to show
     * additional information about the JSF integration.
     * @return the description text
     */
    public String getDescription() {
        return "This is a JSF page created using Jakarta Faces in the GlassFish Admin Console.";
    }
}
