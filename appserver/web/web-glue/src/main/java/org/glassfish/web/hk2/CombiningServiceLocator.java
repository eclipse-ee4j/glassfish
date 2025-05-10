/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.web.hk2;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.Filter;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.MethodParameter;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorState;
import org.glassfish.hk2.api.Unqualified;


/**
 * <p>
 * Combines services from 2 service locators. To be used in system apps to get
 * access to local scopes available only in the parent service locator.
 * <p>
 * It does 3 things:
 *
 * <ul>
 * <li>For calls to getService methods, gets the service in the primary locator.
 * If not found there, gets it from the fallback locator
 * </li>
 * <li>For calls to getAllServices methods, concatenates the list of service in
 * the primary locator and the list of services in the fallback locator. It
 * removes duplicate services
 * </li>
 * <li>Delegates all other calls (including {@code getServiceHandle}) to primary
 * locator. This is to simplify the solution because accessing local scopes from
 * the fallback locator is likely not needed in Admin Console in these methods
 * </li>
 * </ul>
 */
public class CombiningServiceLocator implements ServiceLocator {

    private static final System.Logger LOG = System.getLogger(CombiningServiceLocator.class.getName());
    private final ServiceLocator primaryLocator;
    private final ServiceLocator fallbackLocator;

    public CombiningServiceLocator(ServiceLocator primaryLocator, ServiceLocator fallbackLocator) {
        this.primaryLocator = primaryLocator;
        this.fallbackLocator = fallbackLocator;
    }

    @Override
    public <T> T getService(Class<T> contractOrImpl, Annotation... qualifiers) throws MultiException {
        return getCombinedService(locator -> locator.getService(contractOrImpl, qualifiers));
    }

    private <T> T getCombinedService(Function<ServiceLocator, T> selector) {
        try {
            return selector.apply(primaryLocator);
        } catch (MultiException e) {
            return selector.apply(fallbackLocator);
        }
    }

    @Override
    public <T> T getService(Type contractOrImpl, Annotation... qualifiers) throws MultiException {
        return getCombinedService(locator -> locator.getService(contractOrImpl, qualifiers));
    }

    @Override
    public <T> T getService(Class<T> contractOrImpl, String name, Annotation... qualifiers) throws MultiException {
        return getCombinedService(locator -> locator.getService(contractOrImpl, name, qualifiers));
    }

    @Override
    public <T> T getService(Type contractOrImpl, String name, Annotation... qualifiers) throws MultiException {
        return getCombinedService(locator -> locator.getService(contractOrImpl, name, qualifiers));
    }

    @Override
    public <T> T getService(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root) throws MultiException {
        return getCombinedService(locator -> locator.getService(activeDescriptor, root));
    }

    @Override
    public <T> T getService(ActiveDescriptor<T> activeDescriptor, ServiceHandle<?> root, Injectee injectee) throws MultiException {
        return getCombinedService(locator -> locator.getService(activeDescriptor, root, injectee));
    }

    @Override
    public <T> List<T> getAllServices(Class<T> contractOrImpl, Annotation... qualifiers) throws MultiException {
        return getCombinedServicesWithType(locator -> locator.getAllServiceHandles(contractOrImpl, qualifiers));
    }

    private <T> List<T> getCombinedServicesWithType(Function<ServiceLocator, List<ServiceHandle<T>>> selector) {
        return (List<T>) getCombinedServices(locator -> (List<ServiceHandle<?>>) (List<?>) selector.apply(locator));
    }

    private List<?> getCombinedServices(Function<ServiceLocator, List<ServiceHandle<?>>> selector) {
        List<ServiceHandle<?>> allServicesFromPrimary;
        try {
            allServicesFromPrimary = selector.apply(primaryLocator);
        } catch (MultiException e) {
            allServicesFromPrimary = List.of();
        }
        List<ServiceHandle<?>> allServicesFromFallback = selector.apply(fallbackLocator);
        return unionOfLists(allServicesFromPrimary, allServicesFromFallback);

    }

