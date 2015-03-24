package lt.node.gedcom.snippet

//import _root_.scala.util._
import _root_.scala._

import _root_.net.liftweb._
import http._
import common._
import /*net.liftweb.*/widgets.autocomplete.AutoComplete
import _root_.net.liftweb.util.Helpers._
import http.js.JsCmds.FocusOnLoad
import http.{RequestVar, S, SHtml}
import bootstrap.liftweb._
import lt.node.gedcom.model._
import net.liftweb.common.Full
import scala.Some
import scala.collection.JavaConverters._
import lt.node.gedcom.util.GedcomUtil
import net.liftweb.util.Props
import net.liftweb.common.Full
import scala.Some
import javax.persistence.NoResultException
import scala.xml.NodeSeq
import net.liftweb.common.Full
import scala.Some

class PersonSnips {
  val log = Logger("PersonSnips");

  //-- http://www.assembla.com/wiki/show/liftweb/Binding_via_CSS_Selectors
  //-- http://en.wikipedia.org/wiki/Gender_symbol --  \u2640  ♀ Venus;  \u2642  ♂ Mars;

  //object personVar extends RequestVar[Box[Person]](Empty)
  object personsVar extends SessionVar[Map[Long, String]](Map.empty)
  object personsXmlVar extends SessionVar[Map[Long, NodeSeq]](Map.empty)
  object searchArgVar extends SessionVar[String]("")
  object noParentsVar extends SessionVar[Boolean](false)


