<!--

    Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

-->

<event>
        <!beforeEncode
          if ($attribute{alertSummary}){
            decodeString(str="$attribute{alertSummary}", output=>$attribute{alertSummary});
            decodeString(str="$attribute{alertDetail}", output=>$attribute{alertDetail});
          }
          if (!$attribute{alertSummary}){
            getRequestValue(key="alertType" value="#{requestScope.alertType}");
            getRequestValue(key="alertSummary" value="#{requestScope.alertSummary}");
            getRequestValue(key="alertDetail" value="#{requestScope.alertDetail}");
          }

          if ("!#{escapedAlertDetail}"){
              htmlEscape(value="#{alertSummary}" result="#{requestScope.alertSummary}");
              htmlEscape(value="#{alertDetail}" result="#{requestScope.alertDetail}");
          }
          if ("#{escapedAlertDetail}"){
              setAttribute(key="alertSummary" value="#{alertSummary}");
              setAttribute(key="alertDetail" value="#{alertDetail}");
          }
			
        />
</event>

<sun:alert id="pageAlert"  
            type="#{alertType}" 
            summary="#{alertSummary}"
            detail="#{alertDetail}"
            alt="#{alertSummary} #{alertDetail}">
    <!beforeCreate
        if ($attribute{alertSummary}){
            decodeString(str="$attribute{alertSummary}", output=>$attribute{alertSummary});
            decodeString(str="$attribute{alertDetail}", output=>$attribute{alertDetail});
          }
          if (!$attribute{alertSummary}){
            getRequestValue(key="alertType" value="#{requestScope.alertType}");
            getRequestValue(key="alertSummary" value="#{requestScope.alertSummary}");
            getRequestValue(key="alertDetail" value="#{requestScope.alertDetail}");
          }
      />
</sun:alert>

