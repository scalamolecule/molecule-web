package docs.query

import db.dataModel.dsl.Football.*
import db.dataModel.dsl.Football.metadb.Football_MetaDb_h2
import docs.H2Tests
import molecule.base.error.ModelError
import molecule.db.h2.sync.*
import utest.*


object filterAttrs extends H2Tests {

  override lazy val tests = Tests {

    "same ns" - h2(Football_MetaDb_h2()) { implicit conn =>

      Player.name.goals.assists.insert(
        ("Celso", 1, 2),
        ("Messi", 3, 3),
        ("Salah", 5, 4),
      ).transact


      // Equality

      Player.name.goals(Player.assists_).query.get ==> List(("Messi", 3))
      Player.name.goals_(Player.assists_).query.get ==> List("Messi")
      Player.name.goals(Player.assists_).assists.query.get ==> List(("Messi", 3, 3))


      Player.name.goals_(Player.assists_).assists.not(2).query.get ==> List(("Messi", 3))
      Player.name.goals_(Player.assists_).assists.not(3).query.get ==> List()



      // Different number of goals and assists
      Player.name.goals.not(Player.assists_).assists.query.get ==> List(
        ("Celso", 1, 2),
        ("Salah", 5, 4),
      )

      // Fewer goals than assists
      Player.name.goals.<(Player.assists_).assists.query.get ==> List(
        ("Celso", 1, 2),
      )

      // More goals than assists
      Player.name.goals.>(Player.assists_).assists.query.get ==> List(
        ("Salah", 5, 4),
      )

      // Fewer or equally many goals as assists
      Player.name.goals.<=(Player.assists_).assists.query.get ==> List(
        ("Celso", 1, 2),
        ("Messi", 3, 3),
      )

      // More or equally many goals as assists
      Player.name.goals.>=(Player.assists_).assists.query.get ==> List(
        ("Messi", 3, 3),
        ("Salah", 5, 4),
      )


      // Sub filter --------------------------------------------

      Player.name.goals.>=(Player.assists_).assists.query.get ==> List(
        ("Messi", 3, 3),
        ("Salah", 5, 4),
      )

      // More or equally many goals than assists when assists == 3
      Player.name.goals.>=(Player.assists_).assists(3).query.get ==> List(
        ("Messi", 3, 3),
      )

      // More or equally many goals than assists when assists == 3
      Player.name.goals.>=(Player.assists_).assists(4).query.get ==> List(
        ("Salah", 5, 4),
      )


      // More or equally many goals than assists when assists != 3
      Player.name.goals.>=(Player.assists_).assists.not(3).query.get ==> List(
        ("Salah", 5, 4),
      )

      // More or equally many goals than assists when assists != 4
      Player.name.goals.>=(Player.assists_).assists.not(4).query.get ==> List(
        ("Messi", 3, 3),
      )


      // More or equally many goals than assists when assists < 3
      Player.name.goals.>=(Player.assists_).assists.<(3).query.get ==> List()

      // More or equally many goals than assists when assists < 4
      Player.name.goals.>=(Player.assists_).assists.<(4).query.get ==> List(
        ("Messi", 3, 3),
      )


      // More or equally many goals than assists when assists > 3
      Player.name.goals.>=(Player.assists_).assists.>(3).query.get ==> List(
        ("Salah", 5, 4),
      )

      // More or equally many goals than assists when assists > 4
      Player.name.goals.>=(Player.assists_).assists.>(4).query.get ==> List()


      // More or equally many goals than assists when assists <= 3
      Player.name.goals.>=(Player.assists_).assists.<=(3).query.get ==> List(
        ("Messi", 3, 3),
      )

      // More or equally many goals than assists when assists <= 4
      Player.name.goals.>=(Player.assists_).assists.<=(4).query.get ==> List(
        ("Messi", 3, 3),
        ("Salah", 5, 4),
      )


      // More or equally many goals than assists when assists >= 3
      Player.name.goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Messi", 3, 3),
        ("Salah", 5, 4),
      )

      // More or equally many goals than assists when assists >= 4
      Player.name.goals.>=(Player.assists_).assists.>=(4).query.get ==> List(
        ("Salah", 5, 4),
      )


      // 2 sub filters --------------------------------------------

      // More or equally many goals than assists
      // when goals == 3 and assists >= 3
      Player.name.goals_(3).goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Messi", 3, 3),
      )

