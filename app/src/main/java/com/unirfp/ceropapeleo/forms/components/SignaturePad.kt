package com.unirfp.ceropapeleo.forms.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.ByteArrayOutputStream

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(200.dp),
    onSignatureChanged: () -> Unit,
    onSignatureCleared: () -> Unit = {},
    onPadReady: (SignaturePadView) -> Unit = {}
) {
    var signatureView by remember { mutableStateOf<SignaturePadView?>(null) }

    Column {
        Text(
            text = "Dibuje su firma aquí:",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        AndroidView(
            modifier = modifier,
            factory = { context ->
                SignaturePadView(context).also { view ->
                    signatureView = view
                    onPadReady(view)

                    view.onSigned = {
                        onSignatureChanged()
                    }

                    view.onCleared = {
                        onSignatureCleared()
                    }
                }
            }
        )

        TextButton(
            onClick = {
                signatureView?.clear()
                onSignatureCleared()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Limpiar firma")
        }
    }
}

class SignaturePadView(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = AndroidColor.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }

    private val borderPaint = Paint().apply {
        color = AndroidColor.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    private val path = AndroidPath()
    private var hasSignature = false

    private var lastX = 0f
    private var lastY = 0f

    var onSigned: (() -> Unit)? = null
    var onCleared: (() -> Unit)? = null

    init {
        setBackgroundColor(AndroidColor.WHITE)
    }

    override fun onDraw(canvas: AndroidCanvas) {
        super.onDraw(canvas)

        // Caja visual de firma SOLO en la app
        val padding = 16f
        val left = padding
        val top = padding
        val right = width - padding
        val bottom = height - padding

        canvas.drawRect(left, top, right, bottom, borderPaint)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val padding = 16f
        val minX = padding
        val minY = padding
        val maxX = width - padding
        val maxY = height - padding

        val x = event.x.coerceIn(minX, maxX)
        val y = event.y.coerceIn(minY, maxY)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                path.moveTo(x, y)
                lastX = x
                lastY = y
                hasSignature = true
            }

            MotionEvent.ACTION_MOVE -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                val midX = (lastX + x) / 2f
                val midY = (lastY + y) / 2f
                path.quadTo(lastX, lastY, midX, midY)
                lastX = x
                lastY = y
            }

            MotionEvent.ACTION_UP -> {
                parent?.requestDisallowInterceptTouchEvent(false)
                path.lineTo(x, y)
                onSigned?.invoke()
            }

            MotionEvent.ACTION_CANCEL -> {
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }

        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        hasSignature = false
        invalidate()
        onCleared?.invoke()
    }

    fun hasSignature(): Boolean = hasSignature

    fun exportSignatureBase64(): String? {
        if (!hasSignature || width <= 0 || height <= 0) return null

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)

        // Fondo transparente
        canvas.drawColor(AndroidColor.TRANSPARENT, android.graphics.PorterDuff.Mode.CLEAR)

        // La caja NO se exporta al PDF, solo la firma
        canvas.drawPath(path, paint)

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val bytes = outputStream.toByteArray()

        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }
}