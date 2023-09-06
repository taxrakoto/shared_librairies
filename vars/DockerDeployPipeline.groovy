import jenkins.docker.DockerBuild
import jenkins.docker.DockerComposeUp
import jenkins.docker.DockerPull
import jenkins.docker.DockerPush
import jenkins.docker.DockerRegistryConnect
import jenkins.docker.DockerRegistryLogout

def call(body) {
    /* evaluate the body block, and collect configuration into the object */
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()

    
    /******** Begining declarative Pipeline **********************/
    pipeline {
    agent {label pipelineParams.LABEL}    
           

    /******   Variables
        LABEL
        BRANCH
        CI_REGISTRY='registry.gitlab.com'
        CI_REGISTRY_IMAGE = 'registry.gitlab.com/ugd-mg/equivalence_dipl-me/equivalence_api:staging'      
        CI_REGISTRY_BUILD_IMAGE = 'registry.gitlab.com/ugd-mg/equivalence_dipl-me/equivalence_api:build-staging'    
	    CI_BUILD_USERNAME = 'token-registry'
    
    ********/

    stages {
        
		stage('Build image') {
        	when  { branch pipelineParams.BRANCH }
            steps {
			  withCredentials([string(credentialsId: 'CI_BUILD_TOKEN', variable: 'CI_BUILD_TOKEN')]) {
                script {
                    DockerRegistryConnect("${CI_BUILD_TOKEN}",pipelineParams.CI_REGISTRY, pipelineParams.CI_BUILD_USERNAME)
                    DockerPull(pipelineParams.CI_REGISTRY_IMAGE,pipelineParams.CI_REGISTRY_BUILD_IMAGE)
                    DockerBuild(pipelineParams.CI_REGISTRY_IMAGE,pipelineParams.CI_REGISTRY_BUILD_IMAGE)
                }   
              }
            }
        }
        
		stage('Push image') {  
	        when  { branch pipelineParams.BRANCH}
            steps {
                script { DockerPush(pipelineParams.CI_REGISTRY_IMAGE,pipelineParams.CI_REGISTRY_BUILD_IMAGE) }
            }
        }
		
	    stage('Deploy App') {
            when  { branch pipelineParams.BRANCH}
            steps {
                script {
                    DockerComposeUp()
                    DockerRegistryLogout(pipelineParams.CI_REGISTRY)
                }
            }
        }
    }
   }
}

