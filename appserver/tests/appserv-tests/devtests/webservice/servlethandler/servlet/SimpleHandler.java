/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package servlet;

import java.util.Date;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.*;

public class SimpleHandler extends GenericHandler {

    protected HandlerInfo info = null;

    public void init(HandlerInfo info) {
        this.info = info;
    }

    public boolean handleRequest(MessageContext context) {
        try {
            Date startTime = new Date();
            context.setProperty("startTime", startTime);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean handleResponse(MessageContext context) {
        try {
            Date startTime = (Date) context.getProperty("startTime");
            Date endTime = new Date();
            long elapsed = endTime.getTime() - startTime.getTime();
            System.out.println(" in handler, elapsed " + elapsed);
        } catch(Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public QName[] getHeaders() {
        return new QName[0];
    }
}
