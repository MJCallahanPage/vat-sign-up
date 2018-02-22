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

package uk.gov.hmrc.vatsubscription.services

import java.util.NoSuchElementException
import javax.inject.Inject

import uk.gov.hmrc.vatsubscription.repositories.SubscriptionRequestRepository

import scala.concurrent.{ExecutionContext, Future}

class StoreEmailService @Inject()(subscriptionRequestRepository: SubscriptionRequestRepository
                                 )(implicit ec: ExecutionContext) {
  def storeEmail(vatNumber: String, email: String): Future[Either[StoreEmailFailure, StoreEmailSuccess.type]] =
    subscriptionRequestRepository.upsertEmail(vatNumber, email) map {
      _ => Right(StoreEmailSuccess)
    } recover {
      case e: NoSuchElementException => Left(EmailDatabaseFailureNoVATNumber)
      case _ => Left(EmailDatabaseFailure)
    }
}

object StoreEmailSuccess

sealed trait StoreEmailFailure

object EmailDatabaseFailure extends StoreEmailFailure

object EmailDatabaseFailureNoVATNumber extends StoreEmailFailure

