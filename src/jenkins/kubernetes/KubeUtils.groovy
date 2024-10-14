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

/********************* build and push but without build number ****************************/
def buildnoTag(String IMAGE, String DOCKERFILE){   
      
      script {
        sh """
            sudo docker build -f ${DOCKERFILE} -t ${IMAGE} .
            sudo docker push ${IMAGE}
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



