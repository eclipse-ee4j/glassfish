#!/bin/bash
#
# Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

set -e
set -v

path="${1:-./}";

find "${path}" -type f -regex ".*\/target\/[^\/]*\.jar" ! -path '*/appserver/tests/tck/*' ! -regex ".*/\(glassfish\-embedded\-[a-z]+\)\.jar" -print0 | while IFS= read -r -d '' file; do
	echo "Processing file: ${file}";
	# ignorance: bnd tries to resolve dependencies, but:
	# - not all modules are osgi modules
	# - some modules have dependency on system classpath or code generated at runtime
	# - some modules are really broken, but we are not sure because we don't have any tests for them: TODO/FIXME
	bnd --ignore 'Unresolved references' print --verify "${file}"
done

