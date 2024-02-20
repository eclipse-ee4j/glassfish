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
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.io.File;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * This service provides methods to handle application names
 * in the versioning context
 *
 * @author Romain GRECOURT - SERLI (romain.grecourt@serli.com)
 */
@I18n("versioning.service")
@Service
@PerLookup
public class VersioningService {

    @Inject
    private CommandRunner commandRunner;
    @Inject
    private Domain domain;

    /**
     * Extract the set of version(s) of the given application represented as
     * an untagged version name
     *
     * @param untaggedName the application name as an untagged version : an
     * application name without version identifier
     * @param target the target where we want to get all the versions
     * @return all the version(s) of the given application
     */
    private final List<String> getAllversions(String untaggedName, String target) {
        List<Application> allApplications = null;
        if (target != null) {
            allApplications = domain.getApplicationsInTarget(target);
        } else {
            allApplications = domain.getApplications().getApplications();
        }
        return VersioningUtils.getVersions(untaggedName, allApplications);
    }

   /**
     * Search the enabled versions on the referenced targets of each version
     * matched by the expression.
     * This method is designed to be used with domain target. As different
     * versions maybe enabled on different targets, the return type used is a map.
     *
     * @param versionExpression a version expression (that contains wildcard character)
     * @return a map matching the enabled versions with their target(s)
     * @throws VersioningSyntaxException if getEnabledVersion throws an exception
     */
    public Map<String, Set<String>> getEnabledVersionInReferencedTargetsForExpression(String versionExpression)
            throws VersioningSyntaxException {

        Map<String,Set<String>> enabledVersionsInTargets = Collections.EMPTY_MAP;
        List<String> matchedVersions = getMatchedVersions(versionExpression, "domain");

        for (String matchedVersion : matchedVersions) {

            // retrieved all the enabled version on the referenced target on each matched version
            Map<String,Set<String>> tempMap =
                    getEnabledVersionsInReferencedTargets(matchedVersion);

            if(enabledVersionsInTargets != Collections.EMPTY_MAP){

                // foreach enabled version we combine the target list into the map
                for (Map.Entry<String, Set<String>> entry : tempMap.entrySet()) {
                    String tempKey = entry.getKey();
                    Set<String> tempList = entry.getValue();

                    if(enabledVersionsInTargets.containsKey(tempKey)){
                        enabledVersionsInTargets.get(tempKey).addAll(tempList);
                    } else {
                        enabledVersionsInTargets.put(tempKey, tempList);
                    }
                }
            } else {
                enabledVersionsInTargets = tempMap;
            }
        }
        return enabledVersionsInTargets;
    }

    /**
     * Search the enabled versions on the referenced targets of the given version.
     * This method is designed to be used with domain target. As different
     * versions maybe enabled on different targets, the return type used is a map.
     *
     * @param versionIdentifier a version expression (that contains wildcard character)
     * @return a map matching the enabled versions with their target(s)
     * @throws VersioningSyntaxException if getEnabledVersion throws an exception
     */
    public Map<String,Set<String>> getEnabledVersionsInReferencedTargets(String versionIdentifier)
            throws VersioningSyntaxException {

        Map<String,Set<String>> enabledVersionsInTargets =
                new HashMap<>();

        List<String> allTargets =
                domain.getAllReferencedTargetsForApplication(versionIdentifier);

        for (String target : allTargets) {
            String enabledVersion = getEnabledVersion(versionIdentifier, target);
            if(enabledVersion != null){
                // the key already exists, we just add the new target into the list
                if(enabledVersionsInTargets.containsKey(enabledVersion)){
                    enabledVersionsInTargets.get(enabledVersion).add(target);
                } else {
                    // we have to create the list associated with the key
                    Set<String> setTargets = new HashSet<>();
                    setTargets.add(target);
                    enabledVersionsInTargets.put(enabledVersion, setTargets);
                }
            }
        }
        return enabledVersionsInTargets;
    }

