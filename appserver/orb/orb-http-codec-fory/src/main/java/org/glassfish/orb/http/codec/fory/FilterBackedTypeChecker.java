/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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


package org.glassfish.orb.http.codec.fory;

import java.io.ObjectInputFilter;

import org.apache.fory.resolver.TypeChecker;
import org.apache.fory.resolver.TypeResolver;

/**
 * Applies the transport's deserialization filter to Fory.
 * <p>
 * Fory can be told to require every class to be registered up front, which is
 * safe but defeats the purpose here: an application would have to enumerate
 * its own types to gain a codec it never asked for. The alternative - letting
 * any named class be instantiated from the wire - is the deserialization
 * gadget vector, and swapping codecs must not quietly widen what an attacker
 * can reach.
 * <p>
 * So the same {@link ObjectInputFilter} that guards Java serialization guards
 * this codec too. One policy, two codecs: changing the codec changes the
 * encoding and nothing about what is allowed to be decoded.
 */
final class FilterBackedTypeChecker implements TypeChecker {

    private final ClassLoader loader;

    private final ObjectInputFilter filter;

    FilterBackedTypeChecker(ClassLoader loader, ObjectInputFilter filter) {
        this.loader = loader;
        this.filter = filter;
    }

    @Override
    public boolean checkType(TypeResolver resolver, String className) {
        Class<?> type;
        try {
            // initialize=false: resolving a name must not run static
            // initialisers, which would be code execution before the filter
            // has had any say.
            type = Class.forName(className, false, loader);
        } catch (ClassNotFoundException | LinkageError e) {
            return false;
        }

        return filter.checkInput(new NameOnlyFilterInfo(type)) != ObjectInputFilter.Status.REJECTED;
    }

    /**
     * The filter contract is written for a stream being decoded incrementally;
     * Fory asks a narrower question - "is this class acceptable at all". Only
     * the class is known, so the size and depth limits report as unknown
     * ({@code -1}), which is what the contract prescribes for a value that is
     * not available rather than a value that is zero.
     */
    private record NameOnlyFilterInfo(Class<?> serialClass) implements ObjectInputFilter.FilterInfo {

        @Override
        public long arrayLength() {
            return -1;
        }

        @Override
        public long depth() {
            return -1;
        }

        @Override
        public long references() {
            return -1;
        }

        @Override
        public long streamBytes() {
            return -1;
        }
    }
}
