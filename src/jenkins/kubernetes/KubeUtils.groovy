package jenkins.kubernetes


/********************* Build and push images ****************************************************/
def build(String IMAGE, String DOCKERFILE){   
      
      script {
        sh """
            sudo docker build -f ${DOCKERFILE} -t ${IMAGE}-v${BUILD_NUMBER} .
            sudo docker push ${IMAGE}-v${BUILD_NUMBER}
        """
      }
    
}

/****************** deploy images *************************************************************/
def rollout(String IMAGE, String DEPLOYMENT, String CONTAINER, String NAMESPACE){
        
        script {
          sh """
            kubectl set image deployment/${DEPLOYMENT} ${CONTAINER}=${IMAGE}-v${BUILD_NUMBER} -n ${NAMESPACE}
           """
        }
}



