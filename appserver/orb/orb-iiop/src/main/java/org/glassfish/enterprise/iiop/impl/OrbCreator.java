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

package org.glassfish.enterprise.iiop.impl;

import com.sun.corba.ee.impl.folb.ClientGroupManager;
import com.sun.corba.ee.impl.folb.ServerGroupManager;
import com.sun.corba.ee.impl.orb.ORBImpl;
import com.sun.corba.ee.impl.orb.ORBSingleton;
import com.sun.corba.ee.spi.folb.GroupInfoService;
import com.sun.corba.ee.spi.misc.ORBConstants;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.enterprise.module.HK2Module;

import java.lang.System.Logger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.glassfish.api.admin.ProcessEnvironment.ProcessType;
import org.glassfish.orb.admin.config.IiopListener;
import org.glassfish.orb.admin.config.Orb;
import org.jvnet.hk2.config.types.Property;

import static com.sun.corba.ee.spi.misc.ORBConstants.USER_CONFIGURATOR_PREFIX;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * Creates the {@link ORB} singleton object.
 */
final class OrbCreator {
    private static final Logger LOG = System.getLogger(OrbCreator.class.getName());

    // Various pluggable classes defined in the app server that are used
    // by the ORB.
    private static final String ORB_CLASS = ORBImpl.class.getName();
    private static final String ORB_SINGLETON_CLASS = ORBSingleton.class.getName();

    private static final String PEORB_CONFIG_CLASS = PEORBConfigurator.class.getName();
    private static final String IIOP_SSL_SOCKET_FACTORY_CLASS = IIOPSSLSocketFactory.class.getName();
    private static final String RMI_STUB_CLASS = com.sun.corba.ee.impl.javax.rmi.CORBA.StubDelegateImpl.class.getName();
    private static final String RMI_PRO_CLASS = com.sun.corba.ee.impl.javax.rmi.PortableRemoteObject.class.getName();

    // RMI-IIOP delegate constants
    private static final String ORB_UTIL_CLASS_PROPERTY = "javax.rmi.CORBA.UtilClass";
    private static final String RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY = "javax.rmi.CORBA.StubClass";
    private static final String RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY = "javax.rmi.CORBA.PortableRemoteObjectClass";

    // ORB constants: OMG standard
    private static final String OMG_ORB_CLASS_PROPERTY = "org.omg.CORBA.ORBClass";
    private static final String OMG_ORB_SINGLETON_CLASS_PROPERTY = "org.omg.CORBA.ORBSingletonClass";
    private static final String ORB_UTIL_CLASS = com.sun.corba.ee.impl.javax.rmi.CORBA.Util.class.getName();

    // ORB configuration constants
    private static final String DEFAULT_SERVER_ID = "100";
    private static final String ACC_DEFAULT_SERVER_ID = "101";
    private static final String USER_DEFINED_ORB_SERVER_ID_PROPERTY = "org.glassfish.orb.iiop.orbserverid";

    private static final String DEFAULT_MAX_CONNECTIONS = "1024";
    private static final String GLASSFISH_INITIALIZER = GlassFishORBInitializer.class.getName();

    private static final String SUN_GIOP_DEFAULT_FRAGMENT_SIZE = "1024";
    private static final String SUN_GIOP_DEFAULT_BUFFER_SIZE = "1024";

    private static final String DEFAULT_ORB_INIT_HOST = "localhost";

    // This will only apply for stand-alone java clients, since
    // in the server the orb port comes from domain.xml, and in an appclient
    // the port is set from the sun-acc.xml. It's set to the same
    // value as the default orb port in domain.xml as a convenience.
    // That way the code only needs to do a "new InitialContext()"
    // without setting any jvm properties and the naming service will be
    // found. Of course, if the port was changed in domain.xml for some
    // reason the code will still have to set org.omg.CORBA.ORBInitialPort.
    private static final int DEFAULT_ORB_INIT_PORT = 3700;

