/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.apache.catalina.connector;

import com.sun.appserv.ProxyHandler;

import java.io.CharConversionException;
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.catalina.Container;
import org.apache.catalina.Context;
import org.apache.catalina.Globals;
import org.apache.catalina.Host;
import org.apache.catalina.LogFacade;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.util.ServerInfo;
import org.glassfish.common.util.InputValidationUtil;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.Note;
import org.glassfish.grizzly.http.server.AfterServiceListener;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.util.MappingData;
import org.glassfish.grizzly.http.util.ByteChunk;
import org.glassfish.grizzly.http.util.CharChunk;
import org.glassfish.grizzly.http.util.DataChunk;
import org.glassfish.web.valve.GlassFishValve;
import org.glassfish.web.valve.ServletContainerInterceptor;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.apache.catalina.Globals.SESSION_PARAMETER_NAME;
import static org.apache.catalina.LogFacade.FAILED_TO_INITIALIZE_THE_INTERCEPTOR;
import static org.apache.catalina.LogFacade.HTTP_LISTENER_DISABLED;
import static org.apache.catalina.LogFacade.INTERNAL_ERROR;
import static org.apache.catalina.LogFacade.NO_HOST_MATCHES_SERVER_NAME_INFO;
import static org.apache.catalina.LogFacade.PARSING_CLIENT_CERT_EXCEPTION;
import static org.apache.catalina.LogFacade.REQUEST_PROCESSING_EXCEPTION;
import static org.apache.catalina.connector.Constants.USE_CUSTOM_STATUS_MSG_IN_HEADER;
import static org.glassfish.internal.api.Globals.getDefaultHabitat;

/**
 * Implementation of a request processor which delegates the processing to a Coyote processor.
 *
 * @author Craig R. McClanahan
 * @author Remy Maucherat
 * @version $Revision: 1.34 $ $Date: 2007/08/24 18:38:28 $
 */

public class CoyoteAdapter extends HttpHandler {

    private static final Logger log = LogFacade.getLogger();
    private static final ResourceBundle rb = log.getResourceBundle();

    // -------------------------------------------------------------- Constants

    static final String JVM_ROUTE = System.getProperty("jvmRoute");

    protected static final boolean ALLOW_BACKSLASH =
        Boolean.valueOf(System.getProperty("org.glassfish.grizzly.tcp.tomcat5.CoyoteAdapter.ALLOW_BACKSLASH", "false"));

    private static final boolean COLLAPSE_ADJACENT_SLASHES =
        Boolean.valueOf(System.getProperty("com.sun.enterprise.web.collapseAdjacentSlashes", "true"));

    // Make sure this value is always aligned with {@link ContainerMapper}
    // (@see com.sun.enterprise.v3.service.impl.ContainerMapper)
    protected static final Note<MappingData> MAPPING_DATA =
        org.glassfish.grizzly.http.server.Request.<MappingData>createNote("MappingData");
    static final Note<Request> CATALINA_REQUEST_NOTE =
        org.glassfish.grizzly.http.server.Request.createNote(Request.class.getName());
    static final Note<Response> CATALINA_RESPONSE_NOTE =
        org.glassfish.grizzly.http.server.Request.createNote(Response.class.getName());

    static final CatalinaAfterServiceListener catalinaAfterServiceListener = new CatalinaAfterServiceListener();

    // Make sure this value is always aligned with {@link ContainerMapper}
    // (@see com.sun.enterprise.v3.service.impl.ContainerMapper)
    private final static Note<DataChunk> DATA_CHUNK =
        org.glassfish.grizzly.http.server.Request.<DataChunk>createNote("DataChunk");

    // ----------------------------------------------------- Instance Variables

    private Collection<ServletContainerInterceptor> interceptors;

    /**
     * When mod_jk is used, the adapter must be invoked the same way Tomcat does by invoking service(...) and the
     * afterService(...). This is a hack to make it compatible with Tomcat 5|6.
     */
    private boolean compatWithTomcat;

    private String serverName = ServerInfo.getPublicServerInfo();

