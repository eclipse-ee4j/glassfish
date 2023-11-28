/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.ee.acl;

import static com.sun.enterprise.util.Utility.isEmpty;
import static java.util.Collections.emptySet;
import static java.util.Collections.enumeration;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static java.util.stream.Collectors.toSet;

import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.logging.LogDomains;
import java.io.Serializable;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.Role;
import org.glassfish.security.common.UserNameAndPassword;
import org.glassfish.security.common.UserPrincipal;

/**
 * This Object maintains a mapping of users and groups to application specific Roles. Using this object this mapping
 * information could be maintained and queried at a later time. This is a complete rewrite of the previous RoleMapper
 * for Jakarta Authorization related changes.
 *
 * @author Harpreet Singh
 */
public class RoleMapper implements Serializable, SecurityRoleMapper {

    private static final long serialVersionUID = -4455830942007736853L;
    private static final Logger LOG = LogDomains.getLogger(RoleMapper.class, LogDomains.SECURITY_LOGGER, false);

    private String appName;
    private final Map<String, Subject> roleToSubject = new HashMap<>();

    // Default mapper to emulate Servlet default p2r mapping semantics
    private String defaultPrincipalToRoleMappingClassName;
    private final DefaultRoleToSubjectMapping defaultRoleToSubjectMapping = new DefaultRoleToSubjectMapping();

    /*
     * The following 2 Maps are a copy of roleToSubject. This is added as a support for deployment. Should think of
     * optimizing this.
     */
    private final Map<String, Set<UserPrincipal>> roleToPrincipal = new HashMap<>();
    private final Map<String, Set<Group>> roleToGroup = new HashMap<>();

    /* The following objects are used to detect conflicts during deployment */
    /*
     * .....Mapping of module (or application) that is presently calling assignRole(). It is set by the startMappingFor()
     * method. After all the subjects have been assigned, stopMappingFor() is called and then the mappings can be checked
     * against those previously assigned.
     */
    private Mapping currentMapping;

    /**
     * These override roles mapped in submodules.
     */
    private Set<Role> topLevelRoles;

    /**
     * Used to identify the application level mapping file
     */
    private static final String TOP_LEVEL = "sun-application.xml mapping file";

    // used to log a warning only one time
    private boolean conflictLogged;

    // store roles that have a conflict so they are not re-mapped
    private Set<Role> conflictedRoles;

    private transient SecurityService securityService;

    RoleMapper(String appName) {
        this.appName = appName;
        securityService = Globals.getDefaultBaseServiceLocator().getService(SecurityService.class, ServerEnvironment.DEFAULT_INSTANCE_NAME);
        defaultPrincipalToRoleMappingClassName = getDefaultPrincipalToRoleMappingClassName();
    }

    /**
     * Copy constructor. This is called from the JSR88 implementation. This is not stored into the internal rolemapper maps.
     */
    public RoleMapper(RoleMapper other) {
        this.appName = other.getName();
        for (Iterator<String> it = other.getRoles(); it.hasNext();) {
            String role = it.next();

            // Recover groups
            Enumeration<Group> groups = other.getGroupsAssignedTo(new Role(role));
            Set<Group> groupsToRole = new HashSet<>();
            for (; groups.hasMoreElements();) {
                Group gp = groups.nextElement();
                groupsToRole.add(new Group(gp.getName()));
                addRoleToSubject(gp, role);
            }
            this.roleToGroup.put(role, groupsToRole);

            // Recover principles
            Enumeration<UserPrincipal> users = other.getUsersAssignedTo(new Role(role));
            Set<UserPrincipal> usersToRole = new HashSet<>();
            while (users.hasMoreElements()) {
                UserPrincipal principal = users.nextElement();
                usersToRole.add(new UserNameAndPassword(principal.getName()));
                addRoleToSubject(principal, role);
            }

            this.roleToPrincipal.put(role, usersToRole);
        }
    }

    /**
     * @return The application/module name for this RoleMapper
     */
    @Override
    public String getName() {
        return appName;
    }

    /**
     * @param name The application/module name
     */
    @Override
    public void setName(String name) {
        this.appName = name;
    }

    /**
     * Returns the RoleToSubjectMapping for the RoleMapping
     *
     * @return Map of role->subject mapping
     */
    @Override
    public Map<String, Subject> getRoleToSubjectMapping() {
        // This causes the last currentMapping information to be added
        checkAndAddMappings();

        if (roleToSubject.isEmpty() && isDefaultPrincipalToRoleMapping()) {
            return defaultRoleToSubjectMapping;
        }

        return roleToSubject;
    }

