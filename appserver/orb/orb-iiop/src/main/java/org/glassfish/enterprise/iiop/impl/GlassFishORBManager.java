/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.impl.folb.InitialGroupInfoService ;
import com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl;
import com.sun.corba.ee.impl.javax.rmi.CORBA.Util;
import com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject;
import com.sun.corba.ee.impl.orb.ORBImpl;
import com.sun.corba.ee.impl.orb.ORBSingleton;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.oa.rfm.ReferenceFactoryManager;
import com.sun.corba.ee.spi.orb.ORB ;
import com.sun.corba.ee.spi.osgi.ORBFactory;
import com.sun.enterprise.config.serverbeans.SslClientConfig;
import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.util.Utility;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.api.admin.ProcessEnvironment;
import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.enterprise.iiop.api.GlassFishORBLifeCycleListener;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ORBLocator;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.IiopService;
import org.glassfish.orb.admin.config.Orb;
import org.jvnet.hk2.config.types.Property;

import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * This class initializes the ORB with a list of (standard) properties
 * and provides a few convenience methods to get the ORB etc.
 */
public final class GlassFishORBManager {

    private static final Logger LOG = IIOPImplLogFacade.getLogger(GlassFishORBManager.class);

    private static final Properties EMPTY_PROPERTIES = new Properties();

    // Various pluggable classes defined in the app server that are used
    // by the ORB.
    private static final String ORB_CLASS = ORBImpl.class.getName();
    private static final String ORB_SINGLETON_CLASS = ORBSingleton.class.getName();

    private static final String PEORB_CONFIG_CLASS = PEORBConfigurator.class.getName();
    private static final String IIOP_SSL_SOCKET_FACTORY_CLASS = IIOPSSLSocketFactory.class.getName();
    private static final String RMI_UTIL_CLASS = Util.class.getName();
    private static final String RMI_STUB_CLASS = StubDelegateImpl.class.getName();
    private static final String RMI_PRO_CLASS = PortableRemoteObject.class.getName();

    // JNDI constants
    public static final String JNDI_PROVIDER_URL_PROPERTY = "java.naming.provider.url";
    public static final String JNDI_CORBA_ORB_PROPERTY = "java.naming.corba.orb";

    // RMI-IIOP delegate constants
    public static final String ORB_UTIL_CLASS_PROPERTY = "javax.rmi.CORBA.UtilClass";
    public static final String RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY = "javax.rmi.CORBA.StubClass";
    public static final String RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY = "javax.rmi.CORBA.PortableRemoteObjectClass";

    // ORB constants: OMG standard
    public static final String OMG_ORB_CLASS_PROPERTY = "org.omg.CORBA.ORBClass";
    public static final String OMG_ORB_SINGLETON_CLASS_PROPERTY = "org.omg.CORBA.ORBSingletonClass";

    // ORB constants: Sun specific
    public static final String SUN_ORB_SOCKET_FACTORY_CLASS_PROPERTY = ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY;

    // ORB configuration constants
    private static final String DEFAULT_SERVER_ID = "100";
    private static final String ACC_DEFAULT_SERVER_ID = "101";
    private static final String USER_DEFINED_ORB_SERVER_ID_PROPERTY = "org.glassfish.orb.iiop.orbserverid";

    private static final String DEFAULT_MAX_CONNECTIONS = "1024";
    private static final String GLASSFISH_INITIALIZER = GlassFishORBInitializer.class.getName();

    private static final String SUN_GIOP_DEFAULT_FRAGMENT_SIZE = "1024";
    private static final String SUN_GIOP_DEFAULT_BUFFER_SIZE = "1024";

    public static final String DEFAULT_ORB_INIT_HOST = "localhost";

