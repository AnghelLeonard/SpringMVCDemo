// change the project version (don't rely on version from pom.xml)
env.BN = VersionNumber([
        versionNumberString : '${BUILD_MONTH}.${BUILDS_TODAY}.${BUILD_NUMBER}', 
        projectStartDate : '2017-02-09', 
        versionPrefix : 'v1.'
    ])
		
node ("master") {                  
    stage('Provision') {                
        echo 'PIPELINE STARTED'
        
        echo 'Checkout source code from GitHub ...'
        retry(5){
            git branch: 'developer', credentialsId: 'GitHub', url: 'git@github.com:AnghelLeonard/SpringMVCDemo.git'
        }
        
        echo 'Change the project version ...'
        def W_M2_HOME = tool 'Maven'
        bat "${W_M2_HOME}\\bin\\mvn versions:set -DnewVersion=$BN -DgenerateBackupPoms=false"                
        
        echo "Create a new branch with name release_${BN} ..."
        def W_GIT_HOME = tool 'Git'
        bat "${W_GIT_HOME} checkout -b release_${BN}"
        
        echo 'Stash the project source code ...'
        stash includes: '**', excludes: '**/TestPlan.jmx', name: 'SOURCE_CODE'
    }
}

parallel UnitTests:{
    node ("TestMachine-ut") {
        // we can also use: withEnv(['M2_HOME=/usr/share/maven', 'JAVA_HOME=/usr']) {}
        env.MAVEN_HOME = '/usr/share/maven'
        env.M2_HOME = '/usr/share/maven'
        env.JAVA_HOME = '/usr'	 
      
        echo 'Preparing Artifactory to resolve dependencies ...'          
        def server = Artifactory.server('artifactory')       
        def rtMaven = Artifactory.newMavenBuild()
        rtMaven.opts = '-Xms1024m -Xmx4096m'
        rtMaven.resolver server: server, releaseRepo: 'virtual-repo', snapshotRepo: 'virtual-repo'
        
        stage('Run-ut') {   
            try{		
                echo 'Unstash the project source code ...'
                unstash 'SOURCE_CODE'	                                                       
                                
                echo 'Run the unit tests (and Jacoco) ...'
                // sh "'${M2_HOME}/bin/mvn' clean test-compile jacoco:prepare-agent test -Djacoco.destFile=target/jacoco.exec"   
                rtMaven.run pom: 'pom.xml', goals: 'clean test-compile jacoco:prepare-agent test -Djacoco.destFile=target/jacoco.exec'

                echo 'Run the Jacoco code coverage report for unit tests ...'
                step([$class: 'JacocoPublisher', canComputeNew: false, defaultEncoding: '', healthy: '', 
                        pattern: '**/target/jacoco.exec', unHealthy: ''])
			
                echo 'Stash Jacoco-ut exec ...'
                stash includes: '**/target/jacoco.exec', name: 'JACOCO_UT' 
            
                echo 'jUnit report (surefire) ...'
                junit allowEmptyResults: true, testResults: '**/target/surefire-reports/*.xml'
                currentBuild.result='SUCCESS'
            }catch (any){
                currentBuild.result='FAILURE'
                step([$class: 'Mailer', notifyEveryUnstableBuild: true, recipients: 'sample@org.com', sendToIndividuals: false])
                throw any
            } finally {                
                // ...
            }
        }
    }
},
IntegrationTests:{
    node ("TestMachine-it") {

        // we can also use: withEnv(['M2_HOME=/usr/share/maven', 'JAVA_HOME=/usr']) {}
        env.MAVEN_HOME = '/usr/share/maven'
        env.M2_HOME = '/usr/share/maven'
        env.JAVA_HOME = '/usr'
        
        echo 'Preparing Artifactory to resolve dependencies ...'          
        def server = Artifactory.server('artifactory')       
        def rtMaven = Artifactory.newMavenBuild()
        rtMaven.opts = '-Xms1024m -Xmx4096m'
        rtMaven.resolver server: server, releaseRepo: 'virtual-repo', snapshotRepo: 'virtual-repo'
	
        stage('Run-it') {
        
            echo 'Unstash the project source code ...'
            unstash 'SOURCE_CODE'
		
            echo 'Start postgresql ...'
            sh 'echo jenkins | sudo -S /etc/init.d/postgresql start'
                        
            echo 'Run the integration tests (and Jacoco) ...'
            // sh "'${M2_HOME}/bin/mvn' clean package jacoco:prepare-agent verify -DskipUTs=true -Djacoco.destFile=target/jacoco-it.exec"
            rtMaven.run pom: 'pom.xml', goals: 'clean package jacoco:prepare-agent verify -DskipUTs=true -Djacoco.destFile=target/jacoco-it.exec'

            echo 'Run the Jacoco code coverage report for integration tests ...'
            step([$class: 'JacocoPublisher', canComputeNew: false, defaultEncoding: '', healthy: '', 
                    pattern: '**/target/jacoco-it.exec', unHealthy: ''])
			
            echo 'Stash Jacoco-it exec ...'
            stash includes: '**/target/jacoco-it.exec', name: 'JACOCO_IT'
            
            echo 'jUnit report (failsafe) ...'
            junit allowEmptyResults: true, testResults: '**/target/failsafe-reports/*.xml'
        }
    }
},
failFast: true

