/* eslint-disable no-template-curly-in-string */
const dotenv = require('dotenv')
const packageJson = require('../../package.json')

const baseConfig = {
  productName: packageJson.name,
  appId: packageJson.appId,
  asar: true,
  extends: null,
  compression: 'maximum',
  forceCodeSigning: false,
  artifactName: '${productName} ${version}_${arch}.${ext}',
  directories: {
    output: './release/${version}'
  },
  win: {
    icon: 'buildAssets/icons/icon.ico',
    target: [
      {
        target: 'portable',
        arch: 'x64'
      }
    ]
  },
  portable: {
    artifactName: '${productName} ${version}_${arch} Portable.${ext}'
  }
}

dotenv.config()

baseConfig.copyright = `ⓒ ${new Date().getFullYear()} ${packageJson.author}`
baseConfig.files = [
  'dist/**/*',
  '!dist/main/index.dev.js',
  '!docs/**/*',
  '!tests/**/*',
  '!release/**/*'
]

module.exports = {
  ...baseConfig
}