    // This will only apply for stand-alone java clients, since
    // in the server the orb port comes from domain.xml, and in an appclient
    // the port is set from the sun-acc.xml. It's set to the same
    // value as the default orb port in domain.xml as a convenience.
    // That way the code only needs to do a "new InitialContext()"
    // without setting any jvm properties and the naming service will be
    // found. Of course, if the port was changed in domain.xml for some
    // reason the code will still have to set org.omg.CORBA.ORBInitialPort.
    public static final String DEFAULT_ORB_INIT_PORT = "3700";

    private static final String ORB_SSL_STANDALONE_CLIENT_REQUIRED = "com.sun.CSIV2.ssl.standalone.client.required";

    // We need this to get the ORB monitoring set up correctly
    private static final String S1AS_ORB_ID = "S1AS-ORB";
    private static final String IIOP_ENDPOINTS_PROPERTY = "com.sun.appserv.iiop.endpoints";
    private static final String IIOP_URL = "iiop:1.2@";

    // Set in constructor
    private final ServiceLocator services;
    private final IIOPUtils iiopUtils;

    // the ORB instance
    private ORB orb;

    // The ReferenceFactoryManager from the orb.
    private ReferenceFactoryManager rfm;

    private int orbInitialPort = -1;

    private List<IiopListener> iiopListeners;
    private Orb orbBean;
    private IiopService iiopService;

    private final Properties csiv2Props = new Properties();

    private final ProcessType processType;

    private IiopFolbGmsClient gmsClient;

    /**
     * Keep this class private to the package.  Eventually we need to
     * move all public statics or change them to package private.
     * All external orb/iiop access should go through orb-connector module
     */
    GlassFishORBManager(ServiceLocator h ) {
        LOG.log(Level.CONFIG, "GlassFishORBManager({0})", h);
        services = h;
        iiopUtils = services.getService(IIOPUtils.class);
        ProcessEnvironment processEnv = services.getService(ProcessEnvironment.class);
        processType = processEnv.getProcessType();
        initProperties();
    }

    /**
     * Returns whether an adapterName (from ServerRequestInfo.adapter_name)
     * represents an EJB or not.
     * @param adapterName The adapter name
     * @return whether this adapter is an EJB or not
     */
    public boolean isEjbAdapterName(String[] adapterName) {
        boolean result = false;
        if (rfm != null) {
            result = rfm.isRfmName(adapterName);
        }
        return result;
    }

    /**
     * Returns whether the operationName corresponds to an "is_a" call
     * or not (used to implement PortableRemoteObject.narrow.
     */
    boolean isIsACall(String operationName) {
        return operationName.equals("_is_a");
    }

    /**
     * Return the shared ORB instance for the app server.
     * If the ORB is not already initialized, it is created
     * with the standard server properties, which can be
     * overridden by Properties passed in the props argument.
     */
    synchronized ORB getORB(Properties props) {
        LOG.log(Level.FINEST, "getORB({0})", props);
        if (orb == null) {
            initORB(props);
            LOG.log(Level.INFO, "ORB initialization succeeded: {0}", orb);
        }
        return orb;
    }

    Properties getCSIv2Props() {
        // Return a copy of the CSIv2Props
        return new Properties(csiv2Props);
    }

    void setCSIv2Prop(String name, String value) {
        csiv2Props.setProperty(name, value);
    }

    int getORBInitialPort() {
        return orbInitialPort;
    }

