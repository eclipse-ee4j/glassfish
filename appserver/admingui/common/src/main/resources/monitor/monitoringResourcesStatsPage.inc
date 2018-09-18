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

<!-- monitor/monitoringStatsPage.inc -->
#include "/common/shared/commonHandlers.inc"
<event>
    <!beforeCreate
        gfr.getListOfRunningInstances();
        setPageSessionAttribute(key="displayPropSection" value="#{true}");
        gf.containedIn(list="#{pageSession.runningInstances}" testStr="#{pageSession.instanceName}" contain="#{requestScope.isInstanceRunning}");
        setPageSessionAttribute(key="isMonitoringOff" value="#{false}");
        if("#{requestScope.isInstanceRunning}"){
            setAttribute(key="instanceRunningAlert" value="#{false}");
            gfr.isMonitoringOff();
            if("!#{pageSession.isMonitoringOff}") {
                gf.getChildrenNamesList(endpoint="#{pageSession.monitorURL}/resources", result="#{pageSession.poolsList}");
                gf.getMonitoringPools(endpoint="#{sessionScope.REST_URL}/resources",
                    poolNames = "#{pageSession.poolsList}",
                    jdbcList="#{pageSession.jdbcList}",
                    firstJdbc="#{pageSession.firstJdbc}",
                    connectorList="#{pageSession.connectorList}",
                    firstConnector="#{pageSession.firstConnector}");
                addToMonitorList(oldList="#{pageSession.jdbcList}", newList="#{pageSession.connectorList}"  result="#{pageSession.resourceList}");
                populateResourceMonitorDropDown(ResourceList="#{resourceList}", MonitorList=>$attribute{monitorList} FirstItem=>$attribute{firstVal});
                if(!#{viewVal}) {
                    setAttribute(key="viewVal" value="#{firstVal}");
                }
                if(!#{appVal}) {
                    setAttribute(key="appVal" value="");
                }
            }
            if("#{pageSession.isMonitoringOff}") {
                setPageSessionAttribute(key="displayPropSection" value="#{false}");
            }
        }
        if("!#{requestScope.isInstanceRunning}"){
            setPageSessionAttribute(key="displayPropSection" value="#{false}");
            setAttribute(key="instanceRunningAlert" value="#{true}");
        }
        calculateHelpUrl(pluginId="#{pluginId}", helpKey: "$resource{help_web.monitorResources}", url="#{olhLink}");
    />
</event>    

 <sun:title id="propertyContentPage" title="$resource{i18nc.resourcesMonitor}"
     helpText="$resource{i18nc.monitoring.webContainer.PageHelp}">
     <!facet pageButtonsTop>
         <sun:panelGroup id="topButtons">
             <sun:button id="refreshButton" 
                         text="$resource{i18n.button.Refresh}">
                  <!command
                      setAttribute(key="viewVal" value="#{viewVal}");
                      setAttribute(key="appVal" value="#{appVal}");
                      if ("$pageSession{encodedInstanceName}") {
                          setAttribute(key="instanceName" value="$pageSession{encodedInstanceName}");
                      }
                      if ("$pageSession{encodedClusterName}") {
                          setAttribute(key="clusterName" value="$pageSession{encodedClusterName}");
                      }
                      gf.navigate(page="#{pageSession.selfPage}");
                  />              
             </sun:button>
         </sun:panelGroup>
     </facet> 
