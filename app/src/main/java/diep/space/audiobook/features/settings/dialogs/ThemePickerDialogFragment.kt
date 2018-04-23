package diep.space.audiobook.features.settings.dialogs

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.DialogFragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import com.afollestad.materialdialogs.MaterialDialog
import dagger.android.support.AndroidSupportInjection
import diep.space.audiobook.R
import diep.space.audiobook.injection.PrefKeys
import diep.space.audiobook.persistence.pref.Pref
import diep.space.audiobook.uitools.ThemeUtil
import javax.inject.Inject
import javax.inject.Named

/**
 * Dialog for picking the UI theme.
 */
class ThemePickerDialogFragment : DialogFragment() {

  @field:[Inject Named(PrefKeys.THEME)]
  lateinit var themePref: Pref<ThemeUtil.Theme>

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    AndroidSupportInjection.inject(this)

    val oldTheme = themePref.value
    val existingThemes = ThemeUtil.Theme.values()
    val names = existingThemes.map { getString(it.nameId) }

    return MaterialDialog.Builder(context!!)
      .items(*names.toTypedArray())
      .itemsCallbackSingleChoice(existingThemes.indexOf(oldTheme)) { _, _, i, _ ->
        val newTheme = existingThemes[i]
        themePref.value = newTheme
        AppCompatDelegate.setDefaultNightMode(newTheme.nightMode)

        // use post so the dialog can close correctly
        Handler().post {
          (activity as AppCompatActivity).delegate.applyDayNight()
        }
        true
      }
      .positiveText(R.string.dialog_confirm)
      .negativeText(R.string.dialog_cancel)
      .title(R.string.pref_theme_title)
      .build()
  }

  companion object {
    val TAG: String = ThemePickerDialogFragment::class.java.simpleName
  }
}