    // We need this to get the ORB monitoring set up correctly
    private static final String S1AS_ORB_ID = "S1AS-ORB";
    private static final String IIOP_ENDPOINTS_PROPERTY = "com.sun.appserv.iiop.endpoints";
    private static final String IIOP_URL = "iiop:1.2@";

    private final Orb orbConfig;
    private final ProcessType processType;
    private final List<IiopListener> iiopListeners;
    private final GroupInfoService clusterGroupInfo;
    private final HK2Module corbaOrbOsgiModule;

    /**
     * Set ORB-related system properties that are required in case
     * user code in the app server or app client container creates a
     * new ORB instance.  The default result of calling
     * ORB.init( String[], Properties ) must be a fully usuable, consistent
     * ORB.  This avoids difficulties with having the ORB class set
     * to a different ORB than the RMI-IIOP delegates.
     * @param orbConfig
     *
     * @param processType
     * @param clusterGroupInfo
     * @param iiopListeners
     * @param corbaOrbOsgiModule
     */
    OrbCreator(Orb orbConfig, ProcessType processType, GroupInfoService clusterGroupInfo,
        List<IiopListener> iiopListeners, HK2Module corbaOrbOsgiModule) {
        this.orbConfig = orbConfig;
        this.processType = processType;
        this.iiopListeners = iiopListeners;
        this.clusterGroupInfo = clusterGroupInfo;
        this.corbaOrbOsgiModule = corbaOrbOsgiModule;
        setProperty(OMG_ORB_CLASS_PROPERTY, ORB_CLASS, false);
        setProperty(OMG_ORB_SINGLETON_CLASS_PROPERTY, ORB_SINGLETON_CLASS, false);
        setProperty(ORB_UTIL_CLASS_PROPERTY, ORB_UTIL_CLASS, true);
        setProperty(RMIIIOP_STUB_DELEGATE_CLASS_PROPERTY, RMI_STUB_CLASS, true);
        setProperty(RMIIIOP_PRO_DELEGATE_CLASS_PROPERTY, RMI_PRO_CLASS, true);
        // For ORB compatibility with JDK11+ JDKs
        // See https://github.com/eclipse-ee4j/orb-gmbal/issues/22
        // See TypeEvaluator class in the gmbal.jar
        setProperty("org.glassfish.gmbal.no.multipleUpperBoundsException", "true", true);
    }


