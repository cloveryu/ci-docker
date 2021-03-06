def gitUrl = 'https://github.com/codecentric/conference-app'

createCiJob("conference-app", gitUrl, "app/pom.xml")
createDockerBuildJob("conference-app", "app")
createDockerStartJob("conference-app", "app", "18080")
createDockerStopJob("conference-app", "app")

def createCiJob(def jobName, def gitUrl, def pomFile) {
  job("${jobName}-1-ci") {
    parameters {
      stringParam("BRANCH", "master", "Define TAG or BRANCH to build from")
      stringParam("REPOSITORY_URL", "http://nexus:8081/nexus/content/repositories/releases/", "Nexus Release Repository URL")
    }
    scm {
      git {
        remote {
          url(gitUrl)
        }
        createTag(false)
        clean()
      }
    }
    wrappers {
      colorizeOutput()
      preBuildCleanup()
    }
    triggers {
      scm('H/30 * * * *')
      githubPush()
    }
    steps {
      maven {
          goals('clean versions:set -DnewVersion=DEV-\${BUILD_NUMBER}')
          mavenInstallation('Maven 3.3.3')
          rootPOM( pomFile )
          mavenOpts('-Xms512m -Xmx1024m')
          providedGlobalSettings('MyGlobalSettings')
      }
      maven {
        goals('clean deploy')
        mavenInstallation('Maven 3.3.3')
        rootPOM(pomFile)
        mavenOpts('-Xms512m -Xmx1024m')
        providedGlobalSettings('MyGlobalSettings')
      }
    }
    publishers {
      chucknorris()
      archiveJunit('**/target/surefire-reports/*.xml')
      publishCloneWorkspace('**', '', 'Any', 'TAR', true, null)
      downstreamParameterized {
        trigger("${jobName}-2-docker-build") {
          currentBuild()
        }
      }
    }
  }
}

def createDockerBuildJob(def jobName, def folder) {

  println "############################################################################################################"
  println "Creating Docker Build Job for ${jobName} "
  println "############################################################################################################"

  job("${jobName}-2-docker-build") {
    logRotator {
        numToKeep(10)
    }
    scm {
      cloneWorkspace("${jobName}-1-ci")
    }
    steps {
      steps {
        shell("cd ${folder} && sudo /usr/bin/docker build -t conference-${folder} .")
      }
    }
    publishers {
      chucknorris()
      downstreamParameterized {
        trigger("${jobName}-3-docker-start-container") {
          currentBuild()
        }
      }
    }
  }
}

def createDockerStartJob(def jobName, def folder, def port) {

  println "############################################################################################################"
  println "Creating Docker Start Job for ${jobName} "
  println "############################################################################################################"

  job("${jobName}-3-docker-start-container") {
    logRotator {
        numToKeep(10)
    }
    steps {
      steps {
        shell('echo "Starting Docker Container"')
        shell("sudo /usr/bin/docker run -d --name conference-${folder} -p=${port}:8080 conference-${folder}")
      }
    }
    publishers {
      chucknorris()
    }
  }
}

def createDockerStopJob(def jobName, def folder) {

  println "############################################################################################################"
  println "Creating Docker Stop Job for ${jobName} "
  println "############################################################################################################"

  job("${jobName}-4-docker-stop-container") {
    logRotator {
        numToKeep(10)
    }
    steps {
      steps {
        shell('echo "Stopping Docker Container"')
        shell("sudo /usr/bin/docker stop \$(sudo /usr/bin/docker ps -a -q --filter=\"name=conference-${folder}\")")
        shell("sudo /usr/bin/docker rm \$(sudo /usr/bin/docker ps -a -q --filter=\"name=conference-${folder}\")")
      }
    }
    publishers {
      chucknorris()
    }
  }
}

buildPipelineView('Pipeline') {
    filterBuildQueue()
    filterExecutors()
    title('Conference App CI Pipeline')
    displayedBuilds(5)
    selectedJob("conference-app-1-ci")
    alwaysAllowManualTrigger()
    refreshFrequency(60)
}

listView('Conference App') {
    description('')
    filterBuildQueue()
    filterExecutors()
    jobs {
        regex(/conference-app-.*/)
    }
    columns {
        status()
        buildButton()
        weather()
        name()
        lastSuccess()
        lastFailure()
        lastDuration()
    }
}
