package diep.space.audiobook

import org.mockito.Mockito
import org.mockito.stubbing.OngoingStubbing

fun <T> given(methodCall: () -> T): OngoingStubbing<T> = Mockito.`when`(methodCall())
