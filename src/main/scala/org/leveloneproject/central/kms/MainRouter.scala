package org.leveloneproject.central.kms

import com.google.inject.Inject
import org.leveloneproject.central.kms.domain.keys.KeyRouter

class MainRouter @Inject()(keyRouter: KeyRouter) {

  val route = keyRouter.route
}
