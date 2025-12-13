pipeline {
    agent any // Ensure the agent has Maven, Java, and Trivy installed

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch being built: ${env.BRANCH_NAME}"
            }
        }

        stage('Build') {
            steps {
                echo "Building Maven project..."
                sh '''
                    echo "==== Workspace ===="
                    pwd
                    ls -la

                    echo "==== Running Maven Build ===="
                    mvn clean install
                '''
            }
        }

        stage('Test') {
            steps {
                echo "Running Maven tests..."
                sh 'mvn test'
            }
        }

        stage('Security Scan') {
    steps {
        echo 'Running Trivy security scan...'
        sh '''
        trivy fs --severity HIGH,CRITICAL --exit-code 1 --format json -o trivy-report.json .
        '''
    }
    post {
        failure {
            echo 'Trivy detected vulnerabilities! Build failed.'
        }
        success {
            echo 'No critical vulnerabilities found.'
        }
    }
}

    }

    post {
        success {
            echo "Build SUCCESS for branch: ${env.BRANCH_NAME}"
        }
        failure {
            echo "Build FAILED for branch: ${env.BRANCH_NAME}"
        }
    }
}
