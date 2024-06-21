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
            agent {label pipelineParams.LABEL_STAGING}
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
        
		
		
	    stage('Deploy Staging App') {
            when  { branch pipelineParams.BRANCH_STAGING}
            agent {label 'kubernetes'}
            steps {
                script {
                    Kube.rollout(pipelineParams.IMAGE_STAGING, pipelineParams.DEPLOYMENT_STAGING, pipelineParams.CONTAINER_STAGING, pipelineParams.STAGING_NAMESPACE)
                }
            }
        }
    /*********************   PRODUCTION *********************************************************/
        stage('Build and push production image') {
        	when  { branch pipelineParams.BRANCH_PROD }
            agent {label pipelineParams.LABEL_PROD}
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
        
		
		
	    stage('Deploy production App') {
            when  { branch pipelineParams.BRANCH_PROD}
            agent {label 'kubernetes'}
            steps {
                script {
                    Kube.rollout(pipelineParams.IMAGE_PROD, pipelineParams.DEPLOYMENT_PROD, pipelineParams.CONTAINER_PROD, pipelineParams.PROD_NAMESPACE)
                }
            }
        }
    } /************************************ End of Pipeline Stages *************************************************/
   } /***************************************** End of Pipeline ****************************************************/
   
}
 