    /**
     * The CoyoteConnector with which this processor is associated.
     */
    private Connector connector;


    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new CoyoteProcessor associated with the specified connector.
     *
     * @param connector CoyoteConnector that owns this processor
     */
    public CoyoteAdapter(Connector connector) {
        super();
        this.connector = connector;
        initServletInterceptors();
    }


    // -------------------------------------------------------- Adapter Methods

    /**
     * Service method.
     */
    @Override
    public void service(org.glassfish.grizzly.http.server.Request grizzlyRequest, org.glassfish.grizzly.http.server.Response grizzlyResponse) throws Exception {
        grizzlyResponse.getResponse().setAllowCustomReasonPhrase(USE_CUSTOM_STATUS_MSG_IN_HEADER);

        Request catalinaRequest = grizzlyRequest.getNote(CATALINA_REQUEST_NOTE);
        Response catalinaResponse = grizzlyRequest.getNote(CATALINA_RESPONSE_NOTE);

        // Grizzly already parsed, decoded, and mapped the request.
        // Let's re-use this info here, before firing the
        // requestStartEvent probe, so that the mapping data will be
        // available to any probe event listener via standard
        // ServletRequest APIs (such as getContextPath())
        MappingData mappingData = grizzlyRequest.getNote(MAPPING_DATA);
        final boolean v3Enabled = mappingData != null;
        if (catalinaRequest == null) {

            // Create objects
            catalinaRequest = (Request) connector.createRequest();
            catalinaResponse = (Response) connector.createResponse();

            // Link objects
            catalinaRequest.setResponse(catalinaResponse);
            catalinaResponse.setRequest(catalinaRequest);

            // Set as notes
            grizzlyRequest.setNote(CATALINA_REQUEST_NOTE, catalinaRequest);
            grizzlyRequest.setNote(CATALINA_RESPONSE_NOTE, catalinaResponse);

            // Set query string encoding
            grizzlyRequest.getRequest().getRequestURIRef().setDefaultURIEncoding(Charset.forName(connector.getURIEncoding()));
        }

        catalinaRequest.setGrizzlyRequest(grizzlyRequest);
        catalinaResponse.setCoyoteResponse(grizzlyResponse);

        if (v3Enabled && !compatWithTomcat) {
            catalinaRequest.setMappingData(mappingData);
            catalinaRequest.updatePaths(mappingData);
        }

        grizzlyRequest.addAfterServiceListener(catalinaAfterServiceListener);

        try {
            doService(grizzlyRequest, catalinaRequest, grizzlyResponse, catalinaResponse, v3Enabled);

            // Request may want to initialize async processing
            catalinaRequest.onExitService();
        } catch (Throwable t) {
            log.log(SEVERE, REQUEST_PROCESSING_EXCEPTION, t);
        }
    }

    private void enteringServletContainer(Request req, Response res) {
        if (interceptors == null) {
            return;
        }

        for (ServletContainerInterceptor interceptor : interceptors) {
            try {
                interceptor.preInvoke(req, res);
            } catch (Throwable th) {
                log.log(SEVERE, INTERNAL_ERROR, th);
            }
        }
    }

    private void leavingServletContainer(Request req, Response res) {
        if (interceptors == null) {
            return;
        }

        for (ServletContainerInterceptor interceptor : interceptors) {
            try {
                interceptor.postInvoke(req, res);
            } catch (Throwable th) {
                log.log(SEVERE, INTERNAL_ERROR, th);
            }
        }
    }

    private void initServletInterceptors() {
        try {
            interceptors = getDefaultHabitat().getAllServices(ServletContainerInterceptor.class);
        } catch (Throwable th) {
            log.log(SEVERE, FAILED_TO_INITIALIZE_THE_INTERCEPTOR, th);
        }
    }

