package modules

import com.google.inject.AbstractModule
import com.google.inject.name.Names
import components.storage.Storage
import components.util.Chances

class ApplicationModule extends AbstractModule {
  def configure() = {
    // sessionStorage
    bind(classOf[Storage.Settings])
      .annotatedWith(Names.named("sessionStorageSettings"))
      .toInstance(Storage.Settings(gcMaxLifetime = Some(1440L), gcChance = Chances.random(1, 100)))
    bind(classOf[Storage])
      .annotatedWith(Names.named("sessionStorage"))
      .toProvider(classOf[SessionStorageProvider])
      .asEagerSingleton()
  }
}
