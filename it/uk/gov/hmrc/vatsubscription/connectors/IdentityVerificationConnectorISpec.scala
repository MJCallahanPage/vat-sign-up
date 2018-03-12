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

package uk.gov.hmrc.vatsubscription.connectors

import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.vatsubscription.helpers.ComponentSpecBase
import uk.gov.hmrc.vatsubscription.helpers.IntegrationTestConstants.testToken
import uk.gov.hmrc.vatsubscription.helpers.servicemocks.IdentityVerificationStub._
import uk.gov.hmrc.vatsubscription.httpparsers.IdentityVerified

class IdentityVerificationConnectorISpec extends ComponentSpecBase {

  lazy val connector: IdentityVerificationConnector = app.injector.instanceOf[IdentityVerificationConnector]

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  "getIdentityVerificationOutcome" should {
    "return whether the users identity has been verified" in {
      stubGetIdentityVerifiedOutcome(testToken)("Success")

      val res = connector.getIdentityVerificationOutcome(testToken)

      await(res) shouldBe Right(IdentityVerified)
    }
  }

}
