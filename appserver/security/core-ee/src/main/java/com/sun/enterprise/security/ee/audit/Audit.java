/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.ee.audit;

import com.sun.appserv.security.AuditModule;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbIORConfigurationDescriptor;
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.WebComponentDescriptor;
import com.sun.enterprise.deployment.web.AuthorizationConstraint;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.deployment.web.SecurityConstraint;
import com.sun.enterprise.deployment.web.SecurityRole;
import com.sun.enterprise.deployment.web.UserDataConstraint;
import com.sun.enterprise.deployment.web.WebResourceCollection;
import com.sun.logging.LogDomains;
import jakarta.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.deployment.common.SecurityRoleMapper;
import org.glassfish.security.common.Role;

/**
 * Audit support class.
 *
 * <P>
 * This class provides convenience methods for producing audit output. Audit output is logged using the standard iAS
 * logger SECURITYLOGGER. However, audit output is only produced if auditing is active. Auditing is configured in
 * server.xml in the security-service element.
 *
 * <P>
 * Audit output if logged with Level.WARNING.
 *
 * <P>
 * Some diagnostic methods are also provided for debugging.
 *
 */
public class Audit extends AuditModule {

    private static Logger logger = LogDomains.getLogger(Audit.class, LogDomains.SECURITY_LOGGER);
    private static final String AUDIT_ON = "auditOn";
    private static boolean auditFlag;

    /**
     * Check auditing state.
     *
     * @returns True if auditing is active currently.
     *
     */
    public static boolean isActive() {
        return auditFlag;
    }

    @Override
    public void init(Properties props) {
        super.init(props);
        String audit = props.getProperty(AUDIT_ON);
        auditFlag = audit == null ? false : Boolean.valueOf(audit);
    }

    /**
     * Invoked post authentication request for a user in a given realm
     *
     * @param user username for whom the authentication request was made
     * @param realm the realm name under which the user is authenticated.
     * @param success the status of the authentication
     */
    @Override
    public void authentication(String user, String realm, boolean success) {
        if (auditFlag) {
            StringBuffer sbuf = new StringBuffer("Audit: Authentication for user = (");
            sbuf.append(user);
            sbuf.append(") under realm = (");
            sbuf.append(realm).append(") returned = ").append(success);
            logger.log(Level.INFO, sbuf.toString());
        }
    }

    /**
     * Invoked post web authorization request.
     *
     * @param user the username for whom the authorization was performed
     * @param req the HttpRequest object for the web request
     * @param type either hasResourcePermission, hasUserDataPermission or hasRoleRefPermission
     * @param success the status of the web authorization request
     */
    @Override
    public void webInvocation(String user, HttpServletRequest req, String type, boolean success) {
        if (auditFlag) {
            StringBuilder sbuf = new StringBuilder("Audit: [Web] Authorization for user = (");
            sbuf.append(user).append(") and permission type = (").append(type).append(") for request ");
            sbuf.append(req.getMethod()).append(" ").append(req.getRequestURI()).append(" returned =").append(success);
            logger.log(Level.INFO, sbuf.toString());
        }
    }

    /**
     * Invoked post ejb authorization request.
     *
     * @param user the username for whom the authorization was performed
     * @param ejb the ejb name for which this authorization was performed
     * @param method the method name for which this authorization was performed
     * @param success the status of the ejb authorization request
     */
    @Override
    public void ejbInvocation(String user, String ejb, String method, boolean success) {
        if (auditFlag) {
            // Modified from StringBuffer to StringBuilder
            StringBuilder sbuf = new StringBuilder("Audit: [EJB] Authorization for user =");
            sbuf.append(user).append(" for ejb = (");
            sbuf.append(ejb).append(") method = (").append(method).append(") returned =").append(success);
            logger.log(Level.INFO, sbuf.toString());
        }
    }

    /**
     * Invoked post ejb authorization request.
     *
     * @param user the username for whom the authorization was performed
     * @param ejb the ejb name for which this authorization was performed
     * @param method the method name for which this authorization was performed
     * @param success the status of the ejb authorization request
     */

