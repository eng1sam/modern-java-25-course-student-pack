# Day 01 — Git & the development workflow

**Goal:** run the feature-branch workflow end to end — branch, commit through the three trees,
create and resolve a merge conflict, rebase to a linear history, rescue a "lost" commit with
`reflog`, and review a partner's pull request.

> No starter. You practise on this repository's **real history**.

## Prerequisites

- Git 2.40+ (`git --version`).
- A clone of the course repo. (This module lives inside it.)

## Do this

1. `git switch -c feature/<your-name>-notes`, add a line to a shared scratch file, and watch
   `git status` move your change: working tree → staged → committed.
2. Make two commits, then squash them: `git rebase -i HEAD~2` (or `git commit --fixup` +
   `git rebase --autosquash`).
3. Break things on purpose: `git reset --hard HEAD~1` to "lose" a commit, then `git reflog` to
   find its hash and `git reset --hard <hash>` to bring it back.
4. Pair up: both edit the **same line** on separate branches; merge one, rebase the other onto
   `main`, resolve the conflict.
5. Open a PR (or `git merge --no-ff` locally) and have a partner review it for **intent**.
6. Delete the merged branch.

## Done when

- [ ] A linear, rebased feature branch with one coherent commit.
- [ ] A commit deliberately lost with `reset --hard` and recovered via `reflog`.
- [ ] A conflict created and resolved deliberately (not auto-merged).
- [ ] A reviewed change merged to `main`; branch deleted.
- [ ] `git log --graph --oneline` reads as a clear story.

## Going further

`git bisect` to find a planted bug · `git worktree add` to check out a checkpoint alongside
`main` · `git cat-file -p HEAD` to read the plumbing.

Full brief: `labs/day-01/README.md` · Concepts: `docs/content/day-01/`.
