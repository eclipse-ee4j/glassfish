/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.versioning;

import com.sun.enterprise.config.serverbeans.Application;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class provides utility methods to handle application names
 * in the versioning context
 *
 * @author Romain GRECOURT - SERLI (romain.grecourt@serli.com)
 */
public class VersioningUtils {

    public static final String EXPRESSION_SEPARATOR = ":";
    public static final String EXPRESSION_WILDCARD = "*";
    public static final String REPOSITORY_DASH = "~";

    /**
     * Extract the untagged name for a given application name that complies
     * with versioning rules (version identifier / expression) or not.
     *
     * If the application name is using versioning rules, the method will split
     * the application names with the colon character and retrieve the
     * untagged name from the first token.
     *
     * Else the given application name is an untagged name.
     *
     * @param appName the application name
     * @return the untagged version name
     * @throws VersioningSyntaxException if the given application name had some
     * critical patterns.
     */
    public static final String getUntaggedName(String appName)
            throws VersioningSyntaxException {

        if(appName != null && !appName.isEmpty()){
            int colonIndex = appName.indexOf(EXPRESSION_SEPARATOR);
            // if the appname contains a EXPRESSION_SEPARATOR
            if (colonIndex >= 0){
                if (colonIndex == 0) {
                    // if appName is starting with a colon
                    throw new VersioningSyntaxException(
                            MessageFormat.format(
                            "excepted application name before colon: {0}",
                            appName));
                } else if (colonIndex == (appName.length() - 1)) {
                    // if appName is ending with a colon
                    throw new VersioningSyntaxException(
                        MessageFormat.format(
                            "excepted version identifier after colon: {0}",
                            appName));
                }
                // versioned
                return appName.substring(0, colonIndex);
            }
        }
        // not versioned
        return appName;
    }

    /**
     * Extract the version identifier / expression for a given application name
     * that complies with versioning rules.
     *
     * The method splits the application name with the colon character
     * and retrieve the 2nd token.
     *
     * @param appName the application name
     * @return the version identifier / expression extracted from application name
     * @throws VersioningSyntaxException if the given application name had some
     * critical patterns.
     */
    public static final String getExpression(String appName)
            throws VersioningSyntaxException {

        if(appName != null && !appName.isEmpty()) {
            int colonIndex = appName.indexOf(EXPRESSION_SEPARATOR);
            // if the appname contains a EXPRESSION_SEPARATOR
            if (colonIndex >= 0){
                if (colonIndex == 0) {
                    // if appName is starting with a colon
                    throw new VersioningSyntaxException(
                        MessageFormat.format(
                            "excepted application name before colon: {0}",
                            appName));
                } else if (colonIndex == (appName.length() - 1)) {
                    // if appName is ending with a colon
                    throw new VersioningSyntaxException(
                        MessageFormat.format(
                            "excepted version identifier after colon: {0}",
                            appName));
                }
                // versioned
                return appName.substring(colonIndex + 1, appName.length());
            }
        }
        // not versioned
        return null;
    }

    /**
     * Check a versionned application name.
     *
     * This method is used to provide consistant error messages for identifier
     * aware operations.
     *
     * @param appName the application name
     * @throws VersioningSyntaxException if the given application name had some
     * critical patterns.
     * @throws VersioningException if the given application name had some
     * wildcard character(s) in its identifier part.
     */
    public static final void checkIdentifier(String appName)
            throws VersioningException {

        String identifier = getExpression(appName);
        if (identifier != null && identifier.contains(EXPRESSION_WILDCARD)) {
            throw new VersioningWildcardException("Wildcard character(s) are not allowed in a version identifier.");
        }
    }

    /**
     * Extract the set of version(s) of the given application from a set of
     * applications. This method is used by unit tests.
     *
     * @param untaggedName the application name as an untagged version : an
     * application name without version identifier
     * @param allApplications the set of applications
     * @return all the version(s) of the given application in the given set of
     * applications
     */
    public static final List<String> getVersions(String untaggedName,
            List<Application> allApplications) {

        List<String> allVersions = new ArrayList<>();
        for (Application app : allApplications) {
            // if a tagged version or untagged version of the app
            if (app.getName().startsWith(untaggedName + EXPRESSION_SEPARATOR)
                    || app.getName().equals(untaggedName)) {
                allVersions.add(app.getName());
            }
        }
        return allVersions;
    }

