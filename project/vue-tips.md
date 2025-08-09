

If hot-reloading stops working, you might want to re-install everything and clear the cache:

```
rm -rf node_modules
rm -rf docs/.vuepress/.temp
rm -rf docs/.vuepress/.cache
rm package-lock.json
npm cache clean --force
npm install --legacy-peer-deps
```

--legacy-peer-deps is to also install an earlier version of the google analytics plugin


```
npm run docs:clean-dev
or
npm run docs:dev
```