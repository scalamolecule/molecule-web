# Complex example

Let's explore a more complex example of a company with departments, employees and projects.

### Domain structure

```scala
object Company extends DomainStructure {
  trait Department {
    val name = oneString
  }
  trait Employee {
    val name       = oneString
    val department = manyToOne[Department]
  }
  trait Project {
    val name   = oneString
    val budget = oneInt
  }
  trait Assignment extends Join {
    val employee = manyToOne[Employee]
    val project  = manyToOne[Project]
    val role     = oneString
  }
}
```

### Data

```scala
val List(d1, d2, d3) = Department.name.insert(
  "Development", "Design", "Marketing"
).transact.ids

val List(e1, e2, e3, e4, e5, e6, e7, e8) = 
  Employee.name.department.insert(
    ("Alice", d1),
    ("Bob", d1),
    ("Carol", d1),
    ("Diana", d1),
    ("Eve", d2),
    ("Frank", d2),
    ("Grace", d3),
    ("Boss", d3),
  ).transact.ids

val List(p1, p2, p3) = Project.name.budget.insert(
  ("BigProject", 2000000),
  ("MediumProject", 1200000),
  ("SmallProject", 400000),
).transact.ids

Assignment.employee.project.role.insert(
  // Big project: 4 employees  (budget > 1M)
  (e1, p1, "Dev"), // Alice on BigProject
  (e2, p1, "Dev"), // Bob
  (e3, p1, "Lead"), // Carol
  (e4, p1, "Dev"), // Diana
  
  // Medium project: 2 employees, both on MediumProject (budget > 1M)
  (e1, p2, "Lead"), // Alice on MediumProject
  (e2, p2, "Dev"), // Bob
  (e5, p2, "Designer"), // Eve
  (e6, p2, "Designer"), // Frank
  
  // Small project: 1 employee (budget < 1M)
  (e7, p3, "Marketer"),
  // Boss is not assigned to a project
).transact
```

## Flat queries

Departments and number of employees (biggest first)
::: code-tabs
@tab Molecule
```scala
Department.name.Employees.id(countDistinct).d1.query.get ==> List(
  ("Development", 4), 
  ("Design", 2), 
  ("Marketing", 2)
)
```
@tab SQL
```sql
SELECT
  Department.name,
  COUNT(DISTINCT Employee.id) Employee_id_count
FROM Department
  INNER JOIN Employee ON
    Department.id = Employee.department
WHERE
  Department.name IS NOT NULL
GROUP BY Department.name
ORDER BY Employee_id_count DESC;
```
:::

Departments and number of employees working on projects
::: code-tabs
@tab Molecule
```scala
Department.name
  .Employees.id(countDistinct).d1
  .Assignments.project_.query.i.get ==> List(
  ("Development", 4),
  ("Design", 2), 
  ("Marketing", 1) // Boss not working on a specific project
)
```
@tab SQL
```sql
SELECT
  Department.name,
  COUNT(DISTINCT Employee.id) Employee_id_count
FROM Department
  INNER JOIN Employee ON
    Department.id = Employee.department
  INNER JOIN Assignment ON
    Employee.id = Assignment.employee
WHERE
  Department.name    IS NOT NULL AND
  Assignment.project IS NOT NULL
GROUP BY Department.name
ORDER BY Employee_id_count DESC;
```
:::

Departments and number of developers (no boss) working on projects
::: code-tabs
@tab Molecule
```scala
Department.name
  .Employees.id(countDistinct).d1
  .Assignments.role_("Dev")
  .query.i.get ==> List(("Development", 3))
```
@tab SQL
```sql
SELECT
  Department.name,
  COUNT(DISTINCT Employee.id) Employee_id_count
FROM Department
  INNER JOIN Employee ON
    Department.id = Employee.department
  INNER JOIN Assignment ON
    Employee.id = Assignment.employee
WHERE
  Department.name IS NOT NULL AND
  Assignment.role = 'Dev'
GROUP BY Department.name
ORDER BY Employee_id_count DESC;
```
:::

