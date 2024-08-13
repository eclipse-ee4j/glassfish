/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation
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
package org.apache.catalina.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.naming.NamingException;
import javax.naming.directory.DirContext;

import org.apache.catalina.LifecycleException;
import org.apache.naming.resources.FileDirContext;
import org.apache.naming.resources.Resource;
import org.apache.naming.resources.UrlResource;
import org.junit.jupiter.api.Test;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StandardContextTest {

    /**
     * Covers the fix for https://github.com/eclipse-ee4j/glassfish/issues/24083
     */
    @Test
    public void testServletContextGetResource() throws MalformedURLException, NamingException, LifecycleException, IOException {
        final String RESOURCE_RELATIVE_PATH = "file.txt";
        final String RESOURCE_PATH = "/" + RESOURCE_RELATIVE_PATH;
        final URL RESOURCE_URL = new File(RESOURCE_PATH).toURI().toURL();

        final StandardContext standardContext = new StandardContext();
        standardContext.setName("testContext");

        final Path realDirectory = Files.createTempDirectory("glassfish-test");
        Path realFile = Files.createFile(realDirectory.resolve(RESOURCE_RELATIVE_PATH));
        try {
            standardContext.setDocBase(realDirectory.toString());

            DirContext testDirContext = mockDirContext(RESOURCE_URL, RESOURCE_PATH);

            standardContext.setResources(testDirContext);
            standardContext.resourcesStart();
            try {
                assertNotNull(standardContext.getServletContext().getRealPath(RESOURCE_PATH));
                assertEquals(RESOURCE_URL, standardContext.getResource(RESOURCE_PATH));
            } finally {
                standardContext.resourcesStop();
            }
        } finally {
            Files.deleteIfExists(realFile);
            Files.deleteIfExists(realDirectory);
        }
    }

    private DirContext mockDirContext(URL resourceUrl, String resourcePath) throws NamingException {
        FileDirContext resultDirContext = createNiceMock(FileDirContext.class);

        class TestResource extends Resource implements UrlResource {

            @Override
            public URL getUrl() throws MalformedURLException {
                return resourceUrl;
            }

        }

        expect(resultDirContext.lookup(resourcePath)).andReturn(new TestResource());
        replay(resultDirContext);

        return resultDirContext;
    }

}
