<!--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
    Copyright (c) 2021 Contributors to the Eclipse Foundation

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

<!-- peTree.jsf 

JVM             common  100
Logger Setting  common  150

Web Container    web  200
EJB Container    ejb-lite  250
JMS             jms-plugin  300
Security       common  350
Transaction    jts  400
HTTP Srvice    web  450
Virtual Server web  500
Network Config web  550
ORB            corba  600
Thread Pools   web  650
Admin Service  common 700
Connector Service   750
monitoring     web  800

    // Parent == "tree" priorities:
    200: pluginTreeNodeServer.jsf
    300: pluginTreeNodeApps.jsf
    400: pluginTreeNodeResources.jsf
    500: pluginTreeNodeClusters.jsf
    530: pluginTreeNodeInstances.jsf
    560: pluginTreeNodeNodes.jsf
    700: pluginTreeNodeConfigurations.jsf
    800: update center
-->

// NOTE: Do not include anything outside the sun:form component... it will
// NOTE: not get used on tree refresh.

  <sun:form id="treeForm">
    <!beforeCreate
      gf.listConfigs(configs="#{pageSession.configsList}");
      setAttribute(key="configName" value="#{pageSession.configsList[0]}");
      gf.encodeId(id="${configName}" result="#{requestScope.configNameId}");
    />
    <h:commandButton id="update" style="display: none;">
        <!command
            setResourceBundle(key="i18n" bundle="org.glassfish.admingui.core.Strings");

            // Must use entire treeForm so that all IntegrationPoints are considered
            getUIComponent(clientId="treeForm", component="#{requestScope.oldComp}");
            setAttribute(key="parent" value="#{requestScope.oldComp.parent}");

            // Replace it with something temporary to hold position
            // Needed, or it it will just find the old one vs. create it
            createComponent(type="staticText" component=>$attribute{temp});
            replaceUIComponent(old="$attribute{oldComp}" new="$attribute{temp}");

            // Rengenerate entire tree (must create everything to be complete)
            getLayoutComponent(
                viewId="/common/peTree.inc"
                clientId="treeForm"
                component=>$attribute{desc});
            buildUIComponentTree(layoutElement="$attribute{desc}" parent="#{parent}" result=>$attribute{newComp});
            replaceUIComponent(old="${temp}" new="$attribute{newComp}");
        />
    </h:commandButton>
    <sun:tree id="tree" 
              text="$resource{i18n.tree.commonTasks}"
              url="/common/commonTask.jsf"
              imageURL="/resource/images/common_tasks.gif"
              clientSide="#{true}">
              <!facet content>
                // Doing this avoids the JS which highlights the entire tree.
                <sun:hyperlink id="ct" url="/common/commonTask.jsf" text="$resource{i18n.tree.commonTasks}" />
              </facet>
          <!afterCreate
              getClientId(component="$this{component}" clientId=>$page{treeId});
          />
    </sun:tree>

    <event>
        <!afterCreate
            includeIntegrations(type="org.glassfish.admingui:navNode" root="#{pageSession.treeId}");
            foreach(var="configName" list="#{pageSession.configsList}") {
                if ("!(${configName}=#{pageSession.configsList[0]})") {
                    gf.encodeId(id="${configName}" result="#{requestScope.configNameId}");
                    buildUIComponentTree(layoutElement="$attribute{configTreeNodeLE}" parent="$attribute{configsTreeNode}");
                    includeIntegrations(type="org.glassfish.admingui:navNode" root="$attribute{configsTreeNode}");
                }
            }
        />
    </event>

<!-- We only want to set the width for IE browser only. -->
<f:verbatim>
<script type="text/javascript">
function setTreeWidth(treeId) {
    var version = navigator.appVersion;
    var ix = version.indexOf('MSIE');
    if (ix == -1){
        return;
    }
    var foo = document.getElementById(treeId);
    foo.style.width="40em";
}
</script>
</f:verbatim>
  </sun:form>
