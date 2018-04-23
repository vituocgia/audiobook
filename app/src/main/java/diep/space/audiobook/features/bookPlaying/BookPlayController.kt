package diep.space.audiobook.features.bookPlaying

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.view.ViewCompat
import android.view.MenuItem
import android.view.View
import android.view.ViewConfiguration
import android.widget.SeekBar
import androidx.view.isVisible
import com.squareup.picasso.Picasso
import diep.space.audiobook.R
import diep.space.audiobook.data.Book
import diep.space.audiobook.data.repo.internals.IO
import diep.space.audiobook.features.audio.Equalizer
import diep.space.audiobook.features.audio.LoudnessDialog
import diep.space.audiobook.features.bookmarks.BookmarkController
import diep.space.audiobook.features.settings.SettingsController
import diep.space.audiobook.features.settings.dialogs.PlaybackSpeedDialogFragment
import diep.space.audiobook.injection.App
import diep.space.audiobook.misc.MultiLineSpinnerAdapter
import diep.space.audiobook.misc.clicks
import diep.space.audiobook.misc.color
import diep.space.audiobook.misc.conductor.asTransaction
import diep.space.audiobook.misc.conductor.clearAfterDestroyView
import diep.space.audiobook.misc.coverFile
import diep.space.audiobook.misc.itemSelections
import diep.space.audiobook.mvp.MvpController
import diep.space.audiobook.uitools.CoverReplacement
import diep.space.audiobook.uitools.MAX_IMAGE_SIZE
import diep.space.audiobook.uitools.PlayPauseDrawable
import diep.space.audiobook.uitools.ThemeUtil
import kotlinx.android.synthetic.main.book_play.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val NI_BOOK_ID = "niBookId"

/**
 * Base class for book playing interaction.
 */
