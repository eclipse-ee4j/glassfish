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
 * Used to perform string pre-processing on values found in the configuration file.
 *
 * <p>
 * This hook allows applications to support variable
 * expansions like Ant in the configuration file.
 *
 * @author Kohsuke Kawaguchi
 */
public interface Translator {
    String translate(String str) throws TranslationException;

    /**
     * {@link Translator} that does nothing.
     */
    public static final Translator NOOP = new Translator() {
        public String translate(String str) {
            return str;
        }
    };
    /**
     * A translator that does translation from the system properties. Thus, any reference to an existing
     * System Property like "${name}" will be replaced by its value, i.e. System.getProperty("name"). All
     * found references are translated. If a System Property is not defined, its reference is returned verbatim.
     * No escape sequences are handled.
     */
    public static final Translator SYS_PROP_TR = new Translator() {
        public String translate(String s) {
            StringBuilder sb = new StringBuilder();
            int length = s.length();
            int i = 0;
            while(i < length) {
                char c = s.charAt(i);
                if (c == '$' && (i+1) < length && s.charAt(i+1) == '{') {
                i += 2;
                char cc='\0';
                StringBuilder prop = new StringBuilder();
                while (i < length && (cc=s.charAt(i)) != '}') {
                  prop.append(cc);
                  i++;
                }
                if (cc == '}') {
                  String value = System.getProperty(prop.toString());
                  if (value != null)
                      sb.append(value);
                  else //return reference to non-existent system-property verbatim
                      sb.append("${" + prop + "}");
                  i++;
                } else { //we reached the end, no } found
                  sb.append("${").append(prop);
                }
              } else {
                sb.append(c);
                i++;
              }
          }
          return sb.toString();
        }
    };
}