#include "/common/shared/nameSection.inc"
    <sun:propertySheet id="infoSheet">
        <sun:propertySheetSection id="infoSheetSection">
            <sun:property id="instNotRunningProp" rendered="#{instanceRunningAlert}"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" >
                <sun:staticText id="instNotRunning"text="$resource{i18nc.monitoring.webContainer.instanceNotRunningMsg}" />
            </sun:property>
            <sun:property id="monDisabledProp" rendered="#{isMonitoringOff}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" >
                <sun:staticText id="monDisabled"  text="$resource{i18nc.monitoring.webContainer.disableMonitoringMsg}" />
            </sun:property>
        </sun:propertySheetSection>
    </sun:propertySheet>
     <sun:propertySheet id="propertySheet">
         <sun:propertySheetSection id="viewPropertySection" rendered="#{displayPropSection}">
             <sun:property id="VsProp"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}">
                 <sun:dropDown id="View" label="$resource{i18nc.monitoring.resource} : " selected="#{viewVal}" items="$attribute{monitorList}" submitForm="#{true}" immediate="#{true}" >
                     <!command
                         setAttribute(key="click" value="$this{component}");
                         setAttribute(key="viewVal" value="#{click.selected}");
                         if ("$pageSession{encodedInstanceName}") {
                            setAttribute(key="instanceName" value="$pageSession{encodedInstanceName}");
                        }
                        if ("$pageSession{encodedClusterName}") {
                            setAttribute(key="clusterName" value="$pageSession{encodedClusterName}");
                        }
                         gf.navigate(page="#{pageSession.selfPage}");
                     />
                     <!afterCreate
                         getClientId(component="$this{component}" clientId=>$page{poolId});
                     />
                 </sun:dropDown>
                 "&nbsp;&nbsp;&nbsp;&nbsp;
                 //To provide the support for application based pool monitoring.
                 <sun:dropDown id="appView" visible="#{hasResApps}" label="$resource{i18nc.monitoring.application} : " selected="#{appVal}" items="$attribute{resApps}" submitForm="#{true}" immediate="#{true}" >
                     <!beforeCreate
                         setAttribute(key="resAppsList" value={});
                         gf.getChildrenNamesList(endpoint="#{sessionScope.REST_URL}/applications/application", result="#{appsList}");
                         gf.getChildrenNamesList(endpoint="#{pageSession.monitorURL}/resources/#{viewVal}", result="#{poolAppsList}");
                         foreach (var="poolApp" list="#{poolAppsList}") {
                            gf.containedIn(list="#{appsList}" testStr="#{poolApp}" contain=>$attribute{hasApp});
                            if(#{hasApp}) {
                                listAdd(list="#{resAppsList}" value="#{poolApp}" result=>$attribute{resAppsList});
                            }
                         }
                         setAttribute(key="resAppsSize" value="#{resAppsList.size()}");
                         setAttribute(key="zero" value="$int{0}");
                         if(#{resAppsSize} > #{zero}) {
                            setAttribute(key="hasResApps" value="$boolean{true}");                                                        
                         }
                         if(#{resAppsSize} = #{zero}) {
                            setAttribute(key="hasResApps" value="$boolean{false}");
                         }
                         addEmptyFirstElement(in="#{resAppsList}" out="#{resAppsList}");
                         gf.convertListToOptionArray(list="#{resAppsList}" optionArray=>$attribute{resApps});
                                                  
                     />
                     <!command
                         setAttribute(key="click" value="$this{component}");
                         setAttribute(key="appVal" value="#{click.selected}");
                         getUIComponent(clientId="$pageSession{poolId}", component=>$attribute{poolComp});
                         setAttribute(key="viewVal" value="#{poolComp.selected}");
                         if ("$pageSession{encodedInstanceName}") {
                            setAttribute(key="instanceName" value="$pageSession{encodedInstanceName}");
                         }
                         if ("$pageSession{encodedClusterName}") {
                             setAttribute(key="clusterName" value="$pageSession{encodedClusterName}");
                         }
                         gf.navigate(page="#{pageSession.selfPage}");
                     />
                 </sun:dropDown>
             </sun:property>                 
         </sun:propertySheetSection>
     </sun:propertySheet>   
                   
     <!-- Table .... -->
     <sun:table id="resourcesTable" itemsText="$resource{i18nc.monitoring.statistics}"   style="padding: 10pt" title="$resource{i18nc.monitoring}"  >
         <!afterCreate
             getClientId(component="$this{component}" clientId=>$page{tableId});
         />
   
         <sun:tableRowGroup id="jdbcStats" rendered="#{hasStatsJdbc}" data={"$attribute{jdbcStatsList}"}  sourceVar="td" headerText="$resource{i18nc.monitoring.JDBC} : #{jdbcHeader}"
             groupToggleButton="$boolean{true}" collapsed="$boolean{false}" aboveColumnHeader="$boolean{true}" emptyDataMsg="$resource{i18nc.monitoring.webContainer.NoStats}">
             <!beforeCreate
                 isPool(poolName="#{viewVal}", endpoint="#{sessionScope.REST_URL}/resources/jdbc-connection-pool", result=>$attribute{isJdbcPool});
                 if (#{isJdbcPool}) {
                     if(#{appVal}) {
                        setAttribute(key="jdbcHeader" value="#{viewVal}/#{appVal}");
                     }
                     if(!#{appVal}) {
                        setAttribute(key="jdbcHeader" value="#{viewVal}");
                     }
                     getStats(endpoint="#{pageSession.monitorURL}/resources/#{viewVal}/#{appVal}", result=>$attribute{jdbcStatsList}  hasStats=>$attribute{hasStatsJdbc});
                 }
                 if (!#{isJdbcPool}) {
                     setAttribute(key="hasStatsJdbc" value="#{false}");
                 }
             />
#include "/common/monitor/monitoringTableRows.inc"
             "<br/>
         </sun:tableRowGroup>
         <sun:tableRowGroup id="connectionPoolStats" rendered="#{hasStatsConnectionPool}" data={"$attribute{connectionPoolStatsList}"}  sourceVar="td" headerText="$resource{i18nc.monitoring.ConnectorConnectionPool} : #{connectorHeader}"
             groupToggleButton="$boolean{true}" collapsed="$boolean{false}" aboveColumnHeader="$boolean{true}" emptyDataMsg="$resource{i18nc.monitoring.webContainer.NoStats}">
             <!beforeCreate
                 isPool(poolName="#{viewVal}", endpoint="#{sessionScope.REST_URL}/resources/connector-connection-pool", result=>$attribute{isConnectorPool});
                 if(#{appVal}) {
                    setAttribute(key="connectorHeader" value="#{viewVal}/#{appVal}");
                 }
                 if(!#{appVal}) {
                    setAttribute(key="connectorHeader" value="#{viewVal}");
                 }
                 if (#{isConnectorPool}) {
                     getStats(endpoint="#{pageSession.monitorURL}/resources/#{viewVal}/#{appVal}", result=>$attribute{connectionPoolStatsList}  hasStats=>$attribute{hasStatsConnectionPool});
                 }
                 if (!#{isConnectorPool}) {
                    setAttribute(key="hasStatsConnectionPool" value="#{false}");
                 }
             />
#include "/common/monitor/monitoringTableRows.inc"
        "<br/>
    </sun:tableRowGroup>
</sun:table>
</sun:title>
