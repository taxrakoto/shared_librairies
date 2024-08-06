package jenkins.ocrvs


/************************************************************************************************************/
def fetchEnv(String ENV_ID) {
    def envMap = [:]
    withCredentials([file(credentialsId: ENV_ID, variable: 'ENV_SECRET_FILE')]) {
        // read and write the secret file into a map
        sh 'touch .properties && cat ${ENV_SECRET_FILE} > .properties'
        script {
            def props = readProperties file :'.properties'
            props.each { key, value -> envMap[key] = value }
        }
        sh 'echo "cleaning up" && rm .properties'
    }
    return envMap
}

/****************************************************************************************************/
/*** Fetch secret defined in the environment (or server) and ecrypt it using encryption key **/
def get_secret(String ENV, String SECRET_NAME, String KEY_NAME){
    
    // make sure that a secret file with the ENV as its name exists in jenkins
    // and has all the  variables and secrets in its content
    
        def propertiesMap = fetchEnv(ENV)
        def secret = propertiesMap[SECRET_NAME]

        // check if the secret is empty
        if (!propertiesMap[SECRET_NAME]) {
            error("Secret is empty. Make sure you have added the secret to the Jenkins credentials.")
        }
        // encrypt the secret using Openssl
        def encryptedSecretFile = "${workspace}/encrypted_key.bin"
        sh(script: "echo -n ${propertiesMap[SECRET_NAME]} | openssl enc -aes-256-cbc -pbkdf2 -salt -k ${propertiesMap[KEY_NAME]} -out ${encryptedSecretFile}", returnStdout: false)
        
        // Encode the encrypted secret in Base64
        def encodedEncryptedSecret = sh(script: "base64 < ${encryptedSecretFile}", returnStdout: true).trim()
     
        // return the result
        return encodedEncryptedSecret
        
}
/****************************************************************************************************/
