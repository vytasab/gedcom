package lt.node.gedcom.snippet

//import _root_.scala.util._
import _root_.scala._
import xml.{Elem, NodeSeq, Group, Text}

import _root_.net.liftweb._
import http._
import SHtml._
import js._
import JsCmds._
import js.jquery._
import JqJsCmds._
import common._
import widgets.autocomplete.{AutoComplete}

import _root_.net.liftweb.util._
import _root_.net.liftweb.util.Helpers._

//import http.js.JsCmds.{RedirectTo, FocusOnLoad}

import http.{RequestVar, S, SHtml}


import bootstrap.liftweb.{ErrorXmlMsg, AccessControl, RequestedURL, CurrentUser}
import _root_.lt.node.gedcom.model.{Model,Person,Family}

class PersonSnips {
  val log = Logger("PersonSnips");

  //-- http://www.assembla.com/wiki/show/liftweb/Binding_via_CSS_Selectors
  //-- http://en.wikipedia.org/wiki/Gender_symbol --  \u2640  ♀ Venus;  \u2642  ♂ Mars;

  object personVar extends RequestVar[Box[Person]](Empty)


  def list /*(xhtml: Group): NodeSeq*/ = {
    val items = Model.createNamedQuery[Person]("findAllPersons").getResultList()
    var personList = items.map(i => ((/*"/rest/person/" + */ i.id.toString),
      ((if (i.gender == "M") "♀" else "\u2642") + " " + i.nameGivn + " " + i.nameSurn)
      )
    ).toList;
    personList = ("", "-- " + S.?("select.person") + " --") :: personList

    //log.debug(items.size.toString)
    var selectedPersonURL: String = ""

    def preparePersonData(): Unit = {
      log.debug(<_>selectedPersonURL={selectedPersonURL}</_>.text);
      S.redirectTo(<_>/rest/person/{selectedPersonURL}</_>.text)
    }

    "#domain" #> FocusOnLoad(SHtml.select(personList, Empty,
      { selectedPersonURL = _ },
      "size" -> "1", "onchange" -> "selectWhenChanged(this)" ) ) &
      "#submit" #> SHtml.submit("Save", preparePersonData)
  }


  def sublist = {
    //----------------------------------------------------------
    object personsVar extends /*Request*/ SessionVar[Map[Long, String]](Map.empty)

    def buildQuery(current: String, limit: Int): Seq[String] = {
      //log.info("buildQuery: current= " + current + " limit=" + limit)
      val persons = Model.createNamedQuery[Person]("findPersonOrGivnSurn").
        setParams("nameGivn" -> ("%" + current + "%").toString,
        "nameSurn" -> ("%" + current + "%").toString).getResultList().toList
      val id2person: Map[Long, String] = persons. /*filter{
        p => S.getSessionAttribute("gender") match {
          case Full(x) => p.gender == x
          case _ => true
        }
      }.*/
        map {
        p => (p.id, (<_>{if (p.gender == "M") "♂ " else "♀ "}{p.nameGivn + " "}{p.nameSurn}</_>.text))
      }.toMap
      personsVar(id2person)
      id2person.values.toSeq
    }

    def completeQuery(value: String): Unit = {
      log.debug(<_>completeQuery: value={value};</_>.text)
      val foundPersonId: Option[(Long, String)] =
        personsVar.is.find {
          (kv) => value == kv._2
        }
      //completeQuery(foundPersonId.get._1)
      RequestedURL(Full(<_>/rest/person/{foundPersonId.get._1}</_>.text))
      S.redirectTo(RequestedURL.openOr("/"))
    };

    "#selector" #> FocusOnLoad(AutoComplete("", buildQuery _,
      completeQuery _,
      List(("selectFirst", "false"), ("minChars", "1"))))
  }


  def listOrGnSn = {

    def buildQuery(current: String, limit: Int): Seq[String] = {
      log.info("buildQuery: current= " + current + " limit=" + limit)
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
