/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util.cache;

import com.sun.enterprise.admin.util.AdminLoggerInfo;
import com.sun.enterprise.security.store.AsadminSecurityUtil;
import com.sun.enterprise.util.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link AdminCache} based on file system.<br/>
 * <i>Singleton</i>
 *
 * @author mmares
 */
public class AdminCacheFileStore implements AdminCache {

    private static final String DEFAULT_FILENAME = "#default#.cache";
    private static final AdminCacheFileStore instance = new AdminCacheFileStore();

    private static final Logger LOG = AdminLoggerInfo.getLogger();

    private final AdminCacheUtils adminCacheUtils = AdminCacheUtils.getInstance();

    private AdminCacheFileStore() {
    }

    @Override
    public <A> A get(String key, Class<A> clazz) {
        LOG.log(Level.FINEST, "get(key={0}, clazz={1})", new Object[] {key, clazz});
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Attribute clazz can not be null.");
        }
        DataProvider provider = adminCacheUtils.getProvider(clazz);
        if (provider == null) {
            return null;
        }
        try (InputStream is = getInputStream(key)) {
            return (A) provider.toInstance(is, clazz);
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot read admin cache file for " + key, ex);
            return null;
        }
    }

    private InputStream getInputStream(String key) throws IOException {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (!adminCacheUtils.validateKey(key)) {
            throw new IllegalArgumentException("Attribute key must be in form (([-_.a-zA-Z0-9]+/?)+)");
        }
        File f = getCacheFile(key);
        return new BufferedInputStream(new FileInputStream(f));
    }

    private File getCacheFile(String key) throws IOException {
        int idx = key.lastIndexOf('/');
        if (idx == 0) {
            return new File(AsadminSecurityUtil.GF_CLIENT_DIR, key);
        }
        File dir = new File(AsadminSecurityUtil.GF_CLIENT_DIR, key.substring(0, idx));
        if (!FileUtils.mkdirsMaybe(dir)) {
            throw new IOException("Can't create directory: " + dir);
        }
        key = key.substring(idx + 1);
        if (key.isEmpty()) {
            key = DEFAULT_FILENAME;
        }
        return new File(dir, key);
    }

    @Override
    public synchronized void put(String key, Object data) {
        LOG.log(Level.FINEST, "put(key={0}, data={1})", new Object[] {key, data});
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (!adminCacheUtils.validateKey(key)) {
            throw new IllegalArgumentException("Attribute key must be in form (([-_.a-zA-Z0-9]+/?)+)");
        }
        if (data == null) {
            throw new IllegalArgumentException("Attribute data can not be null.");
        }
        DataProvider provider = adminCacheUtils.getProvider(data.getClass());
        if (provider == null) {
            throw new IllegalStateException("There is no data provider for " + data.getClass());
        }
        File cacheFile;
        try {
            cacheFile = getCacheFile(key);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot write data to cache file for " +  key, e);
            return;
        }

        final File tempFile;
        try {
            tempFile = File.createTempFile("temp", "cache", cacheFile.getParentFile());
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot create a temp file for future cache file " +  cacheFile, e);
            return;
        }
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            provider.writeToStream(data, os);
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Cannot write data to temp file " +  tempFile, e);
            return;
        }

        if (!FileUtils.deleteFileMaybe(cacheFile) || !tempFile.renameTo(cacheFile)) {
            LOG.log(Level.WARNING, "Cannot delete or rename to cache file " +  cacheFile);
            if (!FileUtils.deleteFileMaybe(tempFile)) {
                LOG.log(Level.FINE, "Can't delete file {0}", tempFile);
            }
        }
    }

    @Override
    public boolean contains(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (!adminCacheUtils.validateKey(key)) {
            throw new IllegalArgumentException("Attribute key must be in form (([-_.a-zA-Z0-9]+/?)+)");
        }
        File cacheFile;
        try {
            cacheFile = getCacheFile(key);
        } catch (IOException ex) {
            return false;
        }
        return cacheFile.exists() && cacheFile.isFile();
    }

    @Override
    public Date lastUpdated(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (!adminCacheUtils.validateKey(key)) {
            throw new IllegalArgumentException("Attribute key must be in form (([-_.a-zA-Z0-9]+/?)+)");
        }
        File cacheFile;
        try {
            cacheFile = getCacheFile(key);
        } catch (IOException ex) {
            return null;
        }
        if (!cacheFile.exists() || !cacheFile.isFile()) {
            return null;
        }
        return new Date(cacheFile.lastModified());
    }

    public static AdminCacheFileStore getInstance() {
        return instance;
    }

}
