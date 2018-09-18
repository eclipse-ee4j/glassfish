#!/bin/sh
#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

SYS=`uname`
case $SYS in
   Windows*)
      OS=win32;;   #Windows environments
   Linux*)
      OS=linux;;   #Linux environments
   SunOS*)
      OS=solaris;; #Solaris environments
   *)
      OS=unknown;;    #All the rest
esac

if [ $OS = "win32" ]; then
    for x in `ps|grep "startPB"|cut -d' ' -f 2`
    do
        echo "killing startPB process with processid: $x"; 
        kill -9 $x
    done
    for x in `ps|grep "pointbase"|cut -d' ' -f 2`
    do
        echo "killing pointbase process with processid: $x"; 
        kill -9 $x
    done
else
    for x in `ps -ef|grep "java"|cut -d' ' -f 3`
    do
        echo "killing java process with processid: $x"; 
        kill -9 $x
    done
fi
