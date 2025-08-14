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

import com.sun.enterprise.deployment.node.SaxParserHandlerBundled;
import com.sun.enterprise.universal.glassfish.TokenResolver;

import jakarta.xml.bind.JAXBContext;
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
import java.net.MalformedURLException;
import java.net.URI;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.glassfish.appclient.client.acc.ACCLogger;
import org.glassfish.appclient.client.acc.AgentArguments;
import org.glassfish.appclient.client.acc.AppClientContainer;
import org.glassfish.appclient.client.acc.AppClientContainer.Builder;
import org.glassfish.appclient.client.acc.AppclientCommandArguments;
import org.glassfish.appclient.client.acc.CommandLaunchInfo;
import org.glassfish.appclient.client.acc.CommandLaunchInfo.ClientLaunchType;
import org.glassfish.appclient.client.acc.TargetServerHelper;
import org.glassfish.appclient.client.acc.TransformingClassLoader;
import org.glassfish.appclient.client.acc.Util;
import org.glassfish.appclient.client.acc.config.AuthRealm;
import org.glassfish.appclient.client.acc.config.ClientContainer;
import org.glassfish.appclient.client.acc.config.ClientCredential;
import org.glassfish.appclient.client.acc.config.MessageSecurityConfig;
import org.glassfish.appclient.client.acc.config.Property;
import org.glassfish.appclient.client.acc.config.TargetServer;
import org.glassfish.embeddable.client.ApplicationClientClassLoader;
import org.glassfish.embeddable.client.ApplicationClientContainer;
import org.glassfish.embeddable.client.UserError;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import static org.glassfish.appclient.client.acc.CommandLaunchInfo.ClientLaunchType.UNKNOWN;

/**
 * @author tjquinn
 * @author David Matejcek
 */
public class AppClientContainerHolder implements ApplicationClientContainer {

    private static final String ACC_CONFIG_CONTENT_PROPERTY_NAME = "glassfish-acc.xml.content";
    private static final String MAN_PAGE_PATH = "/org/glassfish/appclient/client/acc/appclient.1m";

    private static volatile AppClientContainerHolder instance;

    private CommandLaunchInfo launchInfo;
    private AppclientCommandArguments appClientCommandArgs;

    private ApplicationClientContainer appClientContainer;


    @Override
    public void launch(String[] args) throws UserError {
        appClientContainer.launch(args);
    }

    public ApplicationClientContainer getAppClientContainer() {
        return appClientContainer;
    }

