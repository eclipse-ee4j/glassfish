/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.security;

import java.security.Security;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.LogFacade;
import org.apache.catalina.startup.CatalinaProperties;

import static java.util.logging.Level.CONFIG;

/**
 * Util class to protect Catalina against package access and insertion.
 * The code are been moved from Catalina.java
 * @author the Catalina.java authors
 * @author Jean-Francois Arcand
 */
public final class SecurityConfig{
    private static volatile SecurityConfig singleton;

    private static final Logger log = LogFacade.getLogger();


    private final static String PACKAGE_ACCESS =  "sun.,"
                                                + "org.apache.catalina."
                                                + ",org.glassfish.wasp."
                                                + ",org.glassfish.grizzly.tcp."
                                                + ",org.glassfish.grizzly.";

    private final static String PACKAGE_DEFINITION= "java.,sun."
                                                + ",org.apache.catalina."
                                                + ",org.glassfish.grizzly.tcp."
                                                + ",org.glassfish.grizzly."
                                                + ",org.glassfish.wasp.";
    /**
     * List of protected package from conf/catalina.properties
     */
    private String packageDefinition;


    /**
     * List of protected package from conf/catalina.properties
     */
    private String packageAccess;


    /**
     * Create a single instance of this class.
     */
    private SecurityConfig(){
        try{
            packageDefinition = CatalinaProperties.getProperty("package.definition");
            packageAccess = CatalinaProperties.getProperty("package.access");
        } catch (java.lang.Exception ex){
            log.log(Level.FINE, "Unable to load properties using CatalinaProperties", ex);
        }
    }


    /**
     * Returns the singleton instance of that class.
     * @return an instance of that class.
     */
    public static SecurityConfig newInstance(){
        if (singleton == null){
            singleton = new SecurityConfig();
        }
        return singleton;
    }


    /**
     * Set the security package.access value.
     */
    public void setPackageAccess(){
        // If catalina.properties is missing, protect all by default.
        if (packageAccess == null){
            appendSecurityProperty("package.access", PACKAGE_ACCESS);
        } else {
            appendSecurityProperty("package.access", packageAccess);
        }
    }


    /**
     * Set the security package.definition value.
     */
    public void setPackageDefinition(){
        // If catalina.properties is missing, protect all by default.
        if (packageDefinition == null){
            appendSecurityProperty("package.definition", PACKAGE_DEFINITION);
        } else {
            appendSecurityProperty("package.definition", packageDefinition);
        }
    }


    /**
     * Append the proper security property
     *
     * @param property the property name
     */
    private void appendSecurityProperty(String property, String packages) {
        if (System.getSecurityManager() == null) {
            return;
        }
        log.log(CONFIG, "appendSecurityProperty(property={0}, packageList={1})", new Object[] {property, packages});
        String definition = Security.getProperty(property);
        if (definition != null && !definition.isEmpty()) {
            definition += ",";
        }
        Security.setProperty(property, definition + packages);
    }
}
