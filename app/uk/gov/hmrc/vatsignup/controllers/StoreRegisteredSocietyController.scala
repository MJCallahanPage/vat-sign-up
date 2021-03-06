/*
 * Copyright 2019 HM Revenue & Customs
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

import javax.inject.{Inject, Singleton}
import play.api.libs.json.{JsResult, JsValue, Json, Reads}
import play.api.mvc.Action
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.vatsignup.config.Constants._
import uk.gov.hmrc.vatsignup.controllers.StoreRegisteredSocietyController._
import uk.gov.hmrc.vatsignup.models.SubscriptionRequest._
import uk.gov.hmrc.vatsignup.services.StoreRegisteredSocietyService
import uk.gov.hmrc.vatsignup.services.StoreRegisteredSocietyService._

import scala.concurrent.ExecutionContext

@Singleton
class StoreRegisteredSocietyController @Inject()(val authConnector: AuthConnector,
                                                 storeRegisteredSocietyService: StoreRegisteredSocietyService
                                                )(implicit ec: ExecutionContext) extends BaseController with AuthorisedFunctions {

  def storeRegisteredSociety(vatNumber: String): Action[(String, Option[String])] =
    Action.async(parse.json(StoreRegisteredSocietyReads)) { implicit req =>
      authorised() {
        req.body match {
          case (companyNumber, ctReferenceOpt) =>
            storeRegisteredSocietyService.storeRegisteredSociety(vatNumber, companyNumber, ctReferenceOpt) map {
              case Right(_) => NoContent
              case Left(DatabaseFailureNoVATNumber) => NotFound
              case Left(CtReferenceMismatch) => BadRequest(Json.obj(HttpCodeKey -> CtReferenceMismatchCode))
              case Left(MatchCtReferenceFailure) => BadGateway
              case Left(_) => InternalServerError
            }
        }

      }
    }

}

object StoreRegisteredSocietyController {
  val CtReferenceMismatchCode = "CtReferenceMismatch"

  object StoreRegisteredSocietyReads extends Reads[(String, Option[String])] {
    override def reads(json: JsValue): JsResult[(String, Option[String])] = for {
      companyNumber <- (json \ companyNumberKey).validate[String]
      ctReference <- (json \ ctReferenceKey).validateOpt[String]
    } yield (companyNumber, ctReference)
  }
}
