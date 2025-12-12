pipeline {
    agent { label 'built-in' }

    stages {
        stage('Checkout') {
            steps {
                echo "Branch: ${env.BRANCH_NAME}"
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean install'
            }
        }
    }
}
