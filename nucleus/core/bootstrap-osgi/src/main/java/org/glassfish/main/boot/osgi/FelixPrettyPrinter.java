/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.boot.osgi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.Version;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.resource.Capability;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.osgi.framework.namespace.PackageNamespace.CAPABILITY_VERSION_ATTRIBUTE;
import static org.osgi.framework.namespace.PackageNamespace.PACKAGE_NAMESPACE;

/**
 * Tools for obtaining readable information from the {@link BundleException} thrown by the Felix resolver.
 * <p>
 * The message format of the resolver is not specified anywhere, so everything in this class is best effort. The one hard rule it follows is
 * that it never destroys information: when a message cannot be understood completely, the original text is appended to the output instead of
 * being silently dropped.
 * <p>
 * For a diagnosis that does not depend on the message format at all, see {@code BundleResolutionAnalyzer}, which asks the resolver model
 * directly instead of reading its prose.
 */
public final class FelixPrettyPrinter {

    private static final Pattern BUNDLE_ID_PATTERN = Pattern.compile("\\[(\\d+)\\]");

    /** Comparison operators of an LDAP filter, longest first so that {@code >=} wins over {@code =}. */
    private static final Pattern FILTER_OPERATOR_PATTERN = Pattern.compile(">=|<=|~=|=");

    private static final String UNABLE_TO_RESOLVE = "Unable to resolve";
    private static final String MISSING_REQUIREMENT = "missing requirement";
    private static final String CAUSED_BY = "caused by:";
    private static final String REVISION_MARKER = "(R ";

    /** Dropped when rendering a filter, so that osgi.wiring.package reads as package and osgi.wiring.host as host. */
    private static final String WIRING_NAMESPACE_PREFIX = "osgi.wiring.";

    private static final String ORIGINAL_MESSAGE_MARKER = "--- original message, not fully understood by the pretty printer ---";

    private static final int INDENT_WIDTH = 4;

    /** A shaded bundle can carry dozens of Maven descriptors, which would drown the actual error. */
    private static final int MAX_POM_PROPERTIES_PER_BUNDLE = 4;

    public static void main(String[] args) {
        System.out.println(prettyPrintExceptionMessage("Unable to resolve org.glassfish.main.hk2.config-types [138](R 138.0): missing requirement [org.glassfish.main.hk2.config-types [138](R 138.0)] osgi.wiring.package; (&(osgi.wiring.package=org.jvnet.hk2.config)(version>=9.0.0)(!(version>=10.0.0))) [caused by: Unable to resolve org.glassfish.main.hk2-config-generator [159](R 159.0): missing requirement [org.glassfish.main.hk2-config-generator [159](R 159.0)] osgi.wiring.package; (&(osgi.wiring.package=org.hibernate.validator)(version>=9.1.0)(!(version>=10.0.0))) [caused by: Unable to resolve org.hibernate.validator [262](R 262.0): missing requirement [org.hibernate.validator [262](R 262.0)] osgi.wiring.package; (&(osgi.wiring.package=jakarta.validation)(version>=3.1.1)(!(version>=4.0.0)))]] Unresolved requirements: [[org.glassfish.main.hk2.config-types [138](R 138.0)] osgi.wiring.package; (&(osgi.wiring.package=org.jvnet.hk2.config)(version>=9.0.0)(!(version>=10.0.0)))]"));
    }

    private FelixPrettyPrinter() {
    }

    /**
     * Renders a resolver message and appends everything known about the bundles it mentions, plus the bundles that export the packages that
     * could not be wired.
     *
     * @param bundleContext The context used to resolve bundle ids and to look for exporters. Must not be null.
     * @param bundleMessage The raw message of the {@link BundleException}. May be null or empty, in which case it is returned unchanged.
     * @return A multiline, human readable rendering of the message.
     */
    public static String prettyPrintFelixMessage(BundleContext bundleContext, String bundleMessage) {
        if (bundleMessage == null || bundleMessage.isEmpty()) {
            return bundleMessage;
        }

        try {
            List<ResolutionFailure> failures = parseFailures(bundleMessage);

            StringBuilder messageBuilder = new StringBuilder(1024);
            messageBuilder.append(format(failures, bundleMessage));

            Set<Long> bundleIds = new LinkedHashSet<>();
            bundleIds.addAll(appendExporters(bundleContext, failures, messageBuilder));
            bundleIds.addAll(findBundleIds(messageBuilder.toString()));

            for (Long bundleId : bundleIds) {
                Bundle bundle = bundleContext.getBundle(bundleId);
                if (bundle != null) {
                    appendBundleInfo(bundle, messageBuilder);
                }
            }

            return messageBuilder.toString();
        } catch (RuntimeException e) {
            // We are usually formatting another failure already - never turn that into a second one.
            return bundleMessage;
        }
    }

