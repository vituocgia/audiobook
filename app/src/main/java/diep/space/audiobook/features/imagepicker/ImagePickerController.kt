package diep.space.audiobook.features.imagepicker

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.view.isVisible
import com.afollestad.materialcab.MaterialCab
import com.squareup.picasso.Picasso
import diep.space.audiobook.R
import diep.space.audiobook.data.Book
import diep.space.audiobook.data.repo.BookRepository
import diep.space.audiobook.features.BaseController
import diep.space.audiobook.injection.App
import diep.space.audiobook.misc.conductor.popOrBack
import diep.space.audiobook.misc.coverFile
import diep.space.audiobook.uitools.ImageHelper
import io.reactivex.subjects.BehaviorSubject
import kotlinx.android.synthetic.main.image_picker.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.net.URLEncoder
import javax.inject.Inject

/**
 * Hosts the image picker.
 */
class ImagePickerController(bundle: Bundle) : BaseController(bundle) {

  constructor(book: Book) : this(
    Bundle().apply {
      putLong(NI_BOOK_ID, book.id)
    }
  )

  init {
    App.component.inject(this)
  }

  @Inject
  lateinit var repo: BookRepository
  @Inject
  lateinit var imageHelper: ImageHelper

  private var cab: MaterialCab? = null

  private val cabCallback = object : MaterialCab.Callback {

    override fun onCabFinished(p0: MaterialCab?): Boolean {
      cropOverlay.selectionOn = false
      fab.show()
      return true
    }

    override fun onCabItemClicked(item: MenuItem): Boolean {
      if (item.itemId == R.id.confirm) {
        // obtain screenshot
        val cropRect = cropOverlay.selectedRect
        cropOverlay.selectionOn = false

        webViewContainer.isDrawingCacheEnabled = true
        webViewContainer.buildDrawingCache()
        val cache: Bitmap = webViewContainer.drawingCache
        val screenShot = Bitmap.createBitmap(
          cache,
          cropRect.left,
          cropRect.top,
          cropRect.width(),
          cropRect.height()
        )
        webViewContainer.isDrawingCacheEnabled = false
        cache.recycle()

        // save screenshot
        launch(UI) {
          val coverFile = book.coverFile()
          imageHelper.saveCover(screenShot, coverFile)
          screenShot.recycle()
          Picasso.with(activity).invalidate(coverFile)
          cab?.finish()
          router.popCurrentController()
        }
        return true
      }
      return false
    }

    override fun onCabCreated(p0: MaterialCab?, p1: Menu?): Boolean {
      return true
    }
  }

  private var webViewIsLoading = BehaviorSubject.createDefault(false)
  private val book by lazy {
    val id = bundle.getLong(NI_BOOK_ID)
    repo.bookById(id)!!
  }
  private val originalUrl by lazy {
    val encodedSearch = URLEncoder.encode("${book.name} cover", Charsets.UTF_8.name())
    "https://www.google.com/search?safe=on&site=imghp&tbm=isch&tbs=isz:lt,islt:qsvga&q=$encodedSearch"
  }

  override val layoutRes = R.layout.image_picker

  @SuppressLint("SetJavaScriptEnabled")
  override fun onViewCreated() {
    with(webView.settings) {
      setSupportZoom(true)
      builtInZoomControls = true
      displayZoomControls = false
      javaScriptEnabled = true
      userAgentString =
          "Mozilla/5.0 (Linux; U; Android 4.4; en-us; Nexus 4 Build/JOP24G) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30"
    }
    // necessary, else the image capturing does not include the web view. Very performance costly.
    webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    webView.webViewClient = object : WebViewClient() {

      override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        Timber.i("page started with $url")
        webViewIsLoading.onNext(true)
      }

      override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

        Timber.i("page stopped with $url")
        webViewIsLoading.onNext(false)
      }

      @Suppress("OverridingDeprecatedMember")
      override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String?,
        failingUrl: String?
      ) {
        Timber.d("received webViewError. Set webView invisible")
        view.loadUrl(ABOUT_BLANK)
        progressBar.isVisible = false
        noNetwork.isVisible = true
        webViewContainer.isVisible = false
      }
    }

    // after first successful load set visibilities
    webViewIsLoading
      .distinctUntilChanged()
      .filter { it }
      .subscribe {
        // sets progressbar and webviews visibilities correctly once the page is loaded
        Timber.i("WebView is now loading. Set webView visible")
        progressBar.isVisible = false
        noNetwork.isVisible = false
        webViewContainer.isVisible = true
      }

    webView.loadUrl(originalUrl)

    fab.setOnClickListener {
      cropOverlay.selectionOn = true
      cab!!.start(cabCallback)
      fab.hide()
    }

    setupToolbar()
  }

  @SuppressLint("InflateParams")
  private fun setupToolbar() {
    toolbar.setTitle(R.string.cover)

    toolbar.setNavigationIcon(R.drawable.close)
    toolbar.setNavigationOnClickListener { popOrBack() }

    toolbar.inflateMenu(R.menu.image_picker)
    toolbar.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.reset -> {
          webView.loadUrl(originalUrl)
          true
        }
        else -> false
      }
    }

    cab = MaterialCab(activity, R.id.cabStub)
      .setMenu(R.menu.crop_menu)

    // set the rotating icon
    val menu = toolbar.menu
    val refreshItem = menu.findItem(R.id.refresh)
    val rotation = AnimationUtils.loadAnimation(activity, R.anim.rotate).apply {
      repeatCount = Animation.INFINITE
    }
    val rotateView = LayoutInflater.from(activity).inflate(R.layout.rotate_view, null).apply {
      animation = rotation
      setOnClickListener { webView.reload() }
    }
    rotateView.setOnClickListener {
      if (webView.url == ABOUT_BLANK) {
        webView.loadUrl(originalUrl)
      } else webView.reload()
    }
    refreshItem.actionView = rotateView

    webViewIsLoading
      .filter { it }
      .filter { !rotation.hasStarted() }
      .doOnNext { Timber.i("is loading. Start animation") }
      .subscribe {
        rotation.start()
      }

    rotation.setAnimationListener(
      object : Animation.AnimationListener {
        override fun onAnimationRepeat(p0: Animation?) {
          if (webViewIsLoading.value == false) {
            Timber.i("we are in the refresh round. cancel now.")
            rotation.cancel()
            rotation.reset()
          }
        }

        override fun onAnimationEnd(p0: Animation?) {
        }

        override fun onAnimationStart(p0: Animation?) {
        }
      }
    )
  }

  override fun onRestoreViewState(view: View, savedViewState: Bundle) {
    // load the last page loaded or the original one of there is none
    val url: String? = savedViewState.getString(SI_URL)
    webView.loadUrl(url)
  }

  override fun handleBack(): Boolean {
    if (cab!!.isActive) {
      cab!!.finish()
      return true
    }

    if (webView.canGoBack()) {
      webView.goBack()
      return true
    }

    return false
  }

  override fun onSaveViewState(view: View, outState: Bundle) {
    if (webView.url != ABOUT_BLANK) {
      outState.putString(SI_URL, webView.url)
    }
  }

  companion object {

    private const val NI_BOOK_ID = "ni"
    private const val ABOUT_BLANK = "about:blank"
    private const val SI_URL = "savedUrl"
  }
}
