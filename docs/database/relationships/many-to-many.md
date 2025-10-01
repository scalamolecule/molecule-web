# Many-to-many

As described in [Domain structure](/database/setup/domain-structure#many-to-many), a relationship can be defined as many-to-many join table between two entities.

```scala
object Company extends DomainStructure {
  trait Project {
    val name   = oneString
    val budget = oneInt
  }
  trait Employee {
    val name = oneString
  }
  trait Assignment extends Join { // extend Join to treat as many-to-many
    val project  = manyToOne[Project]
    val employee = manyToOne[Employee]
    val role     = oneString
  }
}
```

This allows us to define a many-to-many relationship between `Project` and `Employee` entities directly with `Employees.**`:

::: code-tabs
@tab Molecule
```scala
Project.name.a1.Employees.**(Employee.name).query.get ==> List(
  ("BigProject", List("Alice", "Bob", "Carol", "Diana")),
  ("MediumProject", List("Alice", "Bob", "Eve", "Frank")),
  ("SmallProject", List("Grace")),
)
```
@tab SQL
```sql
SELECT DISTINCT
  Project.id,
  Project.name,
  Employee.name
FROM Project
  INNER JOIN Assignment ON Project.id = Assignment.project
  INNER JOIN Employee   ON Assignment.employee = Employee.id
WHERE
  Project.name  IS NOT NULL AND
  Project.id    IS NOT NULL AND
  Employee.name IS NOT NULL
ORDER BY Project.name, Project.id;
```
:::

The `Employees.**` accessor is simply convenience syntax sugar for two relationships via `Assignment`.

Vice-versa we can make a bridged query in the opposite direction too:

::: code-tabs
@tab Molecule
```scala
Employee.name.a1.Projects.**(Project.name).query.get ==> List(
  ("Alice", List("BigProject", "MediumProject")),
  ("Bob", List("BigProject", "MediumProject")),
  ("Carol", List("BigProject")),
  ("Diana", List("BigProject")),
  ("Eve", List("MediumProject")),
  ("Frank", List("MediumProject")),
  ("Grace", List("SmallProject")),
)
```
@tab SQL
```sql
SELECT DISTINCT
  Employee.id,
  Employee.name,
  Project.name
FROM Employee
  INNER JOIN Assignment ON Employee.id = Assignment.employee
  INNER JOIN Project    ON Assignment.project = Project.id
WHERE
  Employee.name IS NOT NULL AND
  Employee.id   IS NOT NULL AND
  Project.name  IS NOT NULL
ORDER BY Employee.name, Employee.id;
```
:::

## Relationship properties

When we are interested in properties of the join table `Assignment` we can query this too:

::: code-tabs
@tab Molecule
```scala
Project.name.Assignments.*(Assignment.role.Employee.name.a1).query.get ==> List(
  ("BigProject", List(
    ("Dev", "Alice"),
    ("Dev", "Bob"),
    ("Lead", "Carol"),
    ("Dev", "Diana"),
  )),
  ("MediumProject", List(
    ("Lead", "Alice"),
    ("Dev", "Bob"),
    ("Designer", "Eve"),
    ("Designer", "Frank"),
  )),
  ("SmallProject", List(
    ("Marketer", "Grace")
  )),
)
```
@tab SQL
```sql
SELECT DISTINCT
  Project.id,
  Project.name,
  Assignment.role,
  Employee.name
FROM Project
  INNER JOIN Assignment ON Project.id = Assignment.project
  INNER JOIN Employee   ON Assignment.employee = Employee.id
WHERE
  Project.name    IS NOT NULL AND
  Project.id      IS NOT NULL AND
  Assignment.role IS NOT NULL AND
  Employee.name   IS NOT NULL
ORDER BY Project.id, Employee.name;
```
:::

Or we can ask for the names of the designers by using a property of the join table:

::: code-tabs
@tab Molecule
```scala
Assignment.role_("Designer").Employee.name.query.get ==> List("Eve", "Frank")
```
@tab SQL
```sql
SELECT DISTINCT
  Employee.name
FROM Assignment
  INNER JOIN Employee ON Assignment.employee = Employee.id
WHERE
  Assignment.role = 'Designer' AND
  Employee.name   IS NOT NULL;
```
:::


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Relationship compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/relation)