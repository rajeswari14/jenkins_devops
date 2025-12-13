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
                    # Run vulnerability scan only (optional: remove secret scan for faster results)
                    trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 --format json -o trivy-report.json .
                    
                    echo "Trivy report saved to trivy-report.json"
                '''
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
