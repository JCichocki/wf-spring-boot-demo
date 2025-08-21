# Kubernetes & OpenShift Deployment

This directory contains Kubernetes and OpenShift deployment manifests for the Spring Boot Demo application using Kustomize for configuration management.

## 🏗️ Architecture

```
.k8s/
├── base/                     # Platform-agnostic base resources
│   ├── deployment.yaml       # Application deployment
│   ├── service.yaml         # Service & ServiceAccount
│   ├── configmap.yaml       # ConfigMap & Secrets
│   ├── pvc.yaml            # Persistent Volume Claims
│   ├── ingress.yaml        # Ingress & NetworkPolicy
│   └── kustomization.yaml  # Base kustomization
├── overlays/
│   ├── openshift/          # OpenShift-specific resources
│   │   ├── route.yaml      # OpenShift Routes
│   │   ├── imagestream.yaml # ImageStream definitions
│   │   ├── deployment-patch.yaml # OpenShift security contexts
│   │   └── kustomization.yaml
│   ├── kubernetes/         # Vanilla K8s resources
│   │   ├── hpa.yaml       # Horizontal Pod Autoscaler
│   │   ├── pdb.yaml       # Pod Disruption Budget
│   │   ├── monitoring.yaml # Prometheus monitoring
│   │   └── kustomization.yaml
│   ├── dev/               # Development environment
│   ├── staging/           # Staging environment
│   └── prod/              # Production environment
├── deploy.sh              # Deployment script with auto-detection
└── README.md             # This file
```

## 🚀 Quick Start

### Automatic Deployment

The simplest way to deploy is using the deployment script with auto-detection:

```bash
# Deploy to development (auto-detects platform)
./.k8s/deploy.sh -e dev

# Deploy to production on OpenShift
./.k8s/deploy.sh -e prod -p openshift

# Dry run to see what would be deployed
./.k8s/deploy.sh -e staging -d
```

### Manual Deployment

#### OpenShift

```bash
# Create project
oc new-project sb-demo-dev

# Deploy using kustomize
kustomize build .k8s/overlays/openshift | oc apply -f -

# With environment overlay
kustomize build .k8s/overlays/dev | oc apply -f -
```

#### Kubernetes

```bash
# Create namespace
kubectl create namespace sb-demo-dev

# Deploy using kustomize
kustomize build .k8s/overlays/kubernetes | kubectl apply -f -

# With environment overlay
kustomize build .k8s/overlays/dev | kubectl apply -f -
```

## 🎯 Deployment Script Usage

The `deploy.sh` script provides intelligent platform detection and environment management:

### Options

```bash
Usage: deploy.sh [OPTIONS]

OPTIONS:
    -e, --environment ENV    Environment (dev|staging|prod) [default: dev]
    -p, --platform PLATFORM Platform (kubernetes|openshift|auto) [default: auto]
    -n, --namespace NS       Namespace/project [default: sb-demo-ENV]
    -t, --tag TAG           Image tag [default: latest]
    -d, --dry-run           Show manifests without applying
    -v, --verbose           Verbose output
    -h, --help              Show help
```

### Examples

```bash
# Development deployment with auto-detection
./deploy.sh -e dev

# Production on specific platform
./deploy.sh -e prod -p openshift -t v1.2.3

# Staging with custom namespace
./deploy.sh -e staging -n my-custom-namespace

# Dry run to preview changes
./deploy.sh -e prod -d -v
```

### Environment Variables

You can also use environment variables:

```bash
export ENVIRONMENT=prod
export PLATFORM=openshift
export IMAGE_TAG=v1.2.3
./deploy.sh
```

## 🏷️ Environments

### Development (`dev`)
- **Replicas**: 1
- **Resources**: 256Mi RAM, 100m CPU
- **Storage**: 1Gi uploads, 2Gi database
- **Features**: Debug logging, all actuator endpoints
- **Profile**: `dev`

### Staging (`staging`)
- **Replicas**: 2
- **Resources**: 384Mi RAM, 200m CPU
- **Storage**: 3Gi uploads, 5Gi database
- **Features**: Production-like setup, metrics enabled
- **Profile**: `staging`

### Production (`prod`)
- **Replicas**: 3
- **Resources**: 512Mi RAM, 250m CPU
- **Storage**: 10Gi uploads, 20Gi database
- **Features**: Security hardened, monitoring, alerting
- **Profile**: `prod`

## 🛡️ Security

### OpenShift Security Contexts

OpenShift automatically assigns random UIDs for security. The overlays remove specific user/group settings to work with Security Context Constraints (SCCs).

### Kubernetes Security

Vanilla Kubernetes deployments include:
- Non-root containers (UID 1001)
- Read-only root filesystem
- Dropped capabilities
- seccomp profiles

## 📊 Monitoring

### Kubernetes

Includes Prometheus ServiceMonitor and alerting rules:

```bash
# Check if monitoring is deployed
kubectl get servicemonitor,prometheusrule -l app=sb-demo
```

### OpenShift

Uses OpenShift's built-in monitoring:

```bash
# Check metrics endpoint
oc get route sb-demo -o jsonpath='{.spec.host}'
curl -k https://<route>/actuator/prometheus
```

## 💾 Storage

