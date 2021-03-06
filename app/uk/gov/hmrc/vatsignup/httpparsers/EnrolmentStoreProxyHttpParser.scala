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

package uk.gov.hmrc.vatsignup.httpparsers

import play.api.http.Status._
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object EnrolmentStoreProxyHttpParser {
  type EnrolmentStoreProxyResponse = Either[EnrolmentStoreProxyFailure, EnrolmentStoreProxySuccess]

  implicit object EnrolmentStoreProxyHttpReads extends HttpReads[EnrolmentStoreProxyResponse] {
    override def read(method: String, url: String, response: HttpResponse): EnrolmentStoreProxyResponse =
      response.status match {
        case OK => Right(EnrolmentAlreadyAllocated)
        case NO_CONTENT => Right(EnrolmentNotAllocated)
        case status => Left(EnrolmentStoreProxyFailure(status))
      }
  }

  sealed trait EnrolmentStoreProxySuccess

  case object EnrolmentAlreadyAllocated extends EnrolmentStoreProxySuccess

  case object EnrolmentNotAllocated extends EnrolmentStoreProxySuccess

  case class EnrolmentStoreProxyFailure(status: Int)

}
