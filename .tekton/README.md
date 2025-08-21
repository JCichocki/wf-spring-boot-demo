# Tekton CI/CD Pipeline for Spring Boot Demo

This directory contains Tekton pipeline definitions compatible with OpenShift 4.16 (Tekton 0.53.x).

## Pipeline Overview

The `sb-demo-pipeline` performs the following steps:
1. **fetch-source** - Clone Git repository
2. **test** - Run Maven tests
3. **build** - Build application with Maven
4. **build-image** - Create container image using Buildah
5. **deploy** - Deploy to OpenShift

## Quick Start

### Deploy the Pipeline

```bash
# Apply all resources using Kustomize
oc apply -k .tekton/

# Or apply individual components
oc apply -f .tekton/pvc.yaml
oc apply -f .tekton/rbac.yaml
oc apply -f .tekton/tasks/
oc apply -f .tekton/pipeline.yaml
oc apply -f .tekton/triggers/
```

### Run the Pipeline Manually

```bash
# Create a PipelineRun
oc create -f - <<EOF
apiVersion: tekton.dev/v1beta1
kind: PipelineRun
metadata:
  generateName: sb-demo-manual-run-
spec:
  serviceAccountName: sb-demo-pipeline-sa
  pipelineRef:
    name: sb-demo-pipeline
  params:
    - name: repo-url
      value: "https://github.com/your-org/wf-spring-boot-demo.git"
    - name: repo-revision
      value: "main"
    - name: image-name
      value: "image-registry.openshift-image-registry.svc:5000/$(oc project -q)/sb-demo"
    - name: image-tag
      value: "latest"
  workspaces:
    - name: shared-data
      persistentVolumeClaim:
        claimName: sb-demo-workspace-pvc
    - name: maven-local-repo
      persistentVolumeClaim:
        claimName: sb-demo-maven-repo-pvc
EOF
```

### Set Up Git Webhooks

1. **Create GitHub webhook secret:**
```bash
oc create secret generic github-webhook-secret \
  --from-literal=secretToken="your-random-secret-token"
```

2. **Get the EventListener route:**
```bash
oc get route el-sb-demo-eventlistener -o jsonpath='{.spec.host}'
```

3. **Configure GitHub webhook:**
   - URL: `https://<eventlistener-route>`
   - Content-Type: `application/json`
   - Secret: Your secret token
   - Events: `push`, `pull_request`

## Components

### Tasks (`tasks/`)
- `git-clone.yaml` - Clone Git repository
- `maven-build.yaml` - Maven build task
- `maven-test.yaml` - Maven test task with results
- `buildah-build.yaml` - Container image build with Buildah
- `oc-deploy.yaml` - OpenShift deployment task

### Pipeline (`pipeline.yaml`)
Main pipeline definition with stages and workspace configuration.

### Triggers (`triggers/`)
- `eventlistener.yaml` - Listen for Git webhooks
- `triggerbinding.yaml` - Extract webhook data
- `triggertemplate.yaml` - Create PipelineRuns from events

### RBAC (`rbac.yaml`)
Service account and permissions for pipeline execution.

### Storage (`pvc.yaml`)
Persistent Volume Claims for workspace and Maven cache.

## Configuration

### Parameters
The pipeline accepts these parameters:
- `repo-url` - Git repository URL
- `repo-revision` - Branch/tag/commit to build
- `image-name` - Container image name
- `image-tag` - Container image tag
- `deployment-name` - Kubernetes deployment name
- `target-namespace` - Deployment namespace

### Workspaces
- `shared-data` - Source code and build artifacts
- `maven-local-repo` - Maven dependency cache
- `maven-settings` - Custom Maven settings (optional)
- `dockerconfig-secret` - Registry credentials (optional)

## Monitoring

View pipeline runs:
```bash
# List all pipeline runs
oc get pipelinerun

# Watch a specific run
oc get pipelinerun <run-name> -o yaml

# View logs
tkn pipelinerun logs <run-name> -f
```

## Troubleshooting

### Common Issues

1. **Permission denied errors**
   - Ensure `sb-demo-pipeline-sa` has proper RBAC permissions
   - Check if service account is applied to the correct namespace

2. **Image push failures**
   - Verify internal registry is accessible
   - Check if service account has `system:image-builder` role

3. **Maven build failures**
   - Increase memory limits in task definitions
   - Check Maven settings and proxy configuration

4. **Storage issues**
   - Ensure PVCs are bound and have sufficient space
   - Check storage class compatibility

### Logs and Debugging

```bash
# View task logs
tkn taskrun logs <taskrun-name> -f

# Describe failed resources
oc describe pipelinerun <run-name>
oc describe taskrun <taskrun-name>

# Check events
oc get events --sort-by=.metadata.creationTimestamp
```

## Customization

### Environment-Specific Overlays

Create environment-specific configurations:

```bash
mkdir -p .tekton/overlays/{dev,staging,prod}
```

Example dev overlay (`overlays/dev/kustomization.yaml`):
```yaml
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - ../../

patchesStrategicMerge:
  - pipeline-patch.yaml

namespace: sb-demo-dev
```

### Custom Tasks

Add custom tasks to the `tasks/` directory and reference them in the pipeline.

### Registry Configuration

For external registries, create a docker config secret:
```bash
oc create secret docker-registry registry-secret \
  --docker-server=your-registry.com \
  --docker-username=username \
  --docker-password=password
```

## OpenShift 4.16 Compatibility

This pipeline is designed for:
- OpenShift 4.16
- Tekton Pipelines 0.53.x
- Red Hat OpenShift Pipelines 1.14+

Features used:
- `v1beta1` API version
- Buildah for container builds
- OpenShift CLI for deployments
- Internal image registry integration
- OpenShift Routes for webhooks