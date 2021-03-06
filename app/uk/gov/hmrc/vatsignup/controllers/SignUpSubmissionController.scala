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
import uk.gov.hmrc.auth.core.retrieve.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.controller.BaseController
import uk.gov.hmrc.vatsignup.services.SubmissionOrchestrationService
import uk.gov.hmrc.vatsignup.services.SubmissionOrchestrationService._

import scala.concurrent.ExecutionContext

@Singleton
class SignUpSubmissionController @Inject()(val authConnector: AuthConnector,
                                           submissionOrchestrationService: SubmissionOrchestrationService)
                                          (implicit ec: ExecutionContext)
  extends BaseController with AuthorisedFunctions {

  def submitSignUpRequest(vatNumber: String): Action[AnyContent] = Action.async {
    implicit request =>
      authorised().retrieve(Retrievals.allEnrolments) {
        enrolments =>
          submissionOrchestrationService.submitSignUpRequest(vatNumber, enrolments) map {
            case Right(SignUpRequestSubmitted) => NoContent
            case Left(InsufficientData) => BadRequest
            case _ => BadGateway
          }
      }
  }

}
