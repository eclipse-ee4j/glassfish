/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util.fileStreamer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

/**
 * <p>
 * This interface provides API's to encapsulate environment specific objects so that the FileStreamer class is not
 * specific to a specific environment (like a Servlet environment).
 * </p>
 */
public interface Context {

    /**
     * <p>
     * Accessor to get the {@link FileStreamer} instance.
     * </p>
     */
    FileStreamer getFileStreamer();

    /**
     * <p>
     * This method locates the appropriate {@link ContentSource} for this <code>Context</code>.
     * </p>
     */
    ContentSource getContentSource();

    /**
     * <p>
     * This method allows the Context to restrict access to resources. It returns <code>true</code> if the user is allowed
     * to view the resource. It returns <code>false</code> if the user should not be allowed access to the resource.
     * </p>
     */
    boolean hasPermission(ContentSource src);

    /**
     * <p>
     * This method is responsible for setting the response header information.
     * </p>
     */
    void writeHeader(ContentSource source) throws IOException;

    /**
     * <p>
     * This method is responsible for sending an error.
     * </p>
     */
    void sendError(int code, String msg) throws IOException;

    /**
     * <p>
     * This method is responsible for returning an <code>OutputStream</code> suitable for writing content.
     * </p>
     */
    OutputStream getOutputStream() throws IOException;

    /**
     * <p>
     * This method may be used to manage arbitrary information between the code invoking the {@link FileStreamer} and the
     * <code>ContentSource</code>. This method retrieves an attribute.
     * </p>
     *
     * <p>
     * See individual {@link ContentSource} implementations for more details on supported / required attributes.
     * </p>
     */
    Object getAttribute(String name);

    /**
     * <p>
     * This provides access to all attributes in this Context.
     * </p>
     *
     * <p>
     * See individual {@link ContentSource} implementations for more details on supported / required attributes.
     * </p>
     */
    Set<String> getAttributeKeys();

    /**
     * <p>
     * This method may be used to manage arbitrary information between the code invoking the {@link FileStreamer} and the
     * <code>ContentSource</code>. This method sets an attribute.
     * </p>
     *
     * <p>
     * See individual {@link ContentSource} implementations for more details on supported / required attributes.
     * </p>
     */
    void setAttribute(String name, Object value);

    /**
     * <p>
     * This method may be used to manage arbitrary information between the coding invoking the {@link FileStreamer} and the
     * <code>ContentSource</code>. This method removes an attribute.
     * </p>
     *
     * <p>
     * See individual {@link ContentSource} implementations for more details on supported / required attributes.
     * </p>
     */
    void removeAttribute(String name);

    /**
     * <p>
     * This is the number of milliseconds into the future until the content should expire. This constant is currently set at
     * about 7 years.
     * </p>
     */
    long EXPIRY_TIME = 1000 * 60 * 60 * 24 * 365 * 7;

    /**
     * <p>
     * This is the {@link Context} attribute name used to specify the filename extension of the content. It is the
     * responsibility of the {@link ContentSource} to set this value. The value should represent the filename extension of
     * the content if it were saved to a filesystem.
     * </p>
     */
    String EXTENSION = "extension";

    /**
     * <p>
     * The Content-type ("ContentType"). This is the {@link Context} attribute used to specify an explicit "Content-type".
     * It may be set by the {@link ContentSource}. If not specified, the {@link #EXTENSION} will typically be used (if
     * possible). If that fails, the {@link FileStreamer#getDefaultMimeType} is used.
     * </p>
     */
    String CONTENT_TYPE = "ContentType";

    /**
     * <p>
     * The value for the "Content-Disposition" ("disposition"). This is the {@link Context} attribute used to specify the
     * content disposition. The content disposition tells the browser how to handle the content. The two standard values for
     * this are:
     * </p>
     *
     * <ul>
     * <li>inline</li>
     * <li>attachment</li>
     * </ul>
     *
     * <p>
     * See RFC 2183 for more information. This value may be set by the {@link ContentSource}. If not specified, nothing will
     * be set. This value may be used in conjunction with the {@link #CONTENT_FILENAME} attribute, or the entire content
     * disposition may be specified with this attribute.
     * </p>
     */
    String CONTENT_DISPOSITION = "disposition";

    /**
     * <p>
     * The value for the "Content-Disposition" ("filename"). This is the {@link Context} attribute used to specify an
     * explicit "filename". It may be set by the {@link ContentSource}. If not specified, nothing will be set. If
     * {@link #CONTENT_DISPOSITION} is also set, this method will append the file name. If not set, it will set the
     * contentDisposition to "attachment".
     * </p>
     */
    String CONTENT_FILENAME = "filename";

    /**
     * <p>
     * This is the path of the requested file ("filename"). It is the responsibility of the {@link Context} implementation
     * to provide this information as an attribute.
     * </p>
     */
    String FILE_PATH = "filePath";

    /**
     * <p>
     * This is the parameter that may be provided to identify the {@link ContentSource} implementation to be used. This
     * value must match the value returned by the <code>ContentSource</code> implementation's <code>getId()</code> method.
     * It is typical for {@link Context} implementations to allow this to be specified by a <code>HttpServletRequest</code>
     * parameter.
     * </p>
     */
    String CONTENT_SOURCE_ID = "contentSourceId";

    /**
     * <p>
     * This String ("ContentSources") is the name of the <code>Servlet</code> init parameter or context param (depending on
     * environment implementation) that should be used to register all available {@link ContentSource} implementations. This
     * should be a list of full classnames of the {@link ContentSource}s.
     * </p>
     */
    String CONTENT_SOURCES = "ContentSources";

    /**
     * <p>
     * This String ("com.sun.jsftemplating.FS_ALLOW_PATHS") is the name of the <code>Servlet</code> init parameter or
     * context param (depending on environment implementation) that should be used to register all valid paths for resources
     * to be streamed by {@link FileStreamer}. This should be specified as a comma (or semi-colon) separated list of path
     * prefixes. Paths are relative to the context root of the application. Any path starting with one of the paths in this
     * list may be served using FileStreamer (provided it is not explicitly excluded via {@link #DENY_PATHS}). Leading "/"
     * characters maybe specified, but will be removed when doing a comparison. A value of "/" will provide access to the
     * entire application, which is the default value if not specified.
     * </p>
     */
    String ALLOW_PATHS = "com.sun.jsftemplating.FS_ALLOW_PATHS";

    /**
     * <p>
     * This String ("com.sun.jsftemplating.FS_DENY_PATHS") is the name of the <code>Servlet</code> init parameter or context
     * param (depending on environment implementation) that should be used to register all valid paths for resources to be
     * streamed by {@link FileStreamer}. This should be specified as a comma (or semi-colon) separated list of path
     * prefixes. Paths are relative to the context root of the application. Any path starting with one of the paths in this
     * list may <b>NOT</b> be served using FileStreamer. Leading '/' characters maybe specified, but will be removed when
     * doing a comparison. In addition to supporting prefix patterns, any path starting with an asterisk (<code>*</code>)
     * character will be interpreted as a suffix pattern. Any path ending with the characters following the asterisk will
     * match and will <b>NOT</b> be served. The default value is:
     * </p>
     * <p>
     * <code>META-INF/,WEB-INF/</code>"
     * </p>
     */
    String DENY_PATHS = "com.sun.jsftemplating.FS_DENY_PATHS";

    /**
     * <p>
     * This is the id of the default {@link ContentSource}. This is set to the id of the {@link ResourceContentSource}.
     * </p>
     */
    String DEFAULT_CONTENT_SOURCE_ID = ResourceContentSource.ID;
}
