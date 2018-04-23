package diep.space.audiobook.features.bookOverview

import diep.space.audiobook.data.repo.BookRepository
import diep.space.audiobook.features.BookAdder
import diep.space.audiobook.injection.PrefKeys
import diep.space.audiobook.misc.Observables
import diep.space.audiobook.mvp.Presenter
import diep.space.audiobook.persistence.pref.Pref
import diep.space.audiobook.playback.PlayStateManager
import diep.space.audiobook.playback.PlayStateManager.PlayState
import diep.space.audiobook.playback.PlayerController
import diep.space.audiobook.uitools.CoverFromDiscCollector
import javax.inject.Inject
import javax.inject.Named

class BookShelfPresenter
@Inject
constructor(
  private val repo: BookRepository,
  private val bookAdder: BookAdder,
  private val playStateManager: PlayStateManager,
  private val playerController: PlayerController,
  private val coverFromDiscCollector: CoverFromDiscCollector,
  @Named(PrefKeys.CURRENT_BOOK)
  private val currentBookIdPref: Pref<Long>
) : Presenter<BookShelfView>() {

  override fun onAttach(view: BookShelfView) {
    bookAdder.scanForFiles()
    setupStateStream()
    setupCoverChanged()
  }

  private fun setupStateStream() {
    val bookStream = repo.booksStream()
    val currentBookIdStream = currentBookIdPref.stream
    val playingStream = playStateManager.playStateStream()
      .map { it == PlayState.PLAYING }
      .distinctUntilChanged()
    val scannerActiveStream = bookAdder.scannerActive
    Observables
      .combineLatest(
        bookStream,
        currentBookIdStream,
        playingStream,
        scannerActiveStream
      ) { books, currentBookId, playing, scannerActive ->
        when {
          books.isEmpty() && !scannerActive -> BookShelfState.NoFolderSet
          if (books.isEmpty()) scannerActive else false -> BookShelfState.Loading
          else -> {
            val currentBook = books.find { it.id == currentBookId }
            BookShelfState.Content(books = books, currentBook = currentBook, playing = playing)
          }
        }
      }
      .subscribe { view.render(it) }
      .disposeOnDetach()
  }

  private fun setupCoverChanged() {
    coverFromDiscCollector.coverChanged()
      .subscribe { view.bookCoverChanged(it) }
      .disposeOnDetach()
  }

  fun playPause() = playerController.playPause()
}
