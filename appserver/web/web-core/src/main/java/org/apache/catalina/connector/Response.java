/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.catalina.Connector;
import org.apache.catalina.Context;
import org.apache.catalina.HttpResponse;
import org.apache.catalina.LogFacade;
import org.apache.catalina.Session;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.util.RequestUtil;
import org.glassfish.grizzly.http.util.CharChunk;
import org.glassfish.grizzly.http.util.CookieHeaderGenerator;
import org.glassfish.grizzly.http.util.FastHttpDateFormat;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.http.util.UEncoder;

import static org.apache.catalina.Globals.JREPLICA_PARAMETER;
import static org.apache.catalina.Globals.JREPLICA_SESSION_NOTE;
import static org.apache.catalina.Globals.SESSION_PARAMETER_NAME;
import static org.apache.catalina.Globals.SESSION_VERSION_PARAMETER;
import static org.apache.catalina.LogFacade.CANNOT_CALL_SEND_ERROR_EXCEPTION;
import static org.apache.catalina.LogFacade.CANNOT_CALL_SEND_REDIRECT_EXCEPTION;
import static org.apache.catalina.LogFacade.CANNOT_CHANGE_BUFFER_SIZE_EXCEPTION;
import static org.apache.catalina.LogFacade.CANNOT_RESET_BUFFER_EXCEPTION;
import static org.apache.catalina.LogFacade.ERROR_DURING_FINISH_RESPONSE;
import static org.apache.catalina.LogFacade.GET_WRITER_BEEN_CALLED_EXCEPTION;
import static org.apache.catalina.Logger.WARNING;
import static org.apache.catalina.connector.Constants.PROXY_JROUTE;
import static org.glassfish.common.util.InputValidationUtil.getSafeHeaderName;
import static org.glassfish.common.util.InputValidationUtil.getSafeHeaderValue;
import static org.glassfish.web.util.HtmlEntityEncoder.encodeXSS;

/**
 * Wrapper object for the Coyote response.
 *
 * @author Remy Maucherat
 * @author Craig R. McClanahan
 * @version $Revision: 1.22 $ $Date: 2007/05/05 05:32:43 $
 */

public class Response implements HttpResponse, HttpServletResponse {

    // ------------------------------------------------------ Static variables

    public static final String HTTP_RESPONSE_DATE_HEADER = "EEE, dd MMM yyyy HH:mm:ss zzz";

    /**
     * Descriptive information about this Response implementation.
     */
    protected static final String info = "org.apache.catalina.connector.Response/1.0";

    private static final Logger log = LogFacade.getLogger();
    private static final ResourceBundle rb = log.getResourceBundle();

    /**
     * Whether or not to enforce scope checking of this object.
     */
    private static boolean enforceScope;

    // ----------------------------------------------------- Instance Variables

    private String detailErrorMsg;

    /**
     * The date format we will use for creating date headers.
     */
    protected SimpleDateFormat format;

    /**
     * Associated context.
     */
    protected Context context;

    protected boolean upgrade;

    /**
     * Associated Catalina connector.
     */
    protected Connector connector;

    /**
     * The request with which this response is associated.
     */
    protected Request connectorRequest;

    /**
     * The facade associated with this response.
     */
    protected ResponseFacade connectorResponsefacade;

    /**
     * Grizzly response.
     */
    protected org.glassfish.grizzly.http.server.Response grizzlyResponse;

    // ----------------------------------------------------------- Constructors

    public Response() {
        outputBuffer = new OutputBuffer();
        outputStream = new CoyoteOutputStream(outputBuffer);
        writer = createWriter(outputBuffer);
        urlEncoder.addSafeCharacter('/');
    }

    public Response(boolean chunkingDisabled) {
        outputBuffer = new OutputBuffer();
        outputStream = new CoyoteOutputStream(outputBuffer);
        writer = createWriter(outputBuffer);
        urlEncoder.addSafeCharacter('/');
    }

    // ------------------------------------------------------------- Properties

    /**
     * Set whether or not to enforce scope checking of this object.
     */
    public static void setEnforceScope(boolean enforce) {
        enforceScope = enforce;
    }

    /**
     * Return the Connector through which this Request was received.
     */
    @Override
    public Connector getConnector() {
        return this.connector;
    }

    /**
     * Set the Connector through which this Request was received.
     *
     * @param connector The new connector
     */
    @Override
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    /**
     * Set the Coyote response.
     *
     * @param coyoteResponse The Coyote response
     */
    public void setCoyoteResponse(org.glassfish.grizzly.http.server.Response coyoteResponse) {
        this.grizzlyResponse = coyoteResponse;
        outputBuffer.setCoyoteResponse(this);
    }

    /**
     * Get the Coyote response.
     */
    public org.glassfish.grizzly.http.server.Response getCoyoteResponse() {
        return grizzlyResponse;
    }

    /**
     * Return the Context within which this Request is being processed.
     */
    @Override
    public Context getContext() {
        /*
         * Ideally, we would call CoyoteResponse.setContext() from CoyoteAdapter (the same way we call it for CoyoteRequest),
         * and have getContext() return this context. However, for backwards compatibility with WS 7.0's NSAPIProcessor, which
         * does not call CoyoteResponse.setContext(), we must delegate to the getContext() method of the linked request object.
         */
        return connectorRequest.getContext();
    }

    /**
     * Set the Context within which this Request is being processed. This must be called as soon as the appropriate Context
     * is identified, because it identifies the value to be returned by <code>getContextPath()</code>, and thus enables
     * parsing of the request URI.
     *
     * @param context The newly associated Context
     */
    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * The associated output buffer.
     */
    protected OutputBuffer outputBuffer;

