# Skill: Version Bump

When the user triggers this skill by saying "version-bump" or "bump version", follow these instructions:

## 1. Get Current Version Information
- Read `app/build.gradle.kts`.
- Identify the current `versionName` and `versionCode`.
- Store the current `versionName` as `oldVersionName`.

## 2. Ask for New Version
- Ask the user: "What is the new version name? (Current is <oldVersionName>)"
- Wait for user input. Let's call this `newVersionName`.

## 3. Update Gradle File
- Increment the `versionCode` by 1.
- Update `app/build.gradle.kts` with the `newVersionName` and the incremented `versionCode`.

## 4. Create Release Notes
- Create the directory `release-notes` if it doesn't exist.
- Identify the Pull Requests merged since the last version bump (check the git log or use `gh pr list --state merged`).
- Create a new Markdown file in the `release-notes` directory with the format: `Release Notes: <newVersionName> - <newVersionCode>.md`.
- The content of the file should include the titles of all the Pull Requests merged into this version.

## 5. Commit Changes
- Run `git add app/build.gradle.kts release-notes/`.
- Run `git commit -m "Version bump from <oldVersionName> -> <newVersionName> and release notes"` (with 60s timeout).

## 6. Trigger Open Pull Request
- Execute the command or prompt: "Run open-pr.md"

## 7. Open Pull Request from Dev to Main
- After the previous step is completed, open another Pull Request from the `dev` branch to the `main` branch.
- Title: "Release <newVersionName>"
- Description: "Automated version bump from <oldVersionName> to <newVersionName>"
- Command: `gh pr create --base main --head dev --title "Release <newVersionName>" --body "Automated version bump from <oldVersionName> to <newVersionName>"`
- If GitHub CLI is not available, provide a direct link to create the PR on the web interface.