    /**
     * Invoked during validation of the web service request
     *
     * @param uri The URL representation of the web service endpoint
     * @param endpoint The name of the endpoint representation
     * @param success the status of the web service request validation
     */
    @Override
    public void webServiceInvocation(String uri, String endpoint, boolean success) {
        if (auditFlag) {
            StringBuilder sbuf = new StringBuilder("Audit: [WebService] ");
            sbuf.append("uri: ").append(uri);
            sbuf.append("endpoint: ").append(endpoint);
            sbuf.append(", valid request =").append(success);
            logger.log(Level.INFO, sbuf.toString());
        }
    }

    /**
     * Invoked during validation of the web service request
     *
     * @param endpoint The URL representation of the web service endpoint
     * @param success the status of the web service request validation
     */
    @Override
    public void ejbAsWebServiceInvocation(String endpoint, boolean success) {
        if (auditFlag) {
            StringBuilder sbuf = new StringBuilder("Audit: [EjbAsWebService] ");
            sbuf.append("endpoint : ").append(endpoint).append(", valid request =").append(success);
            logger.log(Level.INFO, sbuf.toString());
        }
    }

    /**
     * Invoked upon completion of the server startup
     */
    @Override
    public void serverStarted() {
        if (auditFlag) {
            logger.log(Level.INFO, "Audit: Application server startup complete");
        }
    }

    /**
     * Invoked upon completion of the server shutdown
     */
    @Override
    public void serverShutdown() {
        if (auditFlag) {
            logger.log(Level.INFO, "Audit: Application server shutdown complete");
        }
    }

    /**
     * Diagnostic method. Read roles and ACLs from the given Application and dump a somewhat organized summary of what has
     * been set. This can be used to diagnose deployment or runtime deployment errors as well as to help in configuring
     * application descriptors.
     *
     * <P>
     * Implementation is not particularly efficient but this is only called for debugging purposes at startup. All errors
     * are ignored.
     *
     * @param app Application object to analyze.
     *
     */
    public static void showACL(Application app) {
        if (!isActive() || !logger.isLoggable(Level.FINEST)) {
            return;
        }

        try {
            dumpDiagnostics(app);

        } catch (Throwable e) {
            logger.fine("Error while showing ACL diagnostics: " + e.toString());
        }
    }

    /**
     * Do the work for showACL().
     *
     */
    private static void dumpDiagnostics(Application app) {
        logger.finest("====[ Role and ACL Summary ]==========");
        if (!app.isVirtual()) {
            logger.finest("Summary for application: " + app.getRegistrationName());
        } else {
            logger.finest("Standalone module.");
        }
        logger.finest("EJB components: " + getEjbComponentCount(app));
        logger.finest("Web components: " + getWebComponentCount(app));

        StringBuffer sb;

        // show all roles with associated group & user mappings
        Set<Role> allRoles = app.getRoles();
        if (allRoles == null) {
            logger.finest("- No roles present.");
            return;
        }
        SecurityRoleMapper roleMapper = app.getRoleMapper();
        if (roleMapper == null) {
            logger.finest("- No role mappings present.");
            return;
        }

        logger.finest("--[ Configured roles and mappings ]--");
        HashMap<String, Set<String>> allRoleMap = new HashMap<>();

        for (Role role : allRoles) {
            logger.finest(" [" + role.getName() + "]");
            allRoleMap.put(role.getName(), new HashSet<>());

            sb = new StringBuffer();
            sb.append("  is mapped to groups: ");
            Enumeration<? extends Principal> grps = roleMapper.getGroupsAssignedTo(role);
            while (grps.hasMoreElements()) {
                sb.append(grps.nextElement());
                sb.append(" ");
            }
            logger.finest(sb.toString());

            sb = new StringBuffer();
            sb.append("  is mapped to principals: ");
            Enumeration<? extends Principal> users = roleMapper.getUsersAssignedTo(role);
            while (users.hasMoreElements()) {
                sb.append(users.nextElement());
                sb.append(" ");
            }
            logger.finest(sb.toString());
        }

        // Process all EJB modules

        Set<EjbBundleDescriptor> ejbDescriptorSet = app.getBundleDescriptors(EjbBundleDescriptor.class);
        for (EjbBundleDescriptor bundle : ejbDescriptorSet) {
            logger.finest("--[ EJB module: " + bundle.getName() + " ]--");
            Set<? extends EjbDescriptor> ejbs = bundle.getEjbs();
            for (EjbDescriptor ejb : ejbs) {
                logger.finest("EJB: " + ejb.getEjbClassName());

                // check and show run-as if present
                if (!ejb.getUsesCallerIdentity()) {
                    RunAsIdentityDescriptor runas = ejb.getRunAsIdentity();
                    if (runas == null) {
                        logger.finest(" (ejb does not use caller " + "identity)");
                    } else {
                        String role = runas.getRoleName();
                        String user = runas.getPrincipal();
                        logger.finest(" Will run-as: Role: " + role + "  Principal: " + user);
                        if (role == null || "".equals(role) || user == null || "".equals(user)) {
                            if (logger.isLoggable(Level.FINEST)) {
                                logger.finest("*** Configuration error!");
                            }
                        }
                    }
                }

                // iterate through available methods
                logger.finest(" Method to Role restriction list:");
                Set<MethodDescriptor> methods = ejb.getMethodDescriptors();
                for (MethodDescriptor md : methods) {
                    logger.finest("   " + md.getFormattedString());

                    Set<MethodPermission> perms = ejb.getMethodPermissionsFor(md);
                    StringBuffer rbuf = new StringBuffer();
                    rbuf.append("     can only be invoked by: ");
                    boolean unchecked = false, excluded = false, roleBased = false;
                    for (MethodPermission p : perms) {
                        if (p.isExcluded()) {
                            excluded = true;
                            logger.finest("     excluded - can not be invoked");
                        } else if (p.isUnchecked()) {
                            unchecked = true;
                            logger.finest("     unchecked - can be invoked by all");
                        } else if (p.isRoleBased()) {
                            roleBased = true;
                            Role r = p.getRole();
                            rbuf.append(r.getName());
                            rbuf.append(" ");
                            // add to role's accessible list
                            Set<String> ram = allRoleMap.get(r.getName());
                            ram.add(bundle.getName() + ":" + ejb.getEjbClassName() + "." + md.getFormattedString());
                        }
                    }

                    if (roleBased) {
                        logger.finest(rbuf.toString());
                        if (excluded || unchecked) {
                            logger.finest("*** Configuration error!");
                        }
                    } else if (unchecked) {
                        if (excluded) {
                            logger.finest("*** Configuration error!");
                        }
                        Set<String> rks = allRoleMap.keySet();
                        for (String key : rks) {
                            Set<String> ram = allRoleMap.get(key);
                            ram.add(bundle.getName() + ":" + ejb.getEjbClassName() + "." + md.getFormattedString());
                        }
                    } else if (!excluded) {
                        logger.finest("*** Configuration error!");
                    }
                }

                // IOR config for this ejb
                logger.finest(" IOR configuration:");
                Set<EjbIORConfigurationDescriptor> iors = ejb.getIORConfigurationDescriptors();
                if (iors != null) {
                    for (EjbIORConfigurationDescriptor ior : iors) {
                        StringBuffer iorsb = new StringBuffer();
                        iorsb.append("realm=");
                        iorsb.append(ior.getRealmName());
                        iorsb.append(", integrity=");
                        iorsb.append(ior.getIntegrity());
                        iorsb.append(", trust-in-target=");
                        iorsb.append(ior.getEstablishTrustInTarget());
                        iorsb.append(", trust-in-client=");
                        iorsb.append(ior.getEstablishTrustInClient());
                        iorsb.append(", propagation=");
                        iorsb.append(ior.getCallerPropagation());
                        iorsb.append(", auth-method=");
                        iorsb.append(ior.getAuthenticationMethod());
                        logger.finest(iorsb.toString());
                    }
                }
            }
        }

        // show role->accessible methods list
        logger.finest("--[ EJB methods accessible by role ]--");

        Set<String> rks = allRoleMap.keySet();
        for (String roleName : rks) {
            logger.finest(" [" + roleName + "]");
            Set<String> ram = allRoleMap.get(roleName);
            for (String meth : ram) {
                logger.finest("   " + meth);
            }
        }

        // Process all Web modules

        Set<WebBundleDescriptor> webDescriptorSet = app.getBundleDescriptors(WebBundleDescriptor.class);
        for (WebBundleDescriptor wbd : webDescriptorSet) {
            logger.finest("--[ Web module: " + wbd.getContextRoot() + " ]--");

            // login config
            LoginConfiguration lconf = wbd.getLoginConfiguration();
            if (lconf != null) {
                logger.finest("  Login config: realm=" + lconf.getRealmName() + ", method=" + lconf.getAuthenticationMethod() + ", form="
                        + lconf.getFormLoginPage() + ", error=" + lconf.getFormErrorPage());
            }

            // get WebComponentDescriptorsSet() info
            logger.finest("  Contains components:");
            Set<WebComponentDescriptor> webComps = wbd.getWebComponentDescriptors();
            for (WebComponentDescriptor wcd : webComps) {
                StringBuffer name = new StringBuffer();
                name.append("   - ").append(wcd.getCanonicalName());
                name.append(" [ ");
                Enumeration<String> urlPs = wcd.getUrlPatterns();
                while (urlPs.hasMoreElements()) {
                    name.append(urlPs.nextElement());
                    name.append(" ");
                }
                name.append("]");
                logger.finest(name.toString());

                RunAsIdentityDescriptor runas = wcd.getRunAsIdentity();
                if (runas != null) {
                    String role = runas.getRoleName();
                    String user = runas.getPrincipal();
                    logger.finest("      Will run-as: Role: " + role + "  Principal: " + user);
                    if (role == null || "".equals(role) || user == null || "".equals(user)) {
                        logger.finest("*** Configuration error!");
                    }
                }

            }

            // security constraints
            logger.finest("  Security constraints:");
            Set<SecurityConstraint> constraints = wbd.getSecurityConstraints();
            for (SecurityConstraint constraint : constraints) {
                for (WebResourceCollection wrc : constraint.getWebResourceCollections()) {
                    // show list of methods for this collection
                    StringBuffer sbm = new StringBuffer();
                    for (String httpMethod : wrc.getHttpMethods()) {
                        sbm.append(httpMethod);
                        sbm.append(" ");
                    }
                    logger.finest("     Using method: " + sbm.toString());

                    // and then list of url patterns
                    for (String urlPattern : wrc.getUrlPatterns()) {
                        logger.finest("       " + urlPattern);
                    }
                } // end res.collection iterator

                // show roles which apply to above set of collections
                AuthorizationConstraint authCons = constraint.getAuthorizationConstraint();
                Enumeration<SecurityRole> rolesEnum = authCons.getSecurityRoles();
                StringBuffer rsb = new StringBuffer();
                rsb.append("     Accessible by roles: ");
                while (rolesEnum.hasMoreElements()) {
                    SecurityRole sr = rolesEnum.nextElement();
                    rsb.append(sr.getName());
                    rsb.append(" ");
                }
                logger.finest(rsb.toString());

                // show transport guarantee
                UserDataConstraint udc = constraint.getUserDataConstraint();
                if (udc != null) {
                    logger.finest("     Transport guarantee: " + udc.getTransportGuarantee());
                }

            } // end sec.constraint

        } // end webDescriptorSet.iterator

        logger.finest("======================================");
    }

    /**
     * The number of Web Components in this application. Current implementation only return the number of servlets inside
     * the application, and not the JSPs since we cannot get that information from deployment descriptors.
     *
     * @return the number of Web Components
     */
    private static int getWebComponentCount(Application app) {
        int count = 0;
        for (WebBundleDescriptor wbd : app.getBundleDescriptors(WebBundleDescriptor.class)) {
            count = count + wbd.getWebComponentDescriptors().size();
        }
        return count;
    }

    /**
     * The number of EJB JARs in this application.
     *
     * @return the number of EJB JARS
     */
    private static int getEjbComponentCount(Application app) {
        int count = 0;
        for (EjbBundleDescriptor ejbd : app.getBundleDescriptors(EjbBundleDescriptor.class)) {
            count = count + ejbd.getEjbs().size();
        }
        return count;
    }
}
