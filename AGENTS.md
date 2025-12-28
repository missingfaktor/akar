# AGENTS.md

This file provides guidelines for AI agents assisting with development on the Akar project, a Clojure library for pattern matching and syntax utilities. Follow these rules to ensure contributions align with the project's functional programming ethos and community standards.

## Guidelines

- Never construct Git commits on my behalf.
- Never push changes to remote repositories.
- Use `--no-pager` when running Git commands to avoid getting stuck in pagers.
- Aspire for the code to be self-documenting to the extent possible.
- Only add comments after exhausting all avenues for self-documenting code. Comments should typically address "why" aspects.
- Follow the [Clojure Style Guide](https://github.com/bbatsov/clojure-style-guide) for naming, formatting, and structure.
- Prioritize functional programming: avoid mutable state, use pure functions, and leverage Clojure's immutability.
- Ensure code is idiomatic Clojure; prefer core functions over custom implementations.
- Update relevant documentation (e.g., `README.md`, `TUTORIAL.md`, or `CHANGES.md`) for user-facing changes, such as new patterns or API modifications.
- Ensure examples in documentation remain accurate and demonstrate best practices.

## Testing

- Always run the full test suite (using `lein test`) after making changes to verify functionality.
- Add or update tests for new features, bug fixes, or pattern changes. Use clojure.test or consider Midje for better readability.
- Include tests for edge cases in pattern matching, such as trampolining for recursion in akar-core.

## Collaboration

- Ask for clarification from human maintainers if a task is ambiguous or involves significant changes.
- Explain the reasoning behind suggestions, especially for complex pattern logic or syntax modifications.
- Reference GitHub issues (e.g., from `CONTRIBUTING.md`) for context and propose changes via pull requests rather than direct edits.

## Security

- Never expose sensitive information, API keys, or credentials in code or suggestions.
- Avoid suggesting changes that could introduce vulnerabilities, such as insecure pattern matching in user inputs.

## Project-Specific Rules

- When working on `akar-core` or `akar-syntax`, ensure patterns are composable, efficient, and follow the library's trampoline-based recursion model.
- For `akar-commons`, maintain utility functions as general-purpose and well-tested.
- Avoid breaking changes to public APIs without discussion; reference existing issues (e.g., #9 for ClojureScript support).
- Use Leiningen for builds and ensure compatibility across Clojure versions.
