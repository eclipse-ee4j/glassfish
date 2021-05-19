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

package org.glassfish.admin.amx.util.stringifier;

import java.security.Provider;

/**
 * Stringifies a java.security.Provider.
 */
public final class ProviderStringifier implements Stringifier {

    public final static ProviderStringifier DEFAULT = new ProviderStringifier();

    public ProviderStringifier() {
    }


    @Override
    public String stringify(Object object) {
        final Provider provider = (Provider) object;

        final StringBuffer buf = new StringBuffer();

        buf.append(provider.getInfo());

        java.util.Iterator iter = provider.entrySet().iterator();
        while (iter.hasNext()) {
            buf.append(iter.next().toString() + "\n");
        }

        return (buf.toString());
    }
}
