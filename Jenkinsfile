pipeline {
  agent any
  stages {
    stage('Init') {
      steps {
        echo 'init'
        sh 'echo \'initializing...\''
      }
    }
    stage('Build') {
      steps {
        echo 'build'
        sh 'echo \'Building...\''
      }
    }
    stage('Deploy') {
      steps {
        echo 'deploy'
        script {
          def userInput = true
          def didTimeout = false
          try {
            timeout(time: 120, unit: 'SECONDS') { // change to a convenient timeout for you
            userInput = input(
              id: 'Deploy1', message: 'Would you like to deploy this to production?', parameters: [
                [$class: 'BooleanParameterDefinition', defaultValue: true, description: 'Someone must approve production deployments', name: 'Please indicate your decision.']
              ])
            }
          } catch(err) { // timeout reached or input false
          def user = err.getCauses()[0].getUser()
          if('SYSTEM' == user.toString()) { // SYSTEM means timeout.
          didTimeout = true
        } else {
          userInput = false
          echo "Aborted by: [${user}]"
        }
      }
      
      if (userInput == true) {
        sh 'echo \'Deploying...\''
      } else if (didTimeout) {
        echo "No decision making input was received before timeout."
        currentBuild.result = 'NOT_BUILT'
      } else {
        echo "A user decided not to deploy to production."
        currentBuild.result = 'ABORTED'
      }
    }
    
  }
}
}
}