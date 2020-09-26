/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.beans.mock;


import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.Specializes;

import test.beans.TestBeanInterface;
import test.beans.nonmock.AnotherQualifierProducer;
import test.beans.nonmock.TestBeanForAnotherQualifier;

public class MockAnotherQualifierProducer extends AnotherQualifierProducer{
    @Produces @Specializes
    //We don't specify AnotherQualifier here. It is inherited from 
    //AnotherQualifierProducer.getTestBeanWithAnotherQualifier
    //because of the @Specializes annotation
    protected TestBeanInterface getTestBeanWithAnotherQualifier() {
        return new MockTestBeanForAnotherQualifier();
    }

}
