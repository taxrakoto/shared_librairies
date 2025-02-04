package jenkins.CI

def build(String BUILD_CONTEXT, String DOCKERFILE, String REGISTRY_URL, String IMAGE_TAG){
    script {
        sh """
         /kaniko/executor --insecure --skip-tls-verify --cache=true \
         -f ${DOCKERFILE} \
         -c ${BUILD_CONTEXT} \
         --destination=${REGISTRY_URL}:${IMAGE_TAG}-v${BUILD_NUMBER}
        """
    }
}