/*
* Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
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

// See:
// https://www.jenkins.io/doc/book/pipeline/syntax/
// https://www.jenkins.io/doc/pipeline/steps/workflow-basic-steps
// https://plugins.jenkins.io/kubernetes/
// https://github.com/jenkinsci/kubernetes-plugin/blob/master/README.md
// https://kubernetes.io/docs/concepts/workloads/pods/#working-with-pods
// https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/
// https://github.com/jenkinsci/docker-agents/releases
// https://hub.docker.com/r/eclipsecbi/jiro-agent-basic-ubuntu
// https://hub.docker.com/r/jenkins/inbound-agent/

// These limits are collected from logs:
// maximum cpu usage per Pod is 8300m
// maximum cpu usage per Container is 8
// maximum cpu usage is 44800m

def antVersion = '1.10.18'
def antHome = "/home/jenkins/.m2/repository/.ci-tools/apache-ant-${antVersion}"
def mvnVersion = '3.9.16'
def javaVersion = '21'

// Job groups are defined here, because sometimes we move them and it is easier
// when these lists are close together.
def ant_heavy_jobs = [
    "connector_group_4",
    "naming_all",
    "ql_gf_full_profile_all"
]

def ant_light_jobs = [
    "webservice_all",
    "ejb_group_3",
    "ejb_group_1",
    "connector_group_1",
    "deployment_all",
    "security_all",
    "jdbc_group4",
    "jdbc_group3",
    "web_jsp",
    "cdi_all",
    "ejb_group_2",
    "ejb_group_embedded",
    "jdbc_group1",
    "jdbc_group5",
    "connector_group_2",
    "connector_group_3",
    "jdbc_group2",
    "persistence_all",
    "ql_gf_web_profile_all",
    "batch_all"
]

def mvn_jobs = [
    "admin-tests-parent",
    "application-tests",
    "embedded-tests"
]

def podYamlConfigurationTemplate = """
apiVersion: v1
kind: Pod
spec:
  shareProcessNamespace: true
  nodeSelector:
    kubernetes.io/os: "linux"
  containers:
  - name: jnlp
    imagePullPolicy: IfNotPresent
    alwaysPullImage: false
    tty: false
    workingDir: "/home/jenkins/agent"
    env:
    - name: "JENKINS_JAVA_OPTS"
      value: "-Xms64m -Xmx768m -Xss512k -XX:MaxGCPauseMillis=1000 -XX:+UseShenandoahGC"
    volumeMounts:
    - name: "known-hosts"
      mountPath: "/home/jenkins/.ssh"
    resources:
      limits:
        memory: "0.75Gi"
        cpu: "500m"
      requests:
        memory: "0.75Gi"
        cpu: "500m"
  - name: action
    image: maven:${mvnVersion}-eclipse-temurin-${javaVersion}
    imagePullPolicy: IfNotPresent
    alwaysPullImage: false
    command:
    - sleep
    args:
    - 99d
    tty: false
    workingDir: /home/jenkins/agent
    env:
    - name: "HOME"
      value: "/home/jenkins"
    - name: "MAVEN_OPTS"
      value: "-Duser.home=/home/jenkins -Xms1g -Xmx1g -Xss512k -XX:MaxGCPauseMillis=200 -XX:+UseShenandoahGC -XX:+UseStringDeduplication"
    - name: "ANT_HOME"
      value: "${antHome}"
    - name: "ANT_VERSION"
      value: "${antVersion}"
    - name: "ANT_OPTS"
      value: "-Duser.home=/home/jenkins -Xms64m -Xmx256m -Xss512k -XX:MaxGCPauseMillis=200 -XX:+UseShenandoahGC -XX:+UseStringDeduplication"
    volumeMounts:
    - name: "jenkins-home"
      mountPath: "/home/jenkins"
    - name: "known-hosts"
      mountPath: "/home/jenkins/.ssh"
    - name: "workspace-volume"
      mountPath: "/home/jenkins/agent"
      readOnly: false
    - name: "m2-mvnd"
      mountPath: "/home/jenkins/.m2/mvnd"
    - name: "m2-dir"
      mountPath: "/home/jenkins/.m2/toolchains.xml"
      subPath: "toolchains.xml"
      readOnly: true
    - name: "m2-dir"
      mountPath: "/home/jenkins/.mavenrc"
      subPath: ".mavenrc"
      readOnly: true
    - name: "m2-wrapper"
      mountPath: "/home/jenkins/.m2/wrapper"
    - name: "maven-repo-shared-storage"
      mountPath: "/home/jenkins/.m2/repository"
    - name: "maven-repo-local-storage"
      mountPath: "/home/jenkins/.m2/repository/org/glassfish/main"
    - name: "settings-xml"
      mountPath: "/home/jenkins/.m2/settings.xml"
      subPath: "settings.xml"
      readOnly: true
    - name: "settings-security-xml"
      mountPath: "/home/jenkins/.m2/settings-security.xml"
      subPath: "settings-security.xml"
      readOnly: true
    resources: VAR_RESOURCES
  volumes:
  - name: "jenkins-home"
    emptyDir:
      sizeLimit: "4Gi"
  - name: "known-hosts"
    configMap:
      name: "known-hosts"
  - name: "m2-mvnd"
    emptyDir: {}
  - name: "m2-dir"
    configMap:
      name: "m2-dir"
  - name: "m2-wrapper"
    emptyDir: {}
  - name: "m2-secret-dir"
    secret:
      secretName: "m2-secret-dir"
  - name: "maven-repo-shared-storage"
    persistentVolumeClaim:
      claimName: "glassfish-maven-repo-storage"
  - name: "maven-repo-local-storage"
    emptyDir:
      sizeLimit: "2Gi"
  - name: "settings-xml"
    secret:
      secretName: "m2-secret-dir"
      items:
      - key: "settings.xml"
        path: "settings.xml"
  - name: "settings-security-xml"
    secret:
      secretName: "m2-secret-dir"
      items:
      - key: "settings-security.xml"
        path: "settings-security.xml"
"""

def antHeavyContainerCfg = podYamlConfigurationTemplate.replace(
"""    resources: VAR_RESOURCES
""",
"""    resources:
      limits:
        memory: "4Gi"
        cpu: "3000m"
      requests:
        memory: "4Gi"
        cpu: "3000m"
"""
)

def antLightContainerCfg = podYamlConfigurationTemplate.replace(
"""    resources: VAR_RESOURCES
""",
"""    resources:
      limits:
        memory: "3.2Gi"
        cpu: "2500m"
      requests:
        memory: "3.2Gi"
        cpu: "2500m"
"""
)

def mvnHeavyContainerCfg = podYamlConfigurationTemplate.replace(
"""    resources: VAR_RESOURCES
""",
"""    resources:
      limits:
        memory: "7Gi"
        cpu: "7800m"
      requests:
        memory: "7Gi"
        cpu: "7800m"
"""
)

def mvnLightContainerCfg = podYamlConfigurationTemplate.replace(
"""    resources: VAR_RESOURCES
""",
"""    resources:
      limits:
        memory: "5Gi"
        cpu: "4000m"
      requests:
        memory: "5Gi"
        cpu: "4000m"
"""
)

def tinyContainerCfg = podYamlConfigurationTemplate.replace(
"""    resources: VAR_RESOURCES
""",
"""    resources:
      limits:
        memory: "1Gi"
        cpu: "1000m"
      requests:
        memory: "1Gi"
        cpu: "1000m"
"""
)

def installAntToSharedCache() {
   sh (label: 'Ant Installation', script: '''
   if [ ! -x "${ANT_HOME}/bin/ant" ]; then
      echo "Installing Ant ${ANT_VERSION} into shared cache..."
      tmp_extract=\$(mktemp -d -p /home/jenkins/.m2/repository/.ci-tools 2>/dev/null || mktemp -d)
      mkdir -p /home/jenkins/.m2/repository/.ci-tools
      curl -fsSL "https://downloads.apache.org/ant/binaries/apache-ant-${ANT_VERSION}-bin.tar.gz" -o "\${tmp_extract}/ant.tar.gz"
      tar -xzf "\${tmp_extract}/ant.tar.gz" -C "\${tmp_extract}"
      chmod +x "\${tmp_extract}/apache-ant-${ANT_VERSION}/bin/ant"
      mv "\${tmp_extract}/apache-ant-${ANT_VERSION}" "${ANT_HOME}" 2>/dev/null || true
      rm -rf "\${tmp_extract}"
   else
      echo "Ant ${ANT_VERSION} already present in shared cache, skipping install."
   fi
   "${ANT_HOME}/bin/ant" -version
   ''')
}

def dumpSysInfo() {
   sh (label: 'Dump System Info', script: """
   id || true
   uname -a || true
   env | sort || true
   df -h || true
   \${JAVA_HOME}/bin/jcmd || true
   mvn -version || true
   \${ANT_HOME}/bin/ant -version || true
   ps -e -o start,etime,pid,rss,drs,command || true
   lscpu || true
   cat /proc/meminfo || true
   ulimit -a || true
   """)
}

def startVmstatLogging(String stageName) {
   sh (label: 'Start Performance Logging', script: """
   mkdir -p "${WORKSPACE}/logs"
   setsid vmstat -t -w -a -y 10 < /dev/null > "${WORKSPACE}/logs/vmstat-${stageName}.log" 2>&1 &
   echo \$! > "${WORKSPACE}/vmstat.pid"

   if [ -r /sys/fs/cgroup/memory.current ]; then
      memory_current=/sys/fs/cgroup/memory.current
      memory_peak=/sys/fs/cgroup/memory.peak
   elif [ -r /sys/fs/cgroup/memory/memory.usage_in_bytes ]; then
      memory_current=/sys/fs/cgroup/memory/memory.usage_in_bytes
      memory_peak=/sys/fs/cgroup/memory/memory.max_usage_in_bytes
   else
      memory_current=
      memory_peak=
   fi

   if [ -n "\$memory_current" ]; then
      setsid sh -c '
         printf "# current=%s peak=%s\\n" "'"\$memory_current"'" "'"\$memory_peak"'"
         while true; do
            current=\$(cat "'"\$memory_current"'" 2>/dev/null || echo unavailable)
            if [ -n "'"\$memory_peak"'" ] && [ -r "'"\$memory_peak"'" ]; then
               peak=\$(cat "'"\$memory_peak"'" 2>/dev/null || echo unavailable)
            else
               peak=unavailable
            fi
            printf "%s memory.current=%s memory.peak=%s\\n" "\$(date "+%Y-%m-%dT%H:%M:%S%z")" "\$current" "\$peak"
            sleep 10
         done
      ' < /dev/null > "${WORKSPACE}/logs/cgroup-memory-${stageName}.log" 2>&1 &
      echo \$! > "${WORKSPACE}/cgroup-memory.pid"
   fi
   """)
}

def stopVmstatLogging() {
   sh (label: "Stop Performance Logging", script: """
   for pidfile in vmstat.pid cgroup-memory.pid; do
      if [ -f "${WORKSPACE}/\$pidfile" ]; then
         pkill -F "${WORKSPACE}/\$pidfile" || true
         rm -f "${WORKSPACE}/\$pidfile"
      fi
   done
   df -h || true
   """)
   archiveArtifacts artifacts: "logs/*", allowEmptyArchive: true
}

// Allocation of a node to execute action and the execution. If the allocation fails,
// it can be repeated several times (maxInfraRetries).
// job - job and stage name
// label - used to limit the number of running parallel stages of the same label
// action - action to execute
def runOnNode(String job, String label, boolean archiveServerLogs, Closure action) {
   return {
      def infraRetries = 0
      def maxInfraRetries = 10
      while (infraRetries < maxInfraRetries) {
         def infraError = false
         node("${label}") {
            stage("${job}") {
               try {
                  container('action') {
                     script {
                        def vmstatStarted = false
                        try {
                           startVmstatLogging("${job}")
                           vmstatStarted = true
                           dumpSysInfo()
                           unstash 'maven-repo'
                           action()
                        } finally {
                           if (vmstatStarted) {
                              stopVmstatLogging()
                           }
                        }
                     }
                  }
               } catch (e) {
                  echo "Something broke: ${e}";
                  def errorMsg = e.getMessage() ?: ""
                  if (errorMsg.contains("Failed to start websocket connection")) {
                     infraError = true
                     infraRetries++
                     if (infraRetries >= maxInfraRetries) {
                        throw e
                     }
                     echo "⚠️ K8s Infrastructure failure detected (${errorMsg}). Spawning fresh pod (Attempt ${infraRetries}/${maxInfraRetries})..."
                  } else {
                     throw e
                  }
               } finally {
                  if (!infraError) {
                     archiveArtifacts artifacts: "${job}-results.tar.gz", allowEmptyArchive: true
                     junit testResults: 'results/junitreports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
                     junit testResults: '**/surefire-reports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
                     junit testResults: '**/failsafe-reports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
                     if (archiveServerLogs) {
                        archiveArtifacts artifacts: "**/server.log*", onlyIfSuccessful: false, allowEmptyArchive: true
                     }
                  }
               }
            }
         }
         if (!infraError) {
            break
         }
      }
   }
}

