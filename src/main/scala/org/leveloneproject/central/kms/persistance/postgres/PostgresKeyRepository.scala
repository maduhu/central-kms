package org.leveloneproject.central.kms.persistance.postgres

import com.google.inject.Inject
import org.leveloneproject.central.kms.persistance.{DbProvider, KeyRepository}

class PostgresKeyRepository @Inject()(val dbProvider: DbProvider) extends KeyRepository with PostgresDbProfile
