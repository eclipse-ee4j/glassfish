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

package com.sun.ejb;

import com.sun.enterprise.security.SecurityManager;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBContext;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.FinderException;
import jakarta.transaction.TransactionManager;

import java.rmi.Remote;

import javax.naming.spi.NamingManager;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.enterprise.iiop.api.ProtocolManager;

/**
 * A Container stores EJB instances and is responsible for the lifecycle, state management,
 * concurrency, transactions, security, naming, resource management, etc.
 * It does the above by interposing actions before and after invocations on EJBs.
 * It uses the {@link ProtocolManager}, {@link SecurityManager}, {@link TransactionManager},
 * {@link NamingManager} for help with the above responsibilities.
 * <p>
 * Note: the term "Container" here refers to an instance of one of the above container classes.
 * In the EJB spec "container" refers to a process or JVM which hosts EJB instances.
 * <p>
 * There is one instance of the Container for each EJB type (deployment desc).
 * When a JAR is deployed on the EJB server, a Container instance is created
 * for each EJB declared in the ejb-jar.xml for the EJB JAR.
 * <p>
 * The {@link Container} interface provides methods called from other parts of
 * the server as well as from generated {@link EJBHome}/{@link EJBObject} implementations.
 */
public interface Container {

    // These values are for the transaction attribute of a bean method
    /** default */
    int TX_NOT_INITIALIZED = 0;
    int TX_NOT_SUPPORTED = 1;
    int TX_BEAN_MANAGED = 2;
    int TX_REQUIRED = 3;
    int TX_SUPPORTS = 4;
    int TX_REQUIRES_NEW = 5;
    int TX_MANDATORY = 6;
    int TX_NEVER = 7;

    // Must match the values of the tx attributes above.
    String[] txAttrStrings = {
        "TX_NOT_INITIALIZED",
        "TX_NOT_SUPPORTED",
        "TX_BEAN_MANAGED",
        "TX_REQUIRED",
        "TX_SUPPORTS",
        "TX_REQUIRES_NEW",
        "TX_MANDATORY",
        "TX_NEVER",
    };

    // These values are for the security attribute of a bean method
    /** default */
    int SEC_NOT_INITIALIZED = 0;
    int SEC_UNCHECKED = 1;
    int SEC_EXCLUDED = 2;
    int SEC_CHECKED = 3;

    String[] secAttrStrings = {
        "SEC_NOT_INITIALIZED",
        "SEC_UNCHECKED",
        "SEC_EXCLUDED",
        "SEC_CHECKED",
    };


    /**
     * Return the EJBObject/EJBHome for the given instanceKey.
     * @param remoteBusinessIntf True if this invocation is for the RemoteHome
     * view of the bean.  False if for the RemoteBusiness view.
     * Called from the ProtocolManager when a remote invocation arrives.
     */
    Remote getTargetObject(byte[] instanceKey, String remoteBusinessIntf);

    /**
     * Release the EJBObject/EJBHome object.
     * Called from the ProtocolManager after a remote invocation completes.
     */
    void releaseTargetObject(Remote remoteObj);

    /**
     * Performs pre external invocation setup such as setting application
     * context class loader.  Called by getTargetObject() and web service inv
     */
    void externalPreInvoke();

    /**
     * Performs post external invocation cleanup such as restoring the original
     * class loader.  Called by releaseTargetObject() and web service inv
     */
    void externalPostInvoke();

    /**
     * Obtain an Entity EJBObject corresponding to the primary key.
     * Used by the PersistenceManager.
     */
    EJBObject getEJBObjectForPrimaryKey(Object pkey);

    /**
     * Obtain an Entity EJBLocalObject corresponding to the primary key.
     * Used by the PersistenceManager.
     */
    EJBLocalObject getEJBLocalObjectForPrimaryKey(Object pkey, EJBContext ctx);
    EJBLocalObject getEJBLocalObjectForPrimaryKey(Object pkey);

    /**
     * Verify that a given object is an EJBLocalObject of an ejb from this
     * ejb container.  The given object must be an EJBLocalObject and have
     * the same ejb type ( meaning same ejb-jar and same ejb-name ) as this
     * container.  Note that for entity beans this equality check is independent of
     * primary key.
     *
     * @exception EJBException Thrown when the assertion fails.
     */
    void assertValidLocalObject(Object o) throws EJBException;

    /**
     * Verify that a given object is an EJBObject of an ejb from this
     * ejb container.  The given object must be an EJBObject and have
     * the same ejb type ( meaning same ejb-jar and same ejb-name ) as this
     * container.  Note that for entity beans this equality check is independent of
     * primary key.
     *
     * @exception EJBException Thrown when the assertion fails.
     */
    void assertValidRemoteObject(Object o) throws EJBException;

    /**
     * Remove a bean. Used by the PersistenceManager.
     */
    void removeBeanUnchecked(EJBLocalObject bean);

    /**
     * Remove a bean given primary key. Used by the PersistenceManager.
     */
    void removeBeanUnchecked(Object pkey);

