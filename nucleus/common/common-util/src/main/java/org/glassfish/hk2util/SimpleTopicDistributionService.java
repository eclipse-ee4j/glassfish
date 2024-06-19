/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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
 */package org.glassfish.hk2util;

import static java.lang.System.Logger.Level.WARNING;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.api.messaging.MessageReceiver;
import org.glassfish.hk2.api.messaging.SubscribeTo;
import org.glassfish.hk2.api.messaging.Topic;
import org.glassfish.hk2.api.messaging.TopicDistributionService;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.jvnet.hk2.annotations.ContractsProvided;

/**
 *
 * @author Ondro Mihalyi
 */
@Singleton
@Named(SimpleTopicDistributionService.NAME)
@ContractsProvided({TopicDistributionService.class})
public class SimpleTopicDistributionService implements TopicDistributionService {

    public static final String NAME = "SimpleTopicDistributionService";

    @Inject
    private IterableProvider<Object> receivers;

    public static void enable(ServiceLocator locator) {
        if (locator == null) {
            throw new IllegalArgumentException();
        }

        if (locator.getService(SimpleTopicDistributionService.class, SimpleTopicDistributionService.NAME) != null) {
            // Will not add it a second time
            return;
        }

        ServiceLocatorUtilities.addClasses(locator, true, SimpleTopicDistributionService.class);
    }

    @Override
    public void distributeMessage(Topic<?> topic, Object message) {
        receivers.ofType(new TypeLiteral<MessageReceiver>() {
        }.getType())
                .forEach(receiver -> {
                    Arrays.stream(receiver.getClass().getMethods())
                            .filter(method -> {
                                final Parameter[] parameters = method.getParameters();
                                return methodReturnsVoid(method)
                                        && parameters.length == 1
                                        && method.getParameterTypes()[0].isInstance(message)
                                        && containsSubscribeToAnnotation(parameters[0].getAnnotations());
                            })
                            .forEach(method -> {
                                try {
                                    method.invoke(receiver, message);
                                } catch (Exception ex) {
                                    System.getLogger(SimpleTopicDistributionService.class.getName())
                                            .log(WARNING, ex.getMessage(), ex);
                                }
                            });
                });
    }

    private static boolean methodReturnsVoid(Method method) {
        return method.getReturnType().equals(Void.TYPE);
    }

    private boolean containsSubscribeToAnnotation(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (SubscribeTo.class.isInstance(annotation)) {
                return true;
            }
        }
        return false;
    }

}
