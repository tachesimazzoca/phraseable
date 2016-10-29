package modules

import components.storage.Storage
import org.scalatest.FunSuite
import play.api.Mode
import play.api.inject.BindingKey
import play.api.inject.guice.GuiceApplicationBuilder

class ApplicationModuleSuite extends FunSuite {
  test("configure") {
    val injector = new GuiceApplicationBuilder()
      .in(Mode.Test)
      .build()
      .injector

    val sessionStorage = injector.instanceOf(BindingKey(classOf[Storage])
      .qualifiedWith("sessionStorage"))
  }
}