    @Override
    public Map<String, Set<String>> getGroupToRolesMapping() {
        Map<String, Set<String>> groupToRoles = new HashMap<>();

        for (Map.Entry<String, Subject> roleToSubject : getRoleToSubjectMapping().entrySet()) {
            for (String group : getGroups(roleToSubject.getValue())) {
                groupToRoles.computeIfAbsent(group, g -> new HashSet<>())
                            .add(roleToSubject.getKey());
            }
        }

        return groupToRoles;
    }

    /**
     * Assigns a Principal to the specified role. This method delegates work to internalAssignRole() after checking for
     * conflicts. RootDeploymentDescriptor added as a fix for: https://glassfish.dev.java.net/issues/show_bug.cgi?id=2475
     *
     * The first time this is called, a new Mapping object is created to store the role mapping information. When called
     * again from a different module, the old mapping information is checked and stored and a new Mapping object is created.
     *
     * @param principal The principal that needs to be assigned to the role.
     * @param role The Role the principal is being assigned to.
     * @param rootDeploymentDescriptor The descriptor of the module containing the role mapping
     */
    @Override
    public void assignRole(Principal principal, Role role, RootDeploymentDescriptor rootDeploymentDescriptor) {
        String callingModuleID = getModuleID(rootDeploymentDescriptor);

        if (currentMapping == null) {
            currentMapping = new Mapping(callingModuleID);
        } else if (!callingModuleID.equals(currentMapping.owner)) {
            checkAndAddMappings();
            currentMapping = new Mapping(callingModuleID);
        }

        // When using the top level mapping
        if (callingModuleID.equals(TOP_LEVEL) && topLevelRoles == null) {
            topLevelRoles = new HashSet<>();
        }

        // Store principal and role temporarily until stopMappingFor called
        currentMapping.addMapping(principal, role);
    }

    /**
     * Remove the given role-principal mapping
     *
     * @param role Role object
     * @param principal the principal
     */
    @Override
    public void unassignPrincipalFromRole(Role role, Principal principal) {
        String roleName = role.getName();
        Subject subject = roleToSubject.get(roleName);
        if (subject != null) {
            subject.getPrincipals().remove(principal);
            roleToSubject.put(roleName, subject);
        }

        if (principal instanceof Group) {
            Set<Group> groups = roleToGroup.get(roleName);
            if (groups != null) {
                groups.remove(principal);
                roleToGroup.put(roleName, groups);
            }
        } else {
            Set<UserPrincipal> principals = roleToPrincipal.get(roleName);
            if (principals != null) {
                principals.remove(principal);
                roleToPrincipal.put(roleName, principals);
            }
        }
    }


    @Override
    public Iterator<String> getRoles() {
        return roleToSubject.keySet().iterator(); // All the roles
    }

    @Override
    public Enumeration<Group> getGroupsAssignedTo(Role role) {
        Set<Group> groups = roleToGroup.get(role.getName());
        if (groups == null) {
            return enumeration(emptySet());
        }

        return enumeration(groups);
    }


    @Override
    public Enumeration<UserPrincipal> getUsersAssignedTo(Role role) {
        Set<UserPrincipal> principals = roleToPrincipal.get(role.getName());
        if (principals == null) {
            return enumeration(emptySet());
        }

        return enumeration(principals);
    }

    @Override
    public void unassignRole(Role role) {
        if (role != null) {
            String roleName = role.getName();
            roleToSubject.remove(roleName);
            roleToPrincipal.remove(roleName);
            roleToGroup.remove(roleName);
        }
    }

    /**
     * @return String. String representation of the RoleToPrincipal Mapping
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("RoleMapper:");

        for (Iterator<String> e = this.getRoles(); e.hasNext();) {
            String r = e.next();
            s.append("\n\tRole (").append(r).append(") has Principals(");
            Subject sub = roleToSubject.get(r);
            Iterator<Principal> it = sub.getPrincipals().iterator();
            for (; it.hasNext();) {
                Principal p = it.next();
                s.append(p.getName()).append(" ");
            }
            s.append(")");
        }

        LOG.log(Level.FINER, () -> s.toString());

        return s.toString();
    }

    /**
     * @param principal A principal that corresponds to the role
     * @param role A role corresponding to this principal
     */
    private void addRoleToSubject(final Principal principal, String role) {
        roleToSubject.computeIfAbsent(role, e -> new Subject())
                     .getPrincipals()
                     .add(principal);
    }

