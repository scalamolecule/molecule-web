#!/bin/sh

# See https://davidwalsh.name/git-remove-submodule

path="$1"
if [ ! -d "./$path.git" ]; then
  echo "$path is no valid git submodule"
  exit 1
fi
git submodule deinit -f --all $path &&
git rm --cached $path &&
rm -rf .git/modules/$path &&
rm -rf $path &&
git reset HEAD .gitmodules &&
git config -f .gitmodules --remove-section submodule.$path
