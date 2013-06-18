package lt.node.gedcom.model

import _root_.javax.persistence._

/**
This class represents a user with login privileges on the website.
 */

@Entity
@Table(name = "tstaddress")
class TstAddress {
  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "addressId")
  var id: Long = _

  @Column(name = "fullAddress", unique = false, nullable = false, length = 50)
  var fullAddress: String = ""

  @OneToOne(mappedBy = "address") // inverse=true, pointnig TstPerson's address field
  var person: TstPerson = _

  override def toString() = "addres:[" + id + "] " + fullAddress + " " // causes endless loop =>  + person.toString

}