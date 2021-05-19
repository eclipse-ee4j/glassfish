/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.cdi.extension;

import jakarta.inject.Singleton;

/**
 * This is a service that has fields indicating whether or not
 * the various CDI extension points were properly reached
 *
 * @author jwells
 *
 */
@Singleton
public class HK2ExtensionVerifier {
    private boolean afterBeanDiscoveryCalled = false;
    private boolean afterDeploymentValidationCalled = false;
    private boolean processAnnotatedTypeCalled = false;
    private boolean processInjectionTargetCalled = false;
    private boolean processProducerCalled = false;
    private boolean processManagedBeanCalled = false;
    private boolean processSessionBeanCalled = false;
    private boolean processProducerMethodCalled = false;
    private boolean processProducerFieldCalled = false;
    private boolean processObserverMethodCalled = false;

    public void afterBeanDiscoveryCalled() {
        afterBeanDiscoveryCalled = true;
    }

    public void afterDeploymentValidationCalled() {
        afterDeploymentValidationCalled = true;
    }

    public void processAnnotatedTypeCalled() {
        processAnnotatedTypeCalled = true;
    }

    public void processInjectionTargetCalled() {
        processInjectionTargetCalled = true;
    }

    public void processProducerCalled() {
        processProducerCalled = true;
    }

    public void processManagedBeanCalled() {
        processManagedBeanCalled = true;
    }

    public void processSessionBeanCalled() {
        processSessionBeanCalled = true;
    }

    public void processProducerMethodCalled() {
        processProducerMethodCalled = true;
    }

    public void processProducerFieldCalled() {
        processProducerFieldCalled = true;
    }

    public void processObserverMethodCalled() {
        processObserverMethodCalled = true;
    }

    public void validate() {
        if (!afterBeanDiscoveryCalled) {
            throw new AssertionError("AfterBeanDiscovery was not able to get the ServiceLocator");
        }

        if (!afterDeploymentValidationCalled) {
            throw new AssertionError("AfterDeploymentValidation was not able to get the ServiceLocator");
        }

        if (!processAnnotatedTypeCalled) {
            throw new AssertionError("ProcessAnnotatedType was not able to get the ServiceLocator");
        }

        if (!processInjectionTargetCalled) {
            throw new AssertionError("ProcessInjectionTarget was not able to get the ServiceLocator");
        }

        if (!processProducerCalled) {
            throw new AssertionError("ProcessProducer was not able to get the ServiceLocator");
        }

        if (!processManagedBeanCalled) {
            throw new AssertionError("ProcessManagedBean was not able to get the ServiceLocator");
        }

        if (!processSessionBeanCalled) {
            throw new AssertionError("ProcessSessionBean was not able to get the ServiceLocator");
        }

        if (!processProducerMethodCalled) {
            throw new AssertionError("ProcessProducerMethod was not able to get the ServiceLocator");
        }

        if (!processProducerFieldCalled) {
            throw new AssertionError("ProcessProducerField was not able to get the ServiceLocator");
        }

        if (!processObserverMethodCalled) {
            throw new AssertionError("ProcessObserverMethod was not able to get the ServiceLocator");
        }
    }
}
