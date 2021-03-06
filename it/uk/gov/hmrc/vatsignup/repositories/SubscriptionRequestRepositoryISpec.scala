/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.vatsignup.repositories

import java.util.UUID

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.play.test.UnitSpec
import uk.gov.hmrc.vatsignup.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignup.models._

import scala.concurrent.ExecutionContext.Implicits.global

class SubscriptionRequestRepositoryISpec extends UnitSpec with GuiceOneAppPerSuite with BeforeAndAfterEach {
  lazy val repo: SubscriptionRequestRepository = app.injector.instanceOf[SubscriptionRequestRepository]

  private val testSubscriptionRequest = SubscriptionRequest(
    vatNumber = testVatNumber,
    businessEntity = Some(LimitedCompany(testCompanyNumber)),
    isDirectDebit = false
  )

  override def beforeEach: Unit = {
    super.beforeEach()
    await(repo.drop)
  }

  "insert" should {
    "successfully insert and retrieve a SubscriptionRequest model" in {
      val res = for {
        _ <- repo.insert(testSubscriptionRequest)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(testSubscriptionRequest)
    }
  }

  "insertVatNumber" should {
    val testSubscriptionRequest = SubscriptionRequest(
      vatNumber = testVatNumber,
      isDirectDebit = false
    )

    "insert the subscription request where there is not already one" when {
      "isMigratable is true" in {
        val res = for {
          _ <- repo.upsertVatNumber(
            testVatNumber,
            isMigratable = true,
            isDirectDebit = false
          )
          model <- repo.findById(testVatNumber)
        } yield model

        await(res) should contain(testSubscriptionRequest)
      }
      "isMigratable is false" in {
        val res = for {
          _ <- repo.upsertVatNumber(
            testVatNumber,
            isMigratable = false,
            isDirectDebit = false
          )
          model <- repo.findById(testVatNumber)
        } yield model

        await(res) should contain(testSubscriptionRequest.copy(isMigratable = false))
      }
    }

    "replace the previous data when one already exists" in {
      val res = for {
        _ <- repo.insert(
          SubscriptionRequest(
            vatNumber = testVatNumber,
            ctReference = Some(testCtReference),
            businessEntity = Some(SoleTrader(testNino)),
            ninoSource = Some(UserEntered),
            email = Some(testEmail),
            identityVerified = true,
            isDirectDebit = false
          )
        )
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(testSubscriptionRequest)

    }
  }

  "upsertEmail" should {
    val testSubscriptionRequest = SubscriptionRequest(
      vatNumber = testVatNumber,
      email = Some(testEmail),
      isDirectDebit = false
    )

    "throw NoSuchElementException where the vat number doesn't exist" in {
      val res = for {
        _ <- repo.upsertEmail(testVatNumber, testEmail)
        model <- repo.findById(testVatNumber)
      } yield model

      intercept[NoSuchElementException] {
        await(res)
      }
    }

    "update the subscription request where there isn't already an email stored" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertEmail(testVatNumber, testEmail)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(testSubscriptionRequest)
    }

    "replace an existing stored email" in {
      val newEmail = UUID.randomUUID().toString
      val res = for {
        _ <- repo.insert(testSubscriptionRequest)
        _ <- repo.upsertEmail(testVatNumber, newEmail)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(SubscriptionRequest(testVatNumber, email = Some(newEmail), isDirectDebit = false))
    }
  }

  "upsertTransactionEmail" should {
    val testSubscriptionRequest = SubscriptionRequest(
      vatNumber = testVatNumber,
      transactionEmail = Some(testEmail),
      isDirectDebit = false
    )

    "throw NoSuchElementException where the vat number doesn't exist" in {
      val res = for {
        _ <- repo.upsertTransactionEmail(testVatNumber, testEmail)
        model <- repo.findById(testVatNumber)
      } yield model

      intercept[NoSuchElementException] {
        await(res)
      }
    }

    "update the subscription request where there isn't already a transaction email stored" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertTransactionEmail(testVatNumber, testEmail)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(testSubscriptionRequest)
    }

    "replace an existing stored transaction email" in {
      val newEmail = UUID.randomUUID().toString
      val res = for {
        _ <- repo.insert(testSubscriptionRequest)
        _ <- repo.upsertTransactionEmail(testVatNumber, newEmail)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(SubscriptionRequest(
        testVatNumber,
        transactionEmail = Some(newEmail),
        isDirectDebit = false
      ))
    }
  }

  "upsertIdentityVerified" should {
    "throw NoSuchElementException where the vat number doesn't exist" in {
      val res = for {
        _ <- repo.upsertIdentityVerified(testVatNumber)
        model <- repo.findById(testVatNumber)
      } yield model

      intercept[NoSuchElementException] {
        await(res)
      }
    }

    "update the subscription request with IdentityVerified" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertIdentityVerified(testVatNumber)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(SubscriptionRequest(
        vatNumber = testVatNumber,
        identityVerified = true,
        isDirectDebit = false
      ))
    }
  }

  "upsertBusinessEntity" should {
    "store a Sole Trader" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, SoleTrader(testNino))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        ninoSource = Some(UserEntered),
        businessEntity = Some(SoleTrader(testNino)),
        isDirectDebit = false
      ))
    }
    "store a Limited Company" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, LimitedCompany(testCompanyNumber))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(LimitedCompany(testCompanyNumber)),
        isDirectDebit = false
      ))
    }
    "store a non UK company with UK establishment with FC prefix in CRN" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, LimitedCompany(testNonUKCompanyWithUKEstablishmentCompanyNumberFC))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(LimitedCompany(testNonUKCompanyWithUKEstablishmentCompanyNumberFC)),
        isDirectDebit = false
      ))
    }
    "store a non UK company with UK establishment with SF prefix in CRN" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, LimitedCompany(testNonUKCompanyWithUKEstablishmentCompanyNumberSF))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(LimitedCompany(testNonUKCompanyWithUKEstablishmentCompanyNumberSF)),
        isDirectDebit = false
      ))
    }
    "store a non UK company with UK establishment with NF prefix in CRN" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, LimitedCompany(testNonUKCompanyWithUKEstablishmentCompanyNumberNF))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(LimitedCompany(testNonUKCompanyWithUKEstablishmentCompanyNumberNF)),
        isDirectDebit = false
      ))
    }
    "store a General Partnership" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, GeneralPartnership(testUtr))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(GeneralPartnership(testUtr)),
        isDirectDebit = false
      ))
    }
    "store a Limited Partnership" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, LimitedPartnership(testUtr, testCompanyNumber))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(LimitedPartnership(testUtr, testCompanyNumber)),
        isDirectDebit = false
      ))
    }
    "store a Limited Liabiility Partnership" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, LimitedLiabilityPartnership(testUtr, testCompanyNumber))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(LimitedLiabilityPartnership(testUtr, testCompanyNumber)),
        isDirectDebit = false
      ))
    }
    "store a Scottish Limited Partnership" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, ScottishLimitedPartnership(testUtr, testCompanyNumber))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(ScottishLimitedPartnership(testUtr, testCompanyNumber)),
        isDirectDebit = false
      ))
    }

    "store a VAT Group" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, VatGroup)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(VatGroup),
        isDirectDebit = false
      ))
    }

    "store a Registered Society" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, RegisteredSociety(testCompanyNumber))
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(RegisteredSociety(testCompanyNumber)),
        isDirectDebit = false
      ))
    }

    "store a Charity" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, Charity)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(Charity),
        isDirectDebit = false
      ))
    }

    "store an Overseas company" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertBusinessEntity(testVatNumber, Overseas)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) shouldBe Some(SubscriptionRequest(
        vatNumber = testVatNumber,
        isMigratable = true,
        businessEntity = Some(Overseas),
        isDirectDebit = false
      ))
    }
  }

  "upsertCtReference" should {
    "throw NoSuchElementException where the vat number doesn't exist" in {
      val res = for {
        _ <- repo.upsertCtReference(testVatNumber, testCtReference)
        model <- repo.findById(testVatNumber)
      } yield model

      intercept[NoSuchElementException] {
        await(res)
      }
    }

    "update the subscription request with CtReference" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertCtReference(testVatNumber, testCtReference)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(SubscriptionRequest(
        vatNumber = testVatNumber,
        ctReference = Some(testCtReference),
        isDirectDebit = false
      ))
    }
  }


  "upsertContactPreference" should {
    val testSubscriptionRequest = SubscriptionRequest(
      vatNumber = testVatNumber,
      contactPreference = Some(Digital),
      isDirectDebit = false
    )

    "throw NoSuchElementException where the vat number doesn't exist" in {
      val res = for {
        _ <- repo.upsertContactPreference(testVatNumber, Digital)
        model <- repo.findById(testVatNumber)
      } yield model

      intercept[NoSuchElementException] {
        await(res)
      }
    }

    "update the subscription request where there isn't already an contactPreference stored" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        _ <- repo.upsertContactPreference(testVatNumber, Digital)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(testSubscriptionRequest)
    }

    "replace an existing stored contactPreference" in {
      val res = for {
        _ <- repo.insert(testSubscriptionRequest)
        _ <- repo.upsertContactPreference(testVatNumber, Paper)
        model <- repo.findById(testVatNumber)
      } yield model

      await(res) should contain(SubscriptionRequest(testVatNumber, contactPreference = Some(Paper), isDirectDebit = false))
    }
  }

  "deleteRecord" should {
    "delete the entry stored against the vrn" in {
      val res = for {
        _ <- repo.upsertVatNumber(testVatNumber, isMigratable = true, isDirectDebit = false)
        inserted <- repo.findById(testVatNumber)
        _ <- repo.deleteRecord(testVatNumber)
        postDelete <- repo.findById(testVatNumber)
      } yield (inserted, postDelete)

      val (inserted, postDelete) = await(res)
      inserted should contain(SubscriptionRequest(testVatNumber, isDirectDebit = false))
      postDelete shouldBe None
    }
  }

}