Departments and number of employees working on projects with a budget over one million
::: code-tabs
@tab Molecule
```scala
Department.name
  .Employees.id(countDistinct).d1
  .Projects.budget_.>(1000000)
  .query.i.get ==> List(("Development", 4), ("Design", 2))
```
@tab SQL
```sql
SELECT
  Department.name,
  COUNT(DISTINCT Employee.id) Employee_id_count
FROM Department
  INNER JOIN Employee ON
    Department.id = Employee.department
  INNER JOIN Assignment ON
    Employee.id = Assignment.employee
  INNER JOIN Project ON
    Assignment.project = Project.id
WHERE
  Department.name IS NOT NULL AND
  Project.budget  > 1000000
GROUP BY Department.name
ORDER BY Employee_id_count DESC;
```
:::

Departments and number of employees working on projects with more than 2 employees and a budget over one million
::: code-tabs
@tab Molecule
```scala
Department.name
  .Employees.id(countDistinct).>(2).d1
  .Projects.budget_.>(1000000)
  .query.i.get ==> List(("Development", 4))
```
@tab SQL
```sql
SELECT
  Department.name,
  COUNT(DISTINCT Employee.id) Employee_id_count
FROM Department
  INNER JOIN Employee ON
    Department.id = Employee.department
  INNER JOIN Assignment ON
    Employee.id = Assignment.employee
  INNER JOIN Project ON
    Assignment.project = Project.id
WHERE
  Department.name IS NOT NULL AND
  Project.budget  > 1000000
GROUP BY Department.name
HAVING COUNT(DISTINCT Employee.id) > 2
ORDER BY Employee_id_count DESC;
```
:::

Departments and number of developers working on projects with more than 2 developers and a budget over one million

::: code-tabs
@tab Molecule
```scala
Department.name
  .Employees.id(countDistinct).>(2).d1
  .Assignments.role_("Dev")
  .Project.budget_.>(1000000)
  .query.i.get ==> List(("Development", 3))
```
@tab SQL
```sql
SELECT
  Department.name,
  COUNT(DISTINCT Employee.id) Employee_id_count
FROM Department
  INNER JOIN Employee ON
    Department.id = Employee.department
  INNER JOIN Assignment ON
    Employee.id = Assignment.employee
  INNER JOIN Project ON
    Assignment.project = Project.id
WHERE
  Department.name IS NOT NULL AND
  Assignment.role = 'Dev' AND
  Project.budget  > 1000000
GROUP BY Department.name
HAVING COUNT(DISTINCT Employee.id) > 2
ORDER BY Employee_id_count DESC;
```
:::


## Nested queries

Roles of each project
::: code-tabs
@tab Molecule
```scala
Project.name.a1.Assignments.*(Assignment.role).query.get ==> List(
  ("BigProject", List("Dev", "Lead")),
  ("MediumProject", List("Designer", "Dev", "Lead")),
  ("SmallProject", List("Marketer"))
)
```
@tab SQL
```sql
SELECT DISTINCT
  Project.id,
  Project.name,
  Assignment.role
FROM Project
  INNER JOIN Assignment ON
    Project.id = Assignment.project
WHERE
  Project.name    IS NOT NULL AND
  Project.id      IS NOT NULL AND
  Assignment.role IS NOT NULL
ORDER BY Project.name, Project.id;
```
:::

Roles of each employee

::: code-tabs
@tab Molecule
```scala
Employee.name.a1.Assignments.*(Assignment.role).query.get ==> List(
  ("Alice", List("Dev", "Lead")),
  ("Bob", List("Dev")),
  ("Carol", List("Lead")),
  ("Diana", List("Dev")),
  ("Eve", List("Designer")),
  ("Frank", List("Designer")),
  ("Grace", List("Marketer"))
)
```
@tab SQL
```sql
SELECT DISTINCT
  Employee.id,
  Employee.name,
  Assignment.role
FROM Employee
  INNER JOIN Assignment ON
    Employee.id = Assignment.employee
WHERE
  Employee.name   IS NOT NULL AND
  Employee.id     IS NOT NULL AND
  Assignment.role IS NOT NULL
ORDER BY Employee.name, Employee.id;
```
:::