node ("TestMachine-ut") {
    
    // we can also use: withEnv(['M2_HOME=/usr/share/maven', 'JAVA_HOME=/usr']) {}
    env.MAVEN_HOME = '/usr/share/maven'
    env.M2_HOME = '/usr/share/maven'
    env.JAVA_HOME = '/usr'
    
    echo 'Preparing Artifactory to resolve dependencies ...'          
    def server = Artifactory.server('artifactory')       
    def rtMaven = Artifactory.newMavenBuild()
    rtMaven.opts = '-Xms1024m -Xmx4096m'
    rtMaven.resolver server: server, releaseRepo: 'virtual-repo', snapshotRepo: 'virtual-repo'
    
    stage('SCA') {
        echo 'Unstash the project source code ...'
        unstash 'SOURCE_CODE'
                
        echo 'Executing Maven test-compile ...'
        // sh "'${M2_HOME}/bin/mvn' clean test-compile"
        rtMaven.run pom: 'pom.xml', goals: 'clean test-compile'
    }    
    
    parallel Findbugs:{
        stage('Findbugs') {    
            echo 'Running Findbugs ...'
            // sh "'${M2_HOME}/bin/mvn' findbugs:findbugs"
            rtMaven.run pom: 'pom.xml', goals: 'findbugs:findbugs'
            step([$class: 'FindBugsPublisher', canComputeNew: false, defaultEncoding: '', 
                    excludePattern: '', healthy: '', includePattern: '', pattern: '**/target/findbugsXml.xml', unHealthy: ''])
            
        }
    },
    Checkstyle:{
        stage('Checkstyle') {            
            echo 'Running Checkstyle ...'
            // sh "'${M2_HOME}/bin/mvn' checkstyle:check"
            rtMaven.run pom: 'pom.xml', goals: 'checkstyle:check'
            step([$class: 'CheckStylePublisher', canComputeNew: false, defaultEncoding: '', 
                    healthy: '', pattern: '**/target/checkstyle-result.xml', unHealthy: ''])            
        }
    },
    Pmd:{
        stage('Pmd') {         
            echo 'Running PMD ...'		
            // sh "'${M2_HOME}/bin/mvn' pmd:pmd"
            rtMaven.run pom: 'pom.xml', goals: 'pmd:pmd'
            step([$class: 'PmdPublisher', canComputeNew: false, defaultEncoding: '', 
                    healthy: '', pattern: '**/target/pmd.xml', unHealthy: ''])            
        }
    },
    TaskScanner:{
        stage('TaskScanner'){
            echo 'Running TaskRunner ...'
            step([$class: 'TasksPublisher', canComputeNew: false, defaultEncoding: '', 
                    excludePattern: '', healthy: '', high: 'TODO,TO DO,FIXME', low: '', normal: '', pattern: '**/*.java', unHealthy: ''])
        }
    },
    failFast: false
	
    stage('CombinedAnalysis'){
        echo 'Running Analysis publisher ...'
        step([$class: 'AnalysisPublisher', canComputeNew: false, defaultEncoding: '', healthy: '', unHealthy: ''])
    }
}

