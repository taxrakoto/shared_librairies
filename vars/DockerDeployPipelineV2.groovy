def call(body) {
    /********* evaluate the body block && collect configuration into the object ***********/
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    
    /*************** import utilities from src ********************************************/
    def Docker = new jenkins.docker.DockerUtilsV2()


    
    /********************** Begining declarative Pipeline **********************************/
    pipeline {
    agent any   
          
   
    stages {
    /**********************  STAGING  ******************************************************/   
		stage('Build staging image') {
        	when  { branch pipelineParams.BRANCH_STAGING }
            agent {label pipelineParams.LABEL_STAGING}
            steps {
			  withCredentials([string(credentialsId: 'CI_BUILD_TOKEN', variable: 'CI_BUILD_TOKEN')]) {
                script {
                    Docker.connect("${CI_BUILD_TOKEN}",pipelineParams.CI_REGISTRY, pipelineParams.CI_BUILD_USERNAME)
                    Docker.pull(pipelineParams.IMAGE_STAGING,pipelineParams.BUILD_IMAGE_STAGING)
                    Docker.build(pipelineParams.IMAGE_STAGING,pipelineParams.BUILD_IMAGE_STAGING,pipelineParams.STAGING_DOCKERFILE)
                }   
              }
            }
        }
        
		stage('Push staging image') {  
	        when  { branch pipelineParams.BRANCH_STAGING}
            agent {label pipelineParams.LABEL_STAGING}
            steps {
                script { Docker.push(pipelineParams.IMAGE_STAGING,pipelineParams.BUILD_IMAGE_STAGING) }
            }
        }
		
	    stage('Deploy Staging App') {
            when  { branch pipelineParams.BRANCH_STAGING}
            agent {label pipelineParams.LABEL_STAGING}
            steps {
                script {
                    Docker.composeUp(pipelineParams.STAGING_COMPOSE)
                    Docker.Logout(pipelineParams.CI_REGISTRY)
                }
            }
        }
    /*********************   PRODUCTION *********************************************************/
        stage('Build Production image') {
        	when  { branch pipelineParams.BRANCH_PROD }
            agent {label pipelineParams.LABEL_PROD}
            steps {
			  withCredentials([string(credentialsId: 'CI_BUILD_TOKEN', variable: 'CI_BUILD_TOKEN')]) {
                script {
                    Docker.connect("${CI_BUILD_TOKEN}",pipelineParams.CI_REGISTRY, pipelineParams.CI_BUILD_USERNAME)
                    Docker.pull(pipelineParams.IMAGE_PROD,pipelineParams.BUILD_IMAGE_PROD)
                    Docker.build(pipelineParams.IMAGE_PROD,pipelineParams.BUILD_IMAGE_PROD,pipelineParams.PROD_DOCKERFILE)
                }   
              }
            }
        }
        
		stage('Push Production image') {  
	        when  { branch pipelineParams.BRANCH_PROD}
            agent {label pipelineParams.LABEL_PROD}
            steps {
                script { Docker.push(pipelineParams.IMAGE_PROD,pipelineParams.BUILD_IMAGE_PROD) }
            }
        }
		
	    stage('Deploy Production App') {
            when  { branch pipelineParams.BRANCH_PROD}
            agent {label pipelineParams.LABEL_PROD}
            steps {
                script {
                    Docker.composeUp(pipelineParams.PROD_COMPOSE)
                    Docker.Logout(pipelineParams.CI_REGISTRY)
                }
            }
        }
    } /************************************ End of Pipeline Stages *************************************************/
   } /***************************************** End of Pipeline ****************************************************/
   
}
 
