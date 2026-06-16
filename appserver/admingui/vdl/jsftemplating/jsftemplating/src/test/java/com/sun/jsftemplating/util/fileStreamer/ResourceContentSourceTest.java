/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

/**
 *
 */
package com.sun.jsftemplating.util.fileStreamer;

import org.junit.Assert;
import org.junit.Test;

/**
 * TestCase for <code>ResourceContentSource</code>.
 */
public class ResourceContentSourceTest {

    @Test(expected = IllegalArgumentException.class)
    public void normalizeDoesNotAccessContextRootParent() {
        final String testPath = "../bad/path";
        ResourceContentSource.normalize(testPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void normalizeDoesNotAccessContextRootParentWithLeadingSlash() {
        final String testPath = "/../bad/path";
        ResourceContentSource.normalize(testPath);
    }

    @Test(expected = IllegalArgumentException.class)
    public void doesNotGoBackTooFar() {
        final String testPath = "/path/../../../../too/many/backward";
        ResourceContentSource.normalize(testPath);
    }

    @Test
    public void removesExtraSlashesAndBackwardPaths() {
        final String testPath = "//OK/path//with/extra/slashes/and/..//in/the/middle/";
        final String result = ResourceContentSource.normalize(testPath);
        Assert.assertEquals("Wrong result", "OK/path/with/extra/slashes/in/the/middle", result);
    }
}
