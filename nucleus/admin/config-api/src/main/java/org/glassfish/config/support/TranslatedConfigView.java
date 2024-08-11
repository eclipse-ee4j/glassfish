/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.security.store.DomainScopedPasswordAliasStore;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivilegedAction;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigView;

/**
 * View that translate configured attributes containing properties like ${foo.bar} into system properties values.
 *
 * @author Jerome Dochez
 */
public class TranslatedConfigView implements ConfigView {

    final static Pattern p = Pattern.compile("([^\\$]*)\\$\\{([^\\}]*)\\}([^\\$]*)");

    private static final String ALIAS_TOKEN = "ALIAS";
    private static int MAX_SUBSTITUTION_DEPTH = 100;

    public static String expandValue(String value) {
        return (String) getTranslatedValue(value);
    }

    public static Object getTranslatedValue(Object value) {
        if (value != null && value instanceof String) {
            String stringValue = value.toString();
            if (stringValue.indexOf('$') == -1) {
                return value;
            }
            if (domainPasswordAliasStore() != null) {
                if (getAlias(stringValue) != null) {
                    try {
                        return getRealPasswordFromAlias(stringValue);
                    } catch (Exception e) {
                        Logger.getAnonymousLogger()
                                .severe(Strings.get("TranslatedConfigView.aliaserror", stringValue, e.getLocalizedMessage()));
                        return stringValue;
                    }
                }
            }

            // Perform system property substitution in the value
            // The loop limit is imposed to prevent infinite looping to values
            // such as a=${a} or a=foo ${b} and b=bar {$a}
            Matcher m = p.matcher(stringValue);
            String origValue = stringValue;
            int i = 0;
            while (m.find() && i < MAX_SUBSTITUTION_DEPTH) {
                String newValue = System.getProperty(m.group(2).trim());
                if (newValue != null) {
                    stringValue = m.replaceFirst(Matcher.quoteReplacement(m.group(1) + newValue + m.group(3)));
                    m.reset(stringValue);
                }
                i++;
            }
            if (i >= MAX_SUBSTITUTION_DEPTH) {
                Logger.getAnonymousLogger().severe(Strings.get("TranslatedConfigView.badprop", i, origValue));
            }
            return stringValue;
        }
        return value;
    }

    final ConfigView masterView;

    TranslatedConfigView(ConfigView master) {
        this.masterView = master;

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return getTranslatedValue(masterView.invoke(proxy, method, args));
    }

    @Override
    public ConfigView getMasterView() {
        return masterView; //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setMasterView(ConfigView view) {
        // immutable implementation
    }

    @Override
    public <T extends ConfigBeanProxy> Class<T> getProxyType() {
        return masterView.getProxyType();
    }

    @Override
    public <T extends ConfigBeanProxy> T getProxy(Class<T> proxyType) {
        return proxyType.cast(Proxy.newProxyInstance(proxyType.getClassLoader(), new Class[] { proxyType }, this));
    }

    static ServiceLocator habitat;

    public static void setHabitat(ServiceLocator h) {
        habitat = h;
    }

    private static DomainScopedPasswordAliasStore domainPasswordAliasStore = null;

    private static DomainScopedPasswordAliasStore domainPasswordAliasStore() {
        domainPasswordAliasStore = AccessController.doPrivileged(new PrivilegedAction<DomainScopedPasswordAliasStore>() {
            @Override
            public DomainScopedPasswordAliasStore run() {
                return habitat.getService(DomainScopedPasswordAliasStore.class);
            }
        });
        return domainPasswordAliasStore;
    }

    /**
     * check if a given property name matches AS alias pattern ${ALIAS=aliasname}. if so, return the aliasname, otherwise
     * return null.
     *
     * @param propName The property name to resolve. ex. ${ALIAS=aliasname}.
     * @return The aliasname or null.
     */
    static public String getAlias(String propName) {
        String aliasName = null;
        String starter = "${" + ALIAS_TOKEN + "="; //no space is allowed in starter
        String ender = "}";

        propName = propName.trim();
        if (propName.startsWith(starter) && propName.endsWith(ender)) {
            propName = propName.substring(starter.length());
            int lastIdx = propName.length() - 1;
            if (lastIdx > 1) {
                propName = propName.substring(0, lastIdx);
                aliasName = propName.trim();
            }
        }
        return aliasName;
    }

    public static String getRealPasswordFromAlias(final String at)
            throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException {

        final String an = getAlias(at);
        final boolean exists = domainPasswordAliasStore.containsKey(an);
        if (!exists) {

            final String msg = String.format("Alias  %s does not exist", an);
            throw new IllegalArgumentException(msg);
        }
        final String real = new String(domainPasswordAliasStore.get(an));
        return (real);
    }

}
