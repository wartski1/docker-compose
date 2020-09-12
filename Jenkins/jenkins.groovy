pipeline {
    environment {        
        GIT_REPO_URL="https://github.com/wartski1/${proyecto}.git"
        DOCKERHUB_REPO_URL="https://github.com/wartski1/docker-compose.git"        
        DEPLOY_FOLDER="deploy/Kubernetes/${proyecto}/"
        DEPLOY_FILE="${env.DEPLOY_FOLDER}deployment.yaml"
        DOCKER_IMAGE="wartski/${proyecto}"
        TAG="${version}"
    }

    agent any
    stages {
        stage("Checkout app-code") {
            steps {
               dir('app') {
                    git url:"${env.GIT_REPO_URL}" , branch: "${rama}"
                } 
            }
        }
         
        stage("Checkout deploy-code") {
            steps {
               dir('deploy') {
                    git url:"${env.DOCKERHUB_REPO_URL}" , branch: "master"
                } 
            }
        }
        
        stage("Build image") {
            steps {
                dir('app') {
                    script {
                        dockerImage = docker.build("${env.DOCKER_IMAGE}:v1.0")
                    }
                }
            }
        }
        stage("Push image") {
            steps {
                script {
                    docker.withRegistry('', 'pedrodocker') {
                    dockerImage.push()
                    }
                }
            }
        }

        stage('Deploy') {
            steps{
                sh "sed -i 's:DOCKER_IMAGE:${env.DOCKER_IMAGE}:g' ${env.DEPLOY_FILE}"
                sh "sed -i 's:TAG:v1.0:g' ${env.DEPLOY_FILE}"
                step([$class: 'KubernetesEngineBuilder', 
                        projectId: "nice-root-288300",
                        clusterName: "cluster-pedro",
                        zone: "us-central1-b",
                        manifestPattern: "${env.DEPLOY_FOLDER}",
                        credentialsId: "seminario",
                        verifyDeployments: true])
            }
        }
    }
}