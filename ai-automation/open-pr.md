# Skill: Open Pull Request

When the user triggers this skill by saying "open pr" or "open-pr", follow these instructions:

**Note on Git Commands:** Always execute Git commands with a 60-second timeout. To avoid hanging on credential prompts, ensure the environment is configured for non-interactive use (e.g., use a credential manager or set appropriate environment variables like `GIT_TERMINAL_PROMPT=0`). **Avoid using `git branch -r` as it can hang; use `git ls-remote --heads origin` if remote branch verification is necessary.**

## 1. Check for Uncommitted Changes
- Run `git status --short` (with 60s timeout).
- If the command hangs or fails:
    - Attempt to use `git status` without additional flags.
    - If it still fails, inform the user and ask to proceed manually.
- If changes exist:
    - Ask the user: "You have uncommitted changes. Would you like to commit them before opening the Pull Request?"
    - Provide options: [Yes, No]
    - If "Yes":
        - Ask: "What should be the commit message?"
        - Once provided, run `git add .` and `git commit -m "<message>"` (each with 60s timeout).
    - If "No":
        - Proceed to step 2.
- If no changes exist, proceed to step 2.

## 2. Check Branch Synchronization
- Identify the current branch name: `git branch --show-current`.
- Fetch the latest changes from the remote: `git fetch origin` (with 60s timeout).
- If `git fetch` hangs or fails:
    - Try `git remote update origin`.
    - If it still fails, warn the user that synchronization status might be inaccurate.
- Check if the current branch is behind `origin/dev`: `git rev-list --count HEAD..origin/dev` (with 60s timeout).
- If the count is greater than 0:
    - Inform the user: "The current branch is behind 'dev' by <count> commits. It's recommended to merge 'dev' into your branch first."
    - Ask if they want to continue anyway or merge first.
- If the branch is up-to-date or the user chooses to continue, proceed to step 3.

## 3. Open Pull Request
- Ensure the current branch is pushed to remote: `git push origin <current-branch>` (with 60s timeout).
- If `git push` fails or hangs, inform the user and suggest manual push.
- Open a Pull Request from the current branch to the `dev` branch.
- Generate a professional and structured title for the PR, prefixed with the branch type (e.g., `[Feature]`, `[Bugfix]`, `[Chore]`, `[Refactor]`) based on the current branch name.
- Generate a professional and structured description for the PR in **Markdown format**.
- Use the following template for the PR body:
    ```markdown
    ## Summary
    [Provide a high-level overview of the changes]

    ## Purpose
    [Explain why these changes are being made and what they achieve]

    ## Key Changes
    - [File path]: [Brief description of change]
    - ...
    ```
- When using the GitHub CLI:
    1.  Save the generated Markdown description to a temporary file (e.g., `temp_pr_body.md`).
    2.  Use the `--body-file` flag to create the PR:
        `gh pr create --base dev --head <current-branch> --title "<Generated Title>" --body-file "temp_pr_body.md"`
    3.  Delete the temporary file after the PR is successfully created.
- If GitHub CLI is not available, provide a direct link to create the PR on the web interface with the generated title and description.
