package com.jakewharton.diffuse

import com.jakewharton.picnic.TextAlignment.MiddleLeft
import com.jakewharton.picnic.TextAlignment.MiddleRight
import com.jakewharton.picnic.renderText

internal class ArscDiff(
  val oldArsc: Arsc,
  val newArsc: Arsc
) {
  val configsAdded = (newArsc.configs - oldArsc.configs).sorted()
  val configsRemoved = (oldArsc.configs - newArsc.configs).sorted()
  val entriesAdded = (newArsc.entries - oldArsc.entries).sorted()
  val entriesRemoved = (oldArsc.entries - newArsc.entries).sorted()

  val changed = configsAdded.isNotEmpty() ||
      configsRemoved.isNotEmpty() ||
      entriesAdded.isNotEmpty() ||
      entriesRemoved.isNotEmpty()
}

internal fun ArscDiff.toSummaryTable() = diffuseTable {
  header {
    row {
      cell("ARSC")
      cell("old")
      cell("new")
      cell("diff") {
        columnSpan = 2
      }
    }
  }

  body {
    cellStyle {
      alignment = MiddleRight
    }

    row {
      cell("configs")
      cell(oldArsc.configs.size)
      cell(newArsc.configs.size)

      val configsDelta = configsAdded.size - configsRemoved.size
      cell(configsDelta.toDiffString()) {
        borderRight = false
      }

      val delta = if (configsDelta > 0) {
        val added = configsAdded.size.toDiffString(zeroSign = '+')
        val removed = (-configsRemoved.size).toDiffString(zeroSign = '-')
        "($added $removed)"
      } else {
        ""
      }
      cell(delta) {
        borderLeft = false
        paddingLeft = 0
        alignment = MiddleLeft
      }
    }

    row {
      cell("entries")
      cell(oldArsc.entries.size)
      cell(newArsc.entries.size)

      val entriesDelta = entriesAdded.size - entriesRemoved.size
      cell(entriesDelta.toDiffString()) {
        borderRight = false
      }

      val delta = if (entriesDelta > 0) {
        val added = entriesAdded.size.toDiffString(zeroSign = '+')
        val removed = (-entriesRemoved.size).toDiffString(zeroSign = '-')
        "($added $removed)"
      } else {
        ""
      }
      cell(delta) {
        borderLeft = false
        paddingLeft = 0
        alignment = MiddleLeft
      }
    }
  }
}.renderText()

internal fun ArscDiff.toDetailReport() = buildString {
  fun <T> appendComponentDiff(name: String, added: List<T>, removed: List<T>) {
    if (added.isNotEmpty() || removed.isNotEmpty()) {
      appendln()
      appendln("$name:")
      appendln()
      appendln(buildString {
        appendln(diffuseTable {
          header {
            row {
              cell("old")
              cell("new")
              cell("diff")
            }
          }

          val diffSize = (added.size - removed.size).toDiffString()
          val addedSize = added.size.toDiffString(zeroSign = '+')
          val removedSize = (-removed.size).toDiffString(zeroSign = '-')
          row(added.size, removed.size, "$diffSize ($addedSize $removedSize)")
        }.renderText())
        added.forEach {
          appendln("+ $it")
        }
        if (added.isNotEmpty() && removed.isNotEmpty()) {
          appendln()
        }
        removed.forEach {
          appendln("- $it")
        }
      }.prependIndent("  "))
    }
  }

  appendComponentDiff("CONFIGS", configsAdded, configsRemoved)
  appendComponentDiff("ENTRIES", entriesAdded, entriesRemoved)
}
