package docs.query

import db.dataModel.dsl.ChatRoom.*
import db.dataModel.dsl.ChatRoom.metadb.ChatRoom_h2
import docs.H2Tests
import molecule.db.h2.sync._
import utest._


object subscription extends H2Tests {

  override lazy val tests = Tests {

    "refs" - h2(ChatRoom_h2()) {

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



      // Only matching attrs -----------------------------------

//      Post.note("Remember to call Tomorrow").save.transact

      // Chatroom unchanged since `note` is not in the subscription query.
      chatRoomUI ==> List(
        (1, "Bob", "What's up?"),
        (2, "Liz", "Not much..."),
        (3, "Bob", "ok"),
      )


      // Unsubscribe -----------------------------------

      // Don't update chat room any longer
      chatRoomQuery.unsubscribe()

      chatRoomUI ==> List(
        (1, "Bob", "What's up?"),
        (2, "Liz", "Not much..."),
        (3, "Bob", "ok"),
      )

//      Thread.sleep(2000)

      // Late comment
      Post.user.comment.insert(("Bob", "See ya")).transact

      // Chatroom not changed after unsubscription
      chatRoomUI ==> List(
        (1, "Bob", "What's up?"),
        (2, "Liz", "Not much..."),
        (3, "Bob", "ok"),
      )
    }
  }
}
