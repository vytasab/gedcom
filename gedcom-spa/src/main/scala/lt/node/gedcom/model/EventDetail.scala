package lt.node.gedcom.model

import javax.persistence._

import scala.xml._
import _root_.net.liftweb._
import http.S
import common._
import scala.collection.JavaConverters._


@Entity
@Table(name = "eventdetail")
class EventDetail extends MultiLang {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  var id: Long = _

  @Column(unique = false, nullable = true, length = 8000)
  var descriptor: String = ""
  // must be used for "EVEN' tag only
  // locale wrapped up: <lt>Kažkoks tekstas...</lt>[<xx>...</xx>...]

  @Column(unique = false, nullable = true, length = 35)
  var dateValue: String = ""
  // <DATE> = yyyy[-mm[-dd]]
  // <DATE> | FROM <DATE> | TO <DATE> | FROM <DATE> TO <DATE> |
  // BEFORE <DATE> | AFTER <DATE> | BETWEEN <DATE> AND <DATE> |
  // ABOUT <DATE> | CALCULATED <DATE> | ESTIMATED <DATE>
  // INTERPRETED <DATE> (<DATE_PHRASE: some explanatory text>) | (<DATE_PHRASE: some explanatory text>)

  @Column(unique = false, nullable = true, length = 32000)
  var place: String = ""

  @Column(unique = false, nullable = true, length = 12)
  var ageAtEvent: String = ""
  /*
AGE_AT_EVENT: = {Size=1:12}
[ < | > | <NULL>]
[ YYy MMm DDDd | YYy | MMm | DDDd | YYy MMm | YYy DDDd | MMm DDDd |
CHILD | INFANT | STILLBORN ] ]
Where :
> = greater than indicated age
< = less than indicated age
y = a label indicating years
m = a label indicating months
d = a label indicating days
YY = number of full years
MM = number of months
DDD = number of days
CHILD = age < 8 years
INFANT = age < 1 year
STILLBORN = died just prior, at, or near birth, 0 years
   */

  @Column(unique = false, nullable = true, length = 8000)
  var cause: String = ""
  // locale wrapped up: <lt>Kažkoks tekstas...</lt>[<xx>...</xx>...]
  //     Used in special cases to record the reasons which precipitated an event.
  // Normally this will be used subordinate to a death event to show cause of death,
  // such as might be listed on a death certificate.


  @Column(unique = false, nullable = true, length = 32000)
  var source: String = ""
  // locale wrapped up: <lt>Kažkoks tekstas...</lt>[<xx>...</xx>...]
  /*  n  <<SOURCE_CITATION>>  {0:M}   +1 <<NOTE_STRUCTURE>>  {0:M}    +1 <<MULTIMEDIA_LINK>>  {0:M} */

  @Column(unique = false, nullable = true, length = 32000)
  var note: String = ""
  // locale wrapped up: <lt>Kažkoks tekstas...</lt>[<xx>...</xx>...]
  /*  [
  n  NOTE @<XREF:NOTE>@  {1:1}
    +1 SOUR @<XREF:SOUR>@  {0:M}
  |
  n  NOTE [<SUBMITTER_TEXT> | <NULL>]  {1:1}
    +1 [ CONC | CONT ] <SUBMITTER_TEXT>  {0:M}
    +1 SOUR @<XREF:SOUR>@  {0:M}
  ] */

  // TODO pridėti:
  //  n  <<MULTIMEDIA_LINK>>  {0:M}
  //  n  <<NOTE_STRUCTURE>>  {0:M}

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  var personevent: PersonEvent = _

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  var personattrib: PersonAttrib = _

  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  var familyevent: FamilyEvent = _

  @OneToMany(mappedBy = "eventdetailmultimedia", targetEntity = classOf[MultiMedia], cascade = Array(CascadeType.REMOVE))
  var eventdetailmultimedias: java.util.Set[MultiMedia] = new java.util.HashSet[MultiMedia]()

  var submitter = ""

  def setSubmitter(u: User) = this.submitter = u.getSubmitter()

  def getEventDetailMultiMedias(em: EntityManager) = {
    val retrievedEventDetailMultiMedia: java.util.List[MultiMedia] = em.createNamedQuery("findMultiMediaByEventDetail").
      setParameter("eventdetail", this).getResultList().asInstanceOf[java.util.List[MultiMedia]]
    this.eventdetailmultimedias = new java.util.HashSet[MultiMedia](retrievedEventDetailMultiMedia)
  }


