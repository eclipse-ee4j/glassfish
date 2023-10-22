/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.test.example;

import javax.xml.stream.XMLStreamReader;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.OnDeleteCascade;

public class SimpleDocument extends DomDocument<ConfigBean>{

    public SimpleDocument(ServiceLocator habitat) {
        super(habitat);
    }

    @Override
    public ConfigBean make(final ServiceLocator habitat, XMLStreamReader xmlStreamReader,
            ConfigBean dom, ConfigModel configModel) {
        ConfigBean configBean = new ConfigBean(habitat,this, dom, configModel, xmlStreamReader);
        configBean.addInterceptor(Object.class, new OnDeleteCascade());
        return configBean;
    }
}
