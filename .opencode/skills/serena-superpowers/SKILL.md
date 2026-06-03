---
name: serena-superpowers
description: Use when working in this project — bridges superpowers process skills with serena MCP semantic code tools. Load BEFORE applying any superpowers skill (TDD, debugging, planning, etc.) so that the workflow uses `serena_find_symbol`, `serena_replace_symbol_body`, `serena_find_referencing_symbols` and other LSP-backed tools instead of plain text search/edit.
---

# Serena × Superpowers Bridge

This project has two active systems that should work together, not in parallel:

1. **Superpowers** (process skills) — defines *how* to work: TDD, systematic debugging, planning, code review, subagent dispatch, verification.
2. **Serena MCP** (semantic code tools) — provides *what* to call: symbol-level navigation, cross-file refactors, LSP diagnostics, in-place edits.

**Core rule:** when a superpowers skill tells you to "find", "open", "read", "modify", or "trace" code, prefer the serena tool. Only fall back to native `grep` / `read` / `edit` when serena is unavailable or the task is purely textual (e.g. formatting, comments, non-code files).

## Mandatory Pre-Session Setup

On session start (or on first task in this project):

1. Read memories relevant to the task domain:
   ```
   read_memory("project_overview")
   read_memory("tech_stack")
   read_memory("code_conventions")
   read_memory("project_structure")
   ```
   Read `suggested_commands` and `task_checklist` on demand.
2. For any non-trivial change, also read `task_checklist` so the verification items are in context.
3. If you discover a new convention, gotcha, or stable command, write it back to the appropriate memory (`write_memory`).

## Superpowers Skill → Serena Tool Mapping

The table below is the **canonical** translation. Apply it whenever the superpowers skill mentions one of the row activities.

| Superpowers skill / phase | Activity described | Use this serena tool | Instead of |
|---|---|---|---|
| `writing-plans` "Files: Create/Modify" | Locate the exact class to extend | `find_symbol` (with `name_path` like `com/example/backend/service/RecipeService` or substring like `RecipeService`) | `grep` + `read` to find file |
| `writing-plans` | List all members of a class to plan around | `get_symbols_overview` | `read` of full file |
| `writing-plans` | Verify a method exists before referencing it | `find_symbol` with `depth=1` | grep |
| `executing-plans` per task | Re-confirm file structure changed by another task | `get_symbols_overview` | `read` |
| `subagent-driven-development` (dispatch) | Give the subagent concrete file context | include `find_symbol` results in the dispatched prompt instead of file paths only | passing a list of paths to read |
| `systematic-debugging` Phase 1.5 (trace data flow) | Find every caller of a method / every reference to a symbol | `find_referencing_symbols` | grep + manual cross-checking |
| `systematic-debugging` Phase 1.4 (multi-component) | Inspect type hierarchy / interface implementations | `find_implementations` (Java) / `type_hierarchy` (JetBrains) | grep for `implements`/`extends` |
| `test-driven-development` RED | Locate test class matching production class | `find_symbol` filtered by `*.Test` / `*.Spec` / `*.test` | filesystem search |
| `test-driven-development` GREEN | Make a minimal in-place edit | `replace_symbol_body` (for whole methods) or `insert_after_symbol` / `insert_before_symbol` (for new members) | `edit` with line numbers |
| `test-driven-development` REFACTOR | Rename across the codebase | `rename_symbol` (JetBrains) or careful `replace_content` with regex | search-and-replace by hand |
| `requesting-code-review` | Gather review context | `get_symbols_overview` per changed file, `find_referencing_symbols` for impact | full `read` |
| `verification-before-completion` | Check for static-analysis issues | `get_diagnostics_for_file` or `get_diagnostics_for_symbol` | re-running the compiler |
| `finishing-a-development-branch` | Pre-commit sanity | `get_diagnostics_for_file` on touched files | running a build |
| `dispatching-parallel-agents` | Each subagent gets a focused symbol query | `find_symbol` (single subagent per query) | dispatching explorers who all re-read the same file |
| `using-git-worktrees` | Confirm file exists in worktree | `find_file` (serena) | `read` of guessed path |

### When serena doesn't fit

Serena is optimised for source code at the symbol level. For these cases, use native tools or shell:

- Non-code files: `read` (configs, `.md`, `.yml`, `.json`, `.html`, `.scss` — serena's text tools work too but `read` is fine)
- Cross-file text refactors that don't target a single symbol (e.g. renaming a string across many files): `replace_content` with a careful regex, or `bash` + `git grep`
- Shell-bound work (running tests, builds, git ops): `bash` exclusively
- File-system discovery when you don't know the name: `glob` then `find_file` (serena) to filter by glob

## Memory Hygiene

- Read with `read_memory`, write with `write_memory`. Don't read/write memories via raw file paths unless intentionally bypassing the system.
- Use `list_memories` if you're unsure what's available.
- One topic per memory file. Don't dump a 200-line brain-dump into one file.
- If a memory becomes wrong, edit it (serena supports `replace_content` on the memory file directly via the path) — don't just append "FIXME" notes.

## Red Flags — You're Falling Back to Text Tools Too Much

- You just did `grep` for a Java class name → use `find_symbol` with `name_path` containing the package.
- You're about to `read` a 600-line file to find a single method → use `find_symbol` with the method name.
- You're about to edit a method by line numbers from a previous read → use `replace_symbol_body` (no line numbers).
- You're tracing where a value flows by manually opening callers → use `find_referencing_symbols` once.
- You asked three subagents to all "explore the codebase" → give each a `find_symbol` query instead.

## Anti-Patterns

- ❌ `grep` for class/method names — `find_symbol` is faster and disambiguates.
- ❌ `read` of a whole file when you need one symbol — `find_symbol` with `include_body=true` if needed.
- ❌ `edit` on a method body using remembered line numbers — `replace_symbol_body` doesn't care about line shifts.
- ❌ Manual search for "all callers of X" — `find_referencing_symbols` does it atomically.
- ❌ Ignoring `get_diagnostics_for_file` at verification time — it surfaces compile errors without a build.

## How This Skill Loads

This skill is registered in `.opencode/skills/serena-superpowers/SKILL.md` and picked up by opencode because `opencode.json` declares `skills.paths: [".opencode/skills"]`. The `description` frontmatter is intentionally broad so the model loads it as soon as any superpowers skill is mentioned, and before any code work begins.

The `using-superpowers` skill still applies — that one enforces "load skills first". This bridge skill complements it by saying "and when you do, here's which serena tool to use for each step".