    /**
     * Create the shared ORB instance for the server.
     * The ORB is created with the standard server properties, which can be overridden by Properties
     * passed in the props argument.
     *
     * @param processType
     */
    ORB createOrb(Properties props) {
        LOG.log(INFO, "createOrb({0})", props);
        try {
            Properties orbInitProperties = setFOLBProperties(props);

            // Standard OMG Properties.
            String orbDefaultServerId = System.getProperty(USER_DEFINED_ORB_SERVER_ID_PROPERTY,
                processType.isServer() ? DEFAULT_SERVER_ID : ACC_DEFAULT_SERVER_ID);
            orbInitProperties.put(ORBConstants.ORB_SERVER_ID_PROPERTY, orbDefaultServerId);
            orbInitProperties.put(OMG_ORB_CLASS_PROPERTY, ORB_CLASS);
            orbInitProperties.put(ORBConstants.PI_ORB_INITIALIZER_CLASS_PREFIX + GLASSFISH_INITIALIZER, "");
            orbInitProperties.put(ORBConstants.ALLOW_LOCAL_OPTIMIZATION, "true");
            orbInitProperties.put(ORBConstants.GET_SERVICE_CONTEXT_RETURNS_NULL, "true");
            orbInitProperties.put(ORBConstants.ORB_ID_PROPERTY, S1AS_ORB_ID);
            orbInitProperties.put(ORBConstants.SHOW_INFO_MESSAGES, "true");

            // Do this even if propertiesInitialized, since props may override
            // ORBInitialHost and port.
            final String initialPort = Integer.toString(evaluateInitialPort(orbInitProperties, iiopListeners));
            // Make sure we set initial port in System properties so that
            // any instantiations of org.glassfish.jndi.cosnaming.CNCtxFactory
            // use same port.
            orbInitProperties.setProperty(ORBConstants.INITIAL_PORT_PROPERTY, initialPort);

            // Done to initialize the Persistent Server Port, before any
            // POAs are created. This was earlier done in POAEJBORB
            // Do it only in the appserver, not on appclient.
            if (processType.isServer()) {
                orbInitProperties.setProperty(ORBConstants.PERSISTENT_SERVER_PORT_PROPERTY, initialPort);
            }

            final String initialHost = evaluateInitialHost(orbInitProperties, iiopListeners);
            orbInitProperties.setProperty(ORBConstants.INITIAL_HOST_PROPERTY, initialHost);
            LOG.log(DEBUG, "ORB initial host set to {0}", initialHost);

            // In a server, don't configure any default acceptors so that lazy init
            // can be used.  Actual lazy init setup takes place in PEORBConfigurator
            if (processType.isServer()) {
                validateIiopListeners();
                orbInitProperties.put(ORBConstants.NO_DEFAULT_ACCEPTORS, "true");
                // 14734893 - IIOP ports don't bind to the network address set for the cluster instance
                // GLASSFISH-17469   IIOP Listener Network Address Setting Ignored
                String serverHost = evaluateHostname(orbInitProperties, iiopListeners);
                if (serverHost != null) {
                    // set the property, to be used during ORB initialization
                    // Bug 14734893 - IIOP ports don't bind to the network address set for the cluster instance
                    orbInitProperties.setProperty(ORBConstants.SERVER_HOST_PROPERTY, serverHost);
                    LOG.log(DEBUG, "ORB server host set to {0}", serverHost);
                }
            }

            checkConnectionSettings(orbInitProperties);
            checkMessageFragmentSize(orbInitProperties);
            checkForOrbPropertyValues(orbInitProperties);

            String[] args = toOrbArgs(initialPort, initialHost);
            return new GlassFishOrbImpl(corbaOrbOsgiModule, clusterGroupInfo, orbInitProperties, args);
        } catch (Exception ex) {
            throw new IllegalStateException("ORB initialization failed.", ex);
        }
    }

    private String[] toOrbArgs(final String initialPort, final String initialHost) {
        String endpointsProperty = System.getProperty(IIOP_ENDPOINTS_PROPERTY);
        final String[] orbInitRefArgs;
        if (endpointsProperty == null || endpointsProperty.isEmpty()) {
            // Add -ORBInitRef for INS to work
            orbInitRefArgs = getORBInitRef(initialHost, initialPort);
        } else {
            orbInitRefArgs = getORBInitRef(endpointsProperty);
        }
        List<String> argsList = new ArrayList<>();
        argsList.addAll(Arrays.asList(orbInitRefArgs));
        String[] args = argsList.toArray(String[]::new);
        return args;
    }

    /**
     * Set the ORB properties for IIOP failover and load balancing.
     * @param props
     */
    private Properties setFOLBProperties(Properties props) {

        Properties orbInitProperties = new Properties();
        orbInitProperties.putAll(props);
        orbInitProperties.put(ORBConstants.APPSERVER_MODE, "true");

        // The main configurator.
        orbInitProperties.put(USER_CONFIGURATOR_PREFIX + PEORB_CONFIG_CLASS, "dummy");


        orbInitProperties.put(ORBConstants.RFM_PROPERTY, "dummy");
        orbInitProperties.put(ORBConstants.SOCKET_FACTORY_CLASS_PROPERTY, IIOP_SSL_SOCKET_FACTORY_CLASS);

        // ClientGroupManager.
        // Registers itself as
        //   ORBInitializer (that registers ClientRequestInterceptor)
        //   IIOPPrimaryToContactInfo
        //   IORToSocketInfo
        orbInitProperties.setProperty(USER_CONFIGURATOR_PREFIX + ClientGroupManager.class.getName(), "dummy");

        // This configurator registers the CSIv2SSLTaggedComponentHandler
        orbInitProperties.setProperty(USER_CONFIGURATOR_PREFIX + CSIv2SSLTaggedComponentHandlerImpl.class.getName(), "dummy");

        if (this.clusterGroupInfo != null) {
            LOG.log(DEBUG, "GMS available and enabled - doing EE initialization");

            // Register ServerGroupManager.
            // Causes it to register itself as an ORBInitializer
            // that then registers it as
            // IOR and ServerRequest Interceptors.
            orbInitProperties.setProperty(USER_CONFIGURATOR_PREFIX + ServerGroupManager.class.getName(), "dummy");
            LOG.log(TRACE, "Did EE property initialization");
        }
        return orbInitProperties;
    }

