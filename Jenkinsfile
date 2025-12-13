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
        stage('Package') {
    steps {
        echo 'Packaging Maven project...'
        sh '''
            mvn package
            ls -la target
        '''
        // Archive the JAR/WAR artifact in Jenkins
        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
    }
}
        stage('Deploy to Application Server') {
    when {
        branch 'master'
    }
    steps {
        echo "Deploying artifact to Application Server..."
        sh '''
        scp target/maven-calculator-1.0-SNAPSHOT.jar ubuntu@44.200.37.160:/home/ubuntu/
        ssh ubuntu@44.200.37.160 "pkill -f 'maven-calculator-1.0-SNAPSHOT.jar'; nohup java -jar /home/ubuntu/maven-calculator-1.0-SNAPSHOT.jar > app.log 2>&1 &"
        '''
    }
}

   
        stage('Post-Deployment Verification') {
            steps {
                sh 'curl -f http://44.200.37.160:8080/health || exit 1'
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