    private void doService(
        final org.glassfish.grizzly.http.server.Request grizzlyRequest, final Request catalinaRequest,
        final org.glassfish.grizzly.http.server.Response grizzlyResponse, final Response catalinaResponse, final boolean v3Enabled) throws Exception {

        // Check connector for disabled state
        if (!connector.isEnabled()) {
            String msg = MessageFormat.format(rb.getString(HTTP_LISTENER_DISABLED), String.valueOf(connector.getPort()));
            if (log.isLoggable(FINE)) {
                log.log(FINE, msg);
            }
            catalinaResponse.sendError(SC_NOT_FOUND, msg);
            return;
        }

        // Parse and set Catalina and configuration specific request parameters
        if (postParseRequest(grizzlyRequest, catalinaRequest, grizzlyResponse, catalinaResponse, v3Enabled)) {

            boolean authPassthroughEnabled = connector.getAuthPassthroughEnabled();
            ProxyHandler proxyHandler = connector.getProxyHandler();
            if (authPassthroughEnabled && proxyHandler != null) {

                // Otherwise Servlet request.isSecure() value is not propagated when authPassthroughEnabled is set to true
                if (proxyHandler.getSSLKeysize(catalinaRequest.getRequest()) > 0) {
                    catalinaRequest.setSecure(true);
                }

                X509Certificate[] certs = null;
                try {
                    certs = proxyHandler.getSSLClientCertificateChain(catalinaRequest.getRequest());
                } catch (CertificateException ce) {
                    log.log(SEVERE, PARSING_CLIENT_CERT_EXCEPTION, ce);
                }
                if (certs != null) {
                    catalinaRequest.setAttribute(Globals.CERTIFICATES_ATTR, certs);
                }

            }

            // Invoke the web container
            connector.requestStartEvent(catalinaRequest.getRequest(), catalinaRequest.getHost(), catalinaRequest.getContext());
            Container container = connector.getContainer();
            enteringServletContainer(catalinaRequest, catalinaResponse);
            try {
                catalinaRequest.lockSession();
                if (container.getPipeline().hasNonBasicValves() || container.hasCustomPipeline()) {
                    container.getPipeline().invoke(catalinaRequest, catalinaResponse);
                } else {
                    // Invoke host directly
                    Host host = catalinaRequest.getHost();
                    if (host == null) {
                        catalinaResponse.sendError(SC_BAD_REQUEST);
                        catalinaResponse.setDetailMessage(
                            MessageFormat.format(
                                rb.getString(NO_HOST_MATCHES_SERVER_NAME_INFO),
                                catalinaRequest.getRequest().getServerName()));
                        return;
                    }

                    if (host.getPipeline().hasNonBasicValves() || host.hasCustomPipeline()) {
                        host.getPipeline().invoke(catalinaRequest, catalinaResponse);
                    } else {
                        GlassFishValve hostValve = host.getPipeline().getBasic();
                        hostValve.invoke(catalinaRequest, catalinaResponse);
                        // Error handling
                        hostValve.postInvoke(catalinaRequest, catalinaResponse);
                    }
                }
            } finally {
                try {
                    connector.requestEndEvent(catalinaRequest.getRequest(), catalinaRequest.getHost(), catalinaRequest.getContext(), catalinaResponse.getStatus());
                } finally {
                    leavingServletContainer(catalinaRequest, catalinaResponse);
                }
            }
        }

    }
    // ------------------------------------------------------ Protected Methods

