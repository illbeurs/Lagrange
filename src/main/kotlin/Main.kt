import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application


// НУЖНО ДОБАВИТЬ КНОПКУ ДЛЯ ПОДТВЕРЖДЕНИЯ ИЗМЕНЕНИЯ, ЛИБО ПОДСВЕЧИВАТЬ КРАСНЫМ И СОХРАНЯТЬ ПО НАЖАТИЮ НА ENTER
// УМЕНЬШАТЬ ШРИФТ КООРДИНАТ автоматически при масштабировании
// МБ ДОБАВИТЬ ЕЩЁ 2 ИНПУТА ДЛЯ y-ов

@OptIn(ExperimentalTextApi::class, ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class)
@Composable
@Preview
fun App() {
    val textMeasurer = rememberTextMeasurer()
    var xMin by remember { mutableStateOf(-10) }
    var xMax by remember { mutableStateOf(10) }
    var xMinTemporary  by remember { mutableStateOf(xMin.toString()) }
    var xMaxTemporary by remember { mutableStateOf(xMax.toString()) }
    var openDialog by remember { mutableStateOf(false) }
    var textErr by remember { mutableStateOf("") }
    var ySlide by remember { mutableStateOf(0.5f) }
    var points by remember { mutableStateOf(mutableMapOf<Float, Float>()) }
    var flag by remember { mutableStateOf(false) }
    val checkedState = remember { mutableStateOf(false) }
    var zeroOX = 0f
    var zeroOY = 0f
    var segmentLen = 0f
    var bordersFlag by remember { mutableStateOf(false) } // false -- Изменить true -- Подтвердить

    MaterialTheme {
        Column (modifier = Modifier.fillMaxSize()) {
            Row (modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(modifier = Modifier.width(330.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedTextField(
                    modifier = Modifier.defaultMinSize(minWidth = 40.dp),
                    value = xMinTemporary,
                    enabled = bordersFlag,
                    onValueChange = { xMinTemporary = it},
                    label = { Text("Левая граница") })

                OutlinedTextField(
                    modifier = Modifier.defaultMinSize(minWidth = 40.dp),
                    value = xMaxTemporary,
                    enabled = bordersFlag,
                    onValueChange = { xMaxTemporary = it},
                    label = { Text("Правая граница") })
                }

                if (openDialog) {
                    AlertDialog(
                        onDismissRequest = {  },
                        title = { Text(text = "Ошибка!") },
                        text = { Text(textErr) },
                        buttons = {
                            Row(
                                modifier = Modifier.padding(all = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Button(
                                    onClick = { openDialog = false }
                                ) {
                                    Text("Ок")
                                }
                            }
                        }
                    )
                }

                Button(
                    onClick = {
                        if (bordersFlag)
                        {
                            if (xMaxTemporary.toIntOrNull() == null || xMinTemporary.toIntOrNull() == null)
                            {
                                textErr = "Введите целочисленное значение"
                                openDialog = true
                            }
                            else if (xMaxTemporary.toInt() <= xMinTemporary.toInt())
                            {
                                textErr = "Левая граница должна быть меньше правой"
                                openDialog = true
                            }
                            else
                            {
                                xMax = xMaxTemporary.toInt()
                                xMin = xMinTemporary.toInt()
                                bordersFlag = !bordersFlag
                            }
                        }
                        else bordersFlag = !bordersFlag
                    }
                )
                {
                    Text(if (bordersFlag) "Подтвердить изменение" else "Изменить границы")
                }
            }

            Spacer(modifier = Modifier.padding(3.dp))

            Row (modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                Row (modifier = Modifier
                    .border(color = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled), width = 1.dp, shape = MaterialTheme.shapes.small),
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Отрисовка полинома", modifier = Modifier.padding(start = 10.dp))
                    Switch(
                        checked = checkedState.value,
                        onCheckedChange = { checkedState.value = it ; flag = it}
                    )
                }

                Button(
                    onClick =  { points.clear() }
                )
                {
                    Text("Стереть")
                }
            }

            Slider(
                value = ySlide,
                valueRange = 0f ..1.0f,
                onValueChange = { ySlide = it }
            )

            Spacer(modifier = Modifier.size(3.dp))

            Canvas(modifier = Modifier.fillMaxSize()
                .clickable(
//                    interactionSource = MutableInteractionSource(), indication = rememberRipple(
//                    bounded = false,
//                    radius = 0.dp,
//                    color = Color.Green)
                )
                {}
                .onPointerEvent(PointerEventType.Press){
                    var point = it.changes.first().position
                    points[(point.x - zeroOX) / segmentLen] = (point.y - zeroOY) / segmentLen
                },
                onDraw = {
                    segmentLen = this.size.width/(Math.abs(xMax-xMin)+2)

                    zeroOX = segmentLen * (Math.abs(xMin) + 1)
                    zeroOY = this.size.height * ySlide

                    // Ox
                    drawLine(start = Offset(0.0F, zeroOY),
                        end = Offset(this.size.width, zeroOY), color = Color.Black)

                    // Oy
                    drawLine(start = Offset(zeroOX, 0F),
                        end = Offset(zeroOX, this.size.height), color = Color.Black)

                    // Координаты по Ox
                    for (i in xMin .. xMax){
                        if (zeroOY <= this.size.height - 20f)
                        {
                            drawText(textMeasurer = textMeasurer,
                                text = i.toString(),
                                topLeft = Offset((i-xMin+1) * segmentLen + 3f, zeroOY))
                        }
                        else
                        {
                            drawText(textMeasurer = textMeasurer,
                                text = i.toString(),
                                topLeft = Offset((i-xMin+1) * segmentLen + 3f, zeroOY-20f))
                        }


                        // Отрисовка штрихов для координат
                        drawLine(start = Offset((i-xMin+1) * segmentLen, zeroOY + 3f),
                            end = Offset((i-xMin+1) * segmentLen, zeroOY - 3f),
                            color = Color.Black)

                        /*
                            * --ДЛЯ ПРОВЕРКИ--
                            * Координатная сетка

                        drawLine(start = Offset((i-xMin+1)*this.size.width/(xMax-xMin+2), 0f),
                            end = Offset((i-xMin+1)*this.size.width/(xMax-xMin+2), this.size.height),
                            color = Color.Red)
                         */
                    }

                    // Координаты по Oy
                    for(i in 1 ..Math.ceil(this.size.height/(segmentLen).toDouble()).toInt())
                    {
                        // Длина оступа по вертикали
                        // не подходит, нужно менять отступ в зависимости от расположения центра
                        // var a = this.size.height/(segmentLen)/2 - (this.size.height/(segmentLen)/2).toInt()
                        // var centerA = zeroOY - this.size.height/2

                        // Длина оступа по вертикали
                        var aY = zeroOY/(segmentLen) - (zeroOY/(segmentLen)).toInt()
                        // Верхнее число
                        var b = (zeroOY / segmentLen).toInt()


                        /*
                        * Проверка правильного измерения длины отступа
                        drawText(textMeasurer = textMeasurer,
                            text = a.toString(),
                            topLeft = Offset(0f, 0f)
                        )
                        drawLine(start = Offset(segmentLen * (-xMin + 1) - 30f, a * segmentLen),
                            end = Offset(segmentLen * (-xMin + 1) + 30f, a * segmentLen),
                            color = Color.Red)
                        */

                        /*
                        * Рабочая отрисовка палок, без сдвига
                        drawLine(start = Offset(segmentLen * (-xMin + 1) - 3f, (a + i - 1) * segmentLen),
                            end = Offset(segmentLen * (-xMin + 1) + 3f, (a + i - 1) * segmentLen),
                            color = Color.Black)
                        */

                        /*
                        drawLine(start = Offset(segmentLen * (-xMin + 1) - 30f, (a + i - 1) * segmentLen + centerA),
                            end = Offset(segmentLen * (-xMin + 1) + 30f, (a + i - 1) * segmentLen + centerA),
                            color = Color.Red)
                         */

                        drawLine(start = Offset(segmentLen * (-xMin + 1) - 3f, (aY + i - 1) * segmentLen),
                            end = Offset(segmentLen * (-xMin + 1) + 3f, (aY + i - 1) * segmentLen),
                            color = Color.Black)

                        if (0 <= (aY + i - 1) * segmentLen && (aY + i - 1) * segmentLen <= this.size.height)
                        {
                            drawText(textMeasurer = textMeasurer,
                                text = (b-i+1).toString(),
                                topLeft = Offset(segmentLen * (-xMin + 1) + 3f, (aY + i - 1) * segmentLen)
                            )
                        }

/*
                        drawText(textMeasurer = textMeasurer,
                            text = i.toString(),
                            topLeft = Offset(this.size.width/(xMax-xMin+2) * (-xMin + 1) + 3f, zeroOY - i*this.size.width/(xMax-xMin+2))
                        )
                        drawText(textMeasurer = textMeasurer,
                            text = (-i).toString(),
                            topLeft = Offset(this.size.width/(xMax-xMin+2) * (-xMin + 1) + 3f, zeroOY + i*this.size.width/(xMax-xMin+2))
                        )*/
                        // Отрисовка штрихов для координат
                        /*drawLine(start = Offset(segmentLen * (-xMin + 1) - 3f, zeroOY + i * segmentLen),
                                 end = Offset(segmentLen * (-xMin + 1) + 3f, zeroOY + i * segmentLen),
                                 color = Color.Black)
                        drawLine(start = Offset(segmentLen * (-xMin + 1) - 3f, zeroOY - i * segmentLen),
                                 end = Offset(segmentLen * (-xMin + 1) + 3f, zeroOY - i * segmentLen),
                                 color = Color.Black)*/

                        /*
                            * --ДЛЯ ПРОВЕРКИ--
                            * Координатная сетка

                        drawLine(start = Offset(0f, this.size.height/2 + i*this.size.width/(xMax-xMin+2)),
                            end = Offset(this.size.width, this.size.height/2 + i*this.size.width/(xMax-xMin+2)),
                            color = Color.Red)
                        drawLine(start = Offset(0f, this.size.height/2 - i*this.size.width/(xMax-xMin+2)),
                            end = Offset(this.size.width, this.size.height/2 - i*this.size.width/(xMax-xMin+2)),
                            color = Color.Red)
                         */
                    }

                    if (flag && points.size != 0){
                        for (i in 0..this.size.width.toInt()-1){
                            drawLine(color = Color.Green,
                                strokeWidth = 5f,
                                start = Offset(i.toFloat(), Lagrange(points, (i - zeroOX) / segmentLen, segmentLen, zeroOX, zeroOY)),
                                end = Offset((i+1).toFloat(), Lagrange(points, (i + 1 - zeroOX) / segmentLen, segmentLen, zeroOX, zeroOY)))
//                            drawCircle(color = Color.Green, radius = 3f, center = Offset(i.toFloat(), Lagrange(points, i.toFloat())))
                        }
                    }


                    for (point in points){
                        drawCircle(color = Color.Red, radius = 3f, center = Offset(point.key * segmentLen + zeroOX, point.value * segmentLen + zeroOY))
                    }
            })
        }
    }

}



fun l(k: Int, x: Float, points: MutableMap<Float, Float>, segmentLen: Float, zeroOX: Float) : Float{
    var total = 1f
    var mas_x = points.keys.toList()

    for (i in (1..points.size).filter { it != k })
    {
        total *= (((x - mas_x[i-1]) * segmentLen) / ((mas_x[k-1] - mas_x[i-1]) * segmentLen))
    }

    return total
}

fun Lagrange(points: MutableMap<Float, Float>, x: Float, segmentLen: Float, zeroOX: Float, zeroOY: Float) : Float{
    var total = 0f
    var mas_y = points.values.toList()

    for (i in 1..mas_y.size){
        total += (mas_y[i-1] * segmentLen + zeroOY) * l(i, x, points, segmentLen, zeroOX)
    }

    return total
}


fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
