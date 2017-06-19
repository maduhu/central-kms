package org.leveloneproject.central.kms.persistance.postgres

import com.google.inject.Inject
import org.leveloneproject.central.kms.persistance.{DbProvider, HealthCheckRepository}

class PostgresHealthCheckRepository @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with HealthCheckRepository