def generateAntPod(job, label) {
   return runOnNode(job, label, false, {
      unstash 'appserv-tests'
      timeout(time: 1, unit: 'HOURS') {
         sh (label: 'Unpack Requirements', script: '''
         # Mandatory requirement -> fail fast if not available.
         export BUNDLES_DIR="${WORKSPACE}/bundles"
         export PATH="${ANT_HOME}/bin:${PATH}"
         ant -version
         mvn -version
         ls -la ${WORKSPACE}
         mkdir -p ${WORKSPACE}/appserver/tests
         tar -xzf ${BUNDLES_DIR}/maven-repo.tar.gz --overwrite -m -p -C /home/jenkins/.m2/repository
         tar -xzf ${BUNDLES_DIR}/appserv-tests.tar.gz -C ${WORKSPACE}
         ''')
         sh (label: "./runtests.sh ${job}", script: """
         export BUNDLES_DIR="\${WORKSPACE}/bundles"
         export PATH="\${ANT_HOME}/bin:\${PATH}"
         ./runtests.sh ${job}
         """)
      }
   })
}

def generateMvnTestPod(job, label) {
   return runOnNode(job, label, true, {
      unstash 'git'
      timeout(time: 1, unit: 'HOURS') {
         sh (label: 'Unpack Requirements', script: '''
         git reset --hard
         tar -xzf ${BUNDLES_DIR}/maven-repo.tar.gz --overwrite -m -p -C /home/jenkins/.m2/repository
         ''')
         sh (label: "mvn clean verify -pl :${job}", script: """
         mvn -V -B -e clean verify -Psnapshots -pl :${job} -amd
         """)
      }
   })
}