    private List<?> unionOfLists(List<ServiceHandle<?>> primaryList, List<ServiceHandle<?>> fallbackList) {
        Set<Object> serviceKeysInResult = new HashSet<>();
        List<Object> result = new ArrayList<>();
        Iterable<ServiceHandle<?>> iterableOverBothLists = (Iterable<ServiceHandle<?>>) () -> Stream.concat(primaryList.stream(), fallbackList.stream()).iterator();
        for (ServiceHandle<?> serviceHandle : iterableOverBothLists) {
            Type serviceType = serviceHandle.getActiveDescriptor().getImplementationType();
            Long locatorId = serviceHandle.getActiveDescriptor().getLocatorId();
            List<Object> serviceKey = List.of(serviceType, locatorId);
            if (!serviceKeysInResult.contains(serviceKey)) {
                try {
                    Object service = serviceHandle.getService();
                    serviceKeysInResult.add(serviceKey);
                    result.add(service);
                } catch (MultiException e) {
                    LOG.log(System.Logger.Level.DEBUG,
                            () -> "Error getting " + serviceHandle.getActiveDescriptor().getImplementationType()
                            + " from service locator id=" + serviceHandle.getActiveDescriptor().getLocatorId()
                            + " (primaryLocator id=" + primaryLocator.getLocatorId() + ","
                            + " fallbackLocator id=" + fallbackLocator.getLocatorId() + "). " + e.getMessage(), e);
                }
            }
        }
        return result;
    }

    @Override
    public <T> List<T> getAllServices(Type contractOrImpl, Annotation... qualifiers) throws MultiException {
        return (List<T>) getCombinedServices(locator -> (List<ServiceHandle<?>>) locator.getAllServiceHandles(contractOrImpl, qualifiers));
    }

    @Override
    public <T> List<T> getAllServices(Annotation qualifier, Annotation... qualifiers) throws MultiException {
        return (List<T>) getCombinedServices(locator -> locator.getAllServiceHandles(qualifier, qualifiers));
    }

    @Override
    public List<?> getAllServices(Filter searchCriteria) throws MultiException {
        return getCombinedServices(locator -> locator.getAllServiceHandles(searchCriteria));
    }

    @Override
    public <T> ServiceHandle<T> getServiceHandle(Class<T> contractOrImpl, Annotation... qualifiers) throws MultiException {
        return primaryLocator.getServiceHandle(contractOrImpl, qualifiers);
    }