      // More or equally many goals than assists
      // when goals == 5 and assists >= 3
      Player.name.goals_(5).goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Salah", 5, 4),
      )


      // More or equally many goals than assists
      // when goals != 3 and assists >= 3
      Player.name.goals_.not(3)
        .goals.>=(Player.assists_)
        .assists.>=(3).query.get ==> List(
        ("Salah", 5, 4),
      )

      // More or equally many goals than assists
      // when goals != 5 and assists >= 3
      Player.name.goals_.not(5)
        .goals.>=(Player.assists_)
        .assists.>=(3).query.get ==> List(
        ("Messi", 3, 3),
      )


      // More or equally many goals than assists
      // when goals < 3 and assists >= 3
      Player.name.goals_.<(3)
        .goals.>=(Player.assists_).assists.>=(3).query.get ==> List()

      // More or equally many goals than assists
      // when goals < 5 and assists >= 3
      Player.name.goals_.<(5)
        .goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Messi", 3, 3),
      )


      // More or equally many goals than assists
      // when goals > 3 and assists >= 3
      Player.name.goals_.>(3)
        .goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Salah", 5, 4),
      )

      // More or equally many goals than assists
      // when goals > 5 and assists >= 3
      Player.name.goals_.>(5)
        .goals.>=(Player.assists_).assists.>=(3).query.get ==> List()


      // More or equally many goals than assists
      // when goals <= 3 and assists >= 3
      Player.name.goals_.<=(3)
        .goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Messi", 3, 3),
      )

      // More or equally many goals than assists
      // when goals <= 5 and assists >= 3
      Player.name.goals_.<=(5)
        .goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Messi", 3, 3),
        ("Salah", 5, 4),
      )


      // More or equally many goals than assists
      // when goals >= 3 and assists >= 3
      Player.name.goals_.>=(3).goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Messi", 3, 3),
        ("Salah", 5, 4),
      )

      // More or equally many goals than assists
      // when goals >= 5 and assists >= 3
      Player.name.goals_.>=(5).goals.>=(Player.assists_).assists.>=(3).query.get ==> List(
        ("Salah", 5, 4),
      )


      // calling sub filter

      // More or equally many goals than assists when goals >= 3
      Player.name.goals_.>=(3).goals.>=(Player.assists_).assists.query.get ==> List(
        ("Messi", 3, 3),
        ("Salah", 5, 4),
      )

      // More or equally many goals than assists when goals >= 4
      Player.name.goals_.>=(4).goals.>=(Player.assists_).assists.query.get ==> List(
        ("Salah", 5, 4),
      )
    }


    "ref" - h2(Football_MetaDb_h2()) { implicit conn =>
      val List(champions, galactico) = Team.name.goalsToBonus.bonus.insert(
        ("Champions", 3, 1000000),
        ("Galactico", 4, 5000000),
      ).transact.ids

      Player.name.goals.team.insert(
        ("Celso", 1, champions),
        ("Messi", 3, champions),
        ("Salah", 5, galactico),
      ).transact


      Player.name.goals.Team.name.goalsToBonus.bonus.query.get ==> List(
        ("Celso", 1, "Champions", 3, 1000000),
        ("Messi", 3, "Champions", 3, 1000000),
        ("Salah", 5, "Galactico", 4, 5000000),
      )


      Player.name.goals.>=(Team.goalsToBonus_)
        .Team.name.goalsToBonus.bonus.query.get ==> List(
        ("Messi", 3, "Champions", 3, 1000000),
        ("Salah", 5, "Galactico", 4, 5000000),
      )

      Player.name.goals
        .Team.name.goalsToBonus.<=(Player.goals_).bonus.query.get ==> List(
        ("Messi", 3, "Champions", 3, 1000000),
        ("Salah", 5, "Galactico", 4, 5000000),
      )


      // Missing filter attribute
      intercept[ModelError] {
        Player.name.goals.>=(Team.goalsToBonus_)
          .Team.name.bonus // goalsToBonus missing
          .query.get
      }.msg ==> "Please add missing filter attribute Team.goalsToBonus"
    }


    "nested" - h2(Football_MetaDb_h2()) { implicit conn =>

      Team.name.goalsToBonus.bonus.Players.*(Player.name.goals).insert(
        ("Champions", 3, 1000000, List(
          ("Celso", 1),
          ("Messi", 3),
        )),
        ("Galactico", 4, 5000000, List(
          ("Salah", 5),
          ("Yamal", 2),
        )),
      ).transact

      Team.name.goalsToBonus.bonus
        .Players.*(Player.name.goals).query.get ==> List(
        ("Champions", 3, 1000000, List(
          ("Celso", 1),
          ("Messi", 3),
        )),
        ("Galactico", 4, 5000000, List(
          ("Salah", 5),
          ("Yamal", 2),
        )),
      )

      Team.name.goalsToBonus.<=(Player.goals_).bonus
        .Players.*(Player.name.goals).query.get ==> List(
        ("Champions", 3, 1000000, List(
          ("Messi", 3),
        )),
        ("Galactico", 4, 5000000, List(
          ("Salah", 5),
        )),
      )


      Team.name.goalsToBonus.bonus
        .Players.*(Player.name.goals.>=(Team.goalsToBonus_))
        .query.get ==> List(
        ("Champions", 3, 1000000, List(
          ("Messi", 3),
        )),
        ("Galactico", 4, 5000000, List(
          ("Salah", 5),
        )),
      )
      Team.name.goalsToBonus.bonus
        .Players.*(Player.name.goals.>(Team.goalsToBonus_))
        .query.get ==> List(
        ("Galactico", 4, 5000000, List(
          ("Salah", 5),
        )),
      )

      Team.name.goalsToBonus.bonus
        .Players.*(Player.name.goals.>(Team.goalsToBonus_))
        .query.get ==> List(
        ("Galactico", 4, 5000000, List(
          ("Salah", 5),
        )),
      )

      Team.name.goalsToBonus.bonus
        .Players.*?(Player.name.goals.>(Team.goalsToBonus_))
        .query.get ==> List(
        ("Galactico", 4, 5000000, List(
          ("Salah", 5),
        )),
      )
    }
  }
}


