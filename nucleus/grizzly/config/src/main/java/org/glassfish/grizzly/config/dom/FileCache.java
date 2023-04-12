/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.dom;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Created Jan 8, 2009
 *
 * @author <a href="mailto:justin.d.lee@oracle.com">Justin Lee</a>
 */
@Configured
public interface FileCache extends ConfigBeanProxy, PropertyBag {

    boolean ENABLED = false;

    int MAX_AGE = 30;

    int MAX_CACHE_SIZE = 10485760;

    int MAX_FILES = 1024;

    /**
     * Enables the caching of file content.
     */
    @Attribute(defaultValue = "" + ENABLED, dataType = Boolean.class)
    String getEnabled();

    void setEnabled(final String enabled);

    /**
     * How old files can get before aging out of cache in seconds.
     */
    @Attribute(defaultValue = "" + MAX_AGE, dataType = Integer.class)
    String getMaxAgeSeconds();

    void setMaxAgeSeconds(final String maxAge);

    /**
     * Maximum cache size on the disk.
     */
    @Attribute(defaultValue = "" + MAX_CACHE_SIZE, dataType = Integer.class)
    String getMaxCacheSizeBytes();

    void setMaxCacheSizeBytes(final String maxCacheSize);

    /**
     * Maximum number of files in the file cache.
     */
    @Attribute(defaultValue = "" + MAX_FILES, dataType = Integer.class)
    String getMaxFilesCount();

    void setMaxFilesCount(final String maxFilesCount);
}
