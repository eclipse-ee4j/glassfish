/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.test.authpropagation;

import jakarta.annotation.*;
import jakarta.annotation.sql.DataSourceDefinition;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@DataSourceDefinition(
        name = "java:app/primaryDS",
        className = "org.apache.derby.jdbc.ClientXADataSource",
        portNumber = 1527,
        serverName = "localhost",
        user = "APP",
        password = "APP",
        databaseName="db",
        properties = {"connectionAttributes=;create=true"}
)
@Singleton
@Startup
public class DataSourceInitializer {

    @PersistenceContext(unitName = "primaryPU")
    private EntityManager em;

    @Resource(lookup = "java:app/primaryDS")
    private DataSource dataSource;

    @PostConstruct
    public void init() {
        List<AuthGroup> groups = new ArrayList<>();
        MyUser user = new MyUser("user1", hash("user1"), groups);

        em.persist(user);

        List<MyUser> users = new ArrayList<>();
        users.add(user);
        AuthGroup group = new AuthGroup("group1", users);
        em.persist(group);

        groups.add(group);
    }

    private String hash(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(plainPassword.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private final static char[] hexArray = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
