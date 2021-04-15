/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.results;

import org.jvnet.hk2.config.Dom;

import java.util.List;

/**
 * Response information object. Returned on call to GET method on list resource. Information used by provider to
 * generate the appropriate output.
 *
 * @author Rajeshwar Patil
 */
public class GetResultList extends Result {

    /**
     * Constructor
     */
    public GetResultList(List<Dom> domList, String postCommand, String[][] commandResourcesPaths, OptionsResult metaData) {
        __domList = domList;
        __postCommand = postCommand;
        __commandResourcesPaths = commandResourcesPaths;
        __metaData = metaData;
    }

    /**
     * Returns the List<Dom> object associated with the list resource.
     */
    public List<Dom> getDomList() {
        return __domList;
    }

    /**
     * Returns postCommand associated with the resource.
     */
    //    public String getPostCommand() {
    //        return __postCommand;
    //    }

    /**
     * Returns an array of command resources paths and the operation type.
     */
    public String[][] getCommandResourcesPaths() {
        return __commandResourcesPaths;
    }

    /**
     * Returns OptionsResult - the meta-data of this resource.
     */
    public OptionsResult getMetaData() {
        return __metaData;
    }

    List<Dom> __domList;
    String __postCommand;
    String[][] __commandResourcesPaths;
    OptionsResult __metaData;
}
