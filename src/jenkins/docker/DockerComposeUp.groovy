package jenkins.docker

def call(){   
    sh """
     sudo docker compose pull && sudo docker compose up -d
    """
}