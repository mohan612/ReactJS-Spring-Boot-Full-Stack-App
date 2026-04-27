def call(Map config = [:]) {
    def channel = config.channel ?: '#deployments'
    def status = config.status ?: currentBuild.currentResult
    def projectName = config.projectName ?: 'unknown'
    
    return {
        script {
            def color = getColorForStatus(status)
            def emoji = getEmojiForStatus(status)
            def message = "${emoji} ${projectName} build ${status.toLowerCase()}: ${env.JOB_NAME} - ${env.BUILD_NUMBER}"
            
            if (status == 'FAILURE') {
                message += "\\nCheck console output: ${env.BUILD_URL}"
            }
            
            slackSend(
                channel: channel,
                color: color,
                message: message
            )
        }
    }
}

def getColorForStatus(String status) {
    switch (status) {
        case 'SUCCESS': return 'good'
        case 'FAILURE': return 'danger'
        case 'UNSTABLE': return 'warning'
        default: return 'normal'
    }
}

def getEmojiForStatus(String status) {
    switch (status) {
        case 'SUCCESS': return '✅'
        case 'FAILURE': return '❌'
        case 'UNSTABLE': return '⚠️'
        default: return 'ℹ️'
    }
}