  override def toString(/*em: EntityManager*/) = "eventdetail:[" + id + "] " + descriptor + "; " + dateValue + "; " +
    place + "; " + ageAtEvent + "; " + cause + "; " + source + "; " +
    ""


// TODO  padaryti dateValue locale'zatorių
//  def addAuditXml(): NodeSeq = {
//    <_>{if (descriptor.size>0) <descriptor>{MultiLangText.wrapText(descriptor)}</descriptor>}
//      {if (dateValue.size>0) <dateValue>{dateValue}</dateValue>}
//      {if (place.size>0) <place>{MultiLangText.wrapText(place)}</place>}
//      {if (ageAtEvent.size>0) <ageAtEvent>{MultiLangText.wrapText(ageAtEvent)}</ageAtEvent>}
//      {if (cause.size>0) <cause>{MultiLangText.wrapText(cause)}</cause>}
//      {if (source.size>0) <source>{MultiLangText.wrapText(source)}</source>}
//      {if (personevent!=null) <personevent_id>{personevent.id}</personevent_id>}
//      {if (personattrib!=null) <personattrib_id>{personattrib.id}</personattrib_id>}
//      {if (familyevent!=null) <familyevent_id>{familyevent.id}</familyevent_id>}
//    </_>
//  }
//  def addAudit(): String = {
//    addAuditXml().toString
//  }


  def getClone(): EventDetailClone = {
    EventDetailClone (descriptor,
      dateValue, place, ageAtEvent,
      cause,
      source,
      note,
      (if (personevent==null) "0" else personevent.id.toString),
      (if (personattrib==null) "0" else personattrib.id.toString),
      (if (familyevent==null) "0" else familyevent.id.toString)
    )
  }

  def getAuditXml(old: Box[EventDetailClone]): NodeSeq = {
    val lang = S.locale.getLanguage
    old match {
      case Full(x) =>
        AuditHelper.checkChanges(List(
          ("descriptor", lang, descriptor, x.descriptor),
          ("dateValue", "", dateValue, x.dateValue),
          ("place", lang, place, x.place),
          ("ageAtEvent", ""/*B504-3/vsh lang*/, ageAtEvent, x.ageAtEvent),
          ("cause", lang, cause, x.cause),
          ("source", lang, source, x.source),
          ("note", lang, note, x.note),
          ("personevent_id", "", (if (personevent==null) "0" else personevent.id.toString), x.personevent_id),
          ("personattrib_id", "", (if (personattrib==null) "0" else personattrib.id.toString), x.personattrib_id),
          ("familyevent_id", "", (if (familyevent==null) "0" else familyevent.id.toString), x.familyevent_id)))
      case Empty =>
        AuditHelper.checkAddFields(List(
          ("descriptor", lang, descriptor),
          ("dateValue", "", dateValue),
          ("place", lang, place),
          ("ageAtEvent", ""/*B504-3/vsh lang*/, ageAtEvent),
          ("cause", lang, cause),
          ("source", lang, source),
          ("note", lang, note),
          ("personevent_id", "", (if (personevent==null) "0" else personevent.id.toString)),
          ("personattrib_id", "", (if (personattrib==null) "0" else personattrib.id.toString)),
          ("familyevent_id", "", (if (familyevent==null) "0" else familyevent.id.toString))))
      case _ =>
        NodeSeq.Empty
    }
  }

  def getAuditRec(old: Box[EventDetailClone]): String = {
    getAuditXml(old).toString
  }


/*
  def avoidNull(string: String): String = {
    (string != null) match {
      case true if string.startsWith("<_") == true =>
        string
      case true if string.startsWith("<_") == false =>
        <_ d="en"><en>{string}</en></_>.toString()
      case _ =>
        <_ d="en"><en></en></_>.toString()
    }
  }
*/


  def toXml(): NodeSeq = {
    //val a: NodeSeq = (for (mm <- this.eventdetailmultimedias.toArray) yield mm.asInstanceOf[MultiMedia].toXml()).
    /*val a: NodeSeq = (this.eventdetailmultimedias.toArray.asInstanceOf[Array[MultiMedia]]).map {
      (m: MultiMedia) => { m.toXml() } } toSeq*/
    <ed id={id.toString}>
      <descriptor>{Unparsed(avoidNull(descriptor))}</descriptor>
      <dateValue>{/*doLocalizedDate*//*localeGedcomDate*/(dateValue)}</dateValue>
      <place>{Unparsed(avoidNull(place))}</place>
      <ageAtEvent>{doLocalizedAgeAtEevent(ageAtEvent)}</ageAtEvent>
      <cause>{Unparsed(avoidNull(cause))}</cause>
      <source>{Unparsed(avoidNull(source))}</source>
      <note>{Unparsed(avoidNull(note))}</note>
      {(this.eventdetailmultimedias.asScala.toList).map{m: MultiMedia => {m.toXml}}.toSeq}
      <!--{ val it = this.eventdetailmultimedias.iterator();  while (it.hasNext) { {it.next().toXml()} } }-->
      <!--{ for (mm: MultiMedia <- this.eventdetailmultimedias.toArray) yield
        {mm.toXml()}
      } -->
      <!--{ this.eventdetailmultimedias.toArray.foreach ((mm: MultiMedia) =>  {mm.toXml()}) }-->
    </ed>
  }
  // {(this.eventdetailmultimedias.asInstanceOf[java.util.Set[MultiMedia]])..map{m: MultiMedia => {m.toXml}}.toSeq}

