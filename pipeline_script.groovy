node{
    stage('GIT_CHECKOUT'){
        git 'https://github.com/sapnarsy2612/KUBERNETES_DEPLOYMENT_USING_JENKINS.git'
    }
    stage('SENDING_GIT_FILES_TO_ANSIBLE'){
        sshagent(['ANSIBLE']) {
            sh 'ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209'
            sh 'scp /var/lib/jenkins/workspace/KUBERNETES_DEPLOYMENT_PIPELINE/* ubuntu@18.204.10.209:/home/ubuntu'
}
    }
    stage('BUILD_DOCKER_IMAGE'){
        sshagent(['ANSIBLE']) {
            sh 'ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 cd /home/ubuntu'
            sh '''
            JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
            ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 docker image build -t $JOB_SMALL:v1.$BUILD_ID .
            '''
        }
    }
    stage('TAG_IMAGE'){
        sshagent(['ANSIBLE']) {
            sh 'ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 cd /home/ubuntu'
            sh '''
            JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
            ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 docker image tag $JOB_SMALL:v1.$BUILD_ID sravtar/$JOB_SMALL:v1.$BUILD_ID
            ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 docker image tag $JOB_SMALL:v1.$BUILD_ID sravtar/$JOB_SMALL:v1.latest
            '''
}
    }
    stage('PUSH_DOCKER_IMAGE_TO_DOCKERHUB'){
        sshagent(['ANSIBLE']) {
        withCredentials([string(credentialsId: 'DOCKERHUB', variable: 'DOCKERHUB')]) {
            sh '''
            ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 docker login -u sravtar -p Sapna@yadav2612
            JOB_SMALL=$(echo "$JOB_NAME" | tr '[:upper:]' '[:lower:]')
            ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 docker image push sravtar/$JOB_SMALL:v1.$BUILD_ID
            ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 docker image push sravtar/$JOB_SMALL:v1.latest
            ssh -T -o StrictHostKeyChecking=no ubuntu@18.204.10.209 docker image rm sravtar/$JOB_SMALL:v1.$BUILD_ID sravtar/$JOB_SMALL:v1.latest $JOB_SMALL:v1.$BUILD_ID
            '''
}
    }
}
    stage('TRANSFER_FILES_TO_KUBERNETES_SERVER'){
        sshagent(['KUBERNETES']) {
            sh 'ssh -T -o StrictHostKeyChecking=no ubuntu@54.242.193.200'
            sh 'scp /var/lib/jenkins/workspace/KUBERNETES_DEPLOYMENT_PIPELINE/* ubuntu@54.242.193.200:/home/ubuntu'
            sh 'ssh -T -o StrictHostKeyChecking=no ubuntu@54.242.193.200 kubectl delete -f /home/ubuntu/Deployment.yml'
            sh 'ssh -T -o StrictHostKeyChecking=no ubuntu@54.242.193.200 kubectl delete -f /home/ubuntu/Service.yml'
            sh 'ssh -T -o StrictHostKeyChecking=no ubuntu@54.242.193.200 kubectl apply -f /home/ubuntu/Deployment.yml'
            sh 'ssh -T -o StrictHostKeyChecking=no ubuntu@54.242.193.200 kubectl apply -f /home/ubuntu/Service.yml'
        }
    }
}
