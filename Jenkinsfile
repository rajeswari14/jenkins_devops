pipeline {
    agent any

    stages {
        stage('Checkout SCM') {
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
                sh 'trivy fs --scanners vuln --severity HIGH,CRITICAL --exit-code 1 --format json -o trivy-report.json .'
            }
        }
    }

    post {
        success {
            echo "Build SUCCESS for ${env.BRANCH_NAME}"
        }
        failure {
            echo "Build FAILED for ${env.BRANCH_NAME}"
        }
    }
}