  /**
   * Transforms GEDCOM date to localized format. Date format is yyyy [mm [dd]]
   */
  def doLocalizedDate(gedcomDate: String): String = {
    import net.liftweb.http.S
    import java.util.StringTokenizer

    val sb: StringBuilder = new StringBuilder("")
    val st: StringTokenizer = new StringTokenizer(gedcomDate)
    while (st.hasMoreTokens()) {
      st.nextToken() match {
        case "BET" => sb.append(S.?("gd_bet")).append(" ")
        case "BEF" => sb.append(S.?("gd_bef")).append(" ")
        case "AFT" => sb.append(S.?("gd_aft")).append(" ")
        case "ABT" => sb.append(S.?("gd_abt")).append(" ")
        case "FROM" => sb.append(S.?("gd_from")).append(" ")
        case "TO" => sb.append(S.?("gd_to")).append(" ")
        case "AND" => sb.append(S.?("gd_and")).append(" ")
        case string => sb.append(string).append(" ")
      }
    }
    sb.toString.trim
  }


  /**
   * Transforms GEDCOM ageAtEvent to localized format.
   */
  def doLocalizedAgeAtEevent(ageAtEvent: String): String = {
    ageAtEvent
  }

// TODO C118-3/vsh suskaidyti ilgą tekstą į gabalus max 255 chars per CONT ar CONC

  def toGedcom(levelNumber: Int, lang: String): String = {
    //val txt: StringBuffer = new StringBuffer(<_>1 {tag}</_>.text+"\n")
    val txt: StringBuffer = new StringBuffer("")
    (avoidEmpty(descriptor)).length > 0 match {
      case true => txt.append(<_>{levelNumber} TYPE  {(avoidEmpty(descriptor))}</_>.text+"\n")
      case _ =>
    }
    //(avoidEmpty(dateValue)).length > 0 match {
    (dateValue != null && dateValue.length() > 0) match {
      case true => txt.append(<_>{levelNumber} DATE {(dateValue)}</_>.text+"\n")  //TODO convert to dd MMM YYYY
      case _ =>
    }
    (avoidEmpty(place)).length > 0 match {
      case true => txt.append(<_>{levelNumber} PLAC {getLangText(avoidEmpty(place), lang)}</_>.text+"\n")
      case _ =>
    }
    //(avoidEmpty(ageAtEvent)).length > 0 match {
    (ageAtEvent != null && ageAtEvent.length() > 0) match {
      case true => txt.append(<_>{levelNumber} AGE {(/*avoidEmpty*/(doLocalizedAgeAtEevent(ageAtEvent)))}</_>.text+"\n")
      case _ =>
    }
    (avoidEmpty(cause)).length > 0 match {
      case true => txt.append(<_>{levelNumber} CAUS {getLangText(avoidEmpty(cause), lang)}</_>.text+"\n")
      case _ =>
    }
    (avoidEmpty(source)).length > 0 match {
      case true => txt.append(<_>{levelNumber} SOUR {getLangText(avoidEmpty(source), lang)}</_>.text+"\n")
      case _ =>
    }
    (avoidEmpty(note)).length > 0 match {
      case true => txt.append(<_>{levelNumber} NOTE {getLangText(avoidEmpty(note), lang)}</_>.text+"\n")
      case _ =>
    }
    /* <ed id={id.toString}>
      //<descriptor>{Unparsed(avoidNull(descriptor))}</descriptor>
      //<dateValue>{doLocalizedDate(dateValue)}</dateValue>
      //<place>{/*Unparsed*/(avoidNull(place))}</place>
      //<ageAtEvent>{doLocalizedAgeAtEevent(ageAtEvent)}</ageAtEvent>
      //<cause>{Unparsed(avoidNull(cause))}</cause>
      //<source>{Unparsed(avoidNull(source))}</source>
      //<note>{Unparsed(avoidNull(note))}</note>
    </ed> */
    txt.toString
  }

}

case class EventDetailClone (
  descriptor:String,
  dateValue:String, place:String, ageAtEvent:String,
  cause:String,
  source:String,
  note:String,
  personevent_id:String, personattrib_id:String, familyevent_id:String
)
