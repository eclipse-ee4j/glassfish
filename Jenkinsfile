/*
* Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

def mvnVersion = '3.9.11'
def javaVersion = '17'
def jdkTool = "temurin-jdk${javaVersion}-latest"
def mvnTool = "apache-maven-${mvnVersion}"

def mvnContainerCfg = """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:${mvnVersion}-eclipse-temurin-${javaVersion}
    command:
    - cat
    tty: true
    env:
    - name: "HOME"
      value: "/home/jenkins"
    - name: "MAVEN_OPTS"
      value: "-Duser.home=/home/jenkins -Xmx2g -Xss512k -XX:+UseG1GC -XX:+UseStringDeduplication"
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
    resources:
      limits:
        memory: "8Gi"
        cpu: "5500m"
      requests:
        memory: "8Gi"
        cpu: "5500m"
  volumes:
  - name: "jenkins-home"
    emptyDir:
      sizeLimit: "4Gi"
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
    emptyDir:
      sizeLimit: "2Gi"
"""

def dumpSysInfo() {
   sh """
   id || true
   uname -a || true
   env | sort || true
   df -h || true
   \${JAVA_HOME}/bin/jcmd || true
   mvn -version || true
   ant -version || true
   ps -e -o start,etime,pid,rss,drs,command || true
   lscpu || true
   cat /proc/meminfo || true
   ulimit -a || true
   """
}

def startVmstatLogging(String stageName) {
   sh """
   mkdir -p "${WORKSPACE}/logs"
   vmstat -t -w -a -y 10 > "${WORKSPACE}/logs/vmstat-${stageName}.log" 2>&1 & echo \$! > "${WORKSPACE}/vmstat.pid"
   """
}

def stopVmstatLogging() {
   sh """
   if [ -f "${WORKSPACE}/vmstat.pid" ]; then
      pkill -F "${WORKSPACE}/vmstat.pid" || true
      rm -f "${WORKSPACE}/vmstat.pid"
   fi
   df -h || true
   """
   archiveArtifacts artifacts: "logs/*", allowEmptyArchive: true
}

def generateAntPodTemplate(job) {
   return {
      node {
         stage("${job}") {
            try {
               startVmstatLogging("ant-${job}")
               unstash 'maven-repo'
               unstash 'appserv-tests'
               timeout(time: 4, unit: 'HOURS') {
                  withAnt(installation: 'apache-ant-latest') {
                     dumpSysInfo()
                     sh '''
                     mkdir -p ${WORKSPACE}/appserver/tests
                     tar -xvf ${BUNDLES_DIR}/maven-repo.tar.gz --overwrite -m -p -C /home/jenkins/.m2/repository
                     tar -xvf ${BUNDLES_DIR}/appserv-tests.tar.gz -C ${WORKSPACE}
                     '''
                     sh """
                     ./runtests.sh ${job}
                     """
                  }
               }
            } finally {
               stopVmstatLogging()
               archiveArtifacts artifacts: "${job}-results.tar.gz"
               junit testResults: 'results/junitreports/*.xml', allowEmptyResults: false
            }
         }
      }
   }
}

def generateMvnTestPodTemplate(job, nodeCfg) {
   return {
      podTemplate(
         inheritFrom: 'basic',
         yaml: nodeCfg
      ) {
         node(POD_LABEL) {
            stage("${job}") {
               try {
                  checkout scm
                  container('maven') {
                     script {
                        try {
                           startVmstatLogging("mvn-${job}")
                           dumpSysInfo()
                           unstash 'maven-repo'
                           timeout(time: 4, unit: 'HOURS') {
                              sh '''
                              tar -xzf ${BUNDLES_DIR}/maven-repo.tar.gz --overwrite -m -p -C /home/jenkins/.m2/repository
                              ls -la /home/jenkins/.m2/repository/org/glassfish/main/distributions/glassfish/*
                              '''
                              sh """
                              mvn -B -e clean verify -Pci -pl :${job} -amd
                              """
                           }
                        } finally {
                           stopVmstatLogging()
                        }
                     }
                  }
               } finally {
                  archiveArtifacts artifacts: "**/server.log*", onlyIfSuccessful: false, allowEmptyArchive: true
                  junit testResults: '**/*-reports/*.xml', allowEmptyResults: false
                  recordIssues id: "checkstyle-${job}", name: "CheckStyle - ${job}", enabledForFailure: true, tools: [checkStyle(pattern: '**/checkstyle-result.xml')]
               }
            }
         }
      }
   }
}

def ant_connector_jobs = [
    "connector_group_1",
    "connector_group_2",
    "connector_group_3",
    "connector_group_4"
]
def ant_di_jobs = [
    "cdi_all",
    "ejb_group_1",
    "ejb_group_2",
    "ejb_group_3",
    "ejb_group_embedded"
]
def ant_db_jobs = [
    "jdbc_group1",
    "jdbc_group2",
    "jdbc_group3",
    "jdbc_group4",
    "jdbc_group5",
    "persistence_all"
]
def ant_other_jobs = [
    "ql_gf_full_profile_all",
    "ql_gf_web_profile_all",
    "web_jsp",
    "batch_all",
    "naming_all",
    "deployment_all",
    "security_all",
    "webservice_all"
]
def mvn_jobs = [
    "admin-tests-parent",
    "application-tests",
    "embedded-tests"
]

