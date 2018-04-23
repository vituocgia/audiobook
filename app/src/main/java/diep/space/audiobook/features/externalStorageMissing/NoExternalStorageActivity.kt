package diep.space.audiobook.features.externalStorageMissing

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import diep.space.audiobook.R
import diep.space.audiobook.features.BaseActivity
import kotlinx.android.synthetic.main.activity_no_external.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

class NoExternalStorageActivity : AppCompatActivity() {

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_no_external)
    toolbar.setTitle(R.string.no_external_storage_action_bar_title)
  }

  override fun onBackPressed() {
    runBlocking {
      if (BaseActivity.storageMounted()) {
        super.onBackPressed()
      } else {
        val i = Intent(Intent.ACTION_MAIN)
        i.addCategory(Intent.CATEGORY_HOME)
        i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(i)
      }
    }
  }

  public override fun onResume() {
    super.onResume()
    launch(UI) {
      if (BaseActivity.storageMounted()) {
        onBackPressed()
      }
    }
  }
}