    private void checkConnectionSettings(Properties props) {
        if (orbConfig != null) {
            String maxConnections = orbConfig.getMaxConnections();
            try {
                Integer.parseInt(maxConnections);
            } catch (NumberFormatException nfe) {
                LOG.log(WARNING, "The max connections value {0} must be an integer, using default value {1} instead.",
                    maxConnections, DEFAULT_MAX_CONNECTIONS);
                maxConnections = DEFAULT_MAX_CONNECTIONS;
            }
            props.setProperty(ORBConstants.HIGH_WATER_MARK_PROPERTY, maxConnections);
        }
    }

    private void checkMessageFragmentSize(Properties props) {
        if (orbConfig == null) {
            return;
        }
        String fragmentSize = null;
        String bufferSize = null;
        try {
            int fsize = ((Integer.parseInt(orbConfig.getMessageFragmentSize().trim())) / 8) * 8;
            if (fsize < 32) {
                fragmentSize = "32";
                LOG.log(INFO, "Setting ORB Message Fragment size to {0}", fragmentSize);
            } else {
                fragmentSize = String.valueOf(fsize);
            }
            bufferSize = fragmentSize;
        } catch (NumberFormatException nfe) {
            LOG.log(WARNING, "The message fragment size {0} must be an integer, using default value {1} instead.",
                fragmentSize, SUN_GIOP_DEFAULT_FRAGMENT_SIZE);
            fragmentSize = SUN_GIOP_DEFAULT_FRAGMENT_SIZE;
            bufferSize = SUN_GIOP_DEFAULT_BUFFER_SIZE;
        }
        props.setProperty(ORBConstants.GIOP_FRAGMENT_SIZE, fragmentSize);
        props.setProperty(ORBConstants.GIOP_BUFFER_SIZE, bufferSize);
    }

    private void checkForOrbPropertyValues(Properties props) {
        if (orbConfig != null) {
            List<Property> orbConfigProps = orbConfig.getProperty();
            if (orbConfigProps != null) {
                for (Property orbConfigProp : orbConfigProps) {
                    props.setProperty(orbConfigProp.getName(),
                    orbConfigProp.getValue());
                }
            }
        }
    }

    private void validateIiopListeners() {
        if (iiopListeners == null) {
            return;
        }
        var lazyListeners = iiopListeners.stream().filter(ilb -> Boolean.valueOf(ilb.getLazyInit()))
            .collect(Collectors.toList());

        if (lazyListeners.size() > 1) {
            throw new IllegalStateException("Only one iiop-listener can be configured with lazy-init=true. "
                + lazyListeners.stream().map(ilb -> ilb.getId()).collect(Collectors.toList()));
        }
        if (lazyListeners.isEmpty()) {
            return;
        }
        final IiopListener listener = lazyListeners.get(0);
        if ("true".equalsIgnoreCase(listener.getSecurityEnabled()) && listener.getSsl() != null) {
            throw new IllegalStateException(
                "Lazy-init not supported for SSL iiop-listeners. Listener id: " + listener.getId());
        }
    }


