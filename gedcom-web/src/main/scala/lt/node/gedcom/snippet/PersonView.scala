package lt.node.gedcom.snippet

import _root_.scala._
import xml._

import _root_.net.liftweb._
import http._
import provider.servlet.HTTPServletContext
import common._
import _root_.net.liftweb.util.Props
import _root_.net.liftweb.util.Helpers._

import _root_.bootstrap.liftweb._
import _root_.lt.node.gedcom.model._
import java.io.File

//{Model, Person, PersonEvent, PersonAttrib, Family, FamilyEvent, EventDetail}
import _root_.lt.node.gedcom.util._  //XslTransformer
//import _root_.lt.node.gedcom.util.{GedcomDateOptions,PeTags,PaTags,GedcomDate}

object personVar extends RequestVar[Box[Person]](Empty)

object familyReqVar extends RequestVar[Map[Int, Family]](Map.empty)

object locTexts4XSLfilePathReqVar extends RequestVar[String](LiftRules.getResource("/xsl/locTexts4XSL.xml") match {
  case Full(url) => url.getProtocol match {
    case "file" => url.getFile //.substring(0*1)
    case _ =>
      val msg = "PersonView.renderPerson: /xsl/locTexts4XSL.xml: the resource protocol is not 'file'"
      Logger("PersonReading").error(msg)
      S.redirectTo("/errorPage", () => {
        ErrorXmlMsg.set(Some(Map("location" -> <p>PersonView.renderPerson</p>, "message" -> <p>{msg}</p>)))
      })
      url.toString
  }
  case _ =>
    val msg = "PersonView.renderPerson: /xsl/locTexts4XSL.xml: the resource is missing"
    Logger("PersonReading").error(msg)
    S.redirectTo("/errorPage", () => {
      ErrorXmlMsg.set(Some(Map("location" -> <p>PersonView.renderPerson</p>, "message" -> <p>{msg}</p>)))
    })
    "loc_texts_4XSL_unresolved"
}
)


object PersonReading extends Loggable {
  val log = Logger("PersonReading")

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

    val resHtmlFa = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXmlFa), "/xsl/person.xsl",
        Map( "userIs"->AccessControl.userIs(),
          "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
          "lang"->S.locale.getLanguage.toLowerCase,
          "personId"->personVar.get.get.id.toString,
          "app"->Props.get("__app").openOr("/gedcom/"))
    ).toString
    log.debug("getFamDataHtml resHtmlFa |" + resHtmlFa + "|")
    resHtmlFa
  }
//Map("app" -> Props.get("__app").openOr("/gedcom/"))
}

//// http://stackoverflow.com/questions/970675/scala-modifying-nested-elements-in-xml?rq=1
//import scala.xml._
//import scala.xml.transform._
//
//object t1 extends RewriteRule {
//  override def transform(n: Node): Seq[Node] = n match {
//    case Elem(prefix, "dateValue", attribs, scope,/* _**/child)  =>
//      Elem(prefix, "dateValue", attribs, scope, Text("2"))
//    case other => other
//  }
//}
//
//object rt1 extends RuleTransformer(t1)
//
//object t2 extends RewriteRule {
//  override def transform(n: Node): Seq[Node] = n match {
//    case sn @ Elem(_, "subnode", _, _, _*) => rt1(sn)
//    case other => other
//  }
//}
//
//object rt2 extends RuleTransformer(t2)
//
//rt2(InputXml)
// =====================================================================================




// http://simply.liftweb.net/index-15.2.html#prev
object AdjustToNumOfFamilies extends Loggable {
  val log = Logger("AdjustToNumOfFamilies")

  def render = {
    log.debug("AdjustToNumOfFamilies []... ")
    PersonReading()
    personVar.is match {
      case Full(p) =>
        log.debug("AdjustToNumOfFamilies: " + p.families(Model.getUnderlying).toString)
        p.families(Model.getUnderlying).size match {
          case 0 =>
            log.debug("-- 0 --   lift:PersonView.render0")
              <lift:embed what="/gedcom/personView0"/>
          case 1 =>
            log.debug("-- 1 --   lift:PersonView.render1")
              <lift:embed what="/gedcom/personView1"/>
          case 2 =>
            log.debug("-- 2 --   lift:PersonView.render2")
              <lift:embed what="/gedcom/personView2"/>
          case 3 =>
            log.debug("-- 3 --   lift:PersonView.render3")
              <lift:embed what="/gedcom/personView3"/>
          case 4 =>
            log.debug("-- 4 --   lift:PersonView.render4")
              <lift:embed what="/gedcom/personView3"/>
          case n =>
            log.debug("-- 4+ --   lift:PersonView.render4Plus")
             <!-- <lift:embed what="/gedcom/personView3Plus"/>-->
              <lift:embed what="/gedcom/personView4Plus"/>
        }
      case _ =>
        log.debug("--_--")
    }
  }
}


