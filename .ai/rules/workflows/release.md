# Release Checklist
**Related**: [security.md](../mobile/security.md) | [constitution.md](../constitution.md) | [sdd-workflow.md](./sdd-workflow.md) | [environment-configuration.md](../../specs/infrastructure/environment-configuration.md)

---

## Purpose

Define the mandatory pre-release checklist. No build is submitted to the App Store or Google Play without completing every item. This is a blocking checklist — partial completion means no release.

---

## Version Bumping

```yaml
# pubspec.yaml
# Format: MAJOR.MINOR.PATCH+BUILD_NUMBER
# MAJOR: Breaking changes (rare)
# MINOR: New features
# PATCH: Bug fixes
# BUILD_NUMBER: Auto-incremented by CI (never manual)

version: 1.2.3+45   # Example: version 1.2.3, build 45
```

```bash
# Bump version (use the release script):
# Minor release (new features):
dart run scripts/bump_version.dart --minor

# Patch release (bug fixes):
dart run scripts/bump_version.dart --patch

# Never manually edit the build number — CI handles it
```

---

## Release Types

| Type | Version Change | Example |
|---|---|---|
| Major | `2.0.0` | Breaking API changes, full redesign |
| Minor | `1.1.0` | New features, backwards compatible |
| Patch | `1.0.1` | Bug fixes only |
| Hotfix | `1.0.1` (urgent patch) | Critical production bug |

---

## Pre-Release Checklist

### Code Quality

- [ ] `flutter analyze --fatal-infos --fatal-warnings` → **zero** errors or warnings.
- [ ] `dart run custom_lint` → **zero** import violations.
- [ ] `flutter test --coverage` → overall coverage **≥ 80%**.
- [ ] All golden tests updated if UI changed: `flutter test --update-goldens`.
- [ ] No `TODO`, `FIXME`, or `HACK` comments in code going to release.
- [ ] No `debugPrint` or `log` statements with sensitive data.

### Security

- [ ] `flutter pub audit` → zero high-severity vulnerabilities.
- [ ] Certificate pinning is active in release build configuration.
- [ ] All environment secrets are injected via `--dart-define-from-file` (CI verified).
- [ ] No hardcoded secrets: `grep -rn "api_key\|password\|secret" lib/` → zero results.
- [ ] `kReleaseMode` checks are in place for debug-only features (logging, test overrides).

### Environment Validation

```dart
// Verify in main() — fails fast if misconfigured
void main() {
  WidgetsFlutterBinding.ensureInitialized();
  AppConfig.validate(); // Throws AssertionError if any env var is missing
  runApp(const App());
}
```

- [ ] `AppConfig.validate()` passes in release build.
- [ ] Release build points to **production** API URL (not staging).
- [ ] Firebase/Sentry DSN is set to production project.
- [ ] `.env.example` completeness verified (no missing variables from source code).
- [ ] Missing variables detection passed (all keys in `.env.example` match schema).
- [ ] Secret validation confirmed (no leaked credentials, only `<CHANGE_ME>` placeholders in `.env.example`).
- [ ] `SERVER_PORT` conflict validation verified across ecosystem.
- [ ] Deployment readiness verified for backend environments.

### Functionality

- [ ] All features in this release manually tested on:
  - [ ] Android physical device (latest OS)
  - [ ] Android emulator (minimum supported API level: 24)
  - [ ] iOS physical device (latest OS)
  - [ ] iOS simulator (minimum supported: iOS 14)
- [ ] Deep links tested end-to-end (Android App Links + iOS Universal Links).
- [ ] Offline mode tested: app works without network connection.
- [ ] Auth flow tested: login, logout, token refresh, session expiry.
- [ ] Push notifications tested (if applicable).

### Store Metadata

- [ ] `pubspec.yaml` version bumped correctly.
- [ ] **Changelog** updated in `CHANGELOG.md` with all changes since last release.
- [ ] App Store / Google Play release notes written in English (and Spanish if applicable).
- [ ] App Store screenshots updated if UI changed.
- [ ] `minSdkVersion` (Android) and `iOS Deployment Target` match documented minimum.

### Build Validation

```bash
# Android release build
flutter build appbundle --release \
  --dart-define-from-file=.env.prod.json \
  --obfuscate \
  --split-debug-info=build/debug-info/android

# iOS release build
flutter build ipa --release \
  --dart-define-from-file=.env.prod.json \
  --obfuscate \
  --split-debug-info=build/debug-info/ios

# Verify build succeeds with zero warnings
```

- [ ] Android App Bundle builds without errors or warnings.
- [ ] iOS IPA builds without errors or warnings.
- [ ] `--obfuscate` flag used in release builds.
- [ ] `--split-debug-info` configured and debug symbols uploaded to Sentry/Firebase.

### CI/CD

- [ ] All CI checks pass on the release branch.
- [ ] Release branch is based on `main` with no uncommitted changes.
- [ ] PR merged with squash commit using conventional commit format.

---

## Changelog Format

```markdown
# Changelog

## [1.2.0] — 2026-06-01

### Added
- Check-in workflow with digital signature capture
- Room availability calendar view
- Push notifications for booking confirmations

### Changed
- Improved room detail screen layout for tablets
- Updated payment flow to support 3DS authentication

### Fixed
- Room status not updating after check-out (#234)
- Offline mode crashing when sync queue is empty (#256)

### Security
- Updated Dio to 5.4.0 (CVE-2024-XXXX mitigation)
```

---

## Post-Release

- [ ] Tag the release in git: `git tag -a v1.2.0 -m "Release 1.2.0"`
- [ ] Create GitHub Release with changelog content.
- [ ] Upload debug symbols to crash reporting tool.
- [ ] Monitor crash rates in Sentry/Firebase for 24h post-release.
- [ ] Close all fixed issues in the issue tracker.

---

## Agent Verification Checklist

- [ ] `pubspec.yaml` version string is updated to the correct semantic version.
- [ ] `CHANGELOG.md` has a new entry at the top for this release.
- [ ] `flutter analyze` and `flutter test` both pass with no failures.
- [ ] Build commands include `--obfuscate` and `--dart-define-from-file=.env.prod.json`.
- [ ] `AppConfig.validate()` is confirmed to pass in release configuration.
- [ ] `flutter pub audit` returns no high-severity vulnerabilities.
- [ ] Release notes are written in plain language (not technical jargon) for store submission.
- [ ] No staging API URLs remain in production build configuration.
- [ ] Debug symbols are generated and stored alongside the release build.
- [ ] Git tag is created with format `v{MAJOR}.{MINOR}.{PATCH}` after successful release.
