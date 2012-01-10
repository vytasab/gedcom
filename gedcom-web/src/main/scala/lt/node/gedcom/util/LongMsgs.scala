package lt.node.gedcom.util

import _root_.net.liftweb._
import http._
import util._
import util.Helpers._

import _root_.scala._
import xml.NodeSeq

object LongMsgs {

  def getMsg(msgKey: String): NodeSeq = this.longMsgs(msgKey)(S.locale.getLanguage)

  def getMsgText(msgKey: String): String = this.getMsg(msgKey).text

  val getHostPortApp = Props.get("host__").openOr("localhost") +
    Props.get("_port_").openOr(":8080") + Props.get("__app").openOr("/gedcom-web/")

  val postAddUser: Map[String, NodeSeq] = Map(
    "en" -> (<p>Go to your mailbox. </p>
      <p>Open mail [<b>New user has been created in http://...</b>] and click on <i>link</i> in the mail. </p>
      <p>The system will activate your user account you just created. </p>
      <p>Move mouse on <b>Authentication</b>, in one second submenu will appear. </p>
      <p>Click on <b>Log in.</b>
      </p>),
    "lt" -> (<p>Eikit savo pašto dėžutėn. </p>
      <p>Atidarykite laišką [<b>New user has been  created in http://...</b>]</p>
      <p>Tame laiške pele paspauskite nuorodą [<b>http://...</b>] ->
     sistema aktyvuos jūsų ką tik sukurtą naudotojo paskyrą.</p>
      <p>Pele pasiekite pagrindinio meniu punktą <b>Autentikacija</b> ir palaukite vieną sekundę. </p>
      <p>Išsiskleidusiame meniu spustelėkite pele <b>Prisijungti</b> meniu punktą. </p>)
  )
  val endupValidation: Map[String, NodeSeq] = Map(
    "en" -> (<p>The system has just activated your user account. </p>
      <p>Close this browser window and return to previous one. </p>),
    "lt" -> (<p>Sistema ką tik aktyvavo jūsų sukurtą naudotojo paskyrą. </p>
      <p>Uždarykire šitą naršyklės langą ir atidarykite tą langą, kuriame įvedėte naujo naudotojo duomenis. </p>)
  )

  val longMsgs: Map[String, Map[String, NodeSeq]] = Map(
    "after.add.user" -> postAddUser,
    "endup.validation" -> endupValidation,
    "new.user.subj" -> Map(
      "en" -> (<p>New user has been created in {getHostPortApp/*Props.get("host.port.app") openOr ("localhost:8080/gedcom-web/")*/}</p>),
      "lt" -> (<p>Naudotojas sukurtas web saite {getHostPortApp/*Props.get("host.port.app") openOr ("localhost:8080/gedcom-web/")*/}</p>)
    ),
    "new.user.p1" -> Map(
      "en" -> (<p>Someone, probably you, used your email address to request that your credentials
        (in just created user account) be validated. To do this, follow the link below:</p>),
      "lt" -> (<p>Kažkas, tikėtiniausia Jūs, naudojote šį epašto adresą ką tik sukurto naudotojo paskyros
        validavino veiksmui. Spustelkite ant žemiau esančios nuorodos, kad tai atlikti: </p>)
    ),
    "new.user.p2" -> Map(
      "en" -> (<span>This validation address will expire at </span>),
      "lt" -> (<span>Validacija galima iki</span>)
    ),
    "change.password.subj" -> Map(
      "en" -> (<p>Your credentials have been changed</p>),
      "lt" -> (<p>J\u016Bs\u0173 slaptažodis pakeistas</p>)
    ),
    "change.password.p1" -> Map(
      "en" -> (<p>Someone, probably you, used a validation email sent to this address to successfully change
               your credentials. If this wasn't done by you, please contact the webmaster at </p>),
      "lt" -> (<p>Kažkas, tikėtiniausia Jūs, naudojote šį epašto adresą naudotojo paskyros slaptažodžio pakeitimo
        validavino veiksmui atlikti. Jei tai buvote ne Jūs tuoj pat praneškite sistemos administratoriui.</p>)
    ),
    "password.reset.subj" -> Map(
      "en" -> (<p>Important information</p>),
      "lt" -> (<p>Svarbi informacija</p>)
    ),
    "password.reset.p1" -> Map(
      "en" -> (<span>Someone, probably you, used your email address to request that your credentials for the </span>),
      "lt" -> (<span>Kažkas, tikėtiniausia Jūs, naudojote šį epašto adresą naudotojo paskyros slaptažodžio pakeitimo
        veiksmui atlikti svetainėje </span>)
    ),
    "password.reset.p2" -> Map(
      "en" -> (<span>site be reset. To make changes to your site credentials, follow the link below: </span>),
      "lt" -> (<span> atlikti. Spustelkite ant žemiau esančios nuorodos, kad tai atlikti: </span>)
    ),
    "lostPassword.msg" -> Map(
      "en" -> (<p>To reset your password, enter your email address in the form below and click the reset button.</p>
        <p>You will receive instructions on how to reset your password via that email address. </p>
        <p>Note: you must have previously registered your email address with this site for this to work.</p>),
      "lt" -> (<p>Slaptažodžio panaikinimui įveskite jūsų epašto adresą ir paspauskite mygtuką.</p>
        <p>Savp epašto dėžutėje rasita tolimesnius nurodymus. </p>
        <p>Pastaba: Jūs turite būti registravęs naudotojo paskyrą šioje svetainėje.</p>)
    ),
    "confirm.del.pepafe" -> Map
      ("en" -> (<p>Are you sure this info is wrong and you want to delete it?</p>),
        "lt" -> (<p>Ar Jūs tikrai manote VISĄ šią informaciją esant klaidinga ir norite ją ištrinti?</p>)
    ),
    "confirm.remove.familychild" -> Map
      ("en" -> (<p>Are you sure you want to remove the child from family?</p>),
        "lt" -> (<p>Ar Jūs tikrai tikrai norite ištrinti vaiką iš šeimos?</p>)
    ),
    "confirm.del.family" -> Map
      ("en" -> (<p>Are you sure you want to remove the family?</p>),
        "lt" -> (<p>Ar Jūs tikrai tikrai norite ištrinti šeimą?</p>)
    ),
    //"xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>)),
    //"xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>)),
    //"xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>)),
    //"xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>)),
    "xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>))
  )
}

