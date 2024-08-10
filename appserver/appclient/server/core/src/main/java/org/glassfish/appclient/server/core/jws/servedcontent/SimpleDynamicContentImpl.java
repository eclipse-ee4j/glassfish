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

package org.glassfish.appclient.server.core.jws.servedcontent;

import java.util.Properties;

import org.glassfish.appclient.server.core.jws.Util;

/**
 *
 * @author Tim
 */
public class SimpleDynamicContentImpl extends Content.Adapter implements DynamicContent {

    private final String template;
    private final String mimeType;
    private final boolean isMain;

    private Instance instance = null;

    public SimpleDynamicContentImpl(final String template, final String mimeType) {
        this(template, mimeType, false);
    }

    public SimpleDynamicContentImpl(final String template, final String mimeType, final boolean isMain) {
        this.template = template;
        this.mimeType = mimeType;
        this.isMain = isMain;
    }

    public Instance getExistingInstance(Properties tokenValues) {
        return getOrCreateInstance(tokenValues, false);
    }

    public Instance getOrCreateInstance(Properties tokenValues) {
        return getOrCreateInstance(tokenValues, true);
    }

    private Instance getOrCreateInstance(final Properties tokenValues,
            final boolean createIfAbsent) {
//        if (instance == null && createIfAbsent) {
            instance = new DynamicContent.InstanceAdapter(
                    Util.replaceTokens(template, tokenValues));
//        }
        return instance;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public boolean isMain() {
        return isMain;
    }

    @Override
    public String toString() {
        return (instance == null ? "null" : instance.getText());
    }



}
