REM
REM  Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
REM  Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
REM
REM  This program and the accompanying materials are made available under the
REM  terms of the Eclipse Public License v. 2.0, which is available at
REM  http://www.eclipse.org/legal/epl-2.0.
REM
REM  This Source Code may also be made available under the following Secondary
REM  Licenses when the conditions for such availability set forth in the
REM  Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
REM  version 2 with the GNU Classpath Exception, which is available at
REM  https://www.gnu.org/software/classpath/license.html.
REM
REM  SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
REM


REM
REM                       * * *    N O T E     * * *
REM
REM  Although the lines in this file are formatted as environment
REM  variable assignments, this file is NOT typically invoked as a script
REM  from another script to define these variables.  Rather, this file is read
REM  and processed by a server as it starts up.  That scanning code resolves
REM  the relative paths against the GlassFish installation directory.
REM
REM  Yet, this file is also where users of earlier versions have sometimes added
REM  a definition of AS_JAVA to control which version of Java GlassFish
REM  should use.  As a result, in order to run a user-specified version of Java,
REM  the asadmin and appclient scripts do indeed invoke this file as a
REM  script - but ONLY to define AS_JAVA.  Any calling script should not
REM  rely on the other settings because the relative paths will be resolved
REM  against the current directory when the calling script is run, not the
REM  installation directory of GlassFish, and such resolution will not work
REM  correctly unless the script happens to be run from the GlassFish installation
REM  directory.
REM
REM  This file uses UTF-8 character encoding.

endlocal
set "AS_DERBY_INSTALL=%AS_INSTALL%\..\javadb"
set "AS_IMQ_BIN=%AS_INSTALL%\..\mq\bin"
set "AS_IMQ_LIB=%AS_INSTALL%\..\mq\lib"

set "AS_CONFIG=%AS_INSTALL%\config"
set "AS_DEF_DOMAINS_PATH=%AS_INSTALL%\domains"
set "AS_DEF_NODES_PATH=%AS_INSTALL%\nodes"

set "ASADMIN_MODULEPATH=%AS_INSTALL%\lib\bootstrap"
set "ASADMIN_JVM_OPTIONS=-Djava.util.logging.manager=org.glassfish.main.jul.GlassFishLogManager"
set "ASADMIN_CLASSPATH=%AS_INSTALL%\admin-cli.jar;%AS_INSTALL%\lib\asadmin\*;%AS_INSTALL%\modules\admin-util.jar;%AS_INSTALL%\modules\backup.jar;%AS_INSTALL%\modules\cluster-common.jar;%AS_INSTALL%\modules\cluster-ssh.jar;%AS_INSTALL%\modules\config-api.jar;%AS_INSTALL%\modules\config-types.jar;%AS_INSTALL%\modules\common-util.jar;%AS_INSTALL%\modules\glassfish-api.jar;%AS_INSTALL%\modules\hk2.jar;%AS_INSTALL%\modules\hk2-config-generator.jar;%AS_INSTALL%\modules\internal-api.jar;%AS_INSTALL%\modules\jackson-core.jar;%AS_INSTALL%\modules\jakarta.activation-api.jar;%AS_INSTALL%\modules\jakarta.validation-api.jar;%AS_INSTALL%\modules\jakarta.xml.bind-api.jar;%AS_INSTALL%\modules\jaxb-osgi.jar;%AS_INSTALL%\modules\jettison.jar;%AS_INSTALL%\modules\jsch.jar;%AS_INSTALL%\modules\launcher.jar;%AS_INSTALL%\modules\mimepull.jar"
