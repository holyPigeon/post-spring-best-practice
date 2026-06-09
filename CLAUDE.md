# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

## Canonical Documents

`CLAUDE.md` is a short router. Keep shared project rules in `agent-reference/` so Claude Code and Codex use the same source of truth.

| Topic | File |
|---|---|
| Project environment, build/run/test commands, stack, architecture, database | `agent-reference/project-context.md` |
| Java/Spring coding style | `agent-reference/coding-style.md` |
| Testing style | `agent-reference/testing-style.md` |
| Detailed test examples | `agent-reference/testing-examples.md` |
| Commit convention | `agent-reference/commit-convention.md` |

## Task Routing

Before starting a task, read only the documents that match the work.

| Task type | Required documents |
|---|---|
| Java/Spring code writing, modification, refactoring, package moves | `agent-reference/project-context.md`, `agent-reference/coding-style.md` |
| Test writing, test modification, test failure analysis | `agent-reference/project-context.md`, `agent-reference/testing-style.md` |
| Non-trivial test examples or fixture patterns needed | `agent-reference/testing-examples.md` |
| Build, run, Gradle, Docker, profile, database work | `agent-reference/project-context.md` |
| Commit message writing, commit creation, change summary | `agent-reference/commit-convention.md` |

If multiple task types apply, read all matching documents.

Priority order:

1. User request
2. Required environment and architecture rules in `agent-reference/project-context.md`
3. Task-specific `agent-reference/*.md` rules
4. Existing codebase consistency

`agent-reference/*.md` files are the single source of truth for shared project rules.