    /**
     * Notification from persistence manager than an ejbSelect
     * query is about to be invoked on a bean of the ejb type
     * for this container.    This allows the ejb container
     * to perform the same set of actions as take place before a
     * finder method, such as calling ejbStore on bean instances.
     * (See EJB 2.1, Section 10.5.3 ejbFind,ejbStore)
     *
     * @exception jakarta.ejb.EJBException  Thrown if an error occurs
     *          during the preSelect actions performed by the container.
     *          If thrown, the remaining select query steps should be
     *          aborted and an EJBException should be propagated
     *          back to the application code.
     */
    void preSelect() throws jakarta.ejb.EJBException;


    /**
     * Called by the EJB(Local)Object/EJB(Local)Home before an invocation
     * on a bean.
     */
    void preInvoke(EjbInvocation inv);

    /**
     * Called by the EJB(Local)Object/EJB(Local)Home after an invocation
     * on a bean.
     */
    void postInvoke(EjbInvocation inv);

    /**
     * Called by webservice code to do ejb invocation post processing.
     */
    void webServicePostInvoke(EjbInvocation inv);

    /**
     * Called by the EJB(Local)Home after invoking ejbCreate on an EntityBean.
     * After this postCreate the EJB(Local)Home can call ejbPostCreate on
     * the EntityBean.
     * @param primaryKey the value returned from ejbCreate.
     */
    void postCreate(EjbInvocation inv, Object primaryKey)
    throws CreateException;

    /**
     * Called by the EJB(Local)Home after invoking ejbFind* on an EntityBean.
     * @param primaryKeys the primaryKey or collection of primaryKeys
     *        (Collection/Enumeration) returned from ejbFind.
     * @param findParams the parameters to the ejbFind method.
     * @return an EJBObject reference or Collection/Enumeration of EJBObjects.
     */
    Object postFind(EjbInvocation inv, Object primaryKeys, Object[] findParams)
    throws FinderException;

    /**
     * @return the EjbDescriptor containing deployment information
     * for the EJB type corresponding to this Container instance.
     */
    EjbDescriptor getEjbDescriptor();

    /**
     * @return the MetaData for this EJB type.
     */
    EJBMetaData getEJBMetaData();

    /**
     * @return the classloader of this container instance.
     */
    ClassLoader getClassLoader();

    /**
     * @return the EJBHome object reference for this container instance.
     */
    EJBHome getEJBHome();

    /**
     * @return A SecurityManager object for this container.
     */
    SecurityManager getSecurityManager();

    /**
     * EJB spec makes a distinction between access to the UserTransaction
     * object itself and access to its methods.  getUserTransaction covers
     * the first check and this method covers the second.  It is called
     * by the UserTransaction implementation to verify access.
     */
    boolean userTransactionMethodsAllowed(ComponentInvocation inv);

    /**
     * Called from the TM when an EJB with Bean-Managed transactions starts a tx
     */
    void doAfterBegin(ComponentInvocation ci);


    /**
     * Called after all the components in the container's application
     * have loaded successfully.  Allows containers to delay
     * any instance creation or external invocations until the second
     * phase of deployment.  Note that this callback occurs at a point
     * that is still considered within deployment.  Failures should still
     * still be treated as a deployment error.
     * @param deploy true if this method is called during application deploy
     */
    void startApplication(boolean deploy);

    /**
     * Called from EJB JarManager when an application is undeployed.
     */
    void undeploy();

    /**
     * Called when server instance is Ready
     */
    void onReady();

    /**
     * Called when the request started it's processing in the container
     */
    void onEnteringContainer();

    /**
     * Called when the request finished it's processing in the container
     */
    void onLeavingContainer();

    /**
     * Called when server instance is shuting down
     */
    void onShutdown();

    /**
     * Called when server instance is terminating. This method is the last
     * one called during server shutdown.
     */
    void onTermination();

    /**
     * Called from NamingManagerImpl during java:comp/env lookup.
     */
    String getComponentId();

    /**
     * Start servicing invocations for EJB instances in this Container.

     */
     void setStartedState();

    /**
     * Stop servicing invocations for EJB instances in this Container.
     * Subsequent EJB invocations will receive exceptions.
     * Invocations already in progress will be allowed to complete eventually.
     */
     void setStoppedState();

    /**
     * Stop servicing invocations for EJB instances in this Container as the
     * container is being undeployed.
     * No new EJB invocations will be accepted from now on.
     * Invocations already in progress will be allowed to complete eventually.
     */
     void setUndeployedState();

    /**
     * Used by EjbInvocation during JACC EnterpriseBean policy handler request
     * for target EnterpriseBean instance.
     *
     * @return EnterpriseBean instance or null if not applicable for this
     *         invocation.
     */
    Object getJaccEjb(EjbInvocation inv);

    /**
     * Go through ejb container to do ejb security manager authorization.
     */
    boolean authorize(EjbInvocation inv);

    /**
     * Returns true if this Container uses EJB Timer Service.
     */
    boolean isTimedObject();

    /**
     * Returns true if the bean associated with this Container has a LocalHome/Local view
     * OR a Local business view OR both.
     */
    boolean isLocalObject();

    /**
     * Returns true if the bean associated with this Container has a RemoteHome/Remote view
     * OR a Remote business view OR both.
     */
    boolean isRemoteObject();
}
