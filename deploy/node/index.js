'use strict'

const Aws = require('./aws')
const Ecr = require('./ecr')
const Jfrog = require('./jfrog')
const Variables = require('./variables')

const pushService = ({IMAGE, NAME, PORT}, version) => {
  return Ecr.pushImageToEcr(IMAGE, version)
}

const deploy = () => {
  const version = Variables.VERSION
  Aws.configureAws()
    .then(() => pushService(Variables.API, version))
    .then(() => Jfrog.login())
    .then(() => Jfrog.pushImageToJFrog(Variables.API.IMAGE, version))
    .catch(e => {
      console.error(e)
      process.exit(1)
    })
}

module.exports = deploy()
