---
title: "Clojure exchanges"
weight: 50
menu:
  main:
    parent: dev
    identifier: dev-clojure-exchanges
---


# Clojure Schemas

To ease exchanging Datomic schemas with the Clojure world where Namespaces start with a lowercase letter by convention, Molecule can also creates two convenience Schema "converters" that transact aliased attribute names where the first letter is either raised or lowered. To generate these converters, set the MoleculePlugin parameter `moleculeSchemaConversions` to true in your `build.sbt` file:

Putting all the configuration together, we here have a minimal project `build.sbt` setup for a free Datomic Peer system on the jvm platform:

```scala
  .settings(
    // Generate lover/upper schema conversion files (default is false)
    moleculeSchemaConversions := true,
    ...
  )
```


### lowercase to Uppercase

If you for instance want to use Molecule with an externally defined Datomic database Schema or data sets with lowercase namespace names you can define the Data Model as usual in Molecule with uppercase Namespace names and then rea:

```scala
// Read lowercase schema from edn data
val lowercaseExternalSchema = datomic.Util.readAll(new FileReader("external-schema.dtm"))
conn.transact(lowercaseExternalSchema)

// Convert to uppercase schema
conn.transact(ExternalSchemaLowerToUpper.edn)

// Make molecules with uppercase Namespace names...
```

When we test the mBrainz dataset for instance, we [convert from from lower to Upper](https://github.com/scalamolecule/molecule/blob/master/moleculeTests/jvm/src/test/scala/moleculeTests/setup/AsyncTestSuiteImpl.scala#L76).

### Uppercase to lowercase

Vice versa, you can "export" an Uppercase schema to lowercase by transacting `<your-domain>SchemaUpperToLower`:

```scala
conn.transact(YourSchemaUpperToLower.edn)
```
You can then backup the database and let it be conveniently restored in a Clojure setting:

```
cd <your-datomic-distribution>

# Backup to file
bin/datomic backup-db datomic:free://localhost:4334/your-db file:/path/to/lower-db

# Restore from file
bin/datomic restore-db file:/path/to/lower-db datomic:free://localhost:4334/lower-db
```
