pipeline {
    agent any

    options {
        timestamps()  // Adds timestamps to console logs
        retry(1)      // Retries the pipeline once if it fails
    }

    stages {
        stage('Checkout SCM') {
            steps {
                echo "Checking out source code..."
                checkout scm
            }
        }

        stage('Build') {
            steps {
                echo "Building the project..."
                ansiColor('xterm') {
                    sh 'mvn clean install'
                }
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                ansiColor('xterm') {
                    sh 'mvn test'
                }
            }
        }

        stage('Security Scan') {
            steps {
                echo "Running Trivy security scan..."
                ansiColor('xterm') {
                    sh 'trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 --format json -o trivy-report.json . || true'
                }
            }
        }

        stage('Package') {
            steps {
                echo "Packaging Maven project..."
                ansiColor('xterm') {
                    sh 'mvn package'
                    sh 'ls -la target'
                }
                archiveArtifacts artifacts: 'target/maven-calculator-1.0-SNAPSHOT.jar', fingerprint: true
            }
        }

        stage('Deploy to Application Server') {
            steps {
                sshagent(['ubuntu']) {
                    echo "Copying artifact to server..."
                    sh 'scp -o StrictHostKeyChecking=no target/maven-calculator-1.0-SNAPSHOT.jar ubuntu@44.200.37.160:/home/ubuntu/'

                    echo "Stopping old application (if any) and starting new one..."
                    sh """
                        ssh -o StrictHostKeyChecking=no ubuntu@44.200.37.160 << 'EOF'
                        pkill -f maven-calculator-1.0-SNAPSHOT.jar || true
                        nohup java -jar /home/ubuntu/maven-calculator-1.0-SNAPSHOT.jar > /dev/null 2>&1 &
                        EOF
                    """
                }
            }
        }

        stage('Post-Deployment Verification') {
            steps {
                script {
                    def maxRetries = 10
                    def retryInterval = 5
                    def status = 0

                    for (int i = 1; i <= maxRetries; i++) {
                        status = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://44.200.37.160:8080/health || true", returnStdout: true).trim()
                        if (status == '200') {
                            echo "Application is up and running! Status: ${status}"
                            break
                        } else {
                            echo "Attempt ${i}: App not ready yet, status=${status}. Retrying in ${retryInterval}s..."
                            sleep(retryInterval)
                        }
                    }

                    if (status != '200') {
                        error "Application verification failed after ${maxRetries} attempts!"
                    }
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline finished successfully"
        }
        failure {
            echo "Build FAILED for branch: ${env.BRANCH_NAME}"
        }
    }
}
