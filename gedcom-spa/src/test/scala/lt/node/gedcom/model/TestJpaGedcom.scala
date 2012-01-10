package lt.node.gedcom.model

/*
 * Copyright 2008 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions
 * and limitations under the License.
 */

import scala.collection.JavaConversions._

import org.junit.Test
import org.junit.Before
import org.junit.After
import org.junit.Assert._

import javax.persistence._
import java.text.{ParsePosition, SimpleDateFormat}

class TestJpaGedcom {
  var emf: EntityManagerFactory = _

  //object CurrentUserId extends SessionVar[Box[Long]](Empty)
  var CurrentUserId: Option[Long] = None
  //object CurrentUser extends RequestVar[Box[User]](Empty)
  var CurrentUser: Option[User] = None

  @Before
  def initEMF() = {
    println("---- @Before")
    try
    {
      emf = Persistence.createEntityManagerFactory("jpawebTest")
      var emb = emf.createEntityManager()
      emb.getTransaction().begin()

      val toBeDeletedPersonEvents = emb.createNamedQuery("findAllPersonEvents").getResultList().asInstanceOf[java.util.List[PersonEvent]]
      for (r <- toBeDeletedPersonEvents) {
        emb.remove(emb.getReference(classOf[PersonEvent], r.asInstanceOf[PersonEvent].id)); println("PersonEvent")
      }

      val toBeDeletedFamilies = emb.createNamedQuery("findAllFamilies").getResultList().asInstanceOf[java.util.List[Family]]
      for (r <- toBeDeletedFamilies) {
        emb.remove(emb.getReference(classOf[Family], r.asInstanceOf[Family].id)); println("Family")
      }

      val toBeDeletedPersons = emb.createNamedQuery("findAllPersons").getResultList().asInstanceOf[java.util.List[Person]]
      for (r <- toBeDeletedPersons) {
        emb.remove(emb.getReference(classOf[Person], r.asInstanceOf[Person].id)); println("Person")
      }

      val toBeDeletedAudits = emb.createNamedQuery("findAllAudits").getResultList().asInstanceOf[java.util.List[Audit]]
      for (r <- toBeDeletedAudits) {
        emb.remove(emb.getReference(classOf[Audit], r.asInstanceOf[Audit].id)); println("Audit")
      }

      val toBeDeletedUsers = emb.createNamedQuery("findAllUsers").getResultList().asInstanceOf[java.util.List[User]]
      for (r <- toBeDeletedUsers) {
        emb.remove(emb.getReference(classOf[User], r.asInstanceOf[User].id)); println("User")
      }

      emb.getTransaction().commit()
      emb.close()
    } catch {
      case e: Exception => {
        def printAndDescend(ex: Throwable): Unit = {
          println(e.getMessage())
          if (ex.getCause() != null) {
            printAndDescend(ex.getCause())
          }
        }
        printAndDescend(e)
      }
    }
  }

  @After
  def closeEMF() = {
    println("---- @After")
    if (emf != null) emf.close()
  }

