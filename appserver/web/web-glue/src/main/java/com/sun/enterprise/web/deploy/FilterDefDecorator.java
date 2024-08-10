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

package com.sun.enterprise.web.deploy;

import com.sun.enterprise.deployment.web.InitializationParameter;
import com.sun.enterprise.deployment.web.ServletFilter;

import java.util.Map;
import java.util.Vector;

import org.apache.catalina.deploy.FilterDef;


/**
 * Decorator of class <code>org.apache.catalina.deploy.FilterDef</code>
 *
 * @author Jean-Francois Arcand
 */

public class FilterDefDecorator extends FilterDef {

    /**
     * The set of initialization parameters for this filter, keyed by
     * parameter name.
     */
    private Map parameters = null;

    private ServletFilter decoree;

    public FilterDefDecorator(ServletFilter decoree){
        this.decoree = decoree;
        Vector initParams = decoree.getInitializationParameters();
        InitializationParameter initParam;
        for (int i=0; i < initParams.size(); i++){
           initParam = (InitializationParameter)initParams.get(i);
           addInitParameter( initParam.getName(),initParam.getValue() );
        }
    }



    // ------------------------------------------------------------- Properties


    public String getDescription() {
        return decoree.getDescription();
    }

    public String getDisplayName() {
        return decoree.getDisplayName();
    }

    public String getFilterClassName() {
        String className = decoree.getClassName();
        if (null == className || className.isEmpty()) {
            return null;
        } else {
            return className;
        }
    }

    public void setFilterClassName(String filterClassName) {
        super.setFilterClassName(filterClassName);
        decoree.setClassName(filterClassName);
    }

    public String getFilterName() {
        return decoree.getName();
    }

    public String getLargeIcon() {
        return decoree.getLargeIconUri();
    }

    public String getSmallIcon() {
        return decoree.getSmallIconUri();
    }

    public boolean isAsyncSupported() {
        Boolean decoreeAsyncFlag = decoree.isAsyncSupported();
        return (decoreeAsyncFlag == null ? false : decoreeAsyncFlag);
    }

}
