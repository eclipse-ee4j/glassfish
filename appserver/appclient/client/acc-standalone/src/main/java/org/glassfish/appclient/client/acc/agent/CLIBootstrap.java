/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client.acc.agent;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.embeddable.client.ApplicationClientCLIEncoding;
import org.glassfish.embeddable.client.UserError;

import static java.lang.System.arraycopy;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.JAVA_HOME;

/**
 * Constructs a java command to launch the ACC with the correct agent and command line arguments, based on the current
 * operating environment and the user's own command-line arguments.
 *
 * <p>
 * The user might have specified JVM options as well as ACC options as well as arguments to be passed to the client.
 * Further, we need to make sure that the GlassFish extension libraries directories directories are included regardless
 * of whether the user specified any explicitly.
 *
 * <p>
 * This program emits a java command line that will run the ACC so that it will launch the client. The emitted command
 * will need to look like this:
 *
 * <pre>
 * {@code
 * java \
 *   (user-specified JVM options except -jar) \
 *   -javaagent:(path-to-gf-client.jar)=(option string for our agent) \
 *   (main class setting: "-jar x.jar" or "a.b.Main" or "path-to-file.class")
 *   (arguments to be passed to the client)
 * }
 * </pre>
 *
 * <p>
 * The general design of this class uses several inner classes, CommandLineElement and its extensions. These classes
 * have slightly different behavior depending on the specific type of command line element each represents. Each has a
 * regex pattern which it uses to decide whether it recognizes a particular command line element or not. Each also
 * implements (or inherits) the processValue method which actually consumes the command line element being handled --
 * and sometimes the next one as well if the element takes a value (such as -classpath).
 *
 * @author Tim Quinn
 */
public class CLIBootstrap {

    private static final boolean IS_WINDOWS = System.getProperty("os.name", "generic").startsWith("Win");
    public final static String FILE_OPTIONS_INTRODUCER = "argsfile=";

    private final static boolean isDebug = System.getenv("AS_DEBUG") != null;
    private final static String INPUT_ARGS = System.getenv("inputArgs");

    static final String ENV_VAR_PROP_PREFIX = "acc.";


    private final static String SECURITY_POLICY_PROPERTY_EXPR = "-Djava.security.policy=";
    private final static String SECURITY_AUTH_LOGIN_CONFIG_PROPERTY_EXPR = "-Djava.security.auth.login.config=";
    private final static String SYSPROP_SYSTEM_CLASS_LOADER = "-Djava.system.class.loader=";

    private final static String[] ENV_VARS = { "AS_INSTALL", "APPCPATH", "VMARGS" };

    private JavaInfo java = new JavaInfo();
    private GlassFishInfo gfInfo = new GlassFishInfo();
    /** Records how the user specifies the main class: -jar xxx.jar, -client xxx.jar, or a.b.MainClass */
    private final JVMMainOption jvmMainSetting = new JVMMainOption();
    // note: must be defined after jvmMainSetting, because it uses it
    private final UserVMArgs userVMArgs = new UserVMArgs(System.getProperty(ENV_VAR_PROP_PREFIX + "VMARGS"));


    // Set up with various sub-types of command line elements
    /** options to the ACC that take a value */
    private final CommandLineElement accValuedOptions = new ACCValuedOption(
        "-mainclass|-name|-xml|-configxml|-user|-password|-passwordfile|-targetserver");

    /** options to the ACC that take no value */
    private final CommandLineElement accUnvaluedOptions = new ACCUnvaluedOption("-textauth|-noappinvoke|-usage|-help");

    private final CommandLineElement jvmValuedOptions = new JVMValuedOption("-classpath|-cp", userVMArgs.evJVMValuedOptions);
    private final CommandLineElement jvmPropertySettings = new JVMOption("-D.*", userVMArgs.evJVMPropertySettings);
    private final CommandLineElement otherJVMOptions = new JVMOption("-.*", userVMArgs.evOtherJVMOptions);
    private final CommandLineElement arguments = new CommandLineArgument(".*", Pattern.DOTALL);

