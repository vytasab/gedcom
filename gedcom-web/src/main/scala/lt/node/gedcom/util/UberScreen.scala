package lt.node.gedcom.util

/**
 * Lift Google group: Uberscreen
 * author: Maarten Koopmans
 */

//trait UberScreen

import net.liftweb.http.{LiftScreen, SHtml}
import net.liftweb.common.{Box, Full}
import scala.xml._

trait UberScreen extends LiftScreen {

  override def finish: Unit = println("UberScreen.finish method is called")

  def makeField[T](a_name: String, a_default: T, theToForm: (Field {type ValueType = T} => Box[NodeSeq])) = {
    //Console println ("inside makeField")
    new Field {
      type ValueType = T

      override def name: String = a_name

      override implicit def manifest = buildIt[T]

      override def default: T = a_default

      override def toForm: Box[NodeSeq] = theToForm(this)
    }
  }

  def textarea(name: String, default: String, rows: Int = 5, cols: Int = 80) = {
    //Console println (" inSide textarea")
    makeField[String](name, default, field => SHtml.textarea(field.is,
      field.set(_), "rows" -> rows.toString, "cols" -> cols.toString))
    //Console println (" textarea via makeField made")
  }

  def select[T](name: String, default: T, choices: Seq[(T, String)]) = {
    makeField[Box[T]](name, Full(default), field => SHtml.selectObj(choices,
      field.is, (t: T) => field.set(Full(t))))
  }

  def multiselect[T](name: String, default: List[T], choices: Seq[(T, String)]) = {
    makeField[Box[Seq[T]]](name, Full(default), field =>
      SHtml.multiSelectObj(choices, default, (t: List[T]) => field.set(Full(t))))
  }

  def radio(name: String, default: String, choices: Seq[String]) = {
    makeField[Box[String]](name, Full(default), field =>
      Full((SHtml.radio(choices, Full(default), (t: String) =>
        field.set(Full(t)))).toForm))
  }

  def password(name: String, default: String) = {
    makeField[String](name, default, field => SHtml.password(field.is, field.set(_)))
  }

  def link(name: String, to: String, body: scala.xml.NodeSeq, onClick: () => Any) = {
    makeField[String](name, to, field => SHtml.link(to, onClick, body))
  }

  def link(name: String, to: String, body: String, onClick: () => Any = () => {}) = {
    makeField[String](name, to, field => SHtml.link(to, onClick, Text(body)))
  }
}


