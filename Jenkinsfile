pipeline {
    agent any

    environment {
        GIT_CREDENTIALS = 'github-token'
        SSH_CREDENTIALS = 'jenkins-private'
        APP_SERVER = 'ubuntu@44.200.37.160'
        ARTIFACT_NAME = 'maven-calculator-1.0-SNAPSHOT.jar'
        DEPLOY_PATH = '/home/ubuntu'
    }

    stages {
        stage('Checkout SCM') {
            steps {
                checkout scm
                echo "Branch: ${env.BRANCH_NAME}"
            }
        }

        stage('Build') {
            steps {
                echo "Building the project..."
                sh 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                echo "Running tests..."
                sh 'mvn test'
            }
        }

        stage('Security Scan') {
            steps {
                script {
                    echo "Running Trivy security scan..."
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
                sh "ls -la target"
                archiveArtifacts artifacts: "target/${ARTIFACT_NAME}", allowEmptyArchive: true
            }
        }

        stage('Deploy to Application Server') {
    steps {
        sshagent(credentials: ["${SSH_CREDENTIALS}"]) {
            script {
                echo "Copying artifact to server..."
                sh """
                scp -o StrictHostKeyChecking=no ${env.WORKSPACE}/target/${ARTIFACT_NAME} ${APP_SERVER}:${DEPLOY_PATH}/
                """

                echo "Stopping old application (if any) and starting new one..."
                sh """
                ssh -o StrictHostKeyChecking=no ${APP_SERVER} << 'ENDSSH'
                    set -e
                    if pgrep -f "${ARTIFACT_NAME}" > /dev/null; then
                        pkill -f "${ARTIFACT_NAME}"
                        echo "Old application stopped."
                    fi
                    nohup java -jar ${DEPLOY_PATH}/${ARTIFACT_NAME} > ${DEPLOY_PATH}/app.log 2>&1 &
                    echo "New application started."
ENDSSH
                """
            }
        }
    }
}

        stage('Post-Deployment Verification') {
    steps {
        script {
            echo 'Verifying deployment...'
            def retries = 10
            def success = false
            def url = "http://${APP_SERVER.replace('ubuntu@','')}:8080/health"
            for (int i = 1; i <= retries; i++) {
                def status = sh(
                    script: "curl -s -o /dev/null -w '%{http_code}' ${url} || true",
                    returnStdout: true
                ).trim()
                if (status == "200") {
                    echo "Application is up. Status=${status}"
                    success = true
                    break
                } else {
                    echo "Attempt ${i}: App not ready yet, status=${status}. Retrying in 5s..."
                    sleep 5
                }
            }
            if (!success) {
                error "Application verification failed after ${retries} attempts!"
            }
        }
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
    }
}
