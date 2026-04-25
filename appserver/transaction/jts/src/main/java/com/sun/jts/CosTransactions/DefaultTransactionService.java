/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 1995-1997 IBM Corp. All rights reserved.
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

package com.sun.jts.CosTransactions;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.enterprise.util.net.NetUtils;

import java.lang.System.Logger;
import java.util.Properties;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TSIdentification;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * The DefaultTransactionService is our implementation of Jakarta Transaction Service.
 *
 * @author Simon Holdsworth, IBM Corporation 1997
 * @author mvatkina
*/
public class DefaultTransactionService implements ProxyChecker {

    public static final String JTS_SERVER_ID = "com.sun.jts.persistentServerId"; /* FROZEN */
    public static final String JTS_XA_SERVER_NAME = "com.sun.jts.xa-servername";

    private static final Logger LOG = System.getLogger(DefaultTransactionService.class.getName());

    private static CurrentImpl currentInstance = null;
    private static TransactionFactoryImpl factoryInstance = null;
    //private static AdministrationImpl adminInstance = null;
    private static NamingContext namingContext = null;
    private static boolean recoverable = false;
    private static boolean poasCreated = false;
    private static boolean active = false;
    private static boolean orbEnabled;


    /**Default constructor.
     *
     *
     *
     */
    public DefaultTransactionService() {
        // We do not set up the Current instance until we know the ORB.
        // This method is not traced as trace is not configured until the init method
        // is called.

    }

    /**
     * @return true, if transaction manager is available.
     */
    public static boolean isActive() {
        return active;
    }

    /**Obtain the implementation of the Current interface provided
     * by the transaction service implementation.
     *
     *
     * @return An instance of the Current class
     *
     */
    public org.omg.CosTransactions.Current get_current() {
        org.omg.CosTransactions.Current result = null;

        result = currentInstance;

        return result;
    }

    /**Request the transaction service to identify itself with a communication
     * manager.
     * <p>
     * Multiple communication managers may request a transaction
     * service to identify itself.
     *
     * @param orb         The ORB to be used for communication.
     * @param ident       The TSIdentification object with which the Sender and
     *                    Receiver must be registered.
     * @param properties  The Properties with which the ORB was initialised.
     *
     *
     */
    public void identify_ORB(ORB orb,
                             TSIdentification ident,
                             Properties properties) {
        if (!orbEnabled) {
            orbEnabled = orb != null;
            Configuration.setORB(orb);
            Configuration.setProperties(properties);
            Configuration.setProxyChecker(this);
        }

        // We have to wait until this point to trace entry into this method as trace
        // is only started by setting the properties in the Configuration class.

        // Get the persistent server id of the server.  If it does not represent a
        // transient server, then the server is recoverable.

        if( !poasCreated ) {
            setServerName(properties);
            // Set up the POA objects for transient and persistent references.

            try {
                if (orb != null) {
                    createPOAs(orb);
                }
            } catch (Exception exc) {
                throw new INTERNAL(MinorCode.TSCreateFailed, CompletionStatus.COMPLETED_NO);
            }
        }

        // Set up the instance of the Current object now that we know the ORB.

        if( currentInstance == null ) {
            try {
                    currentInstance = new CurrentImpl();
                } catch( Exception exc ) {
                    throw new INTERNAL(MinorCode.TSCreateFailed,CompletionStatus.COMPLETED_NO);
                }
        }

        // Identify Sender and Receiver objects to the Comm Manager.

        if (ident != null) {
            SenderReceiver.identify(ident);
        }

        // If the server is recoverable, create a NamingContext with which to
        // register the factory and admin objects.

        if( recoverable && namingContext == null ) {
            try {
                namingContext = NamingContextHelper.narrow(orb.resolve_initial_references("NameService"/*#Frozen*/));
            } catch( Exception e ) {
                LOG.log(DEBUG,"The ORB daemon, ORBD, is not running.", e);
            }
        }

        // Create a TransactionFactory object and register it with the naming service
        // if recoverable.

        if( factoryInstance == null ) {
            boolean localFactory = true;
            TransactionFactory factory = null;
            factoryInstance = new TransactionFactoryImpl();
            if (localFactory) {
                factory = factoryInstance;
            } else {
                factory = factoryInstance.object();
            }
            try {

                // Since we are instantiating the TransactionFactory object
                // locally, we have a local transaction factory.

                Configuration.setFactory(factory, /*local factory*/ localFactory);

                if (Configuration.isLocalFactory() == false &&  namingContext != null) {
                    NameComponent nc = new NameComponent(TransactionFactoryHelper.id(),"");
                    NameComponent path[] = {nc};
                    namingContext.rebind(path,factory);
                }

                // Commented out by TN
                //if( !recoverable )
                //LOG.log(Level.WARNING,"jts.non_persistent_server");
            } catch( Exception e ) {
                LOG.log(WARNING, "Cannot register [" + factory + "] instance with the ORB.", e);
            }
        }

            active = true; // transaction manager is alive and available
    }

