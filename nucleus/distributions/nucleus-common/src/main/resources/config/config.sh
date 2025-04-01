#!/bin/bash
#
# Copyright (c) 2025 Contributors to the Eclipse Foundation
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v. 2.0, which is available at
# http://www.eclipse.org/legal/epl-2.0.
#
# This Source Code may also be made available under the following Secondary
# Licenses when the conditions for such availability set forth in the
# Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
# version 2 with the GNU Classpath Exception, which is available at
# https://www.gnu.org/software/classpath/license.html.
#
# SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0

# Based on the current path of this script resolves absolute path
# of the server installation (the glassfish directory).
# Then sets it as AS_INSTALL environment variable.
resolveAsInstall() {
    AS_INSTALL="$(realpath -s "$1/..")"
    case "`uname`" in
      CYGWIN*) AS_INSTALL=`cygpath --windows "$AS_INSTALL"`
               cygwinProp=-Dorg.glassfish.isCygwin=true
    esac
    export AS_INSTALL
}

# Loads the asenv.conf file.
loadAsenv() {
    . "${AS_INSTALL}/config/asenv.conf"
}

# Looks for Java at AS_JAVA, JAVA_HOME or in the path.
# If successful and the executable was found, it is exported
# as JAVA environment variable.
chooseJava() {
    if [ "${AS_JAVA}" != "" ]; then
        javaSearchType=AS_JAVA
        javaSearchTarget="$AS_JAVA"
        JAVA="${AS_JAVA}/bin/java"
    elif [ "${JAVA_HOME}" != "" ]; then
        javaSearchType=JAVA_HOME
        javaSearchTarget="$JAVA_HOME"
        JAVA="${JAVA_HOME}/bin/java"
    else
        JAVA=`which java`
        javaSearchType=PATH
        javaSearchTarget=$PATH
    fi

    if [ ! -x "${JAVA}" ]; then
        echo
        echo "The java command \"${JAVA}\" is not executable."
        echo "It was configured as ${javaSearchType}=\"${javaSearchTarget}\""
        exit 1
    fi
    export JAVA
}

resolveAsInstall "$1"
loadAsenv
chooseJava
