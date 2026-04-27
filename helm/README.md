# Helm Charts for Hobbie Microservices

This directory contains Helm charts for deploying the Hobbie application microservices to Kubernetes.

## Chart Structure

```
helm/
├── hobbie-frontend/          # React Frontend Microservice
│   ├── Chart.yaml           # Chart metadata
│   ├── values.yaml          # Default values
│   └── templates/           # Kubernetes manifests
│       ├── deployment.yaml
│       ├── service.yaml
│       ├── ingress.yaml
│       ├── configmap.yaml
│       └── _helpers.tpl
├── hobbie-backend/           # Spring Boot Backend Microservice
│   ├── Chart.yaml           # Chart metadata
│   ├── values.yaml          # Default values
│   └── templates/           # Kubernetes manifests
│       ├── deployment.yaml
│       ├── service.yaml
│       ├── ingress.yaml
│       ├── configmap.yaml
│       ├── secrets.yaml
│       └── _helpers.tpl
├── values-staging.yaml      # Staging environment overrides
└── values-production.yaml   # Production environment overrides
```

## Naming Conventions

### Service Names
- **Frontend**: `hobbie-frontend`
- **Backend**: `hobbie-backend`

### Release Names
- **Staging**: `{service-name}-staging`
- **Production**: `{service-name}-prod`

### Kubernetes Resources
- **Deployments**: `{release-name}`
- **Services**: `{release-name}`
- **Ingress**: `{release-name}`
- **ConfigMaps**: `{service-name}-config`
- **Secrets**: `{service-name}-secrets`

## Deployment Instructions

### Prerequisites
- Kubernetes cluster (v1.20+)
- Helm 3.x installed
- kubectl configured

### Staging Environment

```bash
# Deploy Frontend
helm upgrade --install hobbie-frontend-staging helm/hobbie-frontend/ \
    --namespace staging \
    --create-namespace \
    --values helm/hobbie-frontend/values.yaml \
    --values helm/values-staging.yaml

# Deploy Backend
helm upgrade --install hobbie-backend-staging helm/hobbie-backend/ \
    --namespace staging \
    --create-namespace \
    --values helm/hobbie-backend/values.yaml \
    --values helm/values-staging.yaml
```

### Production Environment

```bash
# Deploy Frontend
helm upgrade --install hobbie-frontend-prod helm/hobbie-frontend/ \
    --namespace production \
    --create-namespace \
    --values helm/hobbie-frontend/values.yaml \
    --values helm/values-production.yaml

# Deploy Backend
helm upgrade --install hobbie-backend-prod helm/hobbie-backend/ \
    --namespace production \
    --create-namespace \
    --values helm/hobbie-backend/values.yaml \
    --values helm/values-production.yaml
```

## Configuration

### Environment Variables

#### Frontend
- `REACT_APP_API_URL`: Backend API URL
- `NODE_ENV`: Node environment (development/staging/production)

#### Backend
- `SPRING_PROFILES_ACTIVE`: Spring profile
- `JAVA_OPTS`: JVM options
- `DATABASE_HOST`: MySQL hostname
- `DATABASE_PORT`: MySQL port
- `DATABASE_NAME`: Database name
- `DATABASE_USER`: Database username
- `DATABASE_PASSWORD`: Database password (from secrets)

### Secrets Management

Create secrets for backend configuration:

```bash
kubectl create secret generic hobbie-backend-secrets \
    --from-literal=DATABASE_PASSWORD=your-db-password \
    --from-literal=JWT_SECRET=your-jwt-secret \
    --namespace staging
```

### Ingress Configuration

Update the `hosts` section in values files to match your domain:

```yaml
ingress:
  enabled: true
  hosts:
    - host: hobbie-frontend.yourdomain.com
      paths:
        - path: /
          pathType: Prefix
```

## Monitoring and Logging

### Health Checks
- Frontend: HTTP GET `/`
- Backend: HTTP GET `/actuator/health`

### Metrics
- Backend exposes Spring Boot actuator metrics
- Frontend Nginx access logs

### Logging
- Application logs are output to stdout
- Use Kubernetes logging drivers for collection

## Security Best Practices

### Container Security
- Non-root user execution
- Read-only filesystem
- Minimal capabilities
- Resource limits

### Network Security
- TLS termination at ingress
- Internal service communication
- Network policies (if enabled)

### Secrets Management
- Use Kubernetes secrets
- Rotate secrets regularly
- Avoid storing secrets in values files

## Scaling

### Horizontal Pod Autoscaling

Enable HPA in production values:

```yaml
autoscaling:
  enabled: true
  minReplicas: 3
  maxReplicas: 10
  targetCPUUtilizationPercentage: 70
```

### Resource Limits

Configure appropriate resource requests and limits based on your workload.

## Troubleshooting

### Common Issues

1. **Pod Not Starting**: Check resource limits and image pull policy
2. **Service Not Accessible**: Verify service type and port configuration
3. **Ingress Not Working**: Check ingress controller and DNS configuration
4. **Database Connection**: Verify database credentials and network connectivity

### Debug Commands

```bash
# Check deployment status
kubectl get deployments -n staging

# Check pod logs
kubectl logs deployment/hobbie-frontend-staging -n staging

# Debug with exec
kubectl exec -it deployment/hobbie-backend-staging -n staging -- bash

# Check events
kubectl get events -n staging --sort-by=.metadata.creationTimestamp
```

## Maintenance

### Chart Updates
- Update Chart.yaml version when making changes
- Test changes in staging before production
- Use helm diff to review changes

### Dependency Management
- Update image tags regularly
- Review and update resource requirements
- Monitor and update security patches

## Backup and Recovery

### Backup ConfigMaps and Secrets
```bash
kubectl get configmap hobbie-backend-config -n production -o yaml > backup-config.yaml
kubectl get secret hobbie-backend-secrets -n production -o yaml > backup-secrets.yaml
```

### Restore from Backup
```bash
kubectl apply -f backup-config.yaml -n production
kubectl apply -f backup-secrets.yaml -n production
```
