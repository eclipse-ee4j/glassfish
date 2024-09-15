/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.util.Utility;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ObjectName;

import org.apache.catalina.Authenticator;
import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;
import org.apache.catalina.LogFacade;
import org.apache.catalina.Realm;
import org.apache.catalina.authenticator.AuthenticatorBase;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.catalina.util.HexUtils;
import org.apache.catalina.util.LifecycleSupport;

import static com.sun.enterprise.util.Utility.isEmpty;
import static com.sun.logging.LogCleanerUtil.neutralizeForLog;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static org.apache.catalina.LogFacade.ACCESS_RESOURCE_DENIED;
import static org.apache.catalina.LogFacade.USERNAME_HAS_ROLE;
import static org.apache.catalina.LogFacade.USERNAME_NOT_HAVE_ROLE;

/**
 * Simple implementation of <b>Realm</b> that reads an XML file to configure the valid users, passwords, and roles. The
 * file format (and default file location) are identical to those currently supported by Tomcat 3.X.
 *
 * @author Craig R. McClanahan
 */

public abstract class RealmBase implements Lifecycle, Realm {

    protected static final Logger log = LogFacade.getLogger();
    protected static final ResourceBundle rb = log.getResourceBundle();

    /**
     * "Expires" header always set to Date(1), so generate once only
     */
    private static final String DATE_ONE = (new SimpleDateFormat(Response.HTTP_RESPONSE_DATE_HEADER, Locale.US)).format(new Date(1));

    // ----------------------------------------------------- Instance Variables

    /**
     * The debugging detail level for this component.
     */
    protected int debug = 0;

    /**
     * The Container with which this Realm is associated.
     */
    protected Container container;

    /**
     * Flag indicating whether a check to see if the request is secure is required before adding Pragma and Cache-Control
     * headers when proxy caching has been disabled
     */
    protected boolean checkIfRequestIsSecure;

    /**
     * Digest algorithm used in storing passwords in a non-plaintext format. Valid values are those accepted for the
     * algorithm name by the MessageDigest class, or <code>null</code> if no digesting should be performed.
     */
    protected String digest;

    /**
     * The encoding charset for the digest.
     */
    protected String digestEncoding;

    /**
     * Descriptive information about this Realm implementation.
     */
    protected static final String info = "org.apache.catalina.realm.RealmBase/1.0";

    /**
     * The lifecycle event support for this component.
     */
    protected LifecycleSupport lifecycle = new LifecycleSupport(this);

    /**
     * The MessageDigest object for digesting user credentials (passwords).
     */
    protected volatile MessageDigest md;

    /**
     * SHA-256 message digest provider.
     */
    protected static volatile MessageDigest sha256Helper;

    /**
     * Has this component been started?
     */
    protected boolean started;

    /**
     * The property change support for this component.
     */
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    /**
     * Should we validate client certificate chains when they are presented?
     */
    protected boolean validate = true;

    // ------------------------------------------------------------- Properties

    @Override
    public Container getContainer() {
        return container;
    }

    /**
     * @return the debugging detail level for this component.
     */
    public int getDebug() {
        return debug;
    }

    /**
     * Set the debugging detail level for this component.
     *
     * @param debug The new debugging detail level
     */
    public void setDebug(int debug) {
        this.debug = debug;
    }

    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    @Override
    public void setContainer(Container container) {
        Container oldContainer = this.container;
        this.container = container;
        this.checkIfRequestIsSecure = container.isCheckIfRequestIsSecure();
        support.firePropertyChange("container", oldContainer, this.container);
    }

    /**
     * @return the digest algorithm used for storing credentials.
     */
    public String getDigest() {
        return digest;
    }

    /**
     * Set the digest algorithm used for storing credentials.
     *
     * @param digest The new digest algorithm
     */
    public void setDigest(String digest) {
        this.digest = digest;
    }

    /**
     * Returns the digest encoding charset.
     *
     * @return The charset (may be null) for {@link StandardCharsets#UTF_8} to use.
     */
    public String getDigestEncoding() {
        return digestEncoding;
    }

    /**
     * Sets the digest encoding charset.
     *
     * @param charset The charset (null for platform default)
     */
    public void setDigestEncoding(String charset) {
        digestEncoding = charset;
    }

    @Override
    public String getInfo() {
        return info;
    }