class BookPlayController(
  bundle: Bundle
) : MvpController<BookPlayMvp.View, BookPlayMvp.Presenter>(bundle),
  BookPlayMvp.View {

  constructor(bookId: Long) : this(Bundle().apply { putLong(NI_BOOK_ID, bookId) })

  @Inject
  lateinit var equalizer: Equalizer

  private val data = ArrayList<BookPlayChapter>()
  private val bookId = bundle.getLong(NI_BOOK_ID)
  private val playPauseDrawable = PlayPauseDrawable()
  private var currentChapter: BookPlayChapter? = null

  override val layoutRes = R.layout.book_play
  override fun createPresenter() = BookPlayPresenter(bookId)

  private var spinnerAdapter: MultiLineSpinnerAdapter<BookPlayChapter> by clearAfterDestroyView()
  private var sleepTimerItem: MenuItem by clearAfterDestroyView()

  init {
    App.component.inject(this)
  }

  override fun render(book: Book) {
    data.clear()
    data.addAll(book.content.chapters.chaptersAsBookPlayChapters())
    spinnerAdapter.setData(data)

    val dataForCurrentFile = data.filter { it.file == book.content.currentFile }

    // find closest position
    val currentChapter =
      dataForCurrentFile.firstOrNull { book.content.positionInChapter >= it.start && book.content.positionInChapter < it.stop }
          ?: dataForCurrentFile.firstOrNull { book.content.positionInChapter == it.stop }
          ?: dataForCurrentFile.first()
    this.currentChapter = currentChapter

    val chapterIndex = data.indexOf(currentChapter)
    bookSpinner.setSelection(chapterIndex, true)
    val duration = currentChapter.duration
    seekBar.max = duration
    maxTime.text = formatTime(duration, duration)

    // Setting seekBar and played getTime view
    val progress = book.content.positionInChapter - currentChapter.start
    if (!seekBar.isPressed) {
      seekBar.progress = progress
      playedTime.text = formatTime(progress, duration)
    }

    // name
    toolbar.title = book.name

    // Next/Prev/spinner/book progress views hiding
    val multipleChapters = data.size > 1
    next.isVisible = multipleChapters
    previous.isVisible = multipleChapters
    bookSpinner.isVisible = multipleChapters

    cover.transitionName = book.coverTransitionName

    launch(IO) {
      val coverReplacement = CoverReplacement(book.name, activity)
      val coverFile = book.coverFile()
      val shouldLoadCover = coverFile.canRead() && coverFile.length() < MAX_IMAGE_SIZE
      withContext(UI) {
        if (shouldLoadCover) {
          Picasso.with(activity)
            .load(coverFile)
            .placeholder(coverReplacement)
            .into(cover)
        } else {
          cover.setImageDrawable(coverReplacement)
        }
      }
    }.cancelOnDestroyView()
  }

  override fun finish() {
    view?.post {
      router.popController(this)
    }
  }

  override fun onViewCreated() {
    setupClicks()
    setupFab()
    setupSeekBar()
    setupSpinner()
    setupToolbar()
  }

  private fun setupClicks() {
    play.setOnClickListener { presenter.playPause() }
    rewind.setOnClickListener { presenter.rewind() }
    fastForward.setOnClickListener { presenter.fastForward() }
    next.setOnClickListener { presenter.next() }
    previous.setOnClickListener { presenter.previous() }
    playedTime.setOnClickListener { launchJumpToPositionDialog() }

    var lastClick = 0L
    val doubleClickTime = ViewConfiguration.getDoubleTapTimeout()
    cover.clicks()
      .filter {
        val currentTime = System.currentTimeMillis()
        val doubleClick = currentTime - lastClick < doubleClickTime
        lastClick = currentTime
        doubleClick
      }
      .doOnNext { lastClick = 0 } // resets so triple clicks won't cause another invoke
      .subscribe { presenter.playPause() }
  }

  private fun setupFab() {
    play.setIconDrawable(playPauseDrawable)
  }

  private fun setupSeekBar() {
    seekBar.setOnSeekBarChangeListener(
      object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(view: SeekBar?, progress: Int, p2: Boolean) {
          //sets text to adjust while using seekBar
          playedTime.text = formatTime(progress, seekBar.max)
        }

        override fun onStartTrackingTouch(view: SeekBar?) {
        }

        override fun onStopTrackingTouch(view: SeekBar?) {
          currentChapter?.let {
            val progress = seekBar.progress
            presenter.seekTo(it.start + progress, it.file)
          }
        }
      }
    )
  }

  private fun setupSpinner() {
    spinnerAdapter = MultiLineSpinnerAdapter(
      spinner = bookSpinner,
      context = activity,
      unselectedTextColor = activity.color(
        ThemeUtil.getResourceId(
          activity,
          android.R.attr.textColorPrimary
        )
      ),
      resolveName = BookPlayChapter::correctedName
    )
    bookSpinner.adapter = spinnerAdapter

    bookSpinner.itemSelections {
      Timber.i("spinner: onItemSelected. firing: $it")
      val item = data[it]
      presenter.seekTo(item.start, item.file)
    }
  }

  private fun setupToolbar() {
    toolbar.setNavigationIcon(R.drawable.ic_arrow_back)

    toolbar.inflateMenu(R.menu.book_play)
    val menu = toolbar.menu

    sleepTimerItem = menu.findItem(R.id.action_sleep)
    val equalizerItem = menu.findItem(R.id.action_equalizer)
    equalizerItem.isVisible = equalizer.exists

    toolbar.findViewById<View>(R.id.action_bookmark)
      .setOnLongClickListener {
        presenter.addBookmark()
        true
      }

    toolbar.setNavigationOnClickListener { router.popController(this) }
    toolbar.setOnMenuItemClickListener {
      when (it.itemId) {
        R.id.action_settings -> {
          val transaction = SettingsController().asTransaction()
          router.pushController(transaction)
          true
        }
        R.id.action_time_change -> {
          launchJumpToPositionDialog()
          true
        }
        R.id.action_sleep -> {
          presenter.toggleSleepTimer()
          true
        }
        R.id.action_time_lapse -> {
          PlaybackSpeedDialogFragment().show(
            fragmentManager,
            PlaybackSpeedDialogFragment.TAG
          )
          true
        }
        R.id.action_bookmark -> {
          val bookmarkController = BookmarkController.newInstance(bookId)
            .asTransaction()
          router.pushController(bookmarkController)
          true
        }
        R.id.action_equalizer -> {
          equalizer.launch(activity)
          true
        }
        R.id.loudness -> {
          LoudnessDialog(bookId).showDialog(router)
          true
        }
        else -> false
      }
    }
  }

  override fun showPlaying(playing: Boolean) {
    val laidOut = ViewCompat.isLaidOut(play)
    if (playing) {
      playPauseDrawable.transformToPause(animated = laidOut)
    } else {
      playPauseDrawable.transformToPlay(animated = laidOut)
    }
  }

  private fun launchJumpToPositionDialog() {
    JumpToPositionDialogFragment().show(fragmentManager, JumpToPositionDialogFragment.TAG)
  }

  override fun showLeftSleepTime(ms: Int) {
    val active = ms > 0
    // sets the correct sleep timer icon
    sleepTimerItem.setIcon(if (active) R.drawable.alarm_off else R.drawable.alarm)
    // set text and visibility
    timerCountdownView.text = formatTime(ms, ms)
    timerCountdownView.isVisible = active
  }

  override fun openSleepTimeDialog() {
    SleepTimerDialogFragment(bookId)
      .show(fragmentManager, "fmSleepTimer")
  }

  override fun onDestroyView() {
    super.onDestroyView()
    bookSpinner.adapter = null
  }

  override fun showBookmarkAdded() {
    Snackbar.make(view!!, R.string.bookmark_added, Snackbar.LENGTH_SHORT)
      .show()
  }

  private fun formatTime(ms: Int, duration: Int): String {
    val h = TimeUnit.MILLISECONDS.toHours(ms.toLong()).toString()
    val m = "%02d".format((TimeUnit.MILLISECONDS.toMinutes(ms.toLong()) % 60))
    val s = "%02d".format((TimeUnit.MILLISECONDS.toSeconds(ms.toLong()) % 60))

    return if (TimeUnit.MILLISECONDS.toHours(duration.toLong()) == 0L) {
      "$m:$s"
    } else {
      "$h:$m:$s"
    }
  }
}
