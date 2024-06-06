/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.deploy.shared;

import java.net.URI;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tim
 */
public class JarArchiveTest {

    @Test
    void noname() throws Exception {
        URI uri = new URI("jar:///");
        assertEquals("", JarArchive.getName(uri));
    }

    @Test
    void justSuffix() throws Exception {
        URI uri = new URI("jar:///.war");
        assertEquals("", JarArchive.getName(uri));
    }

    @Test
    public void shortSubPaths() throws Exception {
        URI uri = new URI("jar:///a/b/c/d.war");
        assertEquals("d", JarArchive.getName(uri));
    }


    @Test
    public void noFileType() throws Exception {
        URI uri = new URI("jar:///aaaaa/bbbb/cc/x");
        assertEquals("x", JarArchive.getName(uri));
    }


    @Test
    public void trailingDot() throws Exception {
        URI uri = new URI("jar:///ww/xx/yy/z.");
        assertEquals("z", JarArchive.getName(uri));
    }


    @Test
    public void multipleDots() throws Exception {
        URI uri = new URI("jar:///ww/xx/yy/this.is.my.jar");
        assertEquals("this.is.my", JarArchive.getName(uri));
    }
}