    /**
     * @return the "validate certificate chains" flag.
     */
    public boolean getValidate() {
        return validate;
    }

    /**
     * Set the "validate certificate chains" flag.
     *
     * @param validate The new validate certificate chains flag
     */
    public void setValidate(boolean validate) {
        this.validate = validate;
    }


    // --------------------------------------------------------- Public Methods

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public Principal authenticate(HttpRequest request, String username, char[] credentials) {
        char[] serverCredentials = getPassword(username);

        boolean validated;
        if (serverCredentials == null) {
            validated = false;
        } else if (hasMessageDigest()) {
            validated = equalsIgnoreCase(serverCredentials, digest(credentials));
        } else {
            validated = Arrays.equals(serverCredentials, credentials);
        }

        if (!validated) {
            return null;
        }

        return getPrincipal(username);
    }

    @Override
    public Principal authenticate(String username, char[] clientDigest, String nOnce, String nc, String cnonce, String qop, String realm, char[] md5a2) {
        char[] md5a1 = getDigest(username, realm);
        if (md5a1 == null) {
            return null;
        }

        int nOnceLength = ((nOnce != null) ? nOnce.length() : 0);
        int ncLength = ((nc != null) ? nc.length() : 0);
        int cnonceLength = ((cnonce != null) ? cnonce.length() : 0);
        int qopLength = ((qop != null) ? qop.length() : 0);
        int md5a2Length = ((md5a2 != null) ? md5a2.length : 0);

        // serverDigestValue = md5a1:nOnce:nc:cnonce:qop:md5a2
        char[] serverDigestValue =
            new char[md5a1.length + 1 + nOnceLength + 1 + ncLength + 1 + cnonceLength + 1 + qopLength + 1 + md5a2Length];

        System.arraycopy(md5a1, 0, serverDigestValue, 0, md5a1.length);
        int ind = md5a1.length;
        serverDigestValue[ind++] = ':';
        if (nOnce != null) {
            System.arraycopy(nOnce.toCharArray(), 0, serverDigestValue, ind, nOnceLength);
            ind += nOnceLength;
        }
        serverDigestValue[ind++] = ':';
        if (nc != null) {
            System.arraycopy(nc.toCharArray(), 0, serverDigestValue, ind, ncLength);
            ind += ncLength;
        }

        serverDigestValue[ind++] = ':';
        if (cnonce != null) {
            System.arraycopy(cnonce.toCharArray(), 0, serverDigestValue, ind, cnonceLength);
            ind += cnonceLength;
        }

        serverDigestValue[ind++] = ':';
        if (qop != null) {
            System.arraycopy(qop.toCharArray(), 0, serverDigestValue, ind, qopLength);
            ind += qopLength;
        }

        serverDigestValue[ind++] = ':';
        if (md5a2 != null) {
            System.arraycopy(md5a2, 0, serverDigestValue, ind, md5a2Length);
        }

        byte[] valueBytes = null;

        try {
            Charset charset = Utility.getCharset(getDigestEncoding());
            valueBytes = Utility.convertCharArrayToByteArray(serverDigestValue, charset);
        } catch (CharacterCodingException cce) {
            String msg = MessageFormat.format(rb.getString(LogFacade.ILLEGAL_DIGEST_ENCODING_EXCEPTION), getDigestEncoding());
            log.log(Level.SEVERE, msg, cce);
            throw new IllegalArgumentException(cce.getMessage());
        }

        char[] serverDigest = null;

        synchronized (sha256Helper) {
            serverDigest = new String(sha256Helper.digest(valueBytes)).toCharArray();
        }

        if (log.isLoggable(FINE)) {
            log.log(FINE,
                "Username:" + username +
                " ClientSigest:" + Arrays.toString(clientDigest) +
                " nOnce:" + nOnce +
                " nc:" + nc +
                " cnonce:" + cnonce +
                " qop:" + qop +
                " realm:" + realm +
                "md5a2:" + Arrays.toString(md5a2) +
                " Server digest:" + String.valueOf(serverDigest));
        }

        if (!Arrays.equals(serverDigest, clientDigest)) {
            return null;
        }

        return getPrincipal(username);
    }

