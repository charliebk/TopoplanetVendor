import { ChildProcessWithoutNullStreams, spawn } from 'child_process'
import { existsSync } from 'fs'
import log from 'electron-log/main'
import { resolve } from 'path'
import Constants from './utils/Constants'

let backendProcess: ChildProcessWithoutNullStreams | null = null

const BACKEND_HEALTH_RETRY_COUNT = 20
const BACKEND_HEALTH_RETRY_INTERVAL_MS = 500

const backendWorkingDirectory = resolve(process.cwd(), '../backend')

const sleep = (ms: number): Promise<void> =>
  new Promise((resolvePromise) => setTimeout(resolvePromise, ms))

const resolveJava21Home = (): string | null => {
  if (process.platform !== 'win32') {
    return null
  }

  const java21Path = resolve(
    'C:/Program Files/Eclipse Adoptium/jdk-21.0.10.7-hotspot'
  )

  return existsSync(java21Path) ? java21Path : null
}

const createBackendEnv = (): NodeJS.ProcessEnv => {
  const env: NodeJS.ProcessEnv = {
    ...process.env
  }

  const javaHome = resolveJava21Home()
  if (javaHome) {
    env.JAVA_HOME = javaHome
    env.PATH = `${resolve(javaHome, 'bin')};${process.env.PATH ?? ''}`
  }

  return env
}

const checkBackendHealth = async (): Promise<boolean> => {
  try {
    const response = await fetch(Constants.BACKEND_HEALTH_URL)
    return response.ok
  } catch {
    return false
  }
}

const startBackendProcess = (): void => {
  if (!existsSync(backendWorkingDirectory)) {
    log.warn(`Backend folder not found at ${backendWorkingDirectory}`)
    return
  }

  const env = createBackendEnv()

  if (process.platform === 'win32') {
    backendProcess = spawn('cmd.exe', ['/c', 'gradlew.bat run'], {
      cwd: backendWorkingDirectory,
      env
    })
  } else {
    backendProcess = spawn('./gradlew', ['run'], {
      cwd: backendWorkingDirectory,
      env
    })
  }

  backendProcess.stdout.on('data', (chunk) => {
    log.info(`[backend] ${chunk.toString().trimEnd()}`)
  })

  backendProcess.stderr.on('data', (chunk) => {
    log.warn(`[backend] ${chunk.toString().trimEnd()}`)
  })

  backendProcess.on('exit', (code) => {
    log.warn(`[backend] exited with code ${code ?? 'unknown'}`)
    backendProcess = null
  })
}

export const ensureBackendReady = async (): Promise<void> => {
  if (!Constants.IS_DEV_ENV) {
    return
  }

  const alreadyHealthy = await checkBackendHealth()
  if (alreadyHealthy) {
    log.info('[backend] already healthy, skip startup')
    return
  }

  if (!backendProcess) {
    startBackendProcess()
  }

  for (let attempt = 1; attempt <= BACKEND_HEALTH_RETRY_COUNT; attempt += 1) {
    await sleep(BACKEND_HEALTH_RETRY_INTERVAL_MS)

    const healthy = await checkBackendHealth()
    if (healthy) {
      log.info('[backend] healthy and ready')
      return
    }
  }

  log.warn('[backend] health check did not become ready in time')
}

export const stopBackendProcess = (): void => {
  if (!backendProcess) {
    return
  }

  backendProcess.kill()
  backendProcess = null
}