    @Override
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl, Annotation... qualifiers) throws MultiException {
        return primaryLocator.getServiceHandle(contractOrImpl, qualifiers);
    }

    @Override
    public <T> ServiceHandle<T> getServiceHandle(Class<T> contractOrImpl, String name, Annotation... qualifiers) throws MultiException {
        return primaryLocator.getServiceHandle(contractOrImpl, name, qualifiers);
    }

    @Override
    public <T> ServiceHandle<T> getServiceHandle(Type contractOrImpl, String name, Annotation... qualifiers) throws MultiException {
        return primaryLocator.getServiceHandle(contractOrImpl, name, qualifiers);
    }

    @Override
    public <T> List<ServiceHandle<T>> getAllServiceHandles(Class<T> contractOrImpl, Annotation... qualifiers) throws MultiException {
        return primaryLocator.getAllServiceHandles(contractOrImpl, qualifiers);
    }

    @Override
    public List<ServiceHandle<?>> getAllServiceHandles(Type contractOrImpl, Annotation... qualifiers) throws MultiException {
        return primaryLocator.getAllServiceHandles(contractOrImpl, qualifiers);
    }

    @Override
    public List<ServiceHandle<?>> getAllServiceHandles(Annotation qualifier, Annotation... qualifiers) throws MultiException {
        return primaryLocator.getAllServiceHandles(qualifier, qualifiers);
    }

    @Override
    public List<ServiceHandle<?>> getAllServiceHandles(Filter searchCriteria) throws MultiException {
        return primaryLocator.getAllServiceHandles(searchCriteria);
    }

    @Override
    public List<ActiveDescriptor<?>> getDescriptors(Filter filter) {
        return primaryLocator.getDescriptors(filter);
    }

    @Override
    public ActiveDescriptor<?> getBestDescriptor(Filter filter) {
        return primaryLocator.getBestDescriptor(filter);
    }

    @Override
    public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor, Injectee injectee) throws MultiException {
        return primaryLocator.reifyDescriptor(descriptor, injectee);
    }

    @Override
    public ActiveDescriptor<?> reifyDescriptor(Descriptor descriptor) throws MultiException {
        return primaryLocator.reifyDescriptor(descriptor);
    }

    @Override
    public ActiveDescriptor<?> getInjecteeDescriptor(Injectee injectee) throws MultiException {
        return primaryLocator.getInjecteeDescriptor(injectee);
    }

    @Override
    public <T> ServiceHandle<T> getServiceHandle(ActiveDescriptor<T> activeDescriptor, Injectee injectee) throws MultiException {
        return primaryLocator.getServiceHandle(activeDescriptor, injectee);
    }

    @Override
    public <T> ServiceHandle<T> getServiceHandle(ActiveDescriptor<T> activeDescriptor) throws MultiException {
        return primaryLocator.getServiceHandle(activeDescriptor);
    }

    @Override
    public String getDefaultClassAnalyzerName() {
        return primaryLocator.getDefaultClassAnalyzerName();
    }

    @Override
    public void setDefaultClassAnalyzerName(String defaultClassAnalyzer) {
        primaryLocator.setDefaultClassAnalyzerName(defaultClassAnalyzer);
    }

    @Override
    public Unqualified getDefaultUnqualified() {
        return primaryLocator.getDefaultUnqualified();
    }

    @Override
    public void setDefaultUnqualified(Unqualified unqualified) {
        primaryLocator.setDefaultUnqualified(unqualified);
    }

    @Override
    public String getName() {
        return primaryLocator.getName();
    }

    @Override
    public long getLocatorId() {
        return primaryLocator.getLocatorId();
    }

    @Override
    public ServiceLocator getParent() {
        return primaryLocator.getParent();
    }

    @Override
    public void shutdown() {
        primaryLocator.shutdown();
    }

    @Override
    public ServiceLocatorState getState() {
        return primaryLocator.getState();
    }

    @Override
    public boolean isShutdown() {
        return primaryLocator.isShutdown();
    }

    @Override
    public boolean getNeutralContextClassLoader() {
        return primaryLocator.getNeutralContextClassLoader();
    }

    @Override
    public void setNeutralContextClassLoader(boolean neutralContextClassLoader) {
        primaryLocator.setNeutralContextClassLoader(neutralContextClassLoader);
    }

    @Override
    public <T> T create(Class<T> createMe) {
        return primaryLocator.create(createMe);
    }

    @Override
    public <T> T create(Class<T> createMe, String strategy) {
        return primaryLocator.create(createMe, strategy);
    }

    @Override
    public void inject(Object injectMe) {
        primaryLocator.inject(injectMe);
    }

    @Override
    public void inject(Object injectMe, String strategy) {
        primaryLocator.inject(injectMe, strategy);
    }

    @Override
    public Object assistedInject(Object injectMe, Method method, MethodParameter... params) {
        return primaryLocator.assistedInject(injectMe, method, params);
    }

    @Override
    public Object assistedInject(Object injectMe, Method method, ServiceHandle<?> root, MethodParameter... params) {
        return primaryLocator.assistedInject(injectMe, method, root, params);
    }

    @Override
    public void postConstruct(Object postConstructMe) {
        primaryLocator.postConstruct(postConstructMe);
    }

    @Override
    public void postConstruct(Object postConstructMe, String strategy) {
        primaryLocator.postConstruct(postConstructMe, strategy);
    }

    @Override
    public void preDestroy(Object preDestroyMe) {
        primaryLocator.preDestroy(preDestroyMe);
    }

    @Override
    public void preDestroy(Object preDestroyMe, String strategy) {
        primaryLocator.preDestroy(preDestroyMe, strategy);
    }

    @Override
    public <U> U createAndInitialize(Class<U> createMe) {
        return primaryLocator.createAndInitialize(createMe);
    }

    @Override
    public <U> U createAndInitialize(Class<U> createMe, String strategy) {
        return primaryLocator.createAndInitialize(createMe, strategy);
    }

}
