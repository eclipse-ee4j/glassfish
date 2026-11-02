/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.deployment.descriptor;

import com.sun.enterprise.deployment.MethodDescriptor;

import jakarta.ejb.Schedule;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * {@code @Schedule} methods are looked up with a {@link MethodDescriptor#TIMER_METHOD} descriptor,
 * which the {@code @TransactionAttribute} handler never binds.
 */
public class EjbDescriptorTimerTransactionAttributeTest {

    @Test
    public void publicScheduleMethodUsesAttributeBoundToBusinessMethod() throws Exception {
        EjbSessionDescriptor ejb = createEjb(MethodAnnotatedBean.class);
        Method method = MethodAnnotatedBean.class.getMethod("publicTimeout");
        ejb.setContainerTransactionFor(new MethodDescriptor(method, MethodDescriptor.EJB_LOCAL),
            new ContainerTransaction(ContainerTransaction.NOT_SUPPORTED, ""));

        assertThat(timerAttribute(ejb, method), equalTo(ContainerTransaction.NOT_SUPPORTED));
    }


    @Test
    public void privateScheduleMethodUsesMethodAnnotation() throws Exception {
        EjbSessionDescriptor ejb = createEjb(MethodAnnotatedBean.class);
        Method method = MethodAnnotatedBean.class.getDeclaredMethod("privateTimeout");

        assertThat(timerAttribute(ejb, method), equalTo(ContainerTransaction.NOT_SUPPORTED));
    }


    @Test
    public void privateScheduleMethodUsesClassAnnotation() throws Exception {
        EjbSessionDescriptor ejb = createEjb(ClassAnnotatedBean.class);
        Method method = ClassAnnotatedBean.class.getDeclaredMethod("privateTimeout");

        assertThat(timerAttribute(ejb, method), equalTo(ContainerTransaction.NOT_SUPPORTED));
    }


    @Test
    public void unannotatedScheduleMethodDefaultsToRequired() throws Exception {
        EjbSessionDescriptor ejb = createEjb(MethodAnnotatedBean.class);
        Method method = MethodAnnotatedBean.class.getDeclaredMethod("unannotatedTimeout");

        assertThat(timerAttribute(ejb, method), equalTo(ContainerTransaction.REQUIRED));
    }


    @Test
    public void businessMethodLookupDoesNotMatchOtherInterface() throws Exception {
        EjbSessionDescriptor ejb = createEjb(MethodAnnotatedBean.class);
        Method method = MethodAnnotatedBean.class.getMethod("publicTimeout");
        ejb.setContainerTransactionFor(new MethodDescriptor(method, MethodDescriptor.EJB_LOCAL),
            new ContainerTransaction(ContainerTransaction.NOT_SUPPORTED, ""));

        ContainerTransaction remote = ejb.getContainerTransactionFor(
            new MethodDescriptor(method, MethodDescriptor.EJB_REMOTE));

        assertThat(remote, not(nullValue()));
        assertThat(remote.getTransactionAttribute(), equalTo(ContainerTransaction.REQUIRED));
    }


    private static EjbSessionDescriptor createEjb(Class<?> beanClass) {
        EjbBundleDescriptorImpl bundle = new EjbBundleDescriptorImpl();
        bundle.setClassLoader(beanClass.getClassLoader());
        EjbSessionDescriptor ejb = new EjbSessionDescriptor();
        ejb.setName(beanClass.getSimpleName());
        ejb.setEjbClassName(beanClass.getName());
        ejb.setTransactionType(EjbDescriptor.CONTAINER_TRANSACTION_TYPE);
        ejb.setUsesDefaultTransaction();
        bundle.addEjb(ejb);
        return ejb;
    }


    private static String timerAttribute(EjbDescriptor ejb, Method method) {
        ContainerTransaction tx = ejb.getContainerTransactionFor(
            new MethodDescriptor(method, MethodDescriptor.TIMER_METHOD));
        assertThat(tx, not(nullValue()));
        return tx.getTransactionAttribute();
    }


    public static class MethodAnnotatedBean {

        @Schedule(hour = "*", persistent = false)
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        public void publicTimeout() {
        }

        @Schedule(hour = "*", persistent = false)
        @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
        private void privateTimeout() {
        }

        @Schedule(hour = "*", persistent = false)
        private void unannotatedTimeout() {
        }
    }


    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public static class ClassAnnotatedBean {

        @Schedule(hour = "*", persistent = false)
        private void privateTimeout() {
        }
    }
}