    /** command line elements from most specific to least specific matching pattern */
    private final CommandLineElement[] elementsInScanOrder = new CommandLineElement[] {
            accValuedOptions,   // collects options into "agentArgs"
            accUnvaluedOptions, // collects options into "agentArgs"
            jvmValuedOptions,
            jvmPropertySettings,
            jvmMainSetting,
            otherJVMOptions,
            arguments };

    /**
     * Command line elements in the order they should appear on the generated command line
     * Add the elements in this order so they appear in the generated java command in the correct positions.
     */
    private final CommandLineElement[] elementsInOutputOrder = new CommandLineElement[] {
            jvmValuedOptions,
            jvmPropertySettings,
            otherJVMOptions,
            jvmMainSetting,
            arguments };


    /** Arguments passed to the ACC Java agent, collected by "accValuedOptions" and "accUnvaluedOptions"  */
    private final AgentArgs agentArgs = new AgentArgs();



    // #### Main() Methods


    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            /*
             * Convert env vars to properties. (This makes testing easier.)
             */
            envToProps();
            CLIBootstrap boot = new CLIBootstrap();

            /*
             * Because of how Windows passes arguments, the calling Windows script assigned the input arguments to an environment
             * variable. Parse that variable's value into the actual arguments.
             */
            if (INPUT_ARGS != null) {
                args = convertInputArgsVariable(INPUT_ARGS);
            }

            String outputCommandLine = boot.run(args);
            if (isDebug) {
                System.err.println(outputCommandLine);
            }

            /*
             * Write the generated java command to System.out. The calling shell script will execute this command.
             *
             * Using print instead of println seems to work better. Using println added a \r to the end of the last command-line
             * argument on Windows under cygwin.
             */
            System.out.print(outputCommandLine);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static void envToProps() {
        for (String envVar : ENV_VARS) {
            String value = System.getenv(envVar);
            if (value != null) {
                System.setProperty(ENV_VAR_PROP_PREFIX + envVar, value);
                if (isDebug) {
                    System.err.println(ENV_VAR_PROP_PREFIX + envVar + " set to " + value);
                }
            }
        }
    }

