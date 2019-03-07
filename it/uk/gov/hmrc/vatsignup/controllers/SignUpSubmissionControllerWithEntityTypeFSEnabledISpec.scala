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

package uk.gov.hmrc.vatsignup.controllers

import play.api.http.Status._
import play.api.libs.json.Json
import uk.gov.hmrc.vatsignup.config.featureswitch.{EtmpEntityType, HybridSolution}
import uk.gov.hmrc.vatsignup.helpers.IntegrationTestConstants._
import uk.gov.hmrc.vatsignup.helpers.servicemocks.AuthStub._
import uk.gov.hmrc.vatsignup.helpers.servicemocks.EmailVerificationStub._
import uk.gov.hmrc.vatsignup.helpers.servicemocks.EntityTypeRegistrationStub._
import uk.gov.hmrc.vatsignup.helpers.servicemocks.SignUpStub._
import uk.gov.hmrc.vatsignup.helpers.servicemocks.TaxEnrolmentsStub._
import uk.gov.hmrc.vatsignup.helpers.{ComponentSpecBase, CustomMatchers, TestEmailRequestRepository, TestSubmissionRequestRepository}
import uk.gov.hmrc.vatsignup.models._

import scala.concurrent.ExecutionContext.Implicits.global

class SignUpSubmissionControllerWithEntityTypeFSEnabledISpec extends ComponentSpecBase with CustomMatchers
  with TestSubmissionRequestRepository with TestEmailRequestRepository {

  val testIsMigratable = true

  override def beforeEach: Unit = {
    super.beforeEach
    enable(HybridSolution)
    enable(EtmpEntityType)
  }

  "/subscription-request/vat-number/:vatNumber/submit" when {
    "the user is a delegate and" when {
      "all downstream services behave as expected" should {
        "return NO_CONTENT for individual sign up" in {
          val testSubscriptionRequest = SubscriptionRequest(
            vatNumber = testVatNumber,
            businessEntity = Some(SoleTrader(testNino)),
            ninoSource = Some(UserEntered),
            email = Some(testEmail),
            isMigratable = testIsMigratable,
            isDirectDebit = false
          )

          stubAuth(OK, successfulAuthResponse(agentEnrolment))
          stubGetEmailVerified(testEmail)
          stubRegisterBusinessEntity(testVatNumber, SoleTrader(testNino))(testSafeId)
          stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
          stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

          await(submissionRequestRepo.insert(testSubscriptionRequest))
          val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

          res should have(
            httpStatus(NO_CONTENT),
            emptyBody
          )
        }

        "return NO_CONTENT for company sign up" in {
          val testSubscriptionRequest = SubscriptionRequest(
            vatNumber = testVatNumber,
            businessEntity = Some(LimitedCompany(testCompanyNumber)),
            email = Some(testEmail),
            isMigratable = testIsMigratable,
            isDirectDebit = false
          )

          stubAuth(OK, successfulAuthResponse(agentEnrolment))
          stubGetEmailVerified(testEmail)
          stubRegisterBusinessEntity(testVatNumber, LimitedCompany(testCompanyNumber))(testSafeId)
          stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
          stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

          await(submissionRequestRepo.insert(testSubscriptionRequest))
          val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

          res should have(
            httpStatus(NO_CONTENT),
            emptyBody
          )
        }

        "transaction email should not be sent to sign up" in {
          val testSubscriptionRequest = SubscriptionRequest(
            vatNumber = testVatNumber,
            businessEntity = Some(LimitedCompany(testCompanyNumber)),
            transactionEmail = Some(testEmail),
            isMigratable = testIsMigratable,
            isDirectDebit = false
          )

          stubAuth(OK, successfulAuthResponse(agentEnrolment))
          stubGetEmailVerified(testEmail)
          stubRegisterBusinessEntity(testVatNumber, LimitedCompany(testCompanyNumber))(testSafeId)
          stubSignUp(testSafeId, testVatNumber, None, None, optIsPartialMigration = Some(!testIsMigratable))(OK)
          stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

          await(submissionRequestRepo.insert(testSubscriptionRequest))
          val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

          res should have(
            httpStatus(NO_CONTENT),
            emptyBody
          )
        }
      }
    }

    "the user is principal and" when {
      "all downstream services behave as expected" should {
        "return NO_CONTENT for individual sign up" in {
          val testSubscriptionRequest = SubscriptionRequest(
            vatNumber = testVatNumber,
            businessEntity = Some(SoleTrader(testNino)),
            ninoSource = Some(UserEntered),
            email = Some(testEmail),
            identityVerified = true,
            isMigratable = testIsMigratable,
            isDirectDebit = false
          )

          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubGetEmailVerified(testEmail)
          stubRegisterBusinessEntity(testVatNumber, SoleTrader(testNino))(testSafeId)
          stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
          stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

          await(submissionRequestRepo.insert(testSubscriptionRequest))
          val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

          res should have(
            httpStatus(NO_CONTENT),
            emptyBody
          )
        }

        "return NO_CONTENT for company sign up" in {
          val testSubscriptionRequest = SubscriptionRequest(
            vatNumber = testVatNumber,
            businessEntity = Some(LimitedCompany(testCompanyNumber)),
            ctReference = Some(testCtReference),
            email = Some(testEmail),
            isMigratable = testIsMigratable,
            isDirectDebit = false
          )

          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubGetEmailVerified(testEmail)
          stubRegisterBusinessEntity(testVatNumber, LimitedCompany(testCompanyNumber))(testSafeId)
          stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
          stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

          await(submissionRequestRepo.insert(testSubscriptionRequest))
          val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

          res should have(
            httpStatus(NO_CONTENT),
            emptyBody
          )
        }

        "return NO_CONTENT for general partnership sign up" in {
          val testSubscriptionRequest = SubscriptionRequest(
            vatNumber = testVatNumber,
            businessEntity = Some(GeneralPartnership(testUtr)),
            email = Some(testEmail),
            identityVerified = true,
            isMigratable = testIsMigratable,
            isDirectDebit = false
          )

          stubAuth(OK, successfulAuthResponse(partnershipEnrolment))
          stubGetEmailVerified(testEmail)
          stubRegisterBusinessEntity(testVatNumber, GeneralPartnership(testUtr))(testSafeId)
          stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
          stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

          await(submissionRequestRepo.insert(testSubscriptionRequest))
          val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

          res should have(
            httpStatus(NO_CONTENT),
            emptyBody
          )
        }
      }
      "the user is signing up a limited company and has provided a CT reference" should {
        "return NO_CONTENT for company sign up" in {
          val testSubscriptionRequest = SubscriptionRequest(
            vatNumber = testVatNumber,
            businessEntity = Some(LimitedCompany(testCompanyNumber)),
            ctReference = Some(testCtReference),
            email = Some(testEmail),
            isMigratable = testIsMigratable,
            isDirectDebit = false
          )

          stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
          stubGetEmailVerified(testEmail)
          stubRegisterBusinessEntity(testVatNumber, LimitedCompany(testCompanyNumber))(testSafeId)
          stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
          stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

          await(submissionRequestRepo.insert(testSubscriptionRequest))
          val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

          res should have(
            httpStatus(NO_CONTENT),
            emptyBody
          )
        }
      }
      "return NO_CONTENT for VAT group sign up" in {
        val testSubscriptionRequest = SubscriptionRequest(
          vatNumber = testVatNumber,
          businessEntity = Some(VatGroup),
          email = Some(testEmail),
          identityVerified = true,
          isMigratable = testIsMigratable,
          isDirectDebit = false
        )

        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubGetEmailVerified(testEmail)
        stubRegisterBusinessEntity(testVatNumber, VatGroup)(testSafeId)
        stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
        stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

        await(submissionRequestRepo.insert(testSubscriptionRequest))
        val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

        res should have(
          httpStatus(NO_CONTENT),
          emptyBody
        )
      }
      "return NO_CONTENT for Division sign up" in {
        val testSubscriptionRequest = SubscriptionRequest(
          vatNumber = testVatNumber,
          businessEntity = Some(AdministrativeDivision),
          email = Some(testEmail),
          identityVerified = true,
          isMigratable = testIsMigratable,
          isDirectDebit = false
        )

        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubGetEmailVerified(testEmail)
        stubRegisterBusinessEntity(testVatNumber, AdministrativeDivision)(testSafeId)
        stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
        stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

        await(submissionRequestRepo.insert(testSubscriptionRequest))
        val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

        res should have(
          httpStatus(NO_CONTENT),
          emptyBody
        )
      }
      "return NO_CONTENT for Trust sign up" in {
        val testSubscriptionRequest = SubscriptionRequest(
          vatNumber = testVatNumber,
          businessEntity = Some(Trust),
          email = Some(testEmail),
          identityVerified = true,
          isMigratable = testIsMigratable,
          isDirectDebit = false
        )

        stubAuth(OK, successfulAuthResponse(vatDecEnrolment))
        stubGetEmailVerified(testEmail)
        stubRegisterBusinessEntity(testVatNumber, Trust)(testSafeId)
        stubSignUp(testSafeId, testVatNumber, Some(testEmail), emailVerified = Some(true), optIsPartialMigration = Some(!testIsMigratable))(OK)
        stubRegisterEnrolment(testVatNumber, testSafeId)(NO_CONTENT)

        await(submissionRequestRepo.insert(testSubscriptionRequest))
        val res = await(post(s"/subscription-request/vat-number/$testVatNumber/submit")(Json.obj()))

        res should have(
          httpStatus(NO_CONTENT),
          emptyBody
        )
      }
    }
  }
}
