/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.xml.ws.api.BindingID;

import jakarta.xml.ws.handler.Handler;
import jakarta.xml.ws.handler.HandlerResolver;
import jakarta.xml.ws.handler.PortInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This implements the HandlerResolver interface introduced in JAXWS
 */
public class HandlerResolverImpl implements HandlerResolver {

    private final Map<PortInfo, List<Handler>> chainMap;

    public HandlerResolverImpl() {
        chainMap = new HashMap<>();
    }

    @Override
    public List<Handler> getHandlerChain(PortInfo info) {
        Iterator<PortInfo> piSet = chainMap.keySet().iterator();
        List<Handler> chain = null;
        while (piSet.hasNext()) {
            PortInfo next = piSet.next();
            PortInfoImpl tmp = new PortInfoImpl(BindingID.parse(info.getBindingID()), info.getPortName(),
                info.getServiceName());
            if (tmp.equals(next)) {
                chain = chainMap.get(next);
                break;
            }
        }
        if (chain == null) {
            chain = new ArrayList<>();
        }
        return chain;
    }


    public void setHandlerChain(PortInfo info, List<Handler> chain) {
        List<Handler> currentList = chainMap.get(info);
        if (currentList == null) {
            chainMap.put(info, chain);
        } else {
            currentList.addAll(chain);
        }
    }
}
