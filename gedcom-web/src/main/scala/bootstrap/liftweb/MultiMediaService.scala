package bootstrap.liftweb

import lt.node.gedcom.model.{Model, MultiMedia, MultiLang}
import net.liftweb.http._
import net.liftweb.common.{Full, Box}
import org.slf4j.{LoggerFactory, Logger}
import net.liftweb.util.{Helpers, TimeHelpers}

/**
 * Created with IntelliJ IDEA.
 * User: vsh
 * Date: 12/15/12
 * Time: 8:05 PM
 * To change this template use File | Settings | File Templates.
 */
object MultiMediaService extends MultiLang {

  val log: Logger = LoggerFactory.getLogger("MultiMediaService")

  private object cache extends RequestMemoize[String, Box[MultiMedia]]

  private def findFromRequest(req: Req): Box[MultiMedia] = {
    val mmId: String = req.path.wholePath.last
    log.debug(<_> mmId={mmId}</_>.text)
    //log.debug(<_> req.pathParam(0)={req.pathParam(0)}</_>.text)
    //log.debug(<_> req.pathParam(1)={req.pathParam(1)}</_>.text)
    cache.get(mmId, Model.find(classOf[MultiMedia], mmId.toLong))
  }

  def serveImage: LiftRules.DispatchPF = {
    case req@Req(List("images", _), _, GetRequest) if findFromRequest(req).isDefined =>
      () => {
        val info = findFromRequest(req).open_!
        // open is valid here because we just tested in the guard

        // Test for expiration
        req.testFor304(Helpers.millis, "Expires" -> TimeHelpers.toInternetDate(Helpers.millis + 30*24*3600*1000)) or
          // load the blob and return it
          Full( InMemoryResponse(info.blobas,
            List(("Last-Modified", TimeHelpers.toInternetDate(Helpers.millis - 1000L)),
              ("Expires", TimeHelpers.toInternetDate(Helpers.millis + 30*24*3600*1000)),
              ("Content-Type", info.mimeType)),
            Nil, 200)
          )
      }
  }

}



////--------------------------------------------------------
///*
// * ImageInfo.scala
// * Copied from http://github.com/dpp/imagine/
// */
//
//package lt.node.vshmix.model
//
//import _root_.net.liftweb._
//import mapper._
//import util._
//import common._
//import Helpers._
//import http._
//import http.RequestMemoize
//
//class ImageInfo extends LongKeyedMapper[ImageInfo] with IdPK {
//  def getSingleton = ImageInfo
//
//  object date extends MappedLong(this) {
//    override def defaultValue = Helpers.millis
//  }
//  object mimeType extends MappedPoliteString(this, 64)
//  object name extends MappedPoliteString(this, 256) {
//    override def dbIndexed_? = true
//  }
//  object blob extends MappedLongForeignKey(this, ImageBlob)
//}
//
//object ImageInfo extends ImageInfo with LongKeyedMetaMapper[ImageInfo]
//// 9C22-2/vsh  trait net.liftweb.mapper.CRUDify  // with CRUDify[Long,ImageInfo]
//{
//  private object cache extends RequestMemoize[String, Box[ImageInfo]]
//
//  private def findFromRequest(req: Req): Box[ImageInfo] = {
//    val toFind = req.path.wholePath.last
//    cache.get(toFind, find(By(name, toFind)))
//  }
//
//  def serveImage: LiftRules.DispatchPF = {
//    case req @ Req(List("images", _ ), _, GetRequest) if findFromRequest(req).isDefined =>
//      () => {
//        val info = findFromRequest(req).open_!
//        // open is valid here because we just tested in the guard
//
//        // Test for expiration
//        req.testFor304(info.date, "Expires" -> toInternetDate(millis + 30.days)) or
//          // load the blob and return it
//          info.blob.obj.map(blob => InMemoryResponse(blob.image,
//            List(("Last-Modified", toInternetDate(info.date.is)),
//              ("Expires", toInternetDate(millis + 30.days)),
//              ("Content-Type", info.mimeType.is)),
//            Nil,  200))
//      }
//  }
//
//  def allImages: List[ImageInfo] = ImageInfo.findAll()
//
//  def thisInfo(id: Long): Box[ImageInfo] = ImageInfo.findByKey(id)
//
//  def thisBlob(blob: Long): Box[ImageBlob] = ImageBlob.findByKey(blob)
//
//  def nameIsUnique(thatName: String): Boolean = ImageInfo.allImages.forall{n =>
//    val x: String = n.name.toString
//    val xx: String = x.substring(0, x.lastIndexOf("."))
//    xx != thatName
//  }
//
//}
////----------------------------------------
///*
// * ImageBlob.scala
// * Copied from http://github.com/dpp/imagine/
// */
//
//package lt.node.vshmix.model
//
//import _root_.net.liftweb._
//import mapper._
//
//class ImageBlob extends LongKeyedMapper[ImageBlob] with IdPK {
//  def getSingleton = ImageBlob
//
//  object image extends MappedBinary(this)
//}
//
//object ImageBlob extends ImageBlob with LongKeyedMetaMapper[ImageBlob]