/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.resourcebase.resources.api;


/**
 * Represents pool information.
 * @author Jagadish Ramu
 */
public class PoolInfo implements org.glassfish.resourcebase.resources.api.GenericResourceInfo {

    private String name;
    private String applicationName = null;
    private String moduleName = null;

    public PoolInfo(String name){
        this.name = name;
    }
    public PoolInfo(String name, String applicationName){
        this.name = name;
        this.applicationName = applicationName;
    }

    public PoolInfo(String name, String applicationName, String moduleName){
        this.name = name;
        this.applicationName = applicationName;
        this.moduleName = moduleName;
    }

    /**
     * @inheritDoc
     */
    public String getName() {
        return name;
    }

    /**
     * @inheritDoc
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * @inheritDoc
     */
    public String getModuleName() {
        return moduleName;
    }

        public String toString(){
            if(applicationName != null && moduleName != null){
                return "{ PoolInfo : (name="+name+"), (applicationName="+applicationName+"), (moduleName="+moduleName+")}";
            }else if(applicationName != null){
                return "{ PoolInfo : (name="+name+"), (applicationName="+applicationName+") }";
            }else{
                return name;
            }
    }

    public boolean equals(Object o){
        boolean result = false;
        if(o == this){
            result = true;
        }else if(o instanceof PoolInfo){
            PoolInfo poolInfo = (PoolInfo)o;
            boolean poolNameEqual = poolInfo.getName().equals(name);
            boolean appNameEqual = false;
            if(applicationName == null && poolInfo.getApplicationName() == null){
                appNameEqual = true;
            }else if(applicationName !=null && poolInfo.getApplicationName() != null
                    && applicationName.equals(poolInfo.getApplicationName())){
                appNameEqual = true;
            }
            boolean moduleNameEqual = false;
            if(moduleName == null && poolInfo.getModuleName() == null){
                moduleNameEqual = true;
            }else if(moduleName !=null && poolInfo.getModuleName() != null
                    && moduleName.equals(poolInfo.getModuleName())){
                moduleNameEqual = true;
            }
            result = poolNameEqual && appNameEqual && moduleNameEqual;
        }
        return result;
    }

    public int hashCode(){
        int result = 67;
        if (name != null)
            result = 67 * result + name.hashCode();
        if (applicationName != null)
            result = 67 * result + applicationName.hashCode();
        if (moduleName != null)
            result = 67 * result + moduleName.hashCode();

        return result;
    }
}
