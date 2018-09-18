<!--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- loadDefaultButton.inc -->

    <!-- To use this general include files for page buttons,  the following page Session attributes should be set:
        setPageSessionAttribute(key="showDefaultButton" value="#{false}" );

        We only show this button if #{pageSession.edit} is set to true.  refer to issue#8660
    -->

    <sun:button id="loadDefaultsButton" style="margin-left: 8pt"  rendered="#{pageSession.showDef}" primary="#{false}" text="$resource{i18n.button.LoadDefaults}"
        onClick="submitAndDisable(this, '$resource{i18n.button.Processing}'); return false;" >
        <!beforeCreate
            if (!#{pageSession.edit}){
                setPageSessionAttribute(key="showDef" value="#{false}");
            }
            if (#{pageSession.edit}){
                setPageSessionAttribute(key="showDef" value="#{pageSession.showDefaultButton}");
            }
        />
        <!command
            gf.getDefaultValues(endpoint="#{pageSession.parentUrl}/#{pageSession.childType}" orig="#{pageSession.valueMap}" valueMap="#{pageSession.valueMap}")
            if(#{pageSession.has2Objects}&#{pageSession.parentUrl2}){
                gf.getDefaultValues(endpoint="#{pageSession.parentUrl2}/#{pageSession.childType2}" orig="#{pageSession.valueMap2}" valueMap="#{pageSession.valueMap2}")
            }
        />
    </sun:button>
   
