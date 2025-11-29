/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
package org.glassfish.main.jnosql.jakartapersistence;


import org.glassfish.hk2.classmodel.reflect.Types;

/**
 * Info about the application provided by GlassFish.
 *
 * @author Ondro Mihalyi
 */
public class JakartaDataApplicationContext {

    final Types types;

    public JakartaDataApplicationContext(Types types) {
        this.types = types;
    }

    /**
     * Types collected during app deployment
     * @return Structure that provides info about types present in the application
     */
    public Types getTypes() {
        return types;
    }

}
