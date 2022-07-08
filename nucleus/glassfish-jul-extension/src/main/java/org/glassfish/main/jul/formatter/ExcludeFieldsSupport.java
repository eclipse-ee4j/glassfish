/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.formatter;

import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;


/**
 * @author sanshriv
 * @author David Matejcek
 */
public class ExcludeFieldsSupport {

    /**
     * Additional log record attributes.
     */
    public enum SupplementalAttribute {
        /** Thread id and name */
        TID("tid"),
        /** Integer value of the log level. See {@link Level#intValue()} */
        LEVEL_VALUE("levelValue"),
        ;

        private final String id;

        SupplementalAttribute(final String id) {
            this.id = id;
        }


        /**
         * @return name of the attribute in logging.properties
         */
        public String getId() {
            return this.id;
        }
    }

    private final BitSet excludedAttributes = new BitSet(SupplementalAttribute.values().length);

    /**
     * @param id
     * @return {@link SupplementalAttribute} if such exists with the same id.
     */
    public static SupplementalAttribute getById(final String id) {
        for (SupplementalAttribute value : SupplementalAttribute.values()) {
            if (value.getId().equals(id)) {
                return value;
            }
        }
        return null;
    }


    /**
     * @param excludeFields comma-separated list of {@link SupplementalAttribute} names.
     */
    public void setExcludedFields(final String excludeFields) {
        excludedAttributes.clear();
        if (excludeFields == null || excludeFields.isEmpty()) {
            return;
        }
        final String[] fields = excludeFields.split(",");
        for (final String field : fields) {
            final SupplementalAttribute found = getById(field);
            if (found == null) {
                GlassFishLoggingTracer.error(getClass(), "Ignoring excluded field because no such exists: " + field);
            } else {
                excludedAttributes.set(found.ordinal());
            }
        }
    }


    /**
     * @param attribute
     * @return true if the attribute should be excluded.
     */
    public boolean isSet(final SupplementalAttribute attribute) {
        return excludedAttributes.get(attribute.ordinal());
    }


    /**
     * Returns excluded field identificators, separated by comma.
     */
    @Override
    public String toString() {
        return Arrays.stream(SupplementalAttribute.values()).filter(this::isSet).map(SupplementalAttribute::getId)
            .collect(Collectors.joining(","));
    }
}
