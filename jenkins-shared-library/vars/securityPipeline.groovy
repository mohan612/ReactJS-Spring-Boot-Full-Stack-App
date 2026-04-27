def call(Map config = [:]) {
    def pipelineConfig = [
        projectName: config.projectName ?: 'unknown',
        dockerImageName: config.dockerImageName ?: 'app',
        buildContext: config.buildContext ?: '.',
        sonarProjectKey: config.sonarProjectKey ?: config.projectName,
        nexusIqAppId: config.nexusIqAppId ?: config.projectName,
        deployEnvironment: config.deployEnvironment ?: 'staging'
    ]
    
    return {
        stage('PR Validation') {
            when { changeRequest() }
            steps {
                script {
                    validatePR()
                }
            }
        }
        
        stage('Secret Scanning') {
            steps {
                script {
                    runTruffleHog()
                }
            }
        }
        
        stage('Code Quality - SonarQube') {
            steps {
                script {
                    runSonarQube(pipelineConfig.sonarProjectKey)
                }
            }
            post {
                always {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error("Pipeline aborted due to quality gate failure: ${qg.status}")
                        }
                    }
                }
            }
        }
        
        stage('Dependency Scan - Nexus IQ') {
            steps {
                script {
                    runNexusIQ(pipelineConfig.nexusIqAppId)
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    buildAndPushDockerImage(pipelineConfig.dockerImageName, pipelineConfig.buildContext)
                }
            }
        }
        
        stage('Image Scan - Trivy') {
            steps {
                script {
                    runTrivyScan(pipelineConfig.dockerImageName)
                }
            }
        }
        
        stage('Deploy to Staging') {
            when { branch 'develop' }
            steps {
                script {
                    deployToKubernetes(pipelineConfig.dockerImageName, pipelineConfig.deployEnvironment)
                }
            }
        }
    }
}

def validatePR() {
    script {
        if (!env.CHANGE_TITLE =~ /^(feat|fix|docs|style|refactor|test|chore)(\\(.+\\))?: .+/) {
            error("PR title must follow conventional commit format: type(scope): description")
        }
        
        def requiredSections = ['## Type of Change', '## Testing', '## Checklist']
        if (!requiredSections.every { env.CHANGE_DESCRIPTION.contains(it) }) {
            error("PR description must include Type of Change, Testing, and Checklist sections")
        }
    }
}

def runTruffleHog() {
    script {
        sh '''
            trufflehog filesystem . \
                --exclude_paths=.gitignore \
                --json \
                --output=trufflehog-results.json || true
        '''
        
        def trufflehogResults = readJSON file: 'trufflehog-results.json'
        if (trufflehogResults.size() > 0) {
            error("Secrets detected in repository! Please review and remove them.")
        }
    }
}

def runSonarQube(String projectKey) {
    script {
        withSonarQubeEnv('SonarQube') {
            sh "sonar-scanner -Dsonar.projectKey=${projectKey} || true"
        }
    }
}

def runNexusIQ(String appId) {
    script {
        sh '''
            curl -u ${NEXUS_IQ_USR}:${NEXUS_IQ_PSW} \
                 -F "file=@package-lock.json" \
                 -F "file=@pom.xml" \
                 -F "applicationId=${appId}-${BUILD_NUMBER}" \
                 -F "stage=build" \
                 https://nexus-iq.example.com/api/v2/scan/applications
        '''
    }
}

def buildAndPushDockerImage(String imageName, String buildContext) {
    script {
        def tag = "${env.BUILD_NUMBER}"
        def image = docker.build("${imageName}:${tag}", buildContext)
        image.push()
        image.push('latest')
    }
}

def runTrivyScan(String imageName) {
    script {
        def tag = "${env.BUILD_NUMBER}"
        sh '''
            trivy image --format json --output trivy-report.json ${imageName}:${tag}
            trivy image --exit-code 1 --severity HIGH,CRITICAL ${imageName}:${tag} || echo "Vulnerabilities found - review required"
        '''
        
        publishHTML([
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '.',
            reportFiles: 'trivy-report.html',
            reportName: 'Trivy Security Scan'
        ])
    }
}

def deployToKubernetes(String imageName, String environment) {
    script {
        def tag = "${env.BUILD_NUMBER}"
        def deploymentName = imageName.replace('-', '')
        
        sh """
            kubectl set image deployment/${deploymentName} ${deploymentName}=${imageName}:${tag} -n ${environment}
            kubectl rollout status deployment/${deploymentName} -n ${environment}
        """
    }
}
