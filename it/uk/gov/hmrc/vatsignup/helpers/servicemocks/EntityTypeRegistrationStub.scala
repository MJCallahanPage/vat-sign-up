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

package uk.gov.hmrc.vatsignup.helpers.servicemocks

import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.vatsignup.models.{BusinessEntity, GeneralPartnership, LimitedCompany, SoleTrader}

object EntityTypeRegistrationStub extends WireMockMethods {
  private val registerUri = "/cross-regime/register/VATC"

  private def registerCompanyJsonBody(vatNumber: String, companyNumber: String): JsObject =
    Json.obj(
      "company" -> Json.obj(
        "vrn" -> vatNumber,
        "crn" -> companyNumber
      )
    )

  private def registerIndividualJsonBody(vatNumber: String, nino: String): JsObject =
    Json.obj(
      "soleTrader" -> Json.obj(
        "vrn" -> vatNumber,
        "nino" -> nino
      )
    )

  private def registerGeneralPartnershipJsonBody(vatNumber: String, sautr: String): JsObject =
    Json.obj(
      "ordinaryPartnership" -> Json.obj(
        "vrn" -> vatNumber,
        "sautr" -> sautr
      )
    )

  private def registerResponseBody(safeId: String): JsObject =
    Json.obj(
      "identification" -> Json.arr(
        Json.obj(
          "idType" -> "SAFEID",
          "idValue" -> safeId
        )
      )
    )

  private val desHeaders = Map(
    "Authorization" -> "Bearer dev",
    "Content-Type" -> "application/json",
    "Environment" -> "dev"
  )

  def stubRegisterBusinessEntity(vatNumber: String, businessEntity: BusinessEntity)(safeId: String): Unit = {
    when(
      method = POST,
      uri = registerUri,
      headers = desHeaders,
      body = businessEntity match {
        case SoleTrader(nino) =>
          registerIndividualJsonBody(vatNumber, nino)
        case LimitedCompany(companyNumber) =>
          registerCompanyJsonBody(vatNumber, companyNumber)
        case GeneralPartnership(sautr) =>
          registerGeneralPartnershipJsonBody(vatNumber, sautr)
      }
    ) thenReturn(OK, registerResponseBody(safeId))
  }

}