---
name: git-commit
description: Draft thoughtful Git commit messages that follow Chris Beams's "How to Write a Git Commit Message" guidance, and use this skill whenever the user asks to create a commit, prepare a commit message, stage-and-commit work, or clean up commit history. Always show the proposed message to the user for review before committing, prefer opening the draft in the editor, and never run `git commit` until the user explicitly approves it.
---

# Git Commit

Use this skill when the user wants help turning local changes into a Git commit.

## Goals

- Produce a commit message that follows the rules described in Chris Beams's article:
  - Separate subject from body with a blank line.
  - Keep the subject line to about 50 characters.
  - Capitalize the subject line.
  - Do not end the subject line with a period.
  - Use the imperative mood in the subject line.
  - Wrap the body at about 72 characters.
  - Write the body in ordinary declarative prose rather than imperative fragments.
  - Explain what changed and why, not just how.
- Make the review step visible and easy for the user.
- Never commit without explicit approval.

## Required workflow

1. Inspect the repo state before drafting anything.
2. Review the staged diff first. If nothing is staged, review the working tree and either:
   - ask whether the user wants specific files staged, or
   - if the user clearly asked you to prepare the whole change, stage the relevant files carefully.
3. Summarize the change in plain language for yourself before writing the commit message.
4. Draft the commit message with:
   - a subject line,
   - an optional body when more context is useful,
   - trailers at the end.
5. Write the draft to `.git/CODEX_COMMIT_EDITMSG` and show it to the user before any commit attempt.
6. Ask for review and wait. The user may suggest edits or edit the file directly.
7. Only after the user clearly approves, create the commit using the reviewed file.

## Draft file workflow

Prefer an editor-based review flow.

1. Write the proposed message to `.git/CODEX_COMMIT_EDITMSG`.
2. If an editor is available, open that file in the user's editor.
   - Prefer `$GIT_EDITOR`, then `$VISUAL`, then `$EDITOR`.
   - If no editor is configured or opening one is impractical, show the message in chat and point the user to the file path.
3. Tell the user exactly which file contains the draft.
4. After any user edits, reread the file before committing so the final commit uses the reviewed text.

## Commit message template

Use this shape unless the change is truly tiny:

```text
<Imperative subject, ~50 chars, no period>

<Explain what changed and why in ordinary declarative prose. Use
present or past tense sentences, not imperative commands. Wrap near
72 columns. Add a second paragraph if it helps the future reader
understand motivation, tradeoffs, or context.>

<AI co-author trailer — see Trailer rules below>
```

## Trailer rules

Always include a co-author trailer identifying the AI that authored the commit. Use the trailer that matches the environment you are running in:

- **Codex (OpenAI):** `Co-authored-by: Codex <codex@openai.com>`
  - If the repo or user provides a different Codex identity, prefer that updated identity.

Keep trailers after the body, separated by a blank line.

## Review prompt

After writing the draft file, explicitly ask for approval in a short form like:

`I drafted the commit message in .git/CODEX_COMMIT_EDITMSG. Please review or edit it there, and tell me when you'd like me to commit.`

Do not soften this into implied approval. Wait for a clear yes.

## Commit command

Once approved, commit with the reviewed message file:

```bash
git commit --file .git/CODEX_COMMIT_EDITMSG
```

If the user asked for an amended commit, use the same review flow first, then:

```bash
git commit --amend --file .git/CODEX_COMMIT_EDITMSG
```

## Guardrails

- Never invent details that are not supported by the diff or user context.
- Do not create a body paragraph for ceremony alone; add it when it helps explain intent, rationale, side effects, or follow-up context.
- Do not write the body as a second imperative headline or a list of command-like fragments.
- If the change mixes unrelated concerns, recommend splitting it into separate commits before drafting.
- If the diff is too large or unclear to summarize responsibly, pause and inspect more before writing.
- Never bypass the user's review step, even for a one-line commit.
