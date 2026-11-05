## Summary

<!-- Describe what this PR changes and why. -->

## Changes

<!-- List the important implementation changes. -->

-
-
-

## Verification

<!-- Describe how the changes were tested. Include commands and relevant results. -->

- [ ] Tests added or updated where needed.
- [ ] Relevant tests pass (`./gradlew test`).
- [ ] Build succeeds (`./gradlew build`).

## Compatibility and safety

<!-- Complete the relevant items. Explain any item that does not apply. -->

- [ ] Existing public API behavior remains compatible, or breaking changes are documented.
- [ ] Coroutine scopes, cancellations, and workflow lifecycles are handled safely.
- [ ] Integration modules (`kiwi-flow-spring-boot-starter`, `kiwi-flow-ktor`) function correctly without bleeding dependencies into Core.
- [ ] Registry state and Bootstrap lifecycles are completely isolated in test environments.
- [ ] No unrelated changes are included.

## Documentation

- [ ] Documentation, KDoc, and examples are updated where needed.
- [ ] No documentation changes are needed.

## Related issues

<!-- Link related issues, e.g. Closes #123. -->