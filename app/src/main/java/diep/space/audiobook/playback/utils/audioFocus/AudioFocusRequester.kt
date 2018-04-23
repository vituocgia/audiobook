package diep.space.audiobook.playback.utils.audioFocus

interface AudioFocusRequester {

  fun request(): Int
  fun abandon()
}
