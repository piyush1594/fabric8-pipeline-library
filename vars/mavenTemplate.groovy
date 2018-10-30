#!/usr/bin/groovy
import io.fabric8.Fabric8Commands
import io.fabric8.Utils

def call(Map parameters = [:], body) {
    def flow = new Fabric8Commands()

    def defaultLabel = buildId('maven')
    def label = parameters.get('label', defaultLabel)

    def mavenImage = parameters.get('mavenImage', 'openshift/jenkins-slave-maven-centos7:v4.0')
    def jnlpImage = 'openshift/jenkins-slave-base-centos7:v4.0'
    def inheritFrom = parameters.get('inheritFrom', 'base')


    def cloud = flow.getCloudConfig()

    /*
        /home/jenkins is used to support the upstream maven image and this is not stored in env.JENKINS_HOME=/var/lib/jenkins
     */
    def javaOptions = parameters.get('javaOptions', '-Duser.home=/home/jenkins -XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap -Dsun.zip.disableMemoryMapping=true -XX:+UseParallelGC -XX:MinHeapFreeRatio=5 -XX:MaxHeapFreeRatio=10 -XX:GCTimeRatio=4 -XX:AdaptiveSizePolicyWeight=90 -Xms10m -Xmx192m')

        def mavenOpts = parameters.get('mavenOpts', '-Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn')

        podTemplate(cloud: cloud, label: label, inheritFrom: "${inheritFrom}", serviceAccount: 'jenkins',
                containers: [
                        containerTemplate(
                                name: 'jnlp',
                                image: "${jnlpImage}",
                                args: '${computer.jnlpmac} ${computer.name}',
                                workingDir: '/home/jenkins/',
                                resourceLimitMemory: '256Mi'),
                        containerTemplate(
                                name: 'maven',
                                image: "${mavenImage}",
                                command: '/bin/bash -c',
                                args: 'cat',
                                ttyEnabled: true,
                                workingDir: '/home/jenkins/',
                                envVars: [
                                        envVar(key: '_JAVA_OPTIONS', value: javaOptions),
                                        envVar(key: 'MAVEN_OPTS', value: mavenOpts)
                                ],
                                resourceLimitMemory: '640Mi')],
                volumes: [
                        secretVolume(secretName: 'jenkins-maven-settings', mountPath: '/home/jenkins/.m2'),
                        secretVolume(secretName: 'jenkins-release-gpg', mountPath: '/home/jenkins/.gnupg-ro'),
                        secretVolume(secretName: 'jenkins-hub-api-token', mountPath: '/home/jenkins/.apitoken'),
                        secretVolume(secretName: 'jenkins-ssh-config', mountPath: '/root/.ssh-ro'),
                        secretVolume(secretName: 'jenkins-git-ssh', mountPath: '/root/.ssh-git-ro')]) {

            body(

            )
        }
}