def parallelStagesMapAntConnectors = ant_connector_jobs.collectEntries {
   ["${it}": generateAntPodTemplate(it)]
}
def parallelStagesMapAntDi = ant_di_jobs.collectEntries {
   ["${it}": generateAntPodTemplate(it)]
}
def parallelStagesMapAntDb = ant_db_jobs.collectEntries {
   ["${it}": generateAntPodTemplate(it)]
}
def parallelStagesMapAnt = ant_other_jobs.collectEntries {
   ["${it}": generateAntPodTemplate(it)]
}
def parallelStagesMapMvn = mvn_jobs.collectEntries {
   ["${it}": generateMvnTestPodTemplate(it, mvnContainerCfg)]
}

pipeline {

   agent {
      kubernetes {
         inheritFrom "basic"
         yaml mvnContainerCfg
      }
   }

   environment {
      BUNDLES_DIR = "${WORKSPACE}/bundles"
      PORT_ADMIN=4848
      PORT_HTTP=8080
      PORT_HTTPS=8181
   }

   options {
      buildDiscarder(logRotator(numToKeepStr: '2'))

      parallelsAlwaysFailFast()

      // to allow re-running a test stage
      preserveStashes()

      // issue related to default 'implicit' checkout, disable it
      skipDefaultCheckout()

      // abort pipeline if previous stage is unstable
      skipStagesAfterUnstable()

      // show timestamps in logs
      timestamps()

      // global timeout, abort after 6 hours
      timeout(time: 8, unit: 'HOURS')
   }

   stages {
      stage('StopOld') {
         steps {
            script {
               milestone ordinal: Integer.parseInt(env.BUILD_NUMBER), label: "Build ${env.BUILD_NUMBER}"
            }
         }
      }
      stage('Build') {
         steps {
            checkout scm
            container('maven') {
               script {
                   try {
                      startVmstatLogging('mvn-build')
                      dumpSysInfo()
                      timeout(time: 1, unit: 'HOURS') {
                         sh '''
                         # Validate the structure in all submodules (especially version ids)
                         mvn -V -B -e -fae clean validate -Ptck,set-version-id
                         '''
                         sh '''
                         mvn -B -e install -Pfastest,ci -T4C
                         '''
                         sh '''
                         mvn -B -e clean
                         mkdir -p ${BUNDLES_DIR}
                         tar -c -C ${WORKSPACE} runtests.sh appserver/tests/common_test.sh appserver/tests/gftest.sh appserver/tests/appserv-tests appserver/tests/quicklook | gzip --fast > ${BUNDLES_DIR}/appserv-tests.tar.gz
                         tar -c -C /home/jenkins/.m2/repository org/glassfish/main | gzip --fast > ${BUNDLES_DIR}/maven-repo.tar.gz
                         '''
                         sh '''
                         # For easy access to built artifacts and using them elsewhere
                         gfVersion="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
                         mvn_copy="mvn -N org.apache.maven.plugins:maven-dependency-plugin:3.9.0:copy -DoutputDirectory=${BUNDLES_DIR}"
                         ${mvn_copy} -Dartifact="org.glassfish.main.distributions:glassfish:${gfVersion}:zip"
                         ${mvn_copy} -Dartifact="org.glassfish.main.distributions:web:${gfVersion}:zip"
                         ${mvn_copy} -Dartifact="org.glassfish.main.extras:glassfish-embedded-all:${gfVersion}:jar"
                         ${mvn_copy} -Dartifact="org.glassfish.main.extras:glassfish-embedded-web:${gfVersion}:jar"
                         ls -la ${BUNDLES_DIR}
                         '''
                      }
                   } finally {
                      stopVmstatLogging()
                   }
               }
            }
            archiveArtifacts artifacts: 'bundles/*.zip', onlyIfSuccessful: true
            archiveArtifacts artifacts: 'bundles/*.jar', onlyIfSuccessful: true
            stash includes: 'bundles/appserv-tests.tar.gz', name: 'appserv-tests'
            stash includes: 'bundles/maven-repo.tar.gz', name: 'maven-repo'
         }
      }

      stage('Test') {
         parallel {
            stage('main-tests') {
               steps {
                  checkout scm
                  container('maven') {
                     script {
                        try {
                           startVmstatLogging('main-tests')
                           dumpSysInfo()
                           timeout(time: 4, unit: 'HOURS') {
                              sh '''
                              mvn -B -e clean verify -Pqa,ci,ci-main-tests
                              '''
                           }
                        } finally {
                           stopVmstatLogging()
                        }
                     }
                  }
               }
               post {
                  always {
                     archiveArtifacts artifacts: "**/server.log*", onlyIfSuccessful: false, allowEmptyArchive: true
                     junit testResults: '**/*-reports/*.xml', allowEmptyResults: false
                     recordIssues name: "CheckStyle - main", enabledForFailure: true, tools: [checkStyle(pattern: '**/checkstyle-result.xml')]
                  }
               }
            }
            stage('itests') {
               steps {
                  script {
                     parallel parallelStagesMapMvn
                  }
               }
            }
            stage('ant-connector') {
               tools {
                  jdk "${jdkTool}"
                  maven "${mvnTool}"
               }
               steps {
                  script {
                     parallel parallelStagesMapAntConnectors
                  }
               }
            }
            stage('ant-db') {
               tools {
                  jdk "${jdkTool}"
                  maven "${mvnTool}"
               }
               steps {
                  script {
                     parallel parallelStagesMapAntDb
                  }
               }
            }
            stage('ant-di') {
               tools {
                  jdk "${jdkTool}"
                  maven "${mvnTool}"
               }
               steps {
                  script {
                     parallel parallelStagesMapAntDi
                  }
               }
            }
            stage('ant-other') {
               tools {
                  jdk "${jdkTool}"
                  maven "${mvnTool}"
               }
               steps {
                  script {
                     parallel parallelStagesMapAnt
                  }
               }
            }
         }
      }
   }
}
