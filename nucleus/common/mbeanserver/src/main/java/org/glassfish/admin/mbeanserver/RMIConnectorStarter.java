/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.mbeanserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.RMISocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.net.ssl.SSLContext;
import javax.rmi.ssl.SslRMIClientSocketFactory;
import javax.security.auth.Subject;

import org.glassfish.admin.mbeanserver.ssl.JMXMasterPasswordImpl;
import org.glassfish.admin.mbeanserver.ssl.SSLClientConfigurator;
import org.glassfish.admin.mbeanserver.ssl.SSLParams;
import org.glassfish.admin.mbeanserver.ssl.SecureRMIServerSocketFactory;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.logging.annotation.LogMessageInfo;

import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_PASSWORD_DEFAULT;
import static com.sun.enterprise.util.SystemPropertyConstants.KEYSTORE_TYPE_DEFAULT;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_FILE;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_PASSWORD;
import static org.glassfish.embeddable.GlassFishVariable.KEYSTORE_TYPE;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_FILE;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_PASSWORD;
import static org.glassfish.embeddable.GlassFishVariable.TRUSTSTORE_TYPE;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * This class configures and starts the JMX RMI connector server using rmi_jrmp protocol.
 * SSL support for connections to the JMX connector are supported. This is achieved
 * via the standard SslRMIClientSocketFactory and a custom SecureRMIServerCoketFactory which
 * extends the standard SslRMIServerSocketFactory.
 * <p/>
 * The connection to the Registry is secure and is provided using the same
 * SslRMIClientSocketFactory instance and SecureRMIServerSocketFactory  instance
 * which is exported via the RMIServer.
 *
 * @author llc
 * @author prasads@dev.java.net
 */
final class RMIConnectorStarter extends ConnectorStarter {

    public static final String RMI_HOSTNAME_PROP = "java.rmi.server.hostname";
    private final Registry mRegistry;
    private final boolean mBindToSingleIP;
    private volatile MyRMIJRMPServerImpl mMyServer;
    /**
     * will be null if we don't need it
     */
    private final MyRMIServerSocketFactory mServerSocketFactory;
    private final SecureRMIServerSocketFactory sslServerSocketFactory;
    private final SslRMIClientSocketFactory sslCsf;
    private String masterPassword = null;

    private final static Logger JMX_LOGGER = Util.JMX_LOGGER;

    @LogMessageInfo(level = "INFO", message = "Security enabled")
    private static final String SECURITY_ENABLED = Util.LOG_PREFIX + "00009";

    @LogMessageInfo(level = "INFO", message = "Binding RMI port to single IP address = {0}, port {1}")
    private static final String BINDING_TO_SINGLE_ADDR = Util.LOG_PREFIX + "00026";

    @LogMessageInfo(level = "SEVERE", message = "Error stopping RMIConnector", action = "unknown", cause = "unknown")
    private static final String ERROR_STOPPING = Util.LOG_PREFIX + "00011";

    @LogMessageInfo(level = "INFO", message = "MyRMIJRMPServerImpl: exported on address {0}")
    private static final String EXPORTED = Util.LOG_PREFIX + "00012";

    @LogMessageInfo(message = "MyRMIJRMPServerImpl: makeClient on address = {0}")
    private final static String MAKE_CLIENT = Util.LOG_PREFIX + "00013";

    public RMIConnectorStarter(
            final MBeanServer mbeanServer,
            final String address,
            final int port,
            final String protocol,
            final boolean securityEnabled,
            final ServiceLocator habitat,
            final BootAMXListener bootListener,
            final Ssl sslConfig) throws UnknownHostException {

        super(mbeanServer, address, port, securityEnabled, habitat, bootListener);

        masterPassword = new String(habitat.<JMXMasterPasswordImpl>getService(JMXMasterPasswordImpl.class).getMasterPassword());

        if (!"rmi_jrmp".equals(protocol)) {
            throw new IllegalArgumentException("JMXConnectorServer not yet supporting protocol: " + protocol);
        }

        final boolean ENABLED = true;
        mBindToSingleIP = ENABLED && !(address.equals("0.0.0.0") || address.equals(""));

        final InetAddress inetAddr = getAddress(address);

        // if to be bound to a single IP then use custom ServerSocketFactory
        if (mBindToSingleIP) {
            if (isSecurityEnabled()) {
                JMX_LOGGER.info(SECURITY_ENABLED);
                sslServerSocketFactory = new SecureRMIServerSocketFactory(
                        habitat, sslConfig, inetAddr);
                sslCsf = getClientSocketFactory(sslConfig);
                mServerSocketFactory = null;
            } else {
                mServerSocketFactory = new MyRMIServerSocketFactory(inetAddr);
                sslServerSocketFactory = null;
                sslCsf = null;
            }
        } else {
            mServerSocketFactory = null;
            if (isSecurityEnabled()) {
                sslServerSocketFactory = new SecureRMIServerSocketFactory(
                        habitat, sslConfig, getAddress(address));
                sslCsf = getClientSocketFactory(sslConfig);
            } else {
                sslServerSocketFactory = null;
                sslCsf = null;
            }
        }

        mRegistry = startRegistry(address, mPort);
    }

