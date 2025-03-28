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
package org.glassfish.runnablejar.commandline;

import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.function.BiConsumer;

/**
 *
 * @author Ondro Mihalyi
 */
public class OrderedProperties extends Properties {

    private LinkedHashMap<String, String> orderedProperties = new LinkedHashMap<>();

    @Override
    public synchronized Object put(Object key, Object value) {
        orderedProperties.put((String)key, (String)value);
        return super.put(key, value);
    }

    public synchronized void forEachOrdered(BiConsumer<? super String, ? super String> action) {
        orderedProperties.forEach(action);
    }

}
