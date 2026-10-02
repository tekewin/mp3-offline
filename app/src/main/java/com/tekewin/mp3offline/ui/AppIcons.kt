package com.tekewin.mp3offline.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** The app's icon set, drawn from the same 24×24 stroke paths as the design. */
object AppIcons {
    val Search = icon("search") { stroke(circle(11f, 11f, 7f)); stroke("M20 20l-3.5-3.5") }
    val Appearance = icon("appearance") { stroke("M20 14.5A8 8 0 0 1 9.5 4a8 8 0 1 0 10.5 10.5z") }
    val Folder = icon("folder") { stroke("M3 7a2 2 0 0 1 2-2h4l2 2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z") }
    val Refresh = icon("refresh") { stroke("M20 11a8 8 0 1 0-2.3 5.7"); stroke("M20 5v6h-6") }
    val Play = icon("play") { fill("M8 5.5v13l11-6.5z") }
    val Pause = icon("pause") { fill("M6 5h4v14H6z"); fill("M14 5h4v14h-4z") }
    val Next = icon("next") { fill("M6 6l9 6-9 6z"); stroke("M18 6v12") }
    val Previous = icon("previous") { fill("M18 6l-9 6 9 6z"); stroke("M6 6v12") }
    val Shuffle = icon("shuffle") {
        stroke("M4 7h3c4 0 6 10 10 10h3"); stroke("M4 17h3c1.6 0 2.8-1.6 3.9-3.6")
        stroke("M13.1 9.6C14.2 8.1 15.4 7 17 7h3"); stroke("M18 4l3 3-3 3"); stroke("M18 14l3 3-3 3")
    }
    val Repeat = icon("repeat") {
        stroke("M17 3l3 3-3 3"); stroke("M4 12v-1a5 5 0 0 1 5-5h11")
        stroke("M7 21l-3-3 3-3"); stroke("M20 12v1a5 5 0 0 1-5 5H4")
    }
    val RepeatOne = icon("repeat_one") {
        stroke("M17 3l3 3-3 3"); stroke("M4 12v-1a5 5 0 0 1 5-5h11")
        stroke("M7 21l-3-3 3-3"); stroke("M20 12v1a5 5 0 0 1-5 5H4")
        stroke("M11 10.5l1.5-1v5.5", width = 1.8f)
    }
    val Back = icon("back") { stroke("M15 5l-7 7 7 7") }
    val More = icon("more") { fill(circle(12f, 5.5f, 1.8f)); fill(circle(12f, 12f, 1.8f)); fill(circle(12f, 18.5f, 1.8f)) }
    val Plus = icon("plus") { stroke("M12 5v14"); stroke("M5 12h14") }
    val ChevronDown = icon("chevron_down") { stroke("M6 9l6 6 6-6") }
    val PlaylistAdd = icon("playlist_add") {
        stroke("M4 6h11"); stroke("M4 11h11"); stroke("M4 16h7"); stroke("M18 13v7"); stroke("M14.5 16.5h7")
    }
    val Check = icon("check") { stroke("M5 12.5l4.5 4.5L19 7.5", width = 3f) }
    val Music = icon("music") { stroke("M9 18V6l10-2v12"); stroke(circle(6.5f, 18f, 2.5f)); stroke(circle(16.5f, 16f, 2.5f)) }
    val Pencil = icon("pencil") { stroke("M4 20h4L19 9l-4-4L4 16z"); stroke("M13.5 6.5l4 4") }
    val Trash = icon("trash") {
        stroke("M4 7h16"); stroke("M10 11v6"); stroke("M14 11v6"); stroke("M6 7l1 13h10l1-13"); stroke("M9 7V4h6v3")
    }
    val Remove = icon("remove") { stroke(circle(12f, 12f, 9f)); stroke("M8 12h8") }
    val NoWifi = icon("no_wifi") {
        stroke("M2 8.5a15 15 0 0 1 4-2.6"); stroke("M10 4.6A15 15 0 0 1 22 8.5"); stroke("M5 12.5a10 10 0 0 1 4.5-2.4")
        stroke("M15 10.4a10 10 0 0 1 4 2.1"); stroke("M8.5 16a5 5 0 0 1 7 0"); stroke("M12 20h.01"); stroke("M3 3l18 18")
    }
    val Shield = icon("shield") { stroke("M12 3l8 3v6c0 4.5-3.4 8.2-8 9-4.6-.8-8-4.5-8-9V6z"); stroke("M8.5 12l2.5 2.5 4.5-5") }
    val Queue = icon("queue") { stroke("M4 6h16"); stroke("M4 12h16"); stroke("M4 18h10") }
    val Equalizer = icon("equalizer") {
        fill("M4 10.5a1.5 1.5 0 0 1 3 0v7a1.5 1.5 0 0 1-3 0z")
        fill("M10.5 5.5a1.5 1.5 0 0 1 3 0v12a1.5 1.5 0 0 1-3 0z")
        fill("M17 13.5a1.5 1.5 0 0 1 3 0v4a1.5 1.5 0 0 1-3 0z")
    }
    val Close = icon("close") { stroke("M6 6l12 12"); stroke("M18 6L6 18") }
}

private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply(block).build()

private fun ImageVector.Builder.stroke(d: String, width: Float = 2f) {
    addPath(
        pathData = addPathNodes(d),
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
    )
}

private fun ImageVector.Builder.fill(d: String) {
    addPath(pathData = addPathNodes(d), fill = SolidColor(Color.Black))
}

private fun circle(cx: Float, cy: Float, r: Float): String =
    "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"
