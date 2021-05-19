/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
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

package template;

import jakarta.servlet.ServletException;
import jakarta.servlet.jsp.JspTagException;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.SimpleTagSupport;
import java.util.HashMap;

public class InsertTag extends SimpleTagSupport {
   private String parameterName = null;
   private String definitionName = null;

   public InsertTag() {
      super();
   }
   public void setParameter(String parameter) {
      this.parameterName = parameter;
   }
   public void setDefinition(String name) {
      this.definitionName = name;
   }
   public void doTag() throws JspTagException {
    Definition definition = null;
    Parameter parameter = null;
    boolean directInclude = false;
    PageContext context = (PageContext)getJspContext();

    // get the definition from the page context
    definition = (Definition)context.getAttribute(definitionName, context.APPLICATION_SCOPE);
    // get the parameter
    if (parameterName != null && definition != null)
        parameter = (Parameter) definition.getParam(parameterName);

    if (parameter != null)
        directInclude = parameter.isDirect();

    try {
        // if parameter is direct, print to out
        if (directInclude && parameter  != null)
          context.getOut().print(parameter.getValue());
        // if parameter is indirect, include results of dispatching to page
        else {
          if ((parameter != null) && (parameter.getValue() !=  null))
              context.include(parameter.getValue());
        }
      } catch (Exception ex) {
          Throwable rootCause = null;
          if (ex instanceof ServletException) {
                              rootCause = ((ServletException) ex).getRootCause();
          }
                             throw new JspTagException(ex.getMessage(), rootCause);
            }
   }
}
