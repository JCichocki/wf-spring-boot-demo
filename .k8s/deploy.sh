#!/bin/bash

# Spring Boot Demo Deployment Script
# Supports both Kubernetes and OpenShift with automatic platform detection

set -euo pipefail

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Default values
ENVIRONMENT="${ENVIRONMENT:-dev}"
PLATFORM="${PLATFORM:-auto}"
NAMESPACE="${NAMESPACE:-}"
IMAGE_TAG="${IMAGE_TAG:-latest}"
DRY_RUN="${DRY_RUN:-false}"
VERBOSE="${VERBOSE:-false}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

log_verbose() {
    if [[ "$VERBOSE" == "true" ]]; then
        echo -e "${BLUE}[VERBOSE]${NC} $1"
    fi
}

# Usage function
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Deploy Spring Boot Demo application to Kubernetes or OpenShift

OPTIONS:
    -e, --environment ENV    Environment to deploy to (dev|staging|prod) [default: dev]
    -p, --platform PLATFORM Platform to deploy to (kubernetes|openshift|auto) [default: auto]
    -n, --namespace NS       Namespace/project to deploy to [default: sb-demo-ENV]
    -t, --tag TAG           Image tag to deploy [default: latest]
    -d, --dry-run           Show what would be deployed without applying
    -v, --verbose           Verbose output
    -h, --help              Show this help message

EXAMPLES:
    # Deploy to development with auto-detection
    $0 -e dev

    # Deploy to production on OpenShift
    $0 -e prod -p openshift

    # Dry run for staging on Kubernetes
    $0 -e staging -p kubernetes -d

    # Deploy with custom namespace and image tag
    $0 -e prod -n my-namespace -t v1.2.3

ENVIRONMENT VARIABLES:
    ENVIRONMENT    Same as -e option
    PLATFORM       Same as -p option
    NAMESPACE      Same as -n option
    IMAGE_TAG      Same as -t option
    DRY_RUN        Same as -d option
    VERBOSE        Same as -v option

EOF
}

# Parse command line arguments
parse_args() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -e|--environment)
                ENVIRONMENT="$2"
                shift 2
                ;;
            -p|--platform)
                PLATFORM="$2"
                shift 2
                ;;
            -n|--namespace)
                NAMESPACE="$2"
                shift 2
                ;;
            -t|--tag)
                IMAGE_TAG="$2"
                shift 2
                ;;
            -d|--dry-run)
                DRY_RUN="true"
                shift
                ;;
            -v|--verbose)
                VERBOSE="true"
                shift
                ;;
            -h|--help)
                usage
                exit 0
                ;;
            *)
                log_error "Unknown option: $1"
                ;;
        esac
    done
}

# Validate environment
validate_environment() {
    case "$ENVIRONMENT" in
        dev|staging|prod)
            log_verbose "Environment '$ENVIRONMENT' is valid"
            ;;
        *)
            log_error "Invalid environment: $ENVIRONMENT. Must be one of: dev, staging, prod"
            ;;
    esac
}

# Detect platform if set to auto
detect_platform() {
    if [[ "$PLATFORM" == "auto" ]]; then
        log_info "Auto-detecting platform..."
        
        if command -v oc &> /dev/null; then
            if oc version --client &> /dev/null; then
                # Try to get server version to confirm we're connected to OpenShift
                if oc version 2>&1 | grep -q "Server Version"; then
                    PLATFORM="openshift"
                    log_success "Detected OpenShift platform"
                    return
                fi
            fi
        fi
        
        if command -v kubectl &> /dev/null; then
            if kubectl version --client &> /dev/null; then
                # Check if we're connected to a cluster
                if kubectl cluster-info &> /dev/null; then
                    # Check if this is OpenShift by looking for OpenShift-specific resources
                    if kubectl api-resources | grep -q "routes.*route.openshift.io"; then
                        PLATFORM="openshift"
                        log_success "Detected OpenShift platform (via kubectl)"
                    else
                        PLATFORM="kubernetes"
                        log_success "Detected Kubernetes platform"
                    fi
                    return
                fi
            fi
        fi
        
        log_error "Could not detect platform. Please specify with -p or ensure kubectl/oc is configured"
    else
        log_verbose "Using specified platform: $PLATFORM"
    fi
}

# Validate platform
validate_platform() {
    case "$PLATFORM" in
        kubernetes|openshift)
            log_verbose "Platform '$PLATFORM' is valid"
            ;;
        *)
            log_error "Invalid platform: $PLATFORM. Must be one of: kubernetes, openshift"
            ;;
    esac
}

# Set default namespace if not provided
set_namespace() {
    if [[ -z "$NAMESPACE" ]]; then
        NAMESPACE="sb-demo-${ENVIRONMENT}"
        log_verbose "Using default namespace: $NAMESPACE"
    else
        log_verbose "Using provided namespace: $NAMESPACE"
    fi
}

# Check required tools
check_tools() {
    log_info "Checking required tools..."
    
    if ! command -v kustomize &> /dev/null; then
        log_error "kustomize is required but not installed. Please install kustomize."
    fi
    
    case "$PLATFORM" in
        kubernetes)
            if ! command -v kubectl &> /dev/null; then
                log_error "kubectl is required for Kubernetes deployment"
            fi
            ;;
        openshift)
            if ! command -v oc &> /dev/null; then
                log_error "oc is required for OpenShift deployment"
            fi
            ;;
    esac
    
    log_success "All required tools are available"
}

