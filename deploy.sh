#!/bin/bash


echo -e "\033[0;32mCopying API...\033[0m"

# Update api
rm -r ./static/api
cp -R ../molecule/core/target/scala-2.12/api ./static/


echo -e "\033[0;32mBuilding public Hugo site...\033[0m"

# Empty public folder
rm -rf public
mkDir public

# Build the project. Will add everything to the public folder.
hugo # if using a theme, replace by `hugo -t <yourtheme>`

echo -e "\033[0;32mDeploying updates to GitHub...\033[0m"

# Go To Public folder
cd public

# Add changes to git.
git add .

# Commit changes.
msg="rebuilding site `date`"
if [ $# -eq 1 ]
  then msg="$1"
fi
git commit -m "$msg"

# Push source and build repos.
git push origin master

# Come Back
cd ..
