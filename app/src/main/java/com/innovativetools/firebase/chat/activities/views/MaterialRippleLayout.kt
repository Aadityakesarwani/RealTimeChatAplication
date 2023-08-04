package com.innovativetools.firebase.chat.activities.views

import android.animation.Animator
import kotlin.jvm.JvmOverloads
import android.widget.FrameLayout
import android.graphics.drawable.Drawable
import android.widget.AdapterView
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View.OnLongClickListener
import android.view.animation.LinearInterpolator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.res.Resources
import android.view.animation.AccelerateInterpolator
import android.view.GestureDetector.SimpleOnGestureListener
import android.content.res.TypedArray
import android.graphics.*
import com.innovativetools.firebase.chat.activities.R
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.util.Property
import android.util.TypedValue
import android.view.*
import android.view.animation.DecelerateInterpolator
import java.lang.NullPointerException
import java.lang.RuntimeException

class MaterialRippleLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bounds = Rect()
    private var rippleColor: Int
    private var rippleOverlay: Boolean
    private var rippleHover: Boolean = false
    private var rippleDiameter: Int
    private var rippleDuration: Int
    private var rippleAlpha: Int
    private var rippleDelayClick: Boolean
    private var rippleFadeDuration: Int
    private var ripplePersistent: Boolean
    private var rippleBackground: Drawable
    private var rippleInAdapter: Boolean
    private var rippleRoundedCorners: Float
    private var radius = 0f
    private var parentAdapter: AdapterView<*>? = null
    private var childView: View? = null
    private var rippleAnimator: AnimatorSet? = null
    private var hoverAnimator: ObjectAnimator? = null
    private var currentCoords = Point()
    private var previousCoords = Point()