    static String evaluateInitialHost(Properties props, List<IiopListener> listeners) {
        // Host setting in system properties always takes precedence.
        String initialHost = System.getProperty(ORBConstants.INITIAL_HOST_PROPERTY);
        if (initialHost == null) {
            initialHost = props.getProperty(ORBConstants.INITIAL_HOST_PROPERTY);
        }
        if (initialHost == null) {
            IiopListener listener = getClearTextIiopListener(listeners);
            if (listener != null) {
                initialHost = listener.getAddress();
            }
        }
        return initialHost == null ? DEFAULT_ORB_INIT_HOST : replaceAnyWithLocalHost(initialHost);
    }

    private static String replaceAnyWithLocalHost(String orbInitialHost) {
        if (orbInitialHost.equals("0.0.0.0") || orbInitialHost.equals("::")
            || orbInitialHost.equals("::ffff:0.0.0.0")) {
            try {
                return InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException uhe) {
                LOG.log(WARNING, "Unknown host exception - Setting host to localhost", uhe);
                return DEFAULT_ORB_INIT_HOST;
            }
        }
        return orbInitialHost;
    }


    // Server host property is used only for ORB running in server mode
    // Return host name (or ip address string) if the SERVER_HOST PROPERTY is set or
    // network-address attribute is specified in iiop-listener element
    // Return null otherwise.
    private static String evaluateHostname(Properties props, List<IiopListener> listeners) {
        // Host setting in system properties always takes precedence.
        String serverHost = System.getProperty(ORBConstants.SERVER_HOST_PROPERTY);
        if (serverHost == null) {
            serverHost = props.getProperty(ORBConstants.SERVER_HOST_PROPERTY );
        }
        if (serverHost == null) {
            IiopListener listener = getClearTextIiopListener(listeners);
            if (listener != null) {
                // For this case, use same value as ORBInitialHost,
                serverHost = listener.getAddress();
            }
        }
        return serverHost;
    }


    static int evaluateInitialPort(Properties props, List<IiopListener> listeners) {
        String initialPort = System.getProperty(ORBConstants.INITIAL_PORT_PROPERTY);
        if (initialPort == null && props != null) {
            initialPort = props.getProperty(ORBConstants.INITIAL_PORT_PROPERTY);
        }
        if (initialPort == null) {
            IiopListener listener = getClearTextIiopListener(listeners);
            if (listener != null) {
                initialPort = listener.getPort();
            }
        }
        return initialPort == null ? DEFAULT_ORB_INIT_PORT : Integer.valueOf(initialPort);
    }

    // Returns the first IiopListenerBean which represents a clear text endpoint
    // Note: it is questionable whether the system actually support multiple
    // endpoints of the same type, or no clear text endpoint at all in the
    // configuration.
    private static IiopListener getClearTextIiopListener(List<IiopListener> listeners) {
        for (IiopListener il : listeners) {
            if (il.getSsl() == null) {
                return il ;
            }
        }
        return null ;
    }

    /**
     * Add -ORBInitRef NameService=....
     * This ensures that INS will be used to talk with the NameService.
     */
    private static String[] getORBInitRef(String orbInitialHost, String initialPort) {
        return new String[] {"-ORBInitRef",
            "NameService=corbaloc:" + IIOP_URL + orbInitialHost + ":" + initialPort + "/NameService"};
    }

    /**
     * Add -ORBInitRef NameService=....
     * This ensures that INS will be used to talk with the NameService.
     */
    private static String[] getORBInitRef(String endpoints) {
        String corbalocURL = getCorbaUrlList(endpoints.split(","));
        return new String[] {"-ORBInitRef", "NameService=corbaloc:" + corbalocURL + "/NameService"};
    }

    private static String getCorbaUrlList(Object[] list) {
        String corbalocURL = "";
        for (Object element : list) {
            LOG.log(DEBUG, "list[i] ==> {0}", element);
            if (corbalocURL.isEmpty()) {
                corbalocURL = IIOP_URL + ((String) element).trim();
            } else {
                corbalocURL = corbalocURL + "," + IIOP_URL + ((String) element).trim();
            }
        }
        LOG.log(INFO, "corbaloc url ==> {0}", corbalocURL);
        return corbalocURL;
    }

}
