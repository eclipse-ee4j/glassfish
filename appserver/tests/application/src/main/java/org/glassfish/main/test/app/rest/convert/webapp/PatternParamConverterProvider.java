/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.app.rest.convert.webapp;

import jakarta.inject.Singleton;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.regex.Pattern;

@Provider
@Singleton
public class PatternParamConverterProvider implements ParamConverterProvider {
    private static final Logger LOG = System.getLogger(PatternParamConverterProvider.class.getName());

    @Override
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType, final Annotation[] annotations) {
        LOG.log(Level.INFO, () -> "getConverter(rawType=" + rawType + ", genericType=" + genericType + ", annotations="
            + Arrays.toString(annotations) + ")");
        if (rawType.isAssignableFrom(Pattern.class)) {
            return new PatternConverter<>();
        }
        return null;
    }

    private static class PatternConverter<T> implements ParamConverter<T> {

        private static final Logger LOG = System.getLogger(PatternConverter.class.getName());

        @Override
        public final T fromString(final String regex) {
            LOG.log(Level.INFO, "fromString(regex={0})", regex);
            if (regex == null) {
                throw new IllegalArgumentException("Expected IAE exception");
            }
            return (T) Pattern.compile(regex);
        }


        @Override
        public final String toString(final T pattern) {
            LOG.log(Level.INFO, "toString(pattern={0})", pattern);
            return pattern == null ? null : pattern.toString();
        }
    }
}
