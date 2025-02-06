def call(body) {
    /********* evaluate the body block && collect configuration into the object ***********/
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    
    
    /*************** import utilities from src ********************************************/
    
    def Docker = new jenkins.CI.BuildTools()

    /********************** Begining declarative Pipeline **********************************/   
    pipeline {
        agent {kubernetes{cloud 'Ambohitsirohitra'}}
        stages {
            stage ('Build image and push to registry'){
                agent {label 'docker'}
                steps{
                    container('kaniko') { 
                       script {
                        Docker.build(pipelineParams.BUILD_CONTEXT, pipelineParams.DOCKERFILE, pipelineParams.REGISTRY_URL, pipelineParams.IMAGE_TAG)
                       }    
                    }                
                }
             }
        }
    }
    /****************************************************************************************/
}