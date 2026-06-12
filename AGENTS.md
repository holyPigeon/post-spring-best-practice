# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working in this repository.

## Canonical Documents

`AGENTS.md` is a short router. Keep shared project rules in `agent-reference/` so Codex and Claude Code use the same source of truth.

| Topic | File |
|---|---|
| Project environment, build/run/test commands, stack, architecture, database | `agent-reference/project-context.md` |
| Java/Spring coding style | `agent-reference/coding-style.md` |
| Testing style | `agent-reference/testing-style.md` |
| Detailed test examples | `agent-reference/testing-examples.md` |
| Commit convention | `agent-reference/commit-convention.md` |
| Agent rule and document writing style | `agent-reference/documentation-style.md` |

## Task Routing

Before starting a task, read only the documents that match the work.

| Task type | Required documents |
|---|---|
| Java/Spring code writing, modification, refactoring, package moves | `agent-reference/project-context.md`, `agent-reference/coding-style.md` |
| Test writing, test modification, test failure analysis | `agent-reference/project-context.md`, `agent-reference/testing-style.md` |
| Non-trivial test examples or fixture patterns needed | `agent-reference/testing-examples.md` |
| Build, run, Gradle, Docker, profile, database work | `agent-reference/project-context.md` |
| Commit message writing, commit creation, change summary | `agent-reference/commit-convention.md` |
| Agent rule or `agent-reference/` document updates | `agent-reference/documentation-style.md` |

If multiple task types apply, read all matching documents.

Priority order:

1. User request
2. Required environment and architecture rules in `agent-reference/project-context.md`
3. Task-specific `agent-reference/*.md` rules
4. Existing codebase consistency

Do not treat `.claude/settings*.json` as Codex work rules.
