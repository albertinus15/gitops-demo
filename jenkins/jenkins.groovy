import java.text.SimpleDateFormat

def getCommitHashAndDateTime() {
    def commitHash = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    def date = new Date()
    def formatter = new SimpleDateFormat("yyyyMMdd")
    def formattedDate = formatter.format(date)
    return "${commitHash}${formattedDate}"
}

pipeline {
    agent any
    
    environment {
        GIT_REPO = 'git@github.com:albertinus15/gitops-demo.git'
        
        REGISTRY_URL = 'your-ip-nexus:8082/repository/repo'  
        IMAGE_NAME = 'gitops-demo'
        
        PORTAINER_WEBHOOK = credentials('portainer_webhook')
    }
    
    stages {
        stage('Checkout Repository') {
            steps {
                echo '===> CLONE REPOSITORY <==='
                cleanWs()                 
                sh "git clone ${GIT_REPO} ."
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    def imageTag = getCommitHashAndDateTime()
                    
                    env.IMAGE_TAG = imageTag
                    
                    dir('app') {
                        echo "Building Docker image dengan tag: ${imageTag}"
                        sh "docker build -t ${REGISTRY_URL}/${IMAGE_NAME}:${imageTag} ."
                        
                        sh "docker tag ${REGISTRY_URL}/${IMAGE_NAME}:${imageTag} ${REGISTRY_URL}/${IMAGE_NAME}:${imageTag}"
                    }
                }
            }
        }
        
        stage('Push to Docker Registry') {
            steps {
                script {
                    withDockerRegistry(credentialsId: 'nexus', url: 'http://your-ip-nexus:8082/repository/repo/') {
                        sh "docker push ${REGISTRY_URL}/${IMAGE_NAME}:${env.IMAGE_TAG}"
                    }
                }
            }
            post {
                success {
                    echo "Docker image pushed successfully to repository"
                }
            }
        }

        stage('Update Config') {
            steps {
                dir('config') {
                    echo '===> EDIT CONFIG <==='
                    sh """
                        sed -i 's|image: ${REGISTRY_URL}/${IMAGE_NAME}:.*|image: ${REGISTRY_URL}/${IMAGE_NAME}:${env.IMAGE_TAG}|g' docker-compose.yml
                        sed -i 's|APP_VERSION=.*|APP_VERSION=${env.IMAGE_TAG}|g' docker-compose.yml
                    """
                }
                
                sh """
                    git config user.email 'your-email@gmail.com'
                    git config user.name 'your-username'
                    git add config/docker-compose.yml
                    git commit -m 'Update image tag to ${env.IMAGE_TAG}'
                    git push origin master
                """
            }
        }
        
        stage('Trigger Portainer Webhook') {
            steps {
                echo '===> TRIGGER WEBHOOK PORTAINER <==='
                sh "curl -X POST ${PORTAINER_WEBHOOK}"
            }
        }
    }
    
    post {
        success {
            echo "Pipeline Succes."
        }
        failure {
            echo 'Pipeline Failed.'
        }
        always {
            sh "docker rmi ${REGISTRY_URL}/${IMAGE_NAME}:${env.IMAGE_TAG}"
        }
    }
}