    /**
     * Utility method to get an InetAddress from the address string from the config
     *
     * @param addrSpec
     * @return InetAddress
     * @throws UnknownHostException
     */
    private static InetAddress getAddress(final String addrSpec) throws UnknownHostException {
        String actual = addrSpec;
        if (addrSpec.equals("localhost")) {
            actual = "127.0.0.1";
        }
        return InetAddress.getByName(actual);
    }

    static String setupRMIHostname(final String host) {
        return setProperty(RMI_HOSTNAME_PROP, host, true);
    }

    private static void restoreRMIHostname(final String saved, final String expectedValue) {
        if (saved == null) {
            setProperty(RMI_HOSTNAME_PROP, null, true);
        } else {
            final String temp = setProperty(RMI_HOSTNAME_PROP, saved, true);
            // check that it didn't change since the last setup
            if (!temp.equals(expectedValue)) {
                throw new IllegalStateException("Something changed " + RMI_HOSTNAME_PROP + " to " + temp);
            }
        }
    }

    /**
     * Starts the RMI Registry , where the RMIServer would be exported. If this
     * is a multihomed machine then the Registry is bound to a specific IP address
     * else its bound to a port alone.
     *
     * @param addr the address where the registry is to be available
     * @param port the port at which the registry is started
     * @return
     */
    private Registry startRegistry(final String addr, final int port) {
        Registry registry = null;

        if (mBindToSingleIP) {
            //System.out.println( RMI_HOSTNAME_PROP + " before: " + System.getProperty(RMI_HOSTNAME_PROP) );
            final String saved = setupRMIHostname(addr);
            try {
                JMX_LOGGER.log(Level.INFO, BINDING_TO_SINGLE_ADDR,
                        new Object[]{System.getProperty(RMI_HOSTNAME_PROP), port});
                registry = _startRegistry(port);
            } finally {
                restoreRMIHostname(saved, addr);
            }
        } else {
            Util.getLogger().log(Level.FINE, "Binding RMI port to *:{0}", port);
            registry = _startRegistry(port);
        }
        return registry;
    }

    /**
     * Delegate method to start the registry based on security is enabled or not.
     *
     * @param port
     * @return Registry
     */
    private Registry _startRegistry(final int port) {
        // Ensure cryptographically strong random number generator used
        // to choose the object number - see java.rmi.server.ObjID
        setProperty("java.rmi.server.randomIDs", "true", true);
        try {
            if (isSecurityEnabled()) {
                return LocateRegistry.createRegistry(port, sslCsf, sslServerSocketFactory);
            }
            return LocateRegistry.createRegistry(port, null, mServerSocketFactory);
        } catch (final Exception e) {
            throw new RuntimeException("Port " + port + " is not available for the internal rmi registry. " +
                    "This means that a call was made with the same port, without closing earlier " +
                    "registry instance. This has to do with the system jmx connector configuration " +
                    "in admin-service element of the configuration associated with this instance");
        }
    }

