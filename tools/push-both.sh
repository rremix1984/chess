#!/usr/bin/env bash
set -euo pipefail

branch=${1:-$(git -C "$(dirname "$0")/.." branch --show-current)}
repo_dir=$(cd "$(dirname "$0")/.." && pwd)

if [ -z "${branch}" ]; then
  echo "Not on a branch (detached HEAD). Specify explicitly: $0 <branch>" >&2
  exit 1
fi

echo "Repo: ${repo_dir}"
cd "$repo_dir"

echo "Pushing ${branch} to origin and github..."
if git remote get-url origin >/dev/null 2>&1; then
  git push origin "${branch}"
else
  echo "Skipping: no 'origin' remote"
fi

if git remote get-url github >/dev/null 2>&1; then
  git push github "${branch}"
else
  echo "Skipping: no 'github' remote"
fi