class PersonView {
  val log = Logger("PersonView");

  //--v D214-4/vsh an attempt to show Person / Family detail info in ReadOnly
  // if (!AccessControl.isAuthenticated_?()) S.redirectTo("/")

  if (S.get("familyEventId") != Empty) S.unsetSessionAttribute("familyEventId")

  /*val locTexts4XSLfilePath = LiftRules.getResource("/xsl/locTexts4XSL.xml") match {
    case Full(url) => url.getProtocol match {
      case "file" => url.getFile //.substring(1*0)
      case _ =>
        val msg = "PersonView.renderPerson: /xsl/locTexts4XSL.xml: the resource protocol is not 'file'"
        log.error(msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>PersonView.renderPerson</p>,
            "message" -> <p>{msg}</p>)))
        })
        url.toString
    }
    case _ =>
      val msg = "PersonView.renderPerson: /xsl/locTexts4XSL.xml: the resource is missing"
      log.error(msg)
      S.redirectTo("/errorPage", () => {
        ErrorXmlMsg.set(Some(Map(
          "location" -> <p>PersonView.renderPerson</p>,
          "message" -> <p>{msg}</p>)))
      })
      "loc_texts_4XSL_unresolved"
  }*/


  // google-group: Lift: [CSS Selector bindings and attributes]
  def render0: net.liftweb.util.CssSel = {
    // TODO D922-7/vsh ar reikalinga kita eilutė ?
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
    // TODO D922-7/vsh ar reikalinga kita eilutė ?
    PersonReading()
    personVar.is match {
      case Full(p) =>
        p.families(Model.getUnderlying).size match {
          case 1 =>
            log.debug("PersonView render1 -- 1 --")
            this.renderPerson()  &
            "#familyinfo1" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(1))) &
            "#fawiz1" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner1.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            })
          case n =>
            log.error("PersonView render1 --n-- person.id=" + p.id.toString)
            "#filler" #> "filler"
        }
      case _ =>
        log.error("PersonView render1 --_-- person.id=")
        "#filler" #> "filler"
    }
  }


  def render2: net.liftweb.util.CssSel = {
    logSomeSessAttrs("render2")
    // TODO D922-7/vsh ar reikalinga kita eilutė ?
    PersonReading()
    personVar.is match {
      case Full(p) =>
        p.families(Model.getUnderlying).size match {
          case 2 =>
            log.debug("PersonView render2 -- 2 --")
            this.renderPerson()  &
            "#familyinfo1" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(1))) &
            "#fawiz1" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner1.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            }) &
            "#familyinfo2" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(2))) &
            "#fawiz2" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner2.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            })
          case n =>
            log.error("PersonView render2 --n-- person.id=" + p.id.toString)
            "#filler" #> "filler"
        }
      case _ =>
        log.error("PersonView render2 --_-- person.id=")
        "#filler" #> "filler"
    }
  }


  def render3: net.liftweb.util.CssSel = {
    logSomeSessAttrs("render3")
    // TODO D922-7/vsh ar reikalinga kita eilutė ?
    PersonReading()
    personVar.is match {
      case Full(p) =>
        p.families(Model.getUnderlying).size match {
          case 3 =>
            log.debug("PersonView render3 -- 3 --")
            this.renderPerson()  &
            "#familyinfo1" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(1))) &
            "#fawiz1" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner1.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            }) &
            "#familyinfo2" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(2))) &
            "#fawiz2" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner2.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            }) &
            "#familyinfo3" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(3))) &
            "#fawiz3" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner3.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            })
          case n =>
            log.error("PersonView render3 --n-- person.id=" + p.id.toString)
            "#filler" #> "filler"
        }
      case _ =>
        log.error("PersonView render3 --_-- person.id=")
        "#filler" #> "filler"
    }
  }


  def render4: net.liftweb.util.CssSel = {
    logSomeSessAttrs("render4")
    // TODO D922-7/vsh ar reikalinga kita eilutė ?
    PersonReading()
    personVar.is match {
      case Full(p) =>
        p.families(Model.getUnderlying).size match {
          case 4 =>
            log.debug("PersonView render4 -- 4 --")
            this.renderPerson()  &
            "#familyinfo1" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(1))) &
            "#fawiz1" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner1.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            }) &
            "#familyinfo2" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(2))) &
            "#fawiz2" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner2.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            }) &
            "#familyinfo3" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(3))) &
            "#fawiz3" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner3.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            }) &
            "#familyinfo4" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", PersonReading.getFamDataHtml(4))) &
            "#fawiz4" #> (AccessControl.userIs() match {
              case "guest" => <span></span>
              case _ => <span>
                <button class="lift:AddFaWizardRunner4.render">
                  <lift:loc>wiz.add.fe</lift:loc>
                  <img src="/images/page_new.gif" />
                </button>
                <br/>
              </span>
            })
          case n =>
            log.error("PersonView render4 --n-- person.id=" + p.id.toString)
            "#filler" #> "filler"
        }
      case _ =>
        log.error("PersonView render4 --_-- person.id=")
        "#filler" #> "filler"
    }
  }


  def getBaseApplicationPath: Box[String] = {
    LiftRules.context match {
      case context: HTTPServletContext => {
        var baseApp: String = context.ctx.getRealPath("/")
        if (!baseApp.endsWith(File.separator))
          baseApp = baseApp + File.separator
        Full(baseApp)
      }
      case _ => Empty
    }
  }

  def renderPerson(): net.liftweb.util.CssSel = {
    log.debug("renderPerson []...")
    log.debug("/ path =|" + getBaseApplicationPath.openOr("___no_path_for_/_") + "|")
    println("/ path =|" + getBaseApplicationPath.openOr("___no_path_for_/_") + "|")
    log.debug("/ path =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").openOr("___no_path_for_/_") + "|")
    println("/ path =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").openOr("___no_path_for_/_") + "|")
    log.debug("LiftRules.getResource.getContent =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").open_!.getContent + "|")
    log.debug("LiftRules.getResource.getProtocol =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").open_!.getProtocol + "|")
    log.debug("LiftRules.getResource.getFile =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").open_!.getFile + "|")
    log.debug("LiftRules.getResource.getPath =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").open_!.getPath + "|")
    println("LiftRules.getResource.getContent =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").open_!.getContent + "|")
    println("LiftRules.getResource.getProtocol =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").open_!.getProtocol + "|")
    println("LiftRules.getResource.getFile =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").open_!.getFile + "|")
    println("LiftRules.getResource.getPath =|" + LiftRules.getResource("/xsl/locTexts4XSL.xml").open_!.getPath + "|")

    // TODO D922-7/vsh ar reikalinga kita eilutė ?
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
        val resHtml = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
          Map( "userIs"->AccessControl.userIs(),
            "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
            "lang"->S.locale.getLanguage.toLowerCase,
            "mode"->"noFams",
            "app"->Props.get("__app").openOr("/gedcom/"))).toString()
        log.debug("renderPerson resHtml |" + resHtml + "|")

        // <button class="lift:AddPeWizardRunner">
        cssSel =
          "#fullinfo" #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml))) &
          "#pewiz" #> (AccessControl.userIs() match {
            case "guest" => <span><br/></span>
            case _ => <span>
              <button class="lift:AddPeWizardRunner.render">
                <lift:loc>wiz.add.pepa</lift:loc>
                <img src="/images/page_new.gif" />
              </button>
              <!--<button class="lift:PeWizard"><lift:loc>wiz.add.pepa</lift:loc></button>-->
              <br/><br/>
            </span>
          })
