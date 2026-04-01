# CustomDataTable Internal Versioning Policy

## Scope

`CustomDataTable` is a frozen public surface with controlled additive evolution, now stored directly under `genericDataTable`.

## Rules

1. The only supported public entrypoint is `custom-data-table.public.ts`.
2. Any new export added to that barrel must include README documentation, at least one unit test and a migration note when behavior changes.
3. Backward-compatible additions are allowed inside V1 when defaults preserve previous behavior.
4. Breaking changes do not ship silently inside V1. They require either:
   - a new folder such as `CustomDataTableV2`, or
   - an explicit migration step documented before release.
5. Files not exported from the public barrel are internal implementation details and may change freely.
6. New UI strings must be added to `generic-data-table.locale.ts` instead of being hardcoded across components.
7. New styling tokens must remain inside V1 scoped styles with safe fallbacks so copied consumers do not depend on app CSS.

## Release checklist for V1 changes

- Update unit tests.
- Update README.
- Update `PORTABILITY_CHECKLIST.md` when copy or verification steps change.
- Update `ORIGINAL_PARITY_SPRINTS.md` if the change closes a tracked sprint or follow-up.
