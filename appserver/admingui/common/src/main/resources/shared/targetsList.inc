<!--

    Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- /common/shared/targetsList.inc -->
gf.listClusters(clusters="#{pageSession.clusterList}");
gf.listInstances(optionKeys={"standaloneonly"} optionValues={"true"}, instances="#{pageSession.targetList}" );
 gf.listCombine(list="#{pageSession.targetList}",  list2="#{pageSession.clusterList}");
 listAdd(list="#{pageSession.targetList}", value="server" index="0");

 setAttribute(key="one" value="$int{1}");
 setAttribute(key="targetSize" value="#{pageSession.targetList.size()}");

 if (#{requestScope.targetSize}=#{requestScope.one}){
     setPageSessionAttribute(key="showTargetSection", value="#{false}");
 }
 if (#{requestScope.targetSize}>#{requestScope.one}){
     setPageSessionAttribute(key="showTargetSection", value="#{true}");
 }
