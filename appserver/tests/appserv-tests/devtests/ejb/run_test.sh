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

test_run_ejb(){

	rm -rf ${S1AS_HOME}/domains/domain1
	cd ${APS_HOME}
	echo "AS_ADMIN_PASSWORD=" > temppwd
	cat ${APS_HOME}/temppwd

	ADMIN_PORT=45707
	JMS_PORT=45708
	JMX_PORT=45709
	ORB_PORT=45710
	SSL_PORT=45711
	INSTANCE_PORT=45712
	ALTERNATE_PORT=45713
	ORB_SSL_PORT=45714
	ORB_SSL_MUTUALAUTH_PORT=45715
	DB_PORT=45716
	DB_PORT_2=45717

	${S1AS_HOME}/bin/asadmin \
		--user anonymous \
		--passwordfile ${APS_HOME}/temppwd \
		create-domain \
			--adminport ${ADMIN_PORT} \
			--domainproperties jms.port=${JMS_PORT}:domain.jmxPort=${JMX_PORT}:orb.listener.port=${ORB_PORT}:http.ssl.port=${SSL_PORT}:orb.ssl.port=${ORB_SSL_PORT}:orb.mutualauth.port=${ORB_SSL_MUTUALAUTH_PORT} \
			--instanceport ${INSTANCE_PORT} \
			domain1

	# Create
	echo "admin.domain=domain1
	admin.domain.dir=\${env.S1AS_HOME}/domains
	admin.port=${ADMIN_PORT}
	admin.user=anonymous
	admin.host=localhost
	http.port=${INSTANCE_PORT}
	https.port=${SSL_PORT}
	http.host=localhost
	http.address=127.0.0.1
	http.alternate.port=${ALTERNATE_PORT}
	orb.port=${ORB_PORT}
	admin.password=
	ssl.password=changeit
	master.password=changeit
	admin.password.file=\${env.APS_HOME}/config/adminpassword.txt
	appserver.instance.name=server
	config.dottedname.prefix=server
	resources.dottedname.prefix=domain.resources
	results.mailhost=localhost
	results.mailer=QLTestsForPEInstallOrDASInEEInstall@sun.com
	results.mailee=yourname@sun.com
	autodeploy.dir=\${env.S1AS_HOME}/domains/\${admin.domain}/autodeploy
	precompilejsp=true
	jvm.maxpermsize=192m
	appserver.instance.dir=\${admin.domain.dir}/\${admin.domain}" > config.properties

	(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true

	cd ${S1AS_HOME}/domains/domain1/config/
	sed "s/1527/${DB_PORT}/g" domain.xml > domain.xml.replaced
	mv domain.xml.replaced domain.xml
	grep PortNumber domain.xml

	cd ${APS_HOME}/config
	(rm derby.properties.replaced  > /dev/null 2>&1) || true
	sed "s/1527/${DB_PORT}/g" derby.properties > derby.properties.replaced
	rm derby.properties
	sed "s/1528/${DB_PORT_2}/g" derby.properties.replaced > derby.properties
	cat derby.properties
	rm -rf ${APS_HOME}/test_results*
	cd ${APS_HOME}/devtests/ejb
	rm count.txt || true

	ant ${TARGET} report-result -Ddb.port=${DB_PORT} -Ddb.port.2=${DB_PORT_2} |tee $TEST_RUN_LOG
}

test_run_ejb_web(){

	rm -rf $S1AS_HOME/domains/domain1

	export ADMIN_PORT=45707 \
				 JMS_PORT=45708 \
				 JMX_PORT=45709 \
				 ORB_PORT=45710 \
				 SSL_PORT=45711 \
				 INSTANCE_PORT=45712 \
				 INSTANCE_HTTPS_PORT=45718 \
				 INSTANCE_PORT_2=45719 \
				 INSTANCE_PORT_3=45720 \
				 ALTERNATE_PORT=45713 \
				 ORB_SSL_PORT=45714 \
				 ORB_SSL_MUTUALAUTH_PORT=45715 \
				 DB_PORT=45716 \
				 DB_PORT_2=45717
	env

	cd ${APS_HOME}

	echo "AS_ADMIN_PASSWORD=" > temppwd
	cat ${APS_HOME}/temppwd
	${S1AS_HOME}/bin/asadmin \
		--user anonymous \
		--passwordfile ${APS_HOME}/temppwd \
		create-domain \
			--adminport ${ADMIN_PORT} \
			--domainproperties jms.port=${JMS_PORT}:domain.jmxPort=${JMX_PORT}:orb.listener.port=${ORB_PORT}:http.ssl.port=${SSL_PORT}:orb.ssl.port=${ORB_SSL_PORT}:orb.mutualauth.port=${ORB_SSL_MUTUALAUTH_PORT} \
			--instanceport ${INSTANCE_PORT} \
			domain1

	# Create
	echo "admin.domain=domain1
	admin.domain.dir=\${env.S1AS_HOME}/domains
	admin.port=${ADMIN_PORT}
	admin.user=anonymous
	admin.host=localhost
	http.port=${INSTANCE_PORT}
	https.port=${SSL_PORT}
	http.host=localhost
	http.address=127.0.0.1
	http.alternate.port=${ALTERNATE_PORT}
	orb.port=${ORB_PORT}
	admin.password=
	ssl.password=changeit
	master.password=changeit
	admin.password.file=\${env.APS_HOME}/config/adminpassword.txt
	appserver.instance.name=server
	config.dottedname.prefix=server
	resources.dottedname.prefix=domain.resources
	results.mailhost=localhost
	results.mailer=QLTestsForPEInstallOrDASInEEInstall@sun.com
	results.mailee=yourname@sun.com
	autodeploy.dir=\${env.S1AS_HOME}/domains/\${admin.domain}/autodeploy
	precompilejsp=true
	jvm.maxpermsize=192m
	appserver.instance.dir=\${admin.domain.dir}/\${admin.domain}" > config.properties

	(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true

	cd ${S1AS_HOME}/domains/domain1/config/
	sed "s/1527/${DB_PORT}/g" domain.xml > domain.xml.replaced
	mv domain.xml.replaced domain.xml
	grep PortNumber domain.xml

	cd ${APS_HOME}/config
	(rm derby.properties.replaced  > /dev/null 2>&1) || true
	sed "s/1527/${DB_PORT}/g" derby.properties > derby.properties.replaced
	rm derby.properties
	sed "s/1528/${DB_PORT_2}/g" derby.properties.replaced > derby.properties
	cat derby.properties
	cd ${APS_HOME}/devtests/ejb
	rm count.txt || true
	ant ${TARGET} report-result -Ddb.port=${DB_PORT} -Ddb.port.2=${DB_PORT_2} |tee $TEST_RUN_LOG
	cat ${S1AS_HOME}/databases/derby.log
	egrep 'DID NOT RUN= *0' count.txt || true
}


test_run_ejb_timer_cluster(){

	rm -rf ${S1AS_HOME}/domains/domain1

	export ADMIN_PORT=45707 \
				 JMS_PORT=45708 \
				 JMX_PORT=45709 \
				 ORB_PORT=45710 \
				 SSL_PORT=45711 \
				 INSTANCE_PORT=45712 \
				 INSTANCE_HTTP_PORT=45721 \
				 INSTANCE_HTTPS_PORT=45718 \
				 INSTANCE_PORT_2=45719 \
				 INSTANCE_PORT_3=45720 \
				 ALTERNATE_PORT=45713 \
				 ORB_SSL_PORT=45714 \
				 ORB_SSL_MUTUALAUTH_PORT=45715 \
				 DB_PORT=45716 \
				 DB_PORT_2=45717
	env

	cd ${APS_HOME}

	echo "AS_ADMIN_PASSWORD=" > temppwd
	cat ${APS_HOME}/temppwd
	${S1AS_HOME}/bin/asadmin \
		--user anonymous \
		--passwordfile ${APS_HOME}/temppwd \
		create-domain \
			--adminport ${ADMIN_PORT} \
			--domainproperties jms.port=${JMS_PORT}:domain.jmxPort=${JMX_PORT}:orb.listener.port=${ORB_PORT}:http.ssl.port=${SSL_PORT}:orb.ssl.port=${ORB_SSL_PORT}:orb.mutualauth.port=${ORB_SSL_MUTUALAUTH_PORT} \
			--instanceport ${INSTANCE_PORT} \
			domain1

	# Create
	echo "admin.domain=domain1
	admin.domain.dir=\${env.S1AS_HOME}/domains
	admin.port=${ADMIN_PORT}
	admin.user=anonymous
	admin.host=localhost
	http.port=${INSTANCE_PORT}
	https.port=${SSL_PORT}
	instance.http.port=${INSTANCE_HTTP_PORT}
	instance.https.port=${INSTANCE_HTTPS_PORT}
	http.host=localhost
	http.address=127.0.0.1
	http.alternate.port=${ALTERNATE_PORT}
	orb.port=${ORB_PORT}
	admin.password=
	ssl.password=changeit
	master.password=changeit
	admin.password.file=\${env.APS_HOME}/config/adminpassword.txt
	appserver.instance.name=server
	config.dottedname.prefix=server
	resources.dottedname.prefix=domain.resources
	results.mailhost=localhost
	results.mailer=QLTestsForPEInstallOrDASInEEInstall@sun.com
	results.mailee=yourname@sun.com
	autodeploy.dir=\${env.S1AS_HOME}/domains/\${admin.domain}/autodeploy
	precompilejsp=true
	jvm.maxpermsize=192m
	appserver.instance.dir=\${admin.domain.dir}/\${admin.domain}" > config.properties

	(jps |grep Main |cut -f1 -d" " | xargs kill -9  > /dev/null 2>&1) || true

	cd ${S1AS_HOME}/domains/domain1/config/
	sed "s/1527/${DB_PORT}/g" domain.xml > domain.xml.replaced
	mv domain.xml.replaced domain.xml
	grep PortNumber domain.xml

	cd ${APS_HOME}/config
	(rm derby.properties.replaced  > /dev/null 2>&1) || true
	sed "s/1527/${DB_PORT}/g" derby.properties > derby.properties.replaced
	rm derby.properties
	sed "s/1528/${DB_PORT_2}/g" derby.properties.replaced > derby.properties
	cat derby.properties

	pushd ${APS_HOME}/devtests/ejb/ee/timer

	ant ${TARGET} |tee ${TEST_RUN_LOG}
	antStatus=${?}
	ant dev-report
}

get_test_target(){
	case $1 in
		ejb_all|ejb_timer_cluster_all )
			TARGET=all ;;
		ejb_web_all)
			TARGET=lite ;;
    * )
      TARGET=$1 ;;
	esac
	export TARGET
}


