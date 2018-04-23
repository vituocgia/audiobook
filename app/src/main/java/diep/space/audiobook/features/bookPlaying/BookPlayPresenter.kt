package diep.space.audiobook.features.bookPlaying

import diep.space.audiobook.data.repo.BookRepository
import diep.space.audiobook.data.repo.BookmarkRepo
import diep.space.audiobook.injection.App
import diep.space.audiobook.misc.Optional
import diep.space.audiobook.playback.PlayStateManager
import diep.space.audiobook.playback.PlayStateManager.PlayState
import diep.space.audiobook.playback.PlayerController
import diep.space.audiobook.playback.SleepTimer
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject

class BookPlayPresenter(private val bookId: Long) : BookPlayMvp.Presenter() {

  @Inject
  lateinit var bookRepository: BookRepository
  @Inject
  lateinit var playerController: PlayerController
  @Inject
  lateinit var playStateManager: PlayStateManager
  @Inject
  lateinit var sleepTimer: SleepTimer
  @Inject
  lateinit var bookmarkRepo: BookmarkRepo

  init {
    App.component.inject(this)
  }

  override fun onAttach(view: BookPlayMvp.View) {
    playStateManager.playStateStream()
      .map { it == PlayState.PLAYING }
      .distinctUntilChanged()
      .subscribe {
        Timber.i("onNext with playing=$it")
        view.showPlaying(it)
      }
      .disposeOnDetach()

    sleepTimer.leftSleepTimeInMs
      .subscribe { view.showLeftSleepTime(it) }
      .disposeOnDetach()

    bookRepository.booksStream()
      .map {
        val currentBook = it.firstOrNull { it.id == bookId }
        Optional.of(currentBook)
      }
      .distinctUntilChanged()
      .subscribe {
        when (it) {
          is Optional.Present -> view.render(it.value)
          is Optional.Absent -> view.finish()
        }
      }
      .disposeOnDetach()
  }

  override fun playPause() {
    playerController.playPause()
  }

  override fun rewind() {
    playerController.rewind()
  }

  override fun fastForward() {
    playerController.fastForward()
  }

  override fun next() {
    playerController.next()
  }

  override fun previous() {
    playerController.previous()
  }

  override fun seekTo(position: Int, file: File?) {
    Timber.i("seekTo position$position, file$file")
    val book = bookRepository.bookById(bookId)
        ?: return
    playerController.changePosition(position, file ?: book.content.currentFile)
  }

  override fun toggleSleepTimer() {
    if (sleepTimer.sleepTimerActive()) sleepTimer.setActive(false)
    else {
      view.openSleepTimeDialog()
    }
  }

  override fun addBookmark() {
    launch(UI) {
      val book = bookRepository.bookById(bookId) ?: return@launch
      val title = book.content.currentChapter.name
      bookmarkRepo.addBookmarkAtBookPosition(book, title)
      view.showBookmarkAdded()
    }
  }
}