//    <button class="lift:AddPeWizardRunner.render"><lift:loc>wiz.add.pepa</lift:loc></button>
      /* <img src="/images/image_new.gif"/>*/
//          "#pewiz" #> <span>
//              <button class="lift:PeWizard.ajaxRender">
//                <lift:loc>wiz.add.pepa</lift:loc>
//              </button>
//            </span>
      case _ =>
        val msg = "PersonView.renderPerson: wrong precondition: the Person is missing"
        log.error(msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map("location" -> <p>PersonView.renderPerson</p>, "message" -> <p>{msg}</p>)))
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
            XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl"/*"/xsl/family.xsl"*/,
              Map( "userIs"->AccessControl.userIs(),
                "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
                "lang"->S.locale.getLanguage.toLowerCase,
                "personId"->p.id.toString(),
                "app"->Props.get("__app").openOr("/gedcom/"))).toString()
          log.debug("resHtml |" + resHtml + "|")
          "#childreninfo" #> Unparsed(Localizer.tagMsg("Fe", "fe", "_", resHtml))
        case _ =>
          val msg = "PersonView.renderSpouseAndChildren: wrong precondition: the Person is missing"
          S.redirectTo("/errorPage", () => {
            ErrorXmlMsg.set(Some(Map("location" -> <p>PersonView.renderSpouseAndChildren</p>, "message" -> <p>{msg}</p>)))
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
                      XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
                        Map( "userIs"->AccessControl.userIs(),
                          "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
                          "lang"->S.locale.getLanguage.toLowerCase,
                          "mode"->"mini",
                          "app"->Props.get("__app").openOr("/gedcom/"))
                      ).toString
                    log.debug("renderParent resHtml |" + resHtml + "|")
                    selector #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml)))
                  case _ =>
                    val msg = "PersonView.renderParent: No person for " + p.id
                    S.redirectTo("/errorPage", () => {
                      ErrorXmlMsg.set(Some(Map("location" -> <p>PersonView.renderParent</p>, "message" -> <p> {msg}
                      </p>)))
                    })
                }
            }
          case null => /* no family */
            log.debug("renderParent: null  no family")
            selector #> <span>&nbsp;--&nbsp;</span>
          /*case _ => /* no family */
            log.debug("renderParent: _  no family")
            selector #> <span>&nbsp;--&nbsp;</span>*/
        }
      case _ =>
        val msg = "PersonView.renderParent: wrong precondition: the Person is missing"
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map("location" -> <p>PersonView.renderParent</p>, "message" -> <p>{msg}</p>)))
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
            ErrorXmlMsg.set(Some(Map("location" -> <p>PersonView.goUpdate</p>, "message" -> <p>{msg}</p>)))
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
    val resHtml = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
      Map( "userIs"->AccessControl.userIs(),
        "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
        "lang"->S.locale.getLanguage.toLowerCase,
        "mode"->"noFams",
        "app"->Props.get("__app").openOr("/gedcom/"))).toString
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
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
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
    val resHtml = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
      Map( "userIs"->AccessControl.userIs(),
        "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
        "mode"->"parentsChildren",
        "lang"->S.locale.getLanguage.toLowerCase,
        "personId"->S.getSessionAttribute("personId").get,
        "childId"->S.getSessionAttribute("childId").get,
        "app"->Props.get("__app").openOr("/gedcom/"))).toString
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
              ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
            })
        }
      } else {
        val place = "PersonView.familyChildDelete.doFamilyChildDelete"
        val msg = ("You are not logged in")
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
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
    val resHtml = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
      Map( "userIs"->AccessControl.userIs(),
        "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
        "mode"->"spouses",
        "lang"->S.locale.getLanguage.toLowerCase,
        "personId"->S.getSessionAttribute("personId").get,
        "familyId"->S.getSessionAttribute("familyId").get,
        "app"->Props.get("__app").openOr("/gedcom/"))).toString
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
      val resHtml = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
        Map(  "userIs"->AccessControl.userIs(),
          "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
          //"locTexts4XSL"->Props.get("loc.texts.4XSL").openOr("loc_texts_4XSL_unresolved"),
          "lang"->S.locale.getLanguage.toLowerCase,
          "peId"->S.getSessionAttribute("personEventId").get,
          "app"->Props.get("__app").openOr("/gedcom/"))).toString
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
            ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
          })
      }
      def doDelete(): Unit = {
        log.debug("[doDelete]...")
        if (AccessControl.isAuthenticated_?) {
          pe.getEventDetail(Model.getUnderlying)
// TODO B414-4/vsh patikrinti kiek ED yra!
          log.debug("[doDelete] pe.eventdetails.size ="+ pe.eventdetails.size)
          val ed: EventDetail = pe.eventdetails.iterator.next()
          log.debug("[doDelete] ed: EventDetail ="+ ed.toString)
          log.debug("[doDelete] ed: EventDetail ="+ ed.toXml.toString)
          var pea = new Audit
          val peClone: Box[PersonEventClone] = Empty
          pea.setFields(CurrentUser.get.get, "PE", pe.id, "del", pe.getAuditRec(peClone))
          log.debug("[doDelete] pea ="+ pea.toString)
          //pea = Model.merge(pea)
          var eda = new Audit
          val edClone: Box[EventDetailClone] = Empty
          eda.setFields(CurrentUser.get.get, "ED", ed.id, "del", ed.getAuditRec(edClone))
          log.debug("[doDelete] eda ="+ eda.toString)
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
            ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
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
      val resHtml = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
        Map(  "userIs"->AccessControl.userIs(),
          "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
          "lang"->S.locale.getLanguage.toLowerCase,
          "paId"->S.getSessionAttribute("personAttribId").get,
          "app"->Props.get("__app").openOr("/gedcom/"))).toString
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
            ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
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
            ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
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
    val resHtml = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
      Map( "userIs"->AccessControl.userIs(),
        "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
        "lang"->S.locale.getLanguage.toLowerCase,
        "feId"->S.getSessionAttribute("familyEventId").get,
        "app"->Props.get("__app").openOr("/gedcom/"))).toString
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
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
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
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
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


  def deleteMultiMedia: net.liftweb.util.CssSel = {
    RequestedURL(Full(S.referer.openOr("/")))
    println("deleteMultiMedia: S.referer= " + S.referer)
    log.debug("deleteMultiMedia: S.referer= " + S.referer)

    PersonReading()
    val person: Person = personVar.get.get
    person.getPersonEvents(Model.getUnderlying)
    val resXml = person.toXmlGeneral(Model.getUnderlying, true).toString
    log.debug("deleteMultiMedia resXml |" + resXml + "|")
    val resHtml = XslTransformer(GedcomUtil.i18nizeXmlDateValues(resXml), "/xsl/person.xsl",
      Map( "userIs"->AccessControl.userIs(),
        "locTexts4XSL"->locTexts4XSLfilePathReqVar.is,
        "lang"->S.locale.getLanguage.toLowerCase,
        //"paId"->S.getSessionAttribute("personAttribId").get,
        "mmId"->S.getSessionAttribute("mmId").get,
        "app"->Props.get("__app").openOr("/gedcom/"))).toString
    log.debug("deleteMultiMedia resHtml |" + resHtml + "|")
    var mm: MultiMedia = null
    val optionMm: Option[MultiMedia] =
      Model.find(classOf[MultiMedia], S.getSessionAttribute("mmId").get.toLong)
    // --^ person.xsl <xsl:template match="mm" mode="full"> assures mmId refres to active record
    optionMm match {
      case Some(mmr) =>
        mm = mmr
      case _ =>
        val place = "PersonView.deleteMultiMedia"
        val msg = ("No MultiMedia for id="+ S.getSessionAttribute("mmId").get)
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () =>
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
        )
    }

    def doDelete(): Unit = {
      log.debug("[doDelete-mm]...")
      if (AccessControl.isAuthenticated_?) {
        mm.idRoot = mm.id
        mm.setModifier(CurrentUser.get.get)
        Model.merge(mm)
        Model.flush
        S.unsetSessionAttribute("mmId")
        log.debug("...[doDelete-mm]")
        S.redirectTo(RequestedURL.is.openOr("/"))
      } else {
        val place = "PersonView.deleteMultiMedia.doDelete"
        val msg = ("You are not logged in")
        log.debug(place+": "+msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map("location" -> <p>{place}</p>, "message" -> <p>{msg}</p>)))
        })
      }
    }
    "#partinfo" #> Unparsed(Localizer.tagMsg("Pe", "pe", "_", Localizer.tagMsg("Pa", "pa", "_", resHtml))) &
      "#confirm" #> LongMsgs.getMsgText("confirm.del.pepafe") &
      "#submit" #> SHtml.submit(S ? "answer.yes"/*"submit"*/, doDelete) &
      "#cancel" #> SHtml.link("index", () => {
        S.unsetSessionAttribute("mmId")
        S.redirectTo(RequestedURL.is.openOr("/")) }, Text(S ? "answer.no"))
  }


}

//-- http://www.assembla.com/wiki/show/liftweb/Binding_via_CSS_Selectors}

