/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.testing;

public class ResponseBody {

    private boolean ignoreExtra = false;

    private boolean isIgnoreExtra() {
        return this.ignoreExtra;
    }

    public ResponseBody ignoreExtra(boolean val) {
        this.ignoreExtra = val;
        return this;
    }

    public ResponseBody ignoreExtra() {
        return ignoreExtra(true);
    }

    ArrayValue items;

    private ArrayValue getItems() {
        return this.items;
    }

    public ResponseBody items(ArrayValue val) {
        this.items = val;
        return this;
    }

    ObjectValue item;

    private ObjectValue getItem() {
        return this.item;
    }

    public ResponseBody item(ObjectValue val) {
        this.item = val;
        return this;
    }

    ArrayValue resources;

    private ArrayValue getResources() {
        return this.resources;
    }

    public ResponseBody resources(ArrayValue val) {
        this.resources = val;
        return this;
    }

    ArrayValue messages;

    private ArrayValue getMessages() {
        return this.messages;
    }

    public ResponseBody messages(ArrayValue val) {
        this.messages = val;
        return this;
    }

    public ObjectValue toObjectVal() {
        ObjectValue val = Common.objectVal();
        if (getItem() != null) {
            val.put("item", getItem());
        }
        if (getItems() != null) {
            val.put("items", getItems());
        }
        if (getResources() != null) {
            val.put("resources", getResources());
        }
        if (getMessages() != null) {
            val.put("messages", getMessages());
        }
        return val.ignoreExtra(isIgnoreExtra());
    }
}
