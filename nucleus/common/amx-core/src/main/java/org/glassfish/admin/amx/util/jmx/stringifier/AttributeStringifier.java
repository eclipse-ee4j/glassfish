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

/*
 * $Header: /cvs/glassfish/appserv-api/src/java/com/sun/appserv/management/util/jmx/stringifier/AttributeStringifier.java,v 1.2 2007/05/05 05:31:04 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2007/05/05 05:31:04 $
 */

package org.glassfish.admin.amx.util.jmx.stringifier;

import javax.management.Attribute;

import org.glassfish.admin.amx.util.stringifier.SmartStringifier;
import org.glassfish.admin.amx.util.stringifier.Stringifier;



public final class AttributeStringifier implements Stringifier {

    public final static AttributeStringifier DEFAULT = new AttributeStringifier();

    public AttributeStringifier() {
    }


    @Override
    public String stringify(Object o) {
        final Attribute attr = (Attribute) o;

        final String prefix = attr.getName() + "=";

        final String stringValue = SmartStringifier.toString(attr.getValue());

        return (prefix + stringValue);
    }
}
