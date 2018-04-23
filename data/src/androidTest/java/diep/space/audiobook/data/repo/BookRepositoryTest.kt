package diep.space.audiobook.data.repo

import android.support.test.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.squareup.moshi.Moshi
import diep.space.audiobook.BookFactory
import diep.space.audiobook.data.repo.internals.BookStorage
import diep.space.audiobook.data.repo.internals.InternalDb
import kotlinx.coroutines.experimental.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class BookRepositoryTest {

  private lateinit var repo: BookRepository

  @Rule
  @JvmField
  val clearAppDbRule = ClearDbRule()

  @Before
  fun setUp() {
    val context = InstrumentationRegistry.getTargetContext()
    val internalDb = InternalDb(context)
    val moshi = Moshi.Builder().build()
    val internalBookRegister = BookStorage(internalDb, moshi)
    repo = BookRepository(internalBookRegister)
  }

  @Test
  fun inOut() {
    runBlocking {
      val dummy = BookFactory.create()
    repo.addBook(dummy)
    val firstBook = repo.activeBooks.first()
    val dummyWithUpdatedId = dummy.copy(id = firstBook.id)

    assertThat(dummyWithUpdatedId).isEqualTo(firstBook)
    }
  }
}
