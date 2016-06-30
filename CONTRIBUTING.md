# Contributing to Akar

Thank you for your interest in contributing to this library.

If you wish to help out with things on our [tasks](#tasks) list, or wish to contribute a new feature to the library, or wish to fix a bug, please create a Github issue, and start a discussion. If/once assigned, you can work on the issue independently, and submit a PR once you have made sure that the [contribution guidelines](#guidelines) for this project are met.

## Tasks 

0. Migrate tests to a better testing library (potentially [Midje](https://github.com/marick/Midje)).
0. Rewrite some tests to achieve better consistency.
0. Define new generally useful patterns.
0. Set up a multi-artifact Leiningen project. We have plans to write a few satellite libraries in future, and it would be nice if they could all be housed under the same repository.
0. Add `core.typed` annotations. These should ideally come as a separate artifact.
0. Release for ClojureScript.
0. Improve error messages.
0. Migrate to `core.spec`. (Once it's stable.)
0. General code improvements.

## Guidelines

### Pull Requests
 
0. Always make the PR against the `master` branch. Except when it's a hotfix, in which case it should be made against the latest release branch.  
0. Add a prefix `[WIP]` to a PR's title until it's ready for review.
0. Break the changes into small commits.
0. Ensure a clean Git history before submitting a PR. Rebase judiciously.
0. Ensure that the PR is up-to-date with the target branch.
0. Include a reference to the relevant Github issue.
0. Add a proper description of the work to the PR. 
0. Include failing tests which pass with the new changes. 
0. Include documentation where it makes sense.
0. Add the change to the "Unreleased" section of [CHANGES.md](CHANGES.md).

### Commits

0. Do not make more than one logical change per commit.
0. Try to isolate pure formatting changes from actual changes in their own commits.
0. The commit should have a descriptive subject line. Make sure that people can understand the commit when doing a `git log --oneline`.
0. The commit message should obey the following formatting guidelines:
  - Separate the subject from body with a blank line.
  - Limit the subject line to 60 characters.
  - Capitalize the subject line.
  - Do not end the subject line with a period.
  - Use the imperative mood in the subject line.
  - Wrap the body at 72 characters.
  - Use the body to explain what and why, and if required, how.