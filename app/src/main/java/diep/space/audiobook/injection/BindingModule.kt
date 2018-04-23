package diep.space.audiobook.injection

import dagger.Module
import dagger.android.ContributesAndroidInjector
import diep.space.audiobook.features.BaseActivity
import diep.space.audiobook.features.MainActivity
import diep.space.audiobook.features.bookOverview.EditBookBottomSheet
import diep.space.audiobook.features.bookOverview.EditBookTitleDialogFragment
import diep.space.audiobook.features.bookOverview.EditCoverDialogFragment
import diep.space.audiobook.features.bookPlaying.JumpToPositionDialogFragment
import diep.space.audiobook.features.bookPlaying.SeekDialogFragment
import diep.space.audiobook.features.bookPlaying.SleepTimerDialogFragment
import diep.space.audiobook.features.folderChooser.FolderChooserActivity
import diep.space.audiobook.features.settings.dialogs.AutoRewindDialogFragment
import diep.space.audiobook.features.settings.dialogs.PlaybackSpeedDialogFragment
import diep.space.audiobook.features.settings.dialogs.ThemePickerDialogFragment
import diep.space.audiobook.features.widget.BaseWidgetProvider
import diep.space.audiobook.playback.PlaybackService

/**
 * Module for dagger bindings
 */
@Module
abstract class BindingModule {

  @ContributesAndroidInjector
  abstract fun mainActivity(): MainActivity

  @ContributesAndroidInjector(modules = arrayOf(PlaybackModule::class))
  @PerService
  abstract fun playbackService(): PlaybackService

  @ContributesAndroidInjector
  abstract fun autoRewindDialogFragment(): AutoRewindDialogFragment

  @ContributesAndroidInjector
  abstract fun editCoverDialogFragment(): EditCoverDialogFragment

  @ContributesAndroidInjector
  abstract fun editBookTitleDialogFragment(): EditBookTitleDialogFragment

  @ContributesAndroidInjector
  abstract fun folderChooserActivity(): FolderChooserActivity

  @ContributesAndroidInjector
  abstract fun jumpToPositionDialogFragment(): JumpToPositionDialogFragment

  @ContributesAndroidInjector
  abstract fun playbackSpeedDialogFragment(): PlaybackSpeedDialogFragment

  @ContributesAndroidInjector
  abstract fun seekDialogFragment(): SeekDialogFragment

  @ContributesAndroidInjector
  abstract fun sleepTimerDialogFragment(): SleepTimerDialogFragment

  @ContributesAndroidInjector
  abstract fun themePickerDialogFragment(): ThemePickerDialogFragment

  @ContributesAndroidInjector
  abstract fun baseWidgetProvider(): BaseWidgetProvider

  @ContributesAndroidInjector
  abstract fun editBookBottomSheet(): EditBookBottomSheet

  @ContributesAndroidInjector
  abstract fun baseActivity(): BaseActivity
}