    public static void setServerName(Properties properties) {
        if( !poasCreated ) {
            Configuration.setProperties(properties);

            //String serverId = properties.getProperty("com.sun.corba.ee.internal.POA.ORBServerId"/*#Frozen*/);
            String serverId = properties.getProperty(JTS_SERVER_ID);
            if (serverId == null) {
                serverId = properties.getProperty("com.sun.CORBA.POA.ORBServerId"/* #Frozen */);
            }
            if (serverId != null) {
                LOG.log(INFO,"Recoverable JTS instance, serverId = [{0}]",serverId);
            }
            String serverName = "UnknownHost"/*#Frozen*/;
            if (properties.getProperty(JTS_XA_SERVER_NAME) == null) {
                serverName = NetUtils.getHostName();
            } else {
                serverName = properties.getProperty(JTS_XA_SERVER_NAME);
                LOG.log(DEBUG, "DTR: Got serverName from JTS_XA_SERVER_NAME");
            }
            if (serverId != null) {
                Configuration.setServerName(
                    getAdjustedServerName(serverName + "," + Configuration.getPropertyValue(Configuration.INSTANCE_NAME)
                        + ",P" + serverId/* #Frozen */),
                    true);
                LOG.log(DEBUG, "DTR: Recoverable Server");
                recoverable = true;
            } else {
                long timestamp = System.currentTimeMillis();
                Configuration.setServerName(
                    getAdjustedServerName(serverName + ",T" + String.valueOf(timestamp)/* #Frozen */), false);
                LOG.log(DEBUG,"DTR: Non-Recoverable Server");
            }
        }
    }

    /**Request the transaction service to stop any further transactional activity.
     *
     * @param immediate  Indicates whether to ignore running transactions.
     *
     *
     */
    public static void shutdown( boolean immediate ) {
        // Remove the admin and factory objects from the naming service.

        if( namingContext != null ) {
            try {
                NameComponent nc = new NameComponent(TransactionFactoryHelper.id(),"");
                NameComponent path[] = {nc};
                namingContext.unbind(path);
                namingContext = null;
            } catch( Exception exc ) {}
        }

        // Inform the local TransactionFactory and CurrentImpl classes that no more
        // transactional activity may occur.

        TransactionFactoryImpl.deactivate();
        CurrentImpl.deactivate();

        // Shut down the basic transaction services.

        currentInstance.shutdown(immediate);

        // Discard the factory and current instances.

        currentInstance = null;
        factoryInstance = null;
        //adminInstance   = null;
        active = false;
    }

    /**Determines whether the given object is a proxy.
     *
     * @param obj  The potential proxy.
     *
     * @return  Indicates whether the object is a proxy.
     *
     */
    @Override
    public final boolean isProxy( org.omg.CORBA.Object obj ) {

        // TN  POA changes
        return !( StubAdapter.isStub(obj) && StubAdapter.isLocal(obj) );
    }

    /**
     * Creates the POA objects which are used for objects within the JTS.
     *
     * @param orb
     * @throws Exception The operation failed.
     */
    static final void createPOAs(ORB orb) throws Exception {

        POA rootPOA = (POA)orb.resolve_initial_references("RootPOA"/*#Frozen*/);

        // Create the POA used for CoordinatorResource objects.

        POA CRpoa = null;
        if( recoverable ) {

            // Create the POA with PERSISTENT and USE_SERVANT_MANAGER policies.

            Policy[] tpolicy = new Policy[2];
            tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
            tpolicy[1] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
            CRpoa = rootPOA.create_POA("com.sun.jts.CosTransactions.CoordinatorResourcePOA"/*#Frozen*/, null, tpolicy);

            // Register the ServantActivator with the POA, then activate POA.

            CoordinatorResourceServantActivator crsa = new CoordinatorResourceServantActivator(orb);
            CRpoa.set_servant_manager(crsa);
        } else { // If the process is not recoverable, then we do not create a persistent POA.
            CRpoa = rootPOA;
        }

        Configuration.setPOA("CoordinatorResource"/*#Frozen*/,CRpoa);

        // Create the POA used for RecoveryCoordinator objects.

        POA RCpoa = null;
        if( recoverable ) {

            // Create the POA with PERSISTENT and USE_SERVANT_MANAGER policies.

            Policy[] tpolicy = new Policy[2];
            tpolicy[0] = rootPOA.create_lifespan_policy(LifespanPolicyValue.PERSISTENT);
            tpolicy[1] = rootPOA.create_request_processing_policy(RequestProcessingPolicyValue.USE_SERVANT_MANAGER);
            RCpoa = rootPOA.create_POA("com.sun.jts.CosTransactions.RecoveryCoordinatorPOA"/*#Frozen*/, null, tpolicy);

            // Register the ServantActivator with the POA, then activate POA.

            RecoveryCoordinatorServantActivator rcsa = new RecoveryCoordinatorServantActivator(orb);
            RCpoa.set_servant_manager(rcsa);
        } else { // If the process is not recoverable, then we do not create a persistent POA.
            RCpoa = rootPOA;
        }

        Configuration.setPOA("RecoveryCoordinator"/*#Frozen*/,RCpoa);

        // Create the POA used for Coordinator objects.

        POA Cpoa = rootPOA.create_POA("CoordinatorPOA"/*#Frozen*/, null, null);
        //  POA Cpoa = rootPOA;
        Configuration.setPOA("Coordinator"/*#Frozen*/,Cpoa);

        // Create the POA used for transient objects.

        Configuration.setPOA("transient"/*#Frozen*/,rootPOA);

        CRpoa.the_POAManager().activate();
        RCpoa.the_POAManager().activate();
        Cpoa.the_POAManager().activate();
        rootPOA.the_POAManager().activate();

        poasCreated = true;

    }