node("TestMachine-so") {
    
    // we can also use: withEnv(['M2_HOME=/usr/share/maven', 'JAVA_HOME=/usr']) {}
    env.MAVEN_HOME = '/usr/share/maven'
    env.M2_HOME = '/usr/share/maven'
    env.JAVA_HOME = '/usr'
    
    echo 'Preparing Artifactory to resolve dependencies ...'          
    def server = Artifactory.server('artifactory')       
    def rtMaven = Artifactory.newMavenBuild()
    rtMaven.opts = '-Xms1024m -Xmx4096m'
    rtMaven.resolver server: server, releaseRepo: 'virtual-repo', snapshotRepo: 'virtual-repo'
    rtMaven.deployer server: server, releaseRepo: 'snapshot-repo', snapshotRepo: 'snapshot-repo'
    
    stage ("Prepare-Sonar") {        
        echo 'Ping SonarQube ...'
        try{
            retry(5) {
                sh script: 'echo jenkins | sudo -S nc -zv localhost 9000 && echo jenkins | sudo -S nc -zv localhost 9092'
            }
        } catch (error) {
            sh 'echo Start SonarQube ...'
            sh 'echo jenkins | sudo -S /opt/sonarqube/bin/linux-x86-64/sonar.sh start'
        } 
        
        timeout(time: 240, unit: 'SECONDS') {
            waitUntil {
                echo 'Waiting for SonarQube to start ...'
                def result = sh script: 'echo jenkins | sudo -S netstat -tulnp | egrep \'9000|9092\'', returnStatus: true
                return (result == 0);
            }
        }
    } 
    
    stage('Run-Sonar') {     
        echo 'Run sonar:sonar ...'
	
        unstash 'SOURCE_CODE'           
        unstash 'JACOCO_UT'
        unstash 'JACOCO_IT'
                
        // sh "'${M2_HOME}/bin/mvn' test-compile sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dmaven.clean.skip=true" 
        rtMaven.run pom: 'pom.xml', goals: 'test-compile sonar:sonar -Dsonar.host.url=http://localhost:9000 -Dmaven.clean.skip=true'
    }
	
    stage("Publish-Snapshot") {
        echo 'Publish SNAPSHOT war ...'	              
        def buildInfo = rtMaven.run pom: 'pom.xml', goals: 'clean install -DskipTests'
        server.publishBuildInfo buildInfo
    }
}  

node ("master") {
    echo 'Stash the performance tests ...'
    stash includes: '**/TestPlan.jmx', name: 'JMETER_TESTS' 
}

node("PfMachine-jm") {
    
    stage("Start-Payara") {     
        echo 'Starting Payara server ...'
        sh 'echo jenkins | sudo -S /opt/payara41/bin/asadmin start-domain'
    }
    
    stage ("Download-WAR") {
        echo 'Download the application WAR ...'
        
        def downloadWAR = """{
            "files": [{
                "pattern": "snapshot-repo/javaee/SpringMVCDemo/${BN}/*.war",
                "target": "war/"
            }]
        }"""
        
        def server = Artifactory.server('artifactory')
        server.download(downloadWAR)
    }
    
    stage ("Deploy-WAR") {
        echo 'Deploy the application WAR in Payara server ...'
        sh "echo jenkins | sudo -S /opt/payara41/bin/asadmin deploy --contextroot '/SpringMVCDemo' ${WORKSPACE}/war/javaee/SpringMVCDemo/${BN}/SpringMVCDemo-${BN}.war"        
    }
    
    stage ("Deploy-JMeter-Tests") {
        echo 'Unstash JMeter tests ...'
        unstash 'JMETER_TESTS'
    }        
    
    stage ("Run-JMeter-Tests") {
        echo 'Run the JMeter tests ...'
        
        sh "/opt/jmeter/bin/jmeter.sh -Jduration=600 -n -t ${WORKSPACE}/TestPlan.jmx -l ${WORKSPACE}/results.jtl"
        performanceReport compareBuildPrevious: true, configType: 'ART', errorFailedThreshold: 0, errorUnstableResponseTimeThreshold: '', 
        errorUnstableThreshold: 0, failBuildIfNoResultFile: false, ignoreFailedBuilds: true, ignoreUnstableBuilds: true, 
        modeOfThreshold: false, modePerformancePerTestCase: true, modeThroughput: true, nthBuildNumber: 0, 
        parsers: [[$class: 'JMeterParser', glob: "${WORKSPACE}/results.jtl"]], 
        relativeFailedThresholdNegative: 0, relativeFailedThresholdPositive: 0, relativeUnstableThresholdNegative: 0, 
        relativeUnstableThresholdPositive: 0
    }
    
    stage("Promote to staging"){
        echo 'Promoting application from SNAPSHOT to STAGING ...'
        
        def server = Artifactory.server('artifactory')
        def promotionConfig = [
            // Mandatory parameters
        'buildName'          : 'project',
        'buildNumber'        : BUILD_NUMBER,
        'targetRepo'         : 'staging-repo',

            // Optional parameters
        'comment'            : 'Promoting to staging ....',
        'sourceRepo'         : 'snapshot-repo',
        'status'             : 'Staging',
        'includeDependencies': false,
        'copy'               : true,
        'failFast'           : true
        ]

        // Promote build
        server.promote promotionConfig
    }
}

