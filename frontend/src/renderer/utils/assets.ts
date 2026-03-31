import { APP_ASSET_PREFIXES, APP_BRANDING } from '@/shared/branding'

export const RENDERER_ASSETS = {
  icon: `/${APP_BRANDING.assets.icon}`,
  trayIcon: `/${APP_BRANDING.assets.trayIcon}`,
  logo: `/${APP_BRANDING.assets.logo}`
} as const

export const RENDERER_ASSET_PREFIXES = APP_ASSET_PREFIXES