  @Test
  def save_stuff() = {
    println("---- @Test")
    val isoDate = new SimpleDateFormat("yyyy-MM-dd")
    var em = emf.createEntityManager()
    val tx = em.getTransaction()
    tx.begin()
    try
    {
      val user = new User
      user.firstName = "Vytautas"
      user.lastName = "Šabanas"
      user.birthDate = isoDate.parse("1949-04-20", new ParsePosition(0))
      user.locale = "lt"
      user.timezone = "Europe/Vilnius"
      user.emailAddress = "vsh@node.lt"
      user.password_=("123123")
      user.setValidation
      user.validated = true
      em.persist(user)
      CurrentUser = (Some(user))
      CurrentUserId = (Some(user.id))

      val mPerson = new Person
      mPerson.nameGivn = "Vytautas"
      mPerson.nameSurn = "Šabanas"
      mPerson.gender = "M"
      mPerson.setSubmitter(user)
      em.persist(mPerson)

      val mpe = new PersonEvent
      mpe.tag = "BIRT"
      mpe.setSubmitter(user)
      em.persist(mpe)
      mpe.personevent = mPerson

      val mped = new EventDetail
      mped.descriptor = "Gimė dabartiniame dėdės Danielaus Sutkaus name, gimė gana silpnas"
      mped.dateValue = "1949-04-20"
      mped.place = "Raudėnai, Šiaulių raj."
      mped.setSubmitter(user)
      mped.personevent = mpe
      em.persist(mped)
      mpe.getEventDetail(em)
      mpe.personevent = mPerson
      println(mped.toString)

      var a = new Audit
      a.entityName = "EventDetail"
      a.entityId = mped.id
      a.action = "add"
      a.message = (<lt>{scala.xml.Utility.escape(<id>999</id>.toString)}</lt>).toString
      a.setSubmitter(user)
      em.persist(a)
      println(a.toString())

      val mpa = new PersonAttrib
      mpa.tag = "NCHI"
      mpa.tagValue = "2"
      mpa.setSubmitter(user)
      em.persist(mpa)
      mpa.personattrib = mPerson

      val mpad = new EventDetail
      //mped.descriptor = "Gimė dabartiniame dėdės Danielaus Sutkaus name, gimė gana silpnas"
      //mped.dateValue = "1949-04-20"
      mpad.place = "Kaunas, RKKL-KMUK-LSUK"
      mpad.setSubmitter(user)
      mpad.personattrib = mpa
      em.persist(mpad)
      mpa.getAttribDetail(em)
      mpa.personattrib = mPerson
      println(mpad.toString)

      a = new Audit
      a.entityName = "EventDetail"
      a.entityId = mpad.id
      a.action = "add"
      a.message = (<lt>{scala.xml.Utility.escape(<id>__a.id_ for_PersonAttrib__</id>.toString)}</lt>).toString
      a.setSubmitter(user)
      em.persist(a)
      println(a.toString())

      val fPerson = new Person
      fPerson.nameGivn = "Dalia"
      fPerson.nameSurn = "Šabanienė"
      fPerson.gender = "F"
      fPerson.setSubmitter(user)
      em.persist(fPerson)
      println(fPerson.toString(em))

      val vdFam = new Family
      vdFam.husbandId = mPerson.id
      vdFam.wifeId = fPerson.id
      vdFam.setSubmitter(user)
      em.persist(vdFam)
      println(vdFam.toString(em))

      val fe = new FamilyEvent
      fe.tag = "MARR"
      fe.setSubmitter(user)
      em.persist(fe)
      fe.familyevent = vdFam

      val vdFamEd = new EventDetail
      vdFamEd.descriptor = "Vestuvių pokylis buvo 'Vilija' kavinėje Vilijampolėje; ten vėliau buvo mafų 'meeting point'. Štai taip."
      vdFamEd.dateValue = "1979-06-30"
      vdFamEd.place = "Kaunas, Vilijampolė, kavinė 'Vilija'"
      vdFamEd.setSubmitter(user)
      vdFamEd.familyevent = fe
      em.persist(vdFamEd)
      fe.getEventDetail(em)
      //mpe.personevent = mPerson
      println(vdFamEd.toString)

      a = new Audit
      a.entityName = "EventDetail"
      a.entityId = vdFamEd.id
      a.action = "add"
      a.message = (<lt>{scala.xml.Utility.escape(<id>111</id>.toString)}</lt>).toString
      a.setSubmitter(user)
      em.persist(a)
      println(a.toString())

      val chldPerson = new Person
      chldPerson.nameGivn = "Andrius"
      chldPerson.nameSurn = "Šabanas"
      chldPerson.gender = "M"
      chldPerson.family = vdFam
      chldPerson.setSubmitter(user)
      em.persist(chldPerson)
      vdFam.getChildren(em)
      println(chldPerson.toString(em))

      tx.commit()
    } catch {
      case ee: EntityExistsException => println("EntityExistsException " + ee.toString)
      case pe: PersistenceException => println("PersistenceException " + pe.toString)
      case e: Exception => println("Exception " + e.toString)
      tx.rollback();
    } finally {
      em.close()
    }
    // Re-open and query
    em = emf.createEntityManager()
    val retrievedPersons = em.createNamedQuery("findAllPersons").getResultList().asInstanceOf[java.util.List[Person]]
    println("Persons:")
    retrievedPersons foreach (r => println(r.toString(em)))


    val retrievedPerson = em.createNamedQuery("findPersonByGnSnGender").
      setParameter("nameGivn", "Andrius").setParameter("nameSurn", "Šabanas").setParameter("gender", "M").
      getSingleResult.asInstanceOf[Person]
    assertEquals("Andrius", retrievedPerson.nameGivn)
    assertEquals("Šabanas", retrievedPerson.nameSurn)
    assertEquals("M", retrievedPerson.gender)


    val retrievedFamilies = em.createNamedQuery("findAllFamilies").getResultList().asInstanceOf[java.util.List[Family]]
    //retrieved.foreach((r:[TstPerson]) => <- retrieved; )
    println("Families:")
    for (r <- retrievedFamilies) println(r.toString())
    println("---")
    retrievedFamilies foreach (r => {
      r.getChildren(em);
      println(r.toString(em))
    }
      )
    //println(familyToString(vdFam, em))

    assert(retrievedFamilies.size() > 0)

    // clean up
    //    em.getTransaction().begin()
    //    println("TstPerson====>" + em.getReference(classOf[TstPerson], person.id).toString)
    //    em.remove(em.getReference(classOf[TstPerson], person.id))
    //    println("TstAddress====>" + em.getReference(classOf[TstAddress], address.id).toString)
    //    em.remove(em.getReference(classOf[TstAddress], address.id))
    //    em.getTransaction().commit()
    if (em != null) em.close()

  }

}
