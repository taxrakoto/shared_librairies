package jenkins.docker

def call(String IMAGE, String BUILD_IMAGE = ""){   
    
    if (BUILD_IMAGE == "") {
      script {
        sh """
            sudo docker push ${IMAGE}
        """
      }
    } else {
      script {
        sh """
           sudo docker push ${BUILD_IMAGE} 
           sudo docker push ${IMAGE}
        """
      }
    }
}