    public static boolean isORBAvailable() {
        return orbEnabled;
    }

    private static String getAdjustedServerName(String originalName) {
        final String tempServerName;
        if (originalName.length() > 56) {
            int hc = originalName.hashCode();
            String newString = Integer.toString(hc);

            if (hc < 0) {
                 newString = newString.replace("-", "R");
            }

            int hcLength = (56 - newString.length());
            tempServerName = originalName.substring(0, hcLength) + newString;
        } else {
            tempServerName = originalName;
        }
        LOG.log(DEBUG,() -> "DTR: Adjusted serverName " + originalName + " to: " + tempServerName );

        return tempServerName;
    }
}

/**The RecoveryCoordinatorServantActivator class provides the means to locate
 * instances of the RecoveryCoordinator class using the transaction identifier
 * and the RecoveryManager.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
*/
//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//------------------------------------------------------------------------------

class RecoveryCoordinatorServantActivator extends LocalObject implements ServantActivator {
    private ORB orb = null;

    /*
     * COMMENT(Ram J) The _ids() method needs to be removed/modified
     * (along with LocalObjectImpl) once OMG standardizes the local
     * object interfaces.
     *
     * TN - removed ids after switching to LocalObject
     */

     // Type-specific CORBA::Object operations
    /*
    private static String[] __ids = {
        "IDL:omg.org/PortableServer/ServantActivator:1.0",
        "IDL:omg.org/PortableServer/ServantManager:1.0"
    };

    public String[] _ids () {
        return __ids;
    }
    */

    /**Creates the servant activator for the RecoveryCoordinator class.
     *
     * @param  orb  The ORB.
     *
     *
     */
    RecoveryCoordinatorServantActivator(ORB orb) {
        this.orb = orb;
    }

    /**Returns the servant object which corresponds to the given object identity
     * for the given POA.
     *
     * @param oid      The object identifier.
     * @param adapter  The POA.
     *
     * @return  The servant.
     *
     */
    @Override
    public Servant incarnate( byte[] oid, POA adapter )
        throws org.omg.PortableServer.ForwardRequest {

        Servant servant = new RecoveryCoordinatorImpl(oid);

        return servant;
    }

    /**Does nothing.
     *
     *
     *
     */
    @Override
    public void etherealize( byte[] oid,
                             POA adapter,
                             Servant servant,
                             boolean cleanup_in_progress,
                             boolean remaining_activations ) {
    }
}

/**The CoordinatorResourceServantActivator class provides the means to locate
 * instances of the CoordinatorResource class after a failure, using the
 * transaction identifier and the RecoveryManager.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 *
*/
//----------------------------------------------------------------------------
// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//------------------------------------------------------------------------------

class CoordinatorResourceServantActivator extends LocalObject implements ServantActivator {
    private ORB orb = null;

    /*
     * COMMENT(Ram J) The _ids() method needs to be removed/modified
     * (along with LocalObjectImpl) once OMG standardizes the local
     * object interfaces.
     *
     * TN - removed ids after switching to LocalObject
     */

    /*
     // Type-specific CORBA::Object operations
    private static String[] __ids = {
        "IDL:omg.org/PortableServer/ServantActivator:1.0",
        "IDL:omg.org/PortableServer/ServantManager:1.0"
    };

    public String[] _ids () {
        return __ids;
    }
    */

    /**Creates the servant activator for the CoordinatorResource class.
     *
     * @param  orb  The ORB.
     *
     *
     */
    CoordinatorResourceServantActivator(ORB orb) {
        this.orb = orb;
    }

    /**Returns the servant object which corresponds to the given object identity
     * for the given POA.
     *
     * @param oid      The object identifier.
     * @param adapter  The POA.
     *
     * @return  The servant.
     *
     */
    @Override
    public Servant incarnate( byte[] oid, POA adapter )
        throws org.omg.PortableServer.ForwardRequest {
        Servant servant = new CoordinatorResourceImpl(oid);
        return servant;
    }

    /**Does nothing.
     *
     *
     *
     */
    @Override
    public void etherealize( byte[] oid,
                             POA adapter,
                             Servant servant,
                             boolean cleanup_in_progress,
                             boolean remaining_activations ) {
    }
}

