package jenkins.docker

def call(String IMAGE, String BUILD_IMAGE = ""){   
    
    if (BUILD_IMAGE == "") {
      script {
        sh """
            sudo docker pull ${IMAGE} || true
        """
      }
    } else {
      script {
        sh """
            sudo docker pull ${BUILD_IMAGE} || true
            sudo docker pull ${IMAGE} || true
        """
      }
    }
}

