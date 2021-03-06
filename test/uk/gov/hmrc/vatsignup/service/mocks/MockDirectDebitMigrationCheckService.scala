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

package uk.gov.hmrc.vatsignup.service.mocks

import org.mockito.ArgumentMatchers
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import uk.gov.hmrc.vatsignup.models.controllist.Stagger
import uk.gov.hmrc.vatsignup.services.DirectDebitMigrationCheckService
import uk.gov.hmrc.vatsignup.services.DirectDebitMigrationCheckService.DirectDebitMigrationEligibility

trait MockDirectDebitMigrationCheckService extends MockitoSugar {
  val mockDirectDebitMigrationCheckService: DirectDebitMigrationCheckService = mock[DirectDebitMigrationCheckService]

  def mockCheckMigrationDate(stagger: Stagger)(result: DirectDebitMigrationEligibility): Unit =
    when(mockDirectDebitMigrationCheckService.checkMigrationDate(ArgumentMatchers.eq(stagger)))
    .thenReturn(result)

}
