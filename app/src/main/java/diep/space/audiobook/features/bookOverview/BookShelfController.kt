package diep.space.audiobook.features.bookOverview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.FragmentTransaction
import android.support.v4.view.ViewCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SimpleItemAnimator
import androidx.view.isVisible
import com.bluelinelabs.conductor.RouterTransaction
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import diep.space.audiobook.R
import diep.space.audiobook.data.Book
import diep.space.audiobook.features.bookOverview.list.BookShelfAdapter
import diep.space.audiobook.features.bookOverview.list.BookShelfClick
import diep.space.audiobook.features.bookOverview.list.BookShelfItemDecoration
import diep.space.audiobook.features.bookPlaying.BookPlayController
import diep.space.audiobook.features.folderOverview.FolderOverviewController
import diep.space.audiobook.features.imagepicker.ImagePickerController
import diep.space.audiobook.features.settings.SettingsController
import diep.space.audiobook.injection.App
import diep.space.audiobook.injection.PrefKeys
import diep.space.audiobook.misc.conductor.asTransaction
import diep.space.audiobook.misc.conductor.clearAfterDestroyView
import diep.space.audiobook.misc.conductor.clearAfterDestroyViewNullable
import diep.space.audiobook.misc.postedIfComputingLayout
import diep.space.audiobook.mvp.MvpController
import diep.space.audiobook.persistence.pref.Pref
import diep.space.audiobook.uitools.BookChangeHandler
import diep.space.audiobook.uitools.PlayPauseDrawable
import kotlinx.android.synthetic.main.book_shelf.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

private const val COVER_FROM_GALLERY = 1

/**
 * Showing the shelf of all the available books and provide a navigation to each book.
 */
