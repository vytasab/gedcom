package lt.node.gedcom.snippet

import _root_.scala._
import xml._

import _root_.net.liftweb._
import http._
import SHtml._
import js._
import JsCmds._
import js.jquery._
import JqJsCmds._
import common._
import _root_.net.liftweb.util.Props
import _root_.net.liftweb.util.Helpers._

import _root_.bootstrap.liftweb._
import _root_.lt.node.gedcom.model._ //{Model, Person, PersonEvent, PersonAttrib, Family, FamilyEvent, EventDetail}
import _root_.lt.node.gedcom.util._  //XslTransformer
//import _root_.lt.node.gedcom.util.{GedcomDateOptions,PeTags,PaTags,GedcomDate}

object personVar extends RequestVar[Box[Person]](Empty)

object familyReqVar extends RequestVar[Map[Int, Family]](Map.empty)


object PersonReading extends Loggable {
  val log = Logger("PersonReading");

  def apply(): Box[Person] = {
    personVar.is match {
      case Full(p) =>
        Full(p)
      case _ =>
        val person: Option[Person] = Model.find(classOf[Person], S.getSessionAttribute("personId").openOr("1").toLong)
        person match {
          case Some(p) =>
            val fams: Map[Int, Family] = p.families(Model.getUnderlying).zipWithIndex.map((kv) =>(kv._2+1, kv._1)).toMap
            log.debug("fams: Map [Int, Family] " + fams.toString)
            familyReqVar.set(fams)
            personVar.set(Full(p))
          case _ =>
            Empty
        }
    }
  }

  def getFamDataHtml(idFamIndex: Int): String = {
    // val resXmlFa: String = familyReqVar.get.get(idFamIndex).get.toXml(Model.getUnderlying).toString
    val resXmlFa: String = AgeAtEvent.localeAgeAtEventInXml(familyReqVar.get.get(idFamIndex).get.toXml(Model.getUnderlying)).toString()
    log.debug("getFamDataHtml resXmlFa |" + resXmlFa + "|")

    //val resXml = personVar.get.get.toXmlGeneral(Model.getUnderlying, true).toString()
    //log.debug("getFamDataHtml gedcomAAE |" + personVar.get.get.toXmlGeneral(Model.getUnderlying, true).toString() + "|")
    //val resXml = AgeAtEvent.localeAgeAtEventInXml(personVar.get.get.toXmlGeneral(Model.getUnderlying, true)).toString()
    //log.debug("getFamDataHtml localeAAE |" + resXml + "|")

    val resHtmlFa = XslTransformer(resXmlFa, "/xsl/person.xsl",
        Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
          "lang"->S.locale.getLanguage.toLowerCase,
          "personId"->personVar.get.get.id.toString,
          "app"->Props.get("__app").openOr("/gedcom-web/"))
    ).toString
    log.debug("getFamDataHtml resHtmlFa |" + resHtmlFa + "|")
    resHtmlFa
  }
//Map("app" -> Props.get("__app").openOr("/gedcom-web/"))
}


// http://simply.liftweb.net/index-15.2.html#prev
object AdjustToNumOfFamilies extends Loggable {
  val log = Logger("AdjustToNumOfFamilies");

  def render = {
    log.debug("AdjustToNumOfFamilies []... ")
    PersonReading()
    personVar.is match {
      case Full(p) =>
        log.debug("AdjustToNumOfFamilies: " + p.families(Model.getUnderlying).toString)
        p.families(Model.getUnderlying).size match {
          case 0 =>
            log.debug("--0--   lift:PersonView.render0")
              <lift:embed what="/gedcom/personView0"/>
          case 1 =>
            log.debug("--1--   lift:PersonView.render1")
              <lift:embed what="/gedcom/personView1"/>
          case n =>
            log.debug("--n--   lift:PersonView.render1Plus")
              <lift:embed what="/gedcom/personView1Plus"/>
          //case 2 => "/gedcom/personView2"
          //case 3 => "/gedcom/personView3"
          //case 4 => "/gedcom/personView4"
          //case n => "/gedcom/personView4Plus"
        }
      case _ =>
        log.debug("--_--")
    }
  }
}


