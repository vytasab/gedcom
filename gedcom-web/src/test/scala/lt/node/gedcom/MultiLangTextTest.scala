package lt.node.gedcom

import org.specs.runner._
import org.specs.Specification
import util.MultiLangText


/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 3/14/11
 * Time: 2:25 PM
 * To change this template use File | Settings | File Templates.
 */

//class aMultiLangTextTest extends SpecificationWithJUnit /*with ScalaTest*/ {
class aMultiLangTextTest extends Runner /*Main*/ (MultiLangTextSpec) with JUnit with Console
// https://github.com/lift/lift/blob/master/framework/lift-base/lift-webkit/src/test/scala/net/liftweb/http/SnippetSpec.scala

object MultiLangTextSpec extends
Specification /*WithJUnit*/ ("MultiLangText functionality testing") /*with ScalaTest*/ {
  // neveikia: override var description = "MultiLangText functionality testing aprašymas"
  val dbField = "dbField"

  "MultiLangText" should /* or 'can' instead of 'should' */ {

    "'hello world' has 11 characters" in {
      "hello world".size must_== 11
    }
    "'hello world' matches 'h.* w.*'" in {
      "hello world" must be matching ("h.* w.*")
    }
    "'Sveiki visi!' has 11 characters" in {
      "Sveiki visi!".size must_== 12
    }

    "'wrapText' process plain text" >> {
      val iniText = ""
      val mlt = new MultiLangText(dbField, iniText)
      MultiLangText.wrapText("plynas tekstas", "lt").toString must_== "<lt>plynas tekstas</lt>"
    }

    "'wrapText' process plain lithuanian text" >> {
      val iniText = ""
      val mlt = new MultiLangText(dbField, iniText)
      val textLt = "plynas LT ąčęėįšųūžĄČĘĖĮŠŲŪŽ tekstas"
      println()
      println("'wrapText' process plain lithuanian text")
      println(MultiLangText.wrapText(textLt, "lt").toString())
      MultiLangText.wrapText(textLt, "lt").toString must_== "<lt>" + textLt + "</lt>"
    }

    //"'wrapText' process rich text" >> {
    //  val iniText = ""
    //  val mlt = new MultiLangText(dbField, iniText)
    //  val textLt = "neplynas <b>storas</b> tekstas"
    //  println()
    //  println(mlt.wrapText(textLt, "lt").toString())
    //  mlt.wrapText(textLt, "lt").toString must_== "<lt>" + textLt + "</lt>"
    //}

    "'txt2xml' process 0-length text" >> {
      val iniText = ""
      val mlt = new MultiLangText(dbField, iniText)
      println()
      println("'txt2xml' process 0-length text")
      println(MultiLangText.txt2xml(iniText, "lt").toString())
      MultiLangText.txt2xml(iniText, "lt")/*.toString*/ must equalIgnoreSpace(<_ d="lt"></_>)
      // must_== " <_ d=""" + textLt + """</_>"
    }

    "'txt2xml' process plain text" >> {
      val iniText = "some plain text"
      val mlt = new MultiLangText(dbField, iniText)
      println("")
      println("'txt2xml' process plain text")
      println(MultiLangText.txt2xml(iniText, "lt").toString())
      MultiLangText.txt2xml(iniText, "lt")/*.toString*/ must equalIgnoreSpace(<_ d="lt"><lt>some plain text</lt></_>)
    }

    "'txt2xml' process plain MultiLangText text" >> {
      val iniText = "<_ d=\"lt\"><lt>some plain text</lt></_>"
      val mlt = new MultiLangText(dbField, iniText)
      println("")
      println("'txt2xml' process plain MultiLangText text")
      println(MultiLangText.txt2xml(iniText, "lt").toString())
      MultiLangText.txt2xml(iniText, "lt")/*.toString*/ must equalIgnoreSpace(<_ d="lt"><lt>some plain text</lt></_>)
    }

    "'hasLang' checks for 'lt' existence" >> {
      val iniText = ""
      val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val mlt = new MultiLangText(dbField, iniText)
      println("")
      println("'hasLang' checks for 'lt' existence")
      println(MultiLangText.hasLang(initXml, "lt").toString())
      MultiLangText.hasLang(initXml, "lt") must be (true)
    }

    "'hasLang' checks for 'ru' absence" >> {
      val iniText = ""
      val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val mlt = new MultiLangText(dbField, iniText)
      println("")
      println("'hasLang' checks for 'ru' absence")
      println(MultiLangText.hasLang(initXml, "ru").toString())
      MultiLangText.hasLang(initXml, "ru") must be (false)
    }

    "'getLangMsgXml' returns 'lt' XML node" >> {
      //val iniText = "<_ d=\"lt\"><lt>plynas tekstas</lt><en>some plain text</en></_>"
      val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val iniText = initXml.toString
      val mlt = new MultiLangText(dbField, "")
      println("")
      println("----- 'getLangMsgXml' returns 'lt' XML node")
      println(mlt.getLangMsgXml(iniText, "lt").toString())
      mlt.getLangMsgXml(iniText, "lt") must equalIgnoreSpace (<lt>plynas tekstas</lt>)
    }

    "'getLangMsgXml' (via class param) returns 'lt' XML node" >> {
      //val iniText = "<_ d=\"lt\"><lt>plynas tekstas</lt><en>some plain text</en></_>"
      val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val iniText = initXml.toString
      val mlt = new MultiLangText(dbField, iniText)
      println("")
      println("----- 'getLangMsgXml' (via class param) returns 'lt' XML node")
      println(mlt.getLangMsgXml(/*iniText, */"lt").toString())
      mlt.getLangMsgXml(/*iniText, */"lt") must equalIgnoreSpace (<lt>plynas tekstas</lt>)
    }

    "'getLangMsg' returns 'lt' text" >> {
      val iniText = "<_ d=\"lt\"><lt>plynas tekstas</lt><en>some plain text</en></_>"
      //val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val mlt = new MultiLangText(dbField, "")
      println("")
      println("'getLangMsg' returns 'lt' text")
      println(mlt.getLangMsg(iniText, "lt").toString())
      mlt.getLangMsg(iniText, "lt") must be_== ("plynas tekstas")
    }

    "'getLangMsg' returns 0-length 'ru' text" >> {
      val iniText = "<_ d=\"lt\"><lt>plynas tekstas</lt><en>some plain text</en></_>"
      val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val mlt = new MultiLangText(dbField, "")
      println("")
      println("'getLangMsg' returns 0-length 'ru' text")
      println("|"+mlt.getLangMsg(iniText, "ru").toString()+"|")
      mlt.getLangMsg(iniText, "ru") must be_== ("")
    }

    "'delLangMsg' deletes 'lt' text from MultiLangText" >> {
      val iniText = "<_ d=\"lt\"><lt>plynas tekstas</lt><en>some plain text</en></_>"
      val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val mlt = new MultiLangText(dbField, "")
      println("")
      println("'delLangMsg' deletes 'lt' text from MultiLangText")
      println("|"+initXml.toString()+"|")
      println("|"+mlt.delLangMsg(initXml, "lt").toString()+"|")
      mlt.delLangMsg(initXml, "lt") must equalIgnoreSpace (<_ d="lt"><en>some plain text</en></_>)
    }

    "'delLangMsg' (via class param) deletes 'lt' text from MultiLangText" >> {
      //val iniText = "<_ d=\"lt\"><lt>plynas tekstas</lt><en>some plain text</en></_>"
      val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val iniText = initXml.toString
      val mlt = new MultiLangText(dbField, iniText)
      println("")
      println("'delLangMsg' (via class param) deletes 'lt' text from MultiLangText")
      println("|"+iniText+"|")
      println("|"+mlt.delLangMsg(/*initXml, */"lt").toString()+"|")
      println("      delLangMsg audit " + mlt.audit)
      mlt.delLangMsg(/*initXml, */"lt") must equalIgnoreSpace (<_ d="lt"><en>some plain text</en></_>)
    }

    "'delLangMsg' ignores absent 'xx' text deletion from MultiLangText" >> {
      val iniText = "<_ d=\"lt\"><lt>plynas tekstas</lt><en>some plain text</en></_>"
      val initXml = <_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>
      val mlt = new MultiLangText(dbField, "")
      println("")
      println("'delLangMsg' ignores absent 'xx' text deletion from MultiLangText")
      println("|"+initXml.toString()+"|")
      println("|"+mlt.delLangMsg(initXml, "xx").toString()+"|")
      mlt.delLangMsg(initXml, "xx") must equalIgnoreSpace (<_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>)
    }

    "'addLangMsg' adds 'lt' text to MultiLangText" >> {
      val initXml = <_ d="lt"><en>some plain text</en></_>
      val ltXml = <lt>plynas tekstas</lt>
      val mlt = new MultiLangText(dbField, "")
      println("")
      println("'addLangMsg' adds 'lt' text to MultiLangText")
      println("|"+initXml.toString()+"|")
      println("|"+mlt.addLangMsg(initXml, ltXml).toString()+"|")
      println("      addupdLangMsg audit " + mlt.audit)
      mlt.addLangMsg(initXml, ltXml) must equalIgnoreSpace (<_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>)
    }

    "'addupdLangMsg' adds 'lt' text to empty MultiLangText" >> {
      //val iniText = "<_ d=\"lt\"><en>some plain text</en></_>"
      val iniText = ""
      //val initXml = <_ d="lt"><en>some plain text</en></_>
      val ltTxt = "plynas tekstas"
      val mlt = new MultiLangText(dbField, "")
      println("")
      println("'addupdLangMsg' adds 'lt' text to empty MultiLangText")
      println("|"+iniText+"|")
      println("|"+mlt.addupdLangMsg(iniText/*, ltTxt, "lt"*/)+"|")
      println("      addupdLangMsg audit " + mlt.audit)
      mlt.addupdLangMsg(iniText/*, ltTxt, "lt"*/) must equalIgnoreSpace (<_ d="lt"><lt>plynas tekstas</lt></_>.toString)
    }

    "'addupdLangMsg' update 'lt' text to MultiLangText" >> {
      val initXml = <_ d="lt"><en>some plain text</en><lt>plynas tekstas</lt></_>
      val iniText = initXml.toString
      val ltTxt = "pataisytas plynas tekstas"
      val mlt = new MultiLangText(dbField, iniText)
      println("")
      println("'addupdLangMsg' update 'lt' text to MultiLangText")
      println("|"+iniText+"|")
      println("|"+mlt.addupdLangMsg(iniText/*, ltTxt, "lt"*/).toString()+"|")
      println("      addupdLangMsg audit " + mlt.audit)
      mlt.addupdLangMsg(iniText/*, ltTxt, "lt"*/) must equalIgnoreSpace (<_ d="lt"><en>some plain text</en><lt>pataisytas plynas tekstas</lt></_>.toString)
    }

    "'addupdLangMsg' adds 'en' text to non-empty MultiLangText" >> {
      //val iniText = "<_ d=\"lt\"><en>some plain text</en></_>"
      val iniText = <_ d="lt"><lt>plynas tekstas</lt></_>.toString
      //val initXml = <_ d="lt"><en>some plain text</en></_>
      val ltTxt = "some plain text"
      val mlt = new MultiLangText(dbField, "")
      println("")
      println("'addupdLangMsg' adds 'en' text to empty MultiLangText")
      println("|"+iniText+"|")
      println("|"+mlt.addupdLangMsg(iniText/*, ltTxt, "en"*/)+"|")
      println("      addupdLangMsg audit " + mlt.audit)
      mlt.addupdLangMsg(iniText/*, ltTxt, "en"*/) must equalIgnoreSpace (<_ d="lt"><lt>plynas tekstas</lt><en>some plain text</en></_>.toString)
    }

    "'addupdLangMsg' adds 'pl' text to non-empty MultiLangText" >> {
      //val iniText = "<_ d=\"lt\"><en>some plain text</en></_>"
      val iniText = <_ d="lt"><lt>plynas tekstas</lt></_>.toString
      //val initXml = <_ d="lt"><en>some plain text</en></_>
      val plTxt = "polski wariant tekstu"
      val mlt = new MultiLangText(dbField, iniText)
      println("")
      println("'addupdLangMsg' adds 'pl' text to non-empty MultiLangText")
      println("|"+iniText+"|")
      println("      addupdLangMsg audit " + mlt.audit)
      println("|"+mlt.addupdLangMsg(/*iniText, */plTxt/*, "pl"*/)+"|")
      mlt.addupdLangMsg(/*iniText, */plTxt/*, "pl"*/) must equalIgnoreSpace (<_ d="lt"><lt>plynas tekstas</lt><pl>polski wariant tekstu</pl></_>.toString)
    }

  }

}