### Persistent Volume Claims

- **sb-demo-uploads**: File upload storage
- **sb-demo-database**: Database data (when using persistent DB)

### Storage Classes

- **OpenShift**: Uses cluster default storage class
- **Kubernetes**: Configurable via `storageClassName`

## 🌐 Networking

### OpenShift Routes

```bash
# Get application URL
oc get route sb-demo -o jsonpath='{.spec.host}'

# Multiple routes available:
# - sb-demo: Main application
# - sb-demo-api: API endpoints only
```

### Kubernetes Ingress

```bash
# Get ingress info
kubectl get ingress sb-demo

# Default host: sb-demo.k8s.local
# Configure DNS or use port-forward for local access
kubectl port-forward svc/sb-demo 8080:8080
```

## 🔧 Configuration

### ConfigMap Structure

```yaml
sb-demo-config:
  SPRING_PROFILES_ACTIVE: "prod"
  SERVER_PORT: "8080"
  java-opts: |
    -XX:+UseG1GC
    -XX:MaxRAMPercentage=75.0
  database-url: "jdbc:derby:memory:sb-demo;create=true"
```

### Secrets Management

```yaml
sb-demo-secrets:
  database-username: <base64>
  database-password: <base64>
  # Add additional secrets as needed
```

### Environment-Specific Overrides

Each environment overlay can override base configurations:

```bash
# View merged configuration
kustomize build .k8s/overlays/prod | grep -A 20 "kind: ConfigMap"
```

## 🔍 Troubleshooting

### Common Issues

#### ImagePullBackOff
```bash
# Check image reference
kubectl describe pod -l app=sb-demo

# For OpenShift, ensure ImageStream is created
oc get imagestream sb-demo
```

#### CrashLoopBackOff
```bash
# Check application logs
kubectl logs -l app=sb-demo --tail=100

# Check resource limits
kubectl describe pod -l app=sb-demo | grep -A 5 "Limits\|Requests"
```

#### Storage Issues
```bash
# Check PVC status
kubectl get pvc

# Check storage class
kubectl get storageclass
```

### Debug Commands

```bash
# Get all resources
kubectl get all -l app=sb-demo

# Describe deployment
kubectl describe deployment sb-demo

# Port forward for local access
kubectl port-forward svc/sb-demo 8080:8080

# Check configuration
kubectl get configmap sb-demo-config -o yaml
```

### Platform-Specific Debugging

#### OpenShift
```bash
# Check SCC issues
oc adm policy who-can use scc restricted

# Check route status
oc get route sb-demo -o yaml

# Check project permissions
oc policy who-can create pods
```

#### Kubernetes
```bash
# Check ingress controller
kubectl get pods -n ingress-nginx

# Check network policies
kubectl get networkpolicy

# Check RBAC
kubectl auth can-i create pods --as=system:serviceaccount:sb-demo:sb-demo
```

## 🔄 Updates and Rollbacks

### Rolling Updates

```bash
# Update image tag
./deploy.sh -e prod -t v1.2.3

# Or manually patch
kubectl set image deployment/sb-demo sb-demo=sb-demo:v1.2.3
```

### Rollbacks

```bash
# Kubernetes
kubectl rollout undo deployment/sb-demo

# OpenShift
oc rollout undo dc/sb-demo
```

### Zero-Downtime Deployments

The deployment configuration ensures zero-downtime updates:
- Rolling update strategy
- Readiness probes prevent traffic to unhealthy pods
- Pod disruption budgets (Kubernetes)

## 📋 Health Checks

### Actuator Endpoints

- **Liveness**: `/actuator/health/liveness`
- **Readiness**: `/actuator/health/readiness`
- **Metrics**: `/actuator/prometheus` (monitoring)

### Manual Health Check

```bash
# Get pod IP and test directly
POD_IP=$(kubectl get pod -l app=sb-demo -o jsonpath='{.items[0].status.podIP}')
curl http://$POD_IP:8080/actuator/health
```

## 🚦 CI/CD Integration

### GitOps Workflow

1. **Build**: Application built in CI pipeline
2. **Test**: Automated tests run
3. **Package**: Container image created
4. **Deploy**: Kustomize + deployment script
5. **Verify**: Health checks and smoke tests

### Integration Examples

#### GitHub Actions
```yaml
- name: Deploy to OpenShift
  run: |
    ./.k8s/deploy.sh -e ${{ matrix.environment }} -p openshift -t ${{ github.sha }}
```

#### Tekton Pipelines
```yaml
- name: deploy
  taskRef:
    name: kustomize-deploy
  params:
    - name: environment
      value: $(params.environment)
    - name: platform
      value: openshift
```

## 📚 Additional Resources

- [Kustomize Documentation](https://kustomize.io/)
- [OpenShift Documentation](https://docs.openshift.com/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)

## 🤝 Contributing

When modifying deployment configurations:

1. **Test locally**: Use dry-run mode first
2. **Validate**: Ensure both platforms work
3. **Document**: Update this README for significant changes
4. **Security**: Follow security best practices for both platforms

```bash
# Validate changes
./deploy.sh -e dev -d -v

# Test on both platforms
./deploy.sh -e dev -p kubernetes -d
./deploy.sh -e dev -p openshift -d
```