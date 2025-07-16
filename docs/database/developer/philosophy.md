
# Design Philosophy

Keep it simple

- No macros
- No type classes
- Sparse implicits
- No higher-order type
- Meta Model without type parameters
  - Fully serializable for interconnectivity


### Let Data come to Scala

The common approach by most database libraries is to _accommodate to the database_. Either by offering to write interpolated raw query strings that the database wants or with a DSL that tries to mimmick the operations of a database like `select`, `filter` etc. - in other words _letting Scala come to Data_.

Molecule takes the opposite approach of letting you write vanilla Scala with the words of your domain in the most simple and intuitive way as if you were coding the data structures of your domain with your domain classes. Molecule then transparently translate this code (molecules) to the language of each database wihout you having to limit your mental model of your domain with database implementation details. <nobr>_Let Data come to Scala_</nobr>.


