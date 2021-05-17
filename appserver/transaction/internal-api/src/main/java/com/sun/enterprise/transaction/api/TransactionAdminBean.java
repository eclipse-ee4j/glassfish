/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction.api;

import java.util.ArrayList;

public class TransactionAdminBean implements java.io.Serializable {
    private Object m_identifier;
        private String m_id;
    private String m_status;
    private long m_elapsedTime;
        private String m_componentName;
        private ArrayList<String> m_resourceNames;

    public TransactionAdminBean(Object identifier, String id, String status, long elapsedTime,
                                    String componentName, ArrayList<String> resourceNames) {
        m_identifier = identifier;
                m_id=id;
        m_status = status;
        m_elapsedTime = elapsedTime;
                m_componentName = componentName;
                m_resourceNames = resourceNames;
    }

    // getter functions ...

    public Object getIdentifier(){
        return m_identifier;
    }

        public String getId(){
            return m_id;
        }

    public String getStatus(){
        return m_status;
    }

    public long getElapsedTime(){
        return m_elapsedTime;
    }

        public String getComponentName() {
            return m_componentName;
        }

        public ArrayList<String> getResourceNames() {
            return m_resourceNames;
        }

    // setter functions ...

    public void setIdentifier(Object id){
        m_identifier = id;
        }

        public void setId(String id){
            m_id=id;
        }

    public void setStatus(String sts){
        m_status = sts;
    }

    public void setElapsedTime(long time){
        m_elapsedTime = time;
    }

        public void setComponentName(String componentName) {
            m_componentName = componentName;
        }

        public void setResourceNames(ArrayList<String> resourceNames) {
            m_resourceNames = resourceNames;
        }

}



