package com.dktech.baseandroidviewdktech.svgparser

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.Xml
import androidx.annotation.RawRes
import androidx.core.graphics.PathParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.xmlpull.v1.XmlPullParser
import java.util.Stack
import kotlin.math.roundToInt

class SegmentParser {
    suspend fun parseSVGFile(
        mContext: Context,
        @RawRes svgRes: Int,
    ): SVGInfo {
        var segmentsID: Int = 0

        return runInterruptible(Dispatchers.Default) {
            val parser = mContext.resources.openRawResource(svgRes)
            val xml = Xml.newPullParser()
            xml.setInput(parser, null)
            val groups = mutableListOf<SegmentGroup>()
            val matrixStack = Stack<Matrix>()
            var currentSegmentGroup: SegmentGroup? = null
            var viewBoxWidth = 0f
            var viewBoxHeight = 0f

            var event = xml.eventType

            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        when (xml.name) {
                            "svg" -> {
                                val viewBox = xml.getAttributeValue(null, "viewBox")
                                val parts = viewBox.split(" ")
                                viewBoxWidth = parts[2].toFloat()
                                viewBoxHeight = parts[3].toFloat()
                            }

                            "g" -> {
                                val id = xml.getAttributeValue(null, "id") ?: "group"
                                val name = xml.getAttributeValue(null, "data-name")
                                val m = Matrix()
                                xml
                                    .getAttributeValue(null, "transform")
                                    ?.let { parseTransform(it, m) }

                                matrixStack.push(m)
                                currentSegmentGroup =
                                    SegmentGroup(id, name, Matrix(), mutableListOf())
                                groups.add(currentSegmentGroup)
                            }

                            "path" -> {
                                convertPath(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++.toString()))
                                    }
                                }
                            }

