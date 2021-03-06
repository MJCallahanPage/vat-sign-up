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
import play.api.mvc.{Action, AnyContent}
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.vatsignup.services.StoreTrustService
import uk.gov.hmrc.vatsignup.services.StoreTrustService.TrustDatabaseFailureNoVATNumber

import scala.concurrent.ExecutionContext

@Singleton
class StoreTrustController @Inject()(val authConnector: AuthConnector,
                                     storeTrustService: StoreTrustService
                                    )(implicit ec: ExecutionContext) extends BaseController with AuthorisedFunctions {

  def storeTrust(vatNumber: String): Action[AnyContent] = Action.async {
    implicit req =>
      authorised() {
        storeTrustService.storeTrust(vatNumber) map {
          case Right(_) => NoContent
          case Left(TrustDatabaseFailureNoVATNumber) => NotFound
          case Left(_) => InternalServerError
        }
      }
  }

}
