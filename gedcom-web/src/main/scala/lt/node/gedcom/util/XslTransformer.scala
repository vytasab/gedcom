package lt.node.gedcom.util

import java.io.{StringReader, StringWriter}

import javax.xml.transform.{Result, Source, TransformerFactory}
import javax.xml.transform.stream.{StreamResult, StreamSource}
import net.liftweb.common.{Loggable,Logger}


// TODO B404-1/vsh efektyvumo dėlei xsl failus laikyti Session var'uose

object XslTransformer extends /*XMLApiHelper with*/ Loggable {
  // google-gr: [ResourceServer problem]
  // google-gr: [Url rewriting of images]

  val log = Logger("XslTransformer");

  def apply(xml: String, xsl: String, params: Map[String, String]): String = {
    log.debug("===|||" + xml + "|||===");
    this.useXmlStringXslFile(xml, xsl, params)
  }

  def useXmlStringXslString(xml: String, xsl: String, params: Map[String, String]): String = {
    try {
      val xmlSource: Source = new StreamSource(new StringReader(xml));
      val xsltSource: StreamSource = new StreamSource(new StringReader(xsl))
      val transformer = TransformerFactory.newInstance().newTransformer(xsltSource)

      params foreach ((kv) => transformer.setParameter(kv._1, kv._2))

      val stringWriter: StringWriter = new StringWriter();
      val result: Result = new StreamResult(stringWriter);
      transformer.transform(xmlSource, result);
      stringWriter.toString();
    } catch {
      case e: Exception =>
        e.getMessage
    }
  }

  def useXmlStringXslFile(xml: String, _xsl_xslFileName: String, params: Map[String, String]): String = {
    try {
        val is: java.io.InputStream = this.getClass().getResourceAsStream(_xsl_xslFileName);
        if (is != null) {
          log.info("Loaded xsl from file: " + this.getClass().getResource(_xsl_xslFileName).getPath());
        }

      val xmlSource: Source = new StreamSource(new StringReader(xml));
//      log.info(" --- xmlSource ---");
      val xsltSource: StreamSource = new StreamSource(is/*new StringReader(xsl)*/)
//      log.info(" --- xsltSource ---" + xsltSource.toString);
      val transformer = TransformerFactory.newInstance().newTransformer(xsltSource)
//      log.info(" --- transformer ---");

      params foreach ((kv) => transformer.setParameter(kv._1, kv._2))
//      log.info(" --- params ---");

      val stringWriter: StringWriter = new StringWriter();
//      log.info(" --- aaa ---");
      val result: Result = new StreamResult(stringWriter);
//      log.info(" --- bbb ---");
      transformer.transform(xmlSource, result);
//      log.info(" --- ccc ---");
      stringWriter.toString();
    } catch {
      case e: Exception =>
        e.getMessage
    }
  }

}
