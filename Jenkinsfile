pipeline {
    agent any
    options {
        timestamps()
        ansiColor('xterm')
        retry(1)
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
                echo 'Building the project...'
                sh 'mvn clean install'
            }
        }

        stage('Test') {
            steps {
                echo 'Running tests...'
                sh 'mvn test'
            }
        }

        stage('Security Scan') {
            steps {
                echo 'Running Trivy security scan...'
                script {
                    sh '''
                    trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 --format json -o trivy-report.json .
                    '''
                    echo 'Trivy scan finished'
                }
            }
        }

        stage('Package') {
            steps {
                echo 'Packaging Maven project...'
                sh 'mvn package'
                sh 'ls -la target'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy to Application Server') {
            steps {
                sshagent(['ubuntu']) {
                    script {
                        echo 'Copying artifact to server...'
                        sh '''
                        scp -o StrictHostKeyChecking=no target/maven-calculator-1.0-SNAPSHOT.jar ubuntu@44.200.37.160:/home/ubuntu/
                        '''
                        
                        echo 'Stopping old application (if any) and starting new one...'
                        sh '''
                        ssh -o StrictHostKeyChecking=no ubuntu@44.200.37.160 "
                        pkill -f 'maven-calculator-1.0-SNAPSHOT.jar' || echo 'No old app running';
                        nohup java -jar /home/ubuntu/maven-calculator-1.0-SNAPSHOT.jar > /home/ubuntu/app.log 2>&1 &
                        "
                        '''
                    }
                }
            }
        }

        stage('Post-Deployment Verification') {
            steps {
                script {
                    echo 'Verifying deployment...'
                    retry(12) {
                        def status = sh(script: "curl -s -o /dev/null -w %{http_code} http://44.200.37.160:8080/health || echo 000", returnStdout: true).trim()
                        echo "App status: ${status}"
                        if (status != '200') {
                            echo "App not ready yet, retrying in 5s..."
                            sleep 5
                            error "Deployment not ready"
                        } else {
                            echo "Application is up and running!"
                        }
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
            echo 'Build SUCCESS for branch: ${env.BRANCH_NAME}'
        }
        failure {
            echo 'Build FAILED for branch: ${env.BRANCH_NAME}'
        }
    }
}
