/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.security.services.impl;

import com.sun.enterprise.security.store.PasswordAdapter;
import com.sun.enterprise.util.Utility;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.crypto.SecretKey;

import org.glassfish.api.admin.PasswordAliasStore;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Provides the PasswordAliasStore behavior using a JCEKS keystore.
 * <p>
 * The keystore is actually managed by the PasswordAdapter, to which
 * this implementation currently delegates its work.
 * <p>
 * Note that this service is currently per-lookup.  This is so that each
 * use of the alias store gets the current on-disk information.  Ideally we can change this
 * when we can use Java 7 features, including the WatchService feature.
 * <p>
 * This class's methods are not
 * synchronized because the PasswordAdapter's methods are.  If this implementation
 * changes so that it no longer delegates to those synchronized PasswordAdapter
 * methods, then make sure that the implementation is thread-safe.
 * <p>
 * Note that the domain-scoped password alias store service class extends this
 * class.  As a service, that class will be instantiated using the no-args
 * constructor.  So the actual initialization of the class occurs in the init
 * method.  The domain-scoped service class invokes the init method itself.
 * Any code that needs to create some other alias store can use the newInstance
 * method to provide the location of the alias store file and the password.
 *
 * @author tjquinn
 */
public class JCEKSPasswordAliasStore implements PasswordAliasStore {

    private PasswordAdapter pa;
    private String pathToAliasStore;
    private char[] storePassword;

    protected final void init(final String pathToAliasStore, final char[] storePassword) {
        this.pathToAliasStore = pathToAliasStore;
        this.storePassword = storePassword;
    }

    private synchronized PasswordAdapter pa() {
        if (pa == null) {
            try {
                pa = new PasswordAdapter(pathToAliasStore, storePassword);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
        return pa;
    }

    public static JCEKSPasswordAliasStore newInstance(final String pathToAliasStore, final char[] storePassword) {
        final JCEKSPasswordAliasStore result = new JCEKSPasswordAliasStore();
        result.init(pathToAliasStore, storePassword);
        return result;
    }

    @Override
    public void clear() {
        try {
            for (Enumeration<String> aliasEnum = pa().getAliases(); aliasEnum.hasMoreElements(); ) {
                pa().removeAlias(aliasEnum.nextElement());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void put(String alias, char[] password) {
        final CharBuffer charBuffer = CharBuffer.wrap(password);
        final ByteBuffer byteBuffer = UTF_8.encode(charBuffer);
        try {
            pa().setPasswordForAlias(alias, Utility.toByteArray(byteBuffer));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void putAll(PasswordAliasStore otherStore) {
        final Map<String,char[]> entries = new HashMap<String,char[]>();
        for (Iterator<String> aliasIt = otherStore.keys(); aliasIt.hasNext();) {
            final String alias = aliasIt.next();
            entries.put(alias, otherStore.get(alias));
        }
        putAll(entries);
    }

    @Override
    public void putAll(Map<String, char[]> settings) {
        for (Map.Entry<String,char[]> entry : settings.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
        settings.clear();
    }

    @Override
    public void remove(String alias) {
        try {
            pa().removeAlias(alias);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean containsKey(String alias) {
        try {
            return pa().aliasExists(alias);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public char[] get(String alias) {
        try {
            final SecretKey secretKey = pa().getPasswordSecretKeyForAlias(alias);
            final ByteBuffer byteBuffer = ByteBuffer.wrap(secretKey.getEncoded());
            return Utility.toCharArray(UTF_8.decode(byteBuffer));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public Iterator<String> keys() {
        final List<String> keys = new ArrayList<String>();
        try {
            for (Enumeration<String> aliases = pa().getAliases(); aliases.hasMoreElements(); keys.add(aliases.nextElement())) {}
            return keys.iterator();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public int size() {
        try {
            int size = 0;
            for (Enumeration<String> aliases = pa().getAliases(); aliases.hasMoreElements(); size++, aliases.nextElement() ) {}
            return size;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
