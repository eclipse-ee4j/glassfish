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
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
@Configured(name="toc")
public class TOC {
    /**
     * <p> Accessor for child {@link TOCItem}s.</p>
     */
    public List<TOCItem> getTOCItems() {
        return this.tocItems;
    }

    /**
     * <p> {@link IntegrationPoint}s setter.</p>
     */
    @Element("tocitem")
    public void setTOCItems(List<TOCItem> tocItems) {
        this.tocItems = tocItems;
    }

    /**
     *
     */
    public String getVersion() {
        return this.version;
    }

    /**
     *
     */
    @Attribute(required=true)
    public void setVersion(String version) {
        this.version = version;
    }


    private String version;
    private List<TOCItem> tocItems;
}