pipeline {
   // Do not hold one large Maven pod for the lifetime of the Pipeline.
   // Prepare/Build and main-tests each get their own stage-scoped Maven pod,
   // so Kubernetes can reclaim each pod's reservation as soon as that work
   // is complete.
   agent none
   environment {
      // Keep this relative because there is no pipeline-wide WORKSPACE when
      // using agent none. Each pod gets the same bundles/ layout after unstash.
      BUNDLES_DIR = "bundles"
      PORT_ADMIN=4848
      PORT_HTTP=8080
      PORT_HTTPS=8181
   }

   options {
      // numToKeepStr - we need to know if it is changing.
      // artifactNumToKeepStr - they are quite large, so we keep just the last products.
      buildDiscarder(logRotator(numToKeepStr: '1', artifactNumToKeepStr: '1'))
      // Any failure will cause interruption of other running steps.
      // Dynamic Kubernetes-agent infrastructure failures are retried inside each branch first.
      parallelsAlwaysFailFast()
      // to allow re-running a test stage, preserves just stashes of the most recent build
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
      // Check Changes and Build deliberately share one pod. The pod is
      // released immediately after Build.
      stage('Prepare') {
         agent {
            kubernetes {
               instanceCap 3
               yaml mvnHeavyContainerCfg
            }
         }
         stages {
            stage('Check Changes') {
               steps {
                  checkout scm
                  container('action') {
                     script {
                        // Workaround - after regular purging of the repository mvn fails with AccessDeniedException.
                        sh "mkdir -p /home/jenkins/.m2/repository/org"
                        // Default: run tests
                        env.SKIP_TESTS = "false"
                        // Only check for docs-only changes in PR builds
                        if (env.CHANGE_TARGET) {
                           echo "PR build detected, checking if only docs changed..."
                           def relevantChanges = sh(label: 'Git Diff Check', returnStdout: true, script: '''
                              (git diff --exit-code --name-only origin/${CHANGE_TARGET}...HEAD && echo "all") | sed '/^docs[/]/d'
                              ''').trim()
                           if (relevantChanges == "") {
                              env.SKIP_TESTS = "true"
                              echo "✓ Only docs/ changes detected - tests will be skipped"
                           } else {
                              echo "✗ Relevant changes detected - tests will run"
                           }
                        } else {
                           echo "Non-PR build - tests will always run"
                        }
                     }
                     // Stash takes some time,
                     stash includes: '.git/**/*', name: 'git', useDefaultExcludes: false
                  }
               }
            }
            stage('Build') {
               steps {
                  container('action') {
                     script {
                        try {
                           startVmstatLogging('mvn-build')
                           timeout(time: 1, unit: 'HOURS') {
                              dumpSysInfo()
                              sh (label: 'mvn clean validate', script:  '''
                              mvn -B -e -fae clean validate -Ptck,set-version-id,snapshots
                              ''')
// Makes build 6 minutes slower.
//                              sh (label: 'Download Maven Plugins', script: '''
//                              mvn -B -e dependency:resolve-plugins -T8C
//                              ''')
                              sh (label: 'mvn install', script: '''
                              mvn -B -e install -Pfastest,ci,snapshots -T4C
                              ''')
                              sh (label: 'Pack for Test Stages', script: '''
                              mvn -B -e clean
                              mkdir -p ${BUNDLES_DIR}
                              tar -c -C ${WORKSPACE} runtests.sh appserver/tests/common_test.sh appserver/tests/gftest.sh appserver/tests/appserv-tests appserver/tests/quicklook | gzip --fast > ${BUNDLES_DIR}/appserv-tests.tar.gz
                              tar -c -C /home/jenkins/.m2/repository org/glassfish/main/distributions org/glassfish/main/extras org/glassfish/main/tests org/glassfish/main/nucleus-parent org/glassfish/main/glassfish-nucleus-parent org/glassfish/main/glassfish-parent org/glassfish/main/glassfish-qa-config | gzip --fast > ${BUNDLES_DIR}/maven-repo.tar.gz
                              ''')
                              sh (label: "Copy to ${BUNDLES_DIR}", script: '''
                              # For easy access to built artifacts and using them elsewhere
                              gfVersion="$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)"
                              mvn_copy="mvn -N dependency:copy -DoutputDirectory=${BUNDLES_DIR}"
                              ${mvn_copy} -Dartifact="org.glassfish.main.distributions:glassfish:${gfVersion}:zip"
                              ${mvn_copy} -Dartifact="org.glassfish.main.distributions:web:${gfVersion}:zip"
                              ${mvn_copy} -Dartifact="org.glassfish.main.extras:glassfish-embedded-all:${gfVersion}:jar"
                              ${mvn_copy} -Dartifact="org.glassfish.main.extras:glassfish-embedded-web:${gfVersion}:jar"
                              ls -la ${BUNDLES_DIR}
                              ''')
                              installAntToSharedCache()
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
         }
      }

      stage('Test') {
         when {
            environment name: 'SKIP_TESTS', value: 'false'
         }
         parallel {
            stage('MainTests') {
               agent {
                  kubernetes {
                     instanceCap 3
                     yaml mvnHeavyContainerCfg
                  }
               }
               steps {
                  container('action') {
                     script {
                        try {
                           startVmstatLogging('main-tests')
                           unstash 'git'
                           unstash 'maven-repo'
                           timeout(time: 4, unit: 'HOURS') {
                             dumpSysInfo()
                             sh (label: 'Unpack Requirements', script: '''
                             git reset --hard
                             tar -xzf ${BUNDLES_DIR}/maven-repo.tar.gz --overwrite -m -p -C /home/jenkins/.m2/repository
                             ''')
                             sh (label: 'mvn clean verify', script: '''
                             mvn -B -e clean verify -Pqa,ci,ci-main-tests,snapshots
                             ''')
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
                     junit testResults: '**/surefire-reports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
                     junit testResults: '**/failsafe-reports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
// Makes Jenkins UI extremely slow in current version
//                    recordIssues name: "CheckStyle - main", enabledForFailure: true, tools: [checkStyle(pattern: '**/checkstyle-result.xml')]
                  }
               }
            }
            stage('ITests') {
               steps {
                  script {
                     def nodeGroupLabel = 'maven-shared-pod-light'
                     podTemplate(
                        name: nodeGroupLabel,
                        label: nodeGroupLabel,
                        instanceCap: 3,
                        slaveConnectTimeout: 120,
                        yaml: mvnLightContainerCfg
                     ) {
                        echo "Starting parallel ITests stages."
                        parallel mvn_jobs.collectEntries {
                           ["${it}": generateMvnTestPod(it, nodeGroupLabel)]
                        }
                        echo "Finished parallel ITests stages."
                     }
                  }
               }
            }
            stage('Ant-Heavy') {
               steps {
                  script {
                     def nodeGroupLabel = 'ant-shared-pod-heavy'
                     podTemplate(
                        name: nodeGroupLabel,
                        label: nodeGroupLabel,
                        instanceCap: 3,
                        slaveConnectTimeout: 120,
                        yaml: antHeavyContainerCfg
                     ) {
                        echo "Starting parallel Ant-Heavy stages."
                        parallel ant_heavy_jobs.collectEntries {
                           ["${it}": generateAntPod(it, nodeGroupLabel)]
                        }
                        echo "Finished parallel Ant-Heavy stages."
                     }
                  }
               }
            }
            stage('Ant-Light') {
               steps {
                  script {
                     def nodeGroupLabel = 'ant-shared-pod-light'
                     podTemplate(
                        name: nodeGroupLabel,
                        label: nodeGroupLabel,
                        instanceCap: 6,
                        slaveConnectTimeout: 120,
                        yaml: antLightContainerCfg
                     ) {
                        echo "Starting parallel Ant-Light stages."
                        parallel ant_light_jobs.collectEntries {
                           ["${it}": generateAntPod(it, nodeGroupLabel)]
                        }
                        echo "Finished parallel Ant-Light stages."
                     }
                  }
               }
            }
         }
      }

      stage('Clear Stashes') {
         agent {
            kubernetes {
               yaml tinyContainerCfg
            }
         }
         steps {
            // Overwrite stashes with empty content
            stash includes: 'nothing', name: 'git', allowEmpty: true
            stash includes: 'nothing', name: 'appserv-tests', allowEmpty: true
            stash includes: 'nothing', name: 'maven-repo', allowEmpty: true
         }
      }
   }
}