// QA, UAT, ... nodes (e.g. EC2 instances, local machines, etc)
echo 'Running in QA node ...'
echo 'Running in UAT node ...'

echo 'Promoting application from STAGING to RELEASE ...'
stage("Promote to release"){
    def server = Artifactory.server('artifactory')
    def promotionConfig = [
        // Mandatory parameters
        'buildName'          : 'project',
        'buildNumber'        : BUILD_NUMBER,
        'targetRepo'         : 'release-repo',

        // Optional parameters
        'comment'            : 'Promoting to release ....',
        'sourceRepo'         : 'staging-repo',
        'status'             : 'Release',
        'includeDependencies': false,
        'copy'               : true,
        'failFast'           : true
    ]

    // Promote build
    server.promote promotionConfig
}

parallel DeployInProd:{	
    node("ProdMachine-pd") {

        stage("Start-Payara") {    

            echo 'Ping Payara Server ...'
            try{
                retry(5) {
                    sh script: 'echo jenkins | sudo -S nc -zv localhost 8080 && echo jenkins | sudo -S nc -zv localhost 4848'
                }
            } catch (error) {
                sh 'echo Start Payara Server ...'
                sh 'echo jenkins | sudo -S /opt/payara41/bin/asadmin start-domain'
            } 
		                   
        }
    
        stage ("Download-WAR") {
            echo 'Download the application WAR ...'
        
            def downloadWAR = """{
            "files": [{
                "pattern": "release-repo/javaee/SpringMVCDemo/${BN}/*.war",
                "target": "war/"
            }]
        }"""
        
            def server = Artifactory.server('artifactory')
            server.download(downloadWAR)
        }
	
	stage ("Deploy-WAR") {
            echo 'Rename the WAR as SpringMVCDemo ...'
            sh "echo jenkins | sudo -S mv ${WORKSPACE}/war/javaee/SpringMVCDemo/${BN}/SpringMVCDemo-${BN}.war ${WORKSPACE}/war/javaee/SpringMVCDemo/${BN}/SpringMVCDemo.war"
			
            echo 'Deploy the application WAR in Payara server ...'
            sh "echo jenkins | sudo -S cp -f ${WORKSPACE}/war/javaee/SpringMVCDemo/${BN}/SpringMVCDemo.war /opt/payara41/glassfish/domains/domain1/autodeploy"
        }
    }
},
DeployReleaseInGit:{
    node ("master") {
        stage('Push-To-Git'){
            echo 'Push new branch to release ...'
            
            sh 'git config --global user.email "provideasparam@foo.com"'
            sh 'git config --global user.name "SomeUser"'
            sh 'git rm -r --cached Jenkinsfile'
            sh 'git add .'
            sh 'git commit -a -m "Release candidate"'
            retry(5) {
                withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: 'GitHubUP', usernameVariable: 'GIT_USERNAME', passwordVariable: 'GIT_PASSWORD']]){                
                    sh "git push https://${GIT_USERNAME}:${GIT_PASSWORD}@github.com/AnghelLeonard/SpringMVCDemo.git release_${BN}"
                }
            }
        }  
    }
},
failFast: false