    private static String[] convertInputArgsVariable(String inputArgs) {
        /*
         * The pattern matches a quoted string (double quotes around a string containing no double quote) or a non-quoted string
         * (a string containing no white space or quotes).
         * Note:
         * escapedDoubleQuoteRegex matches a double quote following odd number of
         * backslash as an escaped string.
         */
        final String escapedDoubleQuoteRegex = "?:(?<!\\\\)(?:(?:\\\\\\\\)*\\\\)\"";
        Pattern argPattern = Pattern.compile(
                "\"((" + escapedDoubleQuoteRegex + "|[^\"])*)\""
                        + "|((" + escapedDoubleQuoteRegex + "|[^\"\\s])+)");

        Matcher matcher = argPattern.matcher(inputArgs);
        List<String> argList = new ArrayList<>();
        while (matcher.find()) {
            String arg = (matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            argList.add(arg);
            if (isDebug) {
                System.err.println("Captured argument " + arg);
            }
        }
        return argList.toArray(new String[argList.size()]);
    }




    // #### Instance methods
    CLIBootstrap() throws Error {
    }



    JavaInfo initJava() {
        return new JavaInfo();
    }

    /**
     * Processes the user-provided command-line elements and creates the resulting output string.
     *
     * @param args
     * @throws UserError
     */
    private String run(String[] args) throws Error {
        java = new JavaInfo();
        gfInfo = new GlassFishInfo();

        String[] augmentedArgs = new String[args.length + 2];
        augmentedArgs[0] = "-configxml";
        augmentedArgs[1] = gfInfo.configxml().getAbsolutePath();
        arraycopy(args, 0, augmentedArgs, 2, args.length);

        /*
         * Process each command-line argument by the first CommandLineElement which matches the argument.
         */
        for (int i = 0; i < augmentedArgs.length;) {
            boolean isMatched = false;
            for (CommandLineElement cle : elementsInScanOrder) {
                isMatched = cle.matches(augmentedArgs[i]);
                if (isMatched) {
                    i = cle.processValue(augmentedArgs, i);
                    break;
                }
            }
            if (!isMatched) {
                throw new Error("arg " + i + " = " + augmentedArgs[i] + " not recognized");
            }
        }

        StringBuilder command = new StringBuilder(quote(java.javaExe));

        addProperties(command);

        /*
         * The user does not specify the -javaagent option we need, so we provide it here. (It is added to the appropriate
         * command-line element object so, when formatted, that command-line element includes the -javaagent option.)
         */
        addAgentOption();

        /*
         * If the user did not specify a client or usage or help then add the -usage option.
         */
        if (!jvmMainSetting.isSet() && !isHelp() && !isUsage()) {
            accUnvaluedOptions.processValue(new String[] { "-usage" }, 0);
        }

        boolean needSep = true;
        for (CommandLineElement e : elementsInOutputOrder) {
            needSep = processCommandElement(command, e, needSep);
        }

        return command.toString();
    }

    /**
     * Adds JVM properties for various ACC settings.
     *
     * @param command
     */
    private void addProperties(final StringBuilder command) {
        final Path gfBootstrapLibs = gfInfo.lib.toPath().resolve("bootstrap").normalize();
        command.append(' ').append("--module-path ").append(quote(gfBootstrapLibs.toString()));
        command.append(' ').append("--add-modules ALL-MODULE-PATH");
        command.append(' ').append("--add-opens=java.base/java.lang=ALL-UNNAMED");
        command.append(' ').append("-Xshare:off");
        command.append(' ').append(SYSPROP_SYSTEM_CLASS_LOADER).append("org.glassfish.appclient.client.acc.agent.ACCAgentClassLoader");
        command.append(' ').append("-D").append(INSTALL_ROOT.getSystemPropertyName()).append('=').append(quote(gfInfo.home().getAbsolutePath()));
        command.append(' ').append("-Dorg.glassfish.gmbal.no.multipleUpperBoundsException=true");
        command.append(' ').append(SECURITY_POLICY_PROPERTY_EXPR).append(quote(gfInfo.securityPolicy().getAbsolutePath()));
        command.append(' ').append(SECURITY_AUTH_LOGIN_CONFIG_PROPERTY_EXPR).append(quote(gfInfo.loginConfig().toExternalForm()));
    }

    /**
     * Adds the -javaagent option to the command line.
     */
    private void addAgentOption() throws Error {
        otherJVMOptions.processValue(new String[] { "-javaagent:" + quote(gfInfo.agentJarPath()) + agentOptionsFromFile() }, 0);
    }

    private boolean processCommandElement(StringBuilder command, CommandLineElement commandLineElement, boolean needSep) {
        if (needSep) {
            command.append(' ');
        }

        return commandLineElement.format(command);
    }



    // #### Static utility methods

    /**
     * Places double quote marks around a string if the string is not already so enclosed.
     *
     * @param string
     * @return the string wrapped in double quotes if not already that way; the original string otherwise
     */
    private static String quote(String string) {
        if (string.length() > 2 && string.charAt(0) != '"' && string.charAt(string.length() - 1) != '"') {
            return '\"' + string + '\"';
        }

        return string;
    }

    /**
     * Quotes the string, on non-Windows systems quoting individually any $. The shell will have replaced any env. var.
     * placeholders with their values before invoking this program. Anything that looks like a placeholder now is an odd but
     * legal name that should not be substituted again.
     *
     * @param string
     * @return
     */
    private static String quoteSuppressTokenSubst(String string) {
        return IS_WINDOWS ? quote(string) : quote(string.replace("$", "\\$"));
    }

    /**
     * Quotes the string, on non-Windows systems escaping metacharacters ('\', '"', '$', '`').
     *
     * @param string
     * @return
     */
    static String quoteEscapedArgument(String string) {
        if (!IS_WINDOWS) {
            string = string.replace("\\", "\\\\").replace("\"", "\\\"").replace("$", "\\$").replace("`", "\\`");
        }
        return "\"" + string + "\"";
    }


    /**
     * Manages the arguments which will be passed to the ACC Java agent.
     */
    private static class AgentArgs {
        private final StringBuilder args = new StringBuilder("=mode=acscript");
        private final char sep = ',';

        AgentArgs() {
            final String appcPath = System.getProperty(ENV_VAR_PROP_PREFIX + "APPCPATH");
            if (appcPath != null && appcPath.length() > 0) {
                add("appcpath=" + quote(appcPath));
            }
        }

        /**
         * Adds an item to the Java agent arguments.
         *
         * @param item
         */
        final void add(String item) {
            args.append(sep).append(item);
        }

        /**
         * Adds an ACC argument to the Java agent arguments.
         *
         * @param accArg
         */
        final void addACCArg(String accArg) {
            add("arg=" + ApplicationClientCLIEncoding.encodeArg(accArg));
        }

        @Override
        public String toString() {
            return args.toString();
        }
    }



    // #### Base classes uses for the concrete elements


    /**
     * A command-line element. Various subtypes have some different behavior for some of the methods.
     */
    private class CommandLineElement {

        private final Pattern pattern;

        private final Pattern whiteSpacePattern = Pattern.compile("[\\r\\n]");

        /** Allows multiple values; not all command line elements support this */
        final List<String> values = new ArrayList<>();

        CommandLineElement(String patternString) {
            this(patternString, 0);
        }

        CommandLineElement(String patternString, int flags) {
            pattern = Pattern.compile(patternString, flags);
        }

        final boolean matchesPattern(String element) {
            Matcher matcher = pattern.matcher(element);
            return matcher.matches();
        }

        boolean matches(String element) {
            return matchesPattern(element);
        }

        /**
         * Processes the command line element at args[slot].
         * <p>
         * Subclass implementations might consume the next element as well.
         *
         * @param args
         * @param slot
         * @return next slot to be processed
         * @throws UserError if the user specified an option that requires a value but provided no value (either the next
         *                   command line element is another option or there is no next element)
         */
        int processValue(String[] args, int slot) throws Error {
            // Ignore an argument that is just unquoted white space.
            Matcher matcher = whiteSpacePattern.matcher(args[slot]);
            if (!matcher.matches()) {
                values.add(args[slot++]);
            } else {
                slot++;
            }

            return slot;
        }

        /**
         * Returns whether there is a next argument.
         *
         * @param args
         * @param currentSlot
         * @return
         */
        boolean isNextArg(String[] args, int currentSlot) {
            return currentSlot < args.length - 1;
        }

        /**
         * Makes sure that there is a next argument and that its value does not start with a "-" which would indicate an option,
         * rather than the value for the option we are currently processing.
         *
         * @param args
         * @param currentSlot
         * @throws UserError
         */
        void ensureNonOptionNextArg(final String[] args, final int currentSlot) throws Error {
            if ((currentSlot >= args.length - 1) || (args[currentSlot + 1].charAt(0) == '-')) {
                throw new Error("Command line element " + args[currentSlot] + " requires non-option value");
            }
        }

        /**
         * Adds a representation for this command-line element to the output command line.
         *
         * @param commandLine
         * @return true if any values from this command-line element was added to the command line, false otherwise
         */
        boolean format(final StringBuilder commandLine) {
            return format(commandLine, true);
        }

        /**
         * Adds a representation for this command-line element to the output command line, quoting the value if requested.
         *
         * @param commandLine
         * @param useQuotes
         * @return true if any values from this command-line element were added to the command line; false otherwise
         */
        boolean format(final StringBuilder commandLine, boolean useQuotes) {
            boolean needSep = false;
            for (String value : values) {
                if (needSep) {
                    commandLine.append(valueSep());
                }
                format(commandLine, useQuotes, value);
                needSep = true;
            }
            return !values.isEmpty();
        }

        /**
         * Returns the separator character to be inserted in the emitted command line between values stored in the same instance
         * of this command line element.
         *
         * @return
         */
        char valueSep() {
            return ' ';
        }

        /**
         * Adds a representation for the specified value to the output command line, quoting the value if required and
         *
         * @param commandLine
         * @param useQuotes
         * @param v
         * @return
         */
        StringBuilder format(final StringBuilder commandLine, final boolean useQuotes, final String v) {
            if (commandLine.length() > 0) {
                commandLine.append(' ');
            }
            commandLine.append((useQuotes ? quoteSuppressTokenSubst(v) : v));

            return commandLine;
        }
    }

    class CommandLineArgument extends CommandLineElement {
        CommandLineArgument(String patternString, int flags) {
            super(patternString, flags);
        }
        @Override
        StringBuilder format(final StringBuilder commandLine,
                final boolean useQuotes, final String nextArg) {
            if (commandLine.length() > 0) {
                commandLine.append(' ');
            }
            commandLine.append((useQuotes ? quoteEscapedArgument(nextArg) : nextArg));
            return commandLine;
        }
    }

    /**
     * A command-line option (an element which starts with "-").
     */
    private class Option extends CommandLineElement {
        Option(String patternString) {
            super(patternString);
        }
    }

    /**
     * An option that takes a value as the next command line element.
     */
    private class ValuedOption extends Option {

        class OptionValue {
            private final String option;
            private final String value;

            OptionValue(String option, String value) {
                this.option = option;
                this.value = value;
            }
        }

        List<OptionValue> optValues = new ArrayList<>();

        ValuedOption(final String patternString) {
            super(patternString);
        }

        @Override
        int processValue(String[] args, int slot) throws Error {
            ensureNonOptionNextArg(args, slot);
            optValues.add(new OptionValue(args[slot++], args[slot++]));

            return slot;
        }

        @Override
        boolean format(final StringBuilder commandLine) {
            for (OptionValue ov : optValues) {
                format(commandLine, false /* useQuotes */, ov.option);
                format(commandLine, true /* useQuotes */, ov.value);
            }
            return !optValues.isEmpty();
        }
    }



    // #### Concrete elements



    /**
     * ACC options can appear until "-jar xxx" on the command line.
     */
    private class ACCValuedOption extends ValuedOption {
        ACCValuedOption(final String patternString) {
            super(patternString);
        }

        @Override
        boolean matches(final String element) {
            return (!jvmMainSetting.isJarSetting()) && super.matches(element);
        }

        @Override
        int processValue(String[] args, int slot) throws Error {
            final int result = super.processValue(args, slot);
            final OptionValue newOptionValue = optValues.get(optValues.size() - 1);
            agentArgs.addACCArg(newOptionValue.option);
            agentArgs.addACCArg(quote(newOptionValue.value));
            return result;
        }

        @Override
        boolean format(final StringBuilder commandLine) {
            /*
             * We do not send ACC arguments to the Java command line. They are placed into the agent argument string instead.
             */
            return false;
        }
    }

    /**
     * ACC options match anywhere on the command line unless and until we see "-jar xxx" in which case we impose the
     * Java-style restriction that anything which follows the specification of the main class is an argument to be passed to
     * the application.
     * <p>
     * We do not impose the same restriction if the user specified -client xxx.jar in order to preserve backward
     * compatibility with earlier releases, in which ACC options and client arguments could be intermixed anywhere on the
     * command line.
     */
    private class ACCUnvaluedOption extends Option {
        ACCUnvaluedOption(final String patternString) {
            super(patternString);
        }

        @Override
        boolean matches(final String element) {
            return (!jvmMainSetting.isJarSetting()) && super.matches(element);
        }

        @Override
        int processValue(String[] args, int slot) throws Error {
            final int result = super.processValue(args, slot);
            agentArgs.addACCArg(values.get(values.size() - 1));
            return result;
        }

        @Override
        boolean format(final StringBuilder commandLine) {
            /*
             * We do not send ACC arguments to the Java command line. They are placed into the agent argument string instead.
             */
            return false;
        }
    }

    private class JVMValuedOption extends ValuedOption {

        JVMValuedOption(final String patternString, final CommandLineElement vmargsJVMValuedOption) {
            super(patternString);
            if (vmargsJVMValuedOption != null) {
                values.addAll(vmargsJVMValuedOption.values);
            }
        }

        @Override
        boolean matches(final String element) {
            return (!jvmMainSetting.isJarSetting()) && super.matches(element);
        }
    }

    /**
     * A JVM command-line option. Only JVM options which appear before the main class setting are propagated to the output
     * command line as JVM options. If they appear after the main class setting then they are treated as arguments to the
     * client.
     * <p>
     * This type of command line element can include values specified using the VMARGS environment variable.
     *
     */
    private class JVMOption extends Option {

        JVMOption(String patternString, CommandLineElement vmargsJVMOptionElement) {
            super(patternString);
            if (vmargsJVMOptionElement != null) {
                values.addAll(vmargsJVMOptionElement.values);
            }
        }

        @Override
        boolean matches(String element) {
            /*
             * Although the element might match the pattern (-.*) we do not treat this as JVM option if we have already processed
             * the main class determinant.
             */
            return (!jvmMainSetting.isSet()) && super.matches(element);
        }
    }

    /**
     * Command line element(s) with which the user specified the client to be run. Note that once "-jar xxx" is specified
     * then all subsequent arguments are passed to the client as arguments. Once "-client xxx" is specified then subsequent
     * arguments are treated as ACC options (if they match) or arguments to the client.
     */
    private class JVMMainOption extends CommandLineElement {
        private static final String JVM_MAIN_PATTERN = "-jar|-client|[^-][^\\s]*";

        private String introducer;

        JVMMainOption() {
            super(JVM_MAIN_PATTERN);
        }

        boolean isJarSetting() {
            return "-jar".equals(introducer);
        }

        boolean isClientSetting() {
            return "-client".equals(introducer);
        }

        boolean isSet() {
            return !values.isEmpty();
        }

        @Override
        boolean matches(String element) {
            /*
             * For backward compatibility, the -client element can appear multiple times with the last appearance overriding earlier
             * ones.
             */
            return ((!isSet()) || ((isClientSetting() && element.equals("-client")))) && super.matches(element);
        }

        @Override
        int processValue(String[] args, int slot) throws Error {
            // We only care about the most recent setting.
            values.clear();

            // If arg[slot] is -jar or -client we expect the next value to be the file.
            // Make sure there is a next item and that it
            if (args[slot].charAt(0) != '-') {
                values.add("-classpath");
                values.add(gfInfo.agentJarPath() + File.pathSeparatorChar
                    + ClassPathUtils.getClassPathForGfClient("."));
                final int result = super.processValue(args, slot);
                String className = values.get(values.size() - 1);
                agentArgs.add("client=class=" + className);
                return result;
            }
            if (!nextLooksOK(args, slot)) {
                throw new Error("-jar or -client requires value but missing");
            }
            introducer = args[slot++];
            final int result = super.processValue(args, slot);
            final String clientJarPath = values.remove(values.size() - 1);
            final File clientJarFile = new File(clientJarPath);
            if (clientJarFile.isDirectory()) {
                // Record in the agent args that the user is launching a directory.
                // Set the main class launch info to launch the ACC JAR.
                agentArgs.add("client=dir=" + quote(clientJarFile.getAbsolutePath()));
                introducer = "-jar";
                values.add(gfInfo.agentJarPath());
            } else {
                agentArgs.add("client=jar=" + quote(clientJarPath));
                // The client path is not a directory. It should be a .jar or a .ear file.
                // If an EAR, then we want Java to launch our ACC jar.
                // If a JAR, then we will launch that JAR.
                if (clientJarPath.endsWith(".ear")) {
                    introducer = "-jar";
                    values.add(gfInfo.agentJarPath());
                } else if (clientJarPath.endsWith(".jar")) {
                    introducer = null;
                    values.add("-classpath");
                    values.add(gfInfo.agentJarPath() + File.pathSeparatorChar
                        + ClassPathUtils.getClassPathForGfClient(clientJarPath));
                    String mainClass = ClassPathUtils.getMainClass(clientJarFile);
                    values.add(mainClass == null ? "" : mainClass);
                } else {
                    throw new Error("Unexpected client: " + clientJarPath);
                }
            }
            return result;
        }


        @Override
        boolean format(final StringBuilder commandLine) {
            if (introducer == null) {
                return super.format(commandLine, false /* useQuotes */);
            }
            // In the generated command we always use "-jar" to indicate the JAR to be launched,
            // even if the user specified "-client" on the appclient command line.
            super.format(commandLine, false /* useQuotes */, introducer);
            return super.format(commandLine, true /* useQuotes */);
        }

        private boolean nextLooksOK(final String[] args, final int slot) {
            return (isNextArg(args, slot) && (args[slot + 1].charAt(0) != '-'));
        }
    }

    private boolean isHelp() {
        return accUnvaluedOptions.values.contains("-help");
    }

    private boolean isUsage() {
        return accUnvaluedOptions.values.contains("-usage");
    }

    private String agentOptionsFromFile() {
        try {
            return '=' + FILE_OPTIONS_INTRODUCER + quote(fileContainingAgentArgs().getAbsolutePath());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private File fileContainingAgentArgs() throws IOException {
        File argsFile = File.createTempFile("acc", ".dat");
        try (PrintStream ps = new PrintStream(argsFile)) {
            ps.println(agentArgs.toString());
        }
        return argsFile;
    }

    /**
     * Encapsulates information about the GlassFish installation, mostly useful directories within the installation.
     * <p>
     * Note that we use the property acc.AS_INSTALL to find the installation.
     */
    static class GlassFishInfo {

        private final File home;
        private final File modules;
        private final File lib;
        private final File libAppclient;
        private static final String ACC_CONFIG_PREFIX = "domains/domain1/config";

        GlassFishInfo() {
            String asInstallPath = System.getProperty(ENV_VAR_PROP_PREFIX + "AS_INSTALL");
            if (asInstallPath == null || asInstallPath.isEmpty()) {
                throw new IllegalArgumentException("AS_INSTALL == null");
            }
            this.home = new File(asInstallPath).toPath().normalize().toFile();
            this.modules = new File(home, "modules");
            this.lib = new File(home, "lib");
            this.libAppclient = new File(lib, "appclient");
        }

        File home() {
            return home;
        }

        File modules() {
            return modules;
        }

        File lib() {
            return lib;
        }

        File libAppclient() {
            return libAppclient;
        }

        File configxml() {
            /*
             * Try using glassfish-acc.xml. If that does not exist then the user might have done an in-place upgrade from an earlier
             * version that used sun-acc.xml.
             */
            File configXMLFile = new File(new File(home, ACC_CONFIG_PREFIX), "glassfish-acc.xml");
            if (configXMLFile.canRead()) {
                return configXMLFile;
            }

            File sunACCXMLFile = new File(new File(home, ACC_CONFIG_PREFIX), "sun-acc.xml");
            if (sunACCXMLFile.canRead()) {
                return sunACCXMLFile;
            }
            /*
             * We found neither, but when an error is reported we want it to report the glassfish-acc.xml file is missing.
             */
            return configXMLFile;
        }

        String extPaths() {
            return new File(lib, "ext").getAbsolutePath();
        }

        /**
         * @return gf-client.jar path
         */
        String agentJarPath() {
            return new File(lib, "gf-client.jar").getAbsolutePath();
        }

        File securityPolicy() {
            return new File(libAppclient, "client.policy");
        }

        URL loginConfig() {
            try {
                return new File(libAppclient, "appclientlogin.conf").toURI().toURL();
            } catch (MalformedURLException e) {
                throw new IllegalStateException("Could not create URL for appclientlogin.conf", e);
            }
        }
    }


    /**
     * Collects information about the current Java implementation.
     * <p>
     * The user might have defined AS_JAVA or JAVA_HOME, or simply relied on the current PATH setting to choose which Java
     * to use. Regardless, once this code is running SOME Java has been successfully chosen. Use the java.home property to
     * find the JRE's home, which we need for the library directory (for example).
     */
    static class JavaInfo {

        private final static String SHELL_PROP_NAME = "org.glassfish.appclient.shell";

        /*
         * The appclient and appclient.bat scripts set JAVA. Properties would be nicer instead of env vars, but the Windows
         * script handling of command line args in the for statement treats the = in -Dprop=value as an argument separator and
         * breaks the property assignment apart into two arguments.
         */
        private final static String ACCJava_ENV_VAR_NAME = "JAVA";

        private final boolean useWindowsSyntax = File.separatorChar == '\\' && (System.getProperty(SHELL_PROP_NAME) == null);

        protected String javaExe;
        protected File jreHome;

        private JavaInfo() {
            init();
        }

        private void init() {
            jreHome = new File(System.getProperty(JAVA_HOME.getSystemPropertyName()));
            javaExe = javaExe();
        }

        protected boolean isValid() {
            return javaExe != null && new File(javaExe).canExecute();
        }

        protected File javaBinDir() {
            return new File(jreHome, "bin");
        }

        String javaExe() {
            return System.getenv(ACCJava_ENV_VAR_NAME);
        }

        File ext() {
            return new File(lib(), "ext");
        }

        File lib() {
            return new File(jreHome, "lib");
        }

        String pathSeparator() {
            return useWindowsSyntax ? ";" : ":";
        }
    }

    /**
     * Handles user-specified VM arguments passed by the environment variable VMARGS.
     *
     * <p>
     * This is very much like the handling of the arguments on the more general command line, except that we expect only
     * valid VM arguments here.
     *
     * <p>
     * Some of the "main" CommandLineElements processed earlier in the class will use the inner command line elements here
     * to augment the values they process.
     */
    class UserVMArgs {

        private CommandLineElement evJVMPropertySettings;
        private CommandLineElement evJVMValuedOptions;
        private CommandLineElement evOtherJVMOptions;

        private final List<CommandLineElement> evElements = new ArrayList<>();

        UserVMArgs(String vmargs) throws Error {

            if (isDebug) {
                System.err.println("VMARGS = " + (vmargs == null ? "null" : vmargs));
            }

            evJVMPropertySettings = new JVMOption("-D.*", null);
            evJVMValuedOptions = new JVMValuedOption("-classpath|-cp", null);
            evOtherJVMOptions = new JVMOption("-.*", null);

            initEVCommandLineElements();

            if (vmargs == null) {
                return;
            }

            processEVCommandLineElements(convertInputArgsVariable(vmargs));
        }

        private void initEVCommandLineElements() {
            evElements.add(evJVMPropertySettings);
            evElements.add(evJVMValuedOptions);
            evElements.add(evOtherJVMOptions);
        }

        private void processEVCommandLineElements(final String[] envVarJVMArgs) throws Error {
            /*
             * Process each command-line argument by the first CommandLineElement which matches the argument.
             */
            for (int i = 0; i < envVarJVMArgs.length;) {
                boolean isMatched = false;
                for (CommandLineElement cle : evElements) {
                    isMatched = cle.matches(envVarJVMArgs[i]);
                    if (isMatched) {
                        i = cle.processValue(envVarJVMArgs, i);
                        break;
                    }
                }
                if (!isMatched) {
                    throw new Error("arg " + i + " = " + envVarJVMArgs[i] + " not recognized");
                }
            }
        }
    }
}
