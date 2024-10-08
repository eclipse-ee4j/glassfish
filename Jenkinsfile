/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2018, 2021 Oracle and/or its affiliates. All rights reserved.
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

// Docker image defined in this project in [glassfish]/etc/docker/Dockerfile
env.gfImage = "ee4jglassfish/ci:tini-jdk-11.0.10"

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
    podTemplate(
      inheritFrom: "${env.label}",
      containers: [
        containerTemplate(
          name: "glassfish-build",
          image: "${env.gfImage}",
          resourceRequestMemory: "7Gi",
          resourceRequestCpu: "2650m"
        )
      ]
    ) {
      node(label) {
        stage("${job}") {
          container('glassfish-build') {
            retry(5) {
              sleep 1
              checkout scm
            }
            timeout(time: 1, unit: 'HOURS') {
              sh """
                mvn clean install
              """
              junit testResults: '**/*-reports/*.xml', allowEmptyResults: false
            }
          }
        }
      }
    }
  }
}

def generateAntPodTemplate(job) {
  return {
    podTemplate(
      inheritFrom: "${env.label}",
      containers: [
        containerTemplate(
          name: "glassfish-build",
          image: "${env.gfImage}",
          resourceRequestMemory: "4Gi",
          resourceRequestCpu: "2650m"
        )
      ]
    ) {
      node(label) {
        stage("${job}") {
          container('glassfish-build') {
            unstash 'build-bundles'
            sh """
              mkdir -p ${WORKSPACE}/appserver/tests
              tar -xzf ${WORKSPACE}/bundles/appserv_tests.tar.gz -C ${WORKSPACE}/appserver/tests
            """
            try {
              timeout(time: 1, unit: 'HOURS') {
                sh """
                  export CLASSPATH=${WORKSPACE}/glassfish6/javadb
                  ${WORKSPACE}/appserver/tests/gftest.sh run_test ${job}
                """
              }
            } finally {
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

  agent {
    kubernetes {
      label "${env.label}"
      yaml """
apiVersion: v1
kind: Pod
metadata:
spec:
  containers:
  - name: jnlp
    image: jenkins/inbound-agent:4.11-1-alpine-jdk11
    imagePullPolicy: IfNotPresent
    env:
      - name: JAVA_TOOL_OPTIONS
        value: "-Xmx768m -Xss768k"
    resources:
      # fixes random failure: minimum cpu usage per Pod is 200m, but request is 100m.
      # affects performance on large repositories
      limits:
        memory: "1200Mi"
        cpu: "300m"
      requests:
        memory: "1200Mi"
        cpu: "300m"
  - name: glassfish-build
    image: ${env.gfImage}
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
        value: "-Duser.home=/home/jenkins -Xmx2500m -Xss768k -XX:+UseStringDeduplication"
      - name: "MVN_EXTRA"
        value: "--batch-mode -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn"
      - name: JAVA_TOOL_OPTIONS
        value: "-Xmx2g -Xss768k -XX:+UseStringDeduplication"
    resources:
      limits:
        memory: "12Gi"
        cpu: "8000m"
      requests:
        memory: "7Gi"
        cpu: "4000m"
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

  options {
    buildDiscarder(logRotator(numToKeepStr: '2'))

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
      agent {
        kubernetes {
          label "${env.label}"
        }
      }
      steps {
        container('glassfish-build') {
          timeout(time: 1, unit: 'HOURS') {
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


