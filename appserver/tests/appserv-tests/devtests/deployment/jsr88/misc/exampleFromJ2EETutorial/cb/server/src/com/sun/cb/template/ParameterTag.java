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
import jakarta.servlet.jsp.tagext.*;
import jakarta.servlet.jsp.PageContext;
import java.util.*;

public class ParameterTag extends SimpleTagSupport {
   private String paramName = null;
   private String paramValue = null;
   private String isDirectString = null;

   public ParameterTag() {
      super();
   }
   public void setName(String paramName) {
      this.paramName = paramName;
   }
   public void setValue(String paramValue) {
      this.paramValue = paramValue;
   }
   public void setDirect(String isDirectString) {
      this.isDirectString = isDirectString;
   }
   public void doTag() {
      boolean isDirect = false;

      if ((isDirectString != null) &&
         isDirectString.toLowerCase().equals("true"))
         isDirect = true;

      try {
         // retrieve the parameters list
        if (paramName != null) {
          ArrayList parameters = ((ScreenTag)getParent()).getParameters();
          if (parameters != null) {
            Parameter param = new Parameter(paramName, paramValue, isDirect);
            parameters.add(param);
          } else
            Debug.println("ParameterTag: parameters do not exist.");
        }
      } catch (Exception e) {
         Debug.println("ParameterTag: error in doTag: " + e);
      }
   }
}
