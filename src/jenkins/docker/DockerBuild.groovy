package jenkins.docker

def call(String IMAGE, String BUILD_IMAGE = ""){   
    
    if (BUILD_IMAGE == "") {
      script {
        sh """
            sudo docker build --cache-from ${IMAGE} -t ${IMAGE} .
        """
      }
    } else {
      script {
        sh """
            sudo docker build --target build-stage --cache-from ${BUILD_IMAGE} -t ${BUILD_IMAGE} .
            sudo docker build --cache-from ${BUILD_IMAGE} --cache-from ${IMAGE} -t ${IMAGE} .
        """
      }
    }
}