                            "circle" -> {
                                convertCircle(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++.toString()))
                                    }
                                }
                            }

                            "rect" -> {
                                convertRect(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++.toString()))
                                    }
                                }
                            }

                            "ellipse" -> {
                                convertEllipse(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++.toString()))
                                    }
                                }
                            }

                            "polygon" -> {
                                convertPolygon(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++.toString()))
                                    }
                                }
                            }

                            "polyline" -> {
                                convertPolyline(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++.toString()))
                                    }
                                }
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        when (xml.name) {
                            "g" -> {
                                matrixStack.pop()
                                currentSegmentGroup = null
                            }
                        }
                    }
                }

                event = xml.next()
            }

            assignLayerNumbers(groups)

            return@runInterruptible SVGInfo(
                viewBoxWidth.roundToInt(),
                viewBoxHeight.roundToInt(),
                "",
                groups
            )
        }
    }

    private fun ensureDefaultGroup(
        groups: MutableList<SegmentGroup>,
        currentGroup: SegmentGroup?,
    ): SegmentGroup? {
        if (currentGroup != null) {
            return currentGroup
        }

        val defaultGroup = groups.firstOrNull { it.id == "default_group" }
        if (defaultGroup != null) {
            return defaultGroup
        }

        val newGroup = SegmentGroup("default_group", "Default Group", Matrix(), mutableListOf())
        groups.add(newGroup)
        return newGroup
    }

    private fun convertPath(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
        val d = xml.getAttributeValue(null, "d") ?: return null
        val id = xml.getAttributeValue(null, "id") ?: "seg"
        val number = xml.getAttributeValue(null, "data-number")?.toInt() ?: 0
        val fillColor = resolveFillColor(xml)

        val path = PathParser.createPathFromPathData(d)
        if (matrixStack.isNotEmpty()) {
            path.transform(matrixStack.peek())
        }

        return createSegment(id, number, path, fillColor, xml)
    }

    private fun convertCircle(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
        val id = xml.getAttributeValue(null, "id") ?: "seg"
        val number = xml.getAttributeValue(null, "data-number")?.toInt() ?: 0
        val cx = xml.getAttributeValue(null, "cx")?.toFloat() ?: 0f
        val cy = xml.getAttributeValue(null, "cy")?.toFloat() ?: 0f
        val r = xml.getAttributeValue(null, "r")?.toFloat() ?: 0f
        val fillColor = resolveFillColor(xml)

        val path =
            Path().apply {
                addCircle(cx, cy, r, Path.Direction.CW)
                if (matrixStack.isNotEmpty()) {
                    transform(matrixStack.peek())
                }
            }

        return createSegment(id, number, path, fillColor, xml)
    }

    private fun convertRect(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
        val id = xml.getAttributeValue(null, "id") ?: "seg"
        val number = xml.getAttributeValue(null, "data-number")?.toInt() ?: 0
        val x = xml.getAttributeValue(null, "x")?.toFloat() ?: 0f
        val y = xml.getAttributeValue(null, "y")?.toFloat() ?: 0f
        val w = xml.getAttributeValue(null, "width")?.toFloat() ?: 0f
        val h = xml.getAttributeValue(null, "height")?.toFloat() ?: 0f
        val fillColor = resolveFillColor(xml)

        val path =
            Path().apply {
                addRect(x, y, x + w, y + h, Path.Direction.CW)
                if (matrixStack.isNotEmpty()) {
                    transform(matrixStack.peek())
                }
            }

        return createSegment(id, number, path, fillColor, xml)
    }

    private fun convertEllipse(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
        val id = xml.getAttributeValue(null, "id") ?: "seg"
        val number = xml.getAttributeValue(null, "data-number")?.toInt() ?: 0
        val cx = xml.getAttributeValue(null, "cx")?.toFloat() ?: 0f
        val cy = xml.getAttributeValue(null, "cy")?.toFloat() ?: 0f
        val rx = xml.getAttributeValue(null, "rx")?.toFloat() ?: 0f
        val ry = xml.getAttributeValue(null, "ry")?.toFloat() ?: 0f
        val fillColor = resolveFillColor(xml)

        val path =
            Path().apply {
                addOval(cx - rx, cy - ry, cx + rx, cy + ry, Path.Direction.CW)
                if (matrixStack.isNotEmpty()) {
                    transform(matrixStack.peek())
                }
            }

        return createSegment(id, number, path, fillColor, xml)
    }

    private fun convertPolygon(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
        val id = xml.getAttributeValue(null, "id") ?: "seg"
        val number = xml.getAttributeValue(null, "data-number")?.toInt() ?: 0
        val points = xml.getAttributeValue(null, "points") ?: return null
        val fillColor = resolveFillColor(xml)

        val path = parsePolygonPoints(points, true)
        if (matrixStack.isNotEmpty()) {
            path.transform(matrixStack.peek())
        }

        return createSegment(id, number, path, fillColor, xml)
    }

    private fun convertPolyline(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
        val id = xml.getAttributeValue(null, "id") ?: "seg"
        val number = xml.getAttributeValue(null, "data-number")?.toInt() ?: 0
        val points = xml.getAttributeValue(null, "points") ?: return null
        val fillColor = resolveFillColor(xml)

        val path = parsePolygonPoints(points, false)
        if (matrixStack.isNotEmpty()) {
            path.transform(matrixStack.peek())
        }

        return createSegment(id, number, path, fillColor, xml)
    }

    private fun createSegment(
        id: String,
        number: Int,
        path: Path,
        fillColor: Int?,
        xml: XmlPullParser,
    ): Segments {
        val region = buildRegion(path)
        val bounds = RectF()
        path.computeBounds(bounds, true)

        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = fillColor ?: Color.BLACK
            }
        val strokePaint = createPaintFromSvg(xml)

        return Segments(
            id = id,
            number = number,
            path = path,
            region = region,
            fillPaint = paint,
            strokePaint = strokePaint,
            originalColor = fillColor,
            bounds = bounds,
        )
    }

    private fun createPaintFromSvg(xml: XmlPullParser): Paint {
        val stroke = xml.getAttributeValue(null, "stroke")
        val strokeWidth = xml.getAttributeValue(null, "stroke-width")?.toFloatOrNull() ?: 0f
        val strokeLinecap = xml.getAttributeValue(null, "stroke-linecap")
        val strokeLinejoin = xml.getAttributeValue(null, "stroke-linejoin")

        return Paint(Paint.ANTI_ALIAS_FLAG).apply {
            if (stroke != null && stroke != "none" && strokeWidth > 0) {
                style = Paint.Style.STROKE
                this.strokeWidth = strokeWidth
                strokeCap =
                    when (strokeLinecap) {
                        "round" -> Paint.Cap.ROUND
                        "square" -> Paint.Cap.SQUARE
                        else -> Paint.Cap.BUTT
                    }
                strokeJoin =
                    when (strokeLinejoin) {
                        "round" -> Paint.Join.ROUND
                        "bevel" -> Paint.Join.BEVEL
                        else -> Paint.Join.MITER
                    }
            } else {
                style = Paint.Style.STROKE
                this.strokeWidth = 0.5f
                strokeJoin = Paint.Join.ROUND
                strokeCap = Paint.Cap.ROUND
            }
            color = android.graphics.Color.BLACK
        }
    }

    private fun parsePolygonPoints(
        points: String,
        close: Boolean,
    ): Path {
        val coords = points.trim().split(Regex("\\s+|,")).mapNotNull { it.toFloatOrNull() }
        val path = Path()

        if (coords.size >= 2) {
            path.moveTo(coords[0], coords[1])
            for (i in 2 until coords.size step 2) {
                if (i + 1 < coords.size) {
                    path.lineTo(coords[i], coords[i + 1])
                }
            }
            if (close) {
                path.close()
            }
        }

        return path
    }

    private fun buildRegion(path: Path): Region {
        val bounds = RectF()
        path.computeBounds(bounds, true)
        return Region().apply {
            setPath(
                path,
                Region(
                    bounds.left.toInt(),
                    bounds.top.toInt(),
                    bounds.right.toInt(),
                    bounds.bottom.toInt(),
                ),
            )
        }
    }

    private fun parseTransform(
        transform: String,
        matrix: Matrix,
    ) {
        val regex = Regex("""(\w+)\(([^)]+)\)""")
        regex.findAll(transform).forEach {
            val type = it.groupValues[1]
            val values = it.groupValues[2].split(",", " ").map { v -> v.toFloat() }

            when (type) {
                "translate" -> matrix.postTranslate(values[0], values.getOrElse(1) { 0f })
                "scale" -> matrix.postScale(values[0], values.getOrElse(1) { values[0] })
                "rotate" -> matrix.postRotate(values[0])
            }
        }
    }

    private fun resolveFillColor(xml: XmlPullParser): Int? {
        val fillValue = xml.getAttributeValue(null, "fill")
        return if (fillValue != null) {
            parseColorValue(fillValue)
        } else {
            null
        }
    }

    private fun parseColorValue(fillValue: String): Int =
        try {
            when {
                fillValue == "none" -> {
                    android.graphics.Color.TRANSPARENT
                }

                fillValue.startsWith("#") -> {
                    android.graphics.Color.parseColor(fillValue)
                }

                fillValue.startsWith("rgb(") -> {
                    val values =
                        fillValue
                            .substringAfter("(")
                            .substringBefore(")")
                            .split(",")
                            .map { it.trim().toInt() }
                    android.graphics.Color.rgb(values[0], values[1], values[2])
                }

                else -> {
                    when (fillValue.lowercase()) {
                        "white" -> android.graphics.Color.WHITE
                        "black" -> android.graphics.Color.BLACK
                        "red" -> android.graphics.Color.RED
                        "green" -> android.graphics.Color.GREEN
                        "blue" -> android.graphics.Color.BLUE
                        else -> android.graphics.Color.WHITE
                    }
                }
            }
        } catch (e: Exception) {
            android.graphics.Color.WHITE
        }

    private fun assignLayerNumbers(groups: List<SegmentGroup>) {
        val colorToLayerMap = mutableMapOf<Int, Int>()
        var currentLayer = 1

        groups.forEach { group ->
            group.segments.forEach { segment ->
                val color = segment.originalColor
                if (!colorToLayerMap.containsKey(color) && color != null) {
                    colorToLayerMap[color] = currentLayer++
                }
            }
        }

        groups.forEach { group ->
            group.segments.forEach { segment ->
                val layerNumber = colorToLayerMap[segment.originalColor] ?: 1
                val updatedSegment = segment.copy(layerNumber = layerNumber)
                val index = group.segments.indexOf(segment)
                group.segments[index] = updatedSegment
            }
        }
    }
}
