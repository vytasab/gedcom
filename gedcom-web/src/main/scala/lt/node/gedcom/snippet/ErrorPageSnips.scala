package lt.node.gedcom.snippet

import _root_.scala.xml.NodeSeq

import _root_.net.liftweb._
import common._
import bootstrap.liftweb.ErrorXmlMsg

//import textile.TextileParser

import _root_.net.liftweb.util.Helpers._

class ErrorPageSnips {
  val log = Logger("ErrorPageSnips");

  def render()/*: CssBind*/ = {
    //object ErrorXmlMsg extends RequestVar[Box[Map[String,NodeSeq]]](Empty)
    val errLocMsg: Map[String,NodeSeq] = ErrorXmlMsg.openOr(Map("location" -> <p>no info message</p>, "message" -> <p>strange: no info message</p>))
    ErrorXmlMsg.set(Empty)
    "#location" #> errLocMsg("location") &
      "#message" #> errLocMsg("message")
    //      val appErrorLocation = S.getSessionAttribute("appErrorLocation") openOr "location is unknown"
    //      val errMessage = S.getSessionAttribute("appError") openOr "Strange: no error message"
    //      S.unsetSessionAttribute("appErrorLocation")
    //      S.unsetSessionAttribute("appError")
    //      "#errLocation" #> Text(appErrorLocation) &
    //      "#errMessage" #> Text(appErrorLocation)
  }

  //object cmsnodeVar extends RequestVar(new CmsNode)                   
  //def cmsnode = cmsnodeVar.is
  //val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  //  var shape = "text"
  //  var sortField = "id"
  //  var sortMode = "A"

  //  def getParent(id: Long): Box[CmsNode] = {
  //    S.setSessionAttribute("cmsnodeId", id.toString)
  //    val item: Box[CmsNode] = id match {
  //      case 0 => {
  //        logger.debug("getParent: S.getSessionAttribute(\"cmsnodeId\") value is Empty")
  //        Empty
  //      }
  //      case someId => {
  //        try {
  //          logger.debug("SnipUserPages.getParent: someId = " + someId)
  //          val tItem = CmsNode.findByKey(id)
  //          //		    val tItem = Model.createNamedQuery[CmsNodes]("findCmsNodesById",
  //          //		      "id"->java.lang.Long.valueOf(someId.toString))
  //          //		      .getSingleResult.asInstanceOf[CmsNodes]
  //          logger.debug("SnipUserPages.getParent: tItem = " + tItem.toString)
  //          tItem // Full(tItem)
  //        } catch {
  //          case ex: javax.persistence.NoResultException => Empty
  //        }
  //      }
  //    }
  //    item match {
  //    //case Empty =>
  //      case Full(parent) => {
  //        shape = parent.shape.is
  //        sortField = parent.sortField.is
  //        sortMode = parent.sortMode.is
  //      }
  //      case _ =>
  //    }
  //    item
  //  }


  //  def somePage(xhtml: NodeSeq, id: Long): NodeSeq = {
  //    S.setSessionAttribute("cmsnodesId", id.toString)
  //    val theItem: Box[CmsNode] = getParent(id)
  //    theItem match {
  //      case Full(item) =>
  //        bind("z", xhtml,
  //          //"title" -> {localizedText(item.title, item.title_LT)},
  //          "title" -> item.cmsNodeTitle,
  //          //"abstr" -> {finalText(localizedText(item.bodyhead, item.bodyhead_LT))},
  //          "abstr" -> <div>{Unparsed(item.cmsNodeHead)}</div>,
  //          //"body" -> {finalText(localizedText(item.bodybody, item.bodybody_LT))},
  //          //"body" -> <div>{Unparsed(item.cmsNodeBody)}</div>,
  //          "body" -> <div>{finalText(item.cmsNodeBody)}</div>,
  //          //"author" -> {localizedLabel(item.author, item.author_LT, item.createdOn, item.updatedOn)},
  //          "author" -> item.author,
  //          "back_2list" -> {S.?("back_2list")},
  //          "go_2comments" -> {S.?("go_2comments")}
  //          )
  //      case Empty =>
  //        logger.debug("SnipUserPages.somePage: an item value is Empty")
  //        S.redirectTo("/errorPage", () => {
  //          S.setSessionAttribute("appErrorLocation", "SnipUserPages.somePage")
  //          S.setSessionAttribute("appError", Empty ?~ "There is no CmsNodes item for id = " + id.toString)
  //        }
  //          )
  //      case Failure(msg, exception, chain) =>
  //        logger.debug("SnipUserPages.somePage: an item value is Failure")
  //        S.redirectTo("/errorPage", () => {
  //          S.setSessionAttribute("appErrorLocation", "SnipUserPages.somePage")
  //          S.setSessionAttribute("appError", msg)
  //        }
  //          )
  //      // 9B22/vsh compile process crashes here:
  //      //      case ParamFailure(msg, exception, chain, param) =>
  //      //        logger.debug("SnipUserPages.somePage: an item value is ParamFailure " + theItem.getClass.toString)
  //      //        S.redirectTo("/errorPage", () => {
  //      //           S.setSessionAttribute("appErrorLocation", "SnipUserPages.somePage")
  //      //           S.setSessionAttribute("appError", msg) }
  //      //        )
  //      case _ =>
  //        logger.debug("SnipUserPages.somePage: an item value is " + theItem.getClass.toString)
  //        S.redirectTo("/errorPage", () => {
  //          S.setSessionAttribute("appErrorLocation", "SnipUserPages.somePage")
  //          S.setSessionAttribute("appError", theItem.getClass.toString + "  " + theItem.toString)
  //        }
  //          )
  //    }
  //  }


