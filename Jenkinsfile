pipeline {
    agent { label 'built-in' }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Branch being built: ${env.BRANCH_NAME}"
            }
        }

        stage('Build') {
            steps {
                sh '''
                    echo "==== Workspace ===="
                    pwd
                    ls -la

                    echo "==== Running Maven build from root ===="
                    mvn clean install
                '''
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
