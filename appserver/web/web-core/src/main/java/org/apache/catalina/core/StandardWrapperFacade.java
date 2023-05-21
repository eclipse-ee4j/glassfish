/*
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

package org.apache.catalina.core;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import java.util.Enumeration;

/**
 * Facade for the <b>StandardWrapper</b> object.
 *
 * @author Remy Maucharat
 * @version $Revision: 1.3 $ $Date: 2006/11/13 19:26:30 $
 */
public final class StandardWrapperFacade implements ServletConfig {

    // ----------------------------------------------------- Instance Variables

    /**
     * Wrapped config.
     */
    private ServletConfig config;

    /**
     * The context facade object for this wrapper.
     */
    private ServletContext context;

    // ----------------------------------------------------------- Constructors

    /**
     * Create a new facade around a StandardWrapper.
     */
    public StandardWrapperFacade(StandardWrapper config) {
        super();
        this.config = config;
    }



    // -------------------------------------------------- ServletConfig Methods

    @Override
    public String getServletName() {
        return config.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        if (context == null) {
            context = config.getServletContext();
            if (context instanceof ApplicationContext) {
                context = ((ApplicationContext) context).getFacade();
            }
        }

        return context;
    }

    @Override
    public String getInitParameter(String name) {
        return config.getInitParameter(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return config.getInitParameterNames();
    }

}