    /**
     * Prints exception messages from Felix bundle classloading in a more human readable way.
     *
     * @param message The error message from the exception. May be null or empty, in which case it is returned unchanged.
     * @return A multiline human readable string, ending with the original message when the parse was incomplete.
     */
    public static String prettyPrintExceptionMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        try {
            return format(parseFailures(message), message);
        } catch (RuntimeException e) {
            // Usually we are processing another exception - if we failed, better return the original.
            return message;
        }
    }

    /**
     * Appends the location and the Maven coordinates of a single bundle to an already rendered message.
     *
     * @param bundle The bundle to describe. May be null, in which case only the message is returned.
     * @param prettyMessage The already rendered message.
     * @return The message with the bundle information appended.
     */
    public static String addBundleInfo(Bundle bundle, String prettyMessage) {
        StringBuilder messageBuilder = new StringBuilder(1024);
        messageBuilder.append('\n').append(prettyMessage);

        if (bundle != null) {
            appendBundleInfo(bundle, messageBuilder);
        }

        return messageBuilder.toString();
    }

    /**
     * @param message The error message from the exception.
     * @return The distinct bundle ids found in the message, in the order they appear. They are the numbers in square brackets.
     */
    public static List<Long> findBundleIds(String message) {
        if (message == null || message.isEmpty()) {
            return List.of();
        }

        Set<Long> bundleIds = new LinkedHashSet<>();
        Matcher bundleMatcher = BUNDLE_ID_PATTERN.matcher(message);
        while (bundleMatcher.find()) {
            bundleIds.add(Long.valueOf(bundleMatcher.group(1)));
        }

        return new ArrayList<>(bundleIds);
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Parsing
    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * A single "Unable to resolve" clause. The namespace and the filter are null when that part of the clause could not be understood, which
     * is what drives the decision to append the original message.
     */
    private record ResolutionFailure(String module, String namespace, String filter, boolean isCause) {

        boolean isComplete() {
            return namespace != null && filter != null;
        }
    }

    /**
     * Splits the message into one clause per "Unable to resolve". Every clause is parsed strictly within its own bounds, so a clause can
     * never borrow the requirement of the clause nesting below it.
     */
    private static List<ResolutionFailure> parseFailures(String message) {
        List<ResolutionFailure> failures = new ArrayList<>();

        int previousIndex = -1;
        int index = message.indexOf(UNABLE_TO_RESOLVE);
        while (index >= 0) {
            int clauseStart = index + UNABLE_TO_RESOLVE.length();

            int clauseEnd = message.indexOf(UNABLE_TO_RESOLVE, clauseStart);
            if (clauseEnd < 0) {
                clauseEnd = message.length();
            }

            // Only nest a clause when the resolver said it is a cause. Sibling clauses stay at the level of the one before them.
            boolean isCause = previousIndex >= 0 && message.lastIndexOf(CAUSED_BY, index) > previousIndex;
            failures.add(parseFailure(message, clauseStart, clauseEnd, isCause));

            previousIndex = index;
            index = message.indexOf(UNABLE_TO_RESOLVE, clauseStart);
        }

        return failures;
    }

    /**
     * Parses one clause, which looks like this:
     *
     * <pre>
     * org.glassfish.batch-connector [103](R 103.0): missing requirement [org.glassfish.batch-connector [103](R 103.0)] osgi.wiring.package;
     *     (&amp;(osgi.wiring.package=jakarta.batch)(version&gt;=2.1.0)(!(version&gt;=3.0.0)))
     * </pre>
     */
    private static ResolutionFailure parseFailure(String message, int start, int end, boolean isCause) {
        int requirementIndex = message.indexOf(MISSING_REQUIREMENT, start);
        if (requirementIndex < 0 || requirementIndex >= end) {
            return new ResolutionFailure(cleanModule(message.substring(start, end)), null, null, isCause);
        }

        String module = cleanModule(message.substring(start, requirementIndex));

        // Skip the revision reference that repeats the module, for example "[org.glassfish.batch-connector [103](R 103.0)]".
        int namespaceStart = skipRevisionReference(message, requirementIndex + MISSING_REQUIREMENT.length(), end);

        int namespaceEnd = message.indexOf(';', namespaceStart);
        if (namespaceEnd < 0 || namespaceEnd >= end) {
            return new ResolutionFailure(module, null, null, isCause);
        }

        // Any namespace is accepted here: osgi.wiring.package, osgi.wiring.host, osgi.wiring.bundle, osgi.ee, osgi.native, ...
        String namespace = message.substring(namespaceStart, namespaceEnd).trim();

        return new ResolutionFailure(module, namespace, extractFilter(message, namespaceEnd + 1, end), isCause);
    }

    private static int skipRevisionReference(String message, int start, int end) {
        int index = start;
        while (index < end && Character.isWhitespace(message.charAt(index))) {
            index++;
        }

        if (index >= end || message.charAt(index) != '[') {
            return index;
        }

        // The reference nests, so counting is the only way to find its end.
        int depth = 0;
        while (index < end) {
            char character = message.charAt(index);
            if (character == '[') {
                depth++;
            } else if (character == ']') {
                depth--;
                if (depth == 0) {
                    return index + 1;
                }
            }
            index++;
        }

        return start;
    }

    /**
     * Extracts a complete LDAP filter by balancing parentheses. Splitting on the first space, as we used to do, breaks on any filter that
     * contains one and on any filter that ends the message.
     */
    private static String extractFilter(String message, int start, int end) {
        int open = start;
        while (open < end && message.charAt(open) != '(') {
            open++;
        }

        if (open >= end) {
            return null;
        }

        int depth = 0;
        for (int index = open; index < end; index++) {
            char character = message.charAt(index);
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
                if (depth == 0) {
                    return message.substring(open, index + 1);
                }
            }
        }

        return null;
    }

    /** Turns "org.glassfish.batch-connector [103](R 103.0): " into "org.glassfish.batch-connector [103]". */
    private static String cleanModule(String module) {
        String name = module.trim();

        int revisionIndex = name.indexOf(REVISION_MARKER);
        if (revisionIndex >= 0) {
            name = name.substring(0, revisionIndex).trim();
        }

        if (name.endsWith(":")) {
            name = name.substring(0, name.length() - 1).trim();
        }

        return name;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Rendering
    // -----------------------------------------------------------------------------------------------------------------------------------

    private static String format(List<ResolutionFailure> failures, String message) {
        if (failures.isEmpty()) {
            return message;
        }

        StringBuilder messageBuilder = new StringBuilder(256);

        // Anything the framework put in front of the first clause is context we do not want to lose.
        String prefix = message.substring(0, message.indexOf(UNABLE_TO_RESOLVE)).trim();
        if (!prefix.isEmpty()) {
            printLine(messageBuilder, 0, prefix);
        }

        int indent = 0;
        for (ResolutionFailure failure : failures) {

            // A cause belongs under the requirement that could not be met, which sits two levels below its own "Unable to resolve".
            if (failure.isCause()) {
                printLine(messageBuilder, indent + 2, CAUSED_BY);
                indent += 3;
            }

            printLine(messageBuilder, indent, UNABLE_TO_RESOLVE);
            printLine(messageBuilder, indent + 1, failure.module());

            if (failure.namespace() != null) {
                // The namespace is not repeated here, the filter below asserts on it and reads better for it.
                printLine(messageBuilder, indent + 1, MISSING_REQUIREMENT);
            }

            if (failure.filter() != null) {
                printLine(messageBuilder, indent + 2, formatFilter(failure.filter()));
            }
        }

        if (!failures.stream().allMatch(ResolutionFailure::isComplete)) {
            messageBuilder.append('\n').append(ORIGINAL_MESSAGE_MARKER).append('\n').append(message).append('\n');
        }

        return messageBuilder.toString();
    }

    /**
     * Renders an LDAP filter the way it is normally read. The resolver prints prefix notation, so
     * "(&(osgi.wiring.package=jakarta.batch)(version&gt;=2.1.0)(!(version&gt;=3.0.0)))" comes in as one dense line and goes out as
     * "package = jakarta.batch &amp; version &gt;= 2.1.0 &amp; !(version &gt;= 3.0.0)".
     *
     * Package private so that {@code BundleResolutionAnalyzer} renders the filter it reads from a requirement directive the same way,
     * rather than the two reports each inventing their own notation for the same thing.
     *
     * @param filter The raw filter as the resolver printed it.
     * @return The filter in infix notation, or the filter merely spaced out when it does not follow the grammar.
     */
    static String formatFilter(String filter) {
        String infix = new FilterParser(filter).parse();
        if (infix != null) {
            return infix;
        }

        // Not a filter we recognise. Show it spaced out rather than not at all.
        return spaceOutFilter(filter);
    }

    private static String spaceOutFilter(String filter) {
        String formatted = FILTER_OPERATOR_PATTERN.matcher(filter).replaceAll(" $0 ");
        formatted = formatted.replace(")(", ") (");

        return stripOuterParentheses(formatted).trim();
    }

    /**
     * Shortens a namespace attribute, so that osgi.wiring.package reads as package and osgi.wiring.host as host. Namespaces without the
     * wiring prefix, osgi.ee for instance, are left alone and stay recognisable.
     */
    private static String shortenNamespace(String namespace) {
        if (namespace.startsWith(WIRING_NAMESPACE_PREFIX)) {
            return namespace.substring(WIRING_NAMESPACE_PREFIX.length());
        }

        return namespace;
    }

    private static String stripOuterParentheses(String text) {
        if (text.length() < 2 || text.charAt(0) != '(' || text.charAt(text.length() - 1) != ')') {
            return text;
        }

        // Only strip when the first parenthesis is the one the last parenthesis closes, so that "(a) (b)" keeps both pairs and an
        // unbalanced filter is handed back untouched.
        int depth = 0;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (character == '(') {
                depth++;
            } else if (character == ')') {
                depth--;
                if (depth == 0 && index < text.length() - 1) {
                    return text;
                }
            }
        }

        if (depth != 0) {
            return text;
        }

        return text.substring(1, text.length() - 1);
    }

    /**
     * Recursive descent parser for the LDAP filter grammar of RFC 1960, rendering the result in infix notation.
     * <p>
     * The grammar is small. A filter is parenthesised and holds either a composite - "&amp;", "|" or "!" followed by nested filters - or a
     * single item of the form attribute, operator, value. Rendering it infix means the operator ends up between its operands, and the
     * parentheses that only carried the prefix grouping disappear.
     * <p>
     * Every method returns null on anything the grammar does not allow, which the caller turns into the untouched filter. Nothing here ever
     * throws, because it runs while another failure is already being reported.
     */
    private static final class FilterParser {

        /**
         * A parsed subexpression. Composite subexpressions are the ones that need their grouping shown when they are nested inside another
         * operator; a single item never does.
         */
        private record Expression(String text, boolean composite) {

            String grouped() {
                return composite ? "(" + text + ")" : text;
            }
        }

        private final String filter;

        private int position;

        FilterParser(String filter) {
            this.filter = filter;
        }

        /**
         * @return The whole filter in infix notation, or null when it is not a filter this parser understands.
         */
        String parse() {
            if (filter == null || filter.isEmpty()) {
                return null;
            }

            Expression expression = parseFilter();
            if (expression == null) {
                return null;
            }

            skipWhitespace();
            if (position != filter.length()) {
                return null;
            }

            return expression.text();
        }

        private Expression parseFilter() {
            skipWhitespace();
            if (position >= filter.length() || filter.charAt(position) != '(') {
                return null;
            }
            position++;

            skipWhitespace();
            if (position >= filter.length()) {
                return null;
            }

            Expression expression = switch (filter.charAt(position)) {
                case '&', '|' -> parseComposite(filter.charAt(position));
                case '!' -> parseNegation();
                default -> parseItem();
            };

            if (expression == null) {
                return null;
            }

            skipWhitespace();
            if (position >= filter.length() || filter.charAt(position) != ')') {
                return null;
            }
            position++;

            return expression;
        }

        private Expression parseComposite(char operator) {
            position++;

            StringBuilder expressionBuilder = new StringBuilder(64);
            int operands = 0;

            skipWhitespace();
            while (position < filter.length() && filter.charAt(position) == '(') {
                Expression operand = parseFilter();
                if (operand == null) {
                    return null;
                }

                if (operands > 0) {
                    expressionBuilder.append(' ').append(operator).append(' ');
                }
                expressionBuilder.append(operand.grouped());
                operands++;

                skipWhitespace();
            }

            if (operands == 0) {
                return null;
            }

            // A single operand needs no grouping of its own, whatever the operator in front of it was.
            return new Expression(expressionBuilder.toString(), operands > 1);
        }

        private Expression parseNegation() {
            position++;

            Expression operand = parseFilter();
            if (operand == null) {
                return null;
            }

            return new Expression("!(" + operand.text() + ")", false);
        }

        private Expression parseItem() {
            int attributeStart = position;
            while (position < filter.length() && isAttributeCharacter(filter.charAt(position))) {
                position++;
            }

            String attribute = filter.substring(attributeStart, position).trim();
            if (attribute.isEmpty()) {
                return null;
            }

            String operator = readOperator();
            if (operator == null) {
                return null;
            }

            return new Expression(shortenNamespace(attribute) + " " + operator + " " + readValue(), false);
        }

        private String readOperator() {
            if (position >= filter.length()) {
                return null;
            }

            char character = filter.charAt(position);
            if (character == '=') {
                position++;
                return "=";
            }

            if (position + 1 >= filter.length() || filter.charAt(position + 1) != '=') {
                return null;
            }

            position += 2;

            return character + "=";
        }

        /**
         * Reads a value up to the closing parenthesis of its item. A parenthesis inside a value has to be escaped, so a backslash always
         * takes the character behind it along.
         */
        private String readValue() {
            StringBuilder valueBuilder = new StringBuilder(32);

            while (position < filter.length() && filter.charAt(position) != ')') {
                char character = filter.charAt(position);

                if (character == '\\' && position + 1 < filter.length()) {
                    valueBuilder.append(character).append(filter.charAt(position + 1));
                    position += 2;
                    continue;
                }

                valueBuilder.append(character);
                position++;
            }

            return valueBuilder.toString().trim();
        }

        private boolean isAttributeCharacter(char character) {
            return character != '=' && character != '>' && character != '<' && character != '~'
                    && character != '(' && character != ')';
        }

        private void skipWhitespace() {
            while (position < filter.length() && Character.isWhitespace(filter.charAt(position))) {
                position++;
            }
        }
    }

    private static void printLine(StringBuilder messageBuilder, int indent, String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        messageBuilder.append(" ".repeat(indent * INDENT_WIDTH)).append(text.trim()).append('\n');
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Bundle information
    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Reports, for every distinct package that could not be wired, which bundles export it, at which version, and whether that export
     * actually satisfies the requirement. The package name and the filter come from the parse, not from re-reading our own output.
     *
     * @return The ids of the exporting bundles, so the caller can describe them once.
     */
    private static Set<Long> appendExporters(BundleContext bundleContext, List<ResolutionFailure> failures, StringBuilder messageBuilder) {
        Set<Long> bundleIds = new LinkedHashSet<>();
        Set<String> reportedPackages = new LinkedHashSet<>();

        for (ResolutionFailure failure : failures) {
            if (!PACKAGE_NAMESPACE.equals(failure.namespace()) || failure.filter() == null) {
                continue;
            }

            String packageName = findRequiredPackage(failure.filter());
            if (packageName == null || !reportedPackages.add(packageName)) {
                continue;
            }

            List<PackageExport> exports = findExporters(bundleContext, packageName, createFilter(failure.filter()));
            if (exports.isEmpty()) {
                messageBuilder.append("\nNo bundle exports \"").append(packageName).append("\"\n");
                continue;
            }

            messageBuilder.append("\nThe following bundles export \"").append(packageName).append("\"\n");
            for (PackageExport export : exports) {
                bundleIds.add(export.bundle().getBundleId());
                appendExport(export, packageName, messageBuilder);
            }
        }

        return bundleIds;
    }

    private static void appendExport(PackageExport export, String packageName, StringBuilder messageBuilder) {
        messageBuilder.append("  ")
                      .append(export.match().label())
                      .append(' ')
                      .append(export.bundle().getSymbolicName())
                      .append(' ')
                      .append(export.bundle().getVersion())
                      .append(" [")
                      .append(export.bundle().getBundleId())
                      .append("] ")
                      .append(toStateName(export.bundle()))
                      .append(" - exports ")
                      .append(packageName)
                      // The bundle version above and the exported package version below are routinely different.
                      .append(" at version ")
                      .append(export.packageVersion())
                      .append('\n');
    }

    private static void appendBundleInfo(Bundle bundle, StringBuilder messageBuilder) {
        messageBuilder.append('[').append(bundle.getBundleId()).append("]\n");
        messageBuilder.append("jar = ").append(bundle.getLocation());
        appendPomProperties(bundle, messageBuilder);
        messageBuilder.append('\n');
    }

    private static void appendPomProperties(Bundle bundle, StringBuilder messageBuilder) {
        // Note: findEntries is specified to attempt to resolve the bundle so that fragment entries can be searched. We are calling it while
        // reporting a resolution failure, which is why it is the last thing we do and why every failure below is swallowed.
        Enumeration<URL> entries = bundle.findEntries("META-INF/maven/", "pom.properties", true);
        if (entries == null) {
            return;
        }

        int printed = 0;
        while (entries.hasMoreElements() && printed < MAX_POM_PROPERTIES_PER_BUNDLE) {
            URL entry = entries.nextElement();
            printed++;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(entry.openStream(), UTF_8))) {
                reader.lines()
                      .map(String::trim)
                      .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                      .forEach(line -> messageBuilder.append('\n').append(line.replace("=", " = ")));
            } catch (IOException e) {
                // The content is unreadable, the location printed above is all we can offer.
            }
        }

        if (entries.hasMoreElements()) {
            messageBuilder.append("\n... further Maven coordinates omitted");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------------------------
    // Exporter lookup
    // -----------------------------------------------------------------------------------------------------------------------------------

    private enum RequirementMatch {

        MATCHES("[matches] "),
        MISMATCH("[mismatch]"),
        UNKNOWN("[unknown] ");

        private final String label;

        RequirementMatch(String label) {
            this.label = label;
        }

        String label() {
            return label;
        }
    }

    private record PackageExport(Bundle bundle, Version packageVersion, RequirementMatch match) {
    }

    /**
     * Finds every bundle that declares an export of the package, resolved or not, and lets the framework itself decide whether that export
     * satisfies the requirement. Declared capabilities are used on purpose: a bundle that cannot be resolved is exactly the one you want to
     * see in this list.
     */
    private static List<PackageExport> findExporters(BundleContext bundleContext, String packageName, Filter requirementFilter) {
        List<PackageExport> exports = new ArrayList<>();

        for (Bundle bundle : bundleContext.getBundles()) {
            BundleRevision revision = bundle.adapt(BundleRevision.class);
            if (revision == null) {
                continue;
            }

            for (Capability capability : revision.getCapabilities(PACKAGE_NAMESPACE)) {
                Map<String, Object> attributes = capability.getAttributes();
                if (!packageName.equals(attributes.get(PACKAGE_NAMESPACE))) {
                    continue;
                }

                exports.add(new PackageExport(bundle, toVersion(attributes.get(CAPABILITY_VERSION_ATTRIBUTE)),
                        toMatch(requirementFilter, attributes)));
                break;
            }
        }

        return exports;
    }

    private static RequirementMatch toMatch(Filter requirementFilter, Map<String, Object> attributes) {
        if (requirementFilter == null) {
            return RequirementMatch.UNKNOWN;
        }

        return requirementFilter.matches(attributes) ? RequirementMatch.MATCHES : RequirementMatch.MISMATCH;
    }

    private static Filter createFilter(String filter) {
        try {
            return FrameworkUtil.createFilter(filter);
        } catch (InvalidSyntaxException e) {
            // We did not understand the filter the way the framework does - report the exporters without a verdict.
            return null;
        }
    }

    /**
     * Reads the required package name out of the filter. This is bounded to the filter, which the parser extracted by balancing
     * parentheses, so it can no longer run past the end of a line and swallow the rest of the message.
     */
    private static String findRequiredPackage(String filter) {
        String assertion = PACKAGE_NAMESPACE + "=";

        int index = filter.indexOf(assertion);
        if (index < 0) {
            return null;
        }

        int start = index + assertion.length();
        int end = start;
        while (end < filter.length() && filter.charAt(end) != '(' && filter.charAt(end) != ')') {
            end++;
        }

        String packageName = filter.substring(start, end).trim();

        return packageName.isEmpty() ? null : packageName;
    }

    private static Version toVersion(Object value) {
        if (value instanceof Version version) {
            return version;
        }

        if (value == null) {
            return Version.emptyVersion;
        }

        try {
            return Version.parseVersion(value.toString());
        } catch (IllegalArgumentException e) {
            return Version.emptyVersion;
        }
    }

    private static String toStateName(Bundle bundle) {
        return switch (bundle.getState()) {
            case Bundle.UNINSTALLED -> "UNINSTALLED";
            case Bundle.INSTALLED -> "INSTALLED";
            case Bundle.RESOLVED -> "RESOLVED";
            case Bundle.STARTING -> "STARTING";
            case Bundle.STOPPING -> "STOPPING";
            case Bundle.ACTIVE -> "ACTIVE";
            default -> "UNKNOWN";
        };
    }
}