    /**
     * The start method which configures the SSLSockets needed and then starts the
     * JMXConnecterServer.
     *
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    @Override
    public JMXConnectorServer start() throws MalformedURLException, IOException, UnknownHostException {

        final String name = "jmxrmi";
        final String hostname = hostname();
        final Map<String, Object> env = new HashMap<String, Object>();

        env.put("jmx.remote.jndi.rebind", "true");
        env.put(RMIConnectorServer.CREDENTIALS_FILTER_PATTERN, String.class.getName() + ";!*");

        // Provide SSL-based RMI socket factories.
        env.put(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE, sslCsf);
        env.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, sslServerSocketFactory);

        // For binding the JMXConnectorServer with the Registry
        env.put("com.sun.jndi.rmi.factory.socket", sslCsf);

        JMXAuthenticator authenticator = getAccessController();
        if (authenticator != null) {
            env.put("jmx.remote.authenticator", authenticator);
        }
        // env.put("jmx.remote.protocol.provider.pkgs", "com.sun.jmx.remote.protocol");
        //env.put("jmx.remote.protocol.provider.class.loader", this.getClass().getClassLoader());
        final String jmxHostPort = hostname + ":" + mPort;
        final String registryHostPort = hostname + ":" + mPort;

        // !!!
        //  extended JMXServiceURL  uses the same port for both the RMIRegistry and the client port
        // see: http://blogs.sun.com/jmxetc/entry/connecting_through_firewall_using_jmx
        // the first hostPort value is the host/port to be used for the client connections; this makes it a fixed
        // port number and we're making it the same as the RMI registry port.
        //final String urlStr = "service:jmx:rmi:///jndi/rmi://" + hostPort + "/" + name;

        final String urlStr = "service:jmx:rmi://" + jmxHostPort + "/jndi/rmi://" + registryHostPort + "/" + name;

        mJMXServiceURL = new JMXServiceURL(urlStr);
        if (mBindToSingleIP) {
            RMIServerSocketFactory rmiSSF = isSecurityEnabled() ? sslServerSocketFactory : mServerSocketFactory;
            mMyServer = new MyRMIJRMPServerImpl(mPort, env, rmiSSF, hostname);
            mConnectorServer = new RMIConnectorServer(mJMXServiceURL, env, mMyServer, mMBeanServer);
        } else {
            mConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(mJMXServiceURL, env, mMBeanServer);
        }

        if (mBootListener != null) {
            mConnectorServer.addNotificationListener(mBootListener, null, mJMXServiceURL.toString());
        }

        mConnectorServer.start();

        return mConnectorServer;
    }

    /**
     * Stops the connector and unexports the RMIServer from the Registry.
     */
    public void stopAndUnexport() {

        super.stop();
        try {
            if (this.mBindToSingleIP) {
                mRegistry.unbind(mHostName);
            }
            UnicastRemoteObject.unexportObject(mRegistry, true);
        } catch (RemoteException ex) {
            Util.getLogger().log(Level.SEVERE, ERROR_STOPPING, ex);
        } catch (NotBoundException ex) {
            Util.getLogger().log(Level.SEVERE, ERROR_STOPPING, ex);
        }
    }

    /**
     * This method sets up an environment based on passed in SSL configuration
     *
     * @param sslConfig
     * @return SslRMIClientSocketFactory
     */
    private SslRMIClientSocketFactory getClientSocketFactory(Ssl sslConfig) {
        // create SSLParams
        SSLParams sslParams = convertToSSLParams(sslConfig);

        // configure the context using these params
        SSLClientConfigurator sslCC = SSLClientConfigurator.getInstance();
        sslCC.setSSLParams(sslParams);
        SSLContext sslContext = sslCC.configure(sslParams);

        // Now pass this context to the ClientSocketFactory
        Security.setProperty("ssl.SocketFactory.provider", sslContext.getClass().getName());

        String enabledProtocols = sslCC.getEnabledProtocolsAsString();
        if (enabledProtocols != null) {
            setProperty("javax.rmi.ssl.client.enabledProtocols", enabledProtocols, true);
        }

        String enabledCipherSuites = sslCC.getEnabledCipherSuitesAsString();
        if (enabledCipherSuites != null) {
            setProperty("javax.rmi.ssl.client.enabledCipherSuites", enabledCipherSuites, true);
        }

        // The keystore and truststore locations are already available as System properties
        // Hence we just add the passwords
        setProperty(KEYSTORE_PASSWORD.getSystemPropertyName(),
            sslParams.getKeyStorePassword() == null ? KEYSTORE_PASSWORD_DEFAULT : sslParams.getKeyStorePassword(),
            true);
        setProperty(TRUSTSTORE_PASSWORD.getSystemPropertyName(),
            sslParams.getTrustStorePassword() == null ? KEYSTORE_PASSWORD_DEFAULT : sslParams.getTrustStorePassword(),
            true);

        return new SslRMIClientSocketFactory();
    }

