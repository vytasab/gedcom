package lt.node.gedcom.snippet

import lt.node.gedcom.model
import model._
import scala.Some
import bootstrap.liftweb.ErrorXmlMsg

import _root_.net.liftweb._
import http._
import common._
import _root_.net.liftweb.util.Helpers._

/**
 * Created with IntelliJ IDEA.
 * User: vsh
 * Date: 11/30/12
 * Time: 12:06 AM
 * To change this template use File | Settings | File Templates.
 */

/*
// http://simply.liftweb.net/index-15.2.html#prev
object ApplyFileUpload extends Loggable {
  val log = Logger("ApplyFileUpload");
  def render = {
    log.debug("ApplyFileUpload []... ")
    <lift:embed what="/gedcom/addMultiMediaInt"/>
  }
}
*/

class MultiMediaCRUD {

  val log = Logger("MultiMediaCRUD")

  def add2Pe() = {
    val ed: EventDetail = EDReading4MM.apply().get

  }
  def render1: net.liftweb.util.CssSel = {
              "#mmwiz1" #> <span>
                <button class="lift:MultiMediaWizardRunner.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
  }

/*
    def editPe: net.liftweb.util.CssSel = {
      logSomeSessAttrs("editPe")
      PersonReading()
      "#pewiz" #> <span>
        <button class="lift:PeWizard">
          <lift:loc>wiz.upd.pe</lift:loc>
          <img src="/images/page_edit.gif" />
        </button>
        <br/>
      </span>
    }
   */

}


object personVar4MM extends RequestVar/*SessionVar*/[Box[Person]](Empty)

object familyVar4MM extends RequestVar/*SessionVar*/[Box[Family]](Empty)
//object familyReqVar extends RequestVar[Map[Int, Family]](Map.empty)

object eventDetailVar4MM extends RequestVar/*SessionVar*/[Box[EventDetail]](Empty)

object peVar4MM extends RequestVar/*SessionVar*/[Box[PersonEvent]](Empty)

object paVar4MM extends RequestVar/*SessionVar*/[Box[PersonAttrib]](Empty)

object feVar4MM extends RequestVar/*SessionVar*/[Box[FamilyEvent]](Empty)

//object detailVar extends RequestVar/*SessionVar*/[Box[EventDetail]](Empty)


object PeReading4MM extends Loggable {
  val log = Logger("PeReading4MM")
  def apply(): Box[Person] = {
    personVar4MM.is match {
      case Full(p) => Full(p)
      case _ =>
        val person: Option[Person] = Model.find(classOf[Person], S.getSessionAttribute("personId").open_!/*.openOr("1")*/.toLong)
        person match {
          case Some(p) =>
            /*val fams: Map[Int, Family] = p.families(Model.getUnderlying).zipWithIndex.map((kv) =>(kv._2+1, kv._1)).toMap
            log.debug("fams: Map [Int, Family] " + fams.toString)
            familyReqVar.set(fams)*/
            personVar4MM.set(Full(p))
          case _ =>
            Empty
        }
    }
  }
}

object FaReading4MM extends Loggable {
  val log = Logger("FaReading4MM")
  def apply(): Box[Family] = {
    familyVar4MM.is match {
      case Full(p) => Full(p)
      case _ =>
        val family: Option[Family] = Model.find(classOf[Family], S.getSessionAttribute("familyId").open_!/*.openOr("1")*/.toLong)
        family match {
          case Some(p) =>
            familyVar4MM.set(Full(p))
          case _ =>
            Empty
        }
    }
  }
}

