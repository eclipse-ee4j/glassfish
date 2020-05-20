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

package connector;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionMetaData;
import jakarta.resource.cci.Interaction;
import jakarta.resource.cci.LocalTransaction;
import jakarta.resource.cci.ResultSetInfo;

public class MyConnection implements Connection {

    @Override
    public void close() throws ResourceException {
        // TODO Auto-generated method stub

    }

    @Override
    public Interaction createInteraction() throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ConnectionMetaData getMetaData() throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ResultSetInfo getResultSetInfo() throws ResourceException {
        // TODO Auto-generated method stub
        return null;
    }

}
