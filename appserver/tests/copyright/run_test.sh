#!/bin/bash -ex
#
# Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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


copyright_run(){
  M2_HOME=/net/gf-hudson/scratch/gf-hudson/export2/hudson/tools/apache-maven-3.0.3
  MAVEN_OPTS="-Xmx512m -Xms256m -XX:MaxPermSize=512m"; export MAVEN_OPTS
  MAVEN_REPO=$WORKSPACE/repository
  MAVEN_SETTINGS=$M2_HOME/settings-nexus.xml
  PATH=$M2_HOME/bin:$JAVA_HOME/bin:$PATH; export PATH
  mvn -version
  echo $WORKSPACE
	rm -f $WORKSPACE/main/copyright-files.txt || true
	rm -f $WORKSPACE/copyright-files-temp*.txt || true
	rm -rf $WORKSPACE/main/tmp-users || true
	cd $WORKSPACE/main

	# TODO move the copyright module in main and main's default reactor in a profile, in order to not trigger the default reactor.
	mvn -e -s $MAVEN_SETTINGS -Dmaven.repo.local=$MAVEN_REPO --quiet -Dcopyright.normalize=true org.glassfish.copyright:glassfish-copyright-maven-plugin:copyright > $WORKSPACE/copyright-files-temp-open.txt
	cat $WORKSPACE/copyright-files-temp-open.txt
	cat $WORKSPACE/copyright-files-temp-open.txt | sed s@$PWD/@@g > copyright-files.txt
}

generate_copyright_results(){
  rm -rf $WORKSPACE/results || true
  mkdir -p $WORKSPACE/results/copyright_results

	num=`wc -l copyright-files.txt | awk '{print $1}'`	
	if [ $num -gt 0 ];then	
	  echo "UNSTABLE" > $WORKSPACE/results/copyright_results/copyrightcheck.log
	else
	  echo "SUCCESS" > $WORKSPACE/results/copyright_results/copyrightcheck.log
	fi
  cp copyright-files.txt $WORKSPACE/results/copyright_results/copyright-files.txt
}

run_test_id(){
  source `dirname $0`/../common_test.sh
  kill_process
  rm main.zip rm version-info.txt || true
  download_test_resources main.zip version-info.txt
  rm -rf main || true
  rm -rf .git || true
  unzip_test_resources "$WORKSPACE/bundles/main.zip -d main/"
  copyright_run
  generate_copyright_results
}

post_test_run(){
    if [[ $? -ne 0 ]]; then
      generate_copyright_results
    fi
    upload_test_results
    delete_bundle
}

list_test_ids(){
	echo copyright
}

OPT=$1
TEST_ID=$2

case $OPT in
	list_test_ids )
		list_test_ids;;
	run_test_id )
    trap post_test_run EXIT
		run_test_id $TEST_ID ;;
esac
