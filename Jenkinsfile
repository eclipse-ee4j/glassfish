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
def mvnVersion = '3.9.16'
def javaVersion = '21'
def jdkTool = "temurin-jdk${javaVersion}-latest"
def mvnTool = "apache-maven-${mvnVersion}"
// The inherited JIRO "basic" template has alwaysPullImage=true for jnlp.
// Kubernetes-plugin inheritance treats false as a default value, so a child
// containerTemplate cannot turn an inherited true back to false. Therefore the
// test pods below reproduce the relevant basic pod mounts directly and define
// jnlp as a non-inherited containerTemplate with alwaysPullImage=false.
// Ant shell/test execution itself runs in a dedicated sidecar container; jnlp
// is used only for Jenkins Remoting.
def antPodCfg = """
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    kubernetes.io/os: "linux"
  containers:
  - name: jnlp
    env:
    - name: "JENKINS_REMOTING_JAVA_OPTS"
      value: "-showversion -XshowSettings:vm -Xmx256m -Dorg.jenkinsci.remoting.engine.JnlpProtocol3.disabled=true -Dorg.jenkinsci.plugins.gitclient.CliGitAPIImpl.useSETSID=true"
    - name: "JAVA_TOOL_OPTIONS"
      value: ""
    - name: "_JAVA_OPTIONS"
      value: ""
    - name: "OPENJ9_JAVA_OPTIONS"
      value: "-XX:+IgnoreUnrecognizedVMOptions -XX:+IdleTuningCompactOnIdle -XX:+IdleTuningGcOnIdle"
    volumeMounts:
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
    - name: "tools"
      mountPath: "/opt/tools"
      readOnly: true
    - name: "m2-repository"
      mountPath: "/home/jenkins/.m2/repository"
    - name: "jenkins-home-basic"
      mountPath: "/home/jenkins"
    - name: "m2-secret-dir"
      mountPath: "/home/jenkins/.m2/settings-security.xml"
      subPath: "settings-security.xml"
      readOnly: true
    - name: "m2-wrapper"
      mountPath: "/home/jenkins/.m2/wrapper"
    - name: "m2-secret-dir"
      mountPath: "/home/jenkins/.m2/settings.xml"
      subPath: "settings.xml"
      readOnly: true
    - name: "known-hosts"
      mountPath: "/home/jenkins/.ssh"
  # Keep Jenkins Remoting in jnlp, but execute all Ant-side shell commands in
  # this sidecar via container('ant'). This mirrors the Maven execution model
  # and avoids Durable Task launching shells directly in the custom jnlp agent.
  - name: ant
    image: docker.io/eclipsecbi/jiro-agent-basic-ubuntu:remoting-3355.3357.v931d3c992987
    imagePullPolicy: IfNotPresent
    command:
    - cat
    tty: true
    workingDir: /home/jenkins/agent
    env:
    - name: "HOME"
      value: "/home/jenkins"
    - name: "JAVA_TOOL_OPTIONS"
      value: ""
    - name: "_JAVA_OPTIONS"
      value: ""
    - name: "OPENJ9_JAVA_OPTIONS"
      value: "-XX:+IgnoreUnrecognizedVMOptions -XX:+IdleTuningCompactOnIdle -XX:+IdleTuningGcOnIdle"
    volumeMounts:
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
    - name: "tools"
      mountPath: "/opt/tools"
      readOnly: true
    - name: "m2-repository"
      mountPath: "/home/jenkins/.m2/repository"
    - name: "jenkins-home-basic"
      mountPath: "/home/jenkins"
    - name: "m2-secret-dir"
      mountPath: "/home/jenkins/.m2/settings-security.xml"
      subPath: "settings-security.xml"
      readOnly: true
    - name: "m2-wrapper"
      mountPath: "/home/jenkins/.m2/wrapper"
    - name: "m2-secret-dir"
      mountPath: "/home/jenkins/.m2/settings.xml"
      subPath: "settings.xml"
      readOnly: true
    - name: "known-hosts"
      mountPath: "/home/jenkins/.ssh"
    - name: "workspace-volume"
      mountPath: "/home/jenkins/agent"
      readOnly: false
    resources:
      limits:
        memory: "4096Mi"
        cpu: "2000m"
      requests:
        # jnlp already reserves the pod's historical 4 GiB footprint.
        # Reserve only a small additional amount for the execution sidecar.
        memory: "256Mi"
        cpu: "500m"
  volumes:
  - name: "m2-mvnd"
    emptyDir: {}
  - name: "m2-dir"
    configMap:
      name: "m2-dir"
  - name: "tools"
    persistentVolumeClaim:
      claimName: "tools-claim-jiro-glassfish"
      readOnly: true
  - name: "m2-repository"
    emptyDir: {}
  - name: "jenkins-home-basic"
    emptyDir: {}
  - name: "m2-wrapper"
    emptyDir: {}
  - name: "m2-secret-dir"
    secret:
      secretName: "m2-secret-dir"
  - name: "known-hosts"
    configMap:
      name: "known-hosts"
"""