class PersonView {
  val log = Logger("PersonView");

  if (!AccessControl.isAuthenticated_?()) S.redirectTo("/")

  if (S.get("familyEventId") != Empty) S.unsetSessionAttribute("familyEventId")

  // google-group: Lift: [CSS Selector bindings and attributes]
  def render0: net.liftweb.util.CssSel = {
    PersonReading()
    personVar.is match {
      case Full(p) =>
        p.families(Model.getUnderlying).size match {
          case 0 =>
            this.renderPerson()
          case n =>
            log.error("PersonView render0 --n-- person.id=" + p.id.toString)
            "#filler" #> "filler"
        }
      case _ =>
        log.error("PersonView render0 --_-- person.id=")
        "#filler" #> "filler"
    }

  }


  def render1: net.liftweb.util.CssSel = {
    logSomeSessAttrs("render1")
    PersonReading()
    personVar.is match {
      case Full(p) =>
        p.families(Model.getUnderlying).size match {
          case 1 =>
            log.debug("PersonView render1 --1--")
            this.renderPerson()  &
            "#familyinfo1" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(1))) &
            "#fawiz1" #> <span>
              <button class="lift:AddFaWizardRunner1.render">
                <lift:loc>wiz.add.fe</lift:loc>
                <img src="/images/page_new.gif" />
              </button>
              <br/>
            </span>
          case n =>
            log.error("PersonView render1 --n-- person.id=" + p.id.toString)
            "#filler" #> "filler"
        }
      case _ =>
        log.error("PersonView render1 --_-- person.id=")
        "#filler" #> "filler"
    }
  }


  def renderPerson(): net.liftweb.util.CssSel = {
    log.debug("renderPerson []...")

    PersonReading
    log.debug("renderPerson ...[]")

    var cssSel: net.liftweb.util.CssSel = "#filler" #> "filler" // net.liftweb.util.CssSel.Empty //    NodeSeq.Empty  //<_></_>
    log.debug("renderPerson init cssSel |" + cssSel.toString + "| ")
    personVar.is match {
      case Full(p) =>
        //val resXml = p.toXml(Model.getUnderlying).toString
        val resXml = AgeAtEvent.localeAgeAtEventInXml(p.toXml(Model.getUnderlying)).toString()
        //log.debug("renderPerson Props.get(\"loc.texts.4XSL\") = " + Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved") + "|");
        //log.debug("renderPerson Props.get(\"__app\") = " + Props.get("__app").openOr("/__app/") + "|");
        log.debug("renderPerson resXml |" + resXml + "|")
        val resHtml = XslTransformer(resXml, "/xsl/person.xsl",
          Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
            "lang"->S.locale.getLanguage.toLowerCase,
            "mode"->"noFams",
            "app"->Props.get("__app").openOr("/gedcom-web/"))).toString()
        log.debug("renderPerson resHtml |" + resHtml + "|")

        // <button class="lift:AddPeWizardRunner">
        cssSel =
          "#fullinfo" #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml))) &
          "#pewiz" #> <span>
              <button class="lift:AddPeWizardRunner.render">
                <lift:loc>wiz.add.pepa</lift:loc>
              </button>
              <br/>
            </span>
//          "#pewiz" #> <span>
//              <button class="lift:PeWizard.ajaxRender">
//                <lift:loc>wiz.add.pepa</lift:loc>
//              </button>
//            </span>
      case _ =>
        val msg = "PersonView.renderPerson: wrong precondition: the Person is missing"
        log.error(msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>
              PersonView.renderPerson
            </p>,
            "message" -> <p>
              {msg}
            </p>)))
        }
        )
    }
    //log.debug("renderPerson cssSel |" + cssSel.toString + "|")
    cssSel
  }


    def renderSpouseAndChildren: net.liftweb.util.CssSel = {
      personVar.is match {
        case Full(p) =>
          val resXml = p.toXmlFamilies(Model.getUnderlying).toString
          log.debug("renderSpouseAndChildren resXml |" + resXml + "|")
          var resHtml =
            XslTransformer(resXml, "/xsl/person.xsl"/*"/xsl/family.xsl"*/,
              Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
                "lang"->S.locale.getLanguage.toLowerCase,
                "personId"->p.id.toString(),
                "app"->Props.get("__app").openOr("/gedcom-web/"))).toString()
          //XslTransformer(resXml, Localizer.xsl4SpouseAndChildren, Map("personId"->p.id.toString)).toString
          log.debug("resHtml |" + resHtml + "|")
          "#childreninfo" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", resHtml))

        //        "#childreninfo" #> Unparsed(XslTransformer(p.toXmlFamilies(Model.getUnderlying).toString,
        //          xsl4SpouseAndChildren, Map("personId"->p.id.toString)))

        case _ =>
          val msg = "PersonView.renderSpouseAndChildren: wrong precondition: the Person is missing"
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map(
              "location" -> <p>
                PersonView.renderSpouseAndChildren
              </p>,
              "message" -> <p>
                {msg}
              </p>)))
          })
      }
    }



  def renderFather: net.liftweb.util.CssSel = {
    renderParent("H")
  }

  def renderMother: net.liftweb.util.CssSel = {
    renderParent("W")
  }

  def renderParent(hw: String): net.liftweb.util.CssSel = {
    log.debug("renderParent []...")
    PersonReading()
    log.debug("renderParent ...")
    var hwId = 0L
    var hwStyle = ""
    var selector = ""
    hw match {
      case "H" =>
        hwStyle = "M-style"
        selector = "#fatherinfo"
      case "W" =>
        hwStyle = "F-style"
        selector = "#motherinfo"
      case _ => ""
    }
    personVar.is match {
      case Full(p) =>
        p.family match {
          case f: Family =>
            hw match {
              case "H" => hwId = f.husbandId
              case "W" => hwId = f.wifeId
              case _ => hwId = 0L
            }
            hwId match {
              case 0L =>
                selector #> <span class={hwStyle}>
                  &nbsp;
                </span>
              case id: Long =>
                Model.find(classOf[Person], id) match {
                  case Some(p) =>
                    val resXml = p.toXml(Model.getUnderlying).toString
                    log.debug("renderParent resXml |" + resXml + "|")
                    val resHtml =
                      XslTransformer(resXml, "/xsl/person.xsl",
                        Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
                          "lang"->S.locale.getLanguage.toLowerCase,
                          "mode"->"mini",
                          "app"->Props.get("__app").openOr("/gedcom-web/"))
                      ).toString
                    log.debug("renderParent resHtml |" + resHtml + "|")
                    selector #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml)))
                  case _ =>
                    val msg = "PersonView.renderParent: No person for " + p.id
                    S.redirectTo("/errorPage", () => {
                      ErrorXmlMsg.set(Some(Map(
                        "location" -> <p>
                          PersonView.renderParent
                        </p>,
                        "message" -> <p>
                          {msg}
                        </p>)))
                    })
                }
            }
          case null => /* no family */
            log.debug("renderParent: null  no family")
            selector #> <span>&nbsp;--&nbsp;</span>
          case _ => /* no family */
            log.debug("renderParent: _  no family")
            selector #> <span>&nbsp;--&nbsp;</span>
        }
      case _ =>
        val msg = "PersonView.renderParent: wrong precondition: the Person is missing"
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>PersonView.renderParent</p>,
            "message" -> <p>{msg}</p>)))
        })
    }
  }

    def goUpdate: net.liftweb.util.CssSel = {
      personVar.is match {
        case Full(p) =>
          log.debug("p.toXmlFamilies  " + p.toXmlFamilies(Model.getUnderlying).toString)
          "#goUpdate" #> SHtml.link("/rest/personUpdate/" + p.id, () => {
          },
              <img src="/images/page_edit.gif"/>,
            "title" -> S.?("edit.person"))
        //        "#goUpdate" #> <a href="/rest/personUpdate/{p.id}">
        //            <img src="/images/page_edit.gif"/></a>
        case _ =>
          val msg = "PersonView.goUpdate: wrong precondition: the Person is already read"
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map(
              "location" -> <p>PersonView.goUpdate</p>,
              "message" -> <p>{msg}</p>)))
          })
      }
    }

  def logSomeSessAttrs(title: String) {
    log.debug(<_>logSomeSessAttrs-{title}: personEventId={S.getSessionAttribute("personEventId").isDefined} personAttribId={S.getSessionAttribute("personAttribId").isDefined}</_>.text)
    log.debug(<_>logSomeSessAttrs-{title}: personEventId=|{S.getSessionAttribute("personEventId").openOr("")}| personAttribId=|{S.getSessionAttribute("personAttribId").openOr("")}|</_>.text)
  }


  def deletePerson: net.liftweb.util.CssSel = {
    RequestedURL(Full(S.referer.openOr("/")))
    println("deletePerson: S.referer= " + S.referer)
    log.debug("deletePerson: S.referer= " + S.referer)

    PersonReading()
    val person: Person = personVar.get.get
    //person.getPersonEvents(Model.getUnderlying)
    val resXml = person.toXmlGeneral(Model.getUnderlying, true).toString
    log.debug("deletePerson resXml |" + resXml + "|")
    val resHtml = XslTransformer(resXml, "/xsl/person.xsl",
      Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
        "lang"->S.locale.getLanguage.toLowerCase,
        "mode"->"noFams",
        "app"->Props.get("__app").openOr("/gedcom-web/"))).toString
    log.debug("deletePerson resHtml |" + resHtml + "|")

    def doDeletePerson() = {
      log.debug("[doDeletePerson]...")
      if (AccessControl.isAuthenticated_?) {
        //pe.getEventDetail(Model.getUnderlying)
        //val ed: EventDetail = pe.eventdetails.iterator.next()
        var pea = new Audit
        val personClone: Box[PersonClone] = Empty
        pea.setFields(CurrentUser.get.get, "Pe", person.id, "del", person.getAuditRec(personClone))
        Model.remove(Model.getReference(classOf[Person],
          S.getSessionAttribute("personId").get.toLong))
        pea = Model.merge(pea)
        Model.flush
        S.unsetSessionAttribute("personId")
        log.debug("...[doDeletePerson]")
        S.redirectTo(RequestedURL.is.openOr("/"))
      } else {
        val place = "PersonView.deletePerson.doDeletePerson"
        val msg = ("You are not logged in")
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>{place}</p>,
            "message" -> <p>{msg}</p>)))
        })
      }
    }
  //"#partinfo" #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml))) &
    "#partinfo" #> Unparsed(resHtml) &
    "#confirm" #> LongMsgs.getMsgText("confirm.del.pepafe") &
    "#submit" #> SHtml.submit(S ? "answer.yes"/*"submit"*/, doDeletePerson) &
    "#cancel" #> SHtml.link("index", () => {
      S.redirectTo(RequestedURL.is.openOr("/")) }, Text(S ? "answer.no" /*"return"*/))
  }


  def familyChildDelete: net.liftweb.util.CssSel = {
    RequestedURL(Full(S.referer.openOr("/")))
    println("familyChildDelete: S.referer= " + S.referer)
    log.debug("familyChildDelete: S.referer= " + S.referer)

    PersonReading()
    val person: Person = personVar.get.get
    //person.getPersonEvents(Model.getUnderlying)
    val resXml = person.toXmlGeneral(Model.getUnderlying, true).toString
    log.debug("familyChildDelete resXml |" + resXml + "|")
    val resHtml = XslTransformer(resXml, "/xsl/person.xsl",
      Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
        "mode"->"parentsChildren",
        "lang"->S.locale.getLanguage.toLowerCase,
        "personId"->S.getSessionAttribute("personId").get,
        "childId"->S.getSessionAttribute("childId").get,
        "app"->Props.get("__app").openOr("/gedcom-web/"))).toString
    log.debug("familyChildDelete resHtml |" + resHtml + "|")

    def doFamilyChildDelete(): Unit = {
      log.debug("[doFamilyChildDelete]...")
      if (AccessControl.isAuthenticated_?) {
        val childOption: Option[Person] = Model.find(classOf[Person], S.getSessionAttribute("childId").openOr("0").toLong)
        childOption match {
          case Some(p) =>
            var pea = new Audit
            val personClone: Box[PersonClone] = Empty
            pea.setFields(CurrentUser.get.get, "Pe", p.id, "fcDel", p.getAuditRec(personClone))
            p.family = null
            /*p = */Model.merge(p)
            pea = Model.merge(pea)
            Model.flush
            S.unsetSessionAttribute("personId")
            S.unsetSessionAttribute("childId")
            log.debug("...[doFamilyChildDelete]")
            S.redirectTo(RequestedURL.is.openOr("/"))
          case _ =>
            val place = "PersonView.familyChildDelete"
            val msg = <_>The person (id={S.getSessionAttribute("childId")}) is not found !</_>.text
            log.debug(place+": "+msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>{place}</p>,
                "message" -> <p>{msg}</p>)))
            })
        }
      } else {
        val place = "PersonView.familyChildDelete.doFamilyChildDelete"
        val msg = ("You are not logged in")
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>{place}</p>,
            "message" -> <p>{msg}</p>)))
        })
      }
    }
  //"#partinfo" #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml))) &
    "#fullinfo" #> Unparsed(resHtml) &
    "#confirm" #> LongMsgs.getMsgText("confirm.remove.familychild")/* + " - "*/ &
    "#submit" #> SHtml.submit(S ? "answer.yes"/*"submit"*/, doFamilyChildDelete) &
    "#cancel" #> SHtml.link("index", () => {
      //S.unsetSessionAttribute("personId")
      //S.unsetSessionAttribute("childId")
      S.redirectTo(RequestedURL.is.openOr("/")) }, Text(S ? "answer.no" /*"return"*/))
  }


  def familyDelete: net.liftweb.util.CssSel = {
    RequestedURL(Full(S.referer.openOr("/")))
    println("familyDelete: S.referer= " + S.referer)
    log.debug("familyDelete: S.referer= " + S.referer)

    PersonReading()
    val person: Person = personVar.get.get
    val resXml = person.toXmlGeneral(Model.getUnderlying, true).toString
    log.debug("familyDelete resXml |" + resXml + "|")
    val resHtml = XslTransformer(resXml, "/xsl/person.xsl",
      Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
        "mode"->"spouses",
        "lang"->S.locale.getLanguage.toLowerCase,
        "personId"->S.getSessionAttribute("personId").get,
        "familyId"->S.getSessionAttribute("familyId").get,
        "app"->Props.get("__app").openOr("/gedcom-web/"))).toString
    log.debug("familyDelete resHtml |" + resHtml + "|")

    def doFamilyDelete(): Unit = {
      log.debug("[doFamilyDelete]...")
      if (AccessControl.isAuthenticated_?) {
        val familyOption: Option[Family] = Model.find(classOf[Family], S.getSessionAttribute("familyId").openOr("0").toLong)
        familyOption match {
          case Some(f) =>
            var pea = new Audit
            val familyClone: Box[FamilyClone] = Empty
            pea.setFields(CurrentUser.get.get, "Fa", f.id, "del", f.getAuditRec(familyClone))
            Model.remove(Model.getReference(classOf[Family],
              S.getSessionAttribute("familyId").get.toLong))
            pea = Model.merge(pea)
            Model.flush
            S.unsetSessionAttribute("personId")
            S.unsetSessionAttribute("familyId")
            log.debug("...[doFamilyDelete]")
            S.redirectTo(RequestedURL.is.openOr("/"))
          case _ =>
            val place = "PersonView.familyDelete"
            val msg = <_>The family (id={S.getSessionAttribute("familyId")}) is not found !</_>.text
            log.debug(place+": "+msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
            })
        }
      } else {
        val place = "PersonView.familyDelete.doFamilyDelete"
        val msg = ("You are not logged in")
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map( "location" -> <p>{place}</p>,"message" -> <p>{msg}</p>)))
        })
      }
    }
    "#fullinfo" #> Unparsed(resHtml) &
    "#confirm" #> LongMsgs.getMsgText("confirm.del.family")/* + " - "*/ &
    "#submit" #> SHtml.submit(S ? "answer.yes", doFamilyDelete) &
    "#cancel" #> SHtml.link("index", () => {
      S.redirectTo(RequestedURL.is.openOr("/")) }, Text(S ? "answer.no"))
  }


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


    def deletePe: net.liftweb.util.CssSel = {
      RequestedURL(Full(S.referer.openOr("/")))
      println("deletePe: S.referer= " + S.referer)
      log.debug("deletePe: S.referer= " + S.referer)

      PersonReading()
      val person: Person = personVar.get.get
      person.getPersonEvents(Model.getUnderlying)
      val resXml = person.toXmlGeneral(Model.getUnderlying, true).toString
      log.debug("deletePe resXml |" + resXml + "|")
      val resHtml = XslTransformer(resXml, "/xsl/person.xsl",
        Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
          "lang"->S.locale.getLanguage.toLowerCase,
          "peId"->S.getSessionAttribute("personEventId").get,
          "app"->Props.get("__app").openOr("/gedcom-web/"))).toString
      log.debug("deletePe resHtml |" + resHtml + "|")

      var pe: PersonEvent = null
      val optionPe: Option[PersonEvent] =
        Model.find(classOf[PersonEvent], S.getSessionAttribute("personEventId").get.toLong) //.asInstanceOf[PersonEvent]
      optionPe match {
        case Some(per) =>
          pe = per
        case _ =>
          val place = "PersonView.deletePe"
          val msg = ("No PersonEvent for id="+ S.getSessionAttribute("personEventId").get)
          log.debug(place+": "+msg)
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map(
              "location" -> <p>{place}</p>,
              "message" -> <p>{msg}</p>)))
          })
      }
      def doDelete(): Unit = {
        log.debug("[doDelete]...")
        if (AccessControl.isAuthenticated_?) {
          pe.getEventDetail(Model.getUnderlying)
// TODO B414-4/vsh patikrinti kiek ED yra!
          val ed: EventDetail = pe.eventdetails.iterator.next()
          var pea = new Audit
          val peClone: Box[PersonEventClone] = Empty
          pea.setFields(CurrentUser.get.get, "PE", pe.id, "del", pe.getAuditRec(peClone))
          //pea = Model.merge(pea)
          var eda = new Audit
          val edClone: Box[EventDetailClone] = Empty
          eda.setFields(CurrentUser.get.get, "ED", ed.id, "del", ed.getAuditRec(edClone))
          //eda = Model.merge(eda)
          //Model.remove(pe)
          Model.remove(Model.getReference(classOf[PersonEvent],
            S.getSessionAttribute("personEventId").get.toLong))
          pea = Model.merge(pea)
          eda = Model.merge(eda)
          Model.flush
          S.unsetSessionAttribute("personEventId")
          log.debug("...[doDelete]")
          S.redirectTo(RequestedURL.is.openOr("/"))
        } else {
          val place = "PersonView.deletePe.doDelete"
          val msg = ("You are not logged in")
          log.debug(place+": "+msg)
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map(
              "location" -> <p>{place}</p>,
              "message" -> <p>{msg}</p>)))
          })
        }
      }
      "#partinfo" #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml))) &
      "#confirm" #> LongMsgs.getMsgText("confirm.del.pepafe") &
      "#submit" #> SHtml.submit(S ? "answer.yes"/*"submit"*/, doDelete) &
      "#cancel" #> SHtml.link("index", () => {
        S.unsetSessionAttribute("personEventId")
        S.redirectTo(RequestedURL.is.openOr("/")) }, Text(S ? "answer.no" /*"return"*/))
    }


    def editPa: net.liftweb.util.CssSel = {
      logSomeSessAttrs("editPa")
      PersonReading()
      "#pewiz" #> <span>
        <button class="lift:PeWizard">
          <lift:loc>wiz.upd.pa</lift:loc>
          <img src="/images/page_edit.gif" />
        </button>
        <br/>
      </span>
    }


    def deletePa: net.liftweb.util.CssSel = {
      RequestedURL(Full(S.referer.openOr("/")))
      println("deletePa: S.referer= " + S.referer)
      log.debug("deletePa: S.referer= " + S.referer)

      PersonReading()
      val person: Person = personVar.get.get
      person.getPersonEvents(Model.getUnderlying)
      val resXml = person.toXmlGeneral(Model.getUnderlying, true).toString
      log.debug("deletePa resXml |" + resXml + "|")
      val resHtml = XslTransformer(resXml, "/xsl/person.xsl",
        Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
          "lang"->S.locale.getLanguage.toLowerCase,
          "paId"->S.getSessionAttribute("personAttribId").get,
          "app"->Props.get("__app").openOr("/gedcom-web/"))).toString
      log.debug("deletePa resHtml |" + resHtml + "|")

      var pa: PersonAttrib = null
      val optionPa: Option[PersonAttrib] =
        Model.find(classOf[PersonAttrib], S.getSessionAttribute("personAttribId").get.toLong)
      optionPa match {
        case Some(par) =>
          pa = par
        case _ =>
          val place = "PersonView.deletePe"
          val msg = ("No PersonAttrib for id="+ S.getSessionAttribute("personAttribId").get)
          log.debug(place+": "+msg)
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map(
              "location" -> <p>{place}</p>,
              "message" -> <p>{msg}</p>)))
          })
      }
      def doDelete(): Unit = {
        log.debug("[doDelete]...")
        if (AccessControl.isAuthenticated_?) {
          pa.getAttribDetail(Model.getUnderlying)
// TODO B414-4/vsh patikrinti kiek ED yra!
          val ed: EventDetail = pa.attribdetails.iterator.next()
          var paa = new Audit
          val paClone: Box[PersonAttribClone] = Empty
          paa.setFields(CurrentUser.get.get, "PA", pa.id, "del", pa.getAuditRec(paClone))
          //paa = Model.merge(paa)
          var eda = new Audit
          val edClone: Box[EventDetailClone] = Empty
          eda.setFields(CurrentUser.get.get, "ED", ed.id, "del", ed.getAuditRec(edClone))
          //eda = Model.merge(eda)
          //Model.remove(pa)
          Model.remove(Model.getReference(classOf[PersonAttrib],
            S.getSessionAttribute("personAttribId").get.toLong))
          paa = Model.merge(paa)
          eda = Model.merge(eda)
          Model.flush
          S.unsetSessionAttribute("personAttribId")
          log.debug("...[doDelete]")
          S.redirectTo(RequestedURL.is.openOr("/"))
        } else {
          val place = "PersonView.deletePa.doDelete"
          val msg = ("You are not logged in")
          log.debug(place+": "+msg)
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map(
              "location" -> <p>{place}</p>,
              "message" -> <p>{msg}</p>)))
          })
        }
      }
      "#partinfo" #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml))) &
      "#confirm" #> LongMsgs.getMsgText("confirm.del.pepafe") &
      "#submit" #> SHtml.submit(S ? "answer.yes"/*"submit"*/, doDelete) &
      "#cancel" #> SHtml.link("index", () => {
        S.unsetSessionAttribute("personAttribId")
        S.redirectTo(RequestedURL.is.openOr("/")) }, Text(S ? "answer.no" /*"return"*/))
    }


    def editFe: net.liftweb.util.CssSel = {
      PersonReading()
      "#fawiz" #> <span>
        <button class="lift:FaWizard">
          <lift:loc>wiz.add.fe</lift:loc>
          <img src="/images/page_edit.gif" />
        </button>
        <br/>
      </span>
    }


  def deleteFe: net.liftweb.util.CssSel = {
    RequestedURL(Full(S.referer.openOr("/")))
    println("deleteFe: S.referer= " + S.referer)
    log.debug("deleteFe: S.referer= " + S.referer)

    PersonReading()
    val person: Person = personVar.get.get
    person.getPersonEvents(Model.getUnderlying)
    val resXml = person.toXmlGeneral(Model.getUnderlying, true).toString
    log.debug("deleteFe resXml |" + resXml + "|")
    val resHtml = XslTransformer(resXml, "/xsl/person.xsl",
      Map("locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
        "lang"->S.locale.getLanguage.toLowerCase,
        "feId"->S.getSessionAttribute("familyEventId").get,
        "app"->Props.get("__app").openOr("/gedcom-web/"))).toString
    log.debug("deleteFe resHtml |" + resHtml + "|")

    var fe: FamilyEvent = null
    val optionFe: Option[FamilyEvent] =
      Model.find(classOf[FamilyEvent], S.getSessionAttribute("familyEventId").get.toLong)
    optionFe match {
      case Some(fer) =>
        fe = fer
      case _ =>
        val place = "PersonView.deleteFe"
        val msg = ("No FamilyEvent for id="+ S.getSessionAttribute("familyEventId").get)
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>{place}</p>,
            "message" -> <p>{msg}</p>)))
        })
    }
    def doDelete(): Unit = {
      log.debug("[doDelete]...")
      if (AccessControl.isAuthenticated_?) {
        fe.getEventDetail(Model.getUnderlying)
// TODO B414-4/vsh patikrinti kiek ED yra!
        val ed: EventDetail = fe.familydetails.iterator.next()
        var fea = new Audit
        val feClone: Box[FamilyEventClone] = Empty
        fea.setFields(CurrentUser.get.get, "FE", fe.id, "del", fe.getAuditRec(feClone))
        //fea = Model.merge(fea)
        var eda = new Audit
        val edClone: Box[EventDetailClone] = Empty
        eda.setFields(CurrentUser.get.get, "ED", ed.id, "del", ed.getAuditRec(edClone))
        //eda = Model.merge(eda)
        //Model.remove(fe)
        Model.remove(Model.getReference(classOf[FamilyEvent],
          S.getSessionAttribute("familyEventId").get.toLong))
        fea = Model.merge(fea)
        eda = Model.merge(eda)
        Model.flush
        S.unsetSessionAttribute("familyEventId")
        log.debug("...[doDelete]")
        S.redirectTo(RequestedURL.is.openOr("/"))
      } else {
        val place = "PersonView.deleteFe.doDelete"
        val msg = ("You are not logged in")
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>{place}</p>,
            "message" -> <p>{msg}</p>)))
        })
      }
    }
    "#partinfo" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", resHtml)) &
    "#confirm" #> LongMsgs.getMsgText("confirm.del.pepafe") &
    "#submit" #> SHtml.submit(S ? "answer.yes"/*"submit"*/, doDelete) &
    "#cancel" #> SHtml.link("index", () => {
      S.unsetSessionAttribute("familyEventId")
      S.redirectTo(RequestedURL.is.openOr("/")) }, Text(S ? "answer.no" /*"return"*/))
  }


}

//-- http://www.assembla.com/wiki/show/liftweb/Binding_via_CSS_Selectors}

