# Shared Frontend Assets

This folder stores shared visual assets for the whole application.

Stable filenames:

- `icons/app-icon.png`: generic application icon for frontend usage.
- `icons/app-tray-icon.png`: tray icon consumed by Electron main.
- `images/app-logo.webp`: main application logo for renderer views.

Canonical prefixes for future shared graphics:

- `empty-state-*`: empty screens, zero-data illustrations, onboarding placeholders.
- `tutorial-*`: tutorial covers, guided walkthrough graphics, help visuals.
- `module-*`: module-level graphics, banners or module identifiers.
- `brand-*`: branding variations derived from the core application identity.

Rule:

- Replace the file contents when you want to change the icon or logo.
- Keep the same filename and expected dimensions whenever possible.
- Add new shared graphics under `icons/` or `images/` instead of scattering files across modules.
- Prefer canonical prefixes when adding new shared illustrations so naming stays predictable.

Packaging alignment:

- Electron packaging icons live in `frontend/buildAssets/icons/`.
- Canonical packaging names mirror runtime naming:
  - `app-icon.png`
  - `app-icon.ico`
  - `app-icon.icns`
