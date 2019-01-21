package com.example.chris.waveanimation

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.Paint.ANTI_ALIAS_FLAG
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.max

class WavesView
@JvmOverloads
constructor(context: Context, attr: AttributeSet? = null, defStyleAttr: Int = R.attr.WavesViewStyle)
    : View(context, attr, defStyleAttr) {

    private var waveAnimator: ValueAnimator? = null
    private var waveRadiusOffset = 0f
        set(value) {
            field = value
            postInvalidateOnAnimation()
        }

    private val wavePaint: Paint
//    private val gradientPaint: Paint
    private val waveGap: Float
    private val wavePath = Path()
    private val type: Int

    private var maxRadius = 0f
    private var center = PointF(0f, 0f)
    private var initialRadius = 0f

    private val green = Color.GREEN
    private val gradientColor = intArrayOf(green, modifyAlpha(green, 100), modifyAlpha(green, 70))

    init {
        val attrs = context.obtainStyledAttributes(attr, R.styleable.WavesView, defStyleAttr, 0)

        wavePaint = Paint(ANTI_ALIAS_FLAG).apply {
            color = attrs.getColor(R.styleable.WavesView_waveColor, 0)
            strokeWidth = attrs.getDimension(R.styleable.WavesView_waveStrokeWidth, 0f)
            style = Paint.Style.STROKE
        }

//        gradientPaint = Paint(ANTI_ALIAS_FLAG).apply {
//            // Highlight only the areas already touched on the canvas
//            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//        }

        waveGap = attrs.getDimension(R.styleable.WavesView_waveGap, 50f)
        type = attrs.getInt(R.styleable.WavesView_waveTypes, 0)

        attrs.recycle()
    }

    private fun modifyAlpha(color: Int, alpha: Int) : Int {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    //region animations
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        waveAnimator = ValueAnimator.ofFloat(0f, waveGap).apply {
            addUpdateListener {
                waveRadiusOffset = it.animatedValue as Float
            }
            duration = 1500L
            repeatMode = ValueAnimator.RESTART
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }
    }

    override fun onDetachedFromWindow() {
        waveAnimator?.cancel()
        super.onDetachedFromWindow()
    }
    //endregion

    //Star path
    private fun createStarPath(radius: Float, path: Path = Path(), points: Int = 20) : Path {
        path.reset()

        // difference between the "far" and "close" points from the center
        val pointDelta = 0.7f
        // essentially 360/20 or 18 degrees, angle each line should be drawn (20 Points to draw)
        val angleInRadians = 2.0 * Math.PI / points
        val startAngleInRadian = 0.0

        path.moveTo(center.x + (radius * pointDelta * Math.cos(startAngleInRadian)).toFloat(),
            center.y + (radius * pointDelta * Math.sin(startAngleInRadian).toFloat()))

        for (i in 1 until points) {
            val hypotenuse = if (i % 2 == 0) {
                //by reducing the distance from the circle every other points, we create the "dip" in the star
                pointDelta * radius
            } else {
                radius
            }

            val nextPointX = center.x + (hypotenuse * Math.cos(startAngleInRadian - angleInRadians * i)).toFloat()
            val nextPointY = center.y + (hypotenuse * Math.sin(startAngleInRadian - angleInRadians * i)).toFloat()

            path.lineTo(nextPointX, nextPointY)
        }

        path.close()

        return path
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        center.set(w / 2f, h / 2f)
        maxRadius = Math.hypot(center.x.toDouble(), center.y.toDouble()).toFloat()

        initialRadius = w / waveGap

//        gradientPaint.shader = RadialGradient(center.x, center.y, maxRadius, gradientColor, null, Shader.TileMode.CLAMP)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        var currentRadius = initialRadius + waveRadiusOffset

        while (currentRadius < maxRadius) {
            if (type == 0) {
                canvas.drawCircle(center.x, center.y, currentRadius, wavePaint)
            } else {
                val  path = createStarPath(currentRadius, wavePath)
                canvas.drawPath(path, wavePaint)
            }

//            canvas.drawPaint(gradientPaint)

            currentRadius += waveGap
        }
    }
}