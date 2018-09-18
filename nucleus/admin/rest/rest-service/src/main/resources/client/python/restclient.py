#
# Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

from domain import *
from connection import *

class RestClient:
    def __init__(self, host='localhost', port=4848, useSsl=False, userName = None, password = None):
        self.host = host
        self.port = port
        self.useSsl = useSsl

        self.connection = Connection(host, port, useSsl, userName, password)

    def getRestUrl(self):
        return ("https" if self.useSsl else "http") + "://" + self.host + ":" + str(self.port) + "/management"

    def getDomain(self):
        return Domain(self.connection, self)