  def list /*(xhtml: Group): NodeSeq*/ = {
    val items = Model.createNamedQuery[Person]("findAllPersons").getResultList()
    var personList = items.map(i => (/*"/rest/person/" + */ i.id.toString,
      (if (i.gender == "M") "♀" else "\u2642") + " " + i.nameGivn + " " + i.nameSurn
      )
    ).toList
    personList = ("", "-- " + S.?("select.person") + " --") :: personList

    //log.debug(items.size.toString)
    var selectedPersonURL: String = ""

    def preparePersonData(): Unit = {
      log.debug(<_>selectedPersonURL={selectedPersonURL}</_>.text);
      S.redirectTo(<_>/rest/person/{selectedPersonURL}</_>.text)
    }

    "#domain" #> FocusOnLoad(SHtml.select(personList, Empty,
      { selectedPersonURL = _ }, "size" -> "1", "onchange" -> "selectWhenChanged(this)" )) &
    "#submit" #> SHtml.submit("Save", preparePersonData)
  }



  def sublistInternal(requestedURLpref: String) = {
    log.debug("[sublistInternal]... AccessControl.isAuthenticated_?(): " + AccessControl.isAuthenticated_?())
    log.debug("[sublistInternal]... CurrentUser.is.isDefined: " + CurrentUser.is.isDefined)
    log.debug(("[sublistInternal]... CurrentUser.is.toString: |%s|", CurrentUser.is.toString))
    var sarg = ""
    def buildQuery(current: String, limit: Int): Seq[String] = {
      log.debug("buildQuery: current= " + current + " limit=" + limit)
      val persons = Model.createNamedQuery[Person]("findPersonOrGivnSurn").
        setParams("nameGivn" -> ("%" + current + "%"),
        "nameSurn" -> ("%" + current + "%")).getResultList().toList
      val id2person: Map[Long, String] = persons. /*filter{
        p => S.getSessionAttribute("gender") match {
          case Full(x) => p.gender == x
          case _ => true
        }
      }.*/
        map { p => (p.id,
          <_>{if (p.gender == "M") "♂ " else "♀ "}{p.nameGivn + " "}{p.nameSurn}{peEventDate(p, "BIRT")}{peEventDate(p, "DEAT")}{adminTestPiece(p.id.toString)}</_>.text)
      }.toMap
      personsVar(id2person)
      log.debug(<_>buildQuery: id2person={id2person.toString()};</_>.text)
      //searchArgVar.set(current)
      sarg = current
      id2person.values.toSeq
    }

    def completeQuery(value: String) {
      requestedURLpref match {
        case "rest/richPeListByXn" =>
          searchArgVar.set(sarg)
          log.debug(<_>completeQuery rest/richPeListByXn: value={value};</_>.text)
          RequestedURL(Full(<_>/{requestedURLpref}/{searchArgVar.is}</_>.text))
          S.redirectTo(RequestedURL.openOr("/"))
        case x if x.size > 0 =>
          log.debug(<_>completeQuery: value={value};</_>.text)
          val foundPersonId: Option[(Long, String)] =
            personsVar.is.find {
              (kv) => value == kv._2
            }
          //RequestedURL(Full(<_>/rest/person/{foundPersonId.get._1}</_>.text))
          RequestedURL(Full(<_>/{requestedURLpref}/{foundPersonId.get._1}</_>.text))
          S.redirectTo(RequestedURL.openOr("/"))
        case _ =>
          S.redirectTo(RequestedURL.openOr("/"))
      }
    }

    // E123-4/vsh:  http://stackoverflow.com/questions/1559871/autocomplete-customize-it-jquery
    "#selector" #> (FocusOnLoad(AutoComplete("", buildQuery _, completeQuery _,
      List(("selectFirst", "false"), ("minChars", "2"), ("width", "350"),
        ("max", "0")
      ))))
  }

  def peEventDate(pe: Person, eventName: String): String  = {
    pe.personevents.asScala.filter(x => x.tag == eventName).toList match {
      case Nil => ""
      case list => (if (eventName=="BIRT") " *" else " +") +
        GedcomUtil.i18nizeGedcomDate(list.head.eventdetails.iterator.next().dateValue)
      //list.head.eventdetails.iterator.next().dateValue
    }
  }

  def adminTestPiece(pid: String):  String = {
    System.getProperty("run.mode") match {
      case "development"  =>  <_> [id:{pid}]</_>.text
      case "production" if AccessControl.isDeveloper_?()  =>  <_> [id:{pid}]</_>.text
      case _  =>  ""
    }
  }

  def sublist = {
    sublistInternal("rest/person")
  }

  def richPeListByXn = {   // sublistInternal("rest/richPeListByXn")
    val requestedURLpref = "rest/richPeListByXn"
    val emptySpan = <span></span>
    var hasParents = false

    def doSearch() = {
      def peInfoMain(id: Long): NodeSeq = {
        Model.find(classOf[Person], id).isDefined match {
          case true =>
            val p: Person = Model.find(classOf[Person], id).get
            <span>{if (p.gender == "M" ) <img src="../images/gender_M.gif"/> else <img src="../images/gender_F.gif"/>}
              {p.nameGivn + " "}{p.nameSurn}{peEventDate(p, "BIRT")}{peEventDate(p, "DEAT")}</span>
          case _ => <span></span>
        }
      }

      def peInfoMainPe(p: Person): NodeSeq = {
        //Model.find(classOf[Person], id).isDefined match {
        //  case true =>
        //    val p: Person = Model.find(classOf[Person], id).get
            <span style="font-size:medium; font-style:normal" >{if (p.gender == "M" ) <img src="../images/gender_M.gif"/> else <img src="../images/gender_F.gif"/>}
              {p.nameGivn + " "}{p.nameSurn}{peEventDate(p, "BIRT")}{peEventDate(p, "DEAT")}</span>
        //  case _ => <span></span>
        //}
      }

      def peInfoParents(id: Long): NodeSeq = {
        Model.find(classOf[Person], id).isDefined match {
          case true =>
            val p: Person = Model.find(classOf[Person], id).get
            p.family match {
              case null => emptySpan //<span></span>
              case fa =>  <span style="font-size:medium; font-style:normal">{peInfoMain(fa.husbandId)} {peInfoMain(fa.wifeId)}</span>
            }
          case _ => emptySpan  //<span></span>
        }
      }

      def peInfoFamilies(id: Long): NodeSeq = {
        Model.find(classOf[Person], id).isDefined match {
          case true =>
            val p: Person = Model.find(classOf[Person], id).get
            p.families(Model.getUnderlying) match {
              case fams if fams.size > 0 =>
                //<span> <img src="../images/family.jpg"/> <img src="../images/family_MF.gif"/> {fams.map(fa =>peInfoFamily(p, fa))}</span>
                //<span> <img src="../images/family.jpg" width="40"/> <img src="../images/family_MF.gif"/> {fams.map(fa =>peInfoFamily(p, fa))}</span>
                <span> <img src="../images/family.jpg" width="40"/>  {fams.map(fa =>peInfoFamily(p, fa))}</span>
              case _  =>  emptySpan  //<span></span>
            }
          case _ => emptySpan  //<span></span>
        }
      }

      def peInfoFamily(p: Person, fa: Family): NodeSeq = {
        <span>[{if (p.id == fa.husbandId) peInfoMain(fa.wifeId) else peInfoMain(fa.husbandId)} {fa.children.asScala.toList.map(c=>peInfoMainPe(c))}]</span>
      }

      def buildQueryXml(current: String): Seq[NodeSeq] = {
        log.debug("buildQuery: current= " + current)
        log.debug("buildQuery: hasParents= " + hasParents.toString)
        val persons = Model.createNamedQuery[Person]("findPersonOrGivnSurn").
          setParams("nameGivn" -> ("%" + current + "%"),
            "nameSurn" -> ("%" + current + "%")).getResultList().toList
        val id2person: Map[Long, NodeSeq] = persons.filter((p: Person) => { hasParents  match {
          case true => {
            log.debug("buildQuery: case true "+ (peInfoParents(p.id) != emptySpan))
            peInfoParents(p.id) == emptySpan
          }
          case _ => {
            log.debug("buildQuery: case _ true")
            true
          }
        }})./*persons.*/map { p => (p.id,
          <p style="font-size:large; font-style:normal">{peInfoMain(p.id)}
            {adminTestPiece(p.id.toString)}
            <b>{{</b>{peInfoParents(p.id)}<b>}}</b>
            <b>[</b>{peInfoFamilies(p.id)}<b>]</b>
          </p>)
        }.toMap
        personsXmlVar(id2person)
        log.debug(<_>buildQuery: id2person={id2person.toString()};</_>.text)
        searchArgVar.set(current)
        noParentsVar.set(hasParents)
        id2person.values.toSeq
      }
      try {
        //log.debug("doSearch: CurrentReq.value.request.params |" + CurrentReq.value.request.params.toList.toString() + "|")
        // --> DEBUG LoginOps - doSearch: CurrentReq.value.request.params |
        // List(HTTPParam(emailAddress,List(vytasab@gmail.com)),
        // HTTPParam(password,List(1...3)),
        // HTTPParam(F406902577514FHAZSK,List(Prisijungti)))|
        //val reqScheme = CurrentReq.value.request.scheme
        //log.debug("doSearch: CurrentReq.value.request.scheme |" + reqScheme + "|")
        //log.debug("doSearch: Props.fileName |" + Props.fileName + "|")
        //log.debug("doSearch: Props.propFileName |" + Props.propFileName + "|")
        //log.debug("doSearch: Props.modeName |" + Props.modeName + "|")
        //log.debug("doSearch: Props.mode |" + Props.mode.toString + "|")
        RequestedURL(Empty)

        //val peLi:Seq[String] = buildQuery(S.param("searchArg").openOr(""))
        /*val peLiXml:Seq[NodeSeq] = */
        buildQueryXml(S.param("searchArg").openOr(""))
      } catch {
        case x: NoResultException =>
          log.error("doSearch: nerasta NoResultException")
          S.error(x.getMessage)
        case e: Exception =>
          log.error("doSearch: nerasta Exception")
          S.error(e.getMessage)
      } finally {
        log.debug("doSearch:  finally; S.hostAndPath = " + S.hostAndPath)
        log.debug("doSearch: finally block")
        RequestedURL(Full(<_>/{requestedURLpref}</_>.text))
        S.redirectTo(RequestedURL.openOr("/"))
      }
    }
    "#searchArg" #> (FocusOnLoad(<input type="text" size="24" name="searchArg"/>)) &
      "#hasParents" #> SHtml.checkbox(hasParents, hasParents = _, ("id", "hasParents" )) &
        //--  http://grokbase.com/t/gg/liftweb/131b9gezpj/lift-shtml-checkbox-removes-id-attribute-from-template
      "#submit" #> SHtml.submit(S.?("sf.dosearch"), doSearch)
  }


  def exportAll = {
    sublistInternal("rest/person")
  }

  def exportPart = {
    sublistInternal("export/exportPart")
  }

  def richPersonList = {
    //val develop = S.getSessionAttribute("searchArg").openOr("")+personsVar.get.values.toList.size + ' ' + S.referer.openOr("/")
    val requestedURL = "/gedcom/richPeListByXn"
    //val noParentsView =
    "#linkTop [href]" #>  requestedURL &
    "#searchArg" #> searchArgVar.get &
    "#noParents" #> (if (noParentsVar.get) "{"/*+S.?("sf.noparents")*/+"}" else "")  &
    //"#linkPdf [href]" #> "http://pdfcrowd.com/url_to_pdf/?width=210mm&height=297mm" &
    "#peLines *" #> personsXmlVar.get.values.toList &
    "#linkBot [href]" #> requestedURL  //S.referer.openOr("/")
  }

  def listOrGnSn = {

    def buildQuery(current: String, limit: Int): Seq[String] = {
      log.debug("buildQuery: current= " + current + " limit=" + limit)
      val persons = Model.createNamedQuery[Person]("findPersonOrGivnSurn").
        setParams("nameGivn" -> ("%" + current + "%").toString,
        "nameSurn" -> ("%" + current + "%").toString).getResultList().toList
      // TODO B117-1 filter obviously incompatible to acting person cases - ancestors
      val id2person: Map[Long, String] = persons.filter {
        p => S.getSessionAttribute("gender") match {
          case Full(x) => p.gender == x
          case _ => true
        }
      }.
        map {
        p => (p.id, (<_>{if (p.gender == "M") "♂ " else "♀ "}{p.nameGivn + " "}{p.nameSurn}</_>.text))
      }.toMap
      PersonSnips.itemsVar(id2person)
      id2person.values.toSeq
    }

    def completeQueryWithExisting(value: String): Unit = {
      log.debug(<_>completeQueryWithExisting: value={value};</_>.text)
      val foundPersonId: Option[(Long, String)] =
        PersonSnips.itemsVar.is.find {
          (kv) => value == kv._2
        }
      log.debug(<_>foundPersonId={foundPersonId.getOrElse((0, "error"))};</_>.text)
      completeQuery(foundPersonId.get._1)
    };

    // "#title" #> (S.?("select.person")+":") &
    "#selector" #> AutoComplete("", buildQuery _,
      completeQueryWithExisting _,
      List(("selectFirst", "false"), ("minChars", "1")))
  }


  /*private*/ def completeQuery(foundPersonId: Long): Unit = {
    log.debug(<_>completeQuery: personId={S.getSessionAttribute("personId").toString};</_>.text)
    log.debug(<_>   familyId={S.getSessionAttribute("familyId").toString};</_>.text)
    log.debug(<_>   gender={S.getSessionAttribute("gender").toString};</_>.text)
    log.debug(<_>   role={S.getSessionAttribute("role").toString};</_>.text)

    S.getSessionAttribute("personId").isDefined &&
      S.getSessionAttribute("familyId").isDefined &&
      S.getSessionAttribute("gender").isDefined &&
      S.getSessionAttribute("role").isDefined match {
      case true =>
        bindPerson(S.getSessionAttribute("personId").open_!.toLong,
          foundPersonId,
          S.getSessionAttribute("role").open_!,
          S.getSessionAttribute("familyId").open_!.toLong)
        S.redirectTo("/rest/person/" + S.getSessionAttribute("personId").open_!)
      case _ =>
        val msg = ("completeQuery: Some params are wrong " +
          <_>personId={S.getSessionAttribute("personId").toString}; </_>.text +
          <_>familyId={S.getSessionAttribute("familyId").toString}; </_>.text +
          <_>gender={S.getSessionAttribute("gender").toString}; </_>.text +
          <_>role={S.getSessionAttribute("role").toString};</_>.text)
        log.debug(msg)
        S.redirectTo("/errorPage", () => {
          ErrorXmlMsg.set(Some(Map(
            "location" -> <p>PersonSnips.completeQuery</p>,
            "message" -> <p>{msg}</p>)))
          //S.setSessionAttribute("appErrorLocation", "PersonSnips.completeQuery")
          //S.setSessionAttribute("appError", msg)
        })
    }

  }


  /*private*/ def bindPerson(bindingPersonId: Long, toBeBoundPersonId: Long,
                         role: String, familyId: Long): Unit = {
    log.debug(<_>bindPerson: bindingPersonId={bindingPersonId};</_>.text)
    log.debug(<_>   toBeBoundPersonId={toBeBoundPersonId};</_>.text)
    log.debug(<_>   role={role}; familyId={familyId};</_>.text)
    role match {
      case sd if "-fSonfDaughter".indexOf(sd) > 0 =>
        familyId match {
          case 0L =>
            val msg = "bindPerson: A familyId is 0 when roles fSon fDaughter"
            log.debug(msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>PersonSnips.bindPerson</p>,
                "message" -> <p>{msg}</p>)))
              //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
              //S.setSessionAttribute("appError", msg)
            })
          case existingId =>
            val toBeBoundPerson = Model.find(classOf[Person], toBeBoundPersonId)
            val family = Model.find(classOf[Family], existingId)
            toBeBoundPerson match {
              case Some(x) =>
                family match {
                  case Some(y) =>
                    x.family = y
                    if (AccessControl.isAuthenticated_?) x.setSubmitter(CurrentUser.is.open_!)
                    Model.merge(x)
                  case _ =>
                    val msg = "bindPerson: There is no Family for id = " + existingId
                    log.debug(msg)
                    S.redirectTo("/errorPage", () => {
                      ErrorXmlMsg.set(Some(Map(
                        "location" -> <p>PersonSnips.bindPerson</p>,
                        "message" -> <p>{msg}</p>)))
                      //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
                      //S.setSessionAttribute("appError", msg)
                    })
                }
              case _ =>
                val msg = "bindPerson: There is no Person for id = " + bindingPersonId
                log.debug(msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>PersonSnips.bindPerson</p>,
                    "message" -> <p>{msg}</p>)))
                  //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
                  //S.setSessionAttribute("appError", msg)
                })
            }
        }
      case "fSpouse" =>
        val bindingPerson = Model.find(classOf[Person], bindingPersonId).get
        val fam = Model.find(classOf[Family], familyId).get
        bindingPerson.gender match {
          case "M" =>
            fam.wifeId = toBeBoundPersonId
          case "F" =>
            fam.husbandId = toBeBoundPersonId
          case _ =>
            val msg = <_>bindPerson: role={role} illegal gender: person={bindingPersonId} gender={bindingPerson.gender}</_>.text
            log.debug(msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>PersonSnips.bindPerson</p>,
                "message" -> <p>{msg}</p>)))
              //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
              //S.setSessionAttribute("appError", msg)
            })
        }
        if (AccessControl.isAuthenticated_?) fam.setSubmitter(CurrentUser.is.open_!)
        Model.merge(fam)
      case "sF" =>
        val bindingPerson = Model.find(classOf[Person], bindingPersonId).get
        bindingPerson.gender match {
          case "M" =>
            val newFam = new Family();
            newFam.husbandId = bindingPersonId
            newFam.wifeId = toBeBoundPersonId
            if (AccessControl.isAuthenticated_?) newFam.setSubmitter(CurrentUser.is.open_!)
            Model.merge(newFam)
          case "F" =>
            val newFam = new Family();
            newFam.husbandId = toBeBoundPersonId
            newFam.wifeId = bindingPersonId
            if (AccessControl.isAuthenticated_?) newFam.setSubmitter(CurrentUser.is.open_!)
            Model.merge(newFam)
          case _ =>
            val msg = <_>bindPerson: role={role} illegal gender: person={bindingPersonId} gender={bindingPerson.gender}</_>.text
            log.debug(msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>PersonSnips.bindPerson</p>,
                "message" -> <p>{msg}</p>)))
              //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
              //S.setSessionAttribute("appError", msg)
            })
        }
      case parentC if "-cBcS".indexOf(parentC) > 0 =>
        familyId match {
          case 0L =>
            val msg = ("bindPerson: A familyId is 0 when roles cB cS")
            log.debug(msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>PersonSnips.bindPerson</p>,
                "message" -> <p>{msg}</p>)))
              //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
              //S.setSessionAttribute("appError", "A familyId is 0 when roles cB cS")
            })
          case existingId =>
            val toBeBoundPerson = Model.find(classOf[Person], toBeBoundPersonId)
            val family = Model.find(classOf[Family], existingId)
            toBeBoundPerson match {
              case Some(x) =>
                family match {
                  case Some(y) =>
                    x.family = y
                    if (AccessControl.isAuthenticated_?) x.setSubmitter(CurrentUser.is.open_!)
                    Model.merge(x)
                  case _ =>
                    val msg = ("bindPerson: There is no Family for id = " + existingId)
                    log.debug(msg)
                    S.redirectTo("/errorPage", () => {
                      ErrorXmlMsg.set(Some(Map(
                        "location" -> <p>PersonSnips.bindPerson</p>,
                        "message" -> <p>{msg}</p>)))
                      //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
                      //S.setSessionAttribute("appError", "There is no Family for id = " + existingId)
                    })
                }
              case _ =>
                val msg = ("bindPerson: There is no Person for id = " + bindingPersonId)
                log.debug(msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>PersonSnips.bindPerson</p>,
                    "message" -> <p>{msg}</p>)))
                  //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
                  //S.setSessionAttribute("appError", "There is no Person for id = " + bindingPersonId)
                })
            }
        }
      case parent if "-pFpM".indexOf(parent) > 0 => //  NEGERAI čia galbūt !!!  /*"-pHpW"*/
        familyId match {
          case 0L =>
          // create Family and bind H or W
            val newFam = createFamily(role, toBeBoundPersonId)
            val bindingPerson = Model.find(classOf[Person], bindingPersonId)
            bindingPerson match {
              case Some(x) =>
                x.family = newFam
                if (AccessControl.isAuthenticated_?) x.setSubmitter(CurrentUser.is.open_!)
                Model.merge(x)
              case _ =>
                val msg = ("bindPerson: There is no Person for id = " + bindingPersonId)
                log.debug(msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>PersonSnips.bindPerson</p>,
                    "message" -> <p>{msg}</p>)))
                  //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
                  //S.setSessionAttribute("appError", "There is no Person for id = " + bindingPersonId)
                })
            }
          case existingId =>
            val family = Model.find(classOf[Family], existingId)
            family match {
              case Some(f) =>
                log.debug("bindPerson: pFpM: role=" + role)
                role match {
                  case "pH" =>
                    f.husbandId = toBeBoundPersonId
                  case _ => /*"pW"*/
                    f.wifeId = toBeBoundPersonId
                }
                log.debug("bindPerson: pFpM: " + f.toString(Model.getUnderlying))
                if (AccessControl.isAuthenticated_?) f.setSubmitter(CurrentUser.is.open_!)
                Model.merge(f)
              case _ =>
                val msg = ("bindPerson: There is no Family for id = " + existingId)
                log.debug(msg)
                S.redirectTo("/errorPage", () => {
                  ErrorXmlMsg.set(Some(Map(
                    "location" -> <p>PersonSnips.bindPerson</p>,
                    "message" -> <p>{msg}</p>)))
                  //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
                  //S.setSessionAttribute("appError", "There is no Family for id = " + existingId)
                })
            }
        }

      case spouse if "-sHsW".indexOf(spouse) > 0 =>
        assert(familyId == 0L)
        val newFam = createFamily(role, toBeBoundPersonId)
        val bindingPerson = Model.find(classOf[Person], bindingPersonId)
        bindingPerson match {
          case Some(x) =>
          //x.families()
            role match {
              case "sH" => newFam.wifeId = x.id
              case "sW" => newFam.husbandId = x.id
              case _ =>
            }
            if (AccessControl.isAuthenticated_?) newFam.setSubmitter(CurrentUser.is.open_!)
            Model.merge(newFam)
          case _ =>
            val msg = ("bindPerson: There is no Person for id = " + bindingPersonId)
            log.debug(msg)
            S.redirectTo("/errorPage", () => {
              ErrorXmlMsg.set(Some(Map(
                "location" -> <p>PersonSnips.bindPerson</p>,
                "message" -> <p>{msg}</p>)))
              //S.setSessionAttribute("appErrorLocation", "PersonSnips.bindPerson")
              //S.setSessionAttribute("appError", "There is no Person for id = " + bindingPersonId)
            })
        }
      case _ => // create error and go to error page
    }
  }

  //  private def findFamily(spouse: Person): Family = {
  //    log.debug(<_>findFamily: spouse={spouse.toString(Model.getUnderlying)};</_>.text);
  //    val families = spouse.families(Model.getUnderlying).filter(fam =>
  //      (spouse.gender == "M") && (fam.wifeId == 0) || (spouse.gender == "F") && (fam.husbandId == 0)
  //    )
  //    families match {
  //      case x :: xs => x
  //      case Nil => new Family()
  //    }
  //
  //    val newFamily = new Family();
  //    S.getSessionAttribute("role").toString match {
  //      case "pF" => newFamily.husbandId = spouseId
  //      case "pM" => newFamily.wifeId = spouseId
  //      case "sH" => newFamily.husbandId = spouseId
  //      case "sW" => newFamily.wifeId = spouseId
  //      case _ =>
  //    }
  //    Model.merge(newFamily); // it makes an attached copy of the passed object and returns the copy
  //    //newFamily
  //  }


  /*private*/ def createFamily(role: String, spouseId: Long): Family = {
    log.debug(<_>createFamily: role={role}; spouseId={spouseId};</_>.text);
    val newFamily = new Family();
    role match {
      case "pF" => newFamily.husbandId = spouseId
      case "pM" => newFamily.wifeId = spouseId
      case "sH" => newFamily.husbandId = spouseId
      case "sW" => newFamily.wifeId = spouseId
      case _ =>
    }
    Model.merge(newFamily); // it makes an attached copy of the passed object and returns the copy
  }

}

object PersonSnips {

  object itemsVar extends SessionVar[Map[Long, String]](Map.empty)

}
