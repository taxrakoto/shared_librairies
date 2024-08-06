///////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////////

def fetchAndEncryptSecret(secretName, envName, encryptionKey) {
    withCredentials([
        string(credentialsId: encryptionKey, variable: 'ENCRYPTION_KEY'),
        string(credentialsId: secretName, variable: 'SECRET_VALUE')
    ]) {
        // Check if the secret is empty
        if (!env.SECRET_VALUE) {
            error("Secret ${secretName} is empty. Make sure you have added the secret to the Jenkins credentials.")
        }

        // Encrypt the secret using OpenSSL
        def encryptedSecretFile = "${workspace}/encrypted_key.bin"
        sh """
            echo -n "${env.SECRET_VALUE}" | openssl enc -aes-256-cbc -pbkdf2 -salt -k "${env.ENCRYPTION_KEY}" -out ${encryptedSecretFile}
        """

        // Encode the encrypted secret in Base64
        def encodedEncryptedSecret = sh(script: "base64 < ${encryptedSecretFile}", returnStdout: true).trim()

        // Generate EOF marker
        def eofMarker = sh(script: "dd if=/dev/urandom bs=15 count=1 status=none | base64", returnStdout: true).trim()

        // Return the result as a map
        return [
            secret_value: encodedEncryptedSecret,
            eof_marker: eofMarker
        ]
    }
}

// Example usage in a Jenkins pipeline
pipeline {
    agent any
    environment {
        ENCRYPTION_KEY = 'encryption_key_id'  // Replace with your Jenkins credential ID for the encryption key
        SECRET_NAME = 'secret_credential_id'  // Replace with your Jenkins credential ID for the secret
        ENV_NAME = 'some_env_name' // Example environment name, adjust as needed
    }
    stages {
        stage('Fetch and Encrypt Secret') {
            steps {
                script {
                    def result = fetchAndEncryptSecret(env.SECRET_NAME, env.ENV_NAME, env.ENCRYPTION_KEY)
                    echo "Encrypted Secret: ${result.secret_value}"
                    echo "EOF Marker: ${result.eof_marker}"
                }
            }
        }
    }
}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
/****                                     OPTION 2                                                        **/
/////////////////////////////////////////////////////////////////////////////////////////////////////////////

secretName=my_secret_id
encryptionKey=my_encryption_key_id

/////////////////////////////////////////////////////////////////////////////////////////////////////////////
def fetchAndEncryptSecret(propertiesFileId, envName) {
    withCredentials([file(credentialsId: propertiesFileId, variable: 'SECRETS_FILE')]) {
        // Read and parse the properties file
        def props = new Properties()
        def propsFile = new File(env.SECRETS_FILE)
        props.load(propsFile.newReader())

        def secretName = props['secretName']
        def encryptionKey = props['encryptionKey']

        // Load secrets and environment variables
        withCredentials([
            string(credentialsId: encryptionKey, variable: 'ENCRYPTION_KEY'),
            string(credentialsId: secretName, variable: 'SECRET_VALUE')
        ]) {
            // Check if the secret is empty
            if (!env.SECRET_VALUE) {
                error("Secret ${secretName} is empty. Make sure you have added the secret to the Jenkins credentials.")
            }

            // Encrypt the secret using OpenSSL
            def encryptedSecretFile = "${workspace}/encrypted_key.bin"
            sh """
                echo -n "${env.SECRET_VALUE}" | openssl enc -aes-256-cbc -pbkdf2 -salt -k "${env.ENCRYPTION_KEY}" -out ${encryptedSecretFile}
            """

            // Encode the encrypted secret in Base64
            def encodedEncryptedSecret = sh(script: "base64 < ${encryptedSecretFile}", returnStdout: true).trim()

            // Generate EOF marker
            def eofMarker = sh(script: "dd if=/dev/urandom bs=15 count=1 status=none | base64", returnStdout: true).trim()

            // Return the result as a map
            return [
                secret_value: encodedEncryptedSecret,
                eof_marker: eofMarker
            ]
        }
    }
}

// Example usage in a Jenkins pipeline
pipeline {
    agent any
    environment {
        SECRETS_FILE_ID = 'secrets_file_id'  // Replace with your Jenkins credential ID for the properties file
        ENV_NAME = 'some_env_name' // Example environment name, adjust as needed
    }
    stages {
        stage('Fetch and Encrypt Secret') {
            steps {
                script {
                    def result = fetchAndEncryptSecret(env.SECRETS_FILE_ID, env.ENV_NAME)
                    echo "Encrypted Secret: ${result.secret_value}"
                    echo "EOF Marker: ${result.eof_marker}"
                }
            }
        }
    }
}
//////////////////////////////////////////////////////////////////////////////////

pipeline {
    agent any
    environment {
        DISK_SPACE = 'your-disk-space' // You need to replace 'your-disk-space' with the actual disk space value or set it in Jenkins
        ENCRYPTION_KEY = credentials('ENCRYPTION_KEY')
        DOCKER_USERNAME = credentials('DOCKER_USERNAME')
        DOCKER_TOKEN = credentials('DOCKER_TOKEN')
        MONGODB_ADMIN_USER = credentials('MONGODB_ADMIN_USER')
        MONGODB_ADMIN_PASSWORD = credentials('MONGODB_ADMIN_PASSWORD')
        BACKUP_ENCRYPTION_PASSPHRASE = credentials('BACKUP_ENCRYPTION_PASSPHRASE')
        ELASTICSEARCH_SUPERUSER_PASSWORD = credentials('ELASTICSEARCH_SUPERUSER_PASSWORD')
        SSH_HOST = 'your-ssh-host' // Replace with actual SSH host
        SSH_USER = credentials('SSH_USER')
    }
    stages {
        stage('Set variables for ansible') {
            steps {
                script {
                    def envMap = [
                        encrypted_disk_size: "${env.DISK_SPACE}",
                        disk_encryption_key: "${env.ENCRYPTION_KEY}",
                        dockerhub_username: "${env.DOCKER_USERNAME}",
                        dockerhub_password: "${env.DOCKER_TOKEN}",
                        mongodb_admin_username: "${env.MONGODB_ADMIN_USER}",
                        mongodb_admin_password: "${env.MONGODB_ADMIN_PASSWORD}",
                        backup_encryption_passphrase: "${env.BACKUP_ENCRYPTION_PASSPHRASE}",
                        elasticsearch_superuser_password: "${env.ELASTICSEARCH_SUPERUSER_PASSWORD}",
                        manager_production_server_ip: "${env.SSH_HOST}",
                        ansible_user: "${env.SSH_USER}"
                    ]
                    def jsonWithNewlines = new groovy.json.JsonBuilder(envMap).toPrettyString()
                    def jsonWithoutNewlines = jsonWithNewlines.replaceAll(/\n/, "")
                    env.EXTRA_VARS = jsonWithoutNewlines
                }
            }
        }
        stage('Read known hosts') {
            steps {
                script {
                    dir("${env.JOB_NAME}") {
                        sh """
                        echo "KNOWN_HOSTS<<EOF" >> ${env.WORKSPACE}/known_hosts.env
                        sed -i -e '\$a\\' ./infrastructure/known-hosts
                        cat ./infrastructure/known-hosts >> ${env.WORKSPACE}/known_hosts.env
                        echo "EOF" >> ${env.WORKSPACE}/known_hosts.env
                        """
                    }
                }
            }
        }
        stage('Run playbook') {
            environment {
                ANSIBLE_PERSISTENT_COMMAND_TIMEOUT = 10
                ANSIBLE_SSH_TIMEOUT = 10
                ANSIBLE_SSH_RETRIES = 5
            }
            steps {
                script {
                    sh """
                    ansible-playbook playbook.yml \
                    -i inventory/${env.ENVIRONMENT}.yml \
                    --verbose \
                    --extra-vars '${env.EXTRA_VARS}'
                    """
                }
            }
        }
    }
}
