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

package com.sun.enterprise.deployment.node;

import java.util.List;

/**
 * This interface defines all the protocol associated with a root node
 * of an XML file.
 *
 * @author  Jerome Dochez
 * @version
 */
public interface RootXMLNode<T>  extends XMLNode<T> {

    /**
     * @return the DOCTYPE that should be written to the XML file
     */
    public String getDocType();

    /**
     * set the DOCTYPE as read in the input XML File
     * @param docType doctype for the xml
     */
    public void setDocType(String docType);

    /**
     * @return the SystemID of the XML file
     */
    public String getSystemID();

    /**
     * @return the list of SystemID of the XML schema supported
     */
    public List<String> getSystemIDs();

    /**
     * @return the default spec version level this node complies to
     */
    public String getSpecVersion();

}

