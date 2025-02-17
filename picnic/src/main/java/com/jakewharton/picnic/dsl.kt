@file:JvmName("-DslKt")

package com.jakewharton.picnic

import kotlin.DeprecationLevel.ERROR

@DslMarker
annotation class PicnicDsl

fun table(content: TableDsl.() -> Unit) = TableBuilder().apply(content).build()

@PicnicDsl
interface TableDsl : SectionDsl {
  fun header(content: SectionDsl.() -> Unit)
  fun body(content: SectionDsl.() -> Unit)
  fun footer(content: SectionDsl.() -> Unit)
  fun style(content: TableStyleDsl.() -> Unit)
}

@PicnicDsl
interface TableStyleDsl {
  var borderStyle: BorderStyle?
}

@PicnicDsl
interface SectionDsl {
  fun row(vararg cells: Any?) {
    row {
      cells.forEach { cell(it) }
    }
  }

  fun row(content: RowDsl.() -> Unit)

  fun cellStyle(content: CellStyleDsl.() -> Unit)
}

@PicnicDsl
interface RowDsl {
  fun cell(content: Any?, style: CellDsl.() -> Unit = {})

  fun cellStyle(content: CellStyleDsl.() -> Unit)
}

@PicnicDsl
interface CellDsl : CellStyleDsl {
  var columnSpan: Int
  var rowSpan: Int
}

@PicnicDsl
interface CellStyleDsl {
  var paddingLeft: Int?
  var paddingRight: Int?
  var paddingTop: Int?
  var paddingBottom: Int?

  var borderLeft: Boolean?
  var borderRight: Boolean?
  var borderTop: Boolean?
  var borderBottom: Boolean?

  var alignment: TextAlignment?

  var border: Boolean
    @JvmSynthetic
    @Deprecated("Use individual getters", level = ERROR)
    get() = throw UnsupportedOperationException()
    set(value) {
      borderLeft = value
      borderRight = value
      borderTop = value
      borderBottom = value
    }

  var padding: Int
    @JvmSynthetic
    @Deprecated("Use individual getters", level = ERROR)
    get() = throw UnsupportedOperationException()
    set(value) {
      paddingLeft = value
      paddingRight = value
      paddingTop = value
      paddingBottom = value
    }
}

private class TableBuilder : TableDsl {
  private val headerBuilder = SectionBuilder(::Header)
  private val bodyBuilder = SectionBuilder(::Body)
  private val footerBuilder = SectionBuilder(::Footer)
  private val cellStyleBuilder = CellStyleBuilder()
  private val tableStyleBuilder = TableStyleBuilder()

  override fun header(content: SectionDsl.() -> Unit) {
    headerBuilder.apply(content)
  }

  override fun body(content: SectionDsl.() -> Unit) {
    bodyBuilder.apply(content)
  }

  override fun footer(content: SectionDsl.() -> Unit) {
    footerBuilder.apply(content)
  }

  override fun row(content: RowDsl.() -> Unit) {
    bodyBuilder.row(content)
  }

  override fun cellStyle(content: CellStyleDsl.() -> Unit) {
    cellStyleBuilder.apply(content)
  }

  override fun style(content: TableStyleDsl.() -> Unit) {
    tableStyleBuilder.apply(content)
  }

  fun build() = Table(
      headerBuilder.buildOrNull(),
      bodyBuilder.build(),
      footerBuilder.buildOrNull(),
      cellStyleBuilder.buildOrNull(),
      tableStyleBuilder.buildOrNull())
}

private class SectionBuilder<T : Any>(
  private val sectionFactory: (List<Row>, CellStyle?) -> T
) : SectionDsl {
  private val rows = mutableListOf<Row>()
  private val cellStyleBuilder = CellStyleBuilder()

  override fun row(content: RowDsl.() -> Unit) {
    rows += RowBuilder().apply(content).build()
  }

  override fun cellStyle(content: CellStyleDsl.() -> Unit) {
    cellStyleBuilder.apply(content)
  }

  fun buildOrNull() = if (rows.isEmpty()) null else build()
  fun build() = sectionFactory(rows.toList(), cellStyleBuilder.buildOrNull())
}

private class RowBuilder : RowDsl {
  private val cells = mutableListOf<Cell>()
  private val cellStyleBuilder = CellStyleBuilder()

  override fun cell(content: Any?, style: CellDsl.() -> Unit) {
    cells += CellBuilder(content).apply(style).build()
  }

  override fun cellStyle(content: CellStyleDsl.() -> Unit) {
    cellStyleBuilder.apply(content)
  }

  fun build() = Row(cells.toList(), cellStyleBuilder.buildOrNull())
}

private class CellBuilder private constructor(
  private val content: Any?,
  private val cellStyleBuilder: CellStyleBuilder
) : CellDsl, CellStyleDsl by cellStyleBuilder {

  constructor(content: Any?): this(content, CellStyleBuilder())

  override var columnSpan: Int = 1
  override var rowSpan: Int = 1

  fun build(): Cell {
    return Cell(
        content = content?.toString() ?: "",
        columnSpan = columnSpan,
        rowSpan = rowSpan,
        style = cellStyleBuilder.buildOrNull()
    )
  }
}

private class CellStyleBuilder : CellStyleDsl {
  override var paddingLeft: Int? = null
  override var paddingRight: Int? = null
  override var paddingTop: Int? = null
  override var paddingBottom: Int? = null
  override var borderLeft: Boolean? = null
  override var borderRight: Boolean? = null
  override var borderTop: Boolean? = null
  override var borderBottom: Boolean? = null
  override var alignment: TextAlignment? = null

  fun buildOrNull(): CellStyle? {
    if (paddingLeft != null ||
        paddingRight != null ||
        paddingTop != null ||
        paddingBottom != null ||
        borderLeft != null ||
        borderRight != null ||
        borderTop != null ||
        borderBottom != null ||
        alignment != null
    ) {
      return CellStyle(
          paddingLeft = paddingLeft,
          paddingRight = paddingRight,
          paddingTop = paddingTop,
          paddingBottom = paddingBottom,
          borderLeft = borderLeft,
          borderRight = borderRight,
          borderTop = borderTop,
          borderBottom = borderBottom,
          alignment = alignment
      )
    }
    return null
  }
}

private class TableStyleBuilder : TableStyleDsl {
  override var borderStyle: BorderStyle? = null

  fun buildOrNull(): TableStyle? {
    if (borderStyle != null) {
      return TableStyle(borderStyle)
    }
    return null
  }
}