    @Override
    public Principal authenticate(HttpRequest request, X509Certificate clientCertificates[]) {
        if (isEmpty(clientCertificates)) {
            return (null);
        }

        // Check the validity of each certificate in the chain
        log.log(FINE, "Authenticating client certificate chain");

        if (validate) {
            for (X509Certificate clientCertificate : clientCertificates) {
                log.log(FINE, () -> "Checking validity for '" + clientCertificate.getSubjectX500Principal().getName() + "'");

                try {
                    clientCertificate.checkValidity();
                } catch (Exception e) {
                    log.log(FINE, "Validity exception", e);
                    return null;
                }
            }
        }

        // Check the existence of the client Principal in our database
        return getPrincipal(clientCertificates[0].getSubjectX500Principal().getName());
    }

    /**
     * Execute a periodic task, such as reloading, etc. This method will be invoked inside the classloading context of this
     * container. Unexpected throwables will be caught and logged.
     */
    public void backgroundProcess() {
    }

    @Override
    public SecurityConstraint[] findSecurityConstraints(HttpRequest request, Context context) {
        return findSecurityConstraints(
                    request.getRequestPathMB().toString(),
                    ((HttpServletRequest) request.getRequest()).getMethod(),
                    context);
    }

    @Override
    public SecurityConstraint[] findSecurityConstraints(String uri, String method, Context context) {
        List<SecurityConstraint> results = null;

        // Are there any defined security constraints?
        if (!context.hasConstraints()) {
            log.log(FINE, "  No applicable constraints defined");
            return (null);
        }

        String origUri = uri;
        boolean caseSensitiveMapping = ((StandardContext) context).isCaseSensitiveMapping();
        if (uri != null && !caseSensitiveMapping) {
            uri = uri.toLowerCase(Locale.ENGLISH);
        }

        boolean found = false;

        List<SecurityConstraint> constraints = context.getConstraints();
        Iterator<SecurityConstraint> i = constraints.iterator();
        while (i.hasNext()) {
            SecurityConstraint constraint = i.next();
            SecurityCollection[] collection = constraint.findCollections();

            // If collection is null, continue to avoid an NPE
            // See Bugzilla 30624
            if (collection == null) {
                continue;
            }

            if (log.isLoggable(FINEST)) {
                log.log(FINEST,
                    "Checking constraint '" + constraint + "' against " + method + " " + origUri +
                    " --> " + constraint.included(uri, method, caseSensitiveMapping));
            }

            if (log.isLoggable(FINE) && constraint.included(uri, method, caseSensitiveMapping)) {
                log.log(FINE, "  Matched constraint '" + constraint + "' against " + method + " " + origUri);
            }

            for (SecurityCollection element : collection) {
                String[] patterns = element.findPatterns();
                // If patterns is null, continue to avoid an NPE
                // See Bugzilla 30624
                if (patterns == null) {
                    continue;
                }

                for (String pattern2 : patterns) {
                    String pattern = caseSensitiveMapping ? pattern2 : pattern2.toLowerCase(Locale.ENGLISH);
                    if (uri != null && uri.equals(pattern)) {
                        found = true;
                        if (element.findMethod(method)) {
                            if (results == null) {
                                results = new ArrayList<>();
                            }
                            results.add(constraint);
                        }
                    }
                }
            }
        } // while

        if (found) {
            return resultsToArray(results);
        }

        int longest = -1;

        i = constraints.iterator();
        while (i.hasNext()) {
            SecurityConstraint constraint = i.next();
            SecurityCollection[] collection = constraint.findCollections();

            // If collection is null, continue to avoid an NPE
            // See Bugzilla 30624
            if (collection == null) {
                continue;
            }

            if (log.isLoggable(FINEST)) {
                log.log(FINE, "  Checking constraint '" + constraint + "' against " + method + " " + origUri + " --> "
                        + constraint.included(uri, method, caseSensitiveMapping));
            }

            if (log.isLoggable(FINE) && constraint.included(uri, method, caseSensitiveMapping)) {
                log.log(FINE, "  Matched constraint '" + constraint + "' against " + method + " " + origUri);
            }

            for (SecurityCollection element : collection) {
                String[] patterns = element.findPatterns();
                // If patterns is null, continue to avoid an NPE
                // See Bugzilla 30624
                if (patterns == null) {
                    continue;
                }

                boolean matched = false;
                int length = -1;
                for (String pattern2 : patterns) {
                    String pattern = caseSensitiveMapping ? pattern2 : pattern2.toLowerCase(Locale.ENGLISH);
                    if (pattern.startsWith("/") && pattern.endsWith("/*") && pattern.length() >= longest) {

                        if (pattern.length() == 2) {
                            matched = true;
                            length = pattern.length();
                        } else if (uri != null && (pattern.regionMatches(0, uri, 0, pattern.length() - 1) || (pattern.length() - 2 == uri.length() && pattern.regionMatches(0, uri, 0, pattern.length() - 2)))) {
                            matched = true;
                            length = pattern.length();
                        }
                    }
                }

                if (matched) {
                    found = true;
                    if (length > longest) {
                        if (results != null) {
                            results.clear();
                        }
                        longest = length;
                    }

                    if (element.findMethod(method)) {
                        if (results == null) {
                            results = new ArrayList<>();
                        }
                        results.add(constraint);
                    }
                }
            }
        } // while

        if (found) {
            return resultsToArray(results);
        }

        i = constraints.iterator();
        while (i.hasNext()) {
            SecurityConstraint constraint = i.next();
            SecurityCollection[] collection = constraint.findCollections();

            // If collection is null, continue to avoid an NPE
            // See Bugzilla 30624
            if (collection == null) {
                continue;
            }

            if (log.isLoggable(FINEST)) {
                String msg = "  Checking constraint '" + constraint + "' against " + method + " " + origUri + " --> "
                        + constraint.included(uri, method, caseSensitiveMapping);
                log.log(FINEST, msg);
            }

            if (log.isLoggable(FINE) && constraint.included(uri, method, caseSensitiveMapping)) {
                log.log(FINE, "  Matched constraint '" + constraint + "' against " + method + " " + origUri);
            }

            boolean matched = false;
            int pos = -1;
            for (int j = 0; j < collection.length; j++) {
                String[] patterns = collection[j].findPatterns();
                // If patterns is null, continue to avoid an NPE
                // See Bugzilla 30624
                if (patterns == null) {
                    continue;
                }

                for (int k = 0; k < patterns.length && !matched; k++) {
                    String pattern = caseSensitiveMapping ? patterns[k] : patterns[k].toLowerCase(Locale.ENGLISH);
                    if (uri != null && pattern.startsWith("*.")) {
                        int slash = uri.lastIndexOf("/");
                        int dot = uri.lastIndexOf(".");
                        if (slash >= 0 && dot > slash && dot != uri.length() - 1 && uri.length() - dot == pattern.length() - 1) {
                            if (pattern.regionMatches(1, uri, dot, uri.length() - dot)) {
                                matched = true;
                                pos = j;
                            }
                        }
                    }
                }
            }

            if (matched) {
                found = true;
                if (collection[pos].findMethod(method)) {
                    if (results == null) {
                        results = new ArrayList<>();
                    }
                    results.add(constraint);
                }
            }
        } // while

        if (found) {
            return resultsToArray(results);
        }

        i = constraints.iterator();
        while (i.hasNext()) {
            SecurityConstraint constraint = i.next();
            SecurityCollection[] collection = constraint.findCollections();

            // If collection is null, continue to avoid an NPE
            // See Bugzilla 30624
            if (collection == null) {
                continue;
            }

            if (log.isLoggable(FINEST)) {
                String msg = "  Checking constraint '" + constraint + "' against " + method + " " + origUri + " --> "
                        + constraint.included(uri, method, caseSensitiveMapping);
                log.log(FINEST, msg);
            }


            if (log.isLoggable(FINE) && constraint.included(uri, method, caseSensitiveMapping)) {
                log.log(FINE, "  Matched constraint '" + constraint + "' against " + method + " " + origUri);
            }

            for (SecurityCollection element : collection) {
                String[] patterns = element.findPatterns();

                // If patterns is null, continue to avoid an NPE
                // See Bugzilla 30624
                if (patterns == null) {
                    continue;
                }

                boolean matched = false;
                for (int k = 0; k < patterns.length && !matched; k++) {
                    String pattern = caseSensitiveMapping ? patterns[k] : patterns[k].toLowerCase(Locale.ENGLISH);
                    if (pattern.equals("/")) {
                        matched = true;
                    }
                }
                if (matched) {
                    if (results == null) {
                        results = new ArrayList<>();
                    }
                    results.add(constraint);
                }
            }
        } // while

        if (results == null) {
            // No applicable security constraint was found
            if (log.isLoggable(FINE)) {
                log.log(FINE, "  No applicable constraint located");
            }
        }

        return resultsToArray(results);
    }

