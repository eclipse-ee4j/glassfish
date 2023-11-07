#!/bin/bash

set -x
set -e

version=${1}
if [ "${version}" == "" ]; then
    echo "New version number expected!"
    exit 1;
fi

oldVersion="$(mvn help:evaluate -Dexpression=project.version -N -q -DforceStdout)"
implicitVersionParams="-DgenerateBackupPoms=false -DprocessAllModules=true -DoldVersion=${oldVersion}"

# change version of the aggregator and keep consistency
mvn versions:set ${implicitVersionParams} -DnewVersion=${version} -Pset-version-id;

# these folders are broken now, but we keep updating version ids at least
find "./appserver/tests" -type f -name "pom.xml" -print0 | while IFS= read -r -d '' file; do
    echo "Processing file: ${file}";
    sedexpression="s/${oldVersion}/${version}/g";
    cat "${file}" | sed -e "${sedexpression}" > "${file}.temporary";
    fileMode=$(stat -c %a "${file}");
    mv "${file}.temporary" "${file}";
    chmod $fileMode "${file}";
done

