---
title: "3.2 Db Connection, js"
weight: 50
menu:
  main:
    parent: setup
---

# Database connection, js

Molecule is fully implemented with Scala.js too. This allows us to transact and query data directly from the client/js side. Molecule calls are identical on both the server and client side and are therefore easy to refactor between the two platforms if needed.

Molecule transparently sends and retrieves data via ajax calls to the server and takes care of marshalling data back and forth with highly efficient Byte encoding/decoding using [BooPickle](https://boopickle.suzaku.io).

Since Molecule knows the exact structure of our data at compile time, only data without attribute names etc is encoded. Compared to json that redundantly adds property names to all values, Molecule avoids this and can thereby more efficiently transfer the bare minimum of actual data.



## Ajax Server endpoint

The server side needs to be set up to receive ajax calls with an endpoint that Molecule can communicate with. Hereafter, all molecule transactions and queries will flow transparently without you having to code manual RPC calls via shared apis and server implementations!

The Molecule ajax server configuration is very simple. Basically, three lines need to be added to your server controller/endpoint. The needed interfaces and implementations are all supplied by Molecule:

1. Extend `MoleculeRpcHandler` with a network interface and port to listen to.
2. Define a router with the `MoleculeRpc` interface and `DatomicRpc` implementation.
3. Encode the result by calling the `moleculeRpcResult` method.

By extending `MoleculeRpcHandler` the method `moleculeRpcResult` encoder for the third step becomes available.

On a Play server, a Controller could be setup like in [this sample project]():

```scala
// 1. Extend MoleculeRpcHandler
class AppController extends MoleculeRpcHandler("localhost", 9000) with InjectedController with HtmlTag {

  // 2. Define router with MoleculeRpc interface and DatomicRpc implementation
  val router = Router[ByteBuffer, Future].route[MoleculeRpc](DatomicRpc)

  def ajax(path: String): Action[RawBuffer] = {
    Action.async(parse.raw) { implicit ajaxRequest =>
      val args = ajaxRequest.body.asBytes(parse.UNLIMITED).get
      
      // 3. Encode data using router, api path and args
      moleculeRpcResult(router, path, args) 
      .map(Ok(_))
    }
  }
}
```


## Client side imports

To use Molecule on the client/js side we need a few imports: the Molecule api, the generated DSL for your Data Model and a `Conn_Js` that holds a proxy connection:

```scala
import molecule.datomic.api._
import app.dsl.yourDomain._
import molecule.core.facade.Conn_Js
```


## Client proxy connection


As on the server side, the client side also needs an implicit connection to be in scope to make molecules. 

We use a `Conn_Js` instantiated with a proxy connection matching the database system that we want to use. The proxy connection contains the necessary information to be sent along each ajax call to establish a real connection on the server side where the real interaction with database happens.

Here's an example of creating an implicit connection on the client side by instantiating the `Conn_Js` with an in-mem Datomic Peer proxy connection:
```scala
// Create in-mem database connection using our Person data model
implicit val conn = Future(Conn_Js(
  DatomicPeerProxy("mem", "", PersonSchema.datomicPeer, PersonSchema.attrMap), "localhost", 9000
))
```

There are [3 Proxy connection types]() that all require the following two schema/attribute arguments:

- A Seq of Schema transaction data that is supplied from the sbt-molecule plugin generated boilerplate code.
- A Map of Attribute meta data, also supplied from the sbt-molecule plugin generated boilerplate code.


### DatomicPeerProxy

The Peer proxy connection requires a protocol String that can be one of the following:

- mem: for an in-mem Peer connection, typically for testing.
- free: a connection to a Datomic Peer Free connection.
- dev or pro: a connection to a Datomic Peer Pro connection.

A database identifier String containing the database network interface and port where the Datomic Transactor is running, and a database name is also supplied. 

A full example of a DatomicPeerProxy could look like this:
```scala
DatomicPeerProxy(
  "pro",
  "localhost:4334/mbrainz-1968-1973",
  MBrainzSchema.datomicPeer, // Note that datomicPeer is used
  MBrainzSchema.attrMap
)
```


### DatomicDevLocalProxy

To test against the Datomic Client api, a dev-local proxy connection can be used. This requires downloading the [dev-tools](https://cognitect.com/dev-tools) and installing them locally as per downloaded instructions.

Four additional arguments are supplied to create a DatomicDevLocalProxy:

- A protocol to be supplied as the DatomicPeerProxy (same as above).
- A name of a Datomic "system" which translates into a directory in the datomic distributions folder where database data will be saved.
- A path to your dev-local distribution.
- Name of database.

DatomicDevLocalProxy example:
```scala
DatomicDevLocalProxy(
  "dev",
  "datomic-samples",
  "/path/to/your/dev-local-distribution",
  "mbrainz-subset",
  MBrainzSchema.datomicClient, // Note that datomicClient is used
  MBrainzSchema.attrMap
)
```


### DatomicPeerServerProxy

Four additional arguments are supplied to create a DatomicDevLocalProxy:

- A keyword.
- A secret/password.
- A network interface and port where the Peer Server is accessible.
- Name of database.

DatomicPeerServerProxy example:
```scala
DatomicPeerServerProxy(
  "key", 
  "secret", 
  "localhost:8998",
  "mbrainz-1968-1973",
  MBrainzSchema.datomicClient, // Note that datomicClient is used
  MBrainzSchema.attrMap
)
```

For examples of complete Client setups, please have a look at the two rpc projects in the [molecule samples repo]()


### Next

Explore various [example database setups...](/setup/db-setups). These are all examples of Server setups. But you'll find that most settings apply to a Client setup too.
