package org.leveloneproject.central.kms.persistance.postgres

import com.google.inject.Inject
import org.leveloneproject.central.kms.persistance.{DbProvider, SidecarRepository}

class PostgresSidecarRepository @Inject()(val dbProvider: DbProvider) extends PostgresDbProfile with SidecarRepository

