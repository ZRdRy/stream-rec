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

package github.hua0512.flv.data

import github.hua0512.flv.exceptions.FlvHeaderErrorException
import java.io.OutputStream

/**
 * FLV header data class
 * @property signature FLV signature "FLV", always 3 bytes
 * @property version FLV version, always 1 byte
 * @property flags FLV flags, always 1 byte
 * @property headerSize FLV header size, always 4 bytes
 */
data class FlvHeader(val signature: String, val version: Int, val flags: FlvHeaderFlags, val headerSize: Int, override val crc32: Long) : FlvData {

  init {
    if (signature.length != 3 || signature != "FLV") {
      throw FlvHeaderErrorException("Invalid FLV signature: $signature")
    }

    if (version != 1) {
      throw FlvHeaderErrorException("Invalid FLV version: $version")
    }

    if (headerSize != 9) {
      throw FlvHeaderErrorException("Invalid FLV header size: $headerSize")
    }
  }

  override val size = headerSize.toLong()


  fun write(os: OutputStream) {
    with(os) {
      // write 'FLV' signature
      write(signature.toByteArray())
      write(version)
      write(flags.value)
      // write header size as Int (4 bytes)
      write(headerSize shr 24)
      write(headerSize shr 16)
      write(headerSize shr 8)
      write(headerSize)
    }
  }
}