package jenkins.docker

def call(String TOKEN, String REGISTRY, String USERNAME){   
    sh """
     sudo docker login -u ${USERNAME} -p ${TOKEN} ${REGISTRY}
    """
}