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

import json

class RestResponse:
    def __init__(self, response, content):
        self.status = int(response['status'])

        if content != '':
            responseMap = json.loads(content)
            self.extraProperties = responseMap['extraProperties'] if responseMap.has_key('extraProperties') else {}
            self.entity = self.extraProperties['entity'] if self.extraProperties.has_key('entity') else []
            self.children = self.extraProperties['childResources'] if self.extraProperties.has_key('childResources') else []
            self.message = responseMap["message"]
            self.properties = responseMap['properties'] if responseMap.has_key('properties') else {}
        else:
            self.entity = {}
            self.children = None
            self.message = None if self.status != 404 else "Resource not found"

    def getStatus(self):
        return self.status

    def getMessage(self):
        return self.message

    def getExtraProperies(self):
        return self.extraProperties

    def getChildren(self):
        return self.children

    def getProperties(self):
        return self.properties

    def getEntityValues(self):
        return self.entity

    def isSuccess(self):
        return 200 <= self.status <= 299
