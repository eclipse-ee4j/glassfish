/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging.logviewer.backend;
import javax.management.Attribute;
import javax.management.AttributeList;

/**
 * LogRecordTemplate will be the first entry in the results that we return
 * back to the Admin Tool LogViewer Front end. It contains the metadata
 * describing the structure of the results.
 *
 * @AUTHOR: Hemanth Puttaswamy
 *
 */
public class LogRecordTemplate {
    private static AttributeList template =
        new AttributeList( );

    private static Attribute templateAttribute;

    static {
        template.add( new Attribute( "FIELD0", "Record Number" ) );
        template.add( new Attribute( "FIELD1", "Logged DateTime" ) );
        template.add( new Attribute( "FIELD2", "Logged Level" ) );
        template.add( new Attribute( "FIELD3", "Product Name" ) );
        template.add( new Attribute( "FIELD4", "Logger Name" ) );
        template.add( new Attribute( "FIELD5", "Name Value Pairs" ) );
        template.add( new Attribute( "FIELD6", "Message" ) );

        templateAttribute = new Attribute( "Header", template );
    }

    static Attribute getHeader( ) {
        return templateAttribute;
    }
}
