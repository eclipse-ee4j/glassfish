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

<f:verbatim>
<div style="height: 435px;background-image: url(#{request.contextPath}/resource/community-theme/images/login-backimage-open.png);
    background-repeat:no-repeat;background-position:left top; width: 720px; margin: auto;">
    <div style="width: 460px; padding-top: 60px; margin-left: auto">
</f:verbatim>
        <sun:image url="#{request.contextPath}/resource/community-theme/images/login-product_name_open.png" alt="$resource{theme.productName}" height="42" width="329" >
        <!beforeCreate
                setResourceBundle(key="theme" bundle="org.glassfish.admingui.community-theme.Strings");
            />
        </sun:image>



<f:verbatim>
&nbsp 
&nbsp 

        <table>
            <tr>
                <td>
</f:verbatim>
                    <sun:staticText text="$resource{i18n.shutdown.RestartHeading}  $resource{i18n.shutdown.RestartInstructions1} "  />
<f:verbatim>
                </td>
            </tr>
            <tr>
                <td>
 &nbsp 

<div style=" padding-left: 20px;">
</f:verbatim>

"        </li><li>

                    <sun:staticText id="text2" text="$resource{i18n.shutdown.RestartStep1}" />
"        </li><li>
            
                    <sun:staticText id="text3" text="$resource{i18n.shutdown.RestartStep2}" />
"        </li><li>
                    <sun:staticText id="text4" text="$resource{i18n.shutdown.RestartStep3}" />
<f:verbatim>
</div>

                </td>
            </tr>
            </li>
            </ul>
            <tr>
                 <td><div class="logLbl"><span class="LblLev2Txt">
<br/><br/>
</f:verbatim>
                    <sun:staticText text="$resource{i18n.shutdown.RestartContinue}" />
<f:verbatim>
                    </span></div></td>
                </tr>
        </table>



    </div>
</div>
</f:verbatim>
