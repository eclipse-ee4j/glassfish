/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment;

import java.util.Properties;

/**
 * @author Dapeng Hu
 */
public abstract class AbstractConnectorResourceDescriptor extends ResourceDescriptor {

    private static final long serialVersionUID = -4452926772142887844L;
    private String name ;
    private String resourceAdapter;
    private Properties properties = new Properties();

    private static final String JAVA_URL = "java:";
    private static final String JAVA_COMP_URL = "java:comp/";

    public static String getJavaName(String theName) {
        if(!theName.contains(JAVA_URL)){
            theName = JAVA_COMP_URL + theName;
        }
        return theName;
    }

    public AbstractConnectorResourceDescriptor() {
        super();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResourceAdapter() {
        return resourceAdapter;
    }

    public void setResourceAdapter(String resourceAdapter) {
        this.resourceAdapter = resourceAdapter;
    }

    public void addProperty(String key, String value){
        properties.put(key, value);
    }
    public String getProperty(String key){
        return (String)properties.get(key);
    }

    public Properties getProperties(){
        return properties;
    }

    public boolean equals(Object object) {
        if (object instanceof AbstractConnectorResourceDescriptor) {
            AbstractConnectorResourceDescriptor another = (AbstractConnectorResourceDescriptor) object;
            if(getResourceType() == another.getResourceType()){
                return getJavaName(this.getName()).equals(getJavaName(another.getName()));
            }
        }
        return false;
    }


    public int hashCode() {
        int result = 17;
        result = 37*result + getName().hashCode();
        return result;
    }


}
