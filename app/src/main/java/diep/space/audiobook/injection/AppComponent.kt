package diep.space.audiobook.injection

import android.app.Application
import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import diep.space.audiobook.data.repo.internals.PersistenceModule
import diep.space.audiobook.features.audio.LoudnessDialog
import diep.space.audiobook.features.bookOverview.BookShelfController
import diep.space.audiobook.features.bookOverview.BookShelfPresenter
import diep.space.audiobook.features.bookOverview.list.BookShelfHolder
import diep.space.audiobook.features.bookPlaying.BookPlayController
import diep.space.audiobook.features.bookPlaying.BookPlayPresenter
import diep.space.audiobook.features.bookmarks.BookmarkPresenter
import diep.space.audiobook.features.folderChooser.FolderChooserPresenter
import diep.space.audiobook.features.folderOverview.FolderOverviewPresenter
import diep.space.audiobook.features.imagepicker.ImagePickerController
import diep.space.audiobook.features.settings.SettingsController
import diep.space.audiobook.playback.MediaPlayer
import diep.space.audiobook.playback.PlayStateManager
import javax.inject.Singleton

/**
 * Base component that is the entry point for injection.
 */
@Singleton
@Component(
  modules = [
    AndroidModule::class,
    PrefsModule::class,
    BindingModule::class,
    AndroidSupportInjectionModule::class,
    PersistenceModule::class
  ]
)
interface AppComponent {

  val bookmarkPresenter: BookmarkPresenter
  val bookShelfPresenter: BookShelfPresenter
  val context: Context
  val player: MediaPlayer
  val playStateManager: PlayStateManager

  @Component.Builder
  interface Builder {

    @BindsInstance
    fun application(application: Application): Builder

    fun build(): AppComponent
  }

  fun inject(target: App)
  fun inject(target: BookPlayController)
  fun inject(target: BookPlayPresenter)
  fun inject(target: BookShelfHolder)
  fun inject(target: BookShelfController)
  fun inject(target: FolderChooserPresenter)
  fun inject(target: FolderOverviewPresenter)
  fun inject(target: ImagePickerController)
  fun inject(target: LoudnessDialog)
  fun inject(target: SettingsController)
}
