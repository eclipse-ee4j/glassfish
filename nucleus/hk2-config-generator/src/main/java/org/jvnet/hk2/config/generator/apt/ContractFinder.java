/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.generator.apt;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.ContractsProvided;

/**
 * Given a {@link TypeElement},
 * find all super-types that have {@link Contract},
 * including ones pointed by {@link ContractsProvided}.
 *
 * @author Kohsuke Kawaguchi
 */
public class ContractFinder {
    /**
     * The entry point.
     */
    public static Set<TypeElement> find(TypeElement d) {
        LinkedHashSet<TypeElement> retVal = new LinkedHashSet<>();

        for (Map.Entry<String, TypeElement> entry : new ContractFinder().check(d).result.entrySet()) {
            retVal.add(entry.getValue());
        }

        return retVal;
    }

    /**
     * Converts the Name from the Element to a String
     * @param name
     * @return
     */
    private static String convertNameToString(Name name) {
        if (name == null) {
            return null;
        }
        return name.toString();
    }

    /**
     * {@link TypeElement}s whose contracts are already checked.
     */
    private final Set<TypeElement> checkedInterfaces = new HashSet<>();

    private final TreeMap<String, TypeElement> result = new TreeMap<>();

    private ContractFinder() {
    }

    private ContractFinder check(TypeElement d) {

        // traverse up the inheritance tree and find all supertypes that have @Contract
        while(true) {
            checkSuperInterfaces(d);
            if (ElementKind.CLASS.equals(d.getKind())) {
                checkContract(d);
                TypeMirror sc = d.getSuperclass();
                if (sc.getKind().equals(TypeKind.NONE)) {
                    break;
                }
                d = (TypeElement) ((DeclaredType) sc).asElement();
            } else {
                break;
            }
        }

        return this;
    }

    private void checkContract(TypeElement type) {
        if (type.getAnnotation(Contract.class) != null) {
            result.put(convertNameToString(type.getQualifiedName()), type);
        }
    }

    private void checkSuperInterfaces(TypeElement d) {
        for (TypeMirror intf : d.getInterfaces()) {
            TypeElement i = (TypeElement) ((DeclaredType) intf).asElement();
            if(checkedInterfaces.add(i)) {
                checkContract(i);
                checkSuperInterfaces(i);
            }
        }
    }
}
