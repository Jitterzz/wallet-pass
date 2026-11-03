package app.passstudio.ui

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.passstudio.data.Catalog
import app.passstudio.data.LogoStyle
import app.passstudio.data.Pass
import app.passstudio.data.PassTheme
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter

/** The barcode types a pass can show, drawing them, and scanning them off a card. */
object Barcodes {
    val types: List<String> = listOf(
        "CODE_128", "CODE_39", "QR_CODE", "PDF_417", "AZTEC", "DATA_MATRIX", "EAN_13", "EAN_8", "UPC_A", "ITF_14", "CODABAR",
    )

    fun label(type: String): String = when (type) {
        "CODE_128" -> "Code 128"
        "CODE_39" -> "Code 39"
        "QR_CODE" -> "QR code"
        "PDF_417" -> "PDF417"
        "AZTEC" -> "Aztec"
        "DATA_MATRIX" -> "Data Matrix"
        "EAN_13" -> "EAN-13"
        "EAN_8" -> "EAN-8"
        "UPC_A" -> "UPC-A"
        "ITF_14" -> "ITF-14"
        "CODABAR" -> "Codabar"
        else -> type
    }

    fun isSquare(type: String) = type == "QR_CODE" || type == "AZTEC" || type == "DATA_MATRIX"

    private fun zxing(type: String): BarcodeFormat? = when (type) {
        "CODE_128" -> BarcodeFormat.CODE_128
        "CODE_39" -> BarcodeFormat.CODE_39
        "QR_CODE" -> BarcodeFormat.QR_CODE
        "PDF_417" -> BarcodeFormat.PDF_417
        "AZTEC" -> BarcodeFormat.AZTEC
        "DATA_MATRIX" -> BarcodeFormat.DATA_MATRIX
        "EAN_13" -> BarcodeFormat.EAN_13
        "EAN_8" -> BarcodeFormat.EAN_8
        "UPC_A" -> BarcodeFormat.UPC_A
        "ITF_14" -> BarcodeFormat.ITF
        "CODABAR" -> BarcodeFormat.CODABAR
        else -> null
    }

    /** The barcode as a picture, or null when the value doesn't fit the type (like letters in an EAN-13). */
    fun render(value: String, type: String, width: Int = 0, height: Int = 0): ImageBitmap? {
        val format = zxing(type) ?: return null
        val square = isSquare(type)
        // Drawn at the size it shows at, so every bar is a whole number of pixels wide and scans cleanly.
        val w = if (width > 0) width else if (square) 400 else 900
        val h = if (height > 0) height else if (square) 400 else 220
        return try {
            val m = MultiFormatWriter().encode(value, format, w, h, mapOf(EncodeHintType.MARGIN to 0))
            val px = IntArray(m.width * m.height)
            for (y in 0 until m.height) {
                for (x in 0 until m.width) px[y * m.width + x] = if (m.get(x, y)) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
            }
            val bmp = Bitmap.createBitmap(m.width, m.height, Bitmap.Config.ARGB_8888)
            bmp.setPixels(px, 0, m.width, 0, 0, m.width, m.height)
            bmp.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    /** Which type a scanned barcode is. */
    fun typeOf(format: Int, value: String): String = when (format) {
        Barcode.FORMAT_CODE_128 -> "CODE_128"
        Barcode.FORMAT_CODE_39 -> "CODE_39"
        Barcode.FORMAT_QR_CODE -> "QR_CODE"
        Barcode.FORMAT_PDF417 -> "PDF_417"
        Barcode.FORMAT_AZTEC -> "AZTEC"
        Barcode.FORMAT_DATA_MATRIX -> "DATA_MATRIX"
        Barcode.FORMAT_EAN_13 -> "EAN_13"
        Barcode.FORMAT_EAN_8 -> "EAN_8"
        Barcode.FORMAT_UPC_A -> "UPC_A"
        Barcode.FORMAT_ITF -> if (value.length == 14) "ITF_14" else "CODE_128"
        Barcode.FORMAT_CODABAR -> "CODABAR"
        else -> "CODE_128"
    }

    /** Opens Google's barcode scanner (it needs no camera permission) and hands back the value and its type. */
    fun scan(context: Context, onResult: (value: String, type: String) -> Unit, onError: (String) -> Unit) {
        val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
        GmsBarcodeScanning.getClient(context, options).startScan()
            .addOnSuccessListener { barcode ->
                val value = barcode.rawValue.orEmpty()
                if (value.isNotBlank()) onResult(value, typeOf(barcode.format, value))
                else onError("That barcode was empty. Try again, or type the number.")
            }
            .addOnFailureListener { e -> onError(e.message ?: "Couldn't open the scanner.") }
    }
}

/**
 * A small version of the pass page, for the editors: the theme's page art with the logo, card name, name and barcode
 * over its sky, and its scene along the bottom.
 */
@Composable
fun PassFace(pass: Pass, theme: PassTheme, modifier: Modifier = Modifier) {
    val sky = parseHex(Catalog.skyColor(theme.art)) ?: parseHex(theme.color) ?: Color(0xFF1A1A1A)
    val ink = inkOn(sky)
    val shadow = Shadow(
        color = if (ink == Color.White) Color.Black.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f),
        offset = Offset(0f, 2f),
        blurRadius = 10f,
    )
    Box(modifier.fillMaxWidth().aspectRatio(0.8f).clip(RoundedCornerShape(28.dp)).background(sky)) {
        Image(
            painter = painterResource(Catalog.pageRes(theme.art)),
            contentDescription = theme.name,
            contentScale = ContentScale.Crop,
            alignment = Alignment.BottomCenter,
            modifier = Modifier.matchParentSize(),
        )
        Column(Modifier.fillMaxWidth().padding(top = 22.dp, start = 24.dp, end = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            val logoScale = pass.logoSize.coerceIn(0.5f, 1.5f)
            PassLogo(pass, 58.dp * logoScale, DpSize(minOf(200.dp * logoScale, 280.dp), 64.dp * logoScale), ink, 26.sp * logoScale)
            Spacer(Modifier.height(if (pass.logoStyle == LogoStyle.BIG && pass.logo.isNotBlank()) 3.dp else 8.dp))
            CardName(pass.title.ifBlank { "Card name" }, pass.titleColor, pass.executiveName(), ink,
                TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, shadow = shadow), moving = false, maxLines = 1)
            Text(pass.header.ifBlank { "Your name" }, color = ink, maxLines = 1, overflow = TextOverflow.Ellipsis,
                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, shadow = shadow))
            if (pass.subheader.isNotBlank()) {
                Text(pass.subheader, color = ink, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, shadow = shadow))
            }
            Spacer(Modifier.height(12.dp))
            BarcodeBox(pass)
        }
    }
}

