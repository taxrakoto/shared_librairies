def call(body) {
    /********* evaluate the body block && collect configuration into the object ***********/
    def pipelineParams= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = pipelineParams
    body()
    
    /*************** import utilities from src ********************************************/
    def Docker = new jenkins.docker.DockerUtilsV2()
    def Kube = new jenkins.kubernetes.KubeUtils()

    
    /********************** Begining declarative Pipeline **********************************/
    pipeline {
    agent any   
          
   
    stages {
    /**********************  STAGING  ******************************************************/   
		stage('Build and push staging image') {
        	when  { branch pipelineParams.BRANCH_STAGING }
            agent {label pipelineParams.STAGING_SERVER}
            steps {
			  withCredentials([string(credentialsId: 'CI_BUILD_TOKEN', variable: 'CI_BUILD_TOKEN')]) {
                script {
                    Docker.connect("${CI_BUILD_TOKEN}",pipelineParams.CI_REGISTRY, pipelineParams.CI_BUILD_USERNAME)
                    Kube.build(pipelineParams.IMAGE_STAGING, pipelineParams.STAGING_DOCKERFILE)
                    Docker.Logout(pipelineParams.CI_REGISTRY)
                }   
              }
            }
        }
        
    /*********************   PRODUCTION *********************************************************/
        stage('Build and push production image') {
        	when  { branch pipelineParams.BRANCH_PROD }
            agent {label pipelineParams.PROD_SERVER}
            steps {
			  withCredentials([string(credentialsId: 'CI_BUILD_TOKEN', variable: 'CI_BUILD_TOKEN')]) {
                script {
                    Docker.connect("${CI_BUILD_TOKEN}",pipelineParams.CI_REGISTRY, pipelineParams.CI_BUILD_USERNAME)
                    Kube.build(pipelineParams.IMAGE_PROD, pipelineParams.PROD_DOCKERFILE)
                    Docker.Logout(pipelineParams.CI_REGISTRY)
                }   
              }
            }
        }
	    
    } /************************************ End of Pipeline Stages *************************************************/
   } /***************************************** End of Pipeline ****************************************************/
   
}
 
