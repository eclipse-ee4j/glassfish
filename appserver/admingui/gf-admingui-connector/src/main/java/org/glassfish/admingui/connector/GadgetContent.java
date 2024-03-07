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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;


/**
 *  <p>        This class is configured via XML.  This is done via the HK2
 * <code>ConfigParser</code>.</p>
 *
 *  FIXME:
 *  <p>        NOTE: THIS CLASS IS NOT BEING USED BECAUSE I CANNOT FIGURE OUT HOW
 * TO GET BOTH THE BODY CONTENT AND HAVE THIS CLASS GET IT'S VALUES!!</p>
 *
 *  @author Ken Paulsen        (ken.paulsen@sun.com)
 */
@Configured(name="Content")
public class GadgetContent {

    /**
     * <p> A unique identifier for the content.</p>
     */
    public String getType() {
        return this.type;
    }

    /**
     * <p> Setter for the type.</p>
     */
    @Attribute(value="type", required=false)
    void setType(String type) {
        this.type = type;
    }

    private String type;

    /**
     * <p> A unique identifier for the content.</p>
     */
    public String getHref() {
        return this.href;
    }

    /**
     * <p> Setter for the href.</p>
     */
    @Attribute(value="href", required=false)
    void setHref(String href) {
        this.href = href;
    }

    private String href;
}
