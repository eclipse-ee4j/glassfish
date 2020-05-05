/*
 * Copyright (c) 2017, 2020 Oracle and/or its affiliates. All rights reserved.
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

package gf;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.ejb.SessionContext;
import jakarta.ejb.MessageDrivenContext;
import jakarta.ejb.TimerService;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedExecutorService;
import jakarta.enterprise.concurrent.ManagedScheduledExecutorService;
import jakarta.enterprise.concurrent.ManagedThreadFactory;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.jms.ConnectionFactory;
import javax.sql.DataSource;
import jakarta.transaction.UserTransaction;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.Validator;

import org.omg.CORBA.ORB;

public class TestServlet {
    // Stored in Resource Reference Descriptor
    // java:comp/DefaultDataSource
    @Resource DataSource ds;

    // java:comp/DefaultJMSConnectionFactory
    @Resource ConnectionFactory cf;

    // java:comp/ORB
    @Resource ORB orb;


    // Stored in Resource Env Ref Descriptor
    // java:comp/DefaultManagedExecutorService
    @Resource ManagedExecutorService managedExecutorService;

    // java:comp/DefaultManagedScheduledExecutorService
    @Resource ManagedScheduledExecutorService managedScheduledExecutorService;

    // java:comp/DefaultManagedThreadFactory
    @Resource ManagedThreadFactory managedThreadFactory;

    // java:comp/DefaultContextService
    @Resource ContextService contextService;

    // java:comp/UserTransaction
    @Resource UserTransaction userTransaction;

    // java:comp/TransactionSynchronizationRegistry
    @Resource TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    // java:comp/BeanManager
    @Resource BeanManager manager;

    // java:comp/ValidatorFactory
    @Resource ValidatorFactory validatorFactory;

    // java:comp/Validator
    @Resource Validator validator;

    @Resource SessionContext sessionContext;

    @Resource MessageDrivenContext messageDrivenContext;

    @Resource EJBContext ejbContext;

    @Resource TimerService timerService;


    // Stored in Env Props
    @Resource(lookup="java:module/ModuleName") String moduleName;

    @Resource(lookup="java:app/AppName") String appName;

    @Resource(lookup="java:comp/InAppClientContainer") boolean inAppClient;
}
