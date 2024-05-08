package jenkins.docker

/********************* Build images ****************************************************/
def build(String IMAGE, String BUILD_IMAGE, String DOCKERFILE){   
    
    if (BUILD_IMAGE == "") {
      script {
        sh """
            sudo docker build -f ${DOCKERFILE} --cache-from ${IMAGE} -t ${IMAGE} .
        """
      }
    } else {
      script {
        sh """
            sudo docker build -f ${DOCKERFILE} --target build-stage --cache-from ${BUILD_IMAGE} -t ${BUILD_IMAGE} .
            sudo docker build -f ${DOCKERFILE} --cache-from ${BUILD_IMAGE} --cache-from ${IMAGE} -t ${IMAGE} .
        """
      }
    }
}

/********************** launch Compose ****************************************************/
def composeUp(String COMPOSE){   
    if (COMPOSE == "") {
      script {
        sh """
            echo " skipping deploy: no compose files provided"
        """
      }
    } else {
      script {
        sh """
            sudo docker compose -f ${COMPOSE} pull && sudo docker compose -f ${COMPOSE} up -d
        """
      }
    }
}
/********************************************************************************************/
def pull(String IMAGE, String BUILD_IMAGE){   
    
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
/**********************************************************************************************/
def push(String IMAGE, String BUILD_IMAGE){   
    
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
/*************************************************************************************************/
def connect(String TOKEN, String REGISTRY, String USERNAME){   
    sh """
     sudo docker login -u ${USERNAME} -p ${TOKEN} ${REGISTRY}
    """
}
/*************************************************************************************************/
def Logout(String REGISTRY){   
    sh """
     sudo docker logout ${REGISTRY}
    """
}
/**************************************************************************************************/