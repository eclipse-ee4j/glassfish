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

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.cci.Connection;
import javax.resource.cci.ConnectionFactory;
import javax.resource.cci.ConnectionSpec;
import javax.resource.cci.RecordFactory;
import javax.resource.cci.ResourceAdapterMetaData;
import javax.resource.spi.ConnectionManager;

public class MyConnectionFactory implements ConnectionFactory {

    private static final long serialVersionUID = -8947169718238922386L;
    private ConnectionManager cm;
    private MyManagedConnectionFactory mcf;
    private Reference ref;
    
    public MyConnectionFactory(MyManagedConnectionFactory mcf, ConnectionManager cm) {
        super();
        this.mcf = mcf;
        this.cm = cm;
    }

    @Override
    public void setReference(Reference ref) {

    }

    @Override
    public Reference getReference() throws NamingException {
        return ref;
    }

    @Override
    public Connection getConnection() throws ResourceException {
        return new MyConnection();
    }

    @Override
    public Connection getConnection(ConnectionSpec spec)  throws ResourceException {
        return new MyConnection();
    }

    @Override
    public ResourceAdapterMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public RecordFactory getRecordFactory() throws ResourceException {
        return null;
    }

}
