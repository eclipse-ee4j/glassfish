/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client;

import com.sun.enterprise.container.common.spi.util.InjectionException;
import com.sun.enterprise.deployment.node.SaxParserHandlerBundled;
import com.sun.enterprise.universal.glassfish.TokenResolver;
import com.sun.enterprise.util.LocalStringManager;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.ValidationEvent;
import jakarta.xml.bind.util.ValidationEventCollector;

import java.io.BufferedReader;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.glassfish.appclient.client.acc.ACCClassLoader;
import org.glassfish.appclient.client.acc.ACCLogger;
import org.glassfish.appclient.client.acc.AgentArguments;
import org.glassfish.appclient.client.acc.AppClientContainer;
import org.glassfish.appclient.client.acc.AppClientContainer.Builder;
import org.glassfish.appclient.client.acc.AppclientCommandArguments;
import org.glassfish.appclient.client.acc.CommandLaunchInfo;
import org.glassfish.appclient.client.acc.CommandLaunchInfo.ClientLaunchType;
import org.glassfish.appclient.client.acc.TargetServerHelper;
import org.glassfish.appclient.client.acc.UserError;
import org.glassfish.appclient.client.acc.Util;
import org.glassfish.appclient.client.acc.config.AuthRealm;
import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.common.util.GlassfishUrlClassLoader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import static org.glassfish.appclient.client.acc.CommandLaunchInfo.ClientLaunchType.UNKNOWN;

/**
 * @author tjquinn
 */
public class AppClientFacade {

    private static final String ACC_CONFIG_CONTENT_PROPERTY_NAME = "glassfish-acc.xml.content";
    private static final String MAN_PAGE_PATH = "/org/glassfish/appclient/client/acc/appclient.1m";
    private static final String LINE_SEP = System.lineSeparator();

    private static final Class<?> stringsAnchor = ACCClassLoader.class;
    private static LocalStringManager localStrings = new LocalStringManagerImpl(stringsAnchor);

    private static CommandLaunchInfo launchInfo;
    private static AppclientCommandArguments appClientCommandArgs;
    private static AppClientContainer appClientContainer;

