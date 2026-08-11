# Contributing to CultivOS

## Branch Strategy

```
main          ← production-ready, protected
develop       ← integration branch, all PRs target here (default)
feature/*     ← feature/STORY-XXX-short-description
hotfix/*      ← hotfix/ISSUE-XXX-short-description
release/*     ← release/v1.0.0
```

## Commit Convention (Conventional Commits)

```
feat(identity): add JWT refresh token endpoint
fix(ai-crop): handle null soil pH values
chore(ci): update GitHub Actions to v4
docs(api): add field-registry OpenAPI spec
test(marketplace): add unit tests for bid service
refactor(gis): extract PostGIS query builder
```

Format: `type(scope): description`
Types: `feat`, `fix`, `chore`, `docs`, `test`, `refactor`, `perf`, `ci`

## Pull Request Process

1. Branch from `develop`: `git checkout -b feature/STORY-XXX-description`
2. Make atomic commits following the convention above
3. Open PR targeting `develop`
4. All CI checks must pass (build, test, lint, Docker)
5. At least 1 approving review required
6. Squash merge to keep history clean

## Definition of Done

- [ ] Unit test coverage ≥ 80%
- [ ] All CI checks pass
- [ ] API changes reflected in OpenAPI spec
- [ ] Docker image builds successfully
- [ ] No secrets committed
- [ ] PR reviewed and approved
- [ ] Linked issue updated / closed

## Code Style

- **Java**: Google Java Style Guide (Checkstyle)
- **Python**: PEP 8 via Ruff
- **TypeScript**: ESLint + Prettier
- **Dart/Flutter**: `flutter analyze`
