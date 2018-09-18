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

import httplib2
from restresponse import *

class Connection:
    def __init__(self, host='localhost', port=4848, useSsl=False, userName = None, password = None):
        self.host = host
        self.port = port
        self.useSsl = useSsl
        self.userName = userName
        self.password = password

        self.h = httplib2.Http(".cache")
        self.h.follow_all_redirects = True
        if userName and password:
            self.h.add_credentials(username, password)

    def request(self, uri, method, body=None, headers = {}):
        if (not headers.has_key('Accept')):
            headers['Accept'] = 'application/json'
        resp, content = self.h.request(uri=uri, method=method, body=body, headers=headers)
        return RestResponse(resp,content)

    def get(self, url, body = None, headers = {}):
        return self.request(url, "GET", body, headers)

    def post(self, url, body = None, headers = {}):
        return self.request(url, "POST", body, headers)

    def put(self, url, body = None, headers = {}):
        return self.request(url, "PUT", body, headers)

    def delete(self, url, body = None, headers = {}):
        return self.request(url, "DELETE", body, headers)