List Devs and what projects they work on
::: code-tabs
@tab Molecule
```scala
Employee.name.a1.Assignments.*(
  Assignment.role_("Dev").Project.name).query.i.get ==> List(
  ("Alice", List("BigProject")),
  ("Bob", List("BigProject", "MediumProject")),
  ("Diana", List("BigProject"))
)
```
@tab SQL
```sql
SELECT DISTINCT
  Employee.id,
  Employee.name,
  Project.name
FROM Employee
  INNER JOIN Assignment ON
    Employee.id = Assignment.employee
  INNER JOIN Project ON
    Assignment.project = Project.id
WHERE
  Employee.name   IS NOT NULL AND
  Employee.id     IS NOT NULL AND
  Assignment.role = 'Dev' AND
  Project.name    IS NOT NULL
ORDER BY Employee.name, Employee.id;
```
:::


Projects and who's assigned to them

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
  INNER JOIN Assignment ON
    Project.id = Assignment.project
  INNER JOIN Employee ON
    Assignment.employee = Employee.id
WHERE
  Project.name  IS NOT NULL AND
  Project.id    IS NOT NULL AND
  Employee.name IS NOT NULL
ORDER BY Project.name, Project.id;
```
:::


Projects and who's assigned to them that belong to a department starting with "De".

::: code-tabs
@tab Molecule
```scala
Project.name.a1.Employees.**(
  Employee.name.Department.name_.startsWith("De")
).query.i.get ==> List(
  ("BigProject", List("Alice", "Bob", "Carol", "Diana")),
  ("MediumProject", List("Alice", "Bob", "Eve", "Frank")),
)
```
@tab SQL
```sql
SELECT DISTINCT
  Project.id,
  Project.name,
  Employee.name
FROM Project
  INNER JOIN Assignment ON
    Project.id = Assignment.project
  INNER JOIN Employee ON
    Assignment.employee = Employee.id
  INNER JOIN Department ON
    Employee.department = Department.id
WHERE
  Project.name    IS NOT NULL AND
  Project.id      IS NOT NULL AND
  Employee.name   IS NOT NULL AND
  Department.name LIKE 'De%'
ORDER BY Project.name, Project.id;
```
:::


Departments with employees and their Projects and budgets

::: code-tabs
@tab Molecule
```scala
Department.name.a1.Employees.*(
  Employee.name.a1.Projects.**(
    Project.name.a1.budget
  )
).query.get ==> List(
  ("Design", List(
    ("Eve", List(
      ("MediumProject", 1200000))),
    ("Frank", List(
      ("MediumProject", 1200000))))),
  ("Development", List(
    ("Alice", List(
      ("BigProject", 2000000),
      ("MediumProject", 1200000))),
    ("Bob", List(
      ("BigProject", 2000000),
      ("MediumProject", 1200000))),
    ("Carol", List(
      ("BigProject", 2000000))),
    ("Diana", List(
      ("BigProject", 2000000))))),
  ("Marketing", List(
    ("Grace", List(
      ("SmallProject", 400000)))))
)
```
@tab SQL
```sql
SELECT DISTINCT
  Department.id,
  Employee.id,
  Department.name,
  Employee.name,
  Project.name,
  Project.budget
FROM Department
  INNER JOIN Employee ON
    Department.id = Employee.department
  INNER JOIN Assignment ON
    Employee.id = Assignment.employee
  INNER JOIN Project ON
    Assignment.project = Project.id
WHERE
  Department.name IS NOT NULL AND
  Department.id   IS NOT NULL AND
  Employee.name   IS NOT NULL AND
  Employee.id     IS NOT NULL AND
  Project.name    IS NOT NULL AND
  Project.budget  IS NOT NULL
ORDER BY Department.name, Department.id, Employee.name, Employee.id, Project.name;
```
:::


Etc...

##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Relationship compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/relation)