# AGENTS.md

Guidance for AI coding agents working in the Akar repository.

## Mission

Akar is a Clojure pattern matching library built around a small core idea:

- A pattern is an ordinary function from a value to either a sequence of emissions or `nil`.
- Clauses and syntax are layered on top of that runtime model.
- Simplicity, composability, and first-class abstractions matter more here than clever machinery.

When making changes, preserve that spirit. Prefer small, explicit, orthogonal building blocks over special cases.

## Repository Map

- `akar-core`: runtime pattern primitives, combinators, syntax macros, and tests.
- `akar-commons`: shared helpers, including variadic combinator helpers and trampoline support.
- `akar-exceptions`: exception-oriented helpers built in the same style as the core syntax layer.
- `README.md`, `TUTORIAL.md`, `FAQs.md`, `GOTCHAS.md`, `CHANGES.md`: user-facing documentation.

Read the relevant subproject before editing it. `akar.syntax` depends heavily on Panini and `clojure.spec`, so do not treat it like ordinary macro code without understanding the grammar layer it uses.

## Ground Rules

- Never create Git commits unless the human explicitly asks.
- Never push to remotes.
- Use `git --no-pager ...` for Git reads.
- Prefer `rg` for searching.
- Keep code self-documenting. Add comments only when they explain intent or a non-obvious constraint.
- Follow idiomatic Clojure style and prefer core functions over custom machinery.
- Avoid introducing mutable state unless there is a compelling, project-consistent reason.

## MCP and REPL Workflow

This repository is configured to use `clojure-mcp` with Codex.

- Prefer `clojure-mcp` tools for Clojure project inspection, REPL-oriented validation, and Clojure-aware editing when those tools are available in the current Codex session.
- At the start of substantive Clojure work, inspect the project with `clojure-mcp` before making assumptions about namespaces, vars, or REPL state.
- When changing behavior, prefer validating the change in the REPL in addition to running the test suite.
- Treat `clojure-mcp` as complementary to shell tools, not a total replacement. Use ordinary shell commands for fast file discovery and repo-wide search.
- The project-local `.clojure-mcp/config.edn` disables the MCP bash tool on purpose. Use Codex shell tools for bash commands and `clojure-mcp` for Clojure-aware operations.

If the current Codex session does not expose `clojure-mcp` tools, fall back gracefully to shell commands and Leiningen, but continue to follow a REPL-driven workflow where practical.

## Design Expectations

### First-class patterns

In `akar-core`, preserve the distinction between:

- runtime patterns in `patterns.clj` and `combinators.clj`
- clause execution in `primitives.clj`
- syntax compilation in `syntax.clj`

Do not solve syntax-level problems by complicating the runtime model unless that tradeoff is clearly worth it.

### Emissions and failure

The core runtime convention is important and should remain consistent:

- `nil` means match failure
- a sequence means match success
- an empty sequence means success with no emissions

New patterns and combinators should fit that contract cleanly.

### Syntax layer

`akar.syntax` is a Panini-powered grammar, not an ad hoc macro parser.

- Keep new syntax rules declarative.
- Prefer extending existing grammar patterns over hand-parsing forms.
- Preserve binding validation behavior such as duplicate-binding checks and `:or` binding restrictions.
- Keep macroexpansions aligned with the runtime primitives rather than reimplementing semantics in macro code.

### Functional style

Favor composition over branching-heavy implementations. For this codebase, a good solution is usually one that makes the model easier to reason about, not one that hides complexity behind magic.

## Subproject Notes

### `akar-core`

- `patterns.clj` should contain simple, reusable pattern functions.
- `combinators.clj` should compose patterns without changing the core contract.
- `syntax.clj` should translate user-facing forms into the same runtime pieces used directly by hand.
- Be careful with nested matching, emitted value ordering, and arity expectations between patterns and clause actions.

### `akar-commons`

- Keep helpers general-purpose.
- `trampoline.clj` supports `defn-trampolined`; changes here should be conservative and well-tested because they affect recursion semantics.

### `akar-exceptions`

- Keep it stylistically aligned with the rest of Akar: syntax should compile into ordinary runtime functions with clear data flow.

## Documentation

Update docs when behavior changes in a user-visible way.

Common places to update:

- `README.md` for public-facing overview or examples
- `TUTORIAL.md` for conceptual or teaching changes
- `FAQs.md` or `GOTCHAS.md` for semantics, tradeoffs, and caveats
- `CHANGES.md` for release-facing notes

If you change syntax, examples, or semantics, verify that the examples in docs still reflect reality.

## Testing

After code changes, run the full test suite from the repo root:

```sh
lein test
```

Also add or update focused tests in the affected subproject. Prefer tests that protect semantics, especially around:

- emission ordering
- nested patterns via `!further` and `!further-many`
- rest patterns in sequence syntax
- macroexpansion shape in `syntax_test.clj`
- trampoline behavior and large recursive inputs
- exception matching behavior in `akar-exceptions`

## Change Discipline

Before making a potentially breaking change, stop and verify the intent with a human if any of the following are true:

- it changes a public macro or pattern contract
- it changes emission order or binding count
- it changes failure behavior from `nil` to success or vice versa
- it introduces a new special case into the syntax layer

Small, principled extensions are welcome. Silent semantic drift is not.

## Practical Workflow

1. Read the relevant namespace and its tests first.
2. If `clojure-mcp` is available, inspect the project and relevant namespaces with it before editing.
3. Trace how the feature is represented at runtime, in syntax, and in docs.
4. Make the smallest coherent change.
5. Validate in the REPL when practical, especially for macroexpansion, emitted values, and syntax behavior.
6. Update docs if the change is user-facing.
7. Run `lein test`.

## If Unsure

State what you know, what you are inferring, and what you have verified. This project values clarity and honesty over bluffing.
