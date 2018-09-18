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

package com.sun.enterprise.connectors.authentication;

import java.util.HashMap;

/**
 * @author Kanwar Oberoi
 */
public class RuntimeSecurityMap {

    private HashMap userMap;

    private HashMap groupMap;

    public RuntimeSecurityMap() {
        this.userMap = new HashMap();
        this.groupMap = new HashMap();
    }

    public RuntimeSecurityMap(HashMap userMap, HashMap groupMap) {
        this.userMap = (HashMap) userMap.clone();
        this.groupMap = (HashMap) groupMap.clone();
    }

    public boolean equals(Object map) {
        if (map instanceof RuntimeSecurityMap) {
            RuntimeSecurityMap rsm = (RuntimeSecurityMap) map;
            if (!(rsm.userMap.equals(this.userMap)))
                return false;
            else if (!(rsm.groupMap.equals(this.groupMap)))
                return false;
            else
                return true;
        }
        return false;
    }

    public int hashCode(){
        return this.userMap.hashCode() + this.groupMap.hashCode();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        // implement
        return sb.toString();
    }

    public boolean isEmpty() {
        if ((this.userMap.size() == 0) && (this.groupMap.size() == 0))
            return true;
        else
            return false;
    }

    public HashMap getUserMap() {
        return (HashMap) ((this.userMap).clone());
    }

    public HashMap getGroupMap() {
        return (HashMap) ((this.groupMap).clone());
    }

}
