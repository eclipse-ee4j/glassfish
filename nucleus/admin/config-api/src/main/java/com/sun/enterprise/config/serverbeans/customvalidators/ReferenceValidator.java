/*
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans.customvalidators;

import com.sun.enterprise.util.LocalStringManagerImpl;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.UnexpectedTypeException;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;

/**
 * @author Martin Mares
 */
public class ReferenceValidator implements ConstraintValidator<ReferenceConstraint, ConfigBeanProxy> {

    static class RemoteKeyInfo {
        final Method method;
        final ReferenceConstraint.RemoteKey annotation;

        public RemoteKeyInfo(Method method, ReferenceConstraint.RemoteKey annotation) {
            this.method = method;
            this.annotation = annotation;
        }
    }

    static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ReferenceValidator.class);

    private ReferenceConstraint rc;

    @Override
    public void initialize(ReferenceConstraint rc) {
        this.rc = rc;
    }

    @Override
    public boolean isValid(ConfigBeanProxy config, ConstraintValidatorContext cvc) throws UnexpectedTypeException {
        if (config == null) {
            return true;
        }
        Dom dom = Dom.unwrap(config);
        if (rc.skipDuringCreation() && dom.getKey() == null) {
            return true; //During creation the coresponding DOM is not fully loaded.
        }
        Collection<RemoteKeyInfo> remoteKeys = findRemoteKeys(config);
        if (!remoteKeys.isEmpty()) {
            ServiceLocator habitat = dom.getHabitat();
            boolean result = true;
            boolean disableGlobalMessage = true;
            for (RemoteKeyInfo remoteKeyInfo : remoteKeys) {
                if (remoteKeyInfo.method.getParameterTypes().length > 0) {
                    throw new UnexpectedTypeException(localStrings.getLocalString("referenceValidator.not.getter",
                            "The RemoteKey annotation must be on a getter method."));
                }
                try {
                    Object value = remoteKeyInfo.method.invoke(config);
                    if (value instanceof String) {
                        String key = (String) value;
                        ConfigBeanProxy component = habitat.getService(remoteKeyInfo.annotation.type(), key);
                        if (component == null) {
                            result = false;
                            if (remoteKeyInfo.annotation.message().isEmpty()) {
                                disableGlobalMessage = false;
                            } else {
                                cvc.buildConstraintViolationWithTemplate(remoteKeyInfo.annotation.message())
                                        .addNode(Dom.convertName(remoteKeyInfo.method.getName())).addConstraintViolation();
                            }
                        }
                    } else {
                        throw new UnexpectedTypeException(localStrings.getLocalString("referenceValidator.not.string",
                                "The RemoteKey annotation must identify a method that returns a String."));
                    }
                } catch (Exception ex) {
                    return false;
                }
            }
            if (!result && disableGlobalMessage) {
                cvc.disableDefaultConstraintViolation();
            }
            return result;
        }
        return true;
    }

    private Collection<RemoteKeyInfo> findRemoteKeys(Object o) {
        Collection<RemoteKeyInfo> result = new ArrayList<RemoteKeyInfo>();
        if (o == null) {
            return result;
        }
        findRemoteKeys(o.getClass(), result);
        return result;
    }

    private void findRemoteKeys(Class c, Collection<RemoteKeyInfo> result) {
        Method[] methods = c.getMethods();
        for (Method method : methods) {
            ReferenceConstraint.RemoteKey annotation = method.getAnnotation(ReferenceConstraint.RemoteKey.class);
            if (annotation != null) {
                result.add(new RemoteKeyInfo(method, annotation));
            }
        }
        Class superclass = c.getSuperclass();
        if (superclass != null) {
            findRemoteKeys(superclass, result);
        }
        Class[] interfaces = c.getInterfaces();
        for (Class iface : interfaces) {
            findRemoteKeys(iface, result);
        }
    }

}
