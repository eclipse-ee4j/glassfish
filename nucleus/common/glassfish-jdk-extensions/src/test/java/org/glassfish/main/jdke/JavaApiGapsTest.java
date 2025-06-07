/*
 * Copyright (c) 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jdke;

import java.util.Hashtable;

import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;

import org.junit.jupiter.api.Test;

public class JavaApiGapsTest {

    @Test
    void setAndUnsetNamingManagerInitialContextFactoryBuilder() throws NamingException {
        JavaApiGaps.setInitialContextFactoryBuilder(new TestInitialContextFactoryBuilder());
        JavaApiGaps.unsetInitialContextFactoryBuilder();
    }


    private static final class TestInitialContextFactoryBuilder implements InitialContextFactoryBuilder {

        @Override
        public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment) throws NamingException {
            return null;
        }
    }
}
