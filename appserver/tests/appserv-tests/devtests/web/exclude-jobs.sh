#!/bin/sh
#
# Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
#


skip() {
    FILE=$1
    echo Parsing ${FILE}
    cat ${FILE} | while read LINE
    do
        NAME=`echo $LINE | sed -e 's/[# ].*//'`
        if [ -d "${NAME}" ]
        then
            echo excluding \"${NAME}\"
            sed -e "s@^ *<ant dir=\"${NAME}\" target=\"all\"/>@<!--&-->@" build.xml > build.xml.sed
            mv build.xml.sed build.xml
        else
            if [ ! -z "${NAME}" ]
            then
                echo "***** ${NAME} is not a valid test directory *****"
            fi
        fi
    done
}

echo start
if [ "x$1" = "x" ]; then
    SKIP_NAME=${JOB_NAME}
else
    SKIP_NAME=$1
fi

if [ -z "${SKIP_NAME}" -o "$SKIP_NAME" = "webtier-dev-tests-v3-source" ]
then
    SKIP_NAME=webtier-dev-tests-v3
fi

if [ -f "${SKIP_NAME}.skip" ]
then
    skip ${SKIP_NAME}.skip
fi

