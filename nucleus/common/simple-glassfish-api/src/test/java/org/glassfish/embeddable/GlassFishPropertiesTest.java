/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates.
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

package org.glassfish.embeddable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class GlassFishPropertiesTest {

    private static final String DOMAIN_XML_ABS_WINDOWS = "file:/" + URLEncoder.encode("D:\\a\\domain.xml", UTF_8);
    private static final File DOMAIN_XML_ABS = getAbsoluteDomainXml();
    private static final Path CURRENT_DIR_ABS = new File(".").getAbsoluteFile().toPath();
    private static final File DOMAIN_XML_REL = CURRENT_DIR_ABS.relativize(DOMAIN_XML_ABS.toPath()).toFile();

    @SuppressWarnings("removal")
    @Test
    public void setConfigFileURIString() {
        GlassFishProperties props = new GlassFishProperties();
        props.setConfigFileURI(DOMAIN_XML_ABS.getAbsolutePath());
        assertEquals(DOMAIN_XML_ABS.toURI(), props.getConfigFileURI());
        props.setConfigFileURI(DOMAIN_XML_REL.getPath());
        assertEquals(DOMAIN_XML_ABS.toURI(), props.getConfigFileURI());
        props.setConfigFileURI(DOMAIN_XML_ABS_WINDOWS);
        assertEquals(DOMAIN_XML_ABS_WINDOWS, props.getConfigFileURI().toString());
        props.setConfigFileURI(null);
        assertNull(props.getConfigFileURI());
    }

    @Test
    public void setConfigFileURI() {
        GlassFishProperties props = new GlassFishProperties();
        props.setConfigFile(DOMAIN_XML_ABS.toURI());
        assertEquals(DOMAIN_XML_ABS.toURI(), props.getConfigFileURI());
        props.setConfigFile(DOMAIN_XML_REL.toURI());
        assertEquals(DOMAIN_XML_ABS.toURI(), props.getConfigFileURI());
        props.setConfigFile(URI.create(DOMAIN_XML_ABS_WINDOWS));
        assertEquals(DOMAIN_XML_ABS_WINDOWS, props.getConfigFileURI().toString());
        props.setConfigFile((URI) null);
        assertNull(props.getConfigFileURI());
    }

    @Test
    public void setConfigFile() {
        GlassFishProperties props = new GlassFishProperties();
        props.setConfigFile(DOMAIN_XML_ABS);
        assertEquals(DOMAIN_XML_ABS.toURI(), props.getConfigFileURI());
        props.setConfigFile(DOMAIN_XML_REL);
        assertEquals(DOMAIN_XML_ABS.toURI(), props.getConfigFileURI());
        props.setConfigFile((File) null);
        assertNull(props.getConfigFileURI());
    }

    private static File getAbsoluteDomainXml() {
        URL url = GlassFishPropertiesTest.class.getClassLoader().getResource("domain.xml");
        try {
            return new File(url.toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