    /**
     * Search for the version(s) matched by the expression contained in the given
     * application name. This method is used by unit tests.
     *
     * @param listVersion the set of all versions of the application
     * @param appName the application name containing the expression
     * @return the expression matched list
     * @throws VersioningException if the expression is an identifier matching
     * a version not registered, or if getExpression throws an exception
     */
    public static final List<String> matchExpression(List<String> listVersion, String appName)
            throws VersioningException {

        if (listVersion.size() == 0) {
            return Collections.EMPTY_LIST;
        }
        String expressionVersion = getExpression(appName);

        // if using an untagged version
        if (expressionVersion == null) {
            // return the matched version if exist
            if (listVersion.contains(appName)) {
                return listVersion.subList(listVersion.indexOf(appName),
                        listVersion.indexOf(appName) + 1);
            } else {
                throw new VersioningException(MessageFormat.format("version {0} not registered", appName));
            }
        }

        // if using an identifier
        if (expressionVersion.indexOf(EXPRESSION_WILDCARD) == -1) {
            // return the matched version if exist
            if (listVersion.contains(appName)) {
                return listVersion.subList(listVersion.indexOf(appName),
                        listVersion.indexOf(appName) + 1);
            } else {
                throw new VersioningException(MessageFormat.format("Version {0} not registered", appName));
            }
        }

        StringTokenizer st = new StringTokenizer(expressionVersion,
                EXPRESSION_WILDCARD);
        String lastToken = null;
        List<String> matchedVersions = new ArrayList<>(listVersion);

        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            for (String app : listVersion) {
                String identifier = getExpression(app);

                // get the position of the last token in the current identifier
                int lastTokenIndex = -1;
                if (lastToken != null) {
                    lastTokenIndex = identifier.indexOf(lastToken);
                }
                // matching expression on the current identifier
                if (identifier != null) {
                    if ( expressionVersion.startsWith(token)
                            && ! identifier.startsWith(token) ) {
                        matchedVersions.remove(app);
                    } else if ( expressionVersion.endsWith(token)
                            && !identifier.endsWith(token) ) {
                        matchedVersions.remove(app);
                    } else if ( !identifier.contains(token.subSequence(0, token.length() - 1))
                            || identifier.indexOf(token) <= lastTokenIndex ) {
                        matchedVersions.remove(app);
                    }
                } else {
                    matchedVersions.remove(app);
                }

            }
            lastToken = token;
        }
        // returns matched version(s)
        return matchedVersions;
    }

    /**
     * Replaces colons with dashs in the given application name.
     *
     * @param appName the application name
     * @return a valid repository name
     */
    public static final String getRepositoryName(String appName) {

        return appName.replace(EXPRESSION_SEPARATOR, REPOSITORY_DASH);
    }

    /**
     * Test if the given application name is an untagged name
     *
     * @param appName the application name
     * @return <code>true</code> is the given application name is not versioned
     * @throws VersioningSyntaxException if getUntaggedName
     * throws exception
     */
    public static final Boolean isUntagged(String appName)
            throws VersioningSyntaxException {

        Boolean isUntagged = false;
        String untaggedName = VersioningUtils.getUntaggedName(appName);
        if (untaggedName != null && untaggedName.equals(appName)) {
            isUntagged = true;
        }
        return isUntagged;
    }

    /**
     * Test if the given application name is a version expression
     *
     * @param appName the application name
     * @return <code>true</code> if the appName is a version expression
     * @throws VersioningSyntaxException if isUntaggedName
     * throws exception
     */
    public static final Boolean isVersionExpression(String appName)
            throws VersioningSyntaxException {

        Boolean isVersionExpression = false;
        if(appName != null){
            isVersionExpression = !isUntagged(appName);
        }
        return isVersionExpression;
    }

     /**
     * Test if the given application name is a version expression containing
     * any wildcard character. That is to say the version expression is matching
     * more than one version.
     *
     * @param appName the application name
     * @return <code>true</code> if the appName is a version expression matching
     * more than one version.
     * @throws VersioningSyntaxException if isVersionExpression
     * throws exception
     */
    public static final Boolean isVersionExpressionWithWildCard(String appName)
            throws VersioningSyntaxException {

        return isVersionExpression(appName)
                && appName.contains(EXPRESSION_WILDCARD);
    }

    /**
     * Test if the given application name is a version identifier.
     *
     * @param appName the application name
     * @return <code>true</code> if the appName is a version identifier
     * @throws VersioningSyntaxException if isVersionExpression
     * throws exception
     */
    public static final Boolean isVersionIdentifier(String appName)
            throws VersioningSyntaxException{

        return isVersionExpression(appName) &&
                !appName.contains(EXPRESSION_WILDCARD);
    }
}
