# CustomDataTable Portability Checklist

## Short recipe

1. Copy the full `genericDataTable` module contents.
2. Import only through `custom-data-table.public.ts`.
3. Ensure the target project already has Vue 3 and PrimeVue table components.
4. Run `npm run test:unit` and the focused lint command from the target project.
5. Render one client-side table and one provider-mode table before calling the migration done.

## Checklist

- The consumer imports only from `genericDataTable/custom-data-table.public`.
- No file outside `genericDataTable` is needed at runtime.
- The target project exposes PrimeVue `DataTable`, `Column`, `Button`, `Checkbox`, `Dropdown`, `InputNumber`, `InputText`, `Tag` and `Calendar` packages.
- The target project does not need app-specific CSS variables; V1 ships fallback design tokens inside its own scoped styles.
- The target project does not need app-wide locale keys for default table texts; V1 ships them in `generic-data-table.locale.ts`.
- The target project can run `npm run test:unit` with the copied sources.
- A client-side example validates columns, filters and row actions.
- A provider-mode example validates query emission, refresh and selection.
- Any project-specific wrappers stay outside the `genericDataTable` module.

## Verification command set

```powershell
npm run test:unit
npm run lint -- src/renderer/components/genericDataTable
```
