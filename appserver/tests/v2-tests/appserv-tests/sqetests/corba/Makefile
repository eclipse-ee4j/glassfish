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

S1AS_HOME="C:\Sun\Appserver7"

JAVA_HOME=$(S1AS_HOME)/jdk

JAVA=$(JAVA_HOME)/bin/java
JAVAC=$(JAVA_HOME)/bin/javac
RMIC=$(JAVA_HOME)/bin/rmic

WORKDIR=classes
CLASSPATH=$(WORKDIR):$(S1AS_HOME)/lib/appserv-rt.jar:$(S1AS_HOME)/lib/j2ee.jar:$(S1AS_HOME)/lib/appserv-ext.jar

DELEGATES=-Djavax.rmi.CORBA.UtilClass=com.sun.corba.ee.impl.oa.poa.ShutdownUtilDelegate

ORB_PROPERTIES=\
	-Dorg.omg.CORBA.ORBClass=com.sun.corba.ee.impl.orb.ORBImpl \
	-Dorg.omg.CORBA.ORBSingletonClass=com.sun.corba.ee.impl.orb.ORBSingleton

INTERCEPTORS=-Dorg.omg.PortableInterceptor.ORBInitializerClass.corba.ORBInitializerImpl

LD_LIBRARY_PATH=$(S1AS_HOME)/lib

HOST=`hostname`
PORT=1050

all	: clean build run

clean	:
	- rm -rf $(WORKDIR)

build 	: compile stubs

compile	:
	mkdir -p $(WORKDIR)
	$(JAVAC) -classpath $(CLASSPATH) -d $(WORKDIR) *.java

stubs	:
	$(RMIC) -poa -iiop -classpath $(CLASSPATH) -d $(WORKDIR) \
	corba.RemoteTestImpl

run	: start-appserver runtest

#replace with command to start appserver
#temp using tnameserv
start-appserver :
	$(JAVA_HOME)/bin/tnameserv -ORBInitialPort $(PORT) &
	sleep 10

runtest :
	$(JAVA) -classpath $(CLASSPATH) $(ORB_PROPERTIES) $(DELEGATES) \
	$(INTERCEPTORS) -Djava.library.path=$(LD_LIBRARY_PATH) \
	-Dorg.omg.CORBA.ORBInitRef=NameService=corbaloc:iiop:1.2@$(HOST):$(PORT)/NameService \
	corba.Server corbaname::$(HOST):$(PORT)#RemoteTest

