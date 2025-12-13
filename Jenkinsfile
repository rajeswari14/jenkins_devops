pipeline {
    agent any

    environment {
        APP_NAME    = "calculator-app"
        DEPLOY_USER = "ubuntu"
        DEPLOY_HOST = "44.200.37.160"
        JAVA_HOME   = "/usr/lib/jvm/java-21-openjdk-amd64"
        PATH        = "${JAVA_HOME}/bin:${env.PATH}"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Package') {
            steps {
                echo "Building multi-module Maven project..."
                sh 'mvn clean install -DskipTests'  // build both engine + app
                sh 'ls -lh calculator-app/target/'   // confirm JAR exists
            }
        }

        stage('Security Scan (Trivy)') {
            steps {
                sh '''
                    trivy fs --exit-code 0 --format json \
                    --output trivy-report.json calculator-app/target/
                '''
            }
            post {
                always {
                    archiveArtifacts artifacts: 'trivy-report.json', fingerprint: true
                }
            }
        }

        stage('Deploy to Server') {
            steps {
                sshagent(['app-server']) {
                    echo "Deploying artifact..."
                    sh 'ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_HOST} "mkdir -p /home/ubuntu/${APP_NAME}"'
                    sh 'scp -o StrictHostKeyChecking=no calculator-app/target/calculator-app-1.0-SNAPSHOT.jar ${DEPLOY_USER}@${DEPLOY_HOST}:/home/ubuntu/${APP_NAME}/app.jar'
                    sh '''
                        ssh -o StrictHostKeyChecking=no ${DEPLOY_USER}@${DEPLOY_HOST} '
                        pkill -f app.jar || true
                        nohup java -jar /home/ubuntu/${APP_NAME}/app.jar > /home/ubuntu/${APP_NAME}/app.log 2>&1 &
                        '
                    '''
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
                        status = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://${DEPLOY_HOST}:8080/health || true", returnStdout: true).trim()
                        if (status == '200') {
                            echo "Application is running! Status: ${status}"
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
