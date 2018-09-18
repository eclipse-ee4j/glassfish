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

package org.glassfish.jaccApi.programmaticauthentication;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.glassfish.jaccApi.common.ArquillianBase;
import static org.glassfish.jaccApi.common.ArquillianBase.mavenWar;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.xml.sax.SAXException;

/**
 * This tests that a call from a Servlet to HttpServletRequest#authenticate can result
 * in a successful authentication.
 *
 * 
 */
@RunWith(Arquillian.class)
public class ProgrammaticAuthenticationIT extends ArquillianBase {

    @Deployment(testable = false)
    public static Archive<?> createDeployment() {
        return mavenWar();
    }


    @Test
    public void testSubjectPrincipals() throws IOException, SAXException {
        String response = getFromServerPath("public/authenticate");
        assertTrue("Should contain web user test and architect in subject principals", response.contains("Principals: test, architect"));
    }
   

}
