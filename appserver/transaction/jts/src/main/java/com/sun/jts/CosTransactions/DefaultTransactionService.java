/*
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

//----------------------------------------------------------------------------
//
// Module:      DefaultTransactionService.java
//
// Description: The JTS TransactionService class.
//
// Product:     com.sun.jts.CosTransactions
//
// Author:      Simon Holdsworth
//
// Date:        June, 1997
//----------------------------------------------------------------------------

package com.sun.jts.CosTransactions;

import com.sun.corba.ee.spi.presentation.rmi.StubAdapter;
import com.sun.logging.LogDomains;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.Policy;
import org.omg.CORBA.TSIdentification;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.CosTransactions.TransactionFactory;
import org.omg.CosTransactions.TransactionFactoryHelper;
import org.omg.PortableServer.AdapterActivator;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantActivator;

/**The DefaultTransactionService is our implemention of the
 * com.sun.CosTransactions.TransactionService class.
 * <p>
 * The TransactionService abstract class describes the packaging of a
 * Java Transaction Service implementation. Each implementation should be
 * packaged as a subclass of the TransactionService class.
 *
 * @version 0.01
 *
 * @author Simon Holdsworth, IBM Corporation
 * @author mvatkina
 *
 * @see
*/
//----------------------------------------------------------------------------// CHANGE HISTORY
//
// Version By     Change Description
//   0.01  SAJH   Initial implementation.
//-----------------------------------------------------------------------------

public class DefaultTransactionService implements ProxyChecker {
    private static CurrentImpl currentInstance = null;
    private static TransactionFactoryImpl factoryInstance = null;
    //private static AdministrationImpl adminInstance = null;
    private static NamingContext namingContext = null;
    private static ORB orb = null;
    private static boolean recoverable = false;
    private static boolean poasCreated = false;
    private static boolean active = false;
    /*
        Logger to log transaction messages
    */
    static Logger _logger = LogDomains.getLogger(DefaultTransactionService.class, LogDomains.TRANSACTION_LOGGER);
    public static final String JTS_SERVER_ID = "com.sun.jts.persistentServerId"; /* FROZEN */
    public static final String JTS_XA_SERVER_NAME = "com.sun.jts.xa-servername";