run_test_id(){
	if [[ ${1} = "ejb_web_all" ]]; then
		unzip_test_resources ${WORKSPACE}/bundles/web.zip
	else
		unzip_test_resources ${WORKSPACE}/bundles/glassfish.zip
	fi
	dname=`pwd`
	cd `dirname ${0}`
	test_init
	get_test_target ${1}
	if [[ ${1} = "ejb_all" || ${1} = "ejb_group"* ]]; then
		test_run_ejb
	elif [[ ${1} = "ejb_timer_cluster_all" ]]; then
		test_run_ejb_timer_cluster
	elif [[ ${1} = "ejb_web_all" ]]; then
		test_run_ejb_web
	else
		echo "Invalid Test ID"
		exit 1
	fi
	check_successful_run
  generate_junit_report ${1}
  change_junit_report_class_names
}

list_test_ids(){
	echo ejb_all ejb_timer_cluster_all ejb_web_all ejb_group_1 ejb_group_2 ejb_group_3
}

OPT=${1}
TEST_ID=${2}
source `dirname ${0}`/../../../common_test.sh

case ${OPT} in
	list_test_ids )
		list_test_ids;;
	run_test_id )
		trap "copy_test_artifacts ${TEST_ID}" EXIT
		run_test_id ${TEST_ID} ;;
esac
