# Molecule website content

- `cd molecule-web`
- `hugo server --watch` to watch site locally at http://localhost:1313/
- make changes until satisfied
- `./deploy.sh` to upload
- ctrl-c to exit script
- commit and push molecule-docs to save changes of original files (from within Intellij)




Removing current submodule
`./rmSubmodule.sh`

Setting up submodule
https://gohugo.io/hosting-and-deployment/hosting-on-github/#step-by-step-instructions
`git submodule add -b master git@github.com:scalamolecule/scalamolecule.github.io.git public`


https://github.com/scalamolecule/molecule-web.git