/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.naming.impl;

import com.sun.enterprise.naming.impl.test.ServerExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.glassfish.internal.api.Globals;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.omg.CORBA.LocalObject;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author David Matejcek
 */
@ExtendWith(ServerExtension.class)
public class SerialContextTest {

    private static SerialContext ctx;

    @BeforeAll
    public static void init() throws Exception {
        ctx = new SerialContext(null, Globals.getStaticBaseServiceLocator());
    }


    @AfterAll
    public static void closeCtx() throws Exception {
        if (ctx != null) {
            ctx.close();
        }
        ProviderManager.getProviderManager().getTransientContext().close();
    }


    @Test
    public void lookupOfNonExisting() throws Exception {
        assertThrows(NamingException.class, () -> ctx.lookup("java:comp/env/to-be-ingored"));
    }


    @Test
    public void corba() throws Exception {
        LocalObject localObject = new LocalObject();
        ctx.bind("corbaname:localobject", localObject);
        assertSame(localObject, ctx.lookup("corbaname:localobject"));
        assertThat(Collections.list(ctx.list("")).stream()
            .map(NameClassPair::getName).collect(Collectors.toList()), hasItems("corbaname:localobject"));
    }


    @Test
    public void jdbc() throws Exception {
        Map<String, String> fakeDS = Map.of();
        String jndiName = "jdbc:derby://localhost:1527/derbyDB;create=true";
        ctx.bind(jndiName, fakeDS);
        assertSame(fakeDS, ctx.lookup(jndiName));
        NamingEnumeration<NameClassPair> enumeration = ctx.list("");
        List<String> names = Collections.list(enumeration).stream().map(NameClassPair::getName)
            .collect(Collectors.toList());
        assertAll(
            () -> assertSame(fakeDS, ctx.lookup(jndiName)),
            () -> assertThat(names, hasItems("jdbc:derby:"))
        );
        ctx.unbind(jndiName);
    }
}
