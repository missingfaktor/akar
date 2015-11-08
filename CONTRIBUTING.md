# Contributing to Akar

Thank you for your interest in contributing to this library.

If you wish to help out with things on our [tasks](#tasks) list, or wish to contribute a new feature to the library, or wish to fix a bug, please create a Github issue, and start a discussion. If/once assigned, you can work on the issue independently, and submit a PR once you have made sure that the [contribution guidelines](#guidelines) for this project are met.

## Tasks 

* Migrate tests to a better testing library (potentially [Midje](https://github.com/marick/Midje)).
* Rewrite some tests to achieve better consistency.
* Define new generally useful patterns.
* Set up a multi-artifact Leiningen project. We have plans to write a few satellite libraries in future, and it would be nice if they could all be housed under the same repository.
* Add `core.typed` annotations. These should ideally come as a separate artifact.
* Release for ClojureScript.
* Improve error messages.
* General code improvements.

## Guidelines

### Pull Requests
 
* Always make the PR against the `master` branch. Except when it's a hotfix, in which case it should be made against the latest release branch.  
* Add a prefix [WIP] to a PR's title until it's ready for review.
* Break the changes into small commits.
* Ensure a clean Git history before submitting a PR. Rebase judiciously.
* Ensure that the PR is up-to-date with the target branch.
* Include a reference to the relevant Github issue.
* Add a proper description of the work to the PR. 
* Include failing tests which pass with the new changes. 
* Include documentation where it makes sense.
* Add the change to the "Unreleased" section of [CHANGES.md](CHANGES.md).

### Commits

* Do not make more than one logical change per commit.
* Try to isolate pure formatting changes from actual changes in their own commits.
* The commit should have a descriptive subject line. Make sure that people can understand the commit when doing a `git log --oneline`.
* The commit message should obey the following formatting guidelines:
- Separate the subject from body with a blank line.
- Limit the subject line to 60 characters.
- Capitalize the subject line.
- Do not end the subject line with a period.
- Use the imperative mood in the subject line.
- Wrap the body at 72 characters.
- Use the body to explain what and why, and if required, how.