    private void initProperties() {
        LOG.log(Level.FINEST, "initProperties(); processType: {0}", processType);
        if (processType != ProcessType.ACC) {
            String sslClientRequired = System.getProperty(ORB_SSL_STANDALONE_CLIENT_REQUIRED);
            if ("true".equals(sslClientRequired)) {
                csiv2Props.put(ORBLocator.ORB_SSL_CLIENT_REQUIRED, "true");
            }
        }

        if (!processType.isServer()) {
            // No access to domain.xml.  Just init properties.
            // In this case iiopListener beans will be null.
            checkORBInitialPort(EMPTY_PROPERTIES);
            return;
        }
        iiopService = iiopUtils.getIiopService();
        iiopListeners = iiopService.getIiopListener() ;
        assert iiopListeners != null;

        // checkORBInitialPort looks at iiopListenerBeans, if present
        checkORBInitialPort(EMPTY_PROPERTIES);

        orbBean = iiopService.getOrb();
        assert orbBean != null;

        // Initialize IOR security config for non-EJB CORBA objects
        //iiopServiceBean.isClientAuthenticationRequired()));
        csiv2Props.put(ORBLocator.ORB_CLIENT_AUTH_REQUIRED,
            String.valueOf(iiopService.getClientAuthenticationRequired()));

        // If there is at least one non-SSL listener, then it means
        // SSL is not required for CORBA objects.
        boolean corbaSSLRequired = true;
        for (IiopListener bean : iiopListeners) {
            if (bean.getSsl() == null) {
                corbaSSLRequired = false;
                break;
            }
        }

        csiv2Props.put(ORBLocator.ORB_SSL_SERVER_REQUIRED, String.valueOf(corbaSSLRequired));
    }

