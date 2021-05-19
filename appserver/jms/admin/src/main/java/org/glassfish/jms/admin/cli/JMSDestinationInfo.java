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

package org.glassfish.jms.admin.cli;

import java.io.Serializable;
import java.util.Properties;


/**
 * A class representing the <code> information </code> on one JMS destination
 * created by the JMS provider.
 *
 * @author Isa Hashim
 * @version 1.0
 */
public class JMSDestinationInfo implements Serializable {

    private String destName, destType;
    private Properties attrs;

    public JMSDestinationInfo(String destName, String destType) {
        // todo: need to enabel this
        // ArgChecker.checkValid(destName, "destName",
        // StringValidator.getInstance()); //noi18n
        // ArgChecker.checkValid(destType, "destType",
        // StringValidator.getInstance()); //noi18n

        this.destName = destName;
        this.destType = destType;
        this.attrs = new Properties();
    }


    public JMSDestinationInfo(String destName, String destType, Properties attrs) {
        this(destName, destType);
        // ArgChecker.checkValid(attrs, "attrs"); //noi18n
        this.attrs = attrs;
    }


    public String getDestinationName() {
        return (destName);
    }


    public String getDestinationType() {
        return (destType);
    }


    public Properties getAttrs() {
        return (attrs);
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(destName);
        sb.append(' ');
        sb.append(destType);
        sb.append(' ');
        sb.append(attrs);
        return sb.toString();
    }
}
