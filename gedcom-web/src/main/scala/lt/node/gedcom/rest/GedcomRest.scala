package lt.node.gedcom.rest

import _root_.scala.xml.NodeSeq
import org.slf4j.{LoggerFactory, Logger}
import _root_.net.liftweb._
import http.rest._
import http._
import lt.node.gedcom.util.{GedcomUtil, MultiLangText}
import common._
import _root_.lt.node.gedcom.model._
import bootstrap.liftweb.{ErrorXmlMsg, PersonIds, FamilyIds}

object GedcomRest extends XMLApiHelper with Loggable {

  val rootId = S.getSessionAttribute("personId").openOr("1").toLong

  val log: Logger = LoggerFactory.getLogger("GedcomRest")

  val bundleTextList = List(
    "js_add_family",
    "js_add_father", "js_add_mother",
    "js_add_spouse", "js_add_husband", "js_add_wife",
    "js_add_brother", "js_add_sister",
    "js_add_son",     "js_add_daughter",
    "js_full_info",   "js_cancel",   /*"js_go_home",*/
    "js_go2PeData",   "js_go2ChgPe",   "js_go2FaAct",
    "js_goUp", "js_goRight", "js_goLeft", "js_goDown", "js_goInit", "js_canvas_center" )
  /*
  js_goInit=restore init view
js_canvas_center=Your mouse click position in canvas will be moved here
 */

  def emptyPids = PersonIds.set(List()) /*pIds = List()*/

  def emptyFids = FamilyIds.set(List()) /*fIds = List()*/


  def xIsNotYetInJS(xIds: SessionVar [List[Long]], xId: Long): Boolean = {
    val result = xIds.get.exists(id => id == xId)
    log.debug(<_>xIsNotYetInJS={xIds.get.toString()} xId={xId} result={result}</_>.text)
    if (!result) xIds.set(xId :: xIds.get)
    log.debug(<_>xIsNotYetInJS={xIds.get.toString()}</_>.text)
    !result
  }


  def pIsNotYetInJS(xId: Long): Boolean = {
    this.xIsNotYetInJS(PersonIds, xId)
  }


  def fIsNotYetInJS(xId: Long): Boolean = {
    this.xIsNotYetInJS(FamilyIds, xId)
  }


