pipeline {
    agent { label 'built-in' }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
                echo "Building branch: ${env.BRANCH_NAME}"
            }
        }

        stage('Build (Develop only)') {

            steps {
                echo "Building calculator-engine module..."
                sh '''
                    cd calculator-engine/maven-calculator-engine
                    mvn clean install
                '''

                echo "Building main calculator module..."
                sh '''
                    cd main1
                    mvn clean install
        }
    }

    post {
        success {
            echo "Build completed successfully for develop branch"
        }
        failure {
            echo "Build failed for develop branch"
        }
    }
}