@Composable
fun BarcodeBox(pass: Pass) {
    val value = pass.barcodeValue.trim()
    val bitmap = remember(value, pass.barcodeType) { Barcodes.render(value.ifEmpty { "000000000000" }, pass.barcodeType) }
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = "Barcode",
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.None,
                modifier = if (Barcodes.isSquare(pass.barcodeType)) Modifier.size(150.dp) else Modifier.fillMaxWidth().height(76.dp),
            )
        } else {
            Text("This number doesn't fit a ${Barcodes.label(pass.barcodeType)} barcode",
                color = Color(0xFFB3261E), style = MaterialTheme.typography.bodySmall)
        }
        if (pass.showNumber) {
            Text(value.ifEmpty { "Your number" }, color = Color(0xFF3C4043), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * A theme's art filling a whole card, with its color washing over the left side so text there stays readable.
 * [content] goes on top.
 */
@Composable
fun ThemedCard(theme: PassTheme, onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable (ink: Color) -> Unit) {
    val bg = parseHex(theme.color) ?: Color(0xFF1A1A1A)
    val ink = inkOn(bg)
    Box(modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(bg).clickable(onClick = onClick)) {
        Image(
            painter = painterResource(Catalog.artRes(theme.art)),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = BiasAlignment(1f, -0.45f),
            modifier = Modifier.matchParentSize(),
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(
                    0f to bg.copy(alpha = 0.94f),
                    0.45f to bg.copy(alpha = 0.72f),
                    0.8f to bg.copy(alpha = 0.0f),
                )
            )
        )
        content(ink)
    }
}

/** A pass in a list: today's theme filling the card, its logo and name, and which theme it's showing. */
@Composable
fun PassCard(pass: Pass, theme: PassTheme, status: String, onClick: () -> Unit) {
    ThemedCard(theme, onClick, Modifier.heightIn(min = 150.dp)) { ink ->
        Column(
            Modifier.fillMaxWidth(0.68f).padding(start = 18.dp, top = 16.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoundLogo(pass, 30.dp, ink, 14.sp)
                Spacer(Modifier.width(10.dp))
                Text(pass.title.ifBlank { "Card name" }, color = ink, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(pass.header.ifBlank { "Your name" }, color = ink, style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(status, color = ink.copy(alpha = 0.85f), style = MaterialTheme.typography.bodySmall, maxLines = 2)
        }
    }
}
