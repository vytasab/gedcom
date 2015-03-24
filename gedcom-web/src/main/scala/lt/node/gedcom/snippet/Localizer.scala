package lt.node.gedcom.snippet

import _root_.lt.node.gedcom.util.{PeTags,PaTags,FeTags}
import net.liftweb.common.{Loggable,Logger}

object Localizer extends Loggable {
  val log = Logger("Localizer");

  def tagMsg(context: String, pref: String, suff: String, text: String): String = {

        /*  // B402-6/vsh veikia taip
        // http://stackoverflow.com/questions/978671/scala-replaceallin
        // http://d.hatena.ne.jp/mpen/20091108/p1    <-- daug regexp pavyzdukų
        val regexp = """(!_\w{3,4}_)""".r  // --> taip netinka:  """^.*?(!_\w{3,4}_).*?""".r
        while((regexp findFirstIn resHtml) != None) {
          log.debug("regexp.findFirstIn(resHtml).get "+regexp.findFirstIn(resHtml).get)
          val m = regexp.findFirstIn(resHtml).get.replaceFirst("!_","").replaceAll("_","")
          log.debug("m "+m)
          resHtml = regexp replaceFirstIn (resHtml, FeTags.getMsg(m))
        }
        "#childreninfo" #> Unparsed(resHtml)
        */

        val tagRegexRawString: String = "(" + pref +  "\\w{3,4}" + suff + ")"  // --> taip netinka: """.*?(!_\w{3,4}_).*?"""
        val tagRegex = java.util.regex.Pattern.compile(tagRegexRawString).matcher(text)
        val sb = new StringBuffer(1000*0)
        val rsb = new StringBuffer(1000*0)
        while (tagRegex.find) {
          rsb.replace(0, rsb.length, tagRegex.group(1))
          log.debug("rsb.1 "+rsb.toString)
          rsb.delete(0,/*2*/pref.length).reverse.delete(0,/*1*/suff.length).reverse
          log.debug("rsb.2 "+rsb.toString)
          val tagMessage: String = context match {
            case "Pe" => PeTags.getMsg(rsb.toString)
            case "Pa" => PaTags.getMsg(rsb.toString)
            case "Fe" => FeTags.getMsg(rsb.toString)
            case _ => "__ERROR__"
          }
          tagRegex.appendReplacement(sb, tagMessage)
        }
        tagRegex.appendTail(sb)
        log.debug("sb |"+sb.toString+"|")
        sb.toString
  }

  //-- http://www.assembla.com/wiki/show/liftweb/Binding_via_CSS_Selectors
  //-- http://en.wikipedia.org/wiki/Gender_symbol --  \u2640  ♀ Venus;  \u2642  ♂ Mars;

}
