/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

<!-- shared/sendTestEmailButton.inc -->

    <!-- To use this general include files for page buttons,  the following page Session attributes should be set:
        setPageSessionAttribute(key="showDefaultButton" value="#{false}" );

        We only show this button if #{pageSession.edit} is set to true.  refer to issue#8660
    -->

    <sun:button id="sendTestEmail" text="$resource{i18njmail.button.SendTestEmail}" primary="#{false}" >
        <!command
            sendTestEmail(host="#{pageSession.valueMap['host']}"
                from="#{pageSession.valueMap['from']}"
                defaultUser="#{pageSession.valueMap['user']}"
                storeProtocol="#{pageSession.valueMap['storeProtocol']}"
                storeProtocolClass="#{pageSession.valueMap['storeProtocolClass']}"
                transportProtocol="#{pageSession.valueMap['transportProtocol']}"
                transportProtocolClass="#{pageSession.valueMap['transportProtocolClass']}"
                debug="#{pageSession.valueMap['debug']}"
                properties="#{pageSession.tableList}");
        />
    </sun:button>
