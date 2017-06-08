package org.leveloneproject.central.kms.domain

case class ErrorWithCommandId(error: Error, commandId: String)