    /**
     * Utility method to convert the SSLConfiguration to lightweight structure
     * which can be used without depending upon GlassFish.
     *
     * @param sslConfig
     * @return
     */
    private SSLParams convertToSSLParams(Ssl sslConfig) {

        // Get the values from the System properties
        String trustStoreType = sslConfig.getTrustStoreType() == null
            ? System.getProperty(TRUSTSTORE_TYPE.getSystemPropertyName(), KEYSTORE_TYPE_DEFAULT)
            : sslConfig.getTrustStoreType();
        String trustStorePwd = sslConfig.getTrustStorePassword() == null
            ? masterPassword
            : sslConfig.getTrustStorePassword();
        File trustStore = sslConfig.getTrustStore() == null
            ? new File(System.getProperty(TRUSTSTORE_FILE.getSystemPropertyName()))
            : new File(sslConfig.getTrustStore());

        String keyStoreType = sslConfig.getTrustStoreType() == null
            ? System.getProperty(KEYSTORE_TYPE.getSystemPropertyName(), KEYSTORE_TYPE_DEFAULT)
            : sslConfig.getKeyStoreType();
        String keyStorePwd = sslConfig.getTrustStorePassword() == null
            ? masterPassword
            : sslConfig.getKeyStorePassword();
        File keyStore = sslConfig.getTrustStore() == null
            ? new File(System.getProperty(KEYSTORE_FILE.getSystemPropertyName()))
            : new File(sslConfig.getKeyStore());

        SSLParams sslParams = new SSLParams(trustStore, trustStorePwd, trustStoreType);

        sslParams.setTrustAlgorithm(sslConfig.getTrustAlgorithm());
        sslParams.setCertNickname(sslConfig.getCertNickname());
        sslParams.setCrlFile(sslConfig.getCrlFile());

        sslParams.setClientAuthEnabled(sslConfig.getClientAuthEnabled());
        sslParams.setClientAuth(sslConfig.getClientAuth());

        sslParams.setKeyAlgorithm(sslConfig.getKeyAlgorithm());
        sslParams.setKeyStore(keyStore.getAbsolutePath());
        sslParams.setKeyStorePassword(keyStorePwd);
        sslParams.setKeyStoreType(keyStoreType);

        sslParams.setSsl2Ciphers(sslConfig.getSsl2Ciphers());
        sslParams.setSsl2Enabled(sslConfig.getSsl2Enabled());
        sslParams.setSsl3Enabled(sslConfig.getSsl3Enabled());
        sslParams.setSsl3TlsCiphers(sslConfig.getSsl3TlsCiphers());
        sslParams.setTlsEnabled(sslConfig.getTlsEnabled());
        sslParams.setTlsRollbackEnabled(sslConfig.getTlsRollbackEnabled());

        return sslParams;
    }


    /**************************************************************************
     * Inner classes
     **************************************************************************/

    /**
     * Custom implementation of the RMISocketFactory used for multihomed machines
     */
    public static final class MyRMIServerSocketFactory extends RMISocketFactory {

        private final InetAddress mAddress;

        public MyRMIServerSocketFactory(final InetAddress addr) {
            mAddress = addr;
        }

        @Override
        public ServerSocket createServerSocket(int port) throws IOException {
            //debug( "MyRMIServerSocketFactory.createServerSocket(): " + mAddress + " : " + port );
            final int backlog = 5;  // plenty
            final ServerSocket s = new ServerSocket(port, backlog, mAddress);
            //debug( "MyRMIServerSocketFactory.createServerSocket(): " + mAddress + " : " + port );
            return s;
        }

        /**
         * shouldn't be called
         */
        @Override
        public Socket createSocket(String host, int port) throws IOException {
            //debug( "MyRMIServerSocketFactory.createSocket(): " + host + " : " + port );
            final Socket s = new Socket(host, port);
            return s;
        }
    }

    /**
     * Purpose: to ensure binding to a specific IP address instead fo all IP addresses.
     */
    private static final class MyRMIJRMPServerImpl extends RMIJRMPServerImpl {

        private final String mBindToAddr;

        public MyRMIJRMPServerImpl(final int port, final Map<String, ?> env,
                                   final RMIServerSocketFactory serverSocketFactory,
                                   final String bindToAddr) throws IOException {
            super(port,
                    (RMIClientSocketFactory) env.get(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE),
                    serverSocketFactory,
                    env);
            mBindToAddr = bindToAddr;
        }

        /**
         * must be 'synchronized': threads can't save/restore the same system property concurrently
         */
        protected synchronized void export(final String host) throws IOException {
            final String saved = setupRMIHostname(mBindToAddr);
            try {
                super.export();
                JMX_LOGGER.log(Level.INFO, EXPORTED, mBindToAddr);
            } finally {
                restoreRMIHostname(saved, mBindToAddr);
            }
        }

        /**
         * must be 'synchronized': threads can't save/restore the same system property concurrently
         */
        @Override
        protected synchronized RMIConnection makeClient(final String connectionId, final Subject subject) throws IOException {
            final String saved = setupRMIHostname(mBindToAddr);

            try {
                Util.getLogger().log(Level.INFO, MAKE_CLIENT, System.getProperty(RMI_HOSTNAME_PROP));
                return super.makeClient(connectionId, subject);
            } finally {
                restoreRMIHostname(saved, mBindToAddr);
            }
        }
    }
}
