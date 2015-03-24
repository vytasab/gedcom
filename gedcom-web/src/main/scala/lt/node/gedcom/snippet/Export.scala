package lt.node.gedcom.snippet

import net.liftweb.http.{RequestVar, S}
import net.liftweb.common.{Box, Empty}
import lt.node.gedcom.model.{Person, Model}
import lt.node.gedcom.util.Utilits

import _root_.net.liftweb.util.Helpers._
import lt.node.gedcom.rest.GedcomRest
import java.lang.StringBuffer
import org.slf4j.{LoggerFactory,Logger}
//import org.apache.log4j.Level

//import java.util.logging.Level

/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 1/17/12
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */

class Export {

  object personVar extends RequestVar[Box[Person]](Empty)

  val log: Logger = LoggerFactory.getLogger("Export");
  //Logger.getLogger("org.hibernate").setLevel(Level.OFF)

  def doAll = {
    GedcomRest.emptyPids
    GedcomRest.emptyFids
    val gedFileTop = Utilits.gedcomHEAD(<_>INDI-0.ged</_>.text)
    val gedText = new StringBuffer();
    val ansdesxxx: Tuple3[Int, Int, Boolean] = (S.get("ancestNum").getOrElse("99").toInt,
      S.get("descendNum").getOrElse("99").toInt,
      if (S.get("showSiblings").getOrElse("1") == "1") true else false)
    val persons: List[Person] = Model.createNamedQuery[Person]("findAllPersons").findAll.toList
    for (person <- persons) {
      //GedcomRest.exportPerson(area: Tuple3[Int, Int, Boolean], id: Long, generation: Int, /*jsText*/gedText: StringBuffer, sbIdGen: StringBuffer): Unit = {
      GedcomRest.exportPerson(/*(04,04,true)*/ansdesxxx, person.id, 0, gedText)
    }
    val gedFileMid: String = gedText.toString()
    val gedFile =  gedFileTop + gedFileMid + Utilits.gedcomTRLR()
    "#gedTitle" #> <span><_>{S.?("export.gedcom")}</_>.text</span> &
    "#gedFile" #> <pre>{gedFile}</pre>
  }


  def doPart = {
    GedcomRest.emptyPids// = PersonIds.set(List()) /*pIds = List()*/
    GedcomRest.emptyFids// = FamilyIds.set(List()) /*fIds = List()*/
    //val person: Option[Person] = Model.find(classOf[Person], S.getSessionAttribute("personId").openOr("1").toLong)
    val gedFileTop = Utilits.gedcomHEAD(<_>INDI-{S.getSessionAttribute("personId").openOr("ISNOT")}.ged</_>.text)
    val gedText = new StringBuffer();
    val gedFileMid: String = (S.getSessionAttribute("personId").openOr("0").toLong) match {
      case 0L => """"""
      case _ =>
        //GedcomRest.exportPerson(area: Tuple3[Int, Int, Boolean], id: Long, generation: Int, /*jsText*/gedText: StringBuffer, sbIdGen: StringBuffer): Unit = {
        GedcomRest.exportPerson(/*(04,04,true)*/(S.get("ancestNum").getOrElse("99").toInt,
          S.get("descendNum").getOrElse("99").toInt,
          if (S.get("showSiblings").getOrElse("1") == "1") true else false),
          S.getSessionAttribute("personId").openOr("0").toLong, 0, gedText)
        gedText.toString()
    }
      val gedFile =  gedFileTop + gedFileMid + Utilits.gedcomTRLR()
      "#gedTitle" #> <span><_>Person id={S.getSessionAttribute("personId").openOr("ISNOT")} {S.?("export.gedcom")}</_>.text</span> &
      "#gedFile" #> <pre>{gedFile}</pre>
  }

}