    public static synchronized AppClientContainerHolder getInstance() {
        if (instance == null) {
            // The container was not initialized by the instrumentation executed
            // by the AppClientContainerAgent. We will try to make it.
            try {
                init(null, null);
            } catch (UserError ue) {
                ue.displayAndExit();
            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
        return instance;
    }

    /**
     * Initializes the {@link ApplicationClientContainer} singleton.
     *
     * @param agentArgsText
     * @param inst
     * @throws UserError
     * @throws Exception
     */
    public static synchronized void init(String agentArgsText, Instrumentation inst) throws UserError, Exception {
        int version = Runtime.version().feature();
        if (version < 17) {
            throw new UserError(
                MessageFormat.format("Current Java version {0} is too low; {1} or later required", version, "17"));
        }

        // Analyze the agent argument string.
        AgentArguments agentArgs = AgentArguments.newInstance(agentArgsText);


        // The agent arguments that correspond to the ones that we want to pass to the ACC are
        // the ones with the "arg=" keyword prefix.
        // These will include arguments with meaning to the ACC (-textauth for example) as well
        // as arguments to be passed on to the client's main method.
        AppClientContainerHolder holder = new AppClientContainerHolder();
        holder.appClientCommandArgs = AppclientCommandArguments.newInstance(agentArgs.namedValues("arg"));

        if (holder.appClientCommandArgs.isUsage()) {
            usage(0);
        } else if (holder.appClientCommandArgs.isHelp()) {
            help();
        }

        // Examine the agent arguments for settings about how to launch the client.
        holder.launchInfo = CommandLaunchInfo.newInstance(agentArgs);
        if (holder.launchInfo.getClientLaunchType() == UNKNOWN) {
            usage(1);
        }

        // Load the ACC configuration XML file.
        ClientContainer clientContainer = readConfig(holder.appClientCommandArgs);

        // Decide what target servers to use. This combines any specified on the command line with
        // any in the config file's target-server elements as well as any set in the properties of
        // the config file.
        final TargetServer[] targetServers = TargetServerHelper.targetServers(clientContainer,
            holder.appClientCommandArgs.getTargetServer());

        ClassLoader dependenciesCL = Thread.currentThread().getContextClassLoader();
        try {
            // This class loader does transformations and is saved inside the container.
            TransformingClassLoader loader = initClassLoader(dependenciesCL, inst == null);
            Thread.currentThread().setContextClassLoader(loader);

            // Get the builder. Doing so correctly involves merging the configuration file data with
            // some of the command line and agent arguments.
            final AppClientContainer.Builder builder = createBuilder(targetServers, clientContainer, holder.appClientCommandArgs);

            // Create the ACC. Again, precisely how we create it depends on some of the command line
            // arguments and agent arguments.
            final AppClientContainer newACC = createContainer(builder, holder.launchInfo, holder.appClientCommandArgs);

            // Because the JMV might invoke the client's main class, the agent needs to prepare the container.
            // (This is done as part of the AppClientContainer.start() processing in the public API.
            newACC.prepare(inst);
            holder.appClientContainer = newACC;
            ((ApplicationClientClassLoader) dependenciesCL).setApplicationClientContainer(holder.appClientContainer);
            AppClientContainerHolder.instance = holder;
        } finally {
            // Reset for possible usage in the AppClientGroupFacade
            Thread.currentThread().setContextClassLoader(dependenciesCL);
        }
    }

    private static void usage(final int exitStatus) {
        System.out.println(getUsage());
        System.exit(exitStatus);
    }

    private static void help() throws IOException {
        final InputStream is = AppClientContainerHolder.class.getResourceAsStream(MAN_PAGE_PATH);
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
        return "appclient [ <classfile> | -client <appjar> ] [-mainclass <appClass-name>|-name <display-name>]"
            + " [-xml <xml>] [-textauth] [-user <username>] [-password <password>|-passwordfile <password-file>]"
            + " [-targetserver host[:port][,host[:port]...] [app-args]" + System.lineSeparator()
            + "  or  :\n\tappclient [ <valid JVM options and valid ACC options> ] [ <appClass-name> | -jar <appjar> ]"
            + " [app args]";
    }


    private static TransformingClassLoader initClassLoader(final ClassLoader parent, final boolean loaderShouldTransform) {
        final TransformingClassLoader loader = TransformingClassLoader.instance();
        if (loader == null) {
            return TransformingClassLoader.newInstance(parent, loaderShouldTransform);
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
        String user = clientCredential == null ? null : clientCredential.getUserName();
        char[] pw = clientCredential != null && clientCredential.getPassword() != null ? clientCredential.getPassword().get() : null;

        /*
         * user on command line?
         */
        String commandLineUser = appClientCommandArgs.getUser();
        if (commandLineUser != null) {
            user = commandLineUser;
        }

        /*
         * password or passwordfile on command line? (theAppClientCommandArgs class takes care of reading the password from the
         * file and/or handling the -password option.
         */
        char[] commandLinePW = appClientCommandArgs.getPassword();
        if (commandLinePW != null) {
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
                // The client name in the launch info is a file path for the directory or JAR to launch.
                container = createContainerForAppClientArchiveOrDir(builder, launchInfo.getClientName(), appClientArgs);
                break;

            case URL:
                container = createContainerForJWSLaunch(builder, launchInfo.getClientName(), appClientArgs);
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


    private static AppClientContainer createContainerForAppClientArchiveOrDir(Builder builder, String appClientPath,
        AppclientCommandArguments appClientArgs) throws Exception, UserError {
        return builder.newContainer(Util.getURI(new File(appClientPath)), null,
            appClientArgs.getMainclass(), appClientArgs.getName(), appClientArgs.isTextauth());
    }


    private static AppClientContainer createContainerForJWSLaunch(Builder builder, String appClientPath,
        AppclientCommandArguments appClientArgs) throws Exception, UserError {
        return builder.newContainer(URI.create(appClientPath), null /* callbackHandler */, appClientArgs.getMainclass(),
            appClientArgs.getName());
    }


    /**
     * Place "." on the class path so that when we convert the class file path
     * to a fully-qualified class name and try to load it, we'll find it.
     */
    private static AppClientContainer createContainerForClassName(Builder builder, String className) throws Exception, UserError {
        return builder.newContainer(Class.forName(className, true, Thread.currentThread().getContextClassLoader()));
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

    private static ClientContainer readConfig(AppclientCommandArguments appClientCommandArgs) throws UserError, Exception {
        final String configPath = appClientCommandArgs.getConfigFilePath();
        ClientContainer result = null;
        Reader configReader = null;
        try {
            /*
             * During a Java Web Start launch, the config is passed as a property value.
             */
            final String configInProperty = System.getProperty(ACC_CONFIG_CONTENT_PROPERTY_NAME);
            final String configFileLocationForErrorMessage;
            if (configInProperty == null) {
                /*
                 * This is not a Java Web Start launch, so read the configuration from a disk file.
                 */
                File configFile = checkXMLFile(configPath);
                checkXMLFile(appClientCommandArgs.getConfigFilePath());
                configReader = new FileReader(configFile);
                configFileLocationForErrorMessage = configFile.getAbsolutePath();
            } else {
                /*
                 * Awkwardly, the glassfish-acc.xml content refers to a config file. We work around this for Java Web Start launch by
                 * capturing the content of that config file into a property setting in the generated JNLP document. We need to write
                 * that content into a temporary file here on the client and then replace a placeholder in the glassfish-acc.xml content
                 * with the path to that temp file.
                 */
                final File securityConfigTempFile = Util.writeTextToTempFile(configInProperty, "wss-client-config", ".xml", false);
                final Properties p = new Properties();
                p.setProperty("security.config.path", securityConfigTempFile.getAbsolutePath());
                configReader = new StringReader(Util.replaceTokens(configInProperty, p));
                configFileLocationForErrorMessage = null;
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
            Unmarshaller u = jc.createUnmarshaller();
            final ValidationEventCollector vec = new ValidationEventCollector();
            u.setEventHandler(vec);
            result = (ClientContainer) u.unmarshal(saxSource);
            if (vec.hasEvents()) {
                printValidationEvents(configFileLocationForErrorMessage, vec.getEvents());
            }
            return result;
        } finally {
            if (configReader != null) {
                configReader.close();
            }
        }
    }

    private static void printValidationEvents(String file, ValidationEvent[] events) {
        /*
         * The parser reported at least one warning or error. If all events were warnings, display them as a message and
         * continue. Otherwise there was at least one error or fatal, so say so and try to continue but say that such errors
         * might be fatal in future releases.
         */
        boolean isError = false;
        final StringBuilder sb = new StringBuilder();
        for (ValidationEvent ve : events) {
            sb.append(ve.getMessage()).append(System.lineSeparator());
            isError |= (ve.getSeverity() != ValidationEvent.WARNING);
        }

        String messageIntroduction = MessageFormat.format(isError
            ? "Error parsing app client container configuration{0}."
                + " Attempting to continue."
                + " In future releases such parsing errors might become fatal."
                + " Please correct your configuration file."
            : "Warning(s) parsing app client container configuration{0}."
                + " Continuing.",
            file == null ? ""  : " file " + file);
        System.err.println(messageIntroduction + System.lineSeparator() + sb);
    }

    private static InputSource replaceTokensForParsing(final Reader reader) throws FileNotFoundException, IOException {
        CharArrayWriter writer = new CharArrayWriter();
        try {
            char[] buffer = new char[4096];
            int charsRead;
            while ((charsRead = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, charsRead);
            }
        } finally {
            writer.close();
        }
        reader.close();

        Map<String, String> mapping = new HashMap<>();
        Properties props = System.getProperties();
        for (String propName : props.stringPropertyNames()) {
            mapping.put(propName, props.getProperty(propName));
        }

        TokenResolver resolver = new TokenResolver(mapping);
        String configWithTokensReplaced = resolver.resolve(writer.toString());
        return new InputSource(new StringReader(configWithTokensReplaced));
    }

    private static File checkXMLFile(String xmlFullName) throws UserError {
        try {
            File f = new File(xmlFullName);
            if (f.exists() && f.isFile() && f.canRead()) {
                return f;
            }
            // If given file does not exists
            xmlMessage(xmlFullName);
            return null;
        } catch (Exception ex) {
            xmlMessage(xmlFullName);
            return null;
        }
    }

    private static void xmlMessage(String xmlFullName) throws UserError {
        UserError ue = new UserError(MessageFormat.format(
            "Client Container xml: {0} not found or unable to read.\nYou may want to use the -xml option to locate your configuration xml.",
            xmlFullName));
        ue.setUsage(getUsage());
        throw ue;

    }
}
