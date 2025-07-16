
# Subscription

A query molecule can subscribe and unsubscribe to updates that involve any of its attributes. 

## subscribe

A simple callback function is registered with `subscribe(<query-result> => Any)`. Then each time one of the query molecules attributes are involved in a transaction, the callback function is called with the latest query result. 

We could for instance have a chat query that updates a user interface everytime a User posts a comment:

```scala
// Chatroom dummy UI
var chatRoomUI    = List.empty[(Long, String, String)]
val chatRoomQuery = Post.id.a1.user.comment.query

// Subscribe to chat room
chatRoomQuery.subscribe { currentChat =>
  // Update UI with current chat
  chatRoomUI = currentChat
}

// Chat
Post.user.comment.insert(("Bob", "What's up?")).transact
Post.user.comment.insert(("Liz", "Not much...")).transact
Post.user.comment.insert(("Bob", "ok")).transact

// Chatroom was updated
chatRoomUI ==> List(
  (1, "Bob", "What's up?"),
  (2, "Liz", "Not much..."),
  (3, "Bob", "ok"),
)
```

The subscription callback will also be called when other mutations,`save`, `update` and `delete` involving any of the subscription query attributes are transacted.


### Only matching attrs

If we transact an attribute that is not in the subscription query molecule, the subscription callback is not called: 


```scala
Post.note("Remember to call Tomorrow").save.transact

// Chatroom unchanged since `note` is not in the subscription query.
chatRoomUI ==> List(
  (1, "Bob", "What's up?"),
  (2, "Liz", "Not much..."),
  (3, "Bob", "ok"),
)
```




## unsubscribe

Stop a subscription by calling `unsubribe` on the subscription molecule:


```scala
// Don't update chat room any longer
chatRoomQuery.unsubscribe

// Late comment
Post.user.comment.insert(("Bob", "See ya")).transact

// Chatroom not changed after unsubscription
chatRoomUI ==> List(
  (1, "Bob", "What's up?"),
  (2, "Liz", "Not much..."),
  (3, "Bob", "ok"),
)
```


See more elaborate examples in the [tests](https://github.com/scalamolecule/molecule/blob/main/coreTests/shared/src/test/scala/molecule/coreTests/spi/subscription/Subscription.scala)


##### [<i class="fas fa-handshake" style="margin-right: 4px;"></i> Subscription compliance tests](https://github.com/scalamolecule/molecule/tree/main/db/compliance/shared/src/test/scala/molecule/db/compliance/test/subscription)