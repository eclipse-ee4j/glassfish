/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.security.Principal;
import java.security.cert.X509Certificate;

import org.apache.catalina.deploy.SecurityConstraint;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Contract;

/**
 * A <b>Realm</b> is a read-only facade for an underlying security realm
 * used to authenticate individual users, and identify the security roles
 * associated with those users. Realms can be attached at any Container
 * level, but will typically only be attached to a Context, or higher level,
 * Container.
 *
 * @author Craig R. McClanahan
 */
@Contract
@PerLookup
public interface Realm {

    /**
     * Flag indicating authentication is needed for current request. Used by
     * preAuthenticateCheck method.
     */
    int AUTHENTICATE_NEEDED = 1;

    /**
     * Flag indicating authentication is not needed for current request. Used
     * by preAuthenticateCheck method.
     */
    int AUTHENTICATE_NOT_NEEDED = 0;

    /**
     * Flag indicating the user has been authenticated but been denied access
     * to the requested resource.
     */
    int AUTHENTICATED_NOT_AUTHORIZED = -1;

    /**
     * @return the Container with which this Realm has been associated.
     */
    Container getContainer();

    /**
     * Set the Container with which this Realm has been associated.
     *
     * @param container The associated Container
     */
    void setContainer(Container container);

    /**
     * @return descriptive information about this Realm implementation and
     *         the corresponding version number, in the format
     *         <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    String getInfo();

    /**
     * Add a property change listener to this component.
     *
     * @param listener The listener to add
     */
    void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Authenticates and sets the SecurityContext in the TLS.
     *
     * @param username Username of the Principal to look up
     * @param credentials Password or other credentials to use in
     *            authenticating this username
     * @return the Principal associated with the specified username and
     *         credentials, if there is one; otherwise return <code>null</code>.
     */
    Principal authenticate(HttpRequest request, String username, char[] credentials);

    /**
     * Authenticates and sets the SecurityContext in the TLS.
     *
     * @param username Username of the Principal to look up
     * @param digest Digest which has been submitted by the client
     * @param nonce Unique (or supposedly unique) token which has been used
     *            for this request
     * @param realm Realm name
     * @param md5a2 Second MD5 digest used to calculate the digest :
     *            MD5(Method + ":" + uri)
     * @return the Principal associated with the specified username, which
     *         matches the digest calculated using the given parameters using the
     *         method described in RFC 2069; otherwise return <code>null</code>.
     */
    Principal authenticate(String username, char[] digest, String nonce, String nc, String cnonce, String qop,
        String realm, char[] md5a2);

    /**
     * Authenticates and sets the SecurityContext in the TLS.
     *
     * @param certs Array of client certificates, with the first one in
     *            the array being the certificate of the client itself.
     * @return the Principal associated with the specified chain of X509
     *         client certificates. If there is none, return <code>null</code>.
     */
    Principal authenticate(HttpRequest request, X509Certificate[] certs);

    /**
     * @param request Request we are processing
     * @return the SecurityConstraints configured to guard the request URI for
     *         this request, or <code>null</code> if there is no such constraint.
     */
    SecurityConstraint[] findSecurityConstraints(HttpRequest request, Context context);

    /**
     * Gets the security constraints configured by the given context
     * for the given request URI and method.
     *
     * @param uri the request URI
     * @param method the request method
     * @param context the context
     * @return the security constraints configured by the given context
     *         for the given request URI and method, or null
     */
    SecurityConstraint[] findSecurityConstraints(String uri, String method, Context context);

