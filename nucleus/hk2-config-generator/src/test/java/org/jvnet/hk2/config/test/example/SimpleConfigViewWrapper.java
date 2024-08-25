/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

package org.jvnet.hk2.config.test.example;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.regex.Pattern;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigView;


/**
 * View that translate configured attributes containing properties like ${foo.bar}
 * into system properties values.
 *
 * @author Jerome Dochez
 */
public class SimpleConfigViewWrapper implements ConfigView {

    final static Pattern p = Pattern.compile("([^\\$]*)\\$\\{([^\\}]*)\\}([^\\$]*)");

    private static final String ALIAS_TOKEN = "ALIAS";

    public static Object getTranslatedValue(Object value) {
        return value;
    }

    final ConfigView masterView;


    SimpleConfigViewWrapper(ConfigView master) {
        this.masterView = master;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return getTranslatedValue(masterView.invoke(proxy, method, args));
    }


    @Override
    public ConfigView getMasterView() {
        return masterView;
    }

    @Override
    public void setMasterView(ConfigView view) {
        // immutable implementation
    }

    @Override
    public <T extends ConfigBeanProxy> Class<T> getProxyType() {
        return masterView.getProxyType();
    }

    @Override
    public <T extends ConfigBeanProxy> T getProxy(Class<T> proxyType) {
        return proxyType.cast(Proxy.newProxyInstance(proxyType.getClassLoader(), new Class[] {proxyType}, this));
    }

   /**
     * check if a given property name matches AS alias pattern ${ALIAS=aliasname}.
     * if so, return the aliasname, otherwise return null.
     * @param propName The property name to resolve. ex. ${ALIAS=aliasname}.
     * @return The aliasname or null.
     */
   static public String getAlias(String propName) {
       String aliasName = null;
       String starter = "${" + ALIAS_TOKEN + "="; // no space is allowed in starter
       String ender = "}";

       propName = propName.trim();
       if (propName.startsWith(starter) && propName.endsWith(ender)) {
           propName = propName.substring(starter.length());
           int lastIdx = propName.length() - 1;
           if (lastIdx > 1) {
               propName = propName.substring(0, lastIdx);
               if (propName != null) {
                   aliasName = propName.trim();
               }
           }
       }
       return aliasName;
   }

}
