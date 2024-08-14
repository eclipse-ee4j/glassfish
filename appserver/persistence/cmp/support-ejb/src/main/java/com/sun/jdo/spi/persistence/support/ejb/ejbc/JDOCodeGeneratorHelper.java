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

/*
 * JDOCodeGeneratorHelper.java
 *
 * Created on Aug 28, 2001
 */

package com.sun.jdo.spi.persistence.support.ejb.ejbc;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.jdo.spi.persistence.support.ejb.codegen.GeneratorException;

import java.util.ResourceBundle;

import org.glassfish.persistence.common.I18NHelper;

/*
 * This is the helper class for JDO code generation
 *
 */
public class JDOCodeGeneratorHelper {

    /**
     * I18N message handler
     */
    private final static ResourceBundle messages = I18NHelper.loadBundle(
        JDOCodeGeneratorHelper.class);

    /** Calculate module name from a bundle.
     * @return module name.
     */
    public static String getModuleName(EjbBundleDescriptor bundle) {
        String moduleName = null;
        Application application = bundle.getApplication();
        if (application.isVirtual()) {
            // Stand-alone module is deployed.
            moduleName = application.getRegistrationName();

        } else {
            // Module is deployed as a part of an Application.
            String jarName = bundle.getModuleDescriptor().getArchiveUri();
            int l = jarName.length();

            // Remove ".jar" from the bundle's jar name.
            moduleName = jarName.substring(0, l - 4);

        }

        return moduleName;
    }

    /**
     * Create GeneratorException for this message key.
     * @param key the message key in the bundle.
     * @param bundle the ejb bundle.
     * @return GeneratorException.
     */
    public static GeneratorException createGeneratorException(
            String key, EjbBundleDescriptor bundle) {
        return new GeneratorException(I18NHelper.getMessage(
            messages, key,
            bundle.getApplication().getRegistrationName(),
            getModuleName(bundle)));
    }

    /**
     * Create GeneratorException for this message key.
     * @param key the message key in the bundle.
     * @param bundle the ejb bundle.
     * @param e the Exception to use for the message.
     * @return GeneratorException.
     */
    public static GeneratorException createGeneratorException(
            String key, EjbBundleDescriptor bundle,  Exception e) {

        return new GeneratorException(I18NHelper.getMessage(
            messages, key,
            bundle.getApplication().getRegistrationName(),
            getModuleName(bundle),
            e.getMessage()));
    }

    /**
     * Create GeneratorException for this message key and bean name.
     * @param key the message key in the bundle.
     * @param bundle the ejb bundle.
     * @return GeneratorException.
     */
    public static GeneratorException createGeneratorException(
            String key, String beanName, EjbBundleDescriptor bundle) {

        return new GeneratorException(I18NHelper.getMessage(
            messages, key, beanName,
            bundle.getApplication().getRegistrationName(),
            getModuleName(bundle)));
    }

    /**
     * Create GeneratorException for this message key and bean name.
     * @param key the message key in the bundle.
     * @param beanName the CMP bean name that caused the exception.
     * @param bundle the ejb bundle.
     * @param e the Exception to use for the message.
     * @return GeneratorException.
     */
    public static GeneratorException createGeneratorException(
            String key, String beanName, EjbBundleDescriptor bundle,
            Exception e) {

        return createGeneratorException(key, beanName, bundle, e.getMessage());
    }

    /**
     * Create GeneratorException for this message key, bean name,
     * and a StringBuffer with validation exceptions.
     * @param key the message key in the bundle.
     * @param beanName the CMP bean name that caused the exception.
     * @param bundle the ejb bundle.
     * @param e the Exception to use for the message.
     * @param buf the StringBuffer with validation exceptions.
     * @return GeneratorException.
     */
    public static GeneratorException createGeneratorException(
            String key, String beanName, EjbBundleDescriptor bundle,
            Exception e, StringBuffer buf) {

        String msg = (buf == null) ?
                e.getMessage() :
                buf.append(e.getMessage()).append('\n').toString();
        return createGeneratorException(key, beanName, bundle, msg);
    }

    /**
     * Create GeneratorException for this message key and bean name.
     * @param key the message key in the bundle.
     * @param beanName the CMP bean name that caused the exception.
     * @param bundle the ejb bundle.
     * @param msg the message text to append.
     * @return GeneratorException.
     */
    public static GeneratorException createGeneratorException(
            String key, String beanName, EjbBundleDescriptor bundle,
            String msg) {

        return new GeneratorException(I18NHelper.getMessage(
            messages, key,
            new Object[] {
                beanName,
                bundle.getApplication().getRegistrationName(),
                getModuleName(bundle),
                msg}
            ));
    }
}
