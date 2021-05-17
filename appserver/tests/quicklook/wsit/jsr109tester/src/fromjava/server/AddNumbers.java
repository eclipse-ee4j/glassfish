/*
 * Copyright (c) 2014, 2018 Oracle and/or its affiliates. All rights reserved.
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

package fromjava.server;

import javax.xml.stream.XMLInputFactory;

/**
 * Simple Webservice to be deployed using jsr109
 */
@jakarta.jws.WebService
public class AddNumbers {

    public int addNumbers (int number1, int number2) {

        // verify the stax tck property is set on woodstox by default
        if (!(Boolean)XMLInputFactory.newFactory().getProperty("com.ctc.wstx.returnNullForDefaultNamespace")) {
            throw new IllegalStateException("Woodstox does not have required stax tck com.ctc.wstx.returnNullForDefaultNamespace property set.");
        }

        return number1 + number2;
    }

}
