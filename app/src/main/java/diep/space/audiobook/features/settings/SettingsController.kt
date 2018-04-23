package diep.space.audiobook.features.settings

import android.support.annotation.StringRes
import diep.space.audiobook.R
import diep.space.audiobook.features.BaseController
import diep.space.audiobook.features.bookPlaying.SeekDialogFragment
import diep.space.audiobook.features.settings.dialogs.AutoRewindDialogFragment
import diep.space.audiobook.features.settings.dialogs.SupportDialogFragment
import diep.space.audiobook.features.settings.dialogs.ThemePickerDialogFragment
import diep.space.audiobook.injection.App
import diep.space.audiobook.injection.PrefKeys
import diep.space.audiobook.persistence.pref.Pref
import diep.space.audiobook.uitools.ThemeUtil
import kotlinx.android.synthetic.main.settings.*
import javax.inject.Inject
import javax.inject.Named

/**
 * Controller for the user settings
 */
class SettingsController : BaseController() {

  @field:[Inject Named(PrefKeys.THEME)]
  lateinit var themePref: Pref<ThemeUtil.Theme>
  @field:[Inject Named(PrefKeys.RESUME_ON_REPLUG)]
  lateinit var resumeOnReplugPref: Pref<Boolean>
  @field:[Inject Named(PrefKeys.RESUME_AFTER_CALL)]
  lateinit var resumeAfterCallPref: Pref<Boolean>
  @field:[Inject Named(PrefKeys.AUTO_REWIND_AMOUNT)]
  lateinit var autoRewindAmountPref: Pref<Int>
  @field:[Inject Named(PrefKeys.SEEK_TIME)]
  lateinit var seekTimePref: Pref<Int>

  init {
    App.component.inject(this)
  }

  override val layoutRes = R.layout.settings

  override fun onViewCreated() {
    setupToolbar()

    // theme
    setupTextSetting(
      doubleSettingView = theme,
      titleRes = R.string.pref_theme_title
    ) {
      ThemePickerDialogFragment().show(fragmentManager, ThemePickerDialogFragment.TAG)
    }
    themePref.stream
      .subscribe { theme.setDescription(it.nameId) }
      .disposeOnDestroyView()

    // resume on playback
    setupSwitchSetting(
      settingView = resumePlayback,
      titleRes = R.string.pref_resume_on_replug,
      contentRes = R.string.pref_resume_on_replug_hint,
      pref = resumeOnReplugPref
    )

    // resume on playback
    setupSwitchSetting(
      settingView = resumeAfterCall,
      titleRes = R.string.pref_resume_after_call,
      contentRes = R.string.pref_resume_after_call_hint,
      pref = resumeAfterCallPref
    )

    // skip amount
    setupTextSetting(
      doubleSettingView = skipAmount,
      titleRes = R.string.pref_seek_time
    ) {
      SeekDialogFragment().show(fragmentManager, SeekDialogFragment.TAG)
    }
    seekTimePref.stream
      .map { resources!!.getQuantityString(R.plurals.seconds, it, it) }
      .subscribe { skipAmount.setDescription(it) }
      .disposeOnDestroyView()

    // auto rewind
    setupTextSetting(
      doubleSettingView = autoRewind,
      titleRes = R.string.pref_auto_rewind_title
    ) {
      AutoRewindDialogFragment().show(fragmentManager, AutoRewindDialogFragment.TAG)
    }
    autoRewindAmountPref.stream
      .map { resources!!.getQuantityString(R.plurals.seconds, it, it) }
      .subscribe { autoRewind.setDescription(it) }
      .disposeOnDestroyView()
  }

  private fun setupToolbar() {
    toolbar.setNavigationIcon(R.drawable.close)
    toolbar.inflateMenu(R.menu.menu_settings)
    toolbar.setOnMenuItemClickListener {
      if (it.itemId == R.id.action_contribute) {
        SupportDialogFragment().show(fragmentManager, SupportDialogFragment.TAG)
        true
      } else
        false
    }
    toolbar.setNavigationOnClickListener {
      activity.onBackPressed()
    }
    toolbar.title = getString(R.string.action_settings)
  }

  private inline fun setupTextSetting(
    doubleSettingView: DoubleSettingView,
    @StringRes titleRes: Int,
    @StringRes contentRes: Int? = null,
    crossinline onClick: () -> Unit
  ) {
    doubleSettingView.setTitle(titleRes)
    if (contentRes != null) doubleSettingView.setDescription(contentRes)
    doubleSettingView.setOnClickListener {
      onClick()
    }
  }

  private fun setupSwitchSetting(
    settingView: SwitchSettingView,
    @StringRes titleRes: Int,
    @StringRes contentRes: Int,
    pref: Pref<Boolean>
  ) {
    settingView.setTitle(titleRes)
    settingView.setDescription(contentRes)

    settingView.onCheckedChanged {
      pref.value = it
    }
    pref.stream
      .subscribe { settingView.setChecked(it) }
      .disposeOnDestroyView()

    settingView.setOnClickListener { settingView.toggle() }
  }
}
