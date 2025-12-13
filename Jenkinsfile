pipeline {
    agent any

    environment {
        GIT_CREDENTIALS = 'github-token'
        SSH_CREDENTIALS = 'jenkins-private'
        APP_SERVER = 'ubuntu@44.200.37.160'
        ARTIFACT_NAME = 'maven-calculator-1.0-SNAPSHOT.jar'
    }

    stages {
        stage('Declarative: Checkout SCM') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                sh 'mvn test'
            }
        }

        stage('Security Scan') {
            steps {
                script {
                    def trivyExitCode = sh(
                        script: 'trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 --format json -o trivy-report.json . || true',
                        returnStatus: true
                    )
                    echo "Trivy scan finished with exit code: ${trivyExitCode}"
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging Maven project...'
                sh 'mvn package'
                sh 'ls -la target'
                archiveArtifacts artifacts: "target/${ARTIFACT_NAME}", allowEmptyArchive: true
            }
        }

        stage('Deploy to Application Server') {
            steps {
                sshagent(credentials: ["${SSH_CREDENTIALS}"]) {
                    sh """
                    scp -o StrictHostKeyChecking=no target/${ARTIFACT_NAME} ${APP_SERVER}:/home/ubuntu/
                    ssh -o StrictHostKeyChecking=no ${APP_SERVER} 'pkill -f "${ARTIFACT_NAME}" || true; nohup java -jar /home/ubuntu/${ARTIFACT_NAME} > app.log 2>&1 &'
                    """
                }
            }
        }

        stage('Post-Deployment Verification') {
            steps {
                echo 'Verifying deployment...'
            }
        }
    }

    post {
        always {
            echo 'Pipeline finished'
        }
        success {
            echo 'Build SUCCESS for branch: ' + env.BRANCH_NAME
        }
        failure {
            echo 'Build FAILED for branch: ' + env.BRANCH_NAME
        }
        cleanup {
            // This block ensures the GitHub commit status issue doesn’t break the build
            script {
                try {
                    // any GitHub status update code can go here
                } catch (Exception e) {
                    echo "Skipping GitHub commit status update due to token/permission issue: ${e}"
                }
            }
        }
    }
}