    /**
     * Convert an ArrayList to a SecurityContraint [].
     */
    private SecurityConstraint[] resultsToArray(List<SecurityConstraint> results) {
        if (results == null) {
            return null;
        }

        SecurityConstraint[] array = new SecurityConstraint[results.size()];
        results.toArray(array);
        return array;
    }

    @Override
    public boolean hasResourcePermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints, Context context) throws IOException {
        if (isEmpty(constraints)) {
            return true;
        }

        // Which user principal have we already authenticated?
        Principal principal = ((HttpServletRequest) request.getRequest()).getUserPrincipal();
        for (SecurityConstraint constraint : constraints) {
            String roles[] = constraint.findAuthRoles();
            if (roles == null) {
                roles = new String[0];
            }

            if (constraint.getAllRoles()) {
                return (true);
            }

            if (log.isLoggable(FINE)) {
                log.log(FINE, "  Checking roles " + principal);
            }

            if (roles.length == 0) {
                if (constraint.getAuthConstraint()) {

                    ((HttpServletResponse) response.getResponse()).sendError(SC_FORBIDDEN);
                    response.setDetailMessage(rb.getString(ACCESS_RESOURCE_DENIED));

                    log.log(FINE, "No roles ");
                    return false; // No listed roles means no access at all
                } else {
                    log.log(FINE, "Passing all access");
                    return true;
                }
            } else if (principal == null) {
                log.log(FINE, "  No user authenticated, cannot grant access");

                ((HttpServletResponse) response.getResponse()).sendError(SC_FORBIDDEN);
                response.setDetailMessage(rb.getString(LogFacade.CONFIG_ERROR_NOT_AUTHENTICATED));
                return false;
            }

            for (String role : roles) {
                if (hasRole(principal, role)) {
                    log.log(FINE, () -> "Role found:  " + role);
                    return true;
                } else {
                    log.log(FINE, () -> "No role found:  " + role);
                }
            }
        }
        // Return a "Forbidden" message denying access to this resource
        ((HttpServletResponse) response.getResponse()).sendError(SC_FORBIDDEN);
        response.setDetailMessage(rb.getString(ACCESS_RESOURCE_DENIED));

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * This method can be overridden by Realm implementations.
     * The default implementation is to forward to {@link #hasRole(Principal, String)}.
     */
    @Override
    public boolean hasRole(HttpRequest request, HttpResponse response, Principal principal, String role) {
        return hasRole(principal, role);
    }

    @Override
    public int preAuthenticateCheck(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints, boolean disableProxyCaching, boolean securePagesWithPragma, boolean ssoEnabled) throws IOException {
        for (SecurityConstraint constraint : constraints) {
            if (constraint.getAuthConstraint()) {
                disableProxyCaching(request, response, disableProxyCaching, securePagesWithPragma);
                return Realm.AUTHENTICATE_NEEDED;
            }
        }

        return Realm.AUTHENTICATE_NOT_NEEDED;
    }

    @Override
    public boolean invokeAuthenticateDelegate(HttpRequest request, HttpResponse response, Context context, Authenticator authenticator, boolean calledFromAuthenticate) throws IOException {
        LoginConfig config = context.getLoginConfig();
        return ((AuthenticatorBase) authenticator).authenticate(request, response, config);
    }

    @Override
    public boolean invokePostAuthenticateDelegate(HttpRequest request, HttpResponse response, Context context) throws IOException {
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * This method can be overridden by Realm implementations, but the default
     * is adequate when an instance of <code>GenericPrincipal</code> is used to represent authenticated Principals from this
     * Realm.
     */
    @Override
    public boolean hasRole(Principal principal, String role) {
        // Should be overridden in JAASRealm - to avoid pretty inefficient conversions
        if ((principal == null) || (role == null) || !(principal instanceof GenericPrincipal)) {
            return (false);
        }

        GenericPrincipal genericPrincipal = (GenericPrincipal) principal;
        if (!(genericPrincipal.getRealm() == this)) {
            log.log(FINE, () -> "Different realm " + this + " " + genericPrincipal.getRealm());
        }

        boolean result = genericPrincipal.hasRole(role);
        if (log.isLoggable(FINE)) {
            String name = principal.getName();
            if (result) {
                log.log(FINE, USERNAME_HAS_ROLE, new Object[] { name, role });
            } else {
                log.log(FINE, USERNAME_NOT_HAVE_ROLE, new Object[] { name, role });
            }
        }

        return result;
    }

    @Override
    public boolean hasUserDataPermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints) throws IOException {
        return hasUserDataPermission(request, response, constraints, null, null);
    }

    @Override
    public boolean hasUserDataPermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints, String uri, String method) throws IOException {
        // Is there a relevant user data constraint?
        if (constraints == null || constraints.length == 0) {
            log.log(FINE, "No applicable security constraint defined");
            return true;
        }

        for (SecurityConstraint constraint : constraints) {
            String userConstraint = constraint.getUserConstraint();
            if (userConstraint == null) {
                log.log(FINE, "No applicable user data constraint defined");
                return true;
            }

            if (userConstraint.equals(Constants.NONE_TRANSPORT)) {
                log.log(FINE, "User data constraint has no restrictions");
                return true;
            }

        }

        // Validate the request against the user data constraint
        if (request.getRequest().isSecure()) {
            if (log.isLoggable(FINE)) {
                log.log(FINE, "  User data constraint already satisfied");
            }
            return true;
        }

        // Initialize variables we need to determine the appropriate action
        HttpServletRequest hrequest = (HttpServletRequest) request.getRequest();
        HttpServletResponse hresponse = (HttpServletResponse) response.getResponse();
        int redirectPort = request.getConnector().getRedirectPort();

        // Is redirecting disabled?
        if (redirectPort <= 0) {
            log.log(FINE, "SSL redirect is disabled");

            hresponse.sendError(SC_FORBIDDEN);
            response.setDetailMessage(hrequest.getRequestURI());
            return false;
        }

        // Redirect to the corresponding SSL port
        StringBuilder file = new StringBuilder();
        String protocol = "https";
        String host = hrequest.getServerName();

        // Protocol
        file.append(protocol).append("://").append(host);

        // Host with port
        if (redirectPort != 443) {
            file.append(":").append(redirectPort);
        }
        // URI
        file.append(hrequest.getRequestURI());
        String requestedSessionId = hrequest.getRequestedSessionId();
        if ((requestedSessionId != null) && hrequest.isRequestedSessionIdFromURL()) {
            String sessionParameterName = ((request.getContext() != null) ? request.getContext().getSessionParameterName() : Globals.SESSION_PARAMETER_NAME);
            file.append(";" + sessionParameterName + "=");
            file.append(requestedSessionId);
        }

        String queryString = hrequest.getQueryString();
        if (queryString != null) {
            file.append('?');
            file.append(queryString);
        }
        log.log(FINE, "Redirecting to {0}", file);

        hresponse.sendRedirect(file.toString());

        return false;
    }

    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    // ------------------------------------------------------ Lifecycle Methods

    /**
     * Add a lifecycle event listener to this component.
     *
     * @param listener The listener to add
     */
    @Override
    public void addLifecycleListener(LifecycleListener listener) {
        lifecycle.addLifecycleListener(listener);
    }

    /**
     * Gets the (possibly empty) list of lifecycle listeners associated with this Realm.
     */
    @Override
    public List<LifecycleListener> findLifecycleListeners() {
        return lifecycle.findLifecycleListeners();
    }

    /**
     * Remove a lifecycle event listener from this component.
     *
     * @param listener The listener to remove
     */
    @Override
    public void removeLifecycleListener(LifecycleListener listener) {
        lifecycle.removeLifecycleListener(listener);
    }

    /**
     * Prepare for the beginning of active use of the public methods of this component. This method should be called before
     * any of the public methods of this component are utilized. It should also send a LifecycleEvent of type START_EVENT to
     * any registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error that prevents this component from being used
     */
    @Override
    public void start() throws LifecycleException {
        // Validate and update our current component state
        if (started) {
            log.log(FINE, LogFacade.REALM_BEEN_STARTED);
            return;
        }

        lifecycle.fireLifecycleEvent(START_EVENT, null);
        started = true;

        // Create a MessageDigest instance for credentials, if desired
        if (digest != null) {
            try {
                md = MessageDigest.getInstance(digest);
            } catch (NoSuchAlgorithmException e) {
                String msg = MessageFormat.format(rb.getString(LogFacade.INVALID_ALGORITHM_EXCEPTION), digest);
                throw new LifecycleException(msg, e);
            }
        }

    }

    /**
     * Gracefully terminate the active use of the public methods of this component. This method should be the last one
     * called on a given instance of this component. It should also send a LifecycleEvent of type STOP_EVENT to any
     * registered listeners.
     *
     * @exception LifecycleException if this component detects a fatal error that needs to be reported
     */
    @Override
    public void stop() throws LifecycleException {
        // Validate and update our current component state
        if (!started) {
            log.log(INFO, LogFacade.REALM_NOT_BEEN_STARTED);
            return;
        }

        lifecycle.fireLifecycleEvent(STOP_EVENT, null);
        started = false;

        // Clean up allocated resources
        md = null;

        destroy();

    }

    public void destroy() {
        // no op
    }

    @Override
    public void logout(HttpRequest hreq) {
        // no-op
    }

    @Override
    public boolean isSecurityExtensionEnabled(ServletContext servletContext) {
        return false;
    }



    // ------------------------------------------------------ Protected Methods

    /**
     * Digest the password using the specified algorithm and convert the result to a corresponding hexadecimal string. If
     * exception, the plain credentials string is returned.
     *
     * @param credentials Password or other credentials to use in authenticating this username
     */
    protected char[] digest(char[] credentials) {
        // If no MessageDigest instance is specified, return unchanged
        if (!hasMessageDigest()) {
            return (credentials);
        }

        // Digest the user credentials and return as hexadecimal
        synchronized (this) {
            try {
                md.reset();

                byte[] bytes = null;
                try {
                    Charset charset = Utility.getCharset(getDigestEncoding());
                    bytes = Utility.convertCharArrayToByteArray(credentials, charset);
                } catch (CharacterCodingException cce) {
                    String msg = MessageFormat.format(rb.getString(LogFacade.ILLEGAL_DIGEST_ENCODING_EXCEPTION), getDigestEncoding());
                    log.log(Level.SEVERE, msg, cce);
                    throw new IllegalArgumentException(cce.getMessage());
                }
                md.update(bytes);

                return (HexUtils.convert(md.digest()));
            } catch (Exception e) {
                log.log(Level.SEVERE, LogFacade.ERROR_DIGESTING_USER_CREDENTIAL_EXCEPTION, e);
                return (credentials);
            }
        }

    }

    protected boolean hasMessageDigest() {
        return !(md == null);
    }

    /**
     * Return the digest associated with given principal's user name.
     */
    protected char[] getDigest(String username, String realmName) {
        if (sha256Helper == null) {
            try {
                sha256Helper = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                log.log(Level.SEVERE, LogFacade.CANNOT_GET_MD5_DIGEST_EXCEPTION, e);
                throw new IllegalStateException(e.getMessage());
            }
        }

        if (hasMessageDigest()) {
            // Use pre-generated digest
            return getPassword(username);
        }

        char[] pwd = getPassword(username);
        int usernameLength = ((username != null) ? username.length() : 0);
        int realmNameLength = ((realmName != null) ? realmName.length() : 0);
        int pwdLength = ((pwd != null) ? pwd.length : 0);

        // digestValue = username:realmName:pwd
        char[] digestValue = new char[usernameLength + 1 + realmNameLength + 1 + pwdLength];
        int ind = 0;
        if (username != null) {
            System.arraycopy(username.toCharArray(), 0, digestValue, 0, usernameLength);
            ind = usernameLength;
        }
        digestValue[ind++] = ':';
        if (realmName != null) {
            System.arraycopy(realmName.toCharArray(), 0, digestValue, ind, realmNameLength);
            ind += realmNameLength;
        }
        digestValue[ind++] = ':';
        if (pwd != null) {
            System.arraycopy(pwd, 0, digestValue, ind, pwdLength);
        }

        byte[] valueBytes = null;
        try {
            Charset charset = Utility.getCharset(getDigestEncoding());
            valueBytes = Utility.convertCharArrayToByteArray(digestValue, charset);
        } catch (CharacterCodingException cce) {
            String msg = MessageFormat.format(rb.getString(LogFacade.ILLEGAL_DIGEST_ENCODING_EXCEPTION), getDigestEncoding());
            log.log(Level.SEVERE, msg, cce);
            throw new IllegalArgumentException(cce.getMessage(), cce);
        }

        byte[] digest = null;
        // Bugzilla 32137
        synchronized (sha256Helper) {
            digest = sha256Helper.digest(valueBytes);
        }

        return new String(digest).toCharArray();
    }

    /**
     * @return a short name for this Realm implementation, for use in log messages.
     */
    protected abstract String getName();

    /**
     * @return the password associated with the given principal's user name.
     */
    protected abstract char[] getPassword(String username);

    /**
     * @return the Principal associated with the given user name.
     */
    protected abstract Principal getPrincipal(String username);

    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        message = neutralizeForLog(message);
        org.apache.catalina.Logger logger = null;
        String name = null;
        if (container != null) {
            logger = container.getLogger();
            name = container.getName();
        }

        if (logger != null) {
            logger.log(getName() + "[" + name + "]: " + message);
        } else {
            if (log.isLoggable(INFO)) {
                log.log(INFO, getName() + "[" + name + "]: " + message);
            }
        }
    }

    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param t Associated exception
     */
    protected void log(String message, Throwable t) {
        message = neutralizeForLog(message);
        org.apache.catalina.Logger logger = null;
        String name = null;
        if (container != null) {
            logger = container.getLogger();
            name = container.getName();
        }

        if (logger != null) {
            logger.log(getName() + "[" + name + "]: " + message, t, org.apache.catalina.Logger.WARNING);
        } else {
            log.log(Level.WARNING, getName() + "[" + name + "]: " + message, t);
        }
    }

    protected void disableProxyCaching(HttpRequest request, HttpResponse response, boolean disableProxyCaching, boolean securePagesWithPragma) {
        HttpServletRequest hsrequest = (HttpServletRequest) request.getRequest();

        // Make sure that constrained resources are not cached by web proxies
        // or browsers as caching can provide a security hole
        if (disableProxyCaching && !"POST".equalsIgnoreCase(hsrequest.getMethod()) && (!checkIfRequestIsSecure || !hsrequest.isSecure())) {
            HttpServletResponse sresponse = (HttpServletResponse) response.getResponse();
            if (securePagesWithPragma) {
                // FIXME: These cause problems with downloading office docs
                // from IE under SSL and may not be needed for newer Mozilla
                // clients.
                sresponse.setHeader("Pragma", "No-cache");
                sresponse.setHeader("Cache-Control", "no-cache");
            } else {
                sresponse.setHeader("Cache-Control", "private");
            }
            sresponse.setHeader("Expires", DATE_ONE);
        }
    }


    // -------------------- JMX and Registration --------------------

    protected ObjectName controller;

    public ObjectName getController() {
        return controller;
    }

    public void setController(ObjectName controller) {
        this.controller = controller;
    }

    /**
     * @return always null.
     */
    @Override
    public Principal getAlternatePrincipal(HttpRequest req) {
        return null;
    }

    /**
     * @return always null.
     */
    @Override
    public String getAlternateAuthType(HttpRequest req) {
        return null;
    }

    /**
     * Doesn't do anything
     */
    @Override
    public void setRealmName(String name, String authMethod) {
        // DO NOTHING. PRIVATE EXTENSION
    }

    /**
     * @return always null
     */
    @Override
    public String getRealmName() {
        // DO NOTHING. PRIVATE EXTENSION
        return null;
    }

    private boolean equalsIgnoreCase(char[] arr1, char[] arr2) {
        if (arr1 == null) {
            return (arr2 == null);
        } else { // arr1 is not null
            if (arr2 == null || arr1.length != arr2.length) {
                return false;
            }
        }

        // here, arr1 and arr2 are not null with equal length
        boolean result = true;
        for (int i = 0; i < arr1.length; i++) {
            if (Character.toLowerCase(arr1[i]) != Character.toLowerCase(arr2[i])) {
                return false;
            }
        }

        return result;
    }
}
