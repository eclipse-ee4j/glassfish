/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.util;

import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.metadata.BeanDescriptor;

import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Service;

@Service
public class ConnectorJavaBeanValidator {

    private final static Logger _logger = LogDomains.getLogger(
            ConnectorJavaBeanValidator.class, LogDomains.RSR_LOGGER);

    public void validateJavaBean(Object bean, String rarName) {
        if (bean != null) {
            Validator validator = getBeanValidator(rarName);
            if (validator != null) {
                BeanDescriptor bd =
                        validator.getConstraintsForClass(bean.getClass());
                bd.getConstraintDescriptors();

                Class array[] = new Class[]{};
                Set constraintViolations = validator.validate(bean, array);


                if (constraintViolations != null && constraintViolations.size() > 0) {
                    ConstraintViolationException cve = new ConstraintViolationException(constraintViolations);
                    StringBuffer msg = new StringBuffer();

                    Iterator it = constraintViolations.iterator();
                    while (it.hasNext()) {
                        ConstraintViolation cv = (ConstraintViolation) it.next();
                        msg.append("\n Bean Class : ").append(cv.getRootBeanClass());
                        msg.append("\n Bean : ").append(cv.getRootBean());
                        msg.append("\n Property path : " ).append(cv.getPropertyPath());
                        msg.append("\n Violation Message : " ).append(cv.getMessage());
                    }

                    Object[] args = new Object[]{bean.getClass(), rarName, msg.toString()};
                    _logger.log(Level.SEVERE, "validation.constraints.violation",args);
                    throw cve;
                }
            } else if(_logger.isLoggable(Level.FINEST)){
               _logger.log(Level.FINEST, "No Bean Validator is available for RAR [ " + rarName + " ]");
            }
        }
    }

    private Validator getBeanValidator(String rarName) {
        Validator beanValidator = ConnectorRegistry.getInstance().getBeanValidator(rarName);
        ValidatorFactory validatorFactory = null;
        // this is needed in case of appclient/standalone client
        // and system-resource-adapters in server.
        if (beanValidator == null) {
            ClassLoader contextCL = null;
            try{
                contextCL = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(ConnectorRuntime.getRuntime().getConnectorClassLoader());
                validatorFactory = Validation.byDefaultProvider().configure().buildValidatorFactory();
                beanValidator = validatorFactory.getValidator();
                ConnectorRegistry.getInstance().addBeanValidator(rarName, beanValidator);
            }finally{
                Thread.currentThread().setContextClassLoader(contextCL);
            }
        }
        return beanValidator;
    }
}
