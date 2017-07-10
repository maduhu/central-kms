package org.leveloneproject.central.kms.domain.inquiries

import java.time.Instant

case class CreateInquiryRequest(service: String, startTime: Instant, endTime: Instant)
