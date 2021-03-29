/*
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
import java.io.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link AdminCahce} based on file system.<br/>
 * <i>Singleton</i>
 *
 * @author mmares
 */
public class AdminCacheFileStore implements AdminCache {

    private static final String DEFAULT_FILENAME = "#default#.cache";
    private static final AdminCacheFileStore instance = new AdminCacheFileStore();

    private static final Logger logger = AdminLoggerInfo.getLogger();

    private AdminCacheUtils adminCahceUtils = AdminCacheUtils.getInstance();

    private AdminCacheFileStore() {
    }

    @Override
    public <A> A get(String key, Class<A> clazz) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (clazz == null) {
            throw new IllegalArgumentException("Attribute clazz can not be null.");
        }
        DataProvider provider = adminCahceUtils.getProvider(clazz);
        if (provider == null) {
            return null;
        }
        // @todo Java SE 7 - use try with resources
        InputStream is = null;
        try {
            is = getInputStream(key);
            return (A) provider.toInstance(is, clazz);
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, AdminLoggerInfo.mCannotReadCache, new Object[] { key });
            }
            return null;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ex) {
                }
            }
        }

    }

    private InputStream getInputStream(String key) throws IOException {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (!adminCahceUtils.validateKey(key)) {
            throw new IllegalArgumentException("Attribute key must be in form (([-_.a-zA-Z0-9]+/?)+)");
        }
        File f = getCacheFile(key);
        return new BufferedInputStream(new FileInputStream(f));
    }

    private File getCacheFile(String key) throws IOException {
        File dir = AsadminSecurityUtil.getDefaultClientDir();
        int idx = key.lastIndexOf('/');
        if (idx > 0) {
            dir = new File(dir, key.substring(0, idx));

            if (!FileUtils.mkdirsMaybe(dir))
                throw new IOException("Can't create directory: " + dir);
            key = key.substring(idx + 1);
            if (key.isEmpty()) {
                key = DEFAULT_FILENAME;
            }
        }
        return new File(dir, key);
    }

    @Override
    public synchronized void put(String key, Object data) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (!adminCahceUtils.validateKey(key)) {
            throw new IllegalArgumentException("Attribute key must be in form (([-_.a-zA-Z0-9]+/?)+)");
        }
        if (data == null) {
            throw new IllegalArgumentException("Attribute data can not be null.");
        }
        DataProvider provider = adminCahceUtils.getProvider(data.getClass());
        if (provider == null) {
            throw new IllegalStateException("There is no data provider for " + data.getClass());
        }
        File cacheFile;
        try {
            cacheFile = getCacheFile(key);
        } catch (IOException ex) {
            return;
        }
        // @todo Java SE 7 - use try with resources
        OutputStream os = null;
        try {
            File tempFile = File.createTempFile("temp", "cache", cacheFile.getParentFile());
            os = new BufferedOutputStream(new FileOutputStream(tempFile));
            provider.writeToStream(data, os);
            os.close();

            if (!FileUtils.deleteFileMaybe(cacheFile) || !tempFile.renameTo(cacheFile)) {
                if (logger.isLoggable(Level.WARNING)) {
                    logger.log(Level.WARNING, AdminLoggerInfo.mCannotWriteCache, new Object[] { cacheFile.getPath() });
                }
                if (!FileUtils.deleteFileMaybe(tempFile)) {
                    logger.log(Level.FINE, "can't delete file: {0}", tempFile);
                }

            }
        } catch (IOException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, AdminLoggerInfo.mCannotWriteCache, new Object[] { cacheFile.getPath() });
            }
        } finally {
            try {
                os.close();
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public boolean contains(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Attribute key must be unempty.");
        }
        if (!adminCahceUtils.validateKey(key)) {
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
        if (!adminCahceUtils.validateKey(key)) {
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
