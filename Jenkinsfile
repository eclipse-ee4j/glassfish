/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
  "web_servlet",
  "web_web-container",
  "web_group-1",
  //"sqe_smoke_all",
  "security_all",
  "admin-cli-group-1",
  "admin-cli-group-2",
  "admin-cli-group-3",
  "admin-cli-group-4",
  "admin-cli-group-5",
  "deployment_all",
  "deployment_cluster_all",
  "ejb_group_1",
  "ejb_group_2",
  "ejb_group_3",
  "ejb_timer_cluster_all",
  "ejb_web_all",
  "transaction-ee-1",
  "transaction-ee-2",
  "transaction-ee-3",
  "transaction-ee-4",
  "cdi_all",
  "ql_gf_full_profile_all",
  "ql_gf_nucleus_all",
  "ql_gf_web_profile_all",
  "ql_gf_embedded_profile_all",
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
  "jms_all",
  //"copyright",
  "batch_all",
  "naming_all",
  "persistence_all",
  "webservice_all",
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
                      checkout scm
                      unstash 'build-bundles'
                      sh """
                        # re-create the local repository from archived chunks
                        cat ${WORKSPACE}/bundles/_maven-repo* | tar -xvz -f - --overwrite -C /root/.m2/repository

                        # run the test!
                        ${WORKSPACE}/appserver/tests/gftest.sh run_test ${job}
                      """
                      archiveArtifacts artifacts: "${job}-results.tar.gz"
                      junit testResults: 'results/junitreports/*.xml', allowEmptyResults: true
                    }
                }
            }
        }
    }
}

pipeline {
  options {
    buildDiscarder(logRotator(numToKeepStr: '5'))
    preserveStashes()
  }
  agent {
    kubernetes {
      label "${env.label}"
      defaultContainer 'jnlp'
      yaml """
apiVersion: v1
kind: Pod
metadata:
spec:
  volumes:
    - name: maven-repo-shared-storage
      # required PVC
      # this needs to be setup on the k8s cluster
      persistentVolumeClaim:
       claimName: glassfish-maven-repo-storage
    - name: maven-repo-local-storage
      emptyDir: {}
  containers:
  - name: glassfish-ci
    image: maven:3.5-jdk-8
    args:
    - cat
    tty: true
    imagePullPolicy: Always
    volumeMounts:
      # local repository is shared with all pipelines
      # this is pointing at the PVC
      - mountPath: "/root/.m2/repository"
        name: maven-repo-shared-storage
      # local repository fragment that is scoped to the pod
      # i.e this is not shared with all pipelines
      - mountPath: "/root/.m2/repository/org/glassfish/main"
        name: maven-repo-local-storage
    env:
      - name: M2_HOME
        value: /usr/share/maven
#    resources:
#      limits:
#        memory: "8Gi"
#        cpu: "1.75"
"""
    }
  }
  environment {
    S1AS_HOME = "${WORKSPACE}/glassfish5/glassfish"
    APS_HOME = "${WORKSPACE}/appserver/tests/appserv-tests"
    TEST_RUN_LOG = "${WORKSPACE}/tests-run.log"
    // required credential (secret text)
    // needs to be manually created on Jenkins
    // base64 encoded script used to inject internal environment
    // create an empty one if not needed
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
          sh """
            # do the build!
            ${WORKSPACE}/gfbuild.sh build_re_dev

            # archive the org/glassfish fragment of the local maven repository
            # split it in chunks of 1M to reduce stress on jenkins master
            tar -cz -f - -C /root/.m2/repository org/glassfish | split -b 1m - ${WORKSPACE}/bundles/_maven-repo
          """
          archiveArtifacts artifacts: 'bundles/*.zip'
          junit testResults: 'test-results/build-unit-tests/results/junitreports/test_results_junit.xml'
          stash includes: 'bundles/*', name: 'build-bundles'
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