# Create temporary kustomization for environment + platform
create_temp_kustomization() {
    local temp_dir="$1"
    local kustomization_file="$temp_dir/kustomization.yaml"
    
    log_verbose "Creating temporary kustomization in $temp_dir"
    
    cat > "$kustomization_file" << EOF
apiVersion: kustomize.config.k8s.io/v1beta1
kind: Kustomization

resources:
  - ../overlays/$PLATFORM
  - ../overlays/$ENVIRONMENT

namespace: $NAMESPACE

images:
  - name: sb-demo
    newTag: $IMAGE_TAG

configMapGenerator:
  - name: deployment-info
    literals:
      - DEPLOYED_BY=$(whoami)
      - DEPLOYED_AT=$(date -u +"%Y-%m-%dT%H:%M:%SZ")
      - DEPLOYMENT_PLATFORM=$PLATFORM
      - DEPLOYMENT_ENVIRONMENT=$ENVIRONMENT
      - GIT_COMMIT=$(git -C "$PROJECT_ROOT" rev-parse --short HEAD 2>/dev/null || echo "unknown")
      - GIT_BRANCH=$(git -C "$PROJECT_ROOT" rev-parse --abbrev-ref HEAD 2>/dev/null || echo "unknown")
    options:
      disableNameSuffixHash: true
EOF

    log_verbose "Temporary kustomization created"
}

# Ensure namespace exists
ensure_namespace() {
    log_info "Ensuring namespace '$NAMESPACE' exists..."
    
    case "$PLATFORM" in
        kubernetes)
            if ! kubectl get namespace "$NAMESPACE" &> /dev/null; then
                if [[ "$DRY_RUN" == "true" ]]; then
                    log_info "Would create namespace: $NAMESPACE"
                else
                    kubectl create namespace "$NAMESPACE"
                    log_success "Created namespace: $NAMESPACE"
                fi
            else
                log_verbose "Namespace '$NAMESPACE' already exists"
            fi
            ;;
        openshift)
            if ! oc get project "$NAMESPACE" &> /dev/null; then
                if [[ "$DRY_RUN" == "true" ]]; then
                    log_info "Would create project: $NAMESPACE"
                else
                    oc new-project "$NAMESPACE" || oc project "$NAMESPACE"
                    log_success "Using project: $NAMESPACE"
                fi
            else
                oc project "$NAMESPACE"
                log_verbose "Using existing project: $NAMESPACE"
            fi
            ;;
    esac
}

# Deploy application
deploy_application() {
    local temp_dir="$1"
    
    log_info "Building manifests with kustomize..."
    
    # Build the manifests
    local manifests
    manifests=$(kustomize build "$temp_dir")
    
    if [[ "$DRY_RUN" == "true" ]]; then
        log_info "=== DRY RUN: Generated manifests ==="
        echo "$manifests"
        log_info "=== END DRY RUN ==="
        return
    fi
    
    log_info "Applying manifests to $PLATFORM cluster..."
    
    case "$PLATFORM" in
        kubernetes)
            echo "$manifests" | kubectl apply -f -
            ;;
        openshift)
            echo "$manifests" | oc apply -f -
            ;;
    esac
    
    log_success "Application deployed successfully!"
}

# Wait for deployment
wait_for_deployment() {
    if [[ "$DRY_RUN" == "true" ]]; then
        return
    fi
    
    log_info "Waiting for deployment to be ready..."
    
    case "$PLATFORM" in
        kubernetes)
            kubectl -n "$NAMESPACE" rollout status deployment/sb-demo --timeout=300s
            ;;
        openshift)
            oc -n "$NAMESPACE" rollout status deployment/sb-demo --timeout=300s
            ;;
    esac
    
    log_success "Deployment is ready!"
}

# Show deployment info
show_deployment_info() {
    if [[ "$DRY_RUN" == "true" ]]; then
        return
    fi
    
    log_info "Deployment information:"
    
    case "$PLATFORM" in
        kubernetes)
            kubectl -n "$NAMESPACE" get pods,svc,ingress -l app=sb-demo
            
            local ingress_host
            ingress_host=$(kubectl -n "$NAMESPACE" get ingress sb-demo -o jsonpath='{.spec.rules[0].host}' 2>/dev/null || echo "")
            if [[ -n "$ingress_host" ]]; then
                log_info "Application URL: https://$ingress_host"
            fi
            ;;
        openshift)
            oc -n "$NAMESPACE" get pods,svc,route -l app=sb-demo
            
            local route_host
            route_host=$(oc -n "$NAMESPACE" get route sb-demo -o jsonpath='{.spec.host}' 2>/dev/null || echo "")
            if [[ -n "$route_host" ]]; then
                log_info "Application URL: https://$route_host"
            fi
            ;;
    esac
}

# Cleanup function
cleanup() {
    if [[ -n "${TEMP_DIR:-}" ]] && [[ -d "$TEMP_DIR" ]]; then
        log_verbose "Cleaning up temporary directory: $TEMP_DIR"
        rm -rf "$TEMP_DIR"
    fi
}

# Main function
main() {
    # Set up cleanup trap
    trap cleanup EXIT
    
    # Parse arguments
    parse_args "$@"
    
    # Validate inputs
    validate_environment
    detect_platform
    validate_platform
    set_namespace
    
    # Check tools
    check_tools
    
    # Create temporary directory for kustomization
    TEMP_DIR=$(mktemp -d)
    create_temp_kustomization "$TEMP_DIR"
    
    # Deploy
    ensure_namespace
    deploy_application "$TEMP_DIR"
    wait_for_deployment
    show_deployment_info
    
    log_success "Deployment completed successfully!"
    log_info "Environment: $ENVIRONMENT"
    log_info "Platform: $PLATFORM"
    log_info "Namespace: $NAMESPACE"
    log_info "Image Tag: $IMAGE_TAG"
}

# Run main function
main "$@"