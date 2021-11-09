/*
 * Copyright (c) 2018-2021 Oracle and/or its affiliates. All rights reserved.
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


// the label is unique and identifies the pod descriptor and its resulting pods
// without this, the agent could be using a pod created from a different descriptor
env.label = "glassfish-ci-pod-${UUID.randomUUID().toString()}"

// list of test ids
def jobs = [
  "cdi_all",
  "ql_gf_full_profile_all",
  "ql_gf_web_profile_all",
  "web_jsp"
]


def jobs_all = [
  "cdi_all",
  "ql_gf_full_profile_all",
  "ql_gf_web_profile_all",
  "web_jsp",

  "deployment_all",
  "ejb_group_1",
  "ejb_group_2",
  "ejb_group_3",
  "ejb_web_all",
  "ql_gf_nucleus_all",
  "nucleus_admin_all",
  "jdbc_all",
  "batch_all",
  "persistence_all",
  "connector_group_1",
  "connector_group_2",
  "connector_group_3",
  "connector_group_4"
]

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
                                sh """
                                  export CLASSPATH=$WORKSPACE/glassfish6/javadb
                                  ./appserver/tests/gftest.sh run_test ${job}
                                """
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
    buildDiscarder(logRotator(numToKeepStr: '10'))

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
    env:
      - name: JAVA_TOOL_OPTIONS
        value: -Xmx1G
    resources:
      limits:
        memory: "1Gi"
        cpu: "1"
  - name: glassfish-ci
    # Docker image defined in this project in [glassfish]/etc/docker/Dockerfile
    image: ee4jglassfish/ci:tini-jdk-11.0.10
    args:
    - cat
    tty: true
    imagePullPolicy: Always
    volumeMounts:
      - name: "jenkins-home"
        mountPath: "/home/jenkins"
        readOnly: false
      - name: maven-repo-shared-storage
        mountPath: /home/jenkins/.m2/repository
      - name: settings-xml
        mountPath: /home/jenkins/.m2/settings.xml
        subPath: settings.xml
        readOnly: true
      - name: settings-security-xml
        mountPath: /home/jenkins/.m2/settings-security.xml
        subPath: settings-security.xml
        readOnly: true
      - name: maven-repo-local-storage
        mountPath: "/home/jenkins/.m2/repository/org/glassfish/main"
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
    S1AS_HOME = "${WORKSPACE}/glassfish6/glassfish"
    APS_HOME = "${WORKSPACE}/appserver/tests/appserv-tests"
    TEST_RUN_LOG = "${WORKSPACE}/tests-run.log"
    GF_INTERNAL_ENV = credentials('gf-internal-env')
    PORT_ADMIN=4848
    PORT_HTTP=8080
    PORT_HTTPS=8181
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

            // do the scm checkout
            checkout scm

            // do the build
            sh '''
              echo Maven version
              mvn -v

              echo User
              id

              echo Uname
              uname -a

              bash -xe ./gfbuild.sh build_re_dev
            '''
            archiveArtifacts artifacts: 'bundles/*.zip'
            // junit testResults: 'test-results/build-unit-tests/results/junitreports/test_results_junit.xml'
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