    /**Default constructor.
     *
     * @param
     *
     * @return
     *
     * @see
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
     * @param
     *
     * @return An instance of the Current class
     *
     * @see
     */
    public org.omg.CosTransactions.Current get_current() {
        org.omg.CosTransactions.Current result = null;

        result = (org.omg.CosTransactions.Current)currentInstance;

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
     * @return
     *
     * @see
     */
    public void identify_ORB(ORB orb,
                             TSIdentification ident,
                             Properties properties) {
        if( this.orb == null ) {
            this.orb = orb;
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
                if (orb != null)
                    createPOAs();
            } catch( Exception exc ) {
                _logger.log(Level.WARNING,"jts.unexpected_error_when_creating_poa",exc);
                throw new INTERNAL(MinorCode.TSCreateFailed,CompletionStatus.COMPLETED_NO);
            }
        }

        // Set up the instance of the Current object now that we know the ORB.

        if( currentInstance == null )
           try {
                currentInstance = new CurrentImpl();
            } catch( Exception exc ) {
                _logger.log(Level.WARNING,"jts.unexpected_error_when_creating_current",exc);
                throw new INTERNAL(MinorCode.TSCreateFailed,CompletionStatus.COMPLETED_NO);
            }

        // Identify Sender and Receiver objects to the Comm Manager.

        if (ident != null)
            SenderReceiver.identify(ident);

        // If the server is recoverable, create a NamingContext with which to
        // register the factory and admin objects.

        if( recoverable && namingContext == null )
            try {
                namingContext = NamingContextHelper.narrow(orb.resolve_initial_references("NameService"/*#Frozen*/));
            } catch( InvalidName inexc ) {
                // _logger.log(Level.WARNING,"jts.orb_not_running");
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,"jts.orb_not_running");
                }
            } catch( Exception exc ) {
                // _logger.log(Level.WARNING,"jts.orb_not_running");
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE,"jts.orb_not_running");
                }
            }

        // Create a TransactionFactory object and register it with the naming service
        // if recoverable.

        if( factoryInstance == null )
            try {
                boolean localFactory = true;
                TransactionFactory factory = null;
                factoryInstance = new TransactionFactoryImpl();
                if (localFactory) {
                    factory = (TransactionFactory) factoryInstance;
                } else {
                    factory = factoryInstance.object();
                }

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
                //_logger.log(Level.WARNING,"jts.non_persistent_server");
            } catch( Exception exc ) {
                _logger.log(Level.WARNING,"jts.cannot_register_with_orb","TransactionFactory");
            }

            active = true; // transaction manager is alive and available
    }

    public static void setServerName(Properties properties) {
        if( !poasCreated ) {
            Configuration.setProperties(properties);

            //String serverId = properties.getProperty("com.sun.corba.ee.internal.POA.ORBServerId"/*#Frozen*/);
            String serverId = properties.getProperty(JTS_SERVER_ID);
            if (serverId == null) {
                serverId =
                    properties.getProperty("com.sun.CORBA.POA.ORBServerId"/*#Frozen*/);
            }
            if (serverId != null) {
                    _logger.log(Level.INFO,"jts.startup_msg",serverId);
            }
            String serverName = "UnknownHost"/*#Frozen*/;
            if(properties.getProperty(JTS_XA_SERVER_NAME) != null) {
                 serverName = properties.getProperty(JTS_XA_SERVER_NAME);
                 if (_logger.isLoggable(Level.FINE))
                     _logger.log(Level.FINE,"DTR: Got serverName from JTS_XA_SERVER_NAME");

            } else {
                try {
                    serverName = InetAddress.getLocalHost().getHostName();
                    if (_logger.isLoggable(Level.FINE))
                        _logger.log(Level.FINE,"DTR: Got serverName from InetAddress.getLocalHost().getHostName()");

                } catch (UnknownHostException ex) {
                }
            }
            if( serverId != null ) {
                Configuration.setServerName(getAdjustedServerName(serverName + "," +
                        Configuration.getPropertyValue(Configuration.INSTANCE_NAME) +
                        ",P" + serverId/*#Frozen*/), true);
                if (_logger.isLoggable(Level.FINE))
                    _logger.log(Level.FINE,"DTR: Recoverable Server");

                recoverable = true;
            } else {
                long timestamp = System.currentTimeMillis();
                Configuration.setServerName(getAdjustedServerName(serverName +
                         ",T" + String.valueOf(timestamp)/*#Frozen*/), false);
                if (_logger.isLoggable(Level.FINE))
                    _logger.log(Level.FINE,"DTR: Non-Recoverable Server");
            }
        }
    }

    /**Request the transaction service to stop any further transactional activity.
     *
     * @param immediate  Indicates whether to ignore running transactions.
     *
     * @return
     *
     * @see
     */
    public static void shutdown( boolean immediate ) {
        // Remove the admin and factory objects from the naming service.

        if( namingContext != null )
            try {
                NameComponent nc = new NameComponent(TransactionFactoryHelper.id(),"");
                NameComponent path[] = {nc};
                namingContext.unbind(path);

                namingContext = null;
            } catch( Exception exc ) {}

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
     * @see
     */
    public final boolean isProxy( org.omg.CORBA.Object obj ) {

        // TN  POA changes
        return !( StubAdapter.isStub(obj) && StubAdapter.isLocal(obj) );
    }

    /**Creates the POA objects which are used for objects within the JTS.
     *
     * @param
     *
     * @return
     *
     * @exception Exception  The operation failed.
     *
     * @see
     */
    final static void createPOAs()
        throws Exception {

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
        }

        // If the process is not recoverable, then we do not create a persistent POA.

        else
            CRpoa = rootPOA;

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
        }

        // If the process is not recoverable, then we do not create a persistent POA.

        else
            RCpoa = rootPOA;

        Configuration.setPOA("RecoveryCoordinator"/*#Frozen*/,RCpoa);

        // Create the POA used for Coordinator objects.

        POA Cpoa = rootPOA.create_POA("CoordinatorPOA"/*#Frozen*/, null, null);
        //  POA Cpoa = rootPOA;
        Configuration.setPOA("Coordinator"/*#Frozen*/,Cpoa);

        // Create the POA used for transient objects.

        Configuration.setPOA("transient"/*#Frozen*/,rootPOA);

        //  rootPOA.the_activator(new JTSAdapterActivator(orb));
        CRpoa.the_POAManager().activate();
        RCpoa.the_POAManager().activate();
        Cpoa.the_POAManager().activate();
        rootPOA.the_POAManager().activate();

        poasCreated = true;

    }

    public static boolean isORBAvailable() {
        return (orb != null);
    }

    private static String getAdjustedServerName(String originalName) {
        String tempServerName = originalName;
        if (tempServerName.length() > 56) {
            int hc = tempServerName.hashCode();
            String newString = Integer.toString(hc);

            if (hc < 0) {
                 newString = newString.replace("-", "R");
            }

            int hcLength = (56 - newString.length());
            tempServerName = tempServerName.substring(0, hcLength) + newString;
        }
        if (_logger.isLoggable(Level.FINE))
            _logger.log(Level.FINE,"DTR: Adjusted serverName " + originalName + " to: " + tempServerName );

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
 * @see
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
     * @return
     *
     * @see
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
     * @see
     */
    public Servant incarnate( byte[] oid, POA adapter )
        throws org.omg.PortableServer.ForwardRequest {

        Servant servant = new RecoveryCoordinatorImpl(oid);

        return servant;
    }

    /**Does nothing.
     *
     * @param
     *
     * @return
     *
     * @see
     */
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
 * @see
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
     * @return
     *
     * @see
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
     * @see
     */
    public Servant incarnate( byte[] oid, POA adapter )
        throws org.omg.PortableServer.ForwardRequest {
        Servant servant = new CoordinatorResourceImpl(oid);
        return servant;
    }

    /**Does nothing.
     *
     * @param
     *
     * @return
     *
     * @see
     */
    public void etherealize( byte[] oid,
                             POA adapter,
                             Servant servant,
                             boolean cleanup_in_progress,
                             boolean remaining_activations ) {
    }
}

class JTSAdapterActivator extends LocalObject implements AdapterActivator {
    private ORB orb;

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
        "IDL:omg.org/PortableServer/AdapterActivator:1.0"
    };

    public String[] _ids() {
        return __ids;
    }
    */

    JTSAdapterActivator(ORB orb) {
        this.orb = orb;
    }

    public boolean unknown_adapter(POA parent, String name) {

        try {
            DefaultTransactionService.createPOAs();
        } catch( Exception exc ) {
        }

        return true;
    }
}