    /**
     * Search for the enabled version of the given application.
     *
     * @param name the application name
     * @param target an option supply from admin command, it's retained for
     * compatibility with other releases
     * @return the enabled version of the application, if exists
     * @throws VersioningSyntaxException if getUntaggedName throws an exception
     */
    public final String getEnabledVersion(String name, String target)
            throws VersioningSyntaxException {

        String untaggedName = VersioningUtils.getUntaggedName(name);
        List<String> allVersions = getAllversions(untaggedName, target);

        if (allVersions != null) {
            for (String app : allVersions) {
                // if a version of the app is enabled
                if (domain.isAppEnabledInTarget(app, target)) {
                    return app;
                }
            }
        }
        // no enabled version found
        return null;
    }

    /**
     * Process the expression matching operation of the given application name.
     *
     * @param name the application name containing the version expression
     * @param target the target we are looking for the verisons
     * @return a List of all expression matched versions, return empty list
     * if no version is registered on this target
     * or if getUntaggedName throws an exception
     */
    public final List<String> getMatchedVersions(String name, String target)
            throws VersioningSyntaxException, VersioningException {

        String untagged = VersioningUtils.getUntaggedName(name);
        List<String> allVersions = getAllversions(untagged, target);

        if (allVersions.size() == 0) {
            // if versionned
            if(!name.equals(untagged)){
                throw new VersioningException(
                    MessageFormat.format("Application {0} has no version registered", untagged));
            }
            return Collections.emptyList();
        }

        return VersioningUtils.matchExpression(allVersions, name);
    }

    /**
     *  Disable the enabled version of the application if it exists. This method
     *  is used in versioning context.
     *
     *  @param appName application's name
     *  @param target an option supply from admin command, it's retained for
     * compatibility with other releases
     *  @param report ActionReport, report object to send back to client.
     *  @param subject the Subject on whose behalf to run
     */
    public void handleDisable(final String appName, final String target,
            final ActionReport report, final Subject subject) throws VersioningSyntaxException {

        Set<String> versionsToDisable = Collections.emptySet();

        if (DeploymentUtils.isDomainTarget(target)) {
            // retrieve the enabled versions on each target in the domain
            Map<String,Set<String>> enabledVersions =
                    getEnabledVersionsInReferencedTargets(appName);

            if (!enabledVersions.isEmpty()) {
                versionsToDisable = enabledVersions.keySet();
            }
        } else {
            // retrieve the currently enabled version of the application
            String enabledVersion = getEnabledVersion(appName, target);

            if (enabledVersion != null
                    && !enabledVersion.equals(appName)) {
                versionsToDisable = new HashSet<>();
                versionsToDisable.add(enabledVersion);
            }
        }

        for (String currentVersion : versionsToDisable) {
            // invoke disable if the currently enabled version is not itself
            if (currentVersion != null
                    && !currentVersion.equals(appName)) {
                final ParameterMap parameters = new ParameterMap();
                parameters.add("DEFAULT", currentVersion);
                parameters.add("target", target);

                ActionReport subReport = report.addSubActionsReport();

                CommandRunner.CommandInvocation inv =
                        commandRunner.getCommandInvocation("disable", subReport, subject);
                inv.parameters(parameters).execute();
            }
        }
    }

    /**
     * Get the version directory-deployed from the given directory
     *
     * @param directory
     * @return the name of the version currently using the directory, else null
     * @throws VersioningSyntaxException     *
    */
    public String getVersionFromSameDir(File dir)
            throws VersioningSyntaxException{

        try {
            Application app = null;

            // check if directory deployment exist
            for (Application element : domain.getApplications().getApplications()) {
                app = element;
                /*
                 * A lifecycle module appears as an application but has a null location.
                 */
                if (dir.toURI().toString().equals(app.getLocation())) {
                    if(!VersioningUtils.getUntaggedName(app.getName()).equals(app.getName())){
                        return app.getName();
                    }
                }
            }
        } catch (VersioningSyntaxException ex) {
            // return null if an exception is thrown
        }
        return null;
    }
}
