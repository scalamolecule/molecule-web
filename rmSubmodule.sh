#!/bin/sh
path="$1"
if [ ! -f "$path/.git" ]; then
  echo "$path is no valid git submodule"
  exit 1
fi
git submodule deinit -f $path &&
git rm --cached $path &&
rm -rf .git/modules/$path &&
rm -rf $path &&
git reset HEAD .gitmodules &&
git config -f .gitmodules --remove-section submodule.$path