    /**
     * @returns the class name used for default Principal to role mapping return null if default P2R mapping is not
     * supported.
     */
    private String getDefaultPrincipalToRoleMappingClassName() {
        String className = null;
        try {
            if (securityService != null && Boolean.parseBoolean(securityService.getActivateDefaultPrincipalToRoleMapping())) {
                className = securityService.getMappedPrincipalClass();
                if (isEmpty(className)) {
                    className = Group.class.getName();
                }
            }

            if (className == null) {
                return null;
            }

            // To avoid a failure later make sure we can instantiate now
            Class.forName(className)
                 .getConstructor(String.class)
                 .newInstance("anystring");

            return className;
        } catch (Exception e) {
            LOG.log(SEVERE, "pc.getDefaultP2RMappingClass: " + e);
            return null;
        }
    }

    // @return true or false depending on activation of
    // the mapping via domain.xml.
    @Override
    public boolean isDefaultPrincipalToRoleMapping() {
        return defaultPrincipalToRoleMappingClassName != null;
    }

    @Override
    public Set<String> getGroups(Subject subject) {
        return
            subject.getPrincipals()
                   .stream()
                   .filter(e -> e instanceof Group)
                   .map(e -> e.getName())
                   .collect(toSet());
    }

    // The method that does the work for assignRole().
    private void internalAssignRole(Principal principal, Role role) {
        String roleName = role.getName();
        if (LOG.isLoggable(FINE)) {
            LOG.log(FINE, "SECURITY:RoleMapper Assigning Role {0} to {1}", new Object[] {roleName, principal});
        }

        addRoleToSubject(principal, roleName);

        if (principal instanceof Group) {
            roleToGroup.computeIfAbsent(roleName, e -> new HashSet<>())
                       .add((Group) principal);
        } else if (principal instanceof UserPrincipal) {
            roleToPrincipal.computeIfAbsent(roleName, e -> new HashSet<>())
                           .add((UserPrincipal) principal);
        } else {
            throw new IllegalArgumentException("Unknown principal class: " + principal.getClass());
        }
    }

    /**
     * Only web/ejb BundleDescriptor and Application descriptor objects are used for role mapping currently. If other
     * subtypes of RootDeploymentDescriptor are used in the future, they should be added here.
     */
    private String getModuleID(RootDeploymentDescriptor rootDeploymentDescriptor) {
        // V3: Can we use this : return rdd.getModuleID();

        if (rootDeploymentDescriptor.isApplication()) {
            return TOP_LEVEL;
        }

        if (rootDeploymentDescriptor.getModuleDescriptor() != null) {
            return rootDeploymentDescriptor.getModuleDescriptor().getArchiveUri();
        }

        // Cannot happen unless glassfish code is changed
        throw new AssertionError(rootDeploymentDescriptor.getClass() + " is not a known descriptor type");

    }

    /**
     * For each role in the current mapping:
     *
     * First check that the role does not already exist in the top-level mapping. If it does, then the top-level role
     * mapping overrides the current one and we do not need to check if they conflict. Just continue with the next role.
     *
     * If the current mapping is from the top-level file, then check to see if the role has already been mapped. If so, do
     * not need to check for conflicts. Simply override and assign the role.
     *
     * If the above cases do not apply, check for conflicts with roles already set. If there is a conflict, it is between
     * two submodules, so the role should be unmapped in the existing role mappings.
     *
     */
    private void checkAndAddMappings() {
        if (currentMapping == null) {
            return;
        }

        for (Role role : currentMapping.getRoles()) {

            if (topLevelRoles != null && topLevelRoles.contains(role)) {
                logConflictWarning();
                LOG.log(FINE, () ->
                        "Role " + role + " from module " + currentMapping.owner + " is being overridden by top-level mapping.");

                continue;
            }

            if (currentMapping.owner.equals(TOP_LEVEL)) {
                topLevelRoles.add(role);
                if (roleToSubject.keySet().contains(role.getName())) {
                    logConflictWarning();
                    LOG.log(FINE, () ->
                            "Role " + role + " from top-level mapping descriptor is " + "overriding existing role in sub module.");

                    unassignRole(role);
                }

            } else if (roleConflicts(role, currentMapping.getPrincipals(role))) {
                // Detail message already logged
                logConflictWarning();
                unassignRole(role);
                continue;
            }

            // No problems, so assign role
            for (Principal principal : currentMapping.getPrincipals(role)) {
                internalAssignRole(principal, role);
            }

        }

        // Clear current mapping
        currentMapping = null;
    }

