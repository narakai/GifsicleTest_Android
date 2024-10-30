import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.doOnEnd
import kotlin.math.absoluteValue
import kotlin.math.min

class LuckyWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isSpinning = false
    private val pointerPath = Path()
    private var targetNumber: Int = 0
    private var debugText: String = "Target: -"

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var centerX = 0f
    private var centerY = 0f
    private var radius = 0f
    private var currentRotation = 0f
    private val sectors = listOf("1", "2", "3", "4", "5", "6")
    private val colors = listOf(
        Color.RED, Color.GREEN, Color.BLUE,
        Color.YELLOW, Color.CYAN, Color.MAGENTA
    )

    init {
        currentRotation = 0f
    }

    private fun setupPointerPath() {
        pointerPath.moveTo(0f, -radius)
        pointerPath.lineTo(-20f, -radius + 50)
        pointerPath.lineTo(20f, -radius + 50)
        pointerPath.close()
    }

    fun spin(targetNumber: Int, duration: Long = 5000) {
        if (isSpinning) {
            return
        }

        this.targetNumber = targetNumber
        debugText = "Target: $targetNumber"
        isSpinning = true

        Log.d(
            "TAG",
            "Spinning to target number: ${targetNumber}"
        )

        if (targetNumber !in 1..6) {
            throw IllegalArgumentException("Target number must be between 1 and 6")
        }

        isSpinning = true

        // 确保当前角度在0-360度范围内
        currentRotation %= 360f

        // 计算当前指针位置对应的扇区索引
        val currentSectorIndex = (((currentRotation.toInt() + 30) / 60 + 5) % 6)
        Log.d("TAG", "currentSectorIndex: $currentSectorIndex")

        // 计算需要旋转的扇区数量
        val sectorsToRotate = (targetNumber - currentSectorIndex + 6) % 6
        Log.d("TAG", "sectorsToRotate: $sectorsToRotate")

        // 计算需要旋转的角度
        var rotationAngle = sectorsToRotate * 60f

        // 加上额外的5圈
        rotationAngle += 360f * 5

        // 设置目标旋转角度
        val targetRotation = currentRotation + rotationAngle

        ValueAnimator.ofFloat(currentRotation, targetRotation).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator(2f)
            addUpdateListener { animation ->
                currentRotation = animation.animatedValue as Float
                invalidate()
            }
            doOnEnd {
                currentRotation = targetRotation % 360f
                isSpinning = false
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Normalize currentRotation to 0-360 range
        val normalizedRotation = currentRotation % 360f
        val effectiveRotation = -normalizedRotation

        // 绘制扇区
        val sectorAngle = 360f / sectors.size
        for (i in sectors.indices) {
            paint.color = colors[i]
            canvas.drawArc(rect, i * sectorAngle + effectiveRotation, sectorAngle, true, paint)
        }

        // 绘制扇区文字
        paint.color = Color.WHITE
        paint.textSize = radius * 0.2f
        paint.textAlign = Paint.Align.CENTER
        for (i in sectors.indices) {
            val angle = (i * sectorAngle + sectorAngle / 2 + effectiveRotation) * Math.PI / 180
            val x = (centerX + radius * 0.6f * Math.cos(angle)).toFloat()
            val y = (centerY + radius * 0.6f * Math.sin(angle)).toFloat() + paint.textSize / 3
            canvas.drawText(sectors[i], x, y, paint)
        }

        // 绘制指针
        paint.color = Color.BLACK
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.drawPath(pointerPath, paint)
        canvas.restore()

        // 绘制调试文本
        paint.color = Color.CYAN
        paint.textSize = 40f
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText(debugText, 20f, height - 20f, paint)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        centerX = w / 2f
        centerY = h / 2f
        radius = min(w, h) / 2f * 0.8f
        rect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
        setupPointerPath()
    }
}