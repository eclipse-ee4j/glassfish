/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
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

package org.apache.catalina;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Iterator;

/**
 * A <b>Request</b> is the Catalina-internal facade for a
 * <code>ServletRequest</code> that is to be processed, in order to
 * produce the corresponding <code>Response</code>.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.4 $ $Date: 2006/04/24 16:36:12 $
 */

public interface Request {


    // ------------------------------------------------------------- Properties


    /**
     * Return the authorization credentials sent with this request.
     */
    String getAuthorization();


    /**
     * Return the Connector through which this Request was received.
     */
    Connector getConnector();


    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    void setConnector(Connector connector);


    /**
     * Return the Context within which this Request is being processed.
     */
    Context getContext();


    /**
     * Set the Context within which this Request is being processed.  This
     * must be called as soon as the appropriate Context is identified, because
     * it identifies the value to be returned by <code>getContextPath()</code>,
     * and thus enables parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    void setContext(Context context);


    /**
     * Get filter chain associated with the request.
     */
    FilterChain getFilterChain();


    /**
     * Set filter chain associated with the request.
     *
     * @param filterChain new filter chain
     */
    void setFilterChain(FilterChain filterChain);


    /**
     * Return the Host within which this Request is being processed.
     */
    Host getHost();


    /**
     * Set the Host within which this Request is being processed.  This
     * must be called as soon as the appropriate Host is identified, and
     * before the Request is passed to a context.
     *
     * @param host The newly associated Host
     */
    void setHost(Host host);


    /**
     * Return descriptive information about this Request implementation and
     * the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    String getInfo();


    /**
     * Return the <code>ServletRequest</code> for which this object
     * is the facade.
     */
    ServletRequest getRequest();


    /**
     * Return the <code>ServletRequest</code> for which this object
     * is the facade.
     *
     * @param maskDefaultContextMapping true if the fact that a request
     * received at the root context was mapped to a default-web-module will
     * be masked, false otherwise
     */
    ServletRequest getRequest(boolean maskDefaultContextMapping);


    /**
     * Return the Response with which this Request is associated.
     */
    Response getResponse();


    /**
     * Set the Response with which this Request is associated.
     *
     * @param response The new associated response
     */
    void setResponse(Response response);


    /**
     * Return the Socket (if any) through which this Request was received.
     * This should <strong>only</strong> be used to access underlying state
     * information about this Socket, such as the SSLSession associated with
     * an SSLSocket.
     */
    Socket getSocket();


    /**
     * Set the Socket (if any) through which this Request was received.
     *
     * @param socket The socket through which this request was received
     */
    void setSocket(Socket socket);


    /**
     * Return the input stream associated with this Request.
     */
    InputStream getStream();


    /**
     * Set the input stream associated with this Request.
     *
     * @param stream The new input stream
     */
    void setStream(InputStream stream);


    /**
     * Return the Wrapper within which this Request is being processed.
     */
    Wrapper getWrapper();


    /**
     * Set the Wrapper within which this Request is being processed.  This
     * must be called as soon as the appropriate Wrapper is identified, and
     * before the Request is ultimately passed to an application servlet.
     *
     * @param wrapper The newly associated Wrapper
     */
    void setWrapper(Wrapper wrapper);


    // --------------------------------------------------------- Public Methods


    /**
     * Create and return a ServletInputStream to read the content
     * associated with this Request.
     *
     * @exception IOException if an input/output error occurs
     */
    ServletInputStream createInputStream() throws IOException;


    /**
     * Perform whatever actions are required to flush and close the input
     * stream or reader, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    void finishRequest() throws IOException;


    /**
     * Return the object bound with the specified name to the internal notes
     * for this request, or <code>null</code> if no such binding exists.
     *
     * @param name Name of the note to be returned
     */
    Object getNote(String name);


    /**
     * Return an Iterator containing the String names of all notes bindings
     * that exist for this request.
     */
    Iterator getNoteNames();


    /**
     * Release all object references, and initialize instance variables, in
     * preparation for reuse of this object.
     */
    void recycle();


    /**
     * Remove any object bound to the specified name in the internal notes
     * for this request.
     *
     * @param name Name of the note to be removed
     */
    void removeNote(String name);


    /**
     * Set the content length associated with this Request.
     *
     * @param length The new content length
     */
    void setContentLength(int length);


    /**
     * Set the content type (and optionally the character encoding)
     * associated with this Request.  For example,
     * <code>text/html; charset=ISO-8859-4</code>.
     *
     * @param type The new content type
     */
    void setContentType(String type);


    /**
     * Bind an object to a specified name in the internal notes associated
     * with this request, replacing any existing binding for this name.
     *
     * @param name Name to which the object should be bound
     * @param value Object to be bound to the specified name
     */
    void setNote(String name, Object value);


    /**
     * Set the protocol name and version associated with this Request.
     *
     * @param protocol Protocol name and version
     */
    void setProtocol(String protocol);


    /**
     * Set the remote IP address associated with this Request.  NOTE:  This
     * value will be used to resolve the value for <code>getRemoteHost()</code>
     * if that method is called.
     *
     * @param remote The remote IP address
     */
    void setRemoteAddr(String remote);


    /**
     * Set the value to be returned by <code>isSecure()</code>
     * for this Request.
     *
     * @param secure The new isSecure value
     */
    void setSecure(boolean secure);


    /**
     * Set the name of the server (virtual host) to process this request.
     *
     * @param name The server name
     */
    void setServerName(String name);


    /**
     * Set the port number of the server to process this request.
     *
     * @param port The server port
     */
    void setServerPort(int port);


    // START CR 6415120
    /**
     * Set whether or not access to resources under WEB-INF or META-INF
     * needs to be checked.
     */
    void setCheckRestrictedResources(boolean check);


    /**
     * Return whether or not access to resources under WEB-INF or META-INF
     * needs to be checked.
     */
    boolean getCheckRestrictedResources();
    // END CR 6415120


    // START SJSAS 6346226
    /**
     * Gets the jroute id of this request, which may have been
     * sent as a separate <code>JROUTE</code> cookie or appended to the
     * session identifier encoded in the URI (if cookies have been disabled).
     *
     * @return The jroute id of this request, or null if this request does not
     * carry any jroute id
     */
    String getJrouteId();
    // END SJSAS 6346226


    /**
     * Generate and return a new session ID.
     *
     * This hook allows connectors to provide their own scalable session
     * ID generators.
     */
    String generateSessionId();


    /**
     * Disables async support on this request.
     */
    void disableAsyncSupport();


    /**
     * Sets the requested session cookie path, see IT 7426
     */
    void setRequestedSessionCookiePath(String cookiePath);


    /**
     * Gets the session associated with this Request, creating one
     * if necessary and requested.
     *
     * @param create true if a new session is to be created if one does not
     * already exist, false otherwise
     */
    Session getSessionInternal(boolean create);


    /**
     * Change the ID of the session that this request is associated with. There
     * are several things that may trigger an ID change. These include moving
     * between nodes in a cluster and session fixation prevention during the
     * authentication process.
     */
    String changeSessionId();

    Session lockSession();

    void unlockSession();
}