object EDReading4MM extends Loggable {
  val log = Logger("EDReading4MM")
  def apply(): Box[EventDetail] = {
    S.getSessionAttribute("role").openOr("Err") match {
      case "PE" =>
        eventDetailVar4MM.is match {
          case Full(i) => Full(i)
          case _ =>
            val item: Option[PersonEvent] = Model.find(classOf[PersonEvent], S.getSessionAttribute("idParentED").open_!/*.openOr("1")*/.toLong)
            item match {
              case Some(ii) =>
                ii.getEventDetail(Model.getUnderlying)
                ii.eventdetails.size() match {
                  case 0 =>
                    val place = "EDReading4MM"
                    val msg = ("EDReading4MM: There is no EventDetail for PersonEvent id = " + S.getSessionAttribute("idParentED").open_!)
                    log.error(place+"; "+msg)
                    S.redirectTo("/errorPage", () => {
                      ErrorXmlMsg.set(Some(Map(
                        "location" -> <p>{place}</p>,
                        "message" -> <p>{msg}</p>)))
                    })
                    Empty
                  //case 1 => eventDetailVar4MM.set(Full(ii.eventdetails.iterator().next()))
                  case _ => eventDetailVar4MM.set(Full(ii.eventdetails.iterator().next()))
                }
              case _ =>
                val place = "EDReading4MM"
                val msg = ("EDReading4MM: There is no PersonEvent for PersonEvent id = " + S.getSessionAttribute("idParentED").open_!)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>{place}</p>,
                    "message" -> <p>{msg}</p>)))
                })
                Empty
            }
        }
      case "PA" =>
        eventDetailVar4MM.is match {
          case Full(i) => Full(i)
          case _ =>
            val item: Option[PersonAttrib] = Model.find(classOf[PersonAttrib], S.getSessionAttribute("idParentED").open_!/*.openOr("1")*/.toLong)
            item match {
              case Some(ii) =>
                ii.getAttribDetail(Model.getUnderlying)
                ii.attribdetails.size() match {
                  case 0 =>
                    val place = "EDReading4MM"
                    val msg = ("EDReading4MM: There is no EventDetail for PersonAttrib id = " + S.getSessionAttribute("idParentED").open_!)
                    log.error(place+"; "+msg)
                    S.redirectTo("/errorPage", () => {
                      ErrorXmlMsg.set(Some(Map(
                        "location" -> <p>{place}</p>,
                        "message" -> <p>{msg}</p>)))
                    })
                    Empty
                  //case 1 => eventDetailVar4MM.set(Full(ii.eventdetails.iterator().next()))
                  case _ => eventDetailVar4MM.set(Full(ii.attribdetails.iterator().next()))
                }
              case _ =>
                val place = "EDReading4MM"
                val msg = ("EDReading4MM: There is no PersonAttrib for PersonAttrib id = " + S.getSessionAttribute("idParentED").open_!)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>{place}</p>,
                    "message" -> <p>{msg}</p>)))
                })
                Empty
            }
        }
      case "FE" =>
        eventDetailVar4MM.is match {
          case Full(i) => Full(i)
          case _ =>
            val item: Option[FamilyEvent] = Model.find(classOf[FamilyEvent], S.getSessionAttribute("idParentED").open_!/*.openOr("1")*/.toLong)
            item match {
              case Some(ii) =>
                ii.getEventDetail(Model.getUnderlying)
                ii.familydetails.size() match {
                  case 0 =>
                    val place = "EDReading4MM"
                    val msg = ("EDReading4MM: There is no EventDetail for FamilyEvent id = " + S.getSessionAttribute("idParentED").open_!)
                    log.error(place+"; "+msg)
                    S.redirectTo("/errorPage", () => {
                      ErrorXmlMsg.set(Some(Map(
                        "location" -> <p>{place}</p>,
                        "message" -> <p>{msg}</p>)))
                    })
                    Empty
                  //case 1 => eventDetailVar4MM.set(Full(ii.eventdetails.iterator().next()))
                  case _ => eventDetailVar4MM.set(Full(ii.familydetails.iterator().next()))
                }
              case _ =>
                val place = "EDReading4MM"
                val msg = ("EDReading4MM: There is no FamilyEvent for FamilyEvent id = " + S.getSessionAttribute("idParentED").open_!)
                log.error(place+"; "+msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>{place}</p>,
                    "message" -> <p>{msg}</p>)))
                })
                Empty
            }
        }
      case _ =>
        val place = "EDReading4MM"
        val msg = ("EDReading4MM: Unexpected role " + S.getSessionAttribute("role").open_!)
        log.error(place+"; "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>{place}</p>,
            "message" -> <p>{msg}</p>)))
        })
        Empty
    }
  }
}
