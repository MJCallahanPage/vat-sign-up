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

package uk.gov.hmrc.vatsubscription.helpers.servicemocks

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.libs.json.{JsValue, Json}
import play.api.http.Status.{OK, NOT_FOUND, BAD_REQUEST}

import uk.gov.hmrc.vatsubscription.helpers.IntegrationTestConstants._

object KnownFactsAndControlListInformationStub extends WireMockMethods {

  private def stubGetKnownFactsAndControlListInformation(vatNumber: String)(status: Int, body: Option[JsValue]): StubMapping =
    when(method = GET, uri = s"/vat/known-facts/control-list/$vatNumber",
      headers = Map(
        "Authorization" -> "Bearer dev",
        "Environment" -> "dev"
      )
    ).thenReturn(status = status, body = body)


  def stubSuccessGetKnownFactsAndControlListInformation(vatNumber: String): StubMapping =
    stubGetKnownFactsAndControlListInformation(vatNumber)(OK, Some(successResponseBody))

  def stubFailureControlListVatNumberNotFound(vatNumber: String): StubMapping =
    stubGetKnownFactsAndControlListInformation(vatNumber)(NOT_FOUND, None)

  def stubFailureKnownFactsInvalidVatNumber(vatNumber: String): StubMapping =
    stubGetKnownFactsAndControlListInformation(vatNumber)(BAD_REQUEST, None)


  private def successResponseBody =
    Json.obj(
      "postcode" -> testPostCode,
      "dateOfReg" -> testDateOfRegistration,
      "controlListInformation" -> testControlListInformation
    )



}