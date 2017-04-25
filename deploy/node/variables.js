const Exec = require('child_process').execSync

const gitHash = () => {
  return Exec('git rev-parse HEAD').toString().trim()
}

module.exports = {
  AWS_ACCOUNT_ID: process.env.AWS_ACCOUNT_ID || 886403637725,
  APP_NAME: process.env.APP_NAME || 'central-kms',
  AWS_REGION: process.env.AWS_REGION || 'us-west-2',
  VERSION: process.env.CIRCLE_TAG || gitHash(),
  API: {
    NAME: 'central-kms',
    IMAGE: process.env.API_IMAGE || 'leveloneproject/central-kms',
    PORT: process.env.API_PORT || 8080
  },
  CLUSTER: process.env.CLUSTER || 'central-services',
  DOCKER_EMAIL: process.env.DOCKER_EMAIL,
  DOCKER_USER: process.env.DOCKER_USER,
  DOCKER_PASS: process.env.DOCKER_PASS,
  HOSTNAME: process.env.HOSTNAME || 'http://central-kms-000000000.us-west-2.elb.amazonaws.com',
  JFROG_REPO: process.env.JFROG_REPO || 'modusbox-level1-docker-release.jfrog.io',
  POSTGRES_USER: process.env.DEV_POSTGRES_USER,
  POSTGRES_PASSWORD: process.env.DEV_POSTGRES_PASSWORD,
  POSTGRES_HOST: process.env.DEV_POSTGRES_HOST
}
