/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997-2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.realm;

import com.sun.enterprise.security.GroupPrincipal;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.security.auth.Subject;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.CredentialExpiredException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.catalina.Container;
import org.apache.catalina.LogFacade;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;

/**
 * <p>
 * Implementation of <b>Realm</b> that authenticates users via the <em>Java Authentication and Authorization
 * Service</em> (JAAS). JAAS support requires either JDK 1.4 (which includes it as part of the standard platform) or JDK
 * 1.3 (with the plug-in <code>jaas.jar</code> file).
 * </p>
 *
 * <p>
 * The value configured for the <code>appName</code> property is passed to the
 * <code>javax.security.auth.login.LoginContext</code> constructor, to specify the <em>application name</em> used to
 * select the set of relevant <code>LoginModules</code> required.
 * </p>
 *
 * <p>
 * The JAAS Specification describes the result of a successful login as a <code>javax.security.auth.Subject</code>
 * instance, which can contain zero or more <code>java.security.Principal</code> objects in the return value of the
 * <code>Subject.getPrincipals()</code> method. However, it provides no guidance on how to distinguish Principals that
 * describe the individual user (and are thus appropriate to return as the value of request.getUserPrincipal() in a web
 * application) from the Principal(s) that describe the authorized roles for this user. To maintain as much independence
 * as possible from the underlying <code>LoginMethod</code> implementation executed by JAAS, the following policy is
 * implemented by this Realm:
 * </p>
 *
 * <ul>
 * <li>The JAAS <code>LoginModule</code> is assumed to return a <code>Subject with at least one <code>Principal</code>
 * instance representing the user himself or herself, and zero or more separate <code>Principals</code> representing the
 * security roles authorized for this user.</li>
 * <li>On the <code>Principal</code> representing the user, the Principal name is an appropriate value to return via the
 * Servlet API method <code>HttpServletRequest.getRemoteUser()</code>.</li>
 * <li>On the <code>Principals</code> representing the security roles, the name is the name of the authorized security
 * role.</li>
 * <li>This Realm will be configured with two lists of fully qualified Java class names of classes that implement
 * <code>java.security.Principal</code> - one that identifies class(es) representing a user, and one that identifies
 * class(es) representing a security role.</li>
 * <li>As this Realm iterates over the <code>Principals</code> returned by <code>Subject.getPrincipals()</code>, it will
 * identify the first <code>Principal</code> that matches the "user classes" list as the <code>Principal</code> for this
 * user.</li>
 * <li>As this Realm iterates over the <code>Principals</code> returned by <code>Subject.getPrincipals()</code>, it will
 * accumulate the set of all <code>Principals</code> matching the "role classes" list as identifying the security roles
 * for this user.</li>
 * <li>It is a configuration error for the JAAS login method to return a validated <code>Subject</code> without a
 * <code>Principal</code> that matches the "user classes" list.</li>
 * </ul>
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.3 $ $Date: 2006/03/12 01:27:04 $
 */

public final class JAASRealm extends RealmBase {

    // ----------------------------------------------------- Instance Variables

    /**
     * The application name passed to the JAAS <code>LoginContext</code>, which uses it to select the set of relevant
     * <code>LoginModules</code>.
     */
    protected String appName;

    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String info = "org.apache.catalina.realm.JAASRealm/1.0";

    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String name = "JAASRealm";

    /**
     * The list of role class names, split out for easy processing.
     */
    protected ArrayList<String> roleClasses = new ArrayList<>();

    /**
     * The set of user class names, split out for easy processing.
     */
    protected ArrayList<String> userClasses = new ArrayList<>();

    /**
     * Comma-delimited list of <code>javax.security.Principal</code> classes that represent security roles.
     */
    protected String roleClassNames = null;

    // ------------------------------------------------------------- Properties

    /**
     * setter for the appName member variable
     *
     * @deprecated JAAS should use the Engine ( domain ) name and webpp/host overrides
     */
    @Deprecated
    public void setAppName(String name) {
        appName = name;
    }

    /**
     * getter for the appName member variable
     */
    public String getAppName() {
        return appName;
    }

    @Override
    public void setContainer(Container container) {
        super.setContainer(container);
        String name = container.getName();
        if (appName == null) {
            appName = name;
            log.log(Level.INFO, LogFacade.SETTING_JAAS_INFO, appName);
        }
    }

    /**
     * Unsupported.
     *
     * @throws UnsupportedOperationException
     */
    @Deprecated
    @Override
    public Principal authenticate(HttpServletRequest hreq) {
        throw new UnsupportedOperationException();
    }


    public String getRoleClassNames() {
        return (this.roleClassNames);
    }

    public void setRoleClassNames(String roleClassNames) {
        this.roleClassNames = roleClassNames;
        roleClasses.clear();
        String temp = this.roleClassNames;
        if (temp == null) {
            return;
        }
        while (true) {
            int comma = temp.indexOf(',');
            if (comma < 0) {
                break;
            }
            roleClasses.add(temp.substring(0, comma).trim());
            temp = temp.substring(comma + 1);
        }
        temp = temp.trim();
        if (temp.length() > 0) {
            roleClasses.add(temp);
        }
    }

    /**
     * Comma-delimited list of <code>javax.security.Principal</code> classes that represent individual users.
     */
    protected String userClassNames = null;

    public String getUserClassNames() {
        return (this.userClassNames);
    }

