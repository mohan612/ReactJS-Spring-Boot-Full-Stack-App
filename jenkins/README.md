# Jenkins CI/CD Configuration

This directory contains Jenkins pipeline configurations for the Hobbie microservices application.

## Pipeline Files

### Individual Pipelines
- **`Jenkinsfile.frontend`**: React frontend pipeline with security and quality checks
- **`Jenkinsfile.backend`**: Spring Boot backend pipeline with security and quality checks

### Shared Library
- **`jenkins-shared-library/`**: Reusable pipeline functions for common operations

## Pipeline Features

### Security Scanning
- ✅ **Secret Scanning**: TruffleHog for detecting secrets in code
- ✅ **Image Scanning**: Trivy for container vulnerability scanning
- ✅ **Dependency Scanning**: Nexus IQ for third-party dependency analysis

### Code Quality
- ✅ **PR Validation**: Title and description validation
- ✅ **SonarQube**: Code quality analysis with quality gates
- ✅ **Static Analysis**: ESLint (frontend), Checkstyle/SpotBugs (backend)

### Build & Test
- ✅ **Automated Testing**: Unit tests with coverage reporting
- ✅ **Docker Build**: Multi-stage Docker builds
- ✅ **Artifact Publishing**: Test results and coverage reports

### Deployment
- ✅ **Staging Deployment**: Automatic deployment on develop branch
- ✅ **Kubernetes Integration**: Rolling updates and health checks

## Required Jenkins Credentials

Configure these credentials in Jenkins:

### Docker Registry
- **ID**: `docker-registry`
- **Type**: Username with password
- **Username**: Your Docker Hub username
- **Password**: Your Docker Hub password or access token

### SonarQube
- **ID**: `sonarqube-token`
- **Type**: Secret text
- **Secret**: Your SonarQube token

### Nexus IQ
- **ID**: `nexus-iq-token`
- **Type**: Username with password
- **Username**: Nexus IQ username
- **Password**: Nexus IQ password

### Trivy
- **ID**: `trivy-token`
- **Type**: Secret text
- **Secret**: Trivy API token (if required)

### Slack
- **ID**: `slack-webhook`
- **Type**: Secret text
- **Secret**: Slack webhook URL for notifications

## Required Jenkins Plugins

Install these plugins in Jenkins:

### Core Plugins
- Pipeline
- Pipeline: Groovy
- Pipeline: Multibranch
- Pipeline: Stage View
- Pipeline: GitHub
- Git

### Security & Quality
- SonarQube Scanner
- OWASP Dependency-Check
- Trivy Scanner
- Docker Pipeline

### Build Tools
- Node.js Plugin
- Maven Integration
- Docker Pipeline

### Notifications
- Slack Notification

### Reporting
- HTML Publisher
- JUnit Plugin
- Coverage Plugin

## Pipeline Configuration

### Environment Variables
Each pipeline uses these environment variables:
- `DOCKER_REGISTRY`: Docker registry credentials
- `SONARQUBE_TOKEN`: SonarQube authentication token
- `NEXUS_IQ_TOKEN`: Nexus IQ authentication token
- `TRIVY_TOKEN`: Trivy authentication token

### Branch Strategy
- **main**: Production deployment with integration tests
- **develop**: Staging deployment
- **feature/***: Build and test only
- **PRs**: Full validation pipeline

## Usage

### Multibranch Pipeline Setup
1. Create a Multibranch Pipeline job in Jenkins
2. Point it to this repository
3. Jenkins will automatically discover and run `Jenkinsfile.frontend` and `Jenkinsfile.backend`

### Shared Library Setup
1. Configure global shared library in Jenkins
2. Point to the `jenkins-shared-library` directory
3. Use `securityPipeline` and `notify` functions in your pipelines

## Security Best Practices

### Credential Management
- Store all secrets in Jenkins credentials
- Use credential IDs in pipeline files
- Never hardcode passwords or tokens

### Pipeline Security
- Use `withCredentials` wrapper for sensitive operations
- Limit pipeline permissions with appropriate roles
- Audit pipeline execution logs

### Container Security
- Scan images before deployment
- Use minimal base images
- Apply security patches regularly

## Monitoring & Alerting

### Build Notifications
- Slack notifications for build status
- Email alerts for critical failures
- Dashboard integration for build metrics

### Quality Metrics
- SonarQube quality gates
- Code coverage thresholds
- Security vulnerability tracking

## Troubleshooting

### Common Issues
1. **Docker build failures**: Check Dockerfile syntax and context
2. **SonarQube timeout**: Increase timeout or check server connectivity
3. **Trivy scan failures**: Verify Trivy installation and permissions
4. **Credential errors**: Validate credential IDs and permissions

### Debug Mode
Enable debug logging by adding this to your pipeline:
```groovy
options {
    timestamps()
    ansiColor('xterm')
}
```

## Maintenance

### Regular Updates
- Update Jenkins plugins monthly
- Review and update security scanning tools
- Monitor pipeline performance and optimize

### Backup Strategy
- Backup Jenkins configuration regularly
- Export pipeline definitions
- Document credential dependencies
