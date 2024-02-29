import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.math.sqrt


@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    var points by remember { mutableStateOf(mutableListOf<Offset>()) }
    MaterialTheme {
        Column() {
            Row(Modifier.width(350.dp)) {
                Button(onClick = { points.clear() }) {
                    Text("Clear", fontSize = 35.sp)
                }
            }
            Row() {
                Canvas(modifier = Modifier.fillMaxSize().clickable {}.onPointerEvent(PointerEventType.Press)
                {
                    var pnt = it.changes.first().position
                    points.add(pnt)
                }) {
                    if (points.isNotEmpty()) {
                        this.drawPoints(points, PointMode.Points, Color.Green, strokeWidth = 7f)
                        var curpnt = points.first()
                        var startpnt = curpnt
                        for (pnt in points) {
                            if (pnt.x > curpnt.x) {
                                curpnt = pnt
                            }
                        }
                        startpnt = curpnt
                        var cosval = -1f
                        var nxtpnt = Offset(0f, 0f)
                        var curnextvec = Offset(1f, 0f)
                        if (points.size >= 3) {
                            for (i in 1..points.size) {
                                cosval = -1f
                                for (pnt in points) {
                                    if (pnt != curpnt) {
                                        if (csn(curnextvec, pnt - curpnt) > cosval) {
                                            cosval = csn(curnextvec, pnt - curpnt)
                                            nxtpnt = pnt
                                        }
                                    }
                                }
                                this.drawLine(Color.Red, curpnt, nxtpnt)
                                curnextvec = nxtpnt - curpnt
                                curpnt = nxtpnt
                            }
                        }
                    }
                }
            }
        }
    }
}

fun csn(a:Offset,b:Offset):Float{
    return (a.x*b.x+a.y*b.y)/sqrt((a.x*a.x+a.y*a.y)*(b.x*b.x+b.y*b.y))
}

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