    /**
     * Set ORB-related system properties that are required in case
     * user code in the app server or app client container creates a
     * new ORB instance.  The default result of calling
     * ORB.init( String[], Properties ) must be a fully usuable, consistent
     * ORB.  This avoids difficulties with having the ORB class set
     * to a different ORB than the RMI-IIOP delegates.
     */
    private void setORBSystemProperties() {
        setProperty(OMG_ORB_CLASS_PROPERTY, ORB_CLASS, false);
        setProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY, ORB_SINGLETON_CLASS, false);
        setProperty(ORB_UTIL_CLASS_PROPERTY, RMI_UTIL_CLASS, true);
        setProperty(RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY, RMI_STUB_CLASS, true);
        setProperty(RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY, RMI_PRO_CLASS, true);
    }

    /**
     * Set the ORB properties for IIOP failover and load balancing.
     */
    private void setFOLBProperties(Properties orbInitProperties) {

        orbInitProperties.put(ORBConstants.RFM_PROPERTY, "dummy");
        orbInitProperties.put(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY, IIOP_SSL_SOCKET_FACTORY_CLASS);

        // ClientGroupManager.
        // Registers itself as
        //   ORBInitializer (that registers ClientRequestInterceptor)
        //   IIOPPrimaryToContactInfo
        //   IORToSocketInfo
        orbInitProperties.setProperty(
            ORBConstants.USER_CONFIGURATOR_PREFIX + "com.sun.corba.ee.impl.folb.ClientGroupManager", "dummy");

        // This configurator registers the CSIv2SSLTaggedComponentHandler
        orbInitProperties.setProperty(
            ORBConstants.USER_CONFIGURATOR_PREFIX + CSIv2SSLTaggedComponentHandlerImpl.class.getName(), "dummy");

        if (processType.isServer()) {
            gmsClient = new IiopFolbGmsClient(services);

            if (gmsClient.isGMSAvailable()) {
                LOG.fine("GMS available and enabled - doing EE initialization");

                // Register ServerGroupManager.
                // Causes it to register itself as an ORBInitializer
                // that then registers it as
                // IOR and ServerRequest Interceptors.
                orbInitProperties.setProperty(
                    ORBConstants.USER_CONFIGURATOR_PREFIX + "com.sun.corba.ee.impl.folb.ServerGroupManager", "dummy");
                LOG.finest("Did EE property initialization");
            }
        }
    }

    private void initORB(Properties props) {
        try {
            LOG.log(Level.CONFIG, "initORB({0})", props);
            setORBSystemProperties();

            Properties orbInitProperties = new Properties();
            orbInitProperties.putAll(props);

            orbInitProperties.put(ORBConstants.APPSERVER_MODE, "true");

            // The main configurator.
            orbInitProperties.put(ORBConstants.USER_CONFIGURATOR_PREFIX + PEORB_CONFIG_CLASS, "dummy");

            setFOLBProperties(orbInitProperties);

            // Standard OMG Properties.
            String orbDefaultServerId = DEFAULT_SERVER_ID;
            if (!processType.isServer()) {
                orbDefaultServerId = ACC_DEFAULT_SERVER_ID;
            }

            orbDefaultServerId = System.getProperty(USER_DEFINED_ORB_SERVER_ID_PROPERTY, orbDefaultServerId);
            orbInitProperties.put(ORBConstants.ORB_SERVER_ID_PROPERTY, orbDefaultServerId);
            orbInitProperties.put(OMG_ORB_CLASS_PROPERTY, ORB_CLASS);
            orbInitProperties.put(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX + GLASSFISH_INITIALIZER, "");
            orbInitProperties.put(ORBConstants.ALLOW_LOCAL_OPTIMIZATION, "true");
            orbInitProperties.put(ORBConstants.GET_SERVICE_CONTEXT_RETURNS_NULL, "true");
            orbInitProperties.put(ORBConstants.ORB_ID_PROPERTY, S1AS_ORB_ID);
            orbInitProperties.put(ORBConstants.SHOW_INFO_MESSAGES, "true");

            // Do this even if propertiesInitialized, since props may override
            // ORBInitialHost and port.
            String initialPort = checkORBInitialPort(orbInitProperties);

            String orbInitialHost = checkORBInitialHost(orbInitProperties);
            String endpointsProperty = System.getProperty(IIOP_ENDPOINTS_PROPERTY);
            String[] orbInitRefArgs;
            if (endpointsProperty != null && !endpointsProperty.isEmpty()) {
                orbInitRefArgs = getORBInitRef(endpointsProperty);
            } else {
                // Add -ORBInitRef for INS to work
                orbInitRefArgs = getORBInitRef(orbInitialHost, initialPort);
            }

            // In a server, don't configure any default acceptors so that lazy init
            // can be used.  Actual lazy init setup takes place in PEORBConfigurator
            if (processType.isServer()) {
                validateIiopListeners();
                orbInitProperties.put(ORBConstants.NO_DEFAULT_ACCEPTORS, "true");
                // 14734893 - IIOP ports don't bind to the network address set for the cluster instance
                // GLASSFISH-17469   IIOP Listener Network Address Setting Ignored
                checkORBServerHost(orbInitProperties);
            }

            checkConnectionSettings(orbInitProperties);
            checkMessageFragmentSize(orbInitProperties);
            checkServerSSLOutboundSettings(orbInitProperties);
            checkForOrbPropertyValues(orbInitProperties);

            Collection<GlassFishORBLifeCycleListener> lcListeners = iiopUtils.getGlassFishORBLifeCycleListeners();

            List<String> argsList = new ArrayList<>();
            argsList.addAll(Arrays.asList(orbInitRefArgs));

            for (GlassFishORBLifeCycleListener listener : lcListeners) {
                listener.initializeORBInitProperties(argsList, orbInitProperties);
            }

            String[] args = argsList.toArray(new String[argsList.size()]);

            // The following is done only on the Server Side to set the
            // ThreadPoolManager in the ORB. ThreadPoolManager on the server
            // is initialized based on configuration parameters found in
            // domain.xml. On the client side this is not done

            if (processType.isServer()) {
                PEORBConfigurator.setThreadPoolManager();
            }

            // orb MUST be set before calling getFVDCodeBaseIOR, or we can
            // recurse back into initORB due to interceptors that run
            // when the TOA supporting the FVD is created!
            // DO NOT MODIFY initORB to return ORB!!!

            /**
             * we can't create object adapters inside the ORB init path,
             * or else we'll get this same problem in slightly different ways.
             * (address in use exception) Having an IORInterceptor
             * (TxSecIORInterceptor) get called during ORB init always
             * results in a nested ORB.init call because of the call to getORB
             * in the IORInterceptor.i
             */

            // TODO Right now we need to explicitly set useOSGI flag.  If it's set to
            // OSGI mode and we're not in OSGI mode, orb initialization fails.
            boolean useOSGI = false;

            final ClassLoader prevCL = Utility.getClassLoader();
            try {
                Utility.setContextClassLoader(GlassFishORBManager.class.getClassLoader());

                if (processType.isServer()) {
                    // start glassfish-corba-orb bundle
                    ModulesRegistry modulesRegistry = services.getService(ModulesRegistry.class);
                    HK2Module corbaOrbModule = null;
                    for (HK2Module m : modulesRegistry.getModules()) {
                        if (m.getName().equals("glassfish-corba-orb")) {
                            corbaOrbModule = m;
                            break;
                        }
                    }

                    if (corbaOrbModule != null) {
                        useOSGI = true;
                        corbaOrbModule.start();
                    }
                }
            } finally {
                Utility.setContextClassLoader(prevCL);
            }

            // Can't run with GlassFishORBManager.class.getClassLoader() as the context ClassLoader

            // For ORB compatibility with JDK11+ JDKs see https://github.com/eclipse-ee4j/orb-gmbal/issues/22
            setProperty("org.glassfish.gmbal.no.multipleUpperBoundsException", "true", true);
            orb = ORBFactory.create();
            ORBFactory.initialize(orb, args, orbInitProperties, useOSGI);

            // Done to indicate this is a server and needs to create listen ports.
            try {
                orb.resolve_initial_references("RootPOA");
            } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
                LOG.log(Level.SEVERE, IIOPImplLogFacade.INVALID_ROOT_POA_NAME, e);
            }

            if (processType.isServer()) {
                // This MUST happen before new InitialGroupInfoService,
                // or the ServerGroupManager will get initialized before the
                // GIS is available.
                gmsClient.setORB(orb) ;

                // J2EEServer's persistent server port is same as ORBInitialPort.
                orbInitialPort = getORBInitialPort();

                for (GlassFishORBLifeCycleListener listener : lcListeners) {
                    listener.orbCreated(orb);
                }

                // TODO: The following statement can be moved to
                // some GlassFishORBLifeCycleListeners

                rfm = (ReferenceFactoryManager) orb.resolve_initial_references(ORBConstants.REFERENCE_FACTORY_MANAGER);
                new InitialGroupInfoService(orb);
                iiopUtils.setORB(orb);
            }

            // SeeBeyond fix for 6325988: needs testing.
            // Still do not know why this might make any difference.
            // Invoke this for its side-effects: ignore returned IOR.
            orb.getFVDCodeBaseIOR();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, IIOPImplLogFacade.ORB_INITIALIZATION_FAILED, ex);
            throw new IllegalStateException("ORB initialization failed.", ex);
        }
    }

    private String checkForAddrAny(Properties props, String orbInitialHost) {
        if (orbInitialHost.equals("0.0.0.0") || orbInitialHost.equals("::")
            || orbInitialHost.equals("::ffff:0.0.0.0")) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException uhe) {
                LOG.log(Level.WARNING, "Unknown host exception - Setting host to localhost");
                return DEFAULT_ORB_INIT_HOST;
            }
        }
        return orbInitialHost;
    }

    // Returns the first IiopListenerBean which represents a clear text endpoint
    // Note: it is questionable whether the system actually support multiple
    // endpoints of the same type, or no clear text endpoint at all in the
    // configuration.
    private IiopListener getClearTextIiopListener() {
        if (iiopListeners != null)  {
            for (IiopListener il : iiopListeners) {
                if (il.getSsl() == null) {
                    return il ;
                }
            }
        }

        return null ;
    }

    private String checkORBInitialHost(Properties props) {
        // Host setting in system properties always takes precedence.
        String initialHost = System.getProperty(ORBConstants.INITIAL_HOST_PROPERTY);
        if (initialHost == null) {
            initialHost = props.getProperty(ORBConstants.INITIAL_HOST_PROPERTY);
        }

        if (initialHost == null) {
            IiopListener il = getClearTextIiopListener();
            if (il != null) {
                initialHost = il.getAddress();
            }
        }

        if (initialHost == null) {
            initialHost = DEFAULT_ORB_INIT_HOST;
        }

        initialHost = checkForAddrAny(props, initialHost);
        props.setProperty(ORBConstants.INITIAL_HOST_PROPERTY, initialHost);
        LOG.log(Level.CONFIG, "ORB initial host set to {0}", initialHost);
        return initialHost;
    }


    private String checkORBInitialPort(Properties props) {
        // Port setting in system properties always takes precedence.
        String initialPort = System.getProperty(
            ORBConstants.INITIAL_PORT_PROPERTY );

        if (initialPort == null) {
            initialPort = props.getProperty(ORBConstants.INITIAL_PORT_PROPERTY);
        }

        if (initialPort == null) {
            IiopListener listener = getClearTextIiopListener();
            if (listener != null) {
                initialPort = listener.getPort();
            }
        }

        if (initialPort == null) {
            initialPort = DEFAULT_ORB_INIT_PORT;
        }

        // Make sure we set initial port in System properties so that
        // any instantiations of org.glassfish.jndi.cosnaming.CNCtxFactory
        // use same port.
        props.setProperty(ORBConstants.INITIAL_PORT_PROPERTY, initialPort);


        // Done to initialize the Persistent Server Port, before any
        // POAs are created. This was earlier done in POAEJBORB
        // Do it only in the appserver, not on appclient.
        if (processType.isServer()) {
            props.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, initialPort);
        }

        LOG.log(Level.CONFIG, "ORB initial port set to {0}", initialPort);
        orbInitialPort = Integer.parseInt(initialPort);
        return initialPort;
    }

    // Server host property is used only for ORB running in server mode
    // Return host name (or ip address string) if the SERVER_HOST PROPERTY is set or
    // network-address attribute is specified in iiop-listener element
    // Return null otherwise.
    private String checkORBServerHost(Properties props) {
        // Host setting in system properties always takes precedence.
        String serverHost = System.getProperty(ORBConstants.SERVER_HOST_PROPERTY);

        if (serverHost == null) {
            serverHost = props.getProperty(ORBConstants.SERVER_HOST_PROPERTY );
        }

        if (serverHost == null) {
            IiopListener listener = getClearTextIiopListener() ;
            if (listener != null) {
                // For this case, use same value as ORBInitialHost,
                serverHost = listener.getAddress();
            }
        }

        if (serverHost != null) {
            // set the property, to be used during ORB initialization
            // Bug 14734893 - IIOP ports don't bind to the network address set for the cluster instance
            props.setProperty(ORBConstants.SERVER_HOST_PROPERTY, serverHost);
            LOG.log(Level.CONFIG, "ORB server host set to {0}", serverHost);
        }

        return serverHost;
    }

    private void validateIiopListeners() {
        if (iiopListeners == null) {
            return;
        }
        var lazyListeners = iiopListeners.stream()
                .filter(ilb -> Boolean.valueOf(ilb.getLazyInit())).collect(Collectors.toList());

        if (lazyListeners.size() > 1) {
            throw new IllegalStateException(
                    "Only one iiop-listener can be configured with lazy-init=true. "
                            + lazyListeners.stream().map(ilb -> ilb.getId()).collect(Collectors.toList()));
        }

        var lazySslListeners = lazyListeners.stream()
                .filter(ilb -> Boolean.valueOf(ilb.getSecurityEnabled()) && ilb.getSsl() != null).collect(Collectors.toList());

        if (lazySslListeners.size() > 0) {
            throw new IllegalStateException(
                    "Lazy-init not supported for SSL iiop-listeners. "
                            + lazySslListeners.stream().map(ilb -> ilb.getId()).collect(Collectors.toList()));
        }
    }

    private void checkConnectionSettings(Properties props) {
        if (orbBean != null) {
            String maxConnections = orbBean.getMaxConnections();
            try {
                Integer.parseInt(maxConnections);
            } catch (NumberFormatException nfe) {
                LOG.log(Level.WARNING, IIOPImplLogFacade.INVALID_MAX_CONNECTIONS,
                    new Object[] {maxConnections, DEFAULT_MAX_CONNECTIONS});
                maxConnections = DEFAULT_MAX_CONNECTIONS;
            }
            props.setProperty(ORBConstants.HIGH_WATER_MARK_PROPERTY, maxConnections);
        }
    }

    private void checkMessageFragmentSize(Properties props) {
        if (orbBean != null) {
            String fragmentSize = null;
            String bufferSize = null;
            try {
                int fsize = ((Integer.parseInt(orbBean.getMessageFragmentSize().trim())) / 8) * 8;
                if (fsize < 32) {
                    fragmentSize = "32";
                    LOG.log(Level.INFO, "Setting ORB Message Fragment size to {0}", fragmentSize);
                } else {
                    fragmentSize = String.valueOf(fsize);
                }
                bufferSize = fragmentSize;
            } catch (NumberFormatException nfe) {
                LOG.log(Level.WARNING, IIOPImplLogFacade.INVALID_MESSAGE_FRAGMENT_SIZE,
                    new Object[] {fragmentSize, SUN_GIOP_DEFAULT_FRAGMENT_SIZE});
                fragmentSize = SUN_GIOP_DEFAULT_FRAGMENT_SIZE;
                bufferSize = SUN_GIOP_DEFAULT_BUFFER_SIZE;
            }
            props.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE, fragmentSize);
            props.setProperty(ORBConstants.GIOP_BUFFER_SIZE, bufferSize);
        }
    }


    private void checkServerSSLOutboundSettings(Properties props) {
        if (iiopService != null) {
            SslClientConfig sslClientConfigBean = iiopService.getSslClientConfig();
            if (sslClientConfigBean != null) {
                Ssl ssl = sslClientConfigBean.getSsl();
                assert (ssl != null);
            }
        }
    }

    private void checkForOrbPropertyValues(Properties props) {
        if (orbBean != null) {
            List<Property> orbBeanProps = orbBean.getProperty();
            if (orbBeanProps != null) {
                for (Property orbBeanProp : orbBeanProps) {
                    props.setProperty(orbBeanProp.getName(),
                    orbBeanProp.getValue());
                }
            }
        }
    }

    private String[] getORBInitRef(String orbInitialHost,
                                          String initialPort) {
        // Add -ORBInitRef NameService=....
        // This ensures that INS will be used to talk with the NameService.
        String[] newArgs = new String[] {"-ORBInitRef",
            "NameService=corbaloc:" + IIOP_URL + orbInitialHost + ":" + initialPort + "/NameService"};

        return newArgs;
    }

    private String[] getORBInitRef(String endpoints) {

        String[] list = endpoints.split(",");
        String corbalocURL = getCorbalocURL(list);
        LOG.log(Level.FINE, "GlassFishORBManager.getORBInitRef = {0}", corbalocURL);

        // Add -ORBInitRef NameService=....
        // This ensures that INS will be used to talk with the NameService.
        String[] newArgs = new String[] {"-ORBInitRef", "NameService=corbaloc:" + corbalocURL + "/NameService"};

        return newArgs;
    }

    private String getCorbalocURL(Object[] list) {

        String corbalocURL = "";
        //convert list into corbaloc url
        for (Object element : list) {
            LOG.log(Level.FINE, "list[i] ==> {0}", element);
            if (corbalocURL.isEmpty()) {
                corbalocURL = IIOP_URL + ((String) element).trim();
            } else {
                corbalocURL = corbalocURL + "," + IIOP_URL + ((String) element).trim();
            }
        }
        LOG.log(Level.INFO, "corbaloc url ==> {0}", corbalocURL);
        return corbalocURL;
    }

    String getIIOPEndpoints() {
        return gmsClient.getIIOPEndpoints() ;
    }
}
