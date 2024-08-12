/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admingui.connector;

import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;


/**
 *  <p>        This class is configured via XML (i.e. a console-config.xml file).
 *   This is done via the HK2 <code>ConfigParser</code>.</p>
 *
 *  <p>        Each module that wishes to provide an integration with the GlassFish
 * admin console should provide a <code>console-config.xml</code> file
 * which provides all the {@link IntegrationPoint} information for the
 * module.  Here is an example of what that file might look like:</p>
 *
 *  <p><code><pre>
 * <?xml version="1.0" encoding="UTF-8"?>
 *
 * <console-config id="uniqueId">
 *     <integration-point id="someId" type="tree" priority="840"
 *             parentId="rootNode" content="/myTreeNode.jsf" />
 *     <integration-point id="anotherId" type="webApplicationTab"
 *             priority="22" parentId="appTab" content="/myTab.jsf" />
 *     <integration-point id="fooId" type="tree" priority="400"
 *             parentId="appNode" content="/fooNode.jsf" />
 * </console-config>
 *
 *  </pre></code></p>
 *
 *  <p>        Normally a <code>console-config.xml</code> file should exist at
 * "<code>META-INF/admingui/console-config.xml</code>" inside your module
 * jar file.</p>
 *
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
@Configured
public class ConsoleConfig {
    /**
     * <p> Accessor for the known Admin Console
     *     {@link IntegrationPoint}s.<?p>
     */
    public List<IntegrationPoint> getIntegrationPoints() {
        return this.integrationPoints;
    }

    /**
     * <p> {@link IntegrationPoint}s setter.</p>
     */
    @Element("integration-point")
    void setIntegrationPoints(List<IntegrationPoint> integrationPoints) {
        this.integrationPoints = integrationPoints;
    }

    /**
     * <p> A unique identifier for the ConsoleConfig instance.</p>
     */
    public String getId() {
        return this.id;
    }

    /**
     * <p> Setter for the id.</p>
     */
    @Attribute(required=true)
    void setId(String id) {
        this.id = id;
    }

    private String id;
    private List<IntegrationPoint> integrationPoints;
}
