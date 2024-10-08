/*
 * MIT License
 *
 * Stream-rec  https://github.com/hua0512/stream-rec
 *
 * Copyright (c) 2024 hua0512 (https://github.com/hua0512)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package github.hua0512.flv.utils

import github.hua0512.flv.FlvReader
import github.hua0512.flv.data.FlvData
import github.hua0512.flv.data.FlvTag
import github.hua0512.flv.exceptions.FlvErrorException
import io.ktor.utils.io.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.EOFException
import java.io.InputStream
import kotlin.toUInt
import kotlin.use


/**
 * Extension function to read FLV data from an InputStream and emit it as a Flow of FlvData.
 *
 * This function creates an instance of FlvReader to read the FLV header and tags from the InputStream.
 * The read data is emitted as a Flow of FlvData objects.
 *
 * @receiver InputStream The input stream from which to read the FLV data.
 * @return Flow<FlvData> A flow emitting FlvData objects read from the InputStream.
 * @author hua0512
 * @date : 2024/9/9 12:13
 */
fun InputStream.asFlvFlow(): Flow<FlvData> = flow {
  val flvReader = FlvReader(this@asFlvFlow)

  var lastTag: FlvData? = null
  try {
    flvReader.use {
      it.readHeader(::emit)
      it.readTags {
        lastTag = it
        emit(it)
      }
    }
  } catch (e: EOFException) {
    // End of file reached
  } catch (e: FlvErrorException) {
    e.printStackTrace()
  } catch (e: Exception) {
    if (e !is CancellationException)
      e.printStackTrace()
  } finally {
    lastTag?.let {
      if (it is FlvTag && it.isAvcEndSequence()) return@let
      if (it is FlvTag) emit(createEndOfSequenceTag(it.num + 1, it.header.timestamp, it.header.streamId.toInt()))
    }
    try {
      close()
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}

/**
 * Read an unsigned 24-bit integer from the InputStream.
 * @receiver InputStream The input stream from which to read the unsigned 24-bit integer.
 * @return UInt The unsigned 24-bit integer read from the InputStream.
 * @author hua0512
 * @date : 2024/6/10 19:31
 */
fun InputStream.readUI24(): UInt = readNBytes(3).let {
  return ((it[0].toUInt() and 0xFFu) shl 16) or
          ((it[1].toUInt() and 0xFFu) shl 8) or
          (it[2].toUInt() and 0xFFu)
}