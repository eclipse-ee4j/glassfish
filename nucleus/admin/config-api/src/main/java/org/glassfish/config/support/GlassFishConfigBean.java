/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.config.support;

import java.lang.reflect.Proxy;
import java.util.StringJoiner;

import javax.xml.stream.XMLStreamReader;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBean;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigModel;
import org.jvnet.hk2.config.ConfigView;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.Transformer;

/**
 * Translated view of a configured objects where values can be represented with a @{xx.yy.zz} name to be translated
 * using a property value translator.
 *
 * @author Jerome Dochez
 */
public final class GlassFishConfigBean extends ConfigBean {

    transient TranslatedConfigView defaultView;

    /**
     * Returns the translated view of a configuration object
     *
     * @param s the config-api interface implementation
     * @return the new interface implementation providing the raw view
     */
    public static <T extends ConfigBeanProxy> T getRawView(T s) {

        Transformer rawTransformer = new Transformer() {
            @SuppressWarnings("unchecked")
            public <T extends ConfigBeanProxy> T transform(T source) {
                final ConfigView handler = (ConfigView) Proxy.getInvocationHandler(source);
                return (T) handler.getMasterView().getProxy(handler.getMasterView().getProxyType());

            }
        };

        return rawTransformer.transform(s);
    }

    public GlassFishConfigBean(ServiceLocator habitat, DomDocument document, GlassFishConfigBean parent, ConfigModel model,
            XMLStreamReader in) {
        super(habitat, document, parent, model, in);
    }

    public GlassFishConfigBean(Dom source, Dom parent) {
        super(source, parent);
    }

    @Override
    public <T extends ConfigBeanProxy> T createProxy(Class<T> proxyType) {
        TranslatedConfigView.setHabitat(getServiceLocator());
        if (defaultView == null) {
            defaultView = new TranslatedConfigView(this);
        }
        return defaultView.getProxy(proxyType);
    }

    /**
     * Returns a copy of itself
     *
     * @return a copy of itself.
     */
    @Override
    protected <T extends Dom> T copy(T parent) {
        return (T) new GlassFishConfigBean(this, parent);
    }

    @Override
    public void initializationCompleted() {
        super.initializationCompleted();
        //System.out.println( "GlassFishConfigBean.initializationCompleted() for " + getProxyType().getName() );
        for (ConfigBeanListener listener : getServiceLocator().<ConfigBeanListener>getAllServices(ConfigBeanListener.class)) {
            listener.onEntered(this);
        }
    }

    public String toString() {
        StringJoiner attributeJoiner = new StringJoiner(",");
        for (String attributeName : getAttributeNames()) {
            attributeJoiner.add(attributeName + "=" + attribute(attributeName));
        }
        return "GlassFishConfigBean." + getProxyType().getName() + "(" + attributeJoiner.toString() + ")";
    }
}