    public void setUserClassNames(String userClassNames) {
        this.userClassNames = userClassNames;
        userClasses.clear();
        String temp = this.userClassNames;
        if (temp == null) {
            return;
        }
        while (true) {
            int comma = temp.indexOf(',');
            if (comma < 0) {
                break;
            }
            userClasses.add(temp.substring(0, comma).trim());
            temp = temp.substring(comma + 1);
        }

        temp = temp.trim();
        if (temp.length() > 0) {
            userClasses.add(temp);
        }
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Return the Principal associated with the specified username and credentials, if there is one; otherwise return
     * <code>null</code>.
     *
     * If there are any errors with the JDBC connection, executing the query or anything we return null (don't
     * authenticate). This event is also logged, and the connection will be closed so that a subsequent request will
     * automatically re-open it.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in authenticating this username
     */
    public Principal authenticate(String username, char[] credentials) {

        // Establish a LoginContext to use for authentication
        try {
            LoginContext loginContext = null;
            if (appName == null) {
                appName = "Tomcat";
            }

            log.log(FINE, "Authenticating {0} to application {1}", new Object[] {username, appName});

            // What if the LoginModule is in the container class loader ?
            //
            ClassLoader ocl = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            try {
                loginContext = new LoginContext(appName, new JAASCallbackHandler(this, username, credentials));
            } catch (Throwable e) {
                if (log.isLoggable(FINE)) {
                    String msg = MessageFormat.format(rb.getString(LogFacade.LOGIN_EXCEPTION_AUTHENTICATING_USERNAME),
                            username);
                    log.log(FINE, msg, e);
                }
                return null;
            } finally {
                Thread.currentThread().setContextClassLoader(ocl);
            }

            log.log(FINE, "Login context created {0}", username);

            // Negotiate a login via this LoginContext
            Subject subject = null;
            try {
                loginContext.login();
                subject = loginContext.getSubject();
                if (subject == null) {
                    log.log(FINE, LogFacade.USERNAME_NOT_AUTHENTICATED_FAILED_LOGIN, username);
                    return null;
                }
            } catch (AccountExpiredException e) {
                log.log(FINE, LogFacade.USERNAME_NOT_AUTHENTICATED_EXPIRED_ACCOUNT, username);
                return null;
            } catch (CredentialExpiredException e) {
                log.log(FINE, LogFacade.USERNAME_NOT_AUTHENTICATED_EXPIRED_CREDENTIAL, username);
                return null;
            } catch (FailedLoginException e) {
                log.log(FINE, LogFacade.USERNAME_NOT_AUTHENTICATED_FAILED_LOGIN, username);
                return null;
            } catch (LoginException e) {
                String msg = MessageFormat.format(rb.getString(LogFacade.LOGIN_EXCEPTION_AUTHENTICATING_USERNAME),
                    username);
                log.log(FINE, msg, e);
                return null;
            } catch (Throwable e) {
                log.log(FINE, "Unexpected error", e);
                return null;
            }

            log.log(FINE, "Getting principal {0}", subject);

            // Return the appropriate Principal for this authenticated Subject
            Principal principal = createPrincipal(username, subject);
            if (principal == null) {
                log.log(FINE, "Failed to authenticate username {0}", username);
                return null;
            }
            log.log(FINE, "Successful to authenticate username {0}", username);

            return (principal);
        } catch (Throwable t) {
            log.log(SEVERE, LogFacade.AUTHENTICATION_ERROR, t);
            return null;
        }
    }

    // -------------------------------------------------------- Package Methods

    // ------------------------------------------------------ Protected Methods

    @Override
    protected String getName() {
        return name;
    }

    /**
     * @return always null
     */
    @Override
    protected char[] getPassword(String username) {
        return null;
    }

    /**
     * @return always null
     */
    @Override
    protected Principal getPrincipal(String username) {
        return null;
    }

    /**
     * Construct and return a <code>java.security.Principal</code> instance representing the authenticated user for the
     * specified Subject. If no such Principal can be constructed, return <code>null</code>.
     *
     * @param subject The Subject representing the logged in user
     */
    protected Principal createPrincipal(String username, Subject subject) {
        // Prepare to scan the Principals for this Subject
        ArrayList<String> roles = new ArrayList<>();

        for (Principal principal : subject.getPrincipals()) {
            // No need to look further - that's our own stuff
            if (principal instanceof GenericPrincipal) {
                log.log(FINE, "Found old GenericPrincipal {0}", principal);
                return principal;
            }
            String principalClass = principal.getClass().getName();
            log.log(FINE, "Principal: {0} {1}", new Object[] {principalClass, principal});

            if (userClasses.contains(principalClass)) {
                // Override the default - which is the original user, accepted by
                // the friendly LoginManager
                username = principal.getName();
            }
            if (roleClasses.contains(principalClass)) {
                roles.add(principal.getName());
            }
            // Same as Jboss - that's a pretty clean solution
            if ((principal instanceof GroupPrincipal) && "Roles".equals(principal.getName())) {
                GroupPrincipal grp = (GroupPrincipal) principal;
                Enumeration en = grp.members();
                while (en.hasMoreElements()) {
                    Principal roleP = (Principal) en.nextElement();
                    roles.add(roleP.getName());
                }

            }
        }

        // Create the resulting Principal for our authenticated user
        if (username != null) {
            // Set password null as it will not be carried forward
            return new GenericPrincipal(this, username, null, roles);
        } else {
            return null;
        }
    }
}
