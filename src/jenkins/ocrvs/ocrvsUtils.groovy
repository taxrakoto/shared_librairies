package jenkins.ocrvs

/****************************************************************************************************/
/*** Fetch secret defined in the environment (or server) and ecrypt it using encryption key **/
def get_secret(String ENV, String SECRET_NAME, String KEY_NAME){
    
    // make sure that a secret file with the ENV as its name exists in jenkins
    // and has all the  variables and secrets in its content
    
    // read  the secret file of the env (qa|staging|prod) and store the contents
    // in a local file
    def propertiesMap = [:]
    withCredentials([file(credentialsId: ENV, variable: 'ENV_SECRET_FILE')]) {
        // read the secret file into a map
        sh 'touch .properties && cat ${ENV_SECRET_FILE} > .properties'
        script {
            def props = readProperties file :'.properties'
            props.each { key, value -> propertiesMap[key] = value }
        }
        
        // DEBUGGING
        sh """
            echo "properties file" && cat .properties"
            echo "secret name is SECRET_NAME "
            echo "secret value is {propertiesMap[SECRET_NAME]} "
            echo "DEBUG secret  = {env.SECRET_NAME}"
            echo "DEBUG key = {env.KEY_NAME}"
        """
        // END OF DEBUGGING
        
        // check if the secret is empty
        if (!env.SECRET_NAME) {
            error("Secret is empty. Make sure you have added the secret to the Jenkins credentials.")
        }
        // encrypt the secret using Openssl
        def encryptedSecretFile = "${workspace}/encrypted_key.bin"
        sh """
           echo -n "${SECRET_NAME}" | openssl enc -aes-256-cbc -pbkdf2 -salt -k "${KEY_NAME}" -out ${encryptedSecretFile}
        """       
        // Encode the encrypted secret in Base64
        def encodedEncryptedSecret = sh(script: "base64 < ${encryptedSecretFile}", returnStdout: true).trim()
     
        // return the result
        return encodedEncryptedSecret
            
     }  
}
/****************************************************************************************************/