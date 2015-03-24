package lt.node.gedcom.util

import _root_.net.liftweb._
import http._
import util._
import util.Helpers._

import xml.NodeSeq
import java.util.Locale

object LongMsgs {

  def getMsg(msgKey: String): NodeSeq = this.longMsgs(msgKey)(S.locale.getLanguage)

  def getMsgText(msgKey: String): String = this.getMsg(msgKey).text

  val getHostPortApp = CurrentReq.value.request.scheme + "://" +
    CurrentReq.value.request.serverName       /*Props.get("host__").openOr("localhost")*/ +
    ":" + CurrentReq.value.request.serverPort /*Props.get("_port_").openOr(":8080")*/ +
    Props.get("__app").openOr("/")

  val postAddUser: Map[String, NodeSeq] = Map(
    "en" -> (<p>Go to your mailbox. </p>
      <p>Open mail [<b>Your account creation is started in ...</b>] and click on <i>link</i> in the mail. </p>
      <p>The site admin will inform you his decision about data editing rights via new email.</p>
      <p>It may take several days</p>),
      /*<p>The system will activate your user account you just created. </p>
      <p>Move mouse on <b>Authentication</b>, in one second submenu will appear. </p>
      <p>Click on <b>Log in.</b>
      </p>,*/
    "lt" -> (<p>Eikit savo pašto dėžutėn. </p>
      <p>Atidarykite laišką [<b>Pradėtas Jūsų paskyros kūrimas svetainėje ...</b>]</p>
      <p>Tame laiške pele paspauskite nuorodą [<b>http(s):// ...</b>] -></p>
      <p>Sveitainės administratorius kitu el. paštu praneš, ar Jums suteikiama duomenų pildymo/redagavimo teisė.</p>
      <p>Tai gali užtrukti kelias dienas</p>)
      /*<p>aktyvuos jūsų ką tik sukurtą naudotojo paskyrą.</p>)
      <p>Pele pasiekite pagrindinio meniu punktą <b>Autentikacija</b> ir palaukite vieną sekundę. </p>
      <p>Išsiskleidusiame meniu spustelėkite pele <b>Prisijungti</b> meniu punktą. </p>)*/
  )
  val endupValidation: Map[String, NodeSeq] = Map(
    /*"en_" -> (<p>The system has just activated your user account. </p>
      <p>Close this browser window and return to previous one. </p>),*/
    "en" -> (<p>The email will be sent to new user regarding the data add/edit rights.
       Close this browser window now.</p>),
      /*<p>Close this browser window and return to previous one. </p>),*/
    /*"lt_" -> (<p>Sistema ką tik aktyvavo jūsų sukurtą naudotojo paskyrą. </p>
      <p>Uždarykite šitą naršyklės langą ir atidarykite tą langą, kuriame įvedėte naujo naudotojo duomenis. </p>),*/
    "lt" -> (<p>Naujajam naudotojui bus išsiųstas laiškas apie suteiktą duomenų pildymo/redagavimo teisę.</p>
       <p>Uždarykite dabar šitą naršyklės langą.</p>)
     /*
     * The site admin will inform you his decision about data editing rights via new email. t may take several days.
     * SSveitainės administratorius kitu el. paštu praneš, ar Jums suteikiama duomenų pildymo/redagavimo teisė.
      Tai gali užtrukti kelias dienas.
     * */
  )
  def execLostPsw(): Map[String, NodeSeq] = Map(       // ISO_639-3
    "en" -> (<p><span>Click on </span><b>{S.?("lost.password", new Locale("eng")/*Locale.ENGLISH*/)}</b><span>, if you really forgot your password.</span></p>),
    "lt" -> (<p><span>Vykdykite </span><b>Pamiršau slaptažodį / {S.?("lost.password", new Locale("lit"))}</b><span>, jeigu taip tikrai YRA </span></p>)
  )
/*
  val execLostPsw: Map[String, NodeSeq] = Map(
    "en" -> (<p><span>Click on </span><b>{S.?("lost.password")}</b><span>, if you really forgot your password.</span></p>),
    "lt" -> (<p><span>Vykdykite </span><b>{S.?("lost.password")}</b><span>, jeigu taip tikrai YRA </span></p>)
  )
*/

  val longMsgs: Map[String, Map[String, NodeSeq]] = Map(
    "after.add.user" -> postAddUser,
    "endup.validation" -> endupValidation,
    "exec.lost.psw" -> execLostPsw,
    "new.user.subj" -> Map(
      "en" -> (<p>Your account creation is started in {getHostPortApp}</p>),
      "lt" -> (<p>Pradėtas Jūsų paskyros kūrimas svetainėje {getHostPortApp}</p>)
    ),
    "user.approval.subj" -> Map(
      "en" -> (<p>User approval decision in {getHostPortApp}</p>),
      "lt" -> (<p>Naudotojo patvirtinimas svetainėje {getHostPortApp}</p>)
    ),
    "new.user.p1" -> Map(
      "en" -> (<p>Someone, probably you, used your email address to request that your credentials
        (in just created user account) be validated. To do this, follow the link below:</p>),
      "lt" -> (<p>Kažkas, tikėtiniausia Jūs, naudojote šį epašto adresą ką tik sukurto naudotojo paskyros
        validavino veiksmui. Spustelkite žemiau esančią nuorodą, kad tai atlikti: </p>)
    ),
    "new.user.p2" -> Map(
      "en" -> (<span>This validation address will expire at </span>),
      "lt" -> (<span>Validacija galima iki</span>)
    ),
    "new.user.p3" -> Map(
      "en" -> (<span>The site admin will inform you his decision about data editing rights via new email.
        It may take several days</span>),
      "lt" -> (<span>Sveitainės administratorius kitu el. paštu praneš, ar Jums suteikiama duomenų pildymo/redagavimo teisė.
        Tai gali užtrukti kelias dienas</span>)
    ),
    "admin.response" -> Map(
      "en" -> (<span>The {getHostPortApp} site admin response </span>),
      "lt" -> (<span>Svetainnės {getHostPortApp} admin'o atsakymas</span>)
    ),
    "allow" -> Map(
      "en" -> (<span>Allow the data add/edit rights</span>),
      "lt" -> (<span>Suteikti duomenų pildymo/redagavimo teisę</span>)
    ),
    "new.user.allow" -> Map(
      "en" -> (<span>You have got the data add/edit rights</span>),
      "lt" -> (<span>Jums suteikta duomenų pildymo/redagavimo teisė</span>)
    ),
    "refuse" -> Map(
      "en" -> (<span>Do not allow the data for add/edit rights</span>),
      "lt" -> (<span>Nesuteikti duomenų pildymo/redagavimo teisės</span>)
    ),
    "new.user.refuse" -> Map(
      "en" -> (<span>You haven't got the data for add/edit rights</span>),
      "lt" -> (<span>Jums nesuteikta duomenų pildymo/redagavimo teisė</span>)
    ),
    "change.password.subj" -> Map(
      "en" -> (<p>Your credentials have been changed</p>),
      "lt" -> (<p>Jūsų slaptažodis pakeistas</p>)
    ),
    "change.password.p1" -> Map(
      "en" -> (<p>Someone, probably you, used a validation email sent to this address to successfully change
               your credentials. If this wasn't done by you, please contact the webmaster at:</p>),
      "lt" -> (<p>Kažkas, tikėtiniausia Jūs, naudojote šį epašto adresą naudotojo paskyros slaptažodžio pakeitimo
        validavino veiksmui atlikti. Jei tai buvote ne Jūs tuoj pat praneškite sistemos administratoriui:</p>)
    ),
    "change.password.retry" -> Map(
      "en" -> (<p>Repeat actions from recently read email.</p>),
      "lt" -> (<p>Pakartokite ką tik skaitytame e-laiške nurodytus veiksmus. </p>)
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
    "lfsb.res.0" -> Map(
      "en" -> (<p>There is no person by the first name, surname and date of birth. Consult the web site admin</p>),
      "lt" -> <p>Nėra asmens pagal nurodytas vardą, pavardę ir gimimo datą.</p>
        <p>Kreipkitės pagalbos į svetainės adminą</p>
    ),
    "lfsb.res.n" -> Map(
      "en" -> (<p>Several persons found under the first name, surname and date of birth.</p>
        <p>Repeat the search using the full first name, surname, or contact the web site admin.</p>),
      "lt" -> (<p>Keli asmenys rasti pagal nurodytus vardą, pavardę ir gimimo datą.</p>
        <p>Pakartokite paiešką naudodami pilna varda, pavardę arba kreipkitės pagalbos į svetainės admina.</p>)
    ),
    //"xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>)),
    //"xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>)),
    //"xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>)),

    "xxx" -> Map("en" -> (<p>en</p>), "lt" -> (<p>lt</p>))
  )
}

