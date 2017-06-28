package org.leveloneproject.central.kms.persistance.postgres

import com.google.inject.Inject
import org.leveloneproject.central.kms.persistance.{DbProvider, SidecarLogsRepository}


class PostgresSidecarLogsRepository @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with SidecarLogsRepository
