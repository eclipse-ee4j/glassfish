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

package org.jvnet.hk2.config;

/**
 * {@link Translator} that does variable resolution in the Ant style.
 *
 * <p>
 * This implementation looks for variables in the string like
 * "${xyz}" or "${abc.DEF.ghi}". The {@link #getVariableValue(String)} method
 * is then used to obtain the actual value for the variable.
 *
 * <p>
 * "$$" works as the escape of "$", so for example "$${abc}" expands to "${abc}"
 * where "${abc}" would have expanded to "value-of-abc".
 * A lone "$" is left as-is, so "$abc" expands to "$abc".
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class VariableResolver implements Translator {
    public String translate(String str) throws TranslationException {
        if(str.indexOf('$')==-1)
            return str; // fast path for the common case

        int idx = 0;
        StringBuilder buf = new StringBuilder();
        while(true) {
            int s = str.indexOf('$',idx);
            if(s==-1) {
                buf.append(str,idx,str.length());
                return buf.toString();
            }

            // copy until this '$'
            buf.append(str,idx,s);

            if(s+1==str.length()) {
                buf.append('$');    // '$' was the last char
                idx=s+1;
                continue;
            }

            char second = str.charAt(s + 1);
            switch(second) {
            case '{': // variable
                int e = str.indexOf('}',s+2);
                if(e ==-1)
                    throw new TranslationException("Missing '}' at the end of \""+str+"\"");
                String varName = str.substring(s+2, e);
                String value;
                try {
                    value = getVariableValue(varName);
                } catch (TranslationException x) {
                    throw new TranslationException("Failed to expand variable ${"+varName+'}',x);
                }
                if (value == null)
                    throw new TranslationException(String.format("Undefined variable ${%s} in \"%s\"",
                        varName, str));
                buf.append(value);
                idx = e+1;
                break;
            case '$': // $ escape
                buf.append('$');
                idx=s+2;
                break;
            default:
                buf.append('$');
                idx=s+1;
                break;
            }
        }
    }

    /**
     * Returns the value of the variable.
     *
     * This class will not try to further expand variables in the returned value.
     * If the implementation wants to do so, that is the implementation's responsibility.
     *
     * @return
     *      null if the variable is not found. The caller will report an error.
     *      When the variable is not found, it's also legal to throw {@link TranslationException},
     *      which is an useful technique if the implementation would like to report
     *      additional errors.
     */
    protected abstract String getVariableValue(String varName) throws TranslationException;
}
