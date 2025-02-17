package com.jakewharton.dex

import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

internal fun ByteArray.startsWith(value: ByteArray): Boolean {
  if (value.size > size) return false
  value.forEachIndexed { i, byte ->
    if (get(i) != byte) {
      return false
    }
  }
  return true
}

internal fun ZipInputStream.entries(): Sequence<ZipEntry> {
  return object : Sequence<ZipEntry> {
    override fun iterator(): Iterator<ZipEntry> {
      return object : Iterator<ZipEntry> {
        var next: ZipEntry? = null
        var done = false

        override fun hasNext(): Boolean {
          if (done) return false
          if (next == null) {
            next = nextEntry
            if (next == null) {
              done = true
            }
          }
          return next != null
        }

        override fun next(): ZipEntry {
          if (!hasNext()) {
            throw NoSuchElementException()
          }
          return next!!.also { next = null }
        }
      }
    }
  }
}

internal fun <T, R> Pair<T, T>.mapEach(body: (T) -> R): Pair<R, R> = body(first) to body(second)

// TODO https://youtrack.jetbrains.com/issue/KT-18242
internal fun Path.readBytes() = Files.readAllBytes(this)

// TODO replace with https://youtrack.jetbrains.com/issue/KT-20690
internal fun <T : Comparable<T>> comparingValues(): Comparator<Iterable<T>> {
  return object : Comparator<Iterable<T>> {
    override fun compare(o1: Iterable<T>, o2: Iterable<T>): Int {
      val i1 = o1.iterator()
      val i2 = o2.iterator()
      while (true) {
        if (!i1.hasNext()) {
          return if (!i2.hasNext()) 0 else -1
        }
        if (!i2.hasNext()) {
          return 1
        }
        val result = i1.next().compareTo(i2.next())
        if (result != 0) {
          return result
        }
      }
    }
  }
}
