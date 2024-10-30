import android.content.Context
import android.graphics.*
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.AttributeSet
import android.view.View
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toBitmap

class GlassEffectView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var effectBitmap: Bitmap? = null
    private var backgroundBitmap: Bitmap? = null
    private val verticalLines = mutableListOf<Float>()
    private val spacing = 155f
    private val blurRadius = 25f
    private val lineStartColor = Color.WHITE
    private val lineEndColor = Color.GRAY
    private val lineAlpha = 180
    private val lineWidthRatio = 0.1f // 线宽为分割块宽度的五分之一

    init {
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    fun setBackgroundImage(drawable: Drawable) {
        backgroundBitmap = drawable.toBitmap()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        effectBitmap?.recycle()
        effectBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        verticalLines.clear()
        var x = 0f
        while (x < w) {
            verticalLines.add(x)
            x += spacing
        }

        backgroundBitmap?.let { original ->
            val scaled = Bitmap.createScaledBitmap(original, w, h, true)
            if (scaled != original) {
                backgroundBitmap = scaled
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        backgroundBitmap?.let { background ->
            val blurredBitmap = blurBitmap(background, blurRadius)
            canvas.drawBitmap(blurredBitmap, 0f, 0f, null)
            blurredBitmap.recycle()
        }

        effectBitmap?.let { bmp ->
            val bmpCanvas = Canvas(bmp)
            bmpCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)

            val lineWidth = spacing * lineWidthRatio
            val gradient = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                lineStartColor, lineEndColor,
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            paint.alpha = lineAlpha

            for (x in verticalLines) {
                val rect = RectF(x - lineWidth / 2, 0f, x + lineWidth / 2, height.toFloat())
                bmpCanvas.drawRect(rect, paint)
            }

            canvas.drawBitmap(bmp, 0f, 0f, null)
        }
    }

    private fun blurBitmap(source: Bitmap, radius: Float): Bitmap {
        val outputBitmap = Bitmap.createBitmap(source)
        val renderScript = RenderScript.create(context)
        val input = Allocation.createFromBitmap(renderScript, source)
        val output = Allocation.createFromBitmap(renderScript, outputBitmap)
        val scriptIntrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        scriptIntrinsicBlur.setRadius(radius)
        scriptIntrinsicBlur.setInput(input)
        scriptIntrinsicBlur.forEach(output)
        output.copyTo(outputBitmap)
        renderScript.destroy()
        return outputBitmap
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        effectBitmap?.recycle()
        effectBitmap = null
        backgroundBitmap?.recycle()
        backgroundBitmap = null
    }
}