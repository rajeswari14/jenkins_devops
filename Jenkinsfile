pipeline {
  agent any
  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }
  }
  post {
    always { echo "Finished pipeline for ${env.BRANCH_NAME}" }
  }
}
