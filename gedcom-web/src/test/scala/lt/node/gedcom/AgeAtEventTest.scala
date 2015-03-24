package lt.node.gedcom

import org.specs.runner._
import org.specs.Specification
import lt.node.gedcom.util.AgeAtEvent

//class aMultiLangTextTest extends SpecificationWithJUnit /*with ScalaTest*/ {
class anAgeAtEventTest extends Runner /*Main*/ (AgeAtEventSpec) with JUnit with Console
// https://github.com/lift/lift/blob/master/framework/lift-base/lift-webkit/src/test/scala/net/liftweb/http/SnippetSpec.scala

object AgeAtEventSpec extends
Specification /*WithJUnit*/ ("AgeAtEvent functionality testing") /*with ScalaTest*/ {
  // neveikia: override var description = "MultiLangText functionality testing aprašymas"
  val dbField = "dbField"
  val lt_en: List[Tuple2[String, String]] = List(
    ("<99M 11m 22d", "<99y 11m 22d"),("<99M", "<99y"),("<11m", "<11m"),("<22d", "<22d"),
    ("99M 11m 22d", "99y 11m 22d"),("99M  11m  22d", "99y 11m 22d"),(" 99M 11m 22d    ", "99y 11m 22d"),
    ("<99M 11m", "<99y 11m"),("<99M 222d", "<99y 222d"),("11m 22d", "11m 22d"),
    (" vaikas", "CHILD"),("kūdikis ", "INFANT"),(" naujagimis ", "STILLBORN")
    )

  "AgeAtEvent" should /* or 'can' instead of 'should' */ {
    var header = ""

    /*"'hello world' has 11 characters" in {
      "hello world".size must_== 11
    }
    "'hello world' matches 'h.* w.*'" in {
      "hello world" must be matching ("h.* w.*")
    }
    "'Sveiki visi!' has 11 characters" in {
      "Sveiki visi!".size must_== 12
    }*/



    /*var header = "'validateAgeAtEvent' process one true case"
    header >> {
      val sAAE = "<99M 11m 22d"
      val tAAE = AgeAtEvent.validateAgeAtEvent(sAAE, "lt")
      tAAE == true
    }*/

    header = "'validateAgeAtEvent' process a list of 'lt' AAEs"
    header >> {
      var res = true
      for (kv <- lt_en) {
        res = res && AgeAtEvent.validateAgeAtEvent(kv._1, "lt")
        println(<_>|{kv._1}|  |{AgeAtEvent.validateAgeAtEvent(kv._1, "lt")}|</_>.text)
      }
      res == true
    }

    header = "'doGedcomAgeAtEvent' process a list of 'lt' AAEs"
    header >> {
      var res = true
      print("-------------------- "); println(header)
      for (kv <- lt_en) {
        var gedcomAAE = AgeAtEvent.doGedcomAgeAtEvent(kv._1, "lt")
        res = res && gedcomAAE == kv._2
        println(<_>|{kv._1}|  |{gedcomAAE}|  |{(gedcomAAE == kv._2).toString}|</_>.text)
      }
      res == true
    }

    header = "'doLocaleAgeAtEvent' process a list of 'lt' AAEs"
    header >> {
      var res = true
      print("-------------------- "); println(header)
      for (kv <- lt_en) {
        var localeAAE = AgeAtEvent.doLocaleAgeAtEvent(kv._2, "lt")
        res = res && localeAAE == kv._1
        println(<_>|{kv._2}|  |{localeAAE}|  |{(localeAAE == AgeAtEvent.normalizeAAE(kv._1).toString)}|</_>.text)
      }
      res == true
    }

    /*header = "'validateAgeAtEvent' process one false case"
    header >> {
      //doGedcomAgeAtEvent(aaeText: String): String
      val sAAE = "<99X 11m 22d"
      val tAAE = AgeAtEvent.validateAgeAtEvent(sAAE, "lt")
      tAAE == false
    }*/

    /*header = "'doGedcomAgeAtEvent' process '<99M 11m 22d'"
      header >> {
      val sAAE = "<99M 11m 22d"
      val tAAE = AgeAtEvent.doGedcomAgeAtEvent(sAAE, "lt")
      print("-------------------- "); println(header)
      println(<_>|{sAAE}| ==&gt; |{tAAE}|</_>.text)
      tAAE must_== "<99y 11m 22d"
    }*/

  }

}