    /**
     * Parse additional request parameters.
     */
    protected boolean postParseRequest(
        final org.glassfish.grizzly.http.server.Request grizzlyRequest, final Request catalinaRequest,
        final org.glassfish.grizzly.http.server.Response grizzlyResponse, final Response catalinaResponse, final boolean v3Enabled) throws Exception {

        // XXX the processor may have set a correct scheme and port prior to this point,
        // in ajp13 protocols dont make sense to get the port from the connector...
        // otherwise, use connector configuration
        catalinaRequest.setSecure(grizzlyRequest.isSecure());

        // URI decoding
        DataChunk decodedURI;
        try {
            decodedURI = grizzlyRequest.getRequest().getRequestURIRef().getDecodedRequestURIBC();
        } catch (CharConversionException cce) {
            catalinaResponse.sendError(SC_BAD_REQUEST, "Invalid URI");
            return false;
        }

        // Normalize Decoded URI
        if (!normalize(decodedURI, catalinaResponse)) {
            catalinaResponse.sendError(SC_BAD_REQUEST, catalinaResponse.getDetailMessage());
            return false;
        }

        if (compatWithTomcat || !v3Enabled) {

            // Set the remote principal
            String principal = grizzlyRequest.getRemoteUser();
            if (principal != null) {
                catalinaRequest.setUserPrincipal(new CoyotePrincipal(principal));
            }

            // Set the authorization type
            String authtype = grizzlyRequest.getAuthType();
            if (authtype != null) {
                catalinaRequest.setAuthType(authtype);
            }
        }

        /*
         * Remove any parameters from the URI, so they won't be considered by the mapping algorithm, and save them in a
         * temporary CharChunk, so that any session id param may be parsed once the target context, which may use a custom
         * session parameter name, has been identified
         */
        final CharChunk uriParamsCC = catalinaRequest.getURIParams();
        final CharChunk uriCC = decodedURI.getCharChunk();
        final int semicolon = uriCC.indexOf(';');
        if (semicolon > 0) {
            final int absSemicolon = uriCC.getStart() + semicolon;
            uriParamsCC.setChars(uriCC.getBuffer(), absSemicolon, uriCC.getEnd() - absSemicolon);
            decodedURI.setChars(uriCC.getBuffer(), uriCC.getStart(), absSemicolon - uriCC.getStart());
        }

        if (compatWithTomcat || !v3Enabled) {
            /* mod_jk */
            DataChunk localDecodedURI = decodedURI;
            if (semicolon > 0) {
                localDecodedURI = grizzlyRequest.getNote(DATA_CHUNK);
                if (localDecodedURI == null) {
                    localDecodedURI = DataChunk.newInstance();
                    grizzlyRequest.setNote(DATA_CHUNK, localDecodedURI);
                }
                localDecodedURI.duplicate(decodedURI);
            }

            connector.getMapper()
                     .map(
                       grizzlyRequest.getRequest().serverName(),
                       localDecodedURI,
                       catalinaRequest.getMappingData());

            MappingData md = catalinaRequest.getMappingData();
            grizzlyRequest.setNote(MAPPING_DATA, md);
            catalinaRequest.updatePaths(md);
        }

        // FIXME: the code below doesn't belongs to here, this is only have sense in Http11, not in ajp13..
        // At this point the Host header has been processed. Override if the proxyPort/proxyHost are set
        String proxyName = connector.getProxyName();
        int proxyPort = connector.getProxyPort();
        if (proxyPort != 0) {
            grizzlyRequest.setServerPort(proxyPort);
        }
        if (proxyName != null) {
            grizzlyRequest.setServerName(proxyName);
        }

        Context catalinaContext = (Context) catalinaRequest.getMappingData().context;

        // Parse session id
        if (catalinaContext != null) {
            if (grizzlyRequest.isRequestedSessionIdFromURL() && SESSION_PARAMETER_NAME.equals(catalinaContext.getSessionParameterName())) {
                catalinaRequest.obtainSessionId();
            } else if (!uriParamsCC.isNull()) {
                catalinaRequest.parseSessionId(catalinaContext.getSessionParameterName(), uriParamsCC);
            }
        }

        catalinaRequest.setDefaultContext(catalinaRequest.getMappingData().isDefaultContext);
        catalinaRequest.setContext(catalinaContext);

        if (catalinaContext != null && !uriParamsCC.isNull()) {
            catalinaRequest.parseSessionVersion(uriParamsCC);
        }

        if (!uriParamsCC.isNull()) {
            catalinaRequest.parseJReplica(uriParamsCC);
        }

        catalinaRequest.setWrapper((Wrapper) catalinaRequest.getMappingData().wrapper);

        // Filter trace method
        if (!connector.getAllowTrace() && Method.TRACE.equals(grizzlyRequest.getMethod())) {
            Wrapper wrapper = catalinaRequest.getWrapper();
            String header = null;
            if (wrapper != null) {
                String[] methods = wrapper.getServletMethods();
                if (methods != null) {
                    for (String method : methods) {
                        // Exclude TRACE from methods returned in Allow header
                        if ("TRACE".equals(method)) {
                            continue;
                        }
                        if (header == null) {
                            header = method;
                        } else {
                            header += ", " + method;
                        }
                    }
                }
            }
            grizzlyResponse.setStatus(405, "TRACE method is not allowed");
            grizzlyResponse.addHeader("Allow", header);

            return false;
        }

        // Possible redirect
        DataChunk redirectPathMB = catalinaRequest.getMappingData().redirectPath;
        if (!redirectPathMB.isNull() && (!catalinaContext.hasAdHocPaths() || (catalinaContext.getAdHocServletName(catalinaRequest.getRequest().getServletPath()) == null))) {
            String redirectPath = redirectPathMB.toString();
            String query = catalinaRequest.getQueryString();
            if (catalinaRequest.isRequestedSessionIdFromURL()) {
                // This is not optimal, but as this is not very common, it shouldn't matter
                redirectPath = redirectPath + ";" + catalinaContext.getSessionParameterName() + "=" + catalinaRequest.getRequestedSessionId();
            }

            redirectPath = catalinaResponse.encode(redirectPath);
            if (query != null) {
                // This is not optimal, but as this is not very common, it shouldn't matter
                redirectPath = redirectPath + "?" + query;
            }

            boolean authPassthroughEnabled = connector.getAuthPassthroughEnabled();
            ProxyHandler proxyHandler = connector.getProxyHandler();
            if (authPassthroughEnabled && proxyHandler != null) {
                if (proxyHandler.getSSLKeysize(catalinaRequest.getRequest()) > 0) {
                    catalinaRequest.setSecure(true);
                }
            }

            // Issue a permanent redirect
            // Validating the redirectPath for header injection
            if (InputValidationUtil.validateStringforCRLF(redirectPath)) {
                catalinaResponse.sendError(403, "Forbidden");
            } else {
                catalinaResponse.sendRedirect(InputValidationUtil.removeLinearWhiteSpaces(redirectPath), false);
            }

            return false;
        }

        // Parse session Id
        catalinaRequest.parseSessionCookiesId();
        catalinaRequest.parseJrouteCookie();

        return true;
    }

    /**
     * Normalize URI.
     * <p>
     * This method normalizes "\", "//", "/./" and "/../". This method will throw an error when trying to go above the root,
     * or if the URI contains a null byte.
     *
     * @param uriDataChunk URI DataChunk to be normalized
     * @param response The Catalina Response that a detail error message will be added to if an error is encountered
     */
    public static boolean normalize(DataChunk uriDataChunk, Response response) {
        DataChunk.Type type = uriDataChunk.getType();
        if (type == DataChunk.Type.Chars) {
            return normalizeChars(uriDataChunk, response);
        }

        return normalizeBytes(uriDataChunk, response);
    }

    private static boolean normalizeBytes(DataChunk uriDataChunk, Response response) {
        ByteChunk uriBC = uriDataChunk.getByteChunk();
        byte[] b = uriBC.getBytes();
        int start = uriBC.getStart();
        int end = uriBC.getEnd();

        // An empty URL is not acceptable
        if (start == end) {
            response.setDetailMessage("Invalid URI: Empty URL");
            return false;
        }

        // URL * is acceptable
        if ((end - start == 1) && b[start] == (byte) '*') {
            return true;
        }

        int pos = 0;
        int index = 0;

        // Replace '\' with '/'
        // Check for null byte
        for (pos = start; pos < end; pos++) {
            if (b[pos] == (byte) '\\') {
                if (ALLOW_BACKSLASH) {
                    b[pos] = (byte) '/';
                } else {
                    response.setDetailMessage("Invalid URI: Backslashes not allowed");
                    return false;
                }
            }
            if (b[pos] == (byte) 0) {
                response.setDetailMessage("Invalid URI: Null byte found during request normalization");
                return false;
            }
        }

        // The URL must start with '/'
        if (b[start] != (byte) '/') {
            response.setDetailMessage("Invalid URI: Request must start with /");
            return false;
        }

        // Replace "//" with "/"
        if (COLLAPSE_ADJACENT_SLASHES) {
            for (pos = start; pos < (end - 1); pos++) {
                if (b[pos] == (byte) '/') {
                    while ((pos + 1 < end) && (b[pos + 1] == (byte) '/')) {
                        copyBytes(b, pos, pos + 1, end - pos - 1);
                        end--;
                    }
                }
            }
        }

        // If the URI ends with "/." or "/..", then we append an extra "/"
        // Note: It is possible to extend the URI by 1 without any side effect
        // as the next character is a non-significant WS.
        if (((end - start) > 2) && (b[end - 1] == (byte) '.')) {
            if ((b[end - 2] == (byte) '/') || ((b[end - 2] == (byte) '.') && (b[end - 3] == (byte) '/'))) {
                b[end] = (byte) '/';
                end++;
            }
        }

        uriBC.setEnd(end);

        index = 0;

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            index = uriBC.indexOf("/./", 0, 3, index);
            if (index < 0) {
                break;
            }
            copyBytes(b, start + index, start + index + 2, end - start - index - 2);
            end = end - 2;
            uriBC.setEnd(end);
        }