    /**
     * Prepares the ACC (if not already done by the agent) and then transfers control to the ACC.
     * <p>
     * Eventually, the Java runtime will invoke this method as the main method of the application, whether or not the
     * command line specified the Java agent. If the agent has already run, then it will have prepared the ACC already. If
     * the agent has not already run, then this method prepares it.
     * <p>
     * If the user has run the generated app client JAR directly - not using the appclient script - then the Java runtime
     * will invoke this method directly and the command-line arguments should be intended for the client only; no agent or
     * ACC settings are possible. If the user has used the appclient script, then the script will have created a Java
     * command which specifies the agent, constructs an agent argument string, and passes as command line arguments only
     * those values which should be passed to the client. The net result is that, no matter how the app client was launched,
     * the args array contains only the arguments that are for the client's consumption, without any agent or ACC arguments.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            if (appClientContainer == null) {
                /*
                 * The facade JAR has been run directly, not via the appclient script and not via Java Web Start. So we have no agent
                 * arguments and no instrumentation for registering transformations.
                 *
                 * Because the agent has not run, we prepare the ACC here. (The agent would have done so itself had it run.)
                 */
                prepareACC(null, null);
            }

            /*
             * In any case, the ACC is now prepared. Launch the app client in the prepared ACC.
             */
            appClientContainer.launch(args);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        } catch (UserError ue) {
            ue.displayAndExit();
        }
    }

    public static AppClientContainer acc() {
        return appClientContainer;
    }

    public static void launch(String[] args) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, IOException, SAXException, InjectionException, UserError {
        appClientContainer.launch(args);
    }

    public static void prepareACC(String agentArgsText, Instrumentation inst) throws UserError, MalformedURLException, URISyntaxException,
            JAXBException, FileNotFoundException, ParserConfigurationException, SAXException, IOException, Exception {
        int version = Runtime.version().feature();
        if (version != 17) {
            throw new UserError(localStrings.getLocalString(stringsAnchor, "main.badVersion",
                "Current Java version {0} is too low; {1} or later required",
                new Object[] {System.getProperty("java.version"), "21"}));
        }

        /*
         * Analyze the agent argument string.
         */
        AgentArguments agentArgs = AgentArguments.newInstance(agentArgsText);

        /*
         * The agent arguments that correspond to the ones that we want to pass to the ACC are the ones with the "arg=" keyword
         * prefix. These will include arguments with meaning to the ACC (-textauth for example) as well as arguments to be
         * passed on to the client's main method.
         */
        appClientCommandArgs = AppclientCommandArguments.newInstance(agentArgs.namedValues("arg"));

        if (appClientCommandArgs.isUsage()) {
            usage(0);
        } else if (appClientCommandArgs.isHelp()) {
            help();
        }

        /*
         * Examine the agent arguments for settings about how to launch the client.
         */
        launchInfo = CommandLaunchInfo.newInstance(agentArgs);
        if (launchInfo.getClientLaunchType() == UNKNOWN) {
            usage(1);
        }

        /*
         * Handle the legacy env. variable APPCPATH.
         */
        ACCClassLoader loader = initClassLoader((inst == null));
        Thread.currentThread().setContextClassLoader(loader);

        /*
         * The installRoot property will be set by the ServerEnvironment initialization using the ACC start-up context. That
         * happens during the ACCModulesManager warm-up.
         */

        /*
         * Load the ACC configuration XML file.
         */
        ClientContainer clientContainer = readConfig(appClientCommandArgs.getConfigFilePath(), loader);

        /*
         * Decide what target servers to use. This combines any specified on the command line with any in the config file's
         * target-server elements as well as any set in the properties of the config file.
         */
        final TargetServer[] targetServers = TargetServerHelper.targetServers(clientContainer, appClientCommandArgs.getTargetServer());

        /*
         * Get the builder. Doing so correctly involves merging the configuration file data with some of the command line and
         * agent arguments.
         */
        final AppClientContainer.Builder builder = createBuilder(targetServers, clientContainer, appClientCommandArgs);

        /*
         * Create the ACC. Again, precisely how we create it depends on some of the command line arguments and agent arguments.
         */
        final AppClientContainer newACC = createContainer(builder, launchInfo, appClientCommandArgs);

        /*
         * Because the JMV might invoke the client's main class, the agent needs to prepare the container. (This is done as part
         * of the AppClientContainer.start() processing in the public API.
         */
        newACC.prepare(inst);

        appClientContainer = newACC;
    }

    private static void usage(final int exitStatus) {
        System.err.println(getUsage());
        System.exit(exitStatus);
    }

    private static void help() throws IOException {
        final InputStream is = AppClientFacade.class.getResourceAsStream(MAN_PAGE_PATH);
        if (is == null) {
            usage(0);
        }

        try (BufferedReader helpReader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = helpReader.readLine()) != null) {
                System.err.println(line);
            }
        } finally {
            System.exit(0);
        }
    }

    private static String getUsage() {
        return localStrings.getLocalString(stringsAnchor, "main.usage",
        "appclient [ <classfile> | -client <appjar> ] [-mainclass <appClass-name>|-name <display-name>] [-xml <xml>] [-textauth] [-user <username>] [-password <password>|-passwordfile <password-file>] [-targetserver host[:port][,host[:port]...] [app-args]")
        + System.lineSeparator() + localStrings.getLocalString(stringsAnchor, "main.usage.1",
        "  or  :\n\tappclient [ <valid JVM options and valid ACC options> ] [ <appClass-name> | -jar <appjar> ] [app args]");
    }

    private static ACCClassLoader initClassLoader(final boolean loaderShouldTransform) throws MalformedURLException {
        ACCClassLoader loader = ACCClassLoader.instance();
        if (loader == null) {
            loader = ACCClassLoader.newInstance(Thread.currentThread().getContextClassLoader(), loaderShouldTransform);
        }

        return loader;
    }

    private static Builder createBuilder(TargetServer[] targetServers, ClientContainer clientContainer,
            AppclientCommandArguments appClientCommandArgs) throws IOException {
        Builder builder = AppClientContainer.newBuilder(targetServers);

        /*
         * Augment the builder with settings from the app client options that can affect the builder itself. (This is distinct
         * from options that affect what client to launch which are handled in creating the ACC itself.
         */
        updateClientCredentials(builder, appClientCommandArgs);
        List<MessageSecurityConfig> msc = clientContainer.getMessageSecurityConfig();
        if (msc != null) {
            builder.getMessageSecurityConfig().addAll(clientContainer.getMessageSecurityConfig());
        }

        builder.logger(new ACCLogger(clientContainer.getLogService()));

        AuthRealm authRealm = clientContainer.getAuthRealm();
        if (authRealm != null) {
            builder.authRealm(authRealm.getClassname());
        }

        List<Property> property = clientContainer.getProperty();
        if (property != null) {
            builder.containerProperties(property);
        }

        return builder;
    }

    private static void updateClientCredentials(final Builder builder, final AppclientCommandArguments appClientCommandArgs) {
        ClientCredential clientCredential = builder.getClientCredential();
        String user = (clientCredential != null ? clientCredential.getUserName() : null);
        char[] pw = (clientCredential != null && clientCredential.getPassword() != null ? clientCredential.getPassword().get() : null);

        /*
         * user on command line?
         */
        String commandLineUser;
        if ((commandLineUser = appClientCommandArgs.getUser()) != null) {
            user = commandLineUser;
        }

        /*
         * password or passwordfile on command line? (theAppClientCommandArgs class takes care of reading the password from the
         * file and/or handling the -password option.
         */
        char[] commandLinePW;
        if ((commandLinePW = appClientCommandArgs.getPassword()) != null) {
            pw = commandLinePW;
        }

        builder.clientCredentials(user, pw);
    }

    private static AppClientContainer createContainer(Builder builder, CommandLaunchInfo launchInfo,
            AppclientCommandArguments appClientArgs) throws Exception, UserError {

        /*
         * The launchInfo already knows something about how to conduct the launch.
         */
        ClientLaunchType launchType = launchInfo.getClientLaunchType();
        AppClientContainer container;

        switch (launchType) {
        case JAR:
        case DIR:
            /*
             * The client name in the launch info is a file path for the directory or JAR to launch.
             */
            container = createContainerForAppClientArchiveOrDir(builder, launchInfo.getClientName(), appClientArgs.getMainclass(),
                    appClientArgs.getName());
            break;

        case URL:
            container = createContainerForJWSLaunch(builder, launchInfo.getClientName(), appClientArgs.getMainclass(),
                    appClientArgs.getName());
            break;

        case CLASS:
            container = createContainerForClassName(builder, launchInfo.getClientName());
            break;

        case CLASSFILE:
            container = createContainerForClassFile(builder, launchInfo.getClientName());
            break;

        default:
            container = null;
        }

        if (container == null) {
            throw new IllegalArgumentException("cannot choose app client launch type");
        }

        return container;
    }

    private static AppClientContainer createContainerForAppClientArchiveOrDir(Builder builder, String appClientPath, String mainClassName, String clientName) throws Exception, UserError {
        return builder.newContainer(Util.getURI(new File(appClientPath)), null /* callbackHandler */, mainClassName, clientName, appClientCommandArgs.isTextauth());
    }

    private static AppClientContainer createContainerForJWSLaunch(Builder builder, String appClientPath, String mainClassName, String clientName) throws Exception, UserError {
        return builder.newContainer(URI.create(appClientPath), null /* callbackHandler */, mainClassName, clientName);
    }

    private static AppClientContainer createContainerForClassName(Builder builder, String className) throws Exception, UserError {

        /*
         * Place "." on the class path so that when we convert the class file path to a fully-qualified class name and try to
         * load it, we'll find it.
         */

        ClassLoader loader = prepareLoaderToFindClassFile(Thread.currentThread().getContextClassLoader());
        Thread.currentThread().setContextClassLoader(loader);

        return builder.newContainer(Class.forName(className, true, loader));
    }

    private static ClassLoader prepareLoaderToFindClassFile(final ClassLoader currentLoader) throws MalformedURLException {
        File currentDirPath = new File(System.getProperty("user.dir"));
        return new GlassfishUrlClassLoader("AppClientFacade(" + currentDirPath + ")",
            new URL[] {currentDirPath.toURI().toURL()}, currentLoader);
    }

    private static AppClientContainer createContainerForClassFile(Builder builder, String classFilePath)
            throws MalformedURLException, ClassNotFoundException, FileNotFoundException, IOException, Exception, UserError {

        Util.verifyFilePath(classFilePath);

        /*
         * Strip off the trailing .class from the path and convert separator characters to dots to build a fully-qualified class
         * name.
         */
        String className = classFilePath.substring(0, classFilePath.lastIndexOf(".class")).replace(File.separatorChar, '.');

        return createContainerForClassName(builder, className);
    }

    private static ClientContainer readConfig(final String configPath, final ClassLoader loader) throws UserError, JAXBException,
            FileNotFoundException, ParserConfigurationException, SAXException, URISyntaxException, IOException {
        ClientContainer result = null;
        Reader configReader = null;
        String configFileLocationForErrorMessage = "";
        try {
            /*
             * During a Java Web Start launch, the config is passed as a property value.
             */
            String configInProperty = System.getProperty(ACC_CONFIG_CONTENT_PROPERTY_NAME);
            if (configInProperty != null) {
                /*
                 * Awkwardly, the glassfish-acc.xml content refers to a config file. We work around this for Java Web Start launch by
                 * capturing the content of that config file into a property setting in the generated JNLP document. We need to write
                 * that content into a temporary file here on the client and then replace a placeholder in the glassfish-acc.xml content
                 * with the path to that temp file.
                 */
                final File securityConfigTempFile = Util.writeTextToTempFile(configInProperty, "wss-client-config", ".xml", false);
                final Properties p = new Properties();
                p.setProperty("security.config.path", securityConfigTempFile.getAbsolutePath());
                configInProperty = Util.replaceTokens(configInProperty, p);
                configReader = new StringReader(configInProperty);
            } else {
                /*
                 * This is not a Java Web Start launch, so read the configuration from a disk file.
                 */
                File configFile = checkXMLFile(configPath);
                checkXMLFile(appClientCommandArgs.getConfigFilePath());
                configReader = new FileReader(configFile);
                configFileLocationForErrorMessage = configFile.getAbsolutePath();
            }

            /*
             * Although JAXB makes it very simple to parse the XML into Java objects, we have to do several things explicitly to use
             * our local copies of DTDs and XSDs.
             */
            SAXParserFactory spf = SAXParserFactory.newInstance();
            spf.setValidating(true);
            spf.setNamespaceAware(true);
            SAXParser parser = spf.newSAXParser();
            XMLReader reader = parser.getXMLReader();

            /*
             * Get the local entity resolver that knows about the bundled .dtd and .xsd files.
             */
            reader.setEntityResolver(new SaxParserHandlerBundled());

            /*
             * To support installation-directory independence the default glassfish-acc.xml refers to the wss-config file using
             * ${com.sun.aas.installRoot}... So preprocess the glassfish-acc.xml file to replace any tokens with the corresponding
             * values, then submit that result to JAXB.
             */
            InputSource inputSource = replaceTokensForParsing(configReader);

            SAXSource saxSource = new SAXSource(reader, inputSource);
            JAXBContext jc = JAXBContext.newInstance(ClientContainer.class);
            final ValidationEventCollector vec = new ValidationEventCollector();

            Unmarshaller u = jc.createUnmarshaller();
            u.setEventHandler(vec);
            result = (ClientContainer) u.unmarshal(saxSource);
            if (vec.hasEvents()) {
                /*
                 * The parser reported at least one warning or error. If all events were warnings, display them as a message and
                 * continue. Otherwise there was at least one error or fatal, so say so and try to continue but say that such errors
                 * might be fatal in future releases.
                 */
                boolean isError = false;
                final StringBuilder sb = new StringBuilder();
                for (ValidationEvent ve : vec.getEvents()) {
                    sb.append(ve.getMessage()).append(LINE_SEP);
                    isError |= (ve.getSeverity() != ValidationEvent.WARNING);
                }

                String messageIntroduction = localStrings.getLocalString(AppClientFacade.class,
                        isError ? "appclient.errParsingConfig" : "appclient.warnParsingConfig",
                        isError ? "Error parsing app client container configuration {0}.  Attempting to continue.  In future releases such parsing errors might become fatal.  Please correct your configuration file."
                                : "Warning(s) parsing app client container configuration {0}.  Continuing.",
                        new Object[] { configFileLocationForErrorMessage });

                System.err.println(messageIntroduction + LINE_SEP + sb.toString());
            }

            return result;
        } finally {
            if (configReader != null) {
                configReader.close();
            }
        }
    }

    private static InputSource replaceTokensForParsing(final Reader reader) throws FileNotFoundException, IOException, URISyntaxException {
        char[] buffer = new char[1024];

        CharArrayWriter writer = new CharArrayWriter();
        int charsRead;
        while ((charsRead = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, charsRead);
        }
        writer.close();
        reader.close();

        Map<String, String> mapping = new HashMap<>();
        Properties props = System.getProperties();
        for (Enumeration e = props.propertyNames(); e.hasMoreElements();) {
            String propName = (String) e.nextElement();
            mapping.put(propName, props.getProperty(propName));
        }

        TokenResolver resolver = new TokenResolver(mapping);
        String configWithTokensReplaced = resolver.resolve(writer.toString());
        InputSource inputSource = new InputSource(new StringReader(configWithTokensReplaced));
        return inputSource;
    }

    private static File checkXMLFile(String xmlFullName) throws UserError {
        try {
            File f = new File(xmlFullName);
            if (f.exists() && f.isFile() && f.canRead()) {
                return f;
            } else {// If given file does not exists
                xmlMessage(xmlFullName);
                return null;
            }
        } catch (Exception ex) {
            xmlMessage(xmlFullName);
            return null;
        }
    }

    private static void xmlMessage(String xmlFullName) throws UserError {
        UserError ue = new UserError(localStrings.getLocalString(stringsAnchor, "main.cannot_read_clientContainer_xml",
                "Client Container xml: {0} not found or unable to read.\nYou may want to use the -xml option to locate your configuration xml.",
                new String[] { xmlFullName }));
        ue.setUsage(getUsage());
        throw ue;

    }
}
