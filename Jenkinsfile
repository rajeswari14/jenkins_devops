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
        sh '''
            echo "==== WORKSPACE ROOT ===="
            pwd
            ls -la

            echo "==== FIND DIRECTORIES ===="
            find . -maxdepth 3 -type d
        '''
               }
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
