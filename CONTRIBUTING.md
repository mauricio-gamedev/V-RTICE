# Contributing

## Workflow

1. Create a focused branch from the current main.
2. Make a small, reviewable change.
3. Add or update tests.
4. Run the relevant Gradle tasks.
5. Update documentation and docs/SESSION_CHECKPOINT.md.
6. Open a pull request with scope, risk, validation evidence, and screenshots when UI changes.

## Branch names

- feat/<short-description>
- fix/<short-description>
- refactor/<short-description>
- docs/<short-description>

## Commit messages

Use short imperative messages with a scope where useful, for example:

- feat(android): add circles list state
- test(backend): cover friendship permissions
- docs: record media authorization decision

## Definition of done

A change is not complete because it compiles. It must handle loading, empty, success, and failure states where applicable; enforce authorization on the backend; include relevant tests; avoid leaking secrets; and document known limitations.
