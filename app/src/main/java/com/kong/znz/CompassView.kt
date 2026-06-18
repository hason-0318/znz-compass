 package com.kong.znz
 
 import android.animation.ObjectAnimator
 import android.animation.PropertyValuesHolder
 import android.content.Context
 import android.graphics.*
 import android.util.AttributeSet
 import android.view.View
 import android.view.animation.DecelerateInterpolator
 import kotlin.math.cos
 import kotlin.math.min
 import kotlin.math.sin
 import kotlin.math.PI
 
 class CompassView @JvmOverloads constructor(
     context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
 ) : View(context, attrs, defStyleAttr) {
 
     private var currentBearing = 0f
     private var targetBearing = 0f
 
     private val paintRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         style = Paint.Style.STROKE
         strokeWidth = 6f
         color = 0xFF4A4A4A.toInt()
     }
     private val paintOuterRing = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         style = Paint.Style.STROKE
         strokeWidth = 3f
         color = 0xFFBDBDBD.toInt()
     }
     private val paintMark = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         style = Paint.Style.STROKE
         strokeWidth = 2f
         color = 0xFF666666.toInt()
     }
     private val paintMarkMajor = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         style = Paint.Style.STROKE
         strokeWidth = 4f
         color = 0xFF1A1A1A.toInt()
     }
     private val paintText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         textAlign = Paint.Align.CENTER
         color = 0xFF1A1A1A.toInt()
         textSize = 36f
         typeface = Typeface.DEFAULT_BOLD
     }
     private val paintDegreeText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         textAlign = Paint.Align.CENTER
         color = 0xFF666666.toInt()
         textSize = 18f
     }
     private val paintNeedleNorth = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         color = 0xFFD32F2F.toInt()
         style = Paint.Style.FILL
     }
     private val paintNeedleSouth = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         color = 0xFFFFFFFF.toInt()
         style = Paint.Style.FILL
     }
     private val paintNeedleBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         color = 0xFF666666.toInt()
         style = Paint.Style.STROKE
         strokeWidth = 1.5f
     }
     private val paintInnerCircle = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         color = 0xFFE0E0E0.toInt()
         style = Paint.Style.FILL
     }
     private val paintCenterDot = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         color = 0xFF1A237E.toInt()
         style = Paint.Style.FILL
     }
     private val paintRose = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         style = Paint.Style.FILL
     }
     private val paintRoseOutline = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         style = Paint.Style.STROKE
         strokeWidth = 2f
         color = 0xFF333333.toInt()
     }
     private val paintBackground = Paint(Paint.ANTI_ALIAS_FLAG).apply {
         style = Paint.Style.FILL
         color = 0xFFFFF8F0.toInt()
     }
 
     private var cx = 0f
     private var cy = 0f
     private var radius = 0f
 
     private val animator = ObjectAnimator.ofPropertyValuesHolder(
         this,
         PropertyValuesHolder.ofFloat("currentBearing", 0f, 0f)
     ).apply {
         duration = 300
         interpolator = DecelerateInterpolator()
     }
 
     fun setBearing(bearing: Float) {
         val old = targetBearing
         targetBearing = bearing
         val delta = shortestAngleDelta(old, bearing)
         animator.setValues(
             PropertyValuesHolder.ofFloat("currentBearing", currentBearing, currentBearing + delta)
         )
         animator.start()
     }
 
     fun getCurrentBearing(): Float = currentBearing
     fun setCurrentBearing(v: Float) {
         currentBearing = v % 360
         invalidate()
     }
 
     private fun shortestAngleDelta(from: Float, to: Float): Float {
         var d = to - from
         while (d > 180) d -= 360
         while (d < -180) d += 360
         return d
     }
 
     override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
         super.onSizeChanged(w, h, oldW, oldH)
         cx = w / 2f
         cy = h / 2f
         radius = min(cx, cy) * 0.82f
         paintText.textSize = radius * 0.13f
         paintDegreeText.textSize = radius * 0.065f
         paintRing.strokeWidth = radius * 0.025f
         paintOuterRing.strokeWidth = radius * 0.012f
     }
 
     override fun onDraw(canvas: Canvas) {
         super.onDraw(canvas)
         drawBackground(canvas)
         canvas.save()
         canvas.rotate(currentBearing, cx, cy)
         drawRing(canvas)
         drawDegreeMarks(canvas)
         drawCardinalText(canvas)
         canvas.restore()
         drawRose(canvas)
         drawNeedle(canvas)
     }
 
     private fun drawBackground(canvas: Canvas) {
         val r = radius + radius * 0.18f
         canvas.drawCircle(cx, cy, r, paintBackground)
         canvas.drawCircle(cx, cy, r, paintOuterRing)
     }
 
     private fun drawRing(canvas: Canvas) {
         canvas.drawCircle(cx, cy, radius, paintRing)
     }
 
     private fun drawDegreeMarks(canvas: Canvas) {
         for (deg in 0 until 360) {
             val rad = Math.toRadians(deg.toDouble())
             val isMajor = deg % 90 == 0
             val isMid = deg % 30 == 0
             val len = when {
                 isMajor -> radius * 0.12f
                 isMid -> radius * 0.08f
                 else -> radius * 0.05f
             }
             val inner = radius - radius * 0.05f
             val outer = inner - len
             val p = if (isMajor || isMid) paintMarkMajor else paintMark
             canvas.drawLine(
                 cx + inner * cos(rad).toFloat(),
                 cy + inner * sin(rad).toFloat(),
                 cx + outer * cos(rad).toFloat(),
                 cy + outer * sin(rad).toFloat(),
                 p
             )
             if (isMajor && deg % 30 != 0) {
                 canvas.drawText(
                     "$deg\u00B0",
                     cx + (radius - radius * 0.18f) * cos(rad).toFloat(),
                     cy + (radius - radius * 0.18f) * sin(rad).toFloat() + paintDegreeText.textSize / 3,
                     paintDegreeText
                 )
             }
         }
     }
 
     private fun drawCardinalText(canvas: Canvas) {
         val dirs = arrayOf(
             "北" to 0f, "东" to 90f, "南" to 180f, "西" to 270f
         )
         for ((text, deg) in dirs) {
             val rad = Math.toRadians(deg.toDouble())
             val r = radius * 0.82f
             val save = paintText.color
             paintText.color = if (deg == 0f) 0xFFD32F2F.toInt() else 0xFF1A1A1A.toInt()
             canvas.drawText(
                 text,
                 cx + r * cos(rad).toFloat(),
                 cy + r * sin(rad).toFloat() + paintText.textSize / 3,
                 paintText
             )
             paintText.color = save
         }
     }
 
     private fun drawRose(canvas: Canvas) {
         val r = radius * 0.15f
         for (i in 0 until 8) {
             val angle = Math.toRadians((i * 45f).toDouble())
             val tipX = cx + r * cos(angle).toFloat()
             val tipY = cy + r * sin(angle).toFloat()
             val baseR = r * 0.5f
             val bx1 = cx + baseR * cos(angle + 0.4).toFloat()
             val by1 = cy + baseR * sin(angle + 0.4).toFloat()
             val bx2 = cx + baseR * cos(angle - 0.4).toFloat()
             val by2 = cy + baseR * sin(angle - 0.4).toFloat()
             val path = Path().apply {
                 moveTo(tipX, tipY)
                 lineTo(bx1, by1)
                 lineTo(bx2, by2)
                 close()
             }
             paintRose.color = if (i % 2 == 0) 0xFF37474F.toInt() else 0xFFB0BEC5.toInt()
             canvas.drawPath(path, paintRose)
             canvas.drawPath(path, paintRoseOutline)
         }
         canvas.drawCircle(cx, cy, r * 0.85f, paintInnerCircle)
     }
 
     private fun drawNeedle(canvas: Canvas) {
         val nLen = radius * 0.62f
         val sLen = radius * 0.42f
         val nWidth = radius * 0.06f
         val sWidth = radius * 0.04f
 
         // North needle (red)
         val northPath = Path().apply {
             moveTo(cx, cy - nLen)
             lineTo(cx - nWidth, cy - nLen * 0.1f)
             lineTo(cx - sWidth, cy)
             lineTo(cx + sWidth, cy)
             lineTo(cx + nWidth, cy - nLen * 0.1f)
             close()
         }
         canvas.drawPath(northPath, paintNeedleNorth)
         canvas.drawPath(northPath, paintNeedleBorder)
 
         // South needle (white)
         val southPath = Path().apply {
             moveTo(cx, cy + sLen)
             lineTo(cx - sWidth, cy + sLen * 0.1f)
             lineTo(cx + sWidth, cy + sLen * 0.1f)
             close()
         }
         canvas.drawPath(southPath, paintNeedleSouth)
         canvas.drawPath(southPath, paintNeedleBorder)
 
         canvas.drawCircle(cx, cy, radius * 0.04f, paintCenterDot)
     }
 }
