package lt.node.gedcom.snippet

import net.liftweb.http.{RequestVar, S}
import net.liftweb.common.{Box, Empty}
import lt.node.gedcom.model.{Person, Model}
import lt.node.gedcom.util.Utilits

import _root_.net.liftweb.util.Helpers._

/**
 * Created by IntelliJ IDEA.
 * User: padargas
 * Date: 1/17/12
 * Time: 5:44 PM
 * To change this template use File | Settings | File Templates.
 */

class Export {

  object personVar extends RequestVar[Box[Person]](Empty)


  def doPart = {

    val person: Option[Person] = Model.find(classOf[Person], S.getSessionAttribute("personId").openOr("1").toLong)
    val gedFileTop = Utilits.gedcomHEAD(<_>INDI-{S.getSessionAttribute("personId").openOr("ISNOT")}.ged</_>.text)
    val gedFileMid = person match {
      case Some(p) =>
          p.toGedcom(Model.getUnderlying, 0, S.locale.getLanguage)
      case _ => """"""
    }
      val gedFile =  gedFileTop + gedFileMid + Utilits.gedcomTRLR()
      "#gedFile" #> <pre>{gedFile}</pre>
  }

}