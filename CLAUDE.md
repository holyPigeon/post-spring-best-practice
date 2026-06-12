# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working in this repository.

## Canonical Documents

`CLAUDE.md` is a short router. Keep shared project rules in `docs/agent/` so Claude Code and Codex use the same source of truth.

| Topic | File |
|---|---|
| Project environment, build/run/test commands, stack, architecture, database | `docs/agent/project-context.md` |
| Java/Spring coding style | `docs/agent/coding-style.md` |
| Testing style | `docs/agent/testing-style.md` |
| Detailed test examples | `docs/agent/testing-examples.md` |
| Commit convention | `docs/agent/commit-convention.md` |
| Agent rule and document writing style | `docs/agent/documentation-style.md` |

## Task Routing

Before starting a task, read only the documents that match the work.

| Task type | Required documents |
|---|---|
| Java/Spring code writing, modification, refactoring, package moves | `docs/agent/project-context.md`, `docs/agent/coding-style.md` |
| Test writing, test modification, test failure analysis | `docs/agent/project-context.md`, `docs/agent/testing-style.md` |
| Non-trivial test examples or fixture patterns needed | `docs/agent/testing-examples.md` |
| Build, run, Gradle, Docker, profile, database work | `docs/agent/project-context.md` |
| Commit message writing, commit creation, change summary | `docs/agent/commit-convention.md` |
| Agent rule or `docs/agent/` document updates | `docs/agent/documentation-style.md` |

If multiple task types apply, read all matching documents.

Priority order:

1. User request
2. Required environment and architecture rules in `docs/agent/project-context.md`
3. Task-specific `docs/agent/*.md` rules
4. Existing codebase consistency

Do not treat `.claude/settings*.json` or `docs/guide.md` as Claude Code work rules unless the user explicitly asks to inspect those files.