  def dispatch: LiftRules.DispatchPF = {
    case Req(List("rest", "person", id), _, GetRequest) =>
      S.setSessionAttribute("personId", id)
      S.redirectTo("/gedcom/forest")

    case Req(List("rest", "richPeListByXn"), _, GetRequest) =>
      S.redirectTo("/gedcom/richPersonList")

    case Req(List("rest", "personView", epId), _, GetRequest) =>
      log.debug("('rest', 'personView', epId)")
      S.setSessionAttribute("personId", epId)
      S.unsetSessionAttribute("role")
      S.redirectTo("/gedcom/personView")

    case Req(List("rest", "editPe", peId), _, GetRequest) =>
      log.debug("('rest', 'editPe', peId)")
      S.setSessionAttribute("personEventId", peId)
      S.unsetSessionAttribute("personAttribId")
      S.unsetSessionAttribute("role")
      S.redirectTo("/gedcom/editPe")
    case Req(List("rest", "deletePe", peId), _, GetRequest) =>
      log.debug("('rest', 'deletePe', peId)")
      S.setSessionAttribute("personEventId", peId)
      S.unsetSessionAttribute("personAttribId")
      S.unsetSessionAttribute("role")
      //S.redirectTo("/_deletePe")
      //S.redirectTo("/gedcom/_deletePe")
      S.redirectTo("/gedcom/deletePe")
    case Req(List("rest", "editPa", paId), _, GetRequest) =>
      log.debug("('rest', 'editPa', paId)")
      S.setSessionAttribute("personAttribId", paId)
      S.unsetSessionAttribute("personEventId")
      S.unsetSessionAttribute("role")
      S.redirectTo("/gedcom/editPa")
    case Req(List("rest", "deletePa", paId), _, GetRequest) =>
      log.debug("('rest', 'deletePa', paId)")
      S.setSessionAttribute("personAttribId", paId)
      S.unsetSessionAttribute("personEventId")
      S.unsetSessionAttribute("role")
      S.redirectTo("/gedcom/deletePa")
    case Req(List("rest", "editFe", feId), _, GetRequest) =>
      log.debug("('rest', 'editFe', "+feId.toString+")")
      S.setSessionAttribute("familyEventId", feId)
      S.unsetSessionAttribute("role")  // ?
      S.redirectTo("/gedcom/editFe")
    case Req(List("rest", "deleteFe", feId), _, GetRequest) =>
      log.debug("('rest', 'deleteFe', feId)")
      S.setSessionAttribute("familyEventId", feId)
      S.unsetSessionAttribute("role")  // ?
      S.redirectTo("/gedcom/deleteFe")

    case Req(List("export", "exportAll", id), _, GetRequest) =>
      S.setSessionAttribute("personId", id)
      S.unsetSessionAttribute("role")
      S.redirectTo("/gedcom/personView")
      //S.redirectTo("/gedcom/forest")
    case Req(List("export", "exportPart", id), _, GetRequest) =>
      S.setSessionAttribute("personId", id)
      S.unsetSessionAttribute("role")
      S.redirectTo("/addendum/doExportPart")



    case Req(List("rest", "personUpdate", epId), _, GetRequest) =>
      log.debug("('rest', 'personUpdate', epId)")
      S.setSessionAttribute("personId", epId)
      S.unsetSessionAttribute("role")
      S.redirectTo("/gedcom/personUpdate")
    case Req(List("gedcom", "personUpdate", epId), _, GetRequest) =>
      log.debug("('gedcom', 'personUpdate', epId)")
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("role", "upd")
      S.redirectTo("/gedcom/addeditPerson") // only Person entity
    case Req(List("gedcom", "personDelete", epId), _, GetRequest) =>
      log.debug("('gedcom', 'personDelete', epId)")
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("role", "del")
      S.redirectTo("/gedcom/personDelete") // only Person entity
    case Req(List("gedcom", "familyChildDelete", parentId, childId), _, GetRequest) =>
      log.debug("('gedcom', 'familyChildDelete', parentId, childId)")
      S.setSessionAttribute("personId", parentId)
      S.setSessionAttribute("childId", childId)
      S.setSessionAttribute("role", "fcDel")
      S.redirectTo("/gedcom/familyChildDelete")
    case Req(List("gedcom", "familyDelete", spouseId, familyId), _, GetRequest) =>
      log.debug("('gedcom', 'familyDelete', spouseId, familyId)")
      S.setSessionAttribute("personId", spouseId)
      S.setSessionAttribute("familyId", familyId)
      S.setSessionAttribute("role", "famDel")
      S.redirectTo("/gedcom/familyDelete")
    case Req(List("rest", "person", epId, "event", tag_or_id), _, GetRequest) =>
      // N.B. !!! -- tag CANNOT be numeral
      log.debug("('rest', 'person', epId, 'event', tag_or_id)")
      S.setSessionAttribute("personId", epId)
      tag_or_id.matches("\\d+") match {
        case true =>
          S.setSessionAttribute("eventId", tag_or_id)
          S.unsetSessionAttribute("eventTag")
        case _ =>
          S.setSessionAttribute("eventTag", tag_or_id)
          S.unsetSessionAttribute("eventId")
      }
      S.redirectTo("/gedcom/addeditPE")
    case Req(List("rest", "person", epId, "attrib", tag_or_id), _, GetRequest) =>
      // N.B. !!! -- tag CANNOT be numeral
      log.debug("('rest', 'person', epId, 'attrib', tag_or_id)")
      S.setSessionAttribute("personId", epId)
      tag_or_id.matches("\\d+") match {
        case true =>
          S.setSessionAttribute("attribId", tag_or_id)
          S.unsetSessionAttribute("attribTag")
        case _ =>
          S.setSessionAttribute("attribTag", tag_or_id)
          S.unsetSessionAttribute("attribId")
      }
      S.redirectTo("/gedcom/addeditPA")
/*
    def addPe(): Unit = {
      log.debug(<_>selectedPeTag={selectedPeTag}</_>.text);
      S.redirectTo(<_>/rest/person/{personVar.is.open_!.id}/event/{selectedPeTag}</_>.text)
    }
    def addPa(): Unit = {
      log.debug(<_>selectedPaTag={selectedPaTag}</_>.text);
      S.redirectTo(<_>/rest/person/{personVar.is.open_!.id}/attrib/{selectedPaTag}</_>.text)
    }
*/
    case Req(List("rest", epId, "addNewFamily", gender), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", "0")
      S.setSessionAttribute("gender", (if (gender == "M") "F" else "M"))
      S.setSessionAttribute("role", "sF")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", epId, "addHusbandToPerson", id), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", id)
      S.setSessionAttribute("gender", "M")
      S.setSessionAttribute("role", "sH")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", epId, "addWifeToPerson", id), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", id)
      S.setSessionAttribute("gender", "F")
      S.setSessionAttribute("role", "sW")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", epId, "addBrotherToFamily", id), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", id)
      S.setSessionAttribute("gender", "M")
      S.setSessionAttribute("role", "cB")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", epId, "addSisterToFamily", id), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", id)
      S.setSessionAttribute("gender", "F")
      S.setSessionAttribute("role", "cS")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", epId, "addFatherToFamily", id), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", id)
      S.setSessionAttribute("gender", "M")
      S.setSessionAttribute("role", "pF")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", epId, "addMotherToFamily", id), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", id)
      S.setSessionAttribute("gender", "F")
      S.setSessionAttribute("role", "pM")
      S.redirectTo("/gedcom/bindPerson")
    //    case Req(List("rest", efId, "addSpouseToFamily", gender), "", GetRequest) => {
    case Req(List("rest", epId, "addSpouseToFamily", efId), "", GetRequest) =>
      val p = Model.find(classOf[Person], epId.toLong).get
      //      S.setSessionAttribute("personId", (if (gender == "M") f.wifeId else f.husbandId))
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", efId)
      S.setSessionAttribute("gender", (if (p.gender == "M") "F" else "M")) // of future spouse
      S.setSessionAttribute("role", "fSpouse")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", epId, "addSonToFamily", efId), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", efId)
      S.setSessionAttribute("gender", "M")
      S.setSessionAttribute("role", "fSon")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", epId, "addDaughterToFamily", efId), "", GetRequest) =>
      S.setSessionAttribute("personId", epId)
      S.setSessionAttribute("familyId", efId)
      S.setSessionAttribute("gender", "F")
      S.setSessionAttribute("role", "fDaughter")
      S.redirectTo("/gedcom/bindPerson")
    case Req(List("rest", "person", id, "xml"), _, GetRequest) => () =>
      /*GedcomRest.*/ getPersonXML(id)

    /*case Req(List("rest", "addMultiMedia", "Pe", idPe), _, GetRequest) => {
      log.debug("('rest', 'addMultiMedia', 'Pe', idPe)")
      S.setSessionAttribute("role", "Pe")
      S.setSessionAttribute("personId", idPe)
      S.setSessionAttribute("mmActionCUD", "C")
      S.redirectTo("/gedcom/addMultiMedia")
    }*/
    /*// TODO CC12-3/vsh not implemented yet
    case Req(List("rest", "addMultiMedia", "Fa", idFa), _, GetRequest) => {
      log.debug("('rest', 'addMultiMedia', 'Fa', idFa)")
      S.setSessionAttribute("role", "Fa")
      S.setSessionAttribute("familyId", idFa)
      S.setSessionAttribute("mmActionCUD", "C")
      S.redirectTo("/gedcom/addMultiMedia")
    }*/
    case Req(List("rest", "addMultiMedia", role, idXx), _, GetRequest) =>
      log.debug("('rest', 'addMultiMedia', " + role + ", " + idXx + ")")
      S.setSessionAttribute("role", role) // possible values: PE PA FE
      role match {
        case "Pe" =>
          S.setSessionAttribute("personId", idXx) // parent (Person) id of future MultiMedia record
        //case "Fa" =>
        case xx  if (List("PE", "PA", "FE").exists(m => m == xx)) =>
          S.setSessionAttribute("idParentED", idXx) // parent id of future MultiMedia record
        case _ =>
          val place = "GedComRest addMultiMedia"
          val msg = "A role is unexpected |"+ role + "|"
          log.error(place+": "+msg)
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
          })
      }
      S.setSessionAttribute("mmActionCUD", "C")
      S.redirectTo("/gedcom/addMultiMedia")
    case Req(List("rest", "editMultiMedia", idMm), _, GetRequest) =>
      log.debug("('rest', 'editMultiMedia', "+idMm)
      S.setSessionAttribute("idMm", idMm)  // tobe updated MultiMedia record id
      S.setSessionAttribute("mmActionCUD", "U")
      S.redirectTo("/gedcom/editMultiMedia")
    case Req(List("rest", "deleteMultiMedia", idMm), _, GetRequest) =>
      log.debug("('rest', 'deleteMultiMedia', "+idMm)
      S.setSessionAttribute("mmId", idMm)  // tobe deleted MultiMedia record id
      S.setSessionAttribute("mmActionCUD", "D")
      S.redirectTo("/gedcom/deleteMultiMedia")
    case Req(List("rest", _), "", _) => failure _
  }

