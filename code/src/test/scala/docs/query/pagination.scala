package docs.query

import db.dataModel.dsl.Gaming._
import db.dataModel.dsl.Gaming.metadb.Gaming_MetaDb_h2
import docs.H2Tests
import molecule.db.h2.sync._
import utest._


object pagination extends H2Tests {

  override lazy val tests = Tests {

    "offset" - h2(Gaming_MetaDb_h2()) { implicit conn =>

      Gamer.rank.insert(1, 2, 3).transact

      val normal: List[(Int, String)] =
        Gamer.rank.name.query.get

      val offset: (List[(Int, String)], Int, Boolean) =
        Gamer.rank.name.query.offset(1).get

      // limit

      Gamer.rank.a1.query.limit(1).get ==> List(1)
      Gamer.rank.a1.query.limit(2).get ==> List(1, 2)

      Gamer.rank.a1.query.limit(-1).get ==> List(3)
      Gamer.rank.a1.query.limit(-2).get ==> List(2, 3)



      // Backward pagination uses offset = totalCount-limit which on large
      // data sets become inefficient
      Gamer.rank.a1.query.limit(-2).get ==> List(2, 3)

      // Get the same data by reversing the sorting. This is more efficient
      // since the offset is kept as low as possible.
      Gamer.rank.d1.query.limit(2).get ==> List(3, 2)

      // Reverse output to appear traversing backwards
      Gamer.rank.d1.query.limit(2).get.reverse ==> List(2, 3)


      // offset

      // Starting from beginning (all)
      Gamer.rank.a1.query.offset(0).get ==> (List(1, 2, 3), 3, false) // all

      // Starting from second row (0-based indexing)
      Gamer.rank.a1.query.offset(1).get ==> (List(2, 3), 3, false)

      // Etc..
      Gamer.rank.a1.query.offset(2).get ==> (List(3), 3, false)
      Gamer.rank.a1.query.offset(3).get ==> (List(), 3, false)


      // Before end (all)
      Gamer.rank.a1.query.offset(0).get ==> (List(1, 2, 3), 3, false)

      // Before last row
      Gamer.rank.a1.query.offset(-1).get ==> (List(1, 2), 3, false)

      // Before second last row, etc.
      Gamer.rank.a1.query.offset(-2).get ==> (List(1), 3, false)
      Gamer.rank.a1.query.offset(-3).get ==> (List(), 3, false)


      // forward

      // First page - more pages after...
      Gamer.rank.a1.query.limit(2).offset(0).get ==> (List(1, 2), 3, true)

      // Next page - is last page (no more afer)
      Gamer.rank.a1.query.limit(2).offset(2).get ==> (List(3), 3, false)


      // backwards

      // Last page - more pages before...
      Gamer.rank.a1.query.limit(-2).offset(0).get ==> (List(2, 3), 3, true)

      // Previous page -  is first page (no more before)
      Gamer.rank.a1.query.limit(-2).offset(-2).get ==> (List(1), 3, false)


      // Order of `offset` and `limit` doesn't matter
      Gamer.rank.a1.query.offset(0).limit(2).get ==> (List(1, 2), 3, true)
      Gamer.rank.a1.query.offset(2).limit(2).get ==> (List(3), 3, false)


      Gamer.rank.a1.query.limit(-2).offset(0).get._1 ==> List(2, 3)
      Gamer.rank.d1.query.limit(2).offset(0).get._1.reverse ==> List(2, 3)
    }


    "cursor, unique forward" - h2(Gaming_MetaDb_h2()) { implicit conn =>
      Gamer.rank.insert(1, 2, 3).transact

      // First page
      val (page1, cursor1, hasMore1) = Gamer.rank.a1.query.from("").limit(2).get
      page1 ==> List(1, 2)
      hasMore1 ==> true // more pages

      // Next page using cursor1 from first page
      val (page2, cursor2, hasMore2) = Gamer.rank.a1.query.from(cursor1).limit(2).get
      page2 ==> List(3)
      hasMore2 ==> false // no more pages
    }


    "cursor, unique backwards" - h2(Gaming_MetaDb_h2()) { implicit conn =>
      Gamer.rank.insert(1, 2, 3).transact

      // Last page
      val (page1, cursor1, hasMore1) = Gamer.rank.a1.query.from("").limit(-2).get
      page1 ==> List(2, 3)
      hasMore1 ==> true // more pages

      // Previous page using cursor1 from last page
      val (page2, cursor2, hasMore2) = Gamer.rank.a1.query.from(cursor1).limit(-2).get
      page2 ==> List(1)
      hasMore2 ==> false // no more pages
    }


    "cursor, id" - h2(Gaming_MetaDb_h2()) { implicit conn =>
      Gamer.username.insert("Blaze", "Venom", "Phoenix").transact

      val (page1, cursor1, _) = Gamer.id.a1.username.query.from("").limit(2).get
      page1 ==> List(
        (1, "Blaze"),
        (2, "Venom"),
      )

      val (page2, _, _) = Gamer.id.a1.username.query.from(cursor1).limit(2).get
      page2 ==> List(
        (3, "Phoenix"),
      )
    }


    "cursor, unique delete before" - h2(Gaming_MetaDb_h2()) { implicit conn =>
      val ids        = Gamer.rank.insert(1, 2, 3, 4, 5, 6, 7).transact.ids
      val (id2, id5) = (ids(1), ids(4))

      // First page
      val (page1, cursor1, _) = Gamer.rank.a1.query.from("").limit(2).get
      page1 ==> List(1, 2)

      // Last row of previous page is deleted
      Gamer(id2).delete.transact

      // Next page unaffected
      val (page2, cursor2, _) = Gamer.rank.a1.query.from(cursor1).limit(2).get
      page2 ==> List(3, 4)

      // First row of next page is deleted
      Gamer(id5).delete.transact

      // Next page simply skips deleted row
      val (page3, _, _) = Gamer.rank.a1.query.from(cursor2).limit(2).get
      page3 ==> List(6, 7) // deleted 5 is skipped
    }


    "cursor, sub-unique" - h2(Gaming_MetaDb_h2()) { implicit conn =>
      Gamer.category.rank.insert(
        ("Arcade", 1),
        ("Arcade", 2),
        ("Action", 3),
      ).transact

      val (page1, cursor1, _) = Gamer.category.a1.rank.a2.query.from("").limit(2).get
      page1 ==> List(
        ("Action", 3), // "Action" sorted first
        ("Arcade", 1),
      )

      val (page2, _, _) = Gamer.category.a1.rank.a2.query.from(cursor1).limit(2).get
      page2 ==> List(
        ("Arcade", 2),
      )
    }


    "cursor, non-unique" - h2(Gaming_MetaDb_h2()) { implicit conn =>
      Gamer.score.insert(1, 2, 3).transact

      val (page1, cursor1, hasMore1) = Gamer.score.a1.query.from("").limit(2).get
      page1 ==> List(1, 2)

      val (page2, cursor2, hasMore2) = Gamer.score.a1.query.from(cursor1).limit(2).get
      page2 ==> List(3)
    }


  }
}


