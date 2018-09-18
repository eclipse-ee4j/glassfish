#
# Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Distribution License v. 1.0, which is available at
# http://www.eclipse.org/org/documents/edl-v10.php.
#
# SPDX-License-Identifier: BSD-3-Clause
#

# These jar files must be present!
export STD_JARS="jmxri.jar;jmxremote.jar;javax77.jar"
export CP=".;amx-client.jar;$STD_JARS"

java -cp $CP samples.amx.SampleMain SampleMain.properties

