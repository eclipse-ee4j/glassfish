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
package org.glassfish.microprofile.config.util;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 *
 * @author Ondro Mihalyi
 */
public class LazyProperty {

    public static <OBJ, PROP> PROP getOrSet(OBJ object, Function<OBJ, PROP> getter, Supplier<PROP> propertyProducer, BiConsumer<OBJ, PROP> setter) {
        PROP result = getter.apply(object);
        if (result == null) {
            synchronized (object) {
                result = propertyProducer.get();
                setter.accept(object, result);
            }
        }
        return result;
    }

}
