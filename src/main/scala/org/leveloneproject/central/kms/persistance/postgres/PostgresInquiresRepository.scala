package org.leveloneproject.central.kms.persistance.postgres

import com.google.inject.Inject
import org.leveloneproject.central.kms.persistance.{DbProvider, InquiriesRepository}

class PostgresInquiresRepository @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with InquiriesRepository