  //  def finalText(txt: String): NodeSeq = {
  //    shape match {
  //      case "text" => (<plaintext>{txt}</plaintext>)
  //      case "html" => (<div>{Unparsed(txt)}</div>)
  //      case "xml" => (<span><plaintext>{txt}</plaintext></span>)
  //      case "textile" => {TextileParser.toHtml(txt)}
  //      case _ => (<plaintext>{txt}</plaintext>)
  //    }
  //  }

  //-- smalsu:
  //Props.requireOrDie("page.id.home", "page.id.admin", "page.id.logInOut")
  //

  //println("=================---------- " + Props.get("users.Genea.eqName", "neeeeeerrraa"))
  //println("=================---------- " + Props.get("page.id.home", "????????????"))
  //println("=================---------- " + Props.getLong("page.id.home", 1L))
  //println("=================---------- " + Props.get("pageidhome", "123L"))

  //  def homePage(xhtml: NodeSeq): NodeSeq = somePage(xhtml, (Props.getLong("page.id.home", 1L)))
  //  //def contactsPage(xhtml : NodeSeq) : NodeSeq =  somePage(xhtml, 22)
  //  //def vshSitePage(xhtml : NodeSeq) : NodeSeq =  somePage(xhtml, 21)
  //  def adminPage(xhtml: NodeSeq): NodeSeq = somePage(xhtml, (Props.getLong("page.id.admin", 4L)))
  //  def cerPage(xhtml : NodeSeq) : NodeSeq =  somePage(xhtml,  (Props.getLong("page.id.cer", 8)))
  //  //def experimPage(xhtml : NodeSeq) : NodeSeq =  somePage(xhtml, 58)
  //  //def experimRestPage(xhtml : NodeSeq) : NodeSeq =  somePage(xhtml, 24)
  //  def logInOutPage(xhtml: NodeSeq): NodeSeq = somePage(xhtml, (Props.getLong("page.id.logInOut", 1L)))
  //  def experimPage(xhtml: NodeSeq): NodeSeq = somePage(xhtml, (Props.getLong("page.id.experim", 6L)))
  //  def experimRestPage(xhtml: NodeSeq): NodeSeq = somePage(xhtml, (Props.getLong("page.id.experimRest", 7L)))

  /*
  def render(): CssBind = {
      val msg: NodeSeq = InfoXmlMsg.openOr(<p>Strange: no info message</p>)
      //val msg = S.getSessionAttribute("appInfo") openOr <msg>Strange: no info message</msg>
      //S.unsetSessionAttribute("appInfo")
      "#message" #> msg
  }
  */
  //  def errorMessagePage(xhtml: Group): NodeSeq = {
  //    bind("e", xhtml,
  //      "location" -> Text(S.getSessionAttribute("appErrorLocation") openOr "location is unknown"),
  //      "error" -> Text(S.getSessionAttribute("appError") openOr "Strange: no error message")
  //      )
  //  }

}