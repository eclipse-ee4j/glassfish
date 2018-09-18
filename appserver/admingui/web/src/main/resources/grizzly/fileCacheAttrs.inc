<!--

    Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.

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

<!-- grizzly/httpAttr.inc -->


<sun:propertySheetSection id="fileTextField" label="#{pageSession.fileCacheSectionTitle}" >

        <sun:property id="protocolName"  rendered="#{edit}" labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.grizzly.protocol.protocolName}"  >
            <sun:staticText id="protocolName" text="#{pageSession.protocolName}" />
        </sun:property>

        <sun:property id="status"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.common.status}" >
            <sun:checkbox label="$resource{i18n_web.common.enabled}" selected="#{pageSession.fileMap['enabled']}" selectedValue="true" />
        </sun:property>

        <sun:property id="maxAge"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.fileCache.maxAge}" helpText="$resource{i18n_web.fileCache.maxAgeHelp}" >
            <sun:textField id="maxAge" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.fileCache.maxAge']}" text="#{pageSession.fileMap['maxAgeSeconds']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Seconds}"/>
        </sun:property>

        <sun:property id="maxCacheSizeBytes"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.fileCache.MaxCacheSizeBytes}" helpText="$resource{i18n_web.fileCache.MaxCacheSizeBytesHelp}" >
            <sun:textField id="maxCacheSizeBytes" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.fileCache.MaxCacheSizeBytes']}" text="#{pageSession.fileMap['maxCacheSizeBytes']}" />
            <sun:staticText id="sec" style="padding: 8pt" text="$resource{i18n.common.Bytes}"/>
        </sun:property>

        <sun:property id="maxFile"  labelAlign="left" noWrap="#{true}" overlapLabel="#{false}" label="$resource{i18n_web.fileCache.MaxFileCount}" helpText="$resource{i18n_web.fileCache.MaxFileCountHelp}" >
            <sun:textField id="maxFile" columns="$int{20}" maxLength="#{sessionScope.fieldLengths['maxLength.fileCache.MaxFileCount']}" text="#{pageSession.fileMap['maxFilesCount']}" />
        </sun:property>

</sun:propertySheetSection>
