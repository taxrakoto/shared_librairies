package jenkins.docker

def call(String REGISTRY){   
    sh """
     sudo docker logout ${REGISTRY}
    """
}