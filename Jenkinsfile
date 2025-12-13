pipeline {
    agent any

    options {
        timestamps()  // Add timestamps in console output
        retry(1)      // Retry once if pipeline fails
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
                echo "Running Trivy security scan..."
                sh 'trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 --format json -o trivy-report.json . || true'
            }
        }

        stage('Package') {
            steps {
                echo "Packaging Maven project..."
                sh 'mvn package'
                sh 'ls -la target'
                archiveArtifacts artifacts: 'target/maven-calculator-1.0-SNAPSHOT.jar', fingerprint: true
            }
        }

        stage('Deploy to Application Server') {
            steps {
                sshagent(['ubuntu']) {
                    echo "Deploying artifact to server..."

                    // Copy artifact
                    sh 'scp -o StrictHostKeyChecking=no target/maven-calculator-1.0-SNAPSHOT.jar ubuntu@44.200.37.160:/home/ubuntu/'

                    echo "Stopping old application and starting new one..."

                    // Run commands safely via SSH
                    sh """
                    ssh -o StrictHostKeyChecking=no ubuntu@44.200.37.160 "
                        cd /home/ubuntu;
                        pkill -f maven-calculator-1.0-SNAPSHOT.jar || true
                        nohup java -jar maven-calculator-1.0-SNAPSHOT.jar > app.log 2>&1 &
                    "
                    """
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
