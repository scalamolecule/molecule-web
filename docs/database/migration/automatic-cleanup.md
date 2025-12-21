# Automatic Cleanup

Molecule provides automatic cleanup using ScalaMeta AST transformations where this can ease your migration workflow.

The cleanup process has two distinct phases:

1. **Domain structure cleanup** - Automatically removes migration markers (`.remove`, `.rename()`) from your domain structure file
2. **Application code cleanup** (optional) - Renames attribute usages across your entire codebase

Domain structure cleanup is automatic; only application code renaming is optional.

## Phase 1: Domain structure cleanup

After running `sbt moleculeGen` successfully, Molecule automatically cleans up migration markers:

```
======================================================================
Migration completed successfully!

Cleaned up migration markers from domain structure:
  Attributes removed:
    - Person.age (line 10)
    - Person.phone (line 12)

  Rename markers removed (you must manually rename the definitions):
    - Person.name -> fullName (line 8)
    - Order.customer -> buyer (line 45)

✓ Domain structure updated: Person.scala
```

### What happens automatically

**For `.remove` markers:**
```scala
// Before
trait Person {
  val name = oneString
  val age = oneInt.remove
  val email = oneString
}

// After cleanup
trait Person {
  val name = oneString
  val email = oneString
}
```

The entire attribute line is removed, including any multi-line definitions.

**For `.rename()` markers:**
```scala
// Before
trait Person {
  val name = oneString.rename("fullName")
  val email = oneString
}

// After cleanup
trait Person {
  val name = oneString
  val email = oneString
}
```

Only the `.rename("...")` marker is removed. You must then **manually rename** the attribute definition:

```scala
// You manually change this
trait Person {
  val fullName = oneString  // Renamed from 'name'
  val email = oneString
}
```

## Phase 2: Application code cleanup

**This phase only applies to renames.** After domain structure cleanup completes, you'll see a second prompt:

```
The old attribute names will cause compile errors in your code.
Would you like to automatically rename attribute usages across your codebase?
(This will update .scala files in src/ directories)

Rename attribute usages? [y/n]:
```

### What happens if you choose 'y'

Molecule searches all `.scala` files in your `src/` directories and renames attribute usages:

```
Searching 47 Scala files...
  Updated: src/main/scala/app/queries/UserQueries.scala (5 occurrences)
  Updated: src/test/scala/app/PersonTests.scala (3 occurrences)
  Skipped: src/main/scala/app/InvalidFile.scala (parse error)

✓ Renamed 8 attribute usage(s) across 2 files
```

### Example transformations

**Before:**
```scala
// In your application code
val people = Person.name.email.age.query.get
val john = Person.name("John").email.query.get.head
case class PersonDTO(name: String, email: String)
```

**After automatic renaming:**
```scala
// After automatic renaming
val people = Person.fullName.email.age.query.get
val john = Person.fullName("John").email.query.get.head
case class PersonDTO(fullName: String, email: String)
```

## How it works - Type safety

Both cleanup phases use **Scala Meta AST parsing** for guaranteed correctness:

### Phase 1: Domain structure cleanup

Uses **captured source positions** from parsing:

1. When Molecule parses your domain structure, it uses Scala Meta to build an AST
2. It detects `.remove` and `.rename()` markers via AST pattern matching
3. It captures **exact source positions** (start/end byte offsets) for each marker
4. These positions are stored in the `MetaAttribute.sourcePosition` field
5. During cleanup, it uses these exact positions to remove/modify code
6. Processes in reverse order to maintain position validity
7. Cleans up excessive blank lines

**Why this is trustworthy:**
- ✅ Positions come from validated Scala Meta parsing
- ✅ No text search or regex pattern matching
- ✅ Can't accidentally match unrelated code
- ✅ 100% accurate identification of what to remove
- ✅ Preserves all comments, formatting, and other code

### Phase 2: Application code cleanup

Uses **AST traversal** to find and rename identifiers:

1. **Discovery**: Recursively finds all `.scala` files in `src/` directories
2. **Exclusion**: Skips `target/`, `.git/`, `moleculeGen/` directories
3. **Parsing**: For each file, parses with Scala Meta: `dialect.parse[Source]`
4. **Traversal**: Walks the AST looking for `Term.Name` nodes
5. **Matching**: Checks if the name matches an attribute being renamed
6. **Collection**: Collects exact positions: `(name.pos.start, name.pos.end, oldName, newName)`
7. **Replacement**: Applies replacements in reverse order (maintains position validity)
8. **Error handling**: Gracefully skips files with parse errors

**Why this is trustworthy:**
- ✅ Only touches actual Scala identifiers (`Term.Name` AST nodes)
- ✅ Strings, comments, and documentation are **never** modified
- ✅ Uses the same Scala Meta parser that validates your domain structure
- ✅ Parse errors are handled gracefully (files skipped, not corrupted)
- ✅ All changes are reviewable in git before committing
- ✅ No external dependencies (Scala Meta is already used for domain parsing)

**Files excluded from renaming:**
- `target/` - Build outputs
- `.git/` - Version control
- `moleculeGen/` - Generated DSL code (will be regenerated)
- Files that fail to parse (logged and skipped)

## Next steps

- [Unambiguous changes](/database/migration/unambiguous-changes.html) - Learn migration markers
- [Reference](/database/migration/reference.html) - Complete API reference