        index = 0;

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            index = uriBC.indexOf("/../", 0, 4, index);
            if (index < 0) {
                break;
            }
            // Prevent from going outside our context
            if (index == 0) {
                response.setDetailMessage("Invalid URI: Request traversed outside of allowed context");
                return false;
            }
            int index2 = -1;
            for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos--) {
                if (b[pos] == (byte) '/') {
                    index2 = pos;
                }
            }
            copyBytes(b, start + index2, start + index + 3, end - start - index - 3);
            end = end + index2 - index - 3;
            uriBC.setEnd(end);
            index = index2;
        }

        uriBC.setBytes(b, start, end);
        return true;
    }

    private static boolean normalizeChars(DataChunk uriDataChunk, Response response) {
        CharChunk uriCharChunk = uriDataChunk.getCharChunk();
        char[] c = uriCharChunk.getChars();
        int start = uriCharChunk.getStart();
        int end = uriCharChunk.getEnd();

        // URL * is acceptable
        if ((end - start == 1) && c[start] == '*') {
            return true;
        }

        int pos = 0;
        int index = 0;

        // Replace '\' with '/'
        // Check for null char
        for (pos = start; pos < end; pos++) {
            if (c[pos] == '\\') {
                if (ALLOW_BACKSLASH) {
                    c[pos] = '/';
                } else {
                    response.setDetailMessage("Invalid URI: Backslashes not allowed");
                    return false;
                }
            }
            if (c[pos] == (char) 0) {
                response.setDetailMessage("Invalid URI: Null byte found during request normalization");
                return false;
            }
        }

        // The URL must start with '/'
        if (c[start] != '/') {
            response.setDetailMessage("Invalid URI: Request must start with /");
            return false;
        }

        // Replace "//" with "/"
        if (COLLAPSE_ADJACENT_SLASHES) {
            for (pos = start; pos < (end - 1); pos++) {
                if (c[pos] == '/') {
                    while ((pos + 1 < end) && (c[pos + 1] == '/')) {
                        copyChars(c, pos, pos + 1, end - pos - 1);
                        end--;
                    }
                }
            }
        }

        // If the URI ends with "/." or "/..", then we append an extra "/"
        // Note: It is possible to extend the URI by 1 without any side effect
        // as the next character is a non-significant WS.
        if (((end - start) > 2) && (c[end - 1] == '.')) {
            if ((c[end - 2] == '/') || ((c[end - 2] == '.') && (c[end - 3] == '/'))) {
                c[end] = '/';
                end++;
            }
        }

        uriCharChunk.setEnd(end);

        index = 0;

        // Resolve occurrences of "/./" in the normalized path
        while (true) {
            index = uriCharChunk.indexOf("/./", 0, 3, index);
            if (index < 0) {
                break;
            }
            copyChars(c, start + index, start + index + 2, end - start - index - 2);
            end = end - 2;
            uriCharChunk.setEnd(end);
        }

        index = 0;

        // Resolve occurrences of "/../" in the normalized path
        while (true) {
            index = uriCharChunk.indexOf("/../", 0, 4, index);
            if (index < 0) {
                break;
            }
            // Prevent from going outside our context
            if (index == 0) {
                response.setDetailMessage("Invalid URI: Request traversed outside of allowed context");
                return false;
            }
            int index2 = -1;
            for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos--) {
                if (c[pos] == '/') {
                    index2 = pos;
                }
            }
            copyChars(c, start + index2, start + index + 3, end - start - index - 3);
            end = end + index2 - index - 3;
            uriCharChunk.setEnd(end);
            index = index2;
        }

        uriCharChunk.setChars(c, start, end);
        return true;
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Copy an array of bytes to a different position. Used during normalization.
     */
    protected static void copyBytes(byte[] b, int dest, int src, int len) {
        for (int pos = 0; pos < len; pos++) {
            b[pos + dest] = b[pos + src];
        }
    }

    /**
     * Copy an array of chars to a different position. Used during normalization.
     */
    private static void copyChars(char[] c, int dest, int src, int len) {
        for (int pos = 0; pos < len; pos++) {
            c[pos + dest] = c[pos + src];
        }
    }

    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    protected void log(String message) {
        log.log(INFO, message);
    }

    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param throwable Associated exception
     */
    protected void log(String message, Throwable throwable) {
        log.log(SEVERE, message, throwable);
    }

    /**
     * Notify all container event listeners that a particular event has occurred for this Adapter. The default
     * implementation performs this notification synchronously using the calling thread.
     *
     * @param type Event type
     * @param data Event data
     */
    public void fireAdapterEvent(String type, Object data) {
        if (connector != null && connector.getContainer() != null) {
            try {
                ((ContainerBase) connector.getContainer()).fireContainerEvent(type, data);
            } catch (Throwable t) {
                log.log(SEVERE, REQUEST_PROCESSING_EXCEPTION, t);
            }
        }
    }

    /**
     * Return true when an instance is executed the same way it does in Tomcat.
     */
    public boolean isCompatWithTomcat() {
        return compatWithTomcat;
    }

    /**
     * <tt>true</tt> if this class needs to be compatible with Tomcat Adapter class. Since Tomcat Adapter implementation
     * doesn't support the afterService method, the afterService method must be invoked inside the service method.
     */
    public void setCompatWithTomcat(boolean compatWithTomcat) {
        this.compatWithTomcat = compatWithTomcat;

        // Add server header
        if (compatWithTomcat) {
            serverName = "Apache/" + serverName;
        } else {
            // Recalculate.
            serverName = ServerInfo.getPublicServerInfo();
        }
    }

    /**
     * Gets the port of this CoyoteAdapter.
     *
     * @return the port of this CoyoteAdapter
     */
    public int getPort() {
        return connector.getPort();
    }

    /**
     * AfterServiceListener, which is responsible for recycle catalina request and response objects.
     */
    static final class CatalinaAfterServiceListener implements AfterServiceListener {

        @Override
        public void onAfterService(final org.glassfish.grizzly.http.server.Request grizzlyRequest) {
            final Request catalinaRequest = grizzlyRequest.getNote(CATALINA_REQUEST_NOTE);
            final Response catalinaResponse = grizzlyRequest.getNote(CATALINA_RESPONSE_NOTE);

            if (catalinaRequest != null) {
                try {
                    if (!catalinaRequest.isUpgrade()) {
                        catalinaResponse.finishResponse();
                    } else {
                        catalinaResponse.setUpgrade(catalinaRequest.isUpgrade());
                    }
                } catch (Exception e) {
                    log.log(SEVERE, REQUEST_PROCESSING_EXCEPTION, e);
                } finally {
                    try {
                        catalinaRequest.unlockSession();
                    } finally {
                        catalinaRequest.recycle();
                        catalinaResponse.recycle();
                    }
                }
            }
        }
    }
}
