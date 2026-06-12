# Documentation Style

Use this file only when updating `AGENTS.md` or files under `agent-reference/`.

## Agent Rule Writing

Keep default context short. Put rules where the agent already reads them for that task, and move detailed rationale or examples to optional documents.

Prefer decision-oriented rules over broad principles. When practical, write rules with:

- Intent: why the rule exists
- Prefer or avoid: the default choice
- Apply when: concrete conditions
- Exceptions: when the opposite choice is acceptable
- Review question: what the agent should ask before applying the rule

Avoid vague instructions such as `clean`, `safe`, `proper`, or `not excessive` unless the rule also gives criteria.

## Document Placement

- Keep `AGENTS.md` as a router.
- Keep task-critical rules in the smallest relevant `agent-reference/*.md` file.
- Keep long examples in optional example files and route them only when needed.
- Do not add a new always-read document when a short tiebreaker in an existing routed document is enough.
