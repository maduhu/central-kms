package org.leveloneproject.central.kms.domain

case class Error(code: Int, message: String, data: Option[Any] = None)
