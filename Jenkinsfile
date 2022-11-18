/*
 * Copyright (c) 2018-2021 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

def jobs = [
  "verifyPhase",
  "cdi_all",
  "ql_gf_full_profile_all",
  "ql_gf_web_profile_all",
  "web_jsp",
  "ejb_group_1",
  "ejb_group_2",
  "ejb_group_3",
  "ejb_group_embedded",
  "batch_all",
  "connector_group_1",
  "connector_group_2",
  "connector_group_3",
  "connector_group_4",
  "jdbc_all",
  "persistence_all",
  "naming_all",
  "deployment_all",
  "security_all",
  "webservice_all"
]

def parallelStagesMap = jobs.collectEntries {
  ["${it}": generateStage(it)]
}

def generateStage(job) {
  if (job == 'verifyPhase') {
    return generateMvnPodTemplate(job)
  } else {
    return generateAntPodTemplate(job)
  }
}

def generateMvnPodTemplate(job) {
  return {
    node {
      stage("${job}") {
          checkout scm
          timeout(time: 1, unit: 'HOURS') {
            sh """
              mvn clean install -P staging
            """
            junit testResults: '**/*-reports/*.xml', allowEmptyResults: false
          }
        }
    }
  }
}

def generateAntPodTemplate(job) {
  return {
    node {
      stage("${job}") {
        unstash 'build-bundles'
        sh """
          mkdir -p ${WORKSPACE}/appserver/tests
          tar -xzf ${WORKSPACE}/bundles/appserv_tests.tar.gz -C ${WORKSPACE}/appserver/tests
        """
        try {
          timeout(time: 1, unit: 'HOURS') {
            withAnt(installation: 'apache-ant-latest') {
              sh """
                export CLASSPATH=${WORKSPACE}/glassfish7/javadb
                ${WORKSPACE}/appserver/tests/gftest.sh run_test ${job}
              """
            }
          }
        } finally {
          archiveArtifacts artifacts: "${job}-results.tar.gz"
          junit testResults: 'results/junitreports/*.xml', allowEmptyResults: false
        }
      }
    }
  }
}

pipeline {

  agent any

  environment {
    S1AS_HOME = "${WORKSPACE}/glassfish7/glassfish"
    APS_HOME = "${WORKSPACE}/appserver/tests/appserv-tests"
    TEST_RUN_LOG = "${WORKSPACE}/tests-run.log"
    GF_INTERNAL_ENV = credentials('gf-internal-env')
    PORT_ADMIN=4848
    PORT_HTTP=8080
    PORT_HTTPS=8181
  }

  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))

    // to allow re-running a test stage
    preserveStashes()

    // issue related to default 'implicit' checkout, disable it
    skipDefaultCheckout()

    // abort pipeline if previous stage is unstable
    skipStagesAfterUnstable()

    // show timestamps in logs
    timestamps()

    // global timeout, abort after 6 hours
    timeout(time: 6, unit: 'HOURS')
  }

  stages {

    stage('build') {
      agent any
      tools {
        jdk 'temurin-jdk17-latest'
        maven 'apache-maven-latest'
      }
      steps {
        checkout scm
        sh '''
          echo Maven version
          mvn -v

          echo User
          id

          echo Uname
          uname -a

          # Until we fix ANTLR in cmp-support-sqlstore, broken in parallel builds. Just -Pfast after the fix.
          mvn clean install -Pfastest,staging -T4C
          ./gfbuild.sh archive_bundles
          mvn clean
          tar -c -C ${WORKSPACE}/appserver/tests common_test.sh gftest.sh appserv-tests quicklook | gzip --fast > ${WORKSPACE}/bundles/appserv_tests.tar.gz
          ls -la ${WORKSPACE}/bundles
        '''
        archiveArtifacts artifacts: 'bundles/*.zip'
        stash includes: 'bundles/*', name: 'build-bundles'
      }
    }

    stage('tests') {
      agent any
      tools {
        jdk 'temurin-jdk17-latest'
        maven 'apache-maven-latest'
      }
      steps {
        script {
          parallel parallelStagesMap
        }
      }
    }
  }
}


