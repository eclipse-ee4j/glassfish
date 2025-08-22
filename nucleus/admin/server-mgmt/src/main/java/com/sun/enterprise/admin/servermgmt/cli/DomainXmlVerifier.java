/*
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

package com.sun.enterprise.admin.servermgmt.cli;

// config imports
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.Result;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Dom;

/**
 * Does basic level verification of domain.xml. This is helpful as there is no DTD to validate the domain's config i.e.
 * domain.xml
 *
 * @author Nandini Ektare
 */
public class DomainXmlVerifier {

    private Domain domain;
    public boolean error;
    PrintStream _out;
    private static final LocalStringsImpl strings = new LocalStringsImpl(DomainXmlVerifier.class);

    public DomainXmlVerifier(Domain domain) throws Exception {
        this(domain, System.out);
    }

    public DomainXmlVerifier(Domain domain, PrintStream out) throws Exception {
        this.domain = domain;
        _out = out;
        error = false;
    }

    /*
     * Returns true if there is an error in the domain.xml or some other problem.
     */
    public boolean invokeConfigValidator() {
        boolean failed = false;
        try {
            failed = validate();
        } catch (Exception e) {
            failed = true;
            e.printStackTrace();
        }
        return failed;
    }

    public boolean validate() {
        try {
            checkUnique(Dom.unwrap(domain));
            if (!error)
                _out.println(strings.get("VerifySuccess"));
        } catch (Exception e) {
            error = true;
            e.printStackTrace();
        }
        return error;
    }

    private void checkUnique(Dom d) {

        try {
            Set<String> eltnames = d.getElementNames();
            Set<String> leafeltnames = d.model.getLeafElementNames();
            for (String elt : eltnames) {
                if (leafeltnames.contains(elt))
                    continue;
                List<Dom> eltlist = d.nodeElements(elt);
                checkDuplicate(eltlist);
                for (Dom subelt : eltlist) {
                    checkUnique(subelt);
                }
            }
        } catch (Exception e) {
            error = true;
            e.printStackTrace();
        }
    }

    private void output(Result result) {
        _out.println(strings.get("VerifyError", result.result()));
    }

    private void checkDuplicate(Collection<? extends Dom> beans) {
        if (beans == null || beans.size() <= 1) {
            return;
        }
        WeakHashMap keyBeanMap = new WeakHashMap();
        ArrayList<String> keys = new ArrayList<String>(beans.size());
        for (Dom b : beans) {
            String key = b.getKey();
            keyBeanMap.put(key, b);
            keys.add(key);
        }

        WeakHashMap<String, Class<ConfigBeanProxy>> errorKeyBeanMap = new WeakHashMap<String, Class<ConfigBeanProxy>>();
        String[] strKeys = keys.toArray(new String[beans.size()]);
        for (int i = 0; i < strKeys.length; i++) {
            boolean foundDuplicate = false;
            for (int j = i + 1; j < strKeys.length; j++) {
                // If the keys are same and if the indexes don't match
                // we have a duplicate. So output that error
                if ((strKeys[i].equals(strKeys[j]))) {
                    foundDuplicate = true;
                    errorKeyBeanMap.put(strKeys[i], ((Dom) keyBeanMap.get(strKeys[i])).getProxyType());
                    error = true;
                    break;
                }
            }
        }

        for (Map.Entry e : errorKeyBeanMap.entrySet()) {
            Result result = new Result(strings.get("VerifyDupKey", e.getKey(), e.getValue()));
            output(result);
        }
    }
}
