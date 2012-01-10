package lt.node.gedcom.util

import _root_.net.liftweb._
import http._
import common._
import scala.List

trait GedcomMsgsI18n {

  def getMsg(key: String /*, tags: List[(String, String)]*/): String = tags.find(_._1 == key).orElse(Full((key, key))).get._2

  def getMsg(key: String, lang: String): String = tags(lang).find(_._1 == key).orElse(Full((key, key))).get._2

  def getKey(value: String): String = {
    //println("GedcomMsgsI18n: " + tags.toString)
    tags.find(_._2 == value).get._1
  }

  def getKey(value: String, lang: String): String = tags(lang).find(_._2 == value).get._1

  def tags: List[(String, String)] = tags(S.locale.getLanguage)

  def tags(lang: String): List[(String, String)] = msgsByLocale(lang).
    sortWith((a, b) => {
    a._1.substring(0, 2).toInt < b._1.substring(0, 2).toInt
  }).
    map((kv) => {
    (kv._1.substring(2), kv._2)
  })

  def msgsByLocale(lang: String): List[(String, String)] = msgs.map {
    kv => (kv._1, kv._2(lang))
  }.toList

  val msgs: Map[String, Map[String, String]]

}
