package diep.space.audiobook.misc

interface ErrorReporter {

  fun log(message: String)
  fun logException(throwable: Throwable)
}