    /**
     * The associated output stream.
     */
    protected CoyoteOutputStream outputStream;

    /**
     * The associated writer.
     */
    protected CoyoteWriter writer;

    /**
     * The application commit flag.
     */
    protected boolean appCommitted;

    /**
     * The included flag.
     */
    protected boolean included;

    /**
     * The characterEncoding flag
     */
    private boolean isCharacterEncodingSet;

    /**
     * The error flag.
     */
    protected boolean error;

    /**
     * Using output stream flag.
     */
    protected boolean usingOutputStream;

    /**
     * Using writer flag.
     */
    protected boolean usingWriter;

    /**
     * URL encoder.
     */
    protected UEncoder urlEncoder = new UEncoder();

    /**
     * Recyclable buffer to hold the redirect URL.
     */
    protected CharChunk redirectURLCharChunk = new CharChunk();

    // --------------------------------------------------------- Public Methods

    /**
     * Release all object references, and initialize instance variables, in preparation for reuse of this object.
     */
    @Override
    public void recycle() {
        if (connectorRequest != null && connectorRequest.isAsyncStarted()) {
            return;
        }

        context = null;
        outputBuffer.recycle();
        usingOutputStream = false;
        usingWriter = false;
        appCommitted = false;
        included = false;
        error = false;
        isCharacterEncodingSet = false;
        detailErrorMsg = null;

        if (enforceScope) {
            if (connectorResponsefacade != null) {
                connectorResponsefacade.clear();
                connectorResponsefacade = null;
            }
            if (outputStream != null) {
                outputStream.clear();
                outputStream = null;
            }
            if (writer != null) {
                writer.clear();
                writer = null;
            }
        } else {
            writer.recycle();
        }

    }

    // ------------------------------------------------------- Response Methods

    /**
     * Return the number of bytes actually written to the output stream.
     */
    @Override
    public int getContentCount() {
        return outputBuffer.getContentWritten();
    }

    /**
     * Set the application commit flag.
     *
     * @param appCommitted The new application committed flag value
     */
    @Override
    public void setAppCommitted(boolean appCommitted) {
        this.appCommitted = appCommitted;
    }

    /**
     * Application commit flag accessor.
     */
    @Override
    public boolean isAppCommitted() {
        return appCommitted || isCommitted() || isSuspended() || getContentLength() > 0 && getContentCount() >= getContentLength();
    }

    /**
     * Return the "processing inside an include" flag.
     */
    @Override
    public boolean getIncluded() {
        return included;
    }

    /**
     * Set the "processing inside an include" flag.
     *
     * @param included <code>true</code> if we are currently inside a RequestDispatcher.include(), else <code>false</code>
     */
    @Override
    public void setIncluded(boolean included) {
        this.included = included;
    }

    /**
     * Return descriptive information about this Response implementation and the corresponding version number, in the format
     * <code>&lt;description&gt;/&lt;version&gt;</code>.
     */
    @Override
    public String getInfo() {
        return info;
    }

    /**
     * Return the Request with which this Response is associated.
     */
    @Override
    public org.apache.catalina.Request getRequest() {
        return connectorRequest;
    }

    /**
     * Set the Request with which this Response is associated.
     *
     * @param request The new associated request
     */
    @Override
    public void setRequest(org.apache.catalina.Request request) {
        if (request instanceof Request) {
            this.connectorRequest = (Request) request;
        }
    }

    /**
     * Return the <code>ServletResponse</code> for which this object is the facade.
     */
    @Override
    public HttpServletResponse getResponse() {
        if (connectorResponsefacade == null) {
            connectorResponsefacade = new ResponseFacade(this);
        }
        return connectorResponsefacade;
    }

    /**
     * Return the output stream associated with this Response.
     */
    @Override
    public OutputStream getStream() {
        if (outputStream == null) {
            outputStream = new CoyoteOutputStream(outputBuffer);
        }
        return outputStream;
    }

    /**
     * Set the output stream associated with this Response.
     *
     * @param stream The new output stream
     */
    @Override
    public void setStream(OutputStream stream) {
        // This method is evil
    }

    /**
     * Set the suspended flag.
     *
     * @param suspended The new suspended flag value
     */
    @Override
    public void setSuspended(boolean suspended) {
        outputBuffer.setSuspended(suspended);
    }

    /**
     * Suspended flag accessor.
     */
    @Override
    public boolean isSuspended() {
        return outputBuffer.isSuspended();
    }

    /**
     * Set the error flag.
     */
    @Override
    public void setError() {
        error = true;
    }

    /**
     * Error flag accessor.
     */
    @Override
    public boolean isError() {
        return error;
    }

    /**
     * Sets detail error message.
     *
     * @param message detail error message
     */
    @Override
    public void setDetailMessage(String message) {
        this.detailErrorMsg = message;
    }

    /**
     * Gets detail error message.
     *
     * @return the detail error message
     */
    @Override
    public String getDetailMessage() {
        return this.detailErrorMsg;
    }

    /**
     * Create and return a ServletOutputStream to write the content associated with this Response.
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public ServletOutputStream createOutputStream() throws IOException {
        // Probably useless
        if (outputStream == null) {
            outputStream = new CoyoteOutputStream(outputBuffer);
        }
        return outputStream;
    }

    /**
     * Perform whatever actions are required to flush and close the output stream or writer, in a single operation.
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void finishResponse() throws IOException {
        // Writing leftover bytes
        try {
            outputBuffer.close();
        } catch (IOException e) {

        } catch (Throwable t) {
            log(rb.getString(ERROR_DURING_FINISH_RESPONSE), t);
        }
    }

    /**
     * Return the content length that was set or calculated for this Response.
     */
    @Override
    public int getContentLength() {
        return grizzlyResponse.getContentLength();
    }

    /**
     * Return the content type that was set or calculated for this response, or <code>null</code> if no content type was
     * set.
     */
    @Override
    public String getContentType() {
        return grizzlyResponse.getContentType();
    }

    /**
     * Return a PrintWriter that can be used to render error messages, regardless of whether a stream or writer has already
     * been acquired.
     *
     * @return Writer which can be used for error reports. If the response is not an error report returned using sendError
     * or triggered by an unexpected exception thrown during the servlet processing (and only in that case), null will be
     * returned if the response stream has already been used.
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public PrintWriter getReporter() throws IOException {
        if (!outputBuffer.isNew()) {
            return null;
        }

        outputBuffer.checkConverter();
        if (writer == null) {
            writer = createWriter(outputBuffer);
        }

        return writer;
    }

    // ------------------------------------------------ ServletResponse Methods

    /**
     * Flush the buffer and commit this response.
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void flushBuffer() throws IOException {
        outputBuffer.flush();
    }

    /**
     * Return the actual buffer size used for this Response.
     */
    @Override
    public int getBufferSize() {
        return outputBuffer.getBufferSize();
    }

    /**
     * Return the character encoding used for this Response.
     */
    @Override
    public String getCharacterEncoding() {
        return grizzlyResponse.getCharacterEncoding();
    }

    /*
     * Overrides the name of the character encoding used in the body of the request. This method must be called prior to
     * reading request parameters or reading input using getReader().
     *
     * @param charset String containing the name of the character encoding.
     */
    @Override
    public void setCharacterEncoding(String charset) {

        // Ignore any call from an included servlet
        // Ignore any call made after the getWriter has been invoked
        // The default should be used
        if (isCommitted() || included || usingWriter) {
            return;
        }

        grizzlyResponse.setCharacterEncoding(charset);
        isCharacterEncodingSet = true;
    }

    /**
     * Return the servlet output stream associated with this Response.
     *
     * @exception IllegalStateException if <code>getWriter</code> has already been called for this response
     * @exception IOException if an input/output error occurs
     */
    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (usingWriter) {
            throw new IllegalStateException(rb.getString(GET_WRITER_BEEN_CALLED_EXCEPTION));
        }

        usingOutputStream = true;
        if (outputStream == null) {
            outputStream = new CoyoteOutputStream(outputBuffer);
        }

        return outputStream;
    }

    /**
     * Return the Locale assigned to this response.
     */
    @Override
    public Locale getLocale() {
        return grizzlyResponse.getLocale();
    }

    /**
     * Return the writer associated with this Response.
     *
     * @exception IllegalStateException if <code>getOutputStream</code> has already been called for this response
     * @exception IOException if an input/output error occurs
     */
    @Override
    public PrintWriter getWriter() throws IOException {
        if (usingOutputStream) {
            throw new IllegalStateException(rb.getString(LogFacade.GET_OUTPUT_STREAM_BEEN_CALLED_EXCEPTION));
        }

        /*
         * If the response's character encoding has not been specified as described in <code>getCharacterEncoding</code> (i.e.,
         * the method just returns the default value <code>ISO-8859-1</code>), <code>getWriter</code> updates it to
         * <code>ISO-8859-1</code> (with the effect that a subsequent call to getContentType() will include a charset=ISO-8859-1
         * component which will also be reflected in the Content-Type response header, thereby satisfying the Servlet spec
         * requirement that containers must communicate the character encoding used for the servlet response's writer to the
         * client).
         */
        setCharacterEncoding(getCharacterEncoding());

        usingWriter = true;
        outputBuffer.checkConverter();
        if (writer == null) {
            writer = createWriter(outputBuffer);
        }

        return writer;
    }

    /**
     * Has the output of this response already been committed?
     */
    @Override
    public boolean isCommitted() {
        return grizzlyResponse.isCommitted();
    }

    /**
     * Clear any content written to the buffer.
     *
     * @exception IllegalStateException if this response has already been committed
     */
    @Override
    public void reset() {
        if (included) {
            return; // Ignore any call from an included servlet
        }

        grizzlyResponse.reset();
        outputBuffer.reset();

        // reset Grizzly duplicated internal attributes
        grizzlyResponse.resetBuffer(true);
        usingOutputStream = false;
        usingWriter = false;
        isCharacterEncodingSet = false;
    }

    /**
     * Reset the data buffer but not any status or header information.
     *
     * @exception IllegalStateException if the response has already been committed
     */
    @Override
    public void resetBuffer() {
        resetBuffer(false);
    }

    /**
     * Reset the data buffer and the using Writer/Stream flags but not any status or header information.
     *
     * @param resetWriterStreamFlags <code>true</code> if the internal <code>usingWriter</code>,
     * <code>usingOutputStream</code>, <code>isCharacterEncodingSet</code> flags should also be reset
     *
     * @exception IllegalStateException if the response has already been committed
     */
    @Override
    public void resetBuffer(boolean resetWriterStreamFlags) {
        if (isCommitted()) {
            throw new IllegalStateException(rb.getString(CANNOT_RESET_BUFFER_EXCEPTION));
        }

        outputBuffer.reset();

        if (resetWriterStreamFlags) {
            usingOutputStream = false;
            usingWriter = false;
            isCharacterEncodingSet = false;
        }
    }

    /**
     * Set the buffer size to be used for this Response.
     *
     * @param size The new buffer size
     *
     * @exception IllegalStateException if this method is called after output has been committed for this response
     */
    @Override
    public void setBufferSize(int size) {
        if (isCommitted() || !outputBuffer.isNew()) {
            throw new IllegalStateException(rb.getString(CANNOT_CHANGE_BUFFER_SIZE_EXCEPTION));
        }

        outputBuffer.setBufferSize(size);
    }

    /**
     * Set the content length (in bytes) for this Response.
     *
     * @param length The new content length
     */
    @Override
    public void setContentLength(int length) {
        setContentLengthLong(length);
    }

    /**
     * Sets the length of the content body in the response In HTTP servlets, this method sets the HTTP Content-Length
     * header.
     *
     * @param length The new content length
     */
    @Override
    public void setContentLengthLong(long length) {
        // Ignore any call from an included servlet
        if (isCommitted() || included || usingWriter) {
            return;
        }

        grizzlyResponse.setContentLengthLong(length);
    }

    /**
     * Set the content type for this Response.
     *
     * @param type The new content type
     */
    @Override
    public void setContentType(String type) {
        // Ignore any call from an included servlet
        if (isCommitted() || included) {
            return;
        }

        // Ignore charset if getWriter() has already been called
        if (usingWriter) {
            if (type != null) {
                int index = type.indexOf(";");
                if (index != -1) {
                    type = type.substring(0, index);
                }
            }
        }

        grizzlyResponse.setContentType(type);

        // Check to see if content type contains charset
        if (type != null) {
            int index = type.indexOf(";");
            if (index != -1) {
                int len = type.length();
                index++;
                while (index < len && Character.isWhitespace(type.charAt(index))) {
                    index++;
                }
                if (index + 7 < len && type.charAt(index) == 'c' && type.charAt(index + 1) == 'h' && type.charAt(index + 2) == 'a'
                        && type.charAt(index + 3) == 'r' && type.charAt(index + 4) == 's' && type.charAt(index + 5) == 'e'
                        && type.charAt(index + 6) == 't' && type.charAt(index + 7) == '=') {
                    isCharacterEncodingSet = true;
                }
            }
        }
    }

    /**
     * Set the Locale that is appropriate for this response, including setting the appropriate character encoding.
     *
     * @param locale The new locale
     */
    @Override
    public void setLocale(Locale locale) {
        // Ignore any call from an included servlet
        if (isCommitted() || included) {
            return;
        }

        grizzlyResponse.setLocale(locale);

        // Ignore any call made after the getWriter has been invoked.
        // The default should be used
        if (usingWriter) {
            return;
        }

        if (isCharacterEncodingSet) {
            return;
        }

        String charset = getContext().getCharsetMapper().getCharset(locale);
        if (charset != null) {
            grizzlyResponse.setCharacterEncoding(charset);
        }
    }

    // --------------------------------------------------- HttpResponse Methods

    /**
     * Return the value for the specified header, or <code>null</code> if this header has not been set. If more than one
     * value was added for this name, only the first is returned; use {@link #getHeaders(String)} to retrieve all of them.
     *
     * @param name Header name to look up
     */
    @Override
    public String getHeader(String name) {
        return grizzlyResponse.getHeader(name);
    }

    /**
     * @return a (possibly empty) <code>Collection</code> of the names of the headers of this response
     */
    @Override
    public Collection<String> getHeaderNames() {
        final Collection<String> headerNames = new ArrayList<>();
        for (String headerName : grizzlyResponse.getResponse().getHeaders().names()) {
            headerNames.add(headerName);
        }

        return headerNames;
    }

    /**
     * @param name the name of the response header whose values to return
     *
     * @return a (possibly empty) <code>Collection</code> of the values of the response header with the given name
     */
    @Override
    public Collection<String> getHeaders(String name) {
        final Collection<String> headers = new ArrayList<>();
        for (String headerValue : grizzlyResponse.getResponse().getHeaders().values(name)) {
            headers.add(headerValue);
        }

        return headers;
    }

    /**
     * Return the error message that was set with <code>sendError()</code> for this Response.
     */
    @Override
    public String getMessage() {
        return grizzlyResponse.getMessage();
    }

    /**
     * Return the HTTP status code associated with this Response.
     */
    @Override
    public int getStatus() {
        return grizzlyResponse.getStatus();
    }

    /**
     * Reset this response, and specify the values for the HTTP status code and corresponding message.
     *
     * @exception IllegalStateException if this response has already been committed
     */
    @Override
    public void reset(int status, String message) {
        reset();
        setStatus(status, message);
    }

    // -------------------------------------------- HttpServletResponse Methods

    /**
     * Add the specified Cookie to those that will be included with this Response.
     *
     * @param cookie Cookie to be added
     */
    @Override
    public void addCookie(final Cookie cookie) {
        // Ignore any call from an included servlet
        if (isCommitted() || included) {
            return;
        }

        String cookieValue = getCookieString(cookie);

        // The header name is Set-Cookie for both "old" and v.1 (RFC2109)
        // RFC2965 is not supported by browsers and the Servlet spec
        // asks for RFC6265 (which obsoletes both RFC2965 and RFC2109)
        addHeader("Set-Cookie", cookieValue);
    }

    /**
     * Special method for adding a session cookie as we should be overriding any previous
     *
     * @param cookie
     */
    @Override
    public void addSessionCookieInternal(final Cookie cookie) {
        if (isCommitted()) {
            return;
        }

        String name = cookie.getName();
        final String headername = "Set-Cookie";
        final String startsWith = name + "=";
        final String cookieString = getCookieString(cookie);
        boolean set = false;

        MimeHeaders headers = grizzlyResponse.getResponse().getHeaders();
        int headersSize = headers.size();
        for (int i = 0; i < headersSize; i++) {
            if (headers.getName(i).toString().equals(headername)) {
                if (headers.getValue(i).toString().startsWith(startsWith)) {
                    headers.getValue(i).setString(cookieString);
                    set = true;
                }
            }
        }

        if (!set) {
            addHeader(headername, cookieString);
        }
    }

    /**
     * Add the specified date header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Date value to be set
     */
    @Override
    public void addDateHeader(String name, long value) {
        // Ignore any call from an included servlet
        if (name == null || name.length() == 0 || isCommitted() || included) {
            return;
        }

        if (format == null) {
            format = new SimpleDateFormat(HTTP_RESPONSE_DATE_HEADER, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        addHeader(name, FastHttpDateFormat.formatDate(value, format));
    }

    /**
     * Add the specified header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Value to be set
     */
    @Override
    public void addHeader(String name, String value) {
        // Ignore any call from an included servlet
        if (name == null || name.length() == 0 || value == null || isCommitted() || included) {
            return;
        }

        grizzlyResponse.addHeader(name, value);
    }

    /**
     * Add the specified integer header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Integer value to be set
     */
    @Override
    public void addIntHeader(String name, int value) {
        // Ignore any call from an included servlet
        if (name == null || name.length() == 0 || isCommitted() || included) {
            return;
        }

        addHeader(name, "" + value);
    }

    /**
     * Has the specified header been set already in this response?
     *
     * @param name Name of the header to check
     */
    @Override
    public boolean containsHeader(String name) {
        return grizzlyResponse.containsHeader(name);
    }

    @Override
    public Supplier<Map<String, String>> getTrailerFields() {
        return grizzlyResponse.getTrailers();
    }

    @Override
    public void setTrailerFields(Supplier<Map<String, String>> supplier) {
        grizzlyResponse.setTrailers(supplier);
    }

    /**
     * Encode the session identifier associated with this response into the specified redirect URL, if necessary.
     *
     * @param url URL to be encoded
     */
    @Override
    public String encodeRedirectURL(String url) {
        if (!isEncodeable(toAbsolute(url))) {
            return url;
        }

        String sessionVersion = null;
        Map<String, String> sessionVersions = connectorRequest.getSessionVersionsRequestAttribute();
        if (sessionVersions != null) {
            sessionVersion = RequestUtil.createSessionVersionString(sessionVersions);
        }

        return toEncoded(url, connectorRequest.getSessionInternal().getIdInternal(), sessionVersion);
    }

    /**
     * Encode the session identifier associated with this response into the specified URL, if necessary.
     *
     * @param url URL to be encoded
     */
    @Override
    public String encodeURL(String url) {
        String absolute = toAbsolute(url);
        if (!isEncodeable(absolute)) {
            return url;
        }

        // W3c spec clearly said
        if (url.equalsIgnoreCase("")) {
            url = absolute;
        } else if (url.equals(absolute) && !hasPath(url)) {
            url += '/';
        }

        String sessionVersion = null;
        Map<String, String> sessionVersions = connectorRequest.getSessionVersionsRequestAttribute();
        if (sessionVersions != null) {
            sessionVersion = RequestUtil.createSessionVersionString(sessionVersions);
        }

        return toEncoded(url, connectorRequest.getSessionInternal().getIdInternal(), sessionVersion);
    }

    /**
     * Apply URL Encoding to the given URL without adding session identifier et al associated to this response.
     *
     * @param url URL to be encoded
     */
    @Override
    public String encode(String url) {
        return urlEncoder.encodeURL(url);
    }

    /**
     * Send an acknowledgment of a request.
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void sendAcknowledgement() throws IOException {
        // Ignore any call from an included servlet
        if (isCommitted() || included) {
            return;
        }

        grizzlyResponse.sendAcknowledgement();
    }

    /**
     * Send an error response with the specified status and a default message.
     *
     * @param status HTTP status code to send
     *
     * @exception IllegalStateException if this response has already been committed
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void sendError(int status) throws IOException {
        sendError(status, null);
    }

    /**
     * Send an error response with the specified status and message.
     *
     * @param status HTTP status code to send
     * @param message Corresponding message to send
     *
     * @exception IllegalStateException if this response has already been committed
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void sendError(int status, String message) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException(rb.getString(CANNOT_CALL_SEND_ERROR_EXCEPTION));
        }

        // Ignore any call from an included servlet
        if (included) {
            return;
        }

        setError();

        grizzlyResponse.setStatus(status);

        // Use encoding in GlassFish
        grizzlyResponse.getResponse().setHtmlEncodingCustomReasonPhrase(false);
        grizzlyResponse.setDetailMessage(encodeXSS(message));

        // Clear any data content that has been buffered
        resetBuffer();

        // Cause the response to be finished (from the application perspective)
        setSuspended(true);
    }

    /**
     * Sends a temporary redirect to the specified redirect location URL.
     *
     * @param location Location URL to redirect to
     *
     * @throws IllegalStateException if this response has already been committed
     * @throws IOException if an input/output error occurs
     */
    @Override
    public void sendRedirect(String location) throws IOException {
        sendRedirect(location, true);
    }

    @Override
    public void sendRedirect(String location, int sc, boolean clearBuffer) throws IOException {
        if (isCommitted()) {
            throw new IllegalStateException(rb.getString(CANNOT_CALL_SEND_REDIRECT_EXCEPTION));
        }

        // Ignore any call from an included servlet
        if (included) {
            return;
        }

        // Clear any data content that has been buffered
        if (clearBuffer) {
            resetBuffer();
        }

        // Generate a temporary redirect to the specified location
        try {
            String absolute;
            if (getContext().getAllowRelativeRedirect()) {
                absolute = location;
            } else {
                absolute = toAbsolute(location);
            }

            setStatus(sc);
            setHeader("Location", absolute);

            // According to RFC2616 section 10.3.3 302 Found,
            // the response SHOULD contain a short hypertext note with
            // a hyperlink to the new URI.
            setContentType("text/html");
            setLocale(Locale.getDefault());

            String href = encodeXSS(absolute);
            StringBuilder sb = new StringBuilder(150 + href.length());

            sb.append("<html>\r\n");
            sb.append("<head><title>Document moved</title></head>\r\n");
            sb.append("<body><h1>Document moved</h1>\r\n");
            sb.append("This document has moved <a href=\"");
            sb.append(href);
            sb.append("\">here</a>.<p>\r\n");
            sb.append("</body>\r\n");
            sb.append("</html>\r\n");

            try {
                getWriter().write(sb.toString());
            } catch (IllegalStateException ise1) {
                try {
                    getOutputStream().print(sb.toString());
                } catch (IllegalStateException ise2) {
                    // ignore; the RFC says "SHOULD" so it is acceptable
                    // to omit the body in case of an error
                }
            }
        } catch (IllegalArgumentException e) {
            setStatus(SC_NOT_FOUND);
        }

        // Cause the response to be finished (from the application perspective)
        setSuspended(true);
    }

    /**
     * Set the specified date header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Date value to be set
     */
    @Override
    public void setDateHeader(String name, long value) {
        // Ignore any call from an included servlet
        if (name == null || name.length() == 0 || isCommitted() || included) {
            return;
        }

        if (format == null) {
            format = new SimpleDateFormat(HTTP_RESPONSE_DATE_HEADER, Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
        }

        setHeader(name, FastHttpDateFormat.formatDate(value, format));
    }

    /**
     * Set the specified header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Value to be set
     */
    @Override
    public void setHeader(String name, String value) {
        if (name == null || name.length() == 0 || value == null || isCommitted()) {
            return;
        }

        // Ignore any call from an included servlet
        if (included) {
            return;
        }

        try {
            grizzlyResponse.setHeader(getSafeHeaderName(name), getSafeHeaderValue(value));
        } catch (Exception e) {
            try {
                grizzlyResponse.sendError(403, "Forbidden");
            } catch (IOException ex) {
                // just return
            }
        }
    }

    /**
     * Set the specified integer header to the specified value.
     *
     * @param name Name of the header to set
     * @param value Integer value to be set
     */
    @Override
    public void setIntHeader(String name, int value) {
        // Ignore any call from an included servlet
        if (name == null || name.length() == 0 || isCommitted() || included) {
            return;
        }

        setHeader(name, "" + value);
    }

    /**
     * Set the HTTP status to be returned with this response.
     *
     * @param status The new HTTP status
     */
    @Override
    public void setStatus(int status) {
        setStatus(status, null);
    }

    @Override
    public void setError(int status, String message) {
        setStatus(status, message);
    }

    /**
     * Set the HTTP status and message to be returned with this response.
     *
     * @param status The new HTTP status
     * @param message The associated text message
     *
     */
    private void setStatus(int status, String message) {
        // Ignore any call from an included servlet
        if (isCommitted() || included) {
            return;
        }

        grizzlyResponse.setStatus(status);
        // Use encoding in GlassFish
        grizzlyResponse.getResponse().setHtmlEncodingCustomReasonPhrase(false);
        grizzlyResponse.setDetailMessage(encodeXSS(message));
    }

    // ------------------------------------------------------ Protected Methods

    /**
     * Return <code>true</code> if the specified URL should be encoded with a session identifier. This will be true if all
     * of the following conditions are met:
     * <ul>
     * <li>The request we are responding to asked for a valid session
     * <li>The requested session ID was not received via a cookie
     * <li>The specified URL points back to somewhere within the web application that is responding to this request
     * </ul>
     *
     * @param location Absolute URL to be validated
     */
    protected boolean isEncodeable(final String location) {
        // Is this an intra-document reference?
        if (location == null || location.startsWith("#")) {
            return false;
        }

        // Are we in a valid session that is not using cookies?
        final Session session = connectorRequest.getSessionInternal(false);
        if (session == null) {
            return false;
        }

        if (connectorRequest.isRequestedSessionIdFromCookie() || getContext() != null && !getContext().isEnableURLRewriting()) {
            return false;
        }

        return doIsEncodeable(connectorRequest, session, location);
    }

    private boolean doIsEncodeable(Request hreq, Session session, String location) {
        // Is this a valid absolute URL?
        URL url = null;
        try {
            url = new URL(location);
        } catch (MalformedURLException e) {
            return false;
        }

        // Does this URL match down to (and including) the context path?
        if (!hreq.getScheme().equalsIgnoreCase(url.getProtocol()) || !hreq.getServerName().equalsIgnoreCase(url.getHost())) {
            return false;
        }

        int serverPort = hreq.getServerPort();
        if (serverPort == -1) {
            if ("https".equals(hreq.getScheme())) {
                serverPort = 443;
            } else {
                serverPort = 80;
            }
        }

        int urlPort = url.getPort();
        if (urlPort == -1) {
            if ("https".equals(url.getProtocol())) {
                urlPort = 443;
            } else {
                urlPort = 80;
            }
        }

        if (serverPort != urlPort) {
            return false;
        }

        Context ctx = getContext();
        if (ctx != null) {
            String contextPath = ctx.getPath();
            if (contextPath != null) {
                String file = url.getFile();
                if (file == null || !file.startsWith(contextPath)) {
                    return false;
                }

                String sessionParamName = ctx.getSessionParameterName();
                if (file.contains(";" + sessionParamName + "=" + session.getIdInternal())) {
                    return false;
                }
            }
        }

        // This URL belongs to our web application, so it is encodeable
        return true;
    }

    /**
     * Convert (if necessary) and return the absolute URL that represents the resource referenced by this possibly relative
     * URL. If this URL is already absolute, return it unchanged.
     *
     * @param location URL to be (possibly) converted and then returned
     *
     * @exception IllegalArgumentException if a MalformedURLException is thrown when converting the relative URL to an
     * absolute one
     */
    protected String toAbsolute(String location) {
        if (location == null) {
            return location;
        }

        boolean leadingSlash = location.startsWith("/");

        if (location.startsWith("//")) {
            // Scheme relative, network-path reference in RFC 3986
            redirectURLCharChunk.recycle();

            // Add the scheme
            String scheme = getRedirectScheme();
            try {
                redirectURLCharChunk.append(scheme, 0, scheme.length());
                redirectURLCharChunk.append(':');
                redirectURLCharChunk.append(location, 0, location.length());

                return redirectURLCharChunk.toString();
            } catch (IOException e) {
                throw new IllegalArgumentException(location, e);
            }

        }

        if (leadingSlash || location.indexOf("://") == -1) {
            redirectURLCharChunk.recycle();

            String scheme = getRedirectScheme();
            String name = connectorRequest.getServerName();
            int port = connectorRequest.getServerPort();

            try {
                redirectURLCharChunk.append(scheme, 0, scheme.length());
                redirectURLCharChunk.append("://", 0, 3);
                redirectURLCharChunk.append(name, 0, name.length());
                if (scheme.equals("http") && port != 80 || scheme.equals("https") && port != 443) {
                    redirectURLCharChunk.append(':');
                    String portS = port + "";
                    redirectURLCharChunk.append(portS, 0, portS.length());
                }

                if (!leadingSlash) {
                    String relativePath = connectorRequest.getDecodedRequestURI();
                    relativePath = relativePath.substring(0, relativePath.lastIndexOf('/'));

                    String encodedURI = urlEncoder.encodeURL(relativePath);

                    redirectURLCharChunk.append(encodedURI, 0, encodedURI.length());
                    redirectURLCharChunk.append('/');
                }
                redirectURLCharChunk.append(location, 0, location.length());
                normalize(redirectURLCharChunk);
            } catch (IOException e) {
                throw new IllegalArgumentException(location, e);
            }

            return redirectURLCharChunk.toString();
        }

        return location;
    }

    /**
     * Returns the scheme for a redirect if it is not specified.
     */
    private String getRedirectScheme() {
        String scheme = connectorRequest.getScheme();

        if (getConnector() != null) {
            if (getConnector().getProxyScheme() != null) {
                scheme = getConnector().getProxyScheme();
            }
            if (getConnector().getAuthPassthroughEnabled()) {
                ProxyHandler proxyHandler = getConnector().getProxyHandler();
                if (proxyHandler != null && proxyHandler.getSSLKeysize(connectorRequest) > 0) {
                    scheme = "https";
                }
            }
        }

        return scheme;
    }

    /**
     * Return the specified URL with the specified session identifier suitably encoded.
     *
     * @param url URL to be encoded with the session id
     * @param sessionId Session id to be included in the encoded URL
     */
    protected String toEncoded(String url, String sessionId) {
        return toEncoded(url, sessionId, null);
    }

    /**
     * Return the specified URL with the specified session identifier suitably encoded.
     *
     * @param url URL to be encoded with the session id
     * @param sessionId Session id to be included in the encoded URL
     * @param sessionVersion Session version to be included in the encoded URL
     */
    private String toEncoded(String url, String sessionId, String sessionVersion) {
        if (url == null || sessionId == null) {
            return url;
        }

        String path = url;
        String query = "";
        String anchor = "";

        int question = url.indexOf('?');
        if (question >= 0) {
            path = url.substring(0, question);
            query = url.substring(question);
        }

        int pound = path.indexOf('#');
        if (pound >= 0) {
            anchor = path.substring(pound);
            path = path.substring(0, pound);
        }

        StringBuilder urlBuilder = new StringBuilder(path);
        if (urlBuilder.length() > 0) { // jsessionid can't be first.
            StandardContext ctx = (StandardContext) getContext();
            String sessionParamName = ctx != null ? ctx.getSessionParameterName() : SESSION_PARAMETER_NAME;
            urlBuilder.append(";" + sessionParamName + "=");
            urlBuilder.append(sessionId);
            if (ctx != null && ctx.getJvmRoute() != null) {
                urlBuilder.append('.').append(ctx.getJvmRoute());
            }

            String jrouteId = connectorRequest.getHeader(PROXY_JROUTE);
            if (jrouteId != null) {
                urlBuilder.append(":");
                urlBuilder.append(jrouteId);
            }

            Session session = connectorRequest.getSessionInternal(false);
            if (session != null) {
                String replicaLocation = (String) session.getNote(JREPLICA_SESSION_NOTE);
                if (replicaLocation != null) {
                    urlBuilder.append(JREPLICA_PARAMETER);
                    urlBuilder.append(replicaLocation);
                }
            }

            if (sessionVersion != null) {
                urlBuilder.append(SESSION_VERSION_PARAMETER);
                urlBuilder.append(sessionVersion);
            }
        }

        urlBuilder.append(anchor);
        urlBuilder.append(query);

        return urlBuilder.toString();
    }

    /**
     * Create an instance of CoyoteWriter
     */
    protected CoyoteWriter createWriter(OutputBuffer outbuf) {
        return new CoyoteWriter(outbuf);
    }

    /**
     * Gets the string representation of the given cookie.
     *
     * @param cookie The cookie whose string representation to get
     *
     * @return The cookie's string representation
     */
    protected String getCookieString(final Cookie cookie) {
        return CookieHeaderGenerator.generateHeader(
            cookie.getName(), cookie.getValue(), cookie.getMaxAge(), cookie.getDomain(),
            cookie.getPath(), cookie.getSecure(), cookie.isHttpOnly(), cookie.getAttributes());
    }

    /**
     * Removes any Set-Cookie response headers whose value contains the string JSESSIONID
     */
    public void removeSessionCookies() {
        String matchExpression = "^" + getContext().getSessionCookieName() + "=.*";
        grizzlyResponse.getResponse().getHeaders().removeHeaderMatches("Set-Cookie", matchExpression);

        matchExpression = "^" + org.apache.catalina.authenticator.Constants.SINGLE_SIGN_ON_COOKIE + "=.*";
        grizzlyResponse.getResponse().getHeaders().removeHeaderMatches("Set-Cookie", matchExpression);
    }

    public void setUpgrade(boolean upgrade) {
        this.upgrade = upgrade;
    }

    void disableWriteHandler() {
        outputBuffer.disableWriteHandler();
    }

    /*
     * Removes /./ and /../ sequences from absolute URLs. Code borrowed heavily from CoyoteAdapter.normalize()
     */
    private void normalize(CharChunk cc) {
        // Strip query string and/or fragment first as doing it this way makes
        // the normalization logic a lot simpler
        int truncate = cc.indexOf('?');
        if (truncate == -1) {
            truncate = cc.indexOf('#');
        }

        char[] truncateCC = null;
        if (truncate > -1) {
            truncateCC = Arrays.copyOfRange(cc.getBuffer(), cc.getStart() + truncate, cc.getEnd());
            cc.setEnd(cc.getStart() + truncate);
        }

        if (cc.endsWith("/.") || cc.endsWith("/..")) {
            try {
                cc.append('/');
            } catch (IOException e) {
                throw new IllegalArgumentException(cc.toString(), e);
            }
        }

        char[] c = cc.getChars();
        int start = cc.getStart();
        int end = cc.getEnd();
        int index = 0;
        int startIndex = 0;

        // Advance past the first three / characters (should place index just
        // scheme://host[:port]

        for (int i = 0; i < 3; i++) {
            startIndex = cc.indexOf('/', startIndex + 1);
        }

        // Remove /./
        index = startIndex;
        while (true) {
            index = cc.indexOf("/./", 0, 3, index);
            if (index < 0) {
                break;
            }
            copyChars(c, start + index, start + index + 2, end - start - index - 2);
            end = end - 2;
            cc.setEnd(end);
        }

        // Remove /../
        index = startIndex;
        int pos;
        while (true) {
            index = cc.indexOf("/../", 0, 4, index);
            if (index < 0) {
                break;
            }
            // Can't go above the server root
            if (index == startIndex) {
                throw new IllegalArgumentException();
            }
            int index2 = -1;
            for (pos = start + index - 1; (pos >= 0) && (index2 < 0); pos--) {
                if (c[pos] == (byte) '/') {
                    index2 = pos;
                }
            }
            copyChars(c, start + index2, start + index + 3, end - start - index - 3);
            end = end + index2 - index - 3;
            cc.setEnd(end);
            index = index2;
        }

        // Add the query string and/or fragment (if present) back in
        if (truncateCC != null) {
            try {
                cc.append(truncateCC, 0, truncateCC.length);
            } catch (IOException ioe) {
                throw new IllegalArgumentException(ioe);
            }
        }
    }

    private void copyChars(char[] c, int dest, int src, int len) {
        for (int pos = 0; pos < len; pos++) {
            c[pos + dest] = c[pos + src];
        }
    }

    /**
     * Determine if an absolute URL has a path component
     */
    private boolean hasPath(String uri) {
        int pos = uri.indexOf("://");
        if (pos < 0) {
            return false;
        }
        pos = uri.indexOf('/', pos + 3);
        if (pos < 0) {
            return false;
        }
        return true;
    }

    private void log(String message, Throwable t) {
        org.apache.catalina.Logger logger = null;
        if (connector != null && connector.getContainer() != null) {
            logger = connector.getContainer().getLogger();
        }

        String localName = "Response";
        if (logger != null) {
            logger.log(localName + " " + message, t, WARNING);
        } else {
            log.log(Level.WARNING, localName + " " + message, t);
        }
    }


}
