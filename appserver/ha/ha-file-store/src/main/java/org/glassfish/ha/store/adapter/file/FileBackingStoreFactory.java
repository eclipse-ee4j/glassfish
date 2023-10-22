/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ha.store.adapter.file;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.ha.store.api.BackingStore;
import org.glassfish.ha.store.api.BackingStoreConfiguration;
import org.glassfish.ha.store.api.BackingStoreException;
import org.glassfish.ha.store.api.BackingStoreFactory;
import org.glassfish.ha.store.api.BackingStoreTransaction;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */
@Service(name = "file")
public class FileBackingStoreFactory
        implements BackingStoreFactory {

    private static ThreadLocal<FileStoreTransaction> _current = new ThreadLocal<>();

    private static ConcurrentHashMap<String, FileBackingStore> _stores
            = new ConcurrentHashMap<>();


    static FileBackingStore getFileBackingStore(String storeName) {
        return _stores.get(storeName);
    }

    static void removemapping(String storeName) {
        _stores.remove(storeName);
    }

    @Override
    public <K extends Serializable, V extends Serializable> BackingStore<K, V> createBackingStore(
            BackingStoreConfiguration<K, V> conf)
                throws BackingStoreException {
        FileBackingStore<K, V> fs = new FileBackingStore<>();
        fs.initialize(conf);
        fs.setFileBackingStoreFactory(this);
        _stores.put(conf.getStoreName(), fs);
        return fs;
    }

    @Override
    public BackingStoreTransaction createBackingStoreTransaction() {
        FileStoreTransaction tx = new FileStoreTransaction();
        _current.set(tx);
        return tx;
    }

    //package
    static final FileStoreTransaction getCurrent() {
        return _current.get();
    }
}
