def call(body) {
    /********* evaluate the body block && collect configuration into the object ***********/
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    
    /*************** import utilities from src ********************************************/
    def Docker = new jenkins.docker.DockerUtils()


    
    /********************** Begining declarative Pipeline **********************************/
    pipeline {
    agent {label pipelineParams.LABEL}    
          
   
    stages {
        
		stage('Build image') {
        	when  { branch pipelineParams.BRANCH }
            steps {
			  withCredentials([string(credentialsId: 'CI_BUILD_TOKEN', variable: 'CI_BUILD_TOKEN')]) {
                script {
                    Docker.connect("${CI_BUILD_TOKEN}",pipelineParams.CI_REGISTRY, pipelineParams.CI_BUILD_USERNAME)
                    Docker.pull(pipelineParams.CI_REGISTRY_IMAGE,pipelineParams.CI_REGISTRY_BUILD_IMAGE)
                    Docker.build(pipelineParams.CI_REGISTRY_IMAGE,pipelineParams.CI_REGISTRY_BUILD_IMAGE)
                }   
              }
            }
        }
        
		stage('Push image') {  
	        when  { branch pipelineParams.BRANCH}
            steps {
                script { Docker.push(pipelineParams.CI_REGISTRY_IMAGE,pipelineParams.CI_REGISTRY_BUILD_IMAGE) }
            }
        }
		
	    stage('Deploy App') {
            when  { branch pipelineParams.BRANCH}
            steps {
                script {
                    Docker.composeUp()
                    Docker.Logout(pipelineParams.CI_REGISTRY)
                }
            }
        }
    }
   }
}
 /************************************ end of Pipeline **************************************/
