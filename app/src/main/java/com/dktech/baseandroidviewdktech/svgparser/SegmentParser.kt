package com.dktech.baseandroidviewdktech.svgparser

import android.content.Context
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Region
import android.util.Xml
import androidx.core.graphics.PathParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.xmlpull.v1.XmlPullParser
import java.util.Stack
import kotlin.math.roundToInt
import androidx.core.graphics.toColorInt
import com.dktech.baseandroidviewdktech.svgparser.model.SVGInfo
import com.dktech.baseandroidviewdktech.svgparser.model.SegmentGroup
import com.dktech.baseandroidviewdktech.svgparser.model.Segments
import java.io.File

class SegmentParser {
    suspend fun parseSVGFile(
        mContext: Context,
        assetFileName: String
    ): SVGInfo {
        var segmentsID: Int = 0
        return runInterruptible(Dispatchers.Default) {
            val inputStream = File(mContext.cacheDir, assetFileName).inputStream()
            val xml = Xml.newPullParser()
            xml.setInput(inputStream, null)
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
                                        currentSegmentGroup.segments.add(segment.copy(id = segmentsID++))
                                    }
                                }
                            }

                            "circle" -> {
                                convertCircle(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++))
                                    }
                                }
                            }

                            "rect" -> {
                                convertRect(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++))
                                    }
                                }
                            }

                            "ellipse" -> {
                                convertEllipse(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++))
                                    }
                                }
                            }

                            "polygon" -> {
                                convertPolygon(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++))
                                    }
                                }
                            }

                            "polyline" -> {
                                convertPolyline(xml, matrixStack)?.let { segment ->
                                    ensureDefaultGroup(groups, currentSegmentGroup)?.let { group ->
                                        currentSegmentGroup = group
                                        currentSegmentGroup?.segments?.add(segment.copy(id = segmentsID++))
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
        val fillColor = resolveFillColor(xml)

        val path = PathParser.createPathFromPathData(d)
        if (matrixStack.isNotEmpty()) {
            path.transform(matrixStack.peek())
        }

        return createSegment(path, fillColor, xml)
    }

    private fun convertCircle(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
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

        return createSegment(path, fillColor, xml)
    }

    private fun convertRect(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
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

        return createSegment( path, fillColor, xml)
    }

    private fun convertEllipse(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
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

        return createSegment(path, fillColor, xml)
    }

    private fun convertPolygon(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
        val points = xml.getAttributeValue(null, "points") ?: return null
        val fillColor = resolveFillColor(xml)

        val path = parsePolygonPoints(points, true)
        if (matrixStack.isNotEmpty()) {
            path.transform(matrixStack.peek())
        }

        return createSegment(path, fillColor, xml)
    }

    private fun convertPolyline(
        xml: XmlPullParser,
        matrixStack: Stack<Matrix>,
    ): Segments? {
        val points = xml.getAttributeValue(null, "points") ?: return null
        val fillColor = resolveFillColor(xml)

        val path = parsePolygonPoints(points, false)
        if (matrixStack.isNotEmpty()) {
            path.transform(matrixStack.peek())
        }

        return createSegment(path, fillColor, xml)
    }

    private fun createSegment(
        path: Path,
        fillColor: Int?,
        xml: XmlPullParser,
    ): Segments {
        val region = buildRegion(path)
        val paint =
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                style = Paint.Style.FILL
                color = fillColor ?: Color.BLACK
            }
//        val strokePaint = createPaintFromSvg(xml)

        return Segments(
            path = path,
            region = region,
            fillPaint = paint,
            originalColor = fillColor,
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
        path.computeBounds(bounds,true)
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
                    Color.TRANSPARENT
                }

                fillValue.startsWith("#") -> {
                    fillValue.toColorInt()
                }

                fillValue.startsWith("rgb(") -> {
                    val values =
                        fillValue
                            .substringAfter("(")
                            .substringBefore(")")
                            .split(",")
                            .map { it.trim().toInt() }
                    Color.rgb(values[0], values[1], values[2])
                }

                else -> {
                    Color.WHITE
                }
            }
        } catch (e: Exception) {
            Color.WHITE
        }


}
