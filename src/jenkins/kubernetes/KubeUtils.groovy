package jenkins.kubernetes


/********************* Build and push images ****************************************************/
def build(String IMAGE, String DOCKERFILE){   
      
      script {
        sh """
            VERSION=${IMAGE}-v${BUILD_NUMBER}
            sudo docker build -f ${DOCKERFILE} -t ${VERSION} .
            sudo docker push ${VERSION}
        """
      }
    
}

/****************** deploy images *************************************************************/
def rollout(String IMAGE, String DEPLOYMENT, String CONTAINER){
        
        script {
          sh """
            VERSION=${IMAGE}-v${BUILD_NUMBER}
            kubectl set image deployment/${DEPLOYMENT} ${CONTAINER}=${VERSION}
           """
        }
}



