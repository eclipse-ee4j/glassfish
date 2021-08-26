#!/bin/bash
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

