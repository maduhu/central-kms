package org.leveloneproject.central.kms.persistance.postgres

import com.google.inject.Inject
import org.leveloneproject.central.kms.persistance.{BatchRepository, DbProvider}

class PostgresBatchRepository @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with BatchRepository