    private boolean roleConflicts(Role r, Set<Principal> ps) {
        // check to see if there has been a previous conflict
        if (conflictedRoles != null && conflictedRoles.contains(r)) {
            LOG.log(FINE,
                    () -> "Role " + r + " from module " + currentMapping.owner + " has already had a conflict with other modules.");

            return true;
        }

        // If role not previously mapped, no conflict
        if (!roleToSubject.keySet().contains(r.getName())) {
            return false;
        }

        // check number of mappings first
        int targetNumPrin = ps.size();
        int actualNum = 0;
        Set<UserPrincipal> pSet = roleToPrincipal.get(r.getName());
        Set<Group> gSet = roleToGroup.get(r.getName());
        actualNum += pSet == null ? 0 : pSet.size();
        actualNum += gSet == null ? 0 : gSet.size();
        if (targetNumPrin != actualNum) {
            if (LOG.isLoggable(FINE)) {
                LOG.log(FINE, "Module " + currentMapping.owner + " has different number of mappings for role " + r.getName()
                        + " than other mapping files");
            }

            if (conflictedRoles == null) {
                conflictedRoles = new HashSet<>();
            }

            conflictedRoles.add(r);
            return true;
        }

        // check the principals and groups
        boolean fail = false;
        for (Principal p : ps) {
            if (p instanceof Group) {
                if (gSet != null && !gSet.contains(p)) {
                    fail = true;
                }

            } else if (pSet != null && !pSet.contains(p)) {
                fail = true;
            }

            if (fail) {
                if (LOG.isLoggable(FINE)) {
                    LOG.log(FINE, "Role " + r + " in module " + currentMapping.owner + " is not included in other modules.");
                }

                if (conflictedRoles == null) {
                    conflictedRoles = new HashSet<>();
                }

                conflictedRoles.add(r);
                return true;
            }

        }

        // no conflicts
        return false;
    }

    private void logConflictWarning() {
        if (!conflictLogged) {
            LOG.log(WARNING, "Role mapping conflicts found in application {0}. Some roles may not be mapped.", getName());
            conflictLogged = true;
        }
    }

    /**
     * Used to represent the role mapping of a single descriptor file.
     */
    private static class Mapping implements Serializable {
        private static final long serialVersionUID = 5863982599500877228L;

        private final String owner;
        private final Map<Role, Set<Principal>> roleMap;

        Mapping(String owner) {
            this.owner = owner;
            roleMap = new HashMap<>();
        }

        void addMapping(Principal principal, Role role) {
            roleMap.computeIfAbsent(role, e -> new HashSet<>())
                   .add(principal);
        }

        Set<Role> getRoles() {
            return roleMap.keySet();
        }

        Set<Principal> getPrincipals(Role r) {
            return roleMap.get(r);
        }
    }

    class DefaultRoleToSubjectMapping extends HashMap<String, Subject> {
        private static final long serialVersionUID = 3074733840327132690L;

        private final Map<String, Subject> roleMap = new HashMap<>();

        DefaultRoleToSubjectMapping() {
        }

        // Do not map '**' to a Principal as this represents the any authenticated user role
        @Override
        public Subject get(Object key) {
            synchronized (roleMap) {
                Subject subject = roleMap.get(key);
                if (subject == null && key instanceof String && !"**".equals(key)) {
                    subject = new Subject();
                    String roleName = (String) key;
                    subject.getPrincipals().add(getSameNamedPrincipal(roleName));
                    roleMap.put(roleName, subject);
                }

                return subject;
            }
        }

        Principal getSameNamedPrincipal(String roleName) {
            try {
                return (Principal)
                    Class.forName(defaultPrincipalToRoleMappingClassName)
                         .getConstructor(String.class)
                         .newInstance(roleName);
            } catch (Exception e) {
                LOG.log(SEVERE, "rm.getSameNamedPrincipal", new Object[] { roleName, e });
                throw new RuntimeException("Unable to get principal by default p2r mapping");
            }
        }
    }
}