class BookShelfController : MvpController<BookShelfView, BookShelfPresenter>(),
  EditCoverDialogFragment.Callback, EditBookBottomSheet.Callback, BookShelfView {

  override fun createPresenter() = App.component.bookShelfPresenter
  override val layoutRes = R.layout.book_shelf

  override fun provideView() = this

  init {
    App.component.inject(this)
  }

  @field:[Inject Named(PrefKeys.CURRENT_BOOK)]
  lateinit var currentBookIdPref: Pref<Long>

  private var playPauseDrawable: PlayPauseDrawable by clearAfterDestroyView()
  private var adapter: BookShelfAdapter by clearAfterDestroyView()
  private var currentTapTarget by clearAfterDestroyViewNullable<TapTargetView>()
  private var menuBook: Book? = null
  private var pendingTransaction: FragmentTransaction? = null

  override fun onViewCreated() {
    playPauseDrawable = PlayPauseDrawable()
    setupToolbar()
    setupFab()
    setupRecyclerView()
  }

  private fun setupFab() {
    fab.setIconDrawable(playPauseDrawable)
    fab.setOnClickListener { presenter.playPause() }
  }

  private fun setupRecyclerView() {
    recyclerView.setHasFixedSize(true)
    adapter = BookShelfAdapter { book, clickType ->
      when (clickType) {
        BookShelfClick.REGULAR -> invokeBookSelectionCallback(book)
        BookShelfClick.MENU -> {
          val editDialog = EditBookBottomSheet.newInstance(this, book)
          editDialog.show(fragmentManager, "editBottomSheet")
        }
      }
    }
    recyclerView.adapter = adapter
    // without this the item would blink on every change
    val anim = recyclerView.itemAnimator as SimpleItemAnimator
    anim.supportsChangeAnimations = false
    val listDecoration = BookShelfItemDecoration(activity)
    recyclerView.addItemDecoration(listDecoration)
    recyclerView.layoutManager = LinearLayoutManager(activity)
  }

  private fun setupToolbar() {
    toolbar.inflateMenu(R.menu.book_shelf)
    toolbar.title = getString(R.string.app_name)
    toolbar.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.action_settings -> {
          val transaction = SettingsController().asTransaction()
          router.pushController(transaction)
          true
        }
        R.id.library -> {
          toFolderOverview()
          true
        }
        else -> false
      }
    }
  }

  private fun toFolderOverview() {
    val controller = FolderOverviewController()
    router.pushController(controller.asTransaction())
  }

  override fun onActivityResumed(activity: Activity) {
    super.onActivityResumed(activity)

    pendingTransaction?.commit()
    pendingTransaction = null
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    when (requestCode) {
      COVER_FROM_GALLERY -> {
        if (resultCode == Activity.RESULT_OK) {
          val imageUri = data?.data
          val book = menuBook
          if (imageUri == null || book == null) {
            return
          }

          @SuppressLint("CommitTransaction")
          pendingTransaction = fragmentManager.beginTransaction()
            .add(
              EditCoverDialogFragment.newInstance(this, book, imageUri),
              EditCoverDialogFragment.TAG
            )
        }
      }
      else -> super.onActivityResult(requestCode, resultCode, data)
    }
  }

  private fun invokeBookSelectionCallback(book: Book) {
    currentBookIdPref.value = book.id
    val transaction = RouterTransaction.with(BookPlayController(book.id))
    val transition = BookChangeHandler()
    transition.transitionName = book.coverTransitionName
    transaction.pushChangeHandler(transition)
      .popChangeHandler(transition)
    router.pushController(transaction)
  }

  override fun render(state: BookShelfState) {
    Timber.i("render ${state.javaClass.simpleName}")
    when (state) {
      is BookShelfState.Content -> {
        adapter.submitList(state.books)
        val currentBook = state.currentBook

        fab.isVisible = currentBook != null
        showPlaying(state.playing)
      }
      is BookShelfState.NoFolderSet -> {
        showNoFolderWarning()
      }
    }
    loadingProgress.isVisible = state == BookShelfState.Loading
  }

  private fun showPlaying(playing: Boolean) {
    Timber.i("Called showPlaying $playing")
    val laidOut = ViewCompat.isLaidOut(fab)
    if (playing) {
      playPauseDrawable.transformToPause(laidOut)
    } else {
      playPauseDrawable.transformToPlay(laidOut)
    }
  }

  /** Show a warning that no audiobook folder was chosen */
  private fun showNoFolderWarning() {
    if (currentTapTarget?.isVisible == true)
      return

    val target = TapTarget.forToolbarMenuItem(
      toolbar,
      R.id.library,
      getString(R.string.onboarding_title),
      getString(R.string.onboarding_content)
    )
      .cancelable(false)
      .tintTarget(false)
      .outerCircleColor(R.color.accentDark)
      .descriptionTextColorInt(Color.WHITE)
      .textColorInt(Color.WHITE)
      .targetCircleColorInt(Color.BLACK)
      .transparentTarget(true)
    currentTapTarget = TapTargetView.showFor(activity, target, object : TapTargetView.Listener() {
      override fun onTargetClick(view: TapTargetView?) {
        super.onTargetClick(view)
        toFolderOverview()
      }
    })
  }

  override fun bookCoverChanged(bookId: Long) {
    // there is an issue where notifyDataSetChanges throws:
    // java.lang.IllegalStateException: Cannot call this method while RecyclerView is computing a layout or scrolling
    recyclerView.postedIfComputingLayout {
      adapter.reloadBookCover(bookId)
    }
  }

  override fun onBookCoverChanged(book: Book) {
    recyclerView.postedIfComputingLayout {
      adapter.reloadBookCover(book.id)
    }
  }

  override fun onInternetCoverRequested(book: Book) {
    router.pushController(ImagePickerController(book).asTransaction())
  }

  override fun onFileCoverRequested(book: Book) {
    menuBook = book
    val galleryPickerIntent = Intent(Intent.ACTION_PICK)
    galleryPickerIntent.type = "image/*"
    startActivityForResult(galleryPickerIntent, COVER_FROM_GALLERY)
  }

  override fun onDestroyView() {
    super.onDestroyView()
    recyclerView.adapter = null
  }
}