def mvnContainerCfg = """
apiVersion: v1
kind: Pod
spec:
  nodeSelector:
    kubernetes.io/os: "linux"
  containers:
  - name: jnlp
    env:
    - name: "JENKINS_REMOTING_JAVA_OPTS"
      value: "-showversion -XshowSettings:vm -Xmx256m -Dorg.jenkinsci.remoting.engine.JnlpProtocol3.disabled=true -Dorg.jenkinsci.plugins.gitclient.CliGitAPIImpl.useSETSID=true"
    - name: "JAVA_TOOL_OPTIONS"
      value: ""
    - name: "_JAVA_OPTIONS"
      value: ""
    - name: "OPENJ9_JAVA_OPTIONS"
      value: "-XX:+IgnoreUnrecognizedVMOptions -XX:+IdleTuningCompactOnIdle -XX:+IdleTuningGcOnIdle"
    volumeMounts:
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
    - name: "tools"
      mountPath: "/opt/tools"
      readOnly: true
    - name: "m2-repository"
      mountPath: "/home/jenkins/.m2/repository"
    - name: "jenkins-home-basic"
      mountPath: "/home/jenkins"
    - name: "m2-secret-dir"
      mountPath: "/home/jenkins/.m2/settings-security.xml"
      subPath: "settings-security.xml"
      readOnly: true
    - name: "m2-wrapper"
      mountPath: "/home/jenkins/.m2/wrapper"
    - name: "m2-secret-dir"
      mountPath: "/home/jenkins/.m2/settings.xml"
      subPath: "settings.xml"
      readOnly: true
    - name: "known-hosts"
      mountPath: "/home/jenkins/.ssh"
  - name: maven
    image: maven:${mvnVersion}-eclipse-temurin-${javaVersion}
    imagePullPolicy: IfNotPresent
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
    - name: "maven-repo-shared-storage"
      mountPath: "/home/jenkins/.m2/repository"
    - name: "settings-xml"
      mountPath: "/home/jenkins/.m2/settings.xml"
      subPath: "settings.xml"
      readOnly: true
    - name: "settings-security-xml"
      mountPath: "/home/jenkins/.m2/settings-security.xml"
      subPath: "settings-security.xml"
      readOnly: true
    - name: "maven-repo-local-storage"
      mountPath: "/home/jenkins/.m2/repository/org/glassfish/main"
    - name: "workspace-volume"
      mountPath: "/home/jenkins/agent"
      readOnly: false
    resources:
      limits:
        memory: "8Gi"
        cpu: "5500m"
      requests:
        memory: "8Gi"
        cpu: "5500m"
  volumes:
  - name: "m2-mvnd"
    emptyDir: {}
  - name: "m2-dir"
    configMap:
      name: "m2-dir"
  - name: "tools"
    persistentVolumeClaim:
      claimName: "tools-claim-jiro-glassfish"
      readOnly: true
  - name: "m2-repository"
    emptyDir: {}
  - name: "jenkins-home-basic"
    emptyDir: {}
  - name: "m2-wrapper"
    emptyDir: {}
  - name: "m2-secret-dir"
    secret:
      secretName: "m2-secret-dir"
  - name: "known-hosts"
    configMap:
      name: "known-hosts"
  - name: "jenkins-home"
    emptyDir:
      sizeLimit: "4Gi"
  - name: "maven-repo-shared-storage"
    persistentVolumeClaim:
      claimName: "glassfish-maven-repo-storage"
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
  - name: "maven-repo-local-storage"
    emptyDir:
      sizeLimit: "2Gi"
"""
def dumpSysInfo() {
   sh """
   id || true
   uname -a || true
   env | sort || true
   df -h || true
   hostname -I || true
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
// Use deterministic per-build jitter instead of java.util.Random so this stays
// simple and reproducible in Jenkins Pipeline execution.
def podJitterSeconds(String key, int maxInclusive) {
   if (maxInclusive <= 0) {
      return 0
   }
   String buildSeed = env.BUILD_TAG ?: env.BUILD_NUMBER ?: '0'
   String seed = "${buildSeed}:${key}"
   int positiveHash = seed.hashCode() & 0x7fffffff
   return positiveHash % (maxInclusive + 1)
}

// Dynamic pod slot 0 starts immediately. Every following slot is spaced by
// 3 seconds and receives another 0-2 seconds of jitter.
def staggerPodStart(int slot, String job) {
   if (slot == 0) {
      echo "${job}: requesting pod immediately (slot 0)"
      return
   }
   int baseDelay = slot * 3
   int jitter = podJitterSeconds("start:${job}", 2)
   int delay = baseDelay + jitter
   echo "${job}: delaying pod request by ${delay}s (slot ${slot}, base ${baseDelay}s + jitter ${jitter}s)"
   sleep time: delay, unit: 'SECONDS'
}

// Five total attempts. Each retry waits substantially longer than the previous
// one before asking Kubernetes for another fresh pod:
//   attempt 2:  20-30s
//   attempt 3:  40-60s
//   attempt 4:  80-120s
//   attempt 5: 160-240s
def waitBeforePodRetry(int attempt, String job) {
   if (attempt <= 1) {
      return
   }
   int baseDelay = 20 * (1 << (attempt - 2))
   int jitter = podJitterSeconds("retry:${job}:${attempt}", baseDelay.intdiv(2))
   int delay = baseDelay + jitter
   echo "${job}: Kubernetes agent attempt ${attempt}/5; waiting ${delay}s before requesting a fresh pod"
   sleep time: delay, unit: 'SECONDS'
}

def runAntJob(job, int startSlot, String nodeCfg) {
   stage("${job}") {
         if (startSlot >= 0) {
            staggerPodStart(startSlot, job)
         } else {
            echo "${job}: Ant worker slot is free; requesting pod now"
         }
         podTemplate(
            containers: [
               containerTemplate(
                  name: 'jnlp',
                  image: 'docker.io/eclipsecbi/jiro-agent-basic-ubuntu:remoting-3355.3357.v931d3c992987',
                  alwaysPullImage: false,
                  ttyEnabled: true,
                  workingDir: '/home/jenkins/agent',
                  resourceRequestMemory: '4096Mi',
                  resourceRequestCpu: '500m',
                  resourceLimitMemory: '4096Mi',
                  resourceLimitCpu: '2000m'
               )
            ],
            yaml: nodeCfg
         ) {
            int attempt = 0
            retry(count: 5, conditions: [kubernetesAgent(), nonresumable()]) {
               attempt++
               waitBeforePodRetry(attempt, job)
               node(POD_LABEL) {
                  boolean vmstatStarted = false
                  try {
                     container('ant') {
                        // Fail quickly if Jenkins cannot execute commands in the
                        // sidecar. In build #7 a broken first sh otherwise took
                        // about two hours to be detected by Durable Task.
                        timeout(time: 2, unit: 'MINUTES') {
                           sh '''
                           echo "Ant execution container: ${POD_CONTAINER:-unknown}"
                           id
                           test -w "${WORKSPACE}"
                           test -x /bin/sh
                           '''
                        }

                        startVmstatLogging("ant-${job}")
                        vmstatStarted = true

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
                     }
                  } finally {
                     if (vmstatStarted) {
                        container('ant') {
                           stopVmstatLogging()
                        }
                     }
                     archiveArtifacts artifacts: "${job}-results.tar.gz", allowEmptyArchive: true
                     junit testResults: 'results/junitreports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
                  }
               }
            }
         }
      }
}

// Each Ant worker runs one job at a time. With 15 workers, at most 15 Ant
// pod allocations can be active concurrently. When a worker finishes a job, it
// immediately starts its next assigned job (without another initial stagger).
def generateAntWorker(int workerNumber, List jobs, String nodeCfg, int initialStartSlot) {
   return {
      echo "Ant worker ${workerNumber}: ${jobs.size()} assigned job(s)"
      for (int jobIndex = 0; jobIndex < jobs.size(); jobIndex++) {
         String job = jobs[jobIndex]
         int startSlot = jobIndex == 0 ? initialStartSlot : -1
         runAntJob(job, startSlot, nodeCfg)
      }
   }
}

def generateMvnTestPodTemplate(job, nodeCfg, int startSlot) {
   return {
      stage("${job}") {
         staggerPodStart(startSlot, job)
         podTemplate(
            containers: [
               containerTemplate(
                  name: 'jnlp',
                  image: 'docker.io/eclipsecbi/jiro-agent-basic-ubuntu:remoting-3355.3357.v931d3c992987',
                  alwaysPullImage: false,
                  ttyEnabled: true,
                  workingDir: '/home/jenkins/agent',
                  resourceRequestMemory: '4096Mi',
                  resourceRequestCpu: '500m',
                  resourceLimitMemory: '4096Mi',
                  resourceLimitCpu: '2000m'
               )
            ],
            yaml: nodeCfg
         ) {
            int attempt = 0
            retry(count: 5, conditions: [kubernetesAgent(), nonresumable()]) {
               attempt++
               waitBeforePodRetry(attempt, job)
               node(POD_LABEL) {
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
                                 '''
                                 sh """
                                 mvn -V -B -e clean verify -Psnapshots -pl :${job} -amd
                                 """
                              }
                           } finally {
                              stopVmstatLogging()
                           }
                        }
                     }
                  } finally {
                     archiveArtifacts artifacts: "**/server.log*", onlyIfSuccessful: false, allowEmptyArchive: true
                     junit testResults: '**/surefire-reports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
                     junit testResults: '**/failsafe-reports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
// Makes Jenkins UI extremely slow in current version
//                     recordIssues id: "checkstyle-${job}", name: "CheckStyle - ${job}", enabledForFailure: true, tools: [checkStyle(pattern: '**/checkstyle-result.xml')]
                  }
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
def mvnSlotOffset = 0

def parallelStagesMapMvn = mvn_jobs.collectEntries {
   ["${it}": generateMvnTestPodTemplate(it, mvnContainerCfg, mvnSlotOffset + mvn_jobs.indexOf(it))]
}

// Global Ant concurrency limit. This is deliberately implemented with a fixed
// number of Pipeline worker branches instead of depending on Lockable Resources
// or Throttle Concurrent Builds plugins.
def maxConcurrentAntPods = 15
def ant_jobs = ant_connector_jobs + ant_db_jobs + ant_di_jobs + ant_other_jobs
def parallelStagesMapAntWorkers = [:]
for (int workerIndex = 0; workerIndex < maxConcurrentAntPods; workerIndex++) {
   def jobsForWorker = []
   for (int jobIndex = workerIndex; jobIndex < ant_jobs.size(); jobIndex += maxConcurrentAntPods) {
      jobsForWorker.add(ant_jobs[jobIndex])
   }
   if (!jobsForWorker.isEmpty()) {
      // Maven dynamic pods use slots 0..2. Stagger the first Ant job in each
      // worker across slots 3..17; later jobs start when their worker is free.
      int initialStartSlot = mvn_jobs.size() + workerIndex
      parallelStagesMapAntWorkers["ant-worker-${workerIndex + 1}"] =
         generateAntWorker(workerIndex + 1, jobsForWorker, antPodCfg, initialStartSlot)
   }
}
pipeline {
   agent {
      kubernetes {
         // Do not inherit "basic" here: its jnlp ContainerTemplate has
         // alwaysPullImage=true, and the plugin cannot override inherited true
         // with false. The relevant basic mounts are reproduced in mvnContainerCfg.
         yaml mvnContainerCfg
         containerTemplate {
            name 'jnlp'
            image 'docker.io/eclipsecbi/jiro-agent-basic-ubuntu:remoting-3355.3357.v931d3c992987'
            alwaysPullImage false
            ttyEnabled true
            workingDir '/home/jenkins/agent'
            resourceRequestMemory '4096Mi'
            resourceRequestCpu '500m'
            resourceLimitMemory '4096Mi'
            resourceLimitCpu '2000m'
         }
      }
   }
   environment {
      BUNDLES_DIR = "${WORKSPACE}/bundles"
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
      stage('Check Changes') {
         steps {
            checkout scm
            container('maven') {
               script {
                  // Default: run tests
                  env.SKIP_TESTS = "false"
                  // Only check for docs-only changes in PR builds
                  if (env.CHANGE_TARGET) {
                     echo "PR build detected, checking if only docs changed..."
                     def relevantChanges = sh(
                        script: '''
                           (git diff --exit-code --name-only origin/${CHANGE_TARGET}...HEAD && echo "all") | sed '/^docs[/]/d'
                        ''',
                        returnStdout: true
                     ).trim()

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
                         mvn -V -B -e -fae clean validate -Ptck,set-version-id,snapshots
                         '''
                         sh '''
                         # Try to prevent Could not transfer artifact ... from/to eclipse.maven.central.mirror ..
                         # the trustAnchors parameter must be non-empty
                         mvn -B dependency:go-offline -T4C
                         '''
                         sh '''
                         mvn -B -e install -Pfastest,ci,snapshots -T4C
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
         when {
            environment name: 'SKIP_TESTS', value: 'false'
         }
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
                              mvn -B -e clean verify -Pqa,ci,ci-main-tests,snapshots
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
                     junit testResults: '**/surefire-reports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
                     junit testResults: '**/failsafe-reports/*.xml', allowEmptyResults: true, stdioRetention: 'FAILED'
// Makes Jenkins UI extremely slow in current version
//                     recordIssues name: "CheckStyle - main", enabledForFailure: true, tools: [checkStyle(pattern: '**/checkstyle-result.xml')]
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
            stage('ant-tests') {
               tools {
                  jdk "${jdkTool}"
                  maven "${mvnTool}"
               }
               steps {
                  script {
                     parallel parallelStagesMapAntWorkers
                  }
               }
            }
         }
      }
   }
   post {
      success {
         // Overwrite stashes with empty content
         stash includes: 'nothing', name: 'appserv-tests', allowEmpty: true
         stash includes: 'nothing', name: 'maven-repo', allowEmpty: true
      }
   }
}