/*
    case Req(List("rest", "deletePa", paId), _, GetRequest) => {
      log.debug("('rest', 'deletePa', paId)")
      S.setSessionAttribute("personAttribId", paId)
      S.unsetSessionAttribute("personEventId")
      S.unsetSessionAttribute("role")
      S.redirectTo("/gedcom/deletePa")
    }
   */

  def createTag(in: NodeSeq) = {
    // final wrap of responses
    println("[CreateTag] " + in)
    <gedcom>{in}</gedcom>
  }


  def getPersonXML(id: String): Box[LiftResponse] = {
    val aPerson = Model.find(classOf[Person], id.toLong)
    val result: Box[NodeSeq] = aPerson match {
      case Some(p) =>
        Full(<person>
          <id>{p.id}</id>
          <nameGivn>{p.nameGivn}</nameGivn>
          <nameSurn>{p.nameSurn}</nameSurn>
          <gender>{p.gender}</gender>
        </person>)
      case None =>
        Full(<person>
          <id>{id}</id>
          <errmsg>{S.?("no.person.for.this.id")}</errmsg>
        </person>)
    }
    result.toResponse
  }


  def failure(): LiftResponse = {
    //val ret: Box[NodeSeq] = Full(<op id="FAILURE"></op>)
    NotFoundResponse("NotFoundResponse")
  }

  /**
   * id: Long, -- person id
   * skipFamily: Boolean, -- do not process Family of the person
   * generation: Int, --  ...,-2,-1 - parents, 0 - current person, 1,2,... - childrens
   * jsText: StringBuffer, -- JS assoc array
   * sbIdGen: StringBuffer) -- personId,generation [...]
   * return Unit
   */
  // TODO test generarion>1 case

  /**
   * area: _1: the number of generations of ancestors;
   * _2: the number of generations of descendants;
   * _3: true -> show siblings, false - no
   */
  def getPersonJS(area: Tuple3[Int, Int, Boolean], id: Long, generation: Int, jsText: StringBuffer, sbIdGen: StringBuffer): Unit = {
    log.debug("getPersonJS []... id=" + id.toString);
    Model.find(classOf[Person], id) match {
      case Some(z) if this.pIsNotYetInJS(z.id) => {
        (-area._1 <= generation, area._2 >= generation) match {
          case (true, true) /*if rootId == id*/ => {
            //if (!(id != rootId && generation == 0)) {
            log.debug("getPersonJS case true true " + z.toString(Model.getUnderlying));
            // show
            val familyIdPart = z.family match {
              case null => ""
              case _ => <_>p.familyId={z.family.id};</_>.text
            };
            val familyPart = z.family match {
              case null => ""
              case _ => {
                val sbf: StringBuffer = new StringBuffer("");
                getFamilyJS((area._1, area._2, area._3, id), z.family, generation - 1, sbf, sbIdGen)
                sbf.toString
              }
            };
            val fams = z.families(Model.getUnderlying);
            val fdIds = new StringBuffer("");
            val sbFams = new StringBuffer("");
            fams match {
              case x :: xs => {
                log.debug("getPersonJS families =" + x.toString(Model.getUnderlying));
                val sbFam = new StringBuffer("");
                fdIds.append("p.fd='")
                var s = "";
                fams.foreach(fam => {
                  fdIds.append(s + fam.id)
                  getFamilyJS((area._1, area._2, area._3, 0L), fam, generation, sbFams, sbIdGen)
                  sbFams.append(sbFam)
                  s = ","
                })
                fdIds.append("';")
              }
              case Nil =>
            }
            val birtDatePlace: Tuple2[String, String] = this.getPeEvent(z /*Person*/, "BIRT")
            val deatDatePlace: Tuple2[String, String] = this.getPeEvent(z /*Person*/, "DEAT")
            sbIdGen.append(z.id + "," + generation + " ");
            jsText.append("\nvar p={};var r=[];p.r=r;" +
              <_>p.id={z.id};g['p'+p.id]=p;p.generation={generation};p.nameGivn='{z.nameGivn}';p.nameSurn='{z.nameSurn}';p.gender='{z.gender}';</_>.text +
              /* E118-6/vsh */
              /*  <_>p.bd='{birtDatePlace._1}';p.bp='{birtDatePlace._2.replaceAll("'", "")}';</_>.text +
              <_>p.dd='{deatDatePlace._1}';p.dp='{deatDatePlace._2.replaceAll("'", "")}';</_>.text +*/
              (if (birtDatePlace._1.replaceAll("'", "").size>0) <_>p.bd='{birtDatePlace._1.replaceAll("'", "")}';</_>.text else "" ) +
              (if (birtDatePlace._2.replaceAll("'", "").size>0) <_>p.bp='{birtDatePlace._2.replaceAll("'", "")}';</_>.text else "" ) +
              (if (deatDatePlace._1.replaceAll("'", "").size>0) <_>p.dd='{deatDatePlace._1.replaceAll("'", "")}';</_>.text else "" ) +
              (if (deatDatePlace._2.replaceAll("'", "").size>0) <_>p.dp='{deatDatePlace._2.replaceAll("'", "")}';</_>.text else "" ) +
              familyIdPart + fdIds + sbFams + familyPart);
            //}
          }
          case _ =>
            log.debug("getPersonJS case "+ (-area._1 <= generation).toString+" "+(area._2 >= generation).toString+" "+z.toString(Model.getUnderlying));
          /*
                    case (false, false) =>
                      log.debug("getPersonJS case false false " + z.toString(Model.getUnderlying));
                    case (false, true) =>
                      log.debug("getPersonJS case false true " + z.toString(Model.getUnderlying));
                    case (true, false) =>
                      log.debug("getPersonJS case true false " + z.toString(Model.getUnderlying));
          */
        }
      }
      case None => {
        jsText.append("\nvar p={};var r=[];p.r=r;" + <_>p.id={id};g['p'+p.id]=p;p.errmsg='{S.?("no.person.for.this.id")}';</_>.text)
      }
      case _ => {
        log.warn("getPersonJS Model.find(classOf[Person], id) match case _");
      }
    }
  }

  def getPeEvent(pe: Person, evenTag: String): Tuple2 [String/*date*/, String/*place*/] = {
    log.debug("getPeEvent []... ");
    val aList: List[PersonEvent] = pe.personevents.toArray.toList.asInstanceOf[List[PersonEvent]]
    aList.find(pe => pe.tag == evenTag) match {
      case Some(x) =>
        val ed: EventDetail = x.eventdetails.iterator.next
        //(ed.dateValue, (new MultiLangText("place", ed.place)).getLangMsg())
        //log.debug("getPeEvent ###########################|" + ed.place + "|");
        (GedcomUtil.i18nizeGedcomDate(ed.dateValue), (new MultiLangText("place", ed.place)).getLangMsg())
      case _ =>
        ("", "")
    }
  }

  def getFaEvent(fa: Family, evenTag: String): Tuple2 [String/*date*/, String/*place*/] = {
    log.debug("getFaEvent []... ");
    val aList: List[FamilyEvent] = fa.familyevents.toArray.toList.asInstanceOf[List[FamilyEvent]]
    aList.find(fe => fe.tag == evenTag) match {
      case Some(x) =>
        val ed: EventDetail = x.familydetails.iterator.next
        //(ed.dateValue, (new MultiLangText("place", ed.place)).getLangMsg())
        (GedcomUtil.i18nizeGedcomDate(ed.dateValue), (new MultiLangText("place", ed.place)).getLangMsg())
      case _ =>
        ("", "")
    }
  }

  /**
   * area: _1: the number of generations of ancestors;
   * _2: the number of generations of descendants;
   * _3: true -> show siblings, false - no
   * _4: the calling Person id or 0L
   */
  def getFamilyJS(area: Tuple4[Int, Int, Boolean, Long], family: Family, generation: Int, jsText: StringBuffer, sbIdGen: StringBuffer): Unit =
    if (this.fIsNotYetInJS(family.id)) {
      //bc02-4 if (family.children.size > 0) {
      (generation >= 0) match {
        case true =>
          val iter = family.children.iterator
          while (iter.hasNext) {
            val c: Person = iter.next()
            this.getPersonJS((area._1, area._2, area._3), c.id, generation + 1, jsText, sbIdGen)
          }
        case false =>
      }
      ///bc02-4 }
      (-area._1 <= generation, area._2 >= generation) match {
        case (true, true) => {
          log.debug("getFamilyJS case true true " + family.toString(Model.getUnderlying));
          var childrenIds: StringBuffer = new StringBuffer("")
          if (family.husbandId > 0)this.getPersonJS((area._1, area._2, area._3), family.husbandId, generation, jsText, sbIdGen)
          if (family.wifeId > 0)this.getPersonJS((area._1, area._2, area._3), family.wifeId, generation, jsText, sbIdGen)
          if (area._3/* || generation >= 0*/) {
            // show siblings
            (generation >= 0) match {
              case true =>
                var separ = ""
                val iter = family.children.iterator
                while (iter.hasNext) {
                  childrenIds.append(separ + iter.next().id.toString)
                  separ = ","
                }
              case false =>
                childrenIds = childrenIds.append(area._4.toString)
            }
          } else {
            // a caller Person is only one child
            //- childrenIds = childrenIds.append(area._4.toString)
          }
          if (childrenIds.length > 0) childrenIds = new StringBuffer("f.children='" + childrenIds.toString + "';");
          val marrDatePlace: Tuple2[String, String] = this.getFaEvent(family, "MARR")
          val divDatePlace: Tuple2[String, String] = this.getFaEvent(family, "DIV")
          jsText.append("\nvar f={};var r=[];f.r=r;" +
            <_>f.id={family.id};g['f'+f.id]=f;f.generation={generation};f.father={family.husbandId};f.mother={family.wifeId};{childrenIds.toString}</_>.text +
            <_>f.md='{marrDatePlace._1}';f.mp='{marrDatePlace._2.replaceAll("'", "")}';</_>.text +
            <_>f.dd='{divDatePlace._1}';f.dp='{divDatePlace._2.replaceAll("'", "")}';</_>.text
          )
        }
        case _ =>
          log.debug("getFamilyJS case "+ (-area._1 <= generation).toString+" "+(area._2 >= generation).toString+" "+family.toString(Model.getUnderlying));
        /*
                case (false, false) =>
                  log.debug("getFamilyJS case false false " + family.toString(Model.getUnderlying));
                case (false, true) =>
                  log.debug("getFamilyJS case false true " + family.toString(Model.getUnderlying));
                case (true, false) =>
                  log.debug("getFamilyJS case true false " + family.toString(Model.getUnderlying));
                  //if (family.husbandId > 0)this.getPersonJS((area._1, area._2, area._3), family.husbandId, generation, jsText, sbIdGen)
                  //if (family.wifeId > 0)this.getPersonJS((area._1, area._2, area._3), family.wifeId, generation, jsText, sbIdGen)
        */
      }
    }

  def getLocaleStrings() = {
    val result: StringBuffer = new StringBuffer("\nL={}; ")
    bundleTextList.foreach(str => result.append("L." + str + "='" + S.?(str) + "'; "))
    result.toString
  }


  /**
   * area: _1: the number of generations of ancestors;
   * _2: the number of generations of descendants;
   * _3: true -> show siblings, false - no
   */
  def exportPerson(area: Tuple3[Int, Int, Boolean], id: Long, generation: Int, gedText: StringBuffer): Unit = {
    log.debug("exportPerson []... id=" + id.toString);
    Model.find(classOf[Person], id) match {
      case Some(z) if this.pIsNotYetInJS(z.id) => {
        (-area._1 <= generation, area._2 >= generation) match {
          case (true, true) /*if rootId == id*/ => {
            log.debug("exportPerson case true true " + z.toString(Model.getUnderlying));
            gedText.append(z.toGedcom(Model.getUnderlying, 0, S.locale.getLanguage))
            val fams = z.families(Model.getUnderlying);
              fams match {
              case x :: xs => {
                log.debug("exportPerson families =" + x.toString(Model.getUnderlying));
                fams.foreach(fam => {
                  exportFamily((area._1, area._2, area._3, 0L), fam, generation, gedText)
                })
              }
              case Nil =>
            }
          }
          case _ =>
            log.debug("exportPerson case "+ (-area._1 <= generation).toString+" "+(area._2 >= generation).toString+" "+z.toString(Model.getUnderlying));
        }
      }
      case None => {
        // TODO errmsg='{S.?("no.person.for.this.id")}';
      }
      case _ => {
        log.warn("exportPerson Model.find(classOf[Person], id) match case _");
      }
    }
  }

  /**
   * area: _1: the number of generations of ancestors;
   * _2: the number of generations of descendants;
   * _3: true -> show siblings, false - no
   * _4: the calling Person id or 0L
   */
  def exportFamily(area: Tuple4[Int, Int, Boolean, Long], family: Family, generation: Int, gedText: StringBuffer): Unit =
    if (this.fIsNotYetInJS(family.id)) {
      (generation >= 0) match {
        case true =>
          val iter = family.children.iterator
          while (iter.hasNext) {
            val c: Person = iter.next()
            this.exportPerson((area._1, area._2, area._3), c.id, generation + 1, gedText)
          }
        case false =>
      }
      (-area._1 <= generation, area._2 >= generation) match {
        case (true, true) => {
          log.debug("getFamilyJS case true true " + family.toString(Model.getUnderlying));
          if (family.husbandId > 0)this.exportPerson((area._1, area._2, area._3), family.husbandId, generation, gedText)
          if (family.wifeId > 0)this.exportPerson((area._1, area._2, area._3), family.wifeId, generation, gedText)
          gedText.append(family.toGedcom(Model.getUnderlying, 0, S.locale.getLanguage))
        }
        case _ =>
          log.debug("getFamilyJS case "+ (-area._1 <= generation).toString+" "+(area._2 >= generation).toString+" "+family.toString(Model.getUnderlying));
      }
    }

}

// http://stackoverflow.com/questions/2183503/substitute-values-in-a-string-with-placeholders-in-scala


//            val birtDatePlace: Tuple2[String, String] = this.getPeEvent(z /*Person*/, "BIRT")
//            val deatDatePlace: Tuple2[String, String] = this.getPeEvent(z /*Person*/, "DEAT")
//            sbIdGen.append(z.id + "," + generation + " ");
//            jsText.append("\nvar p={};var r=[];p.r=r;" +
//              <_>p.id={z.id};g['p'+p.id]=p;p.generation={generation};p.nameGivn='{z.nameGivn}';p.nameSurn='{z.nameSurn}';p.gender='{z.gender}';</_>.text +
//              <_>p.bd='{birtDatePlace._1}';p.bp='{birtDatePlace._2}';</_>.text +
//              <_>p.dd='{deatDatePlace._1}';p.dp='{deatDatePlace._2}';</_>.text +
//              familyIdPart + fdIds + sbFams + familyPart);
