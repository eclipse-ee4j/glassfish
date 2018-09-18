<!--

    Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.

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

<sun:button id="saveButton" rendered="#{edit}" text="$resource{i18n.button.Save} "
    onClick="if (checkSSHPswd('#{pageSession.typeId}','#{pageSession.nodeHostId}',
        '#{pageSession.psSelectId}', '#{pageSession.ps1Id}', 
        '#{pageSession.setupSshId}', '#{pageSession.setupPswdId}',
        '#{pageSession.winPsSelectId}', '#{pageSession.winPs1Id}' ))
    {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;"
>
    <!command
        setAttribute(key="includeInstallDir" value="false");
        if ("#{pageSession.valueMap['type']}=CONFIG"){
            setAttribute(key="attrsList" value={"nodehost", "nodedir" });
            setAttribute(key="endpoint" value="nodes/node/#{pageSession.nodeName}/update-node-config");
            gfr.prepareNodeCmd();
            mapPut(map="#{pageSession.attrsMap}", key="installdir" value="#{pageSession.valueMap['installdirConfig']}");
        }
        if ("#{pageSession.valueMap['type']}=SSH"){
            gfr.fixPswd();
            if (!#{pageSession.valueMap['force']}){
                mapPut(map="#{pageSession.valueMap}"  key="force" value="false");
            }
            setAttribute(key="attrsList" value={"nodehost", "nodedir", "installdir", "sshport", "sshuser", "sshpassword" "sshkeyfile", "force"});
            setAttribute(key="endpoint" value="nodes/node/#{pageSession.nodeName}/update-node-ssh");
            gfr.prepareNodeCmd();
            //Run setup-ssh
            if ("#{pageSession.valueMap['setupSsh']}=true"){
                createMap(result="#{pageSession.sMap}");
                if ("#{pageSession.valueMap['generatekey']}"){
                    mapPut(map="#{pageSession.sMap}"  key="generatekey" value="true");
                }
                if ("#{pageSession.setupPswd}"){
                    mapPut(map="#{pageSession.sMap}", key="sshpassword" value="#{pageSession.setupPswd}");
                }
                mapPut(map="#{pageSession.sMap}", key="hosts" value="#{pageSession.valueMap['nodehost']}");
                foreach(var="sattr" list={"nodehost", "sshport", "sshuser",  "sshkeyfile" "sshpublickeyfile" } ){
                    mapGet(Map="#{pageSession.valueMap}" Key="#{requestScope.sattr}" Value="#{requestScope.sval}");
                    if ("#{requestScope.sval}"){
                        mapPut(map="#{pageSession.sMap}", key="#{requestScope.sattr}" value="#{requestScope.sval}");
                    }
                }
                gf.restRequest( endpoint="#{sessionScope.REST_URL}/nodes/setup-ssh" attrs="#{pageSession.sMap}" method="POST" )
            }
        }
        gf.restRequest(endpoint="#{sessionScope.REST_URL}/#{requestScope.endpoint}" method="POST" attrs="#{pageSession.attrsMap}")
        prepareSuccessfulMsg();
        gf.redirect(page="#{request.contextPath}/cluster/#{selfPage}&alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
    />
</sun:button>

<sun:button id="newButton" rendered="#{!edit}" text="$resource{i18n.button.OK}"
    onClick="if (checkSSHPswd('#{pageSession.typeId}', '#{pageSession.nodeHostId}',
        '#{pageSession.psSelectId}', '#{pageSession.ps1Id}',
        '#{pageSession.setupSshId}', '#{pageSession.setupPswdId}',
        '#{pageSession.winPsSelectId}', '#{pageSession.winPs1Id}'))
    {submitAndDisable(this, '$resource{i18n.button.Processing}');}; return false;"
>
<!command
    setAttribute(key="nameToCheck" value="#{pageSession.valueMap['id']}");
    gfj.checkNameExist();
    setAttribute(key="includeInstallDir" value="true");
    if ("#{pageSession.valueMap['type']}=CONFIG"){
        setAttribute(key="attrsList" value={"id", "nodehost", "nodedir"});
        setAttribute(key="endpoint" value="nodes/create-node-config");
        gfr.prepareNodeCmd();
        mapPut(map="#{pageSession.attrsMap}", key="installdir" value="#{pageSession.valueMap['installdirConfig']}");
    }
    if ("#{pageSession.valueMap['type']}=SSH"){
        gfr.fixPswd();
        setAttribute(key="attrsList" value={"id", "nodehost", "nodedir", "installdir", "sshport", "sshuser", "sshpassword" "sshkeyfile", "force", "install"});
        setAttribute(key="endpoint" value="nodes/create-node-ssh");
        if (!#{pageSession.valueMap['force']}){
            mapPut(map="#{pageSession.valueMap}"  key="force" value="false");
        }
        gfr.prepareNodeCmd();

        //Run setup-ssh
        if ("#{pageSession.valueMap['setupSsh']}=true"){
            createMap(result="#{pageSession.sMap}");
            if ("#{pageSession.valueMap['generatekey']}"){
                mapPut(map="#{pageSession.sMap}"  key="generatekey" value="true");
            }
            if ("#{pageSession.setupPswd}"){
                mapPut(map="#{pageSession.sMap}", key="sshpassword" value="#{pageSession.setupPswd}");
            }
            mapPut(map="#{pageSession.sMap}", key="hosts" value="#{pageSession.valueMap['nodehost']}");
            foreach(var="sattr" list={"nodehost", "sshport", "sshuser",  "sshkeyfile" "sshpublickeyfile" } ){
                mapGet(Map="#{pageSession.valueMap}" Key="#{requestScope.sattr}" Value="#{requestScope.sval}");
                if ("#{requestScope.sval}"){
                    mapPut(map="#{pageSession.sMap}", key="#{requestScope.sattr}" value="#{requestScope.sval}");
                }
            }
            gf.restRequest( endpoint="#{sessionScope.REST_URL}/nodes/setup-ssh" attrs="#{pageSession.sMap}" method="POST" )
        }
    }
    gf.restRequest( endpoint="#{sessionScope.REST_URL}/#{requestScope.endpoint}" attrs="#{pageSession.attrsMap}" method="POST" )
    gf.redirect(page="#{request.contextPath}/cluster/node/nodes.jsf?alertType=${alertType}&alertSummary=${alertSummary}&alertDetail=${alertDetail}");
 />
</sun:button>

<sun:button id="cancelButton" immediate="#{true}" text="$resource{i18n.button.Cancel}" primary="#{false}" >
    <!command
        gf.redirect(page="#{request.contextPath}/cluster/node/nodes.jsf");
    />
</sun:button>