//    private val layerType = 0
    private var eventCancelled = false
    private var prepressed = false
    private var positionInAdapter = 0
    private val gestureDetector: GestureDetector
    private var pendingPressEvent: PressedEvent? = null
    fun <T : View?> getChildView(): T? {
        return childView as T?
    }

    override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
        check(childCount <= 0) { "MaterialRippleLayout can host only one child" }
        childView = child
        super.addView(child, index, params)
    }

    override fun setOnClickListener(onClickListener: OnClickListener?) {
        checkNotNull(childView) { "MaterialRippleLayout must have a child view to handle clicks" }
        childView!!.setOnClickListener(onClickListener)
    }

    override fun setOnLongClickListener(onClickListener: OnLongClickListener?) {
        checkNotNull(childView) { "MaterialRippleLayout must have a child view to handle clicks" }
        childView!!.setOnLongClickListener(onClickListener)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return !findClickableViewInChild(childView, event.x.toInt(), event.y.toInt())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val superOnTouchEvent = super.onTouchEvent(event)
        if (!isEnabled || !childView!!.isEnabled) return superOnTouchEvent
        val isEventInBounds = bounds.contains(event.x.toInt(), event.y.toInt())
        if (isEventInBounds) {
            previousCoords[currentCoords.x] = currentCoords.y
            currentCoords[event.x.toInt()] = event.y.toInt()
        }
        val gestureResult = gestureDetector.onTouchEvent(event)
        if (!gestureResult && !hasPerformedLongPress) {
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_UP -> {
                    val pendingClickEvent: PerformClickEvent = PerformClickEvent()
                    if (prepressed) {
                        childView!!.isPressed = true
                        postDelayed(
                            { childView!!.isPressed = false },
                            ViewConfiguration.getPressedStateDuration().toLong()
                        )
                    }
                    if (isEventInBounds) {
                        startRipple(pendingClickEvent)
                    } else if (!rippleHover) {
                        setRadius(0f)
                    }
                    if (!rippleDelayClick && isEventInBounds) {
                        pendingClickEvent.run()
                    }
                    cancelPressedEvent()
                }
                MotionEvent.ACTION_DOWN -> {
                    setPositionInAdapter()
                    eventCancelled = false
                    pendingPressEvent = PressedEvent(event)
                    if (isInScrollingContainer) {
                        cancelPressedEvent()
                        prepressed = true
                        postDelayed(pendingPressEvent, ViewConfiguration.getTapTimeout().toLong())
                    } else {
                        pendingPressEvent!!.run()
                    }
                }
                MotionEvent.ACTION_CANCEL -> {
                    if (rippleInAdapter) {
                        // don't use current coords in adapter since they tend to jump drastically on scroll
                        currentCoords[previousCoords.x] = previousCoords.y
                        previousCoords = Point()
                    }
                    childView!!.onTouchEvent(event)
                    if (rippleHover) {
                        if (!prepressed) {
                            startRipple(null)
                        }
                    } else {
                        childView!!.isPressed = false
                    }
                    cancelPressedEvent()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (rippleHover) {
                        if (isEventInBounds && !eventCancelled) {
                            invalidate()
                        } else if (!isEventInBounds) {
                            startRipple(null)
                        }
                    }
                    if (!isEventInBounds) {
                        cancelPressedEvent()
                        if (hoverAnimator != null) {
                            hoverAnimator!!.cancel()
                        }
                        childView!!.onTouchEvent(event)
                        eventCancelled = true
                    }
                }
            }
        }
        return true
    }

    private fun cancelPressedEvent() {
        if (pendingPressEvent != null) {
            removeCallbacks(pendingPressEvent)
            prepressed = false
        }
    }

    private var hasPerformedLongPress = false
    private fun startHover() {
        if (eventCancelled) return
        if (hoverAnimator != null) {
            hoverAnimator!!.cancel()
        }
        val radius = (Math.sqrt(
            Math.pow(width.toDouble(), 2.0) + Math.pow(
                height.toDouble(), 2.0
            )
        ) * 1.2f).toFloat()
        hoverAnimator =
            ObjectAnimator.ofFloat(this, radiusProperty, rippleDiameter.toFloat(), radius)
                .setDuration(HOVER_DURATION)
        hoverAnimator!!.interpolator = LinearInterpolator()
        hoverAnimator!!.start()
    }

    private fun startRipple(animationEndRunnable: Runnable?) {
        if (eventCancelled) return
        val endRadius = endRadius
        cancelAnimations()
        rippleAnimator = AnimatorSet()
        rippleAnimator!!.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!ripplePersistent) {
                    setRadius(0f)
                    setRippleAlpha(rippleAlpha)
                }
                if (animationEndRunnable != null && rippleDelayClick) {
                    animationEndRunnable.run()
                }
                childView!!.isPressed = false
            }
        })
        val ripple = ObjectAnimator.ofFloat(this, radiusProperty, radius, endRadius)
        ripple.duration = rippleDuration.toLong()
        ripple.interpolator = DecelerateInterpolator()
        val fade = ObjectAnimator.ofInt(this, circleAlphaProperty, rippleAlpha, 0)
        fade.duration = rippleFadeDuration.toLong()
        fade.interpolator = AccelerateInterpolator()
        fade.startDelay = (rippleDuration - rippleFadeDuration - FADE_EXTRA_DELAY).toLong()
        if (ripplePersistent) {
            rippleAnimator!!.play(ripple)
        } else if (getRadius() > endRadius) {
            fade.startDelay = 0
            rippleAnimator!!.play(fade)
        } else {
            rippleAnimator!!.playTogether(ripple, fade)
        }
        rippleAnimator!!.start()
    }

    private fun cancelAnimations() {
        if (rippleAnimator != null) {
            rippleAnimator!!.cancel()
            rippleAnimator!!.removeAllListeners()
        }
        if (hoverAnimator != null) {
            hoverAnimator!!.cancel()
        }
    }

    private val endRadius: Float
        private get() {
            val width = width
            val height = height
            val halfWidth = width / 2
            val halfHeight = height / 2
            val radiusX =
                if (halfWidth > currentCoords.x) (width - currentCoords.x).toFloat() else currentCoords.x.toFloat()
            val radiusY =
                if (halfHeight > currentCoords.y) (height - currentCoords.y).toFloat() else currentCoords.y.toFloat()
            return Math.sqrt(Math.pow(radiusX.toDouble(), 2.0) + Math.pow(radiusY.toDouble(), 2.0))
                .toFloat() * 1.2f
        }
    private val isInScrollingContainer: Boolean
        private get() {
            var p = parent
            while (p is ViewGroup) {
                if (p.shouldDelayChildPressedState()) {
                    return true
                }
                p = p.getParent()
            }
            return false
        }

    private fun findParentAdapterView(): AdapterView<*>? {
        if (parentAdapter != null) {
            return parentAdapter
        }
        var current = parent
        while (true) {
            if (current is AdapterView<*>) {
                parentAdapter = current
                return parentAdapter
            } else {
                current = try {
                    current.parent
                } catch (npe: NullPointerException) {
                    throw RuntimeException("Could not find a parent AdapterView")
                }
            }
        }
    }

    private fun setPositionInAdapter() {
        if (rippleInAdapter) {
            positionInAdapter =
                findParentAdapterView()!!.getPositionForView(this@MaterialRippleLayout)
        }
    }

    private fun adapterPositionChanged(): Boolean {
        if (rippleInAdapter) {
            val newPosition =
                findParentAdapterView()!!.getPositionForView(this@MaterialRippleLayout)
            val changed = newPosition != positionInAdapter
            positionInAdapter = newPosition
            if (changed) {
                cancelPressedEvent()
                cancelAnimations()
                childView!!.isPressed = false
                setRadius(0f)
            }
            return changed
        }
        return false
    }

    private fun findClickableViewInChild(view: View?, x: Int, y: Int): Boolean {
        if (view is ViewGroup) {
            val viewGroup = view
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)
                val rect = Rect()
                child.getHitRect(rect)
                val contains = rect.contains(x, y)
                if (contains) {
                    return findClickableViewInChild(child, x - rect.left, y - rect.top)
                }
            }
        } else if (view !== childView) {
            return view!!.isEnabled && (view.isClickable || view.isLongClickable || view.isFocusableInTouchMode)
        }
        return view!!.isFocusableInTouchMode
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        bounds[0, 0, w] = h
        rippleBackground.bounds = bounds
    }

    override fun isInEditMode(): Boolean {
        return true
    }

    /*
     * Drawing
     */
    override fun draw(canvas: Canvas) {
        val positionChanged = adapterPositionChanged()
        if (rippleOverlay) {
            if (!positionChanged) {
                rippleBackground.draw(canvas)
            }
            super.draw(canvas)
            if (!positionChanged) {
                if (rippleRoundedCorners != 0f) {
                    val clipPath = Path()
                    val rect = RectF(
                        0F, 0F, canvas.width.toFloat(), canvas.height
                            .toFloat()
                    )
                    clipPath.addRoundRect(
                        rect,
                        rippleRoundedCorners,
                        rippleRoundedCorners,
                        Path.Direction.CW
                    )
                    canvas.clipPath(clipPath)
                }
                canvas.drawCircle(
                    currentCoords.x.toFloat(),
                    currentCoords.y.toFloat(),
                    radius,
                    paint
                )
            }
        } else {
            if (!positionChanged) {
                rippleBackground.draw(canvas)
                canvas.drawCircle(
                    currentCoords.x.toFloat(),
                    currentCoords.y.toFloat(),
                    radius,
                    paint
                )
            }
            super.draw(canvas)
        }
    }

    /*
     * Animations
     */
    private val radiusProperty: Property<MaterialRippleLayout, Float> =
        object : Property<MaterialRippleLayout, Float>(
            Float::class.java, "radius"
        ) {
            override fun get(`object`: MaterialRippleLayout): Float {
                return `object`.getRadius()
            }

            override fun set(`object`: MaterialRippleLayout, value: Float) {
                `object`.setRadius(value)
            }
        }

    private fun getRadius(): Float {
        return radius
    }

    private fun setRadius(radius: Float) {
        this.radius = radius
        invalidate()
    }

    private val circleAlphaProperty: Property<MaterialRippleLayout, Int> =
        object : Property<MaterialRippleLayout, Int>(
            Int::class.java, "rippleAlpha"
        ) {
            override fun get(`object`: MaterialRippleLayout): Int {
                return `object`.getRippleAlpha()
            }

            override fun set(`object`: MaterialRippleLayout, value: Int) {
                `object`.setRippleAlpha(value)
            }
        }

    init {
        setWillNotDraw(false)
        val longClickListener: SimpleOnGestureListener = object : SimpleOnGestureListener() {
            override fun onLongPress(e: MotionEvent) {
                hasPerformedLongPress = childView!!.performLongClick()
                if (hasPerformedLongPress) {
                    if (rippleHover) {
                        startRipple(null)
                    }
                    cancelPressedEvent()
                }
            }

            override fun onDown(e: MotionEvent): Boolean {
                hasPerformedLongPress = false
                return super.onDown(e)
            }
        }
        gestureDetector = GestureDetector(context, longClickListener)
        val a = context.obtainStyledAttributes(attrs, R.styleable.MaterialRippleLayout)
        rippleColor = a.getColor(R.styleable.MaterialRippleLayout_mrl_rippleColor, DEFAULT_COLOR)
        rippleDiameter = a.getDimensionPixelSize(
            R.styleable.MaterialRippleLayout_mrl_rippleDimension, dpToPx(
                resources, DEFAULT_DIAMETER_DP
            ).toInt()
        )
        rippleOverlay =
            a.getBoolean(R.styleable.MaterialRippleLayout_mrl_rippleOverlay, DEFAULT_RIPPLE_OVERLAY)
        rippleHover = a.getBoolean(R.styleable.MaterialRippleLayout_mrl_rippleHover, DEFAULT_HOVER)
        rippleDuration =
            a.getInt(R.styleable.MaterialRippleLayout_mrl_rippleDuration, DEFAULT_DURATION)
        rippleAlpha = (255 * a.getFloat(
            R.styleable.MaterialRippleLayout_mrl_rippleAlpha,
            DEFAULT_ALPHA
        )).toInt()
        rippleDelayClick =
            a.getBoolean(R.styleable.MaterialRippleLayout_mrl_rippleDelayClick, DEFAULT_DELAY_CLICK)
        rippleFadeDuration = a.getInteger(
            R.styleable.MaterialRippleLayout_mrl_rippleFadeDuration,
            DEFAULT_FADE_DURATION
        )
        rippleBackground = ColorDrawable(
            a.getColor(
                R.styleable.MaterialRippleLayout_mrl_rippleBackground,
                DEFAULT_BACKGROUND
            )
        )
        ripplePersistent =
            a.getBoolean(R.styleable.MaterialRippleLayout_mrl_ripplePersistent, DEFAULT_PERSISTENT)
        rippleInAdapter = a.getBoolean(
            R.styleable.MaterialRippleLayout_mrl_rippleInAdapter,
            DEFAULT_SEARCH_ADAPTER
        )
        rippleRoundedCorners = a.getDimensionPixelSize(
            R.styleable.MaterialRippleLayout_mrl_rippleRoundedCorners,
            DEFAULT_ROUNDED_CORNERS
        ).toFloat()
        a.recycle()
        paint.color = rippleColor
        paint.alpha = rippleAlpha
        enableClipPathSupportIfNecessary()
    }

    private fun getRippleAlpha(): Int {
        return paint.alpha
    }

    private fun setRippleAlpha(rippleAlpha: Int) {
        paint.alpha = rippleAlpha
        invalidate()
    }

    /*
     * Accessor
     */
    private fun setRippleColor(rippleColor: Int) {
        this.rippleColor = rippleColor
        paint.color = rippleColor
        paint.alpha = rippleAlpha
        invalidate()
    }

    private fun setRippleOverlay(rippleOverlay: Boolean) {
        this.rippleOverlay = rippleOverlay
    }

    private fun setRippleDiameter(rippleDiameter: Int) {
        this.rippleDiameter = rippleDiameter
    }

    private fun setRippleDuration(rippleDuration: Int) {
        this.rippleDuration = rippleDuration
    }

    private fun setRippleBackground(color: Int) {
        rippleBackground = ColorDrawable(color)
        rippleBackground.bounds = bounds
        invalidate()
    }

    private fun setRippleHover(rippleHover: Boolean) {
        this.rippleHover = rippleHover
    }

    private fun setRippleDelayClick(rippleDelayClick: Boolean) {
        this.rippleDelayClick = rippleDelayClick
    }

    private fun setRippleFadeDuration(rippleFadeDuration: Int) {
        this.rippleFadeDuration = rippleFadeDuration
    }

    private fun setRipplePersistent(ripplePersistent: Boolean) {
        this.ripplePersistent = ripplePersistent
    }

    private fun setRippleInAdapter(rippleInAdapter: Boolean) {
        this.rippleInAdapter = rippleInAdapter
    }

    private fun setRippleRoundedCorners(rippleRoundedCorner: Int) {
        rippleRoundedCorners = rippleRoundedCorner.toFloat()
        enableClipPathSupportIfNecessary()
    }

    private fun setDefaultRippleAlpha(alpha: Int) {
        rippleAlpha = alpha
        paint.alpha = alpha
        invalidate()
    }

    fun performRipple() {
        currentCoords = Point(width / 2, height / 2)
        startRipple(null)
    }

    fun performRipple(anchor: Point) {
        currentCoords = Point(anchor.x, anchor.y)
        startRipple(null)
    }

    /**
     * [Canvas.clipPath] is not supported in hardware accelerated layers
     * before API 18. Use software layer instead
     *
     *
     * https://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported
     */
    private fun enableClipPathSupportIfNecessary() {
        //Commented by Prashant
//        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            if (rippleRoundedCorners != 0) {
//                layerType = getLayerType();
//                setLayerType(LAYER_TYPE_SOFTWARE, null);
//            } else {
//                setLayerType(layerType, null);
//            }
//        }
    }

    /*
     * Helper
     */
    private inner class PerformClickEvent : Runnable {
        override fun run() {
            if (hasPerformedLongPress) return

            // if parent is an AdapterView, try to call its ItemClickListener
            if (parent is AdapterView<*>) {
                clickAdapterView(parent as AdapterView<*>)
            } else if (rippleInAdapter) {
                // find adapter view
                clickAdapterView(findParentAdapterView())
            } else {
                // otherwise, just perform click on child
                childView!!.performClick()
            }
        }

        private fun clickAdapterView(parent: AdapterView<*>?) {
            val position = parent!!.getPositionForView(this@MaterialRippleLayout)
            val itemId = if (parent.adapter != null) parent.adapter.getItemId(position) else 0
            if (position != AdapterView.INVALID_POSITION) {
                parent.performItemClick(this@MaterialRippleLayout, position, itemId)
            }
        }
    }

    private inner class PressedEvent(private val event: MotionEvent) : Runnable {
        override fun run() {
            prepressed = false
            childView!!.isLongClickable =
                false //prevent the child's long click,let's the ripple layout call it's performLongClick
            childView!!.onTouchEvent(event)
            childView!!.isPressed = true
            if (rippleHover) {
                startHover()
            }
        }
    }

    /*
     * Builder
     */
    class RippleBuilder(private val child: View) {
        private val context: Context
        private var rippleColor = DEFAULT_COLOR
        private var rippleOverlay = DEFAULT_RIPPLE_OVERLAY
        private var rippleHover = DEFAULT_HOVER
        private var rippleDiameter = DEFAULT_DIAMETER_DP
        private var rippleDuration = DEFAULT_DURATION
        private var rippleAlpha = DEFAULT_ALPHA
        private var rippleDelayClick = DEFAULT_DELAY_CLICK
        private var rippleFadeDuration = DEFAULT_FADE_DURATION
        private var ripplePersistent = DEFAULT_PERSISTENT
        private var rippleBackground = DEFAULT_BACKGROUND
        private var rippleSearchAdapter = DEFAULT_SEARCH_ADAPTER
        private var rippleRoundedCorner = DEFAULT_ROUNDED_CORNERS.toFloat()

        init {
            context = child.context
        }

        fun rippleColor(color: Int): RippleBuilder {
            rippleColor = color
            return this
        }

        fun rippleOverlay(overlay: Boolean): RippleBuilder {
            rippleOverlay = overlay
            return this
        }

        fun rippleHover(hover: Boolean): RippleBuilder {
            rippleHover = hover
            return this
        }

        fun rippleDiameterDp(diameterDp: Int): RippleBuilder {
            rippleDiameter = diameterDp.toFloat()
            return this
        }

        fun rippleDuration(duration: Int): RippleBuilder {
            rippleDuration = duration
            return this
        }

        fun rippleAlpha(alpha: Float): RippleBuilder {
            rippleAlpha = 255 * alpha
            return this
        }

        fun rippleDelayClick(delayClick: Boolean): RippleBuilder {
            rippleDelayClick = delayClick
            return this
        }

        fun rippleFadeDuration(fadeDuration: Int): RippleBuilder {
            rippleFadeDuration = fadeDuration
            return this
        }

        fun ripplePersistent(persistent: Boolean): RippleBuilder {
            ripplePersistent = persistent
            return this
        }

        fun rippleBackground(color: Int): RippleBuilder {
            rippleBackground = color
            return this
        }

        fun rippleInAdapter(inAdapter: Boolean): RippleBuilder {
            rippleSearchAdapter = inAdapter
            return this
        }

        fun rippleRoundedCorners(radiusDp: Int): RippleBuilder {
            rippleRoundedCorner = radiusDp.toFloat()
            return this
        }

        fun create(): MaterialRippleLayout {
            val layout = MaterialRippleLayout(context)
            layout.setRippleColor(rippleColor)
            layout.setDefaultRippleAlpha(rippleAlpha.toInt())
            layout.setRippleDelayClick(rippleDelayClick)
            layout.setRippleDiameter(dpToPx(context.resources, rippleDiameter).toInt())
            layout.setRippleDuration(rippleDuration)
            layout.setRippleFadeDuration(rippleFadeDuration)
            layout.setRippleHover(rippleHover)
            layout.setRipplePersistent(ripplePersistent)
            layout.setRippleOverlay(rippleOverlay)
            layout.setRippleBackground(rippleBackground)
            layout.setRippleInAdapter(rippleSearchAdapter)
            layout.setRippleRoundedCorners(dpToPx(context.resources, rippleRoundedCorner).toInt())
            val params = child.layoutParams
            val parent = child.parent as ViewGroup
            var index = 0
            check(parent !is MaterialRippleLayout) { "MaterialRippleLayout could not be created: parent of the view already is a MaterialRippleLayout" }
            if (parent != null) {
                index = parent.indexOfChild(child)
                parent.removeView(child)
            }
            layout.addView(
                child,
                ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
            parent?.addView(layout, index, params)
            return layout
        }
    }

    companion object {
        private const val DEFAULT_DURATION = 350
        private const val DEFAULT_FADE_DURATION = 75
        private const val DEFAULT_DIAMETER_DP = 35f
        private const val DEFAULT_ALPHA = 0.2f
        private const val DEFAULT_COLOR = Color.BLACK
        private const val DEFAULT_BACKGROUND = Color.TRANSPARENT
        private const val DEFAULT_HOVER = true
        private const val DEFAULT_DELAY_CLICK = true
        private const val DEFAULT_PERSISTENT = false
        private const val DEFAULT_SEARCH_ADAPTER = false
        private const val DEFAULT_RIPPLE_OVERLAY = false
        private const val DEFAULT_ROUNDED_CORNERS = 0
        private const val FADE_EXTRA_DELAY = 50
        private const val HOVER_DURATION: Long = 2500
        fun on(view: View): RippleBuilder {
            return RippleBuilder(view)
        }

        private fun dpToPx(resources: Resources, dp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                resources.displayMetrics
            )
        }
    }
}