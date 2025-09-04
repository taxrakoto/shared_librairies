# Jenkins Shared Libraries

This repository provides a collection of reusable Jenkins pipeline functions for building, pushing, and deploying Docker images, as well as integrating with Kubernetes.  

It is designed to centralize CI/CD workflows so that multiple Jenkins pipelines can share the same logic, improving maintainability and consistency across projects.

---

## 📂 Project Structure

- **`vars/`** – Contains pipeline functions accessible in Jenkinsfiles.
- **`src/Jenkins/`** – Houses helper methods used internally by the pipelines.
- **`resources/`** – Example Jenkinsfiles demonstrating how to use the shared library.

---

## 🚀 Available Pipelines

### 🔹 DockerDeployPipeline
- Builds and pushes a Docker image to a container registry.
- Requires a Jenkins credential with ID: **`CI_bUILD_Token`**.

### 🔹 DockerDeployPipelineV2
- Similar to `DockerDeployPipeline`, but supports **two phases**: `staging` and `production`.

### 🔹 imagebuildPush
- A simplified pipeline to **build and push in a single stage**.

### 🔹 KubernetesDeployPipelineV2
- Builds and pushes a Docker image, then deploys it to a Kubernetes cluster using **imperative `kubectl` commands**.

### 🔹 K8SContinousIntegrationPipeline
- Designed for **Jenkins hosted inside a Kubernetes cluster**.
- Uses a pod template (label: **`docker`**) and leverages the **Kaniko** image for building and pushing container images.

---

## ⚙️ Requirements

- A **Jenkins server** with the Shared Library configured.
- A **node** (agent) with:
  - Docker installed
  - Kubectl installed
  - Proper node label set in Jenkins
- For Kubernetes integration:  
  - Jenkins must have access to the cluster (`kubeconfig` or service account).  
  - For Kaniko builds, configure a **pod template labeled `docker`** in Jenkins (Cloud section).

---

## 🔑 Credentials

- **Registry Access**: Jenkins credential with ID **`CI_bUILD_Token`** must be created for authentication against the Docker registry.

---

## 📖 Usage

1. In Jenkins, configure this repository as a **Global Shared Library**.  
   - *Manage Jenkins* → *Configure System* → *Global Pipeline Libraries*.  
   - Add the repo URL and choose `master` (or your default branch).

2. In your `Jenkinsfile`, load the library and call a function:

```groovy
@Library('shared_librairies') _

K8SContinuousIntegrationPipeline {
    BUILD_CONTEXT = '.'
    DOCKERFILE = 'Dockerfile'
    REGISTRY_URL = 'registry.gitlab.com/my-account/my-folder/my-project_name:my-tag'
    IMAGE_TAG = 'staging'
}

