/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.annotation.handlers;

import jakarta.enterprise.concurrent.ContextServiceDefinition;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.config.support.TranslatedConfigView;
import org.jvnet.hk2.annotations.Service;

/**
 * @author David Matejcek
 */
@Service
class ContextServiceDefinitionConverter {
    private static final Logger LOG = System.getLogger(ContextServiceDefinitionConverter.class.getName());

    Set<ContextServiceDefinitionData> convert(ContextServiceDefinition[] definitions) {
        LOG.log(Level.TRACE, "convert(definitions={0})", (Object) definitions);
        if (definitions == null) {
            return Collections.emptySet();
        }
        return Arrays.stream(definitions).map(this::convert).collect(Collectors.toSet());
    }


    ContextServiceDefinitionData convert(ContextServiceDefinition definition) {
        LOG.log(Level.DEBUG, "convert(definition={0})", definition);
        Set<String> unused = collectUnusedContexts(definition);
        ContextServiceDefinitionData data = new ContextServiceDefinitionData();
        data.setName(new SimpleJndiName(TranslatedConfigView.expandValue(definition.name())));
        data.setPropagated(evaluateContexts(definition.propagated(), unused));
        data.setCleared(evaluateContexts(definition.cleared(), unused));
        data.setUnchanged(evaluateContexts(definition.unchanged(), unused));
        return data;
    }


    private Set<String> collectUnusedContexts(ContextServiceDefinition definition) {
        Set<String> usedContexts = new HashSet<>();
        for (String context : definition.propagated()) {
            usedContexts.add(context);
        }
        for (String context : definition.cleared()) {
            boolean previous = usedContexts.add(context);
            if (!previous) {
                throw new IllegalArgumentException("Duplicit context " + context + " in " + usedContexts
                    + " and cleared context attributes in ContextServiceDefinition annotation!");
            }
        }
        for (String context : definition.unchanged()) {
            boolean previous = usedContexts.add(context);
            if (!previous) {
                throw new IllegalArgumentException("Duplicit context " + context + " in " + previous
                    + " and unchanged context attributes in ContextServiceDefinition annotation!");
            }
        }
        Set<String> allStandardContexts = new HashSet<>(Set.of(
            ContextServiceDefinition.APPLICATION,
            ContextServiceDefinition.SECURITY,
            ContextServiceDefinition.TRANSACTION));
        allStandardContexts.removeAll(usedContexts);
        return allStandardContexts;
    }


    private Set<String> evaluateContexts(String[] sourceContexts, Set<String> unusedContexts) {
        Set<String> contexts = new HashSet<>();
        for (String context : sourceContexts) {
            if (ContextServiceDefinition.ALL_REMAINING.equals(context)) {
                contexts.addAll(unusedContexts);
                contexts.add(ContextServiceDefinition.ALL_REMAINING);
            } else {
                contexts.add(context);
            }
        }
        return contexts;
    }
}
