/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.model;

import com.sun.webui.jsf.util.MessageUtil;

/**
 * A default list of options, pre-populated with three default items.
 */
public class DefaultOptionsList extends OptionsList {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -1377760857272606790L;

    /**
     * Create a new instance.
     */
    public DefaultOptionsList() {
        String bundle = DefaultOptionsList.class.getPackage().getName()
                + ".Bundle";
        Option[] options = new Option[]{
            new Option("item1", MessageUtil.getMessage(bundle, "item1")),
            new Option("item2", MessageUtil.getMessage(bundle, "item2")),
            new Option("item3", MessageUtil.getMessage(bundle, "item3"))
        };
        this.setOptions(options);
    }
}
