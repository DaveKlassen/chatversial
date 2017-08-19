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
          echo "User: [${user}]"
          
          if('SYSTEM' == user.toString()) { // SYSTEM means timeout.
          didTimeout = true
        } else {
          userInput = false
          echo "Aborted by: [${user}]"
        }
      }
      echo "userInput [${userInput}]"
      echo "didTimeout [${didTimeout}]"
      
      if (userInput == true) {
        sh 'echo \'Deploying...\''
        currentBuild.result = 'SUCCESS'
      } else if (didTimeout) {
        echo "No decision making input was received before timeout."
        currentBuild.result = 'NOT_BUILT'
      } else {
        echo "A user decided not to deploy to production."
        currentBuild.result = 'ABORTED'
      }
      
      echo "Build Result : [${currentBuild.result.toString()}]"
    }
    
  }
}
stage('Next') {
  steps {
    script {
      def outcome = input id: 'Run-test-suites',
      message: 'Workflow Configuration',
      ok: 'Okay',
      parameters: [
        [
          $class: 'BooleanParameterDefinition',
          defaultValue: true,
          name: 'Run test suites?',
          description: 'A checkbox option'
        ],
        [
          $class: 'StringParameterDefinition',
          defaultValue: "Hello",
          name: 'Enter some text',
          description: 'A text option'
        ],
        [
          $class: 'PasswordParameterDefinition',
          defaultValue: "MyPasswd",
          name: 'Enter a password',
          description: 'A password option'
        ],
        [
          $class: 'ChoiceParameterDefinition', choices: 'Choice 1\nChoice 2\nChoice 3',
          name: 'Take your pick',
          description: 'A select box option'
        ]
      ]
      
      echo "P1: ${outcome.get('Run test suites?')}"
      echo "P2: ${outcome.get('Enter some text')}"
      echo "P3: ${outcome.get('Enter a password')}"
      echo "P4: ${outcome.get('Take your pick')}"
    }
    
  }
}
stage('Finally') {
  steps {
    script {
      def outcome3 = input id: 'Run-test-suites',
      message: 'Workflow Configuration',
      ok: 'Okay',
      parameters: [
        [
          $class: 'ChoiceParameterDefinition', choices: 'Deploy\nAbort\nUndecided',
          name: 'Decision',
          description: 'A select box option'
        ]
      ]
      
      echo "P4: ${outcome3}"
    }
    
  }
}
stage('Last') {
  steps {
    script {
      def userInput = true
      def didTimeout = false
      try {
        timeout(time: 120, unit: 'SECONDS') { // change to a convenient timeout for you
        userInput = input(
          id: 'DeployToProd',
          message: 'Would you like to deploy this to production?',
          ok: 'Deploy',
          parameters: [
            [$class: 'ChoiceParameterDefinition', choices: 'Deploy\nAbort\nUndecided', name: 'Please indicate your decision.', description: 'Someone must approve production deployments']
          ]
        )
      }
    } catch(err) { // timeout reached or input false
    def user = err.getCauses()[0].getUser()
    echo "User: [${user}]"
    
    if('SYSTEM' == user.toString()) { // SYSTEM means timeout.
    didTimeout = true
  } else {
    userInput = false
    echo "Aborted by: [${user}]"
  }
}
echo "userInput [${userInput}]"
echo "didTimeout [${didTimeout}]"

if (userInput == "Deploy") {
  sh 'echo \'Deploying...\''
} else if (userInput == "Abort") {
  echo "The user decided to abort deployment..."
  currentBuild.result = 'ABORTED'
  
} else if (userInput == "Undecided") {
  echo "A user was undecided about a production deployment."
  currentBuild.result = 'NOT_BUILT'
} else {
  echo "Timed out at deploy decision."
  currentBuild.result = 'UNSTABLE'
}

echo "Build Result : [${currentBuild.result.toString()}]"
}

}
}
}
}