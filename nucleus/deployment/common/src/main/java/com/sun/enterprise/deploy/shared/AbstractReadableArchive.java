/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deploy.shared;

import org.glassfish.api.deployment.archive.ReadableArchive;
import java.util.Map;
import java.util.HashMap;

/**
 * Common methods for ReadableArchive implementations
 */
public abstract class AbstractReadableArchive implements ReadableArchive {
    protected ReadableArchive parentArchive;
    protected Map<Class<?>, Object> extraData=new HashMap<Class<?>, Object>();
    protected Map<String, Object> archiveMetaData = new HashMap<String, Object>();


    /**
     * set the parent archive for this archive
     *
     * @param parentArchive the parent archive
     */
    public void setParentArchive(ReadableArchive parentArchive) {
        this.parentArchive = parentArchive;
    }

    /**
     * get the parent archive of this archive
     *
     * @return the parent archive
     */
    public ReadableArchive getParentArchive() {
        return parentArchive;
    }

    /**
     * Returns any data that could have been calculated as part of
     * the descriptor loading.
     *
     * @param dataType the type of the extra data
     * @return the extra data or null if there are not an instance of
     * type dataType registered.
     */
    public synchronized <U> U getExtraData(Class<U> dataType) {
        return dataType.cast(extraData.get(dataType));
    }

    public synchronized <U> void setExtraData(Class<U> dataType, U instance) {
        extraData.put(dataType, instance);
    }

    public synchronized <U> void removeExtraData(Class<U> dataType) {
        extraData.remove(dataType);
    }


    public void addArchiveMetaData(String metaDataKey, Object metaData) {
        if (metaData!=null) {
            archiveMetaData.put(metaDataKey, metaData);
        }
    }

    public <T> T getArchiveMetaData(String metaDataKey, Class<T> metadataType) {
        Object metaData = archiveMetaData.get(metaDataKey);
        if (metaData != null) {
            return metadataType.cast(metaData);
        }
        return null;
    }

    public void removeArchiveMetaData(String metaDataKey) {
        archiveMetaData.remove(metaDataKey);
    }
}
