/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.kernel.bean_validator;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import javax.naming.NamingException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorContext;
import jakarta.validation.ValidatorFactory;

import org.glassfish.api.naming.NamespacePrefixes;
import org.glassfish.api.naming.NamedNamingObjectProxy;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

@Service
@NamespacePrefixes({BeanValidatorNamingProxy.nameForValidator, BeanValidatorNamingProxy.nameForValidatorFactory})
public class BeanValidatorNamingProxy implements NamedNamingObjectProxy {

    static final String nameForValidator = "java:comp/Validator";
    static final String nameForValidatorFactory = "java:comp/ValidatorFactory";

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @Inject @Named("ValidationNamingProxy") @Optional
    private NamedNamingObjectProxy cdiNamingProxy;

    public Object handle(String name) throws NamingException {
        Object result = null;

        // see if CDI is active, use BeanManager to obtain Validator/ValidatorFactory
        if (cdiNamingProxy != null) {
            result = cdiNamingProxy.handle(name);

            if (result != null) {
                return result;
            }
        }

        if (nameForValidator.equals(name)) {
            result = getValidator();
        } else if (nameForValidatorFactory.equals(name)) {
            result = getValidatorFactory();
        }
        return result;
    }

    private Validator getValidator() throws NamingException {
        if (null == validator) {
            try {
                ValidatorFactory factory = getValidatorFactory();
                ValidatorContext validatorContext = factory.usingContext();
                validator = validatorContext.getValidator();
            } catch (Throwable t) {
                NamingException ne = new NamingException("Error retrieving Validator for " + nameForValidator + " lookup");
                ne.initCause(t);
                throw ne;
            }
        }
        return validator;
    }

    private ValidatorFactory getValidatorFactory() throws NamingException {

        if (null == validatorFactory) {
            try {
                validatorFactory = Validation.buildDefaultValidatorFactory();
            } catch (Throwable t) {
                NamingException ne = new NamingException("Error retrieving ValidatorFactory for " + nameForValidatorFactory + " lookup");
                ne.initCause(t);
                throw ne;
            }
        }

        return validatorFactory;
    }



}
