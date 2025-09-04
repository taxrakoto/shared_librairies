# Jenkins Shared Libraries

![Jenkins](https://img.shields.io/badge/Jenkins-Pipeline-blue?logo=jenkins&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-Deploy-326CE5?logo=kubernetes&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-green)

Reusable Jenkins Shared Library providing pipelines for **Docker build & push** and **Kubernetes deployments**.  
Centralizes CI/CD workflows so teams can share and maintain pipeline logic consistently.

---

## 📑 Table of Contents

- [📂 Project Structure](#-project-structure)
- [🚀 Available Pipelines](#-available-pipelines)
- [⚙️ Requirements](#️-requirements)
- [🔑 Credentials](#-credentials)
- [📖 Usage](#-usage)
- [🧩 Examples](#-examples)
- [🤝 Contributing](#-contributing)
- [📜 License](#-license)

---

## 📂 Project Structure

- **`vars/`** – Pipeline entrypoints (Groovy functions).
- **`src/Jenkins/`** – Helper methods used by pipelines.
- **`resources/`** – Example Jenkinsfiles.

---

## 🚀 Available Pipelines

### 🔹 `DockerDeployPipeline`
- Builds and pushes a Docker image to a registry.  
- Uses Jenkins credential: **`CI_bUILD_Token`**.

### 🔹 `DockerDeployPipelineV2`
- Extends `DockerDeployPipeline` with **two phases**: `staging` and `production`.

### 🔹 `imagebuildPush`
- Single-stage pipeline to **build & push in one go**.

### 🔹 `KubernetesDeployPipelineV2`
- Builds and pushes a Docker image, then deploys to a Kubernetes cluster using **kubectl**.

### 🔹 `K8SContinousIntegrationPipeline`
- For **Jenkins running inside Kubernetes**.  
- Uses a **Kaniko-based pod template** labeled `docker`.

---

## ⚙️ Requirements

- **Jenkins server** configured with this Shared Library.
- **Build node (agent)** with:
  - Docker installed  
  - Kubectl installed  
  - Proper Jenkins node label
- For Kubernetes deployments:
  - Jenkins must have access to the cluster (`kubeconfig` or service account).  
- For Kaniko builds:
  - Pod template labeled `docker` defined in Jenkins Cloud configuration.

---

## 🔑 Credentials

- **Registry Access**: Jenkins credential with ID **`CI_bUILD_Token`** is required to push images.

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
    REGISTRY_URL = 'registry.gitlab.com/my-account/my-folder/my-project_name'
    IMAGE_TAG = 'staging'
}
```

## 🤝 Contributing

Contributions are welcome!  

## 📜 License

This project is licensed under the **MIT License**.  
See the [LICENSE](./LICENSE) file for details.




