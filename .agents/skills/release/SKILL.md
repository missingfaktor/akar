---
name: release
description: Cut a release for this project — bump the version in project.clj, commit, push, create a GitHub release, and deploy to Clojars. Use this skill whenever the user asks to release, publish, cut a version, or ship to Clojars.
---

# Release

## Workflow

1. Read the current version from `project.clj`. Infer the bump type from context, or ask if unclear. Confirm the new version with the user before touching anything.
2. Update the version in `project.clj`. Commit (using the git-commit skill) and push.
3. Run `gh release create vX.Y.Z --title "vX.Y.Z" --notes "..."`. Derive release notes from `git log <prev-tag>..HEAD --oneline`.
4. Run `lein deploy clojars`.

## Guardrails

- Don't bump or tag until the user confirms the version.
- Don't deploy to Clojars before the GitHub release is in place.
- Don't release from a dirty working tree.
