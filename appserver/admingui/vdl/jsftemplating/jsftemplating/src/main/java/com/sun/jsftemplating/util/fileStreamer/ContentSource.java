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
import java.io.InputStream;

/**
 * <p>
 * Implement this interface to provide an Object that is capable of providing data to <code>FileStreamer</code>.
 * <code>ContentSource</code> implementations must be thread safe. The <code>FileStreamer</code> will reuse the same
 * instance when 2 requests are made to the same ContentSource type. Instance variables, therefore, should not be used;
 * you may use the context to store local information.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public interface ContentSource {

    /**
     * <p>
     * This method should return a unique string used to identify this <code>ContentSource</code>. This string must be
     * specified in order to select the appropriate <code>ContentSource</code> when using the <code>FileStreamer</code>.
     * </p>
     */
    String getId();

    /**
     * <p>
     * This method is responsible for generating the content and returning an InputStream to that content. It is also
     * responsible for setting any attribute values in the {@link Context}, such as {@link Context#EXTENSION} or
     * {@link Context#CONTENT_TYPE}.
     * </p>
     */
    InputStream getInputStream(Context ctx) throws IOException;

    /**
     * <p>
     * This method returns the path of the resource that was requested.
     * </p>
     */
    String getResourcePath(Context ctx);

    /**
     * <p>
     * This method may be used to clean up any temporary resources. It will be invoked after the <code>InputStream</code>
     * has been completely read.
     * </p>
     */
    void cleanUp(Context ctx);

    /**
     * <p>
     * This method is responsible for returning the last modified date of the content, or -1 if not applicable. This
     * information will be used for caching.
     * </p>
     */
    long getLastModified(Context context);
}