    /**
     * Perform access control based on the specified authorization constraint.
     * Return <code>true</code> if this constraint is satisfied and processing
     * should continue, or <code>false</code> otherwise.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param constraint Security constraint we are enforcing
     * @param context Context to which client of this class is attached.
     * @throws IOException if an input/output error occurs
     */
    boolean hasResourcePermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraint,
        Context context) throws IOException;

    /**
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     * @return <code>true</code> if the specified Principal has the specified
     *         security role, within the context of this Realm; otherwise return
     *         <code>false</code>.
     */
    boolean hasRole(Principal principal, String role);

    /**
     * @param request Request we are processing
     * @param response Response we are creating
     * @param principal Principal for whom the role is to be checked
     * @param role Security role to be checked
     * @return <code>true</code> if the specified Principal has the specified
     *         security role, within the context of this Realm; otherwise return
     *         <code>false</code>.
     */
    boolean hasRole(HttpRequest request, HttpResponse response, Principal principal, String role);

    /**
     * Checks whether or not authentication is needed.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param constraints Security constraint we are enforcing
     * @param disableProxyCaching whether or not to disable proxy caching for
     *            protected resources.
     * @param securePagesWithPragma true if we add headers which
     *            are incompatible with downloading office documents in IE under SSL but
     *            which fix a caching problem in Mozill
     * @param ssoEnabled true if sso is enabled
     * @return an int, one of AUTHENTICATE_NOT_NEEDED, AUTHENTICATE_NEEDED,
     *         or AUTHENTICATED_NOT_AUTHORIZED.
     * @throws IOException if an input/output error occurs
     */
    int preAuthenticateCheck(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints,
        boolean disableProxyCaching, boolean securePagesWithPragma, boolean ssoEnabled) throws IOException;

    /**
     * Authenticates the user making this request, based on the specified
     * login configuration.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param context The Context to which client of this class is attached.
     * @param authenticator the current authenticator.
     * @param calledFromAuthenticate true if the call originates from
     *            HttpServletRequest.authenticate
     * @return <code>true</code> if any specified
     *         requirements have been satisfied, or <code>false</code> if we have
     *         created a response challenge already.
     * @throws IOException if an input/output error occurs
     */
    boolean invokeAuthenticateDelegate(HttpRequest request, HttpResponse response, Context context,
        Authenticator authenticator, boolean calledFromAuthenticate) throws IOException;

    /**
     * Post authentication for given request and response.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param context The Context to which client of this class is attached.
     * @throws IOException if an input/output error occurs
     */
    boolean invokePostAuthenticateDelegate(HttpRequest request, HttpResponse response, Context context)
        throws IOException;

    /**
     * Enforce any user data constraint required by the security constraint
     * guarding this request URI.
     *
     * @param request Request we are processing
     * @param response Response we are creating
     * @param constraint Security constraint being checked
     * @return <code>true</code> if this constraint
     *         was not violated and processing should continue, or <code>false</code>
     *         if we have created a response already.
     * @throws IOException if an input/output error occurs
     */
    boolean hasUserDataPermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraint)
        throws IOException;

    /**
     * Checks if the given request URI and method are the target of any
     * user-data-constraint with a transport-guarantee of CONFIDENTIAL,
     * and whether any such constraint is already satisfied.
     *
     * If <tt>uri</tt> and <tt>method</tt> are null, then the URI and method
     * of the given <tt>request</tt> are checked.
     *
     * If a user-data-constraint exists that is not satisfied, then the
     * given <tt>request</tt> will be redirected to HTTPS.
     *
     * @param request the request that may be redirected
     * @param response the response that may be redirected
     * @param constraints the security constraints to check against
     * @param uri the request URI (minus the context path) to check
     * @param method the request method to check
     * @return true if the request URI and method are not the target of any
     *         unsatisfied user-data-constraint with a transport-guarantee of
     *         CONFIDENTIAL, and false if they are (in which case the given request
     *         will have been redirected to HTTPS)
     * @throws IOException if an input/output error occurs
     */
    boolean hasUserDataPermission(HttpRequest request, HttpResponse response, SecurityConstraint[] constraints,
        String uri, String method) throws IOException;

    /**
     * Remove a property change listener from this component.
     *
     * @param listener The listener to remove
     */
    void removePropertyChangeListener(PropertyChangeListener listener);

    /**
     * Return an alternate principal from the request if available.
     *
     * @param req The request object.
     * @return Alternate principal or null.
     */
    Principal getAlternatePrincipal(HttpRequest req);

    /**
     * Return an alternate auth type from the request if available.
     *
     * @param req The request object.
     * @return Alternate auth type or null.
     */
    String getAlternateAuthType(HttpRequest req);

    /**
     * Set the name of the associated realm.
     *
     * @param name the name of the realm.
     */
    void setRealmName(String name, String authMethod);

    /**
     * Returns the name of the associated realm.
     *
     * @return realm name or null if not set.
     */
    String getRealmName();

    /**
     * Does digest authentication and returns the Principal associated with the username in the
     * HTTP header.
     *
     * @param hreq HTTP servlet request.
     * @return {@link Principal} associated with the username in the HTTP header.
     */
    Principal authenticate(HttpServletRequest hreq);

    /**
     * Returns whether the specified ServletContext indicates that security
     * extension is enabled.
     *
     * @param servletContext the ServletContext
     * @return true if security extension is enabled; false otherwise
     */
    boolean isSecurityExtensionEnabled(ServletContext servletContext);

    /**
     * Logs out.
     *
     * @param hreq the HttpRequest
     */
    void logout(HttpRequest hreq);

}
