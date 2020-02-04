/*
 * Copyright (c) 2018-2020 Oracle and/or its affiliates. All rights reserved.
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

// list of test ids
def jobs = [
  "web_jsp",
  //"web_servlet",
  //"web_web-container",
  //"web_group-1",
  //"sqe_smoke_all",
  //"security_all",
  //"admin-cli-group-1",
  //"admin-cli-group-2",
  //"admin-cli-group-3",
  //"admin-cli-group-4",
  //"admin-cli-group-5",
  "deployment_all",
  //"deployment_cluster_all",
  "ejb_group_1",
  "ejb_group_2",
  "ejb_group_3",
  //"ejb_timer_cluster_all",
  "ejb_web_all",
  //"transaction-ee-1",
  //"transaction-ee-2",
  //"transaction-ee-3",
  //"transaction-ee-4",
  "cdi_all",
  "ql_gf_full_profile_all",
  "ql_gf_nucleus_all",
  "ql_gf_web_profile_all",
  // TODO fix this test suite (fails because of no test descriptor)
  //"ql_gf_embedded_profile_all",
  "nucleus_admin_all",
  //"cts_smoke_group-1",
  //"cts_smoke_group-2",
  //"cts_smoke_group-3",
  //"cts_smoke_group-4",
  //"cts_smoke_group-5",
  //"servlet_tck_servlet-api-servlet",
  //"servlet_tck_servlet-api-servlet-http",
  //"servlet_tck_servlet-compat",
  //"servlet_tck_servlet-pluggability",
  //"servlet_tck_servlet-spec",
  //"findbugs_all",
  //"findbugs_low_priority_all",
  "jdbc_all",
  //"jms_all",
  //"copyright",
  "batch_all",
  //"naming_all",
  "persistence_all",
  //"webservice_all",
  "connector_group_1",
  "connector_group_2",
  "connector_group_3",
  "connector_group_4"
]

// the label is unique and identifies the pod descriptor and its resulting pods
// without this, the agent could be using a pod created from a different descriptor
env.label = "glassfish-ci-pod-${UUID.randomUUID().toString()}"

def parallelStagesMap = jobs.collectEntries {
  ["${it}": generateStage(it)]
}

def generateStage(job) {
    return {
        podTemplate(label: env.label) {
            node(label) {
                stage("${job}") {
                    container('glassfish-ci') {
                      // do the scm checkout
                      retry(10) {
                        sleep 60
                        checkout scm
                      }
                      // run the test
                      unstash 'build-bundles'
                      try {
                          retry(3) {
                              timeout(time: 2, unit: 'HOURS') {
                                sh "./appserver/tests/gftest.sh run_test ${job}"
                              }
                          }
                      } finally {
                        // archive what we can...
                        archiveArtifacts artifacts: "${job}-results.tar.gz"
                        junit testResults: 'results/junitreports/*.xml', allowEmptyResults: false
                      }
                    }
                }
            }
        }
    }
}

pipeline {
  options {
    // keep at most 50 builds
    buildDiscarder(logRotator(numToKeepStr: '50'))
    // preserve the stashes to allow re-running a test stage
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
  agent {
    kubernetes {
      label "${env.label}"
      defaultContainer 'glassfish-ci'
      yaml """
apiVersion: v1
kind: Pod
metadata:
spec:
  volumes:
    - name: "jenkins-home"
      emptyDir: {}
    - name: maven-repo-shared-storage
      persistentVolumeClaim:
       claimName: glassfish-maven-repo-storage
    - name: settings-xml
      secret:
        secretName: m2-secret-dir
        items:
        - key: settings.xml
          path: settings.xml
    - name: settings-security-xml
      secret:
        secretName: m2-secret-dir
        items:
        - key: settings-security.xml
          path: settings-security.xml
    - name: maven-repo-local-storage
      emptyDir: {}
  containers:
  - name: jnlp
    image: jenkins/jnlp-slave:alpine
    imagePullPolicy: IfNotPresent
    volumeMounts:
    env:
      - name: JAVA_TOOL_OPTIONS
        value: -Xmx1G
    resources:
      limits:
        memory: "1Gi"
        cpu: "1"
  - name: glassfish-ci
    image: rohitkujain/ee4jglassfish:tiny
    args:
    - cat
    tty: true
    imagePullPolicy: Always
    volumeMounts:
      - mountPath: "/home/jenkins"
        name: "jenkins-home"
        readOnly: false
      - mountPath: /home/jenkins/.m2/repository
        name: maven-repo-shared-storage
      - name: settings-xml
        mountPath: /home/jenkins/.m2/settings.xml
        subPath: settings.xml
        readOnly: true
      - name: settings-security-xml
        mountPath: /home/jenkins/.m2/settings-security.xml
        subPath: settings-security.xml
        readOnly: true
      - mountPath: "/home/jenkins/.m2/repository/org/glassfish/main"
        name: maven-repo-local-storage
    env:
      - name: "MAVEN_OPTS"
        value: "-Duser.home=/home/jenkins"
      - name: "MVN_EXTRA"
        value: "--batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
    resources:
      limits:
        memory: "7Gi"
        cpu: "3"
"""
    }
  }
  environment {
    S1AS_HOME = "${WORKSPACE}/glassfish5/glassfish"
    APS_HOME = "${WORKSPACE}/appserver/tests/appserv-tests"
    TEST_RUN_LOG = "${WORKSPACE}/tests-run.log"
    GF_INTERNAL_ENV = credentials('gf-internal-env')
  }
  stages {
    stage('build') {
      agent {
        kubernetes {
          label "${env.label}"
        }
      }
      steps {
        container('glassfish-ci') {
          timeout(time: 1, unit: 'HOURS') {
            checkout scm
            // do the build
            sh '''
              echo Maven version
              mvn -v
              
              bash -xe ./gfbuild.sh build_re_dev
            '''
            archiveArtifacts artifacts: 'bundles/*.zip'
            junit testResults: 'test-results/build-unit-tests/results/junitreports/test_results_junit.xml'
            stash includes: 'bundles/*', name: 'build-bundles'
          }
        }
      }
    }
    stage('tests') {
      steps {
        script {
          parallel parallelStagesMap
        }
      }
    }
  }
}
