package lt.node.gedcom.model

import org.specs.runner._
import org.specs.Specification

import scala.xml._

/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 3/19/11
 * Time: 12:09 PM
 * To change this template use File | Settings | File Templates.
 */

// run ==> Ctrl + Alt + F10

//class AuditHelperTest
//class aMultiLangTextTest extends Runner /*Main*/ (MultiLangTextSpec) with JUnit with Console
class aAuditHelperTest extends Runner(AuditHelperSpec) with JUnit with Console

object AuditHelperSpec /*MultiLangTextSpec*/ extends Specification("AuditHelper functionality testing") {
// http://code.google.com/p/specs/wiki/MatchersGuide
  "AuditHelper" should {

//    "'hello world' has 11 characters" in {
//      "hello world".size must_== 11
//    }
//    "'hello world' matches 'h.* w.*'" in {
//      "hello world" must be matching ("h.* w.*")
//    }
//    "'Sveiki visi!' has 11 characters" in {
//      "Sveiki visi!".size must_== 12
//    }

    val header0 = "'hasLang' returns true"
    header0 >> {
      val iniText = XML.loadString("""<_ d="lt"><lt>some plain text</lt></_>""")
      println(<_>|{/*Unparsed*/(iniText).toString}|</_>.text)
      print("-------------------- "); println(header0)
      println(<_>||{AuditHelper.hasLang(/*Unparsed*/(iniText), "lt")}||</_>.text)
      true == (AuditHelper.hasLang(/*Unparsed*/(iniText), "lt"))
    }

    val header00 = "'getLangMsg' returns plain nonzero-length string"
    header00 >> {
      //val iniText = "<_ d=\"lt\"><lt>some plain text</lt></_>"
      val iniText = XML.loadString("""<_ d="lt"><lt>some plain text</lt></_>""")
      print("-------------------- "); println(header00)
      println(<_>|{AuditHelper.getLangMsg(/*Unparsed*/(iniText), "lt")}|</_>.text)
      "some plain text" must equalIgnoreSpace (AuditHelper.getLangMsg(/*Unparsed*/(iniText), "lt"))
    }

    val header = "'checkAddField' zero-Value field returns NodeSeq.Empty"
    header >> {
      //val ah = AuditHelper.checkAddField("fldA", "", "")
      val ah = AuditHelper.checkAddFields(List(("fldA", "", "")))
      print ("-------------------- "); println(header)
      println(<_>|{ah.toString}|</_>.text)
      ah.toString.size must beLessThanOrEqualTo (0)
    }

    val header1 = "'checkAddField' LT value field returns <f n'=...'><lt> ... </lt></f> node"
    header1 >> {
      val iniText = """<_ d="lt"><lt>fldB LT tekstas ąčęėįšųūžĄČĘĖĮŠŲŪŽ</lt></_>"""
      //val ah = AuditHelper.checkAddField("fldB", "lt", (iniText))
      val ah = AuditHelper.checkAddFields(List(("fldB", "lt", iniText)))
      print("-------------------- "); println(header1)
      println(<_>|{ah.toString}|</_>.text)
      ah must equalIgnoreSpace (<f n="fldB"><lt>fldB LT tekstas ąčęėįšųūžĄČĘĖĮŠŲŪŽ</lt></f>)
    }

    val header2 = "'checkAddField' value field returns <f n'=...'> ... </f> node"
    header2 >> {
      //val ah = AuditHelper.checkAddField("fldC", "", "fldB tekstas ąčęėįšųūžĄČĘĖĮŠŲŪŽ")
      val ah = AuditHelper.checkAddFields(List(("fldC", "", "fldB tekstas ąčęėįšųūžĄČĘĖĮŠŲŪŽ")))
      print("-------------------- "); println(header2)
      println(<_>|{ah.toString}|</_>.text)
      ah must equalIgnoreSpace (<f n="fldC">fldB tekstas ąčęėįšųūžĄČĘĖĮŠŲŪŽ</f>)
    }

    val header3 = "'checkAddFields' contains several items"
    header3 >> {    //       val iniText = "<_ d=\"lt\"><lt>some plain text</lt></_>"
      val ah: NodeSeq = AuditHelper.checkAddFields(List(
        //("fldA", "", null),
        ("fldA", "", ""),
        ("fldB", "lt", """<_ d="lt"><lt>fldB LT tekstas ąčęėįšųūžĄČĘĖĮŠŲŪŽ</lt></_>"""),
        ("fldC", "", "fldC tekstas ąčęėįšųūžĄČĘĖĮŠŲŪŽ")
        ))
      print("-------------------- "); println(header3)
      println(<_>|{ah.toString}|</_>.text)
      1 must beGreaterThan(0)
    }

    val header_1 = "'checkChanges' contains several items"
    header3 >> {
      val ah: NodeSeq = AuditHelper.checkChanges(List(
        ("fld_A", "", "fldA LT tekstas ąčęėįšųūž", "fldB LT tekstas ĄČĘĖĮŠŲŪŽ"),
        ("fld_B", "lt", """<_ d="lt"><lt>fldB LT tekstas</lt></_>""", """<_ d="lt"><lt>fldB LT tekstas</lt></_>"""),
        ("fld_BB", "lt", """<_ d="lt"><lt>fldBB LT tekstas ąčęėįšųūž</lt></_>""", """<_ d="lt"><lt>fldBB LT tekstas ĄČĘĖĮŠŲŪŽ</lt></_>"""),
        ("fld_C", "", "fldC tekstas ąčęėįšųūž", ""),
        ("fld_D", "", "", "fldD tekstas ĄČĘĖĮŠŲŪŽ"),
        ("fld_E", "lt", """<_ d="lt"><lt>fldE LT tekstas</lt></_>""", ""),
        ("fld_F", "lt", "", """<_ d="lt"><lt>fldF LT tekstas</lt></_>"""),
        ("fld_X", "", "", "")
        ))
      print("-------------------- "); println(header_1)
      println(<_>|{ah.toString}|</_>.text)
      1 must beGreaterThan(0)
    }


  }

}