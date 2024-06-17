package com.egiwon.example.flexablecalendarexample

import android.animation.ValueAnimator
import android.util.DisplayMetrics
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.max

class CalendarGestureDetector(
    private val recyclerView: RecyclerView,
    private val onVerticalScrolled: (Boolean) -> Unit,
    private val onHorizontalScrolled: (Boolean) -> Unit
) : SimpleOnGestureListener(), OnTouchListener {

    private val gestureDetector = GestureDetector(recyclerView.context, this)
    private var initialRecyclerViewHeight: Int = 0
    private val maxHeight = recyclerView.context.resources.displayMetrics.heightPixels
    private val minimumHeight = maxHeight / 2
    private var isUpDrag = false

    private var isScrollingVertically = false
    private var isScrollingHorizontally = false
    private var initialX = 0f
    private var initialY = 0f

    private var isScrolled = false

    private var isVerticalAnimating: Boolean = false

    private var horizontalHelper: OrientationHelper? = null
    override fun onTouch(v: View?, event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        if (isScrolled) {
            recyclerView.stopNestedScroll()
            recyclerView.stopScroll()
            return false
        }

        return false
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        recyclerView.performClick()
        Log.e(TAG, "onSingleTapUp:")
        return super.onSingleTapUp(e)
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        recyclerView.performClick()
        Log.e(TAG, "onSingleTapConfirmed:")
        return super.onSingleTapConfirmed(e)
    }

    override fun onDown(e: MotionEvent): Boolean {
        initialX = e.x
        initialY = e.y
        isScrollingVertically = false
        onVerticalScrolled(false)
        isScrollingHorizontally = false
        onHorizontalScrolled(false)
        return true
    }

    override fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        if (!isScrollingHorizontally && !isScrollingVertically) {
            val deltaX = abs(e2.x - initialX)
            val deltaY = abs(e2.y - initialY)
            if (deltaX > deltaY) {
                isScrollingHorizontally = true
                onHorizontalScrolled(true)
            } else {
                isScrollingVertically = true
                onVerticalScrolled(true)
            }
        }

        if (isScrollingHorizontally) {
            Log.e(TAG, "onScroll: horizontal Scroll")
            return false
        }

        isVerticalAnimating = true

        if (distanceY > 0) { // 위 방향으로 스크롤
            isUpDrag = false
        } else if (distanceY < 0) {
            isUpDrag = true
        }

        val layoutParams = recyclerView.layoutParams
        initialRecyclerViewHeight = recyclerView.height
        var newHeight = initialRecyclerViewHeight - distanceY.toInt()
        newHeight = newHeight.coerceAtLeast(minimumHeight).coerceAtMost(maxHeight) // 최소 높이 설정
        layoutParams.height = newHeight
        recyclerView.layoutParams = layoutParams
        Log.e(TAG, "onScroll: newHeight $newHeight params.height ${layoutParams.height}")

        return false
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val layoutManager = recyclerView.layoutManager ?: return false
        val minFlingVelocity: Int = recyclerView.minFlingVelocity
        return (abs(velocityY) > minFlingVelocity || abs(velocityX) > minFlingVelocity)
                && snapFromFling(layoutManager, velocityX.toInt(), velocityY.toInt())
    }

    fun handleTouchEvent(event: MotionEvent) {
        if (event.action == MotionEvent.ACTION_UP || event.action == MotionEvent.ACTION_CANCEL) {
            if (isScrollingVertically) {
                val layoutParams = recyclerView.layoutParams
                val currentHeight = layoutParams.height

                val targetHeight = if (isUpDrag) {
                    (initialRecyclerViewHeight * 2).coerceAtMost(maxHeight)
                } else {
                    (initialRecyclerViewHeight / 2).coerceAtLeast(minimumHeight)
                }
                val animator = ValueAnimator.ofInt(currentHeight, targetHeight)

                animator.addUpdateListener { animation ->
                    layoutParams.height = if (isUpDrag) {
                        (animation.animatedValue as Int).coerceAtMost(maxHeight)
                    } else {
                        (animation.animatedValue as Int).coerceAtLeast(minimumHeight)
                    }
                    recyclerView.layoutParams = layoutParams
                }
                animator.duration = 300
                animator.start()
            }
        }
    }

    private fun findCenterView(
        layoutManager: RecyclerView.LayoutManager,
        helper: OrientationHelper
    ): View? {
        val childCount = layoutManager.childCount

        if (childCount == 0) {
            return null
        }

        var closestChild: View? = null
        val center = helper.startAfterPadding + helper.totalSpace / 2
        var absClosest = Int.MAX_VALUE

        for (i in 0 until childCount) {
            val child = layoutManager.getChildAt(i)
            val childCenter =
                helper.getDecoratedStart(child) + helper.getDecoratedMeasurement(child) / 2
            val absDistance = abs(childCenter - center)

            if (absDistance < absClosest) {
                absClosest = absDistance
                closestChild = child
            }
        }

        return closestChild
    }

    private fun distanceToCenter(
        targetView: View,
        helper: OrientationHelper
    ): Int {
        val childCenter =
            helper.getDecoratedStart(targetView) + helper.getDecoratedMeasurement(targetView) / 2
        val containerCenter = helper.startAfterPadding + helper.totalSpace / 2
        return childCenter - containerCenter
    }

    private fun snapFromFling(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Boolean {
        if (layoutManager !is RecyclerView.SmoothScroller.ScrollVectorProvider) {
            return false
        }

        val smoothScroller = createSnapScroller(layoutManager)
        val targetPosition = findTargetSnapPosition(layoutManager, velocityX, velocityY)
        if (targetPosition == RecyclerView.NO_POSITION) {
            return false
        }

        smoothScroller.targetPosition = targetPosition
        layoutManager.startSmoothScroll(smoothScroller)
        return true
    }

    private fun createSnapScroller(layoutManager: RecyclerView.LayoutManager): LinearSmoothScroller {
        return object : LinearSmoothScroller(recyclerView.context) {
            override fun onTargetFound(
                targetView: View,
                state: RecyclerView.State,
                action: Action
            ) {
                val snapDistances = calculateDistanceToFinalSnap(layoutManager, targetView)
                val dx = snapDistances[0]
                val dy = snapDistances[1]
                val time = calculateTimeForDeceleration(max(abs(dx), abs(dy)))
                if (time > 0) {
                    action.update(dx, dy, time, mDecelerateInterpolator)
                }
            }

            override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
                return super.calculateSpeedPerPixel(displayMetrics)
            }
        }
    }

    private fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollHorizontally()) {
            out[0] = distanceToCenter(targetView, getHorizontalHelper(layoutManager))
        } else {
            out[0] = 0
        }
        return out
    }

    private fun findTargetSnapPosition(
        layoutManager: RecyclerView.LayoutManager,
        velocityX: Int,
        velocityY: Int
    ): Int {
        val itemCount = layoutManager.itemCount
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION
        }

        val orientationHelper = this.getHorizontalHelper(layoutManager)
        var closestChildBeforeCenter: View? = null
        var distanceBefore = Int.MIN_VALUE
        var closestChildAfterCenter: View? = null
        var distanceAfter = Int.MAX_VALUE
        val childCount = layoutManager.childCount

        var visibleView: View? = null
        var visiblePosition: Int
        for (i in 0 until childCount) {
            visibleView = layoutManager.getChildAt(i)
            visibleView?.let { nonNullVisibleView ->
                visiblePosition = this.distanceToCenter(nonNullVisibleView, orientationHelper)

                if (visiblePosition in (distanceBefore + 1)..0) {
                    distanceBefore = visiblePosition
                    closestChildBeforeCenter = visibleView
                }

                if (visiblePosition in 0..<distanceAfter) {
                    distanceAfter = visiblePosition
                    closestChildAfterCenter = visibleView
                }
            }
        }
        val forwardDirection: Boolean = velocityX > 0
        if (forwardDirection && closestChildAfterCenter != null) {
            return layoutManager.getPosition(
                closestChildAfterCenter ?: return RecyclerView.NO_POSITION
            )
        } else if (!forwardDirection && closestChildBeforeCenter != null) {
            return layoutManager.getPosition(
                closestChildBeforeCenter ?: return RecyclerView.NO_POSITION
            )
        } else {
            visiblePosition =
                layoutManager.getPosition(visibleView ?: return RecyclerView.NO_POSITION)

            val snapToPosition = visiblePosition + 1
            return if (snapToPosition in 0..<itemCount) {
                snapToPosition
            } else {
                RecyclerView.NO_POSITION
            }
        }
    }

    private fun getHorizontalHelper(layoutManager: RecyclerView.LayoutManager): OrientationHelper {
        if (horizontalHelper == null || horizontalHelper?.layoutManager !== layoutManager) {
            horizontalHelper = OrientationHelper.createHorizontalHelper(layoutManager)
        }
        return OrientationHelper.createHorizontalHelper(layoutManager)
    }

    fun attachToRecyclerView() {
        if (recyclerView.onFlingListener == null) {
            recyclerView.onFlingListener = null
        }

        recyclerView.onFlingListener = object : RecyclerView.OnFlingListener() {
            override fun onFling(velocityX: Int, velocityY: Int): Boolean {
                return this@CalendarGestureDetector.onFling(
                    null,
                    MotionEvent.obtain(0, 0, MotionEvent.ACTION_UP, 0f, 0f, 0),
                    velocityX.toFloat(),
                    velocityY.toFloat()
                )
            }
        }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                isScrolled = newState != RecyclerView.SCROLL_STATE_IDLE
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    isVerticalAnimating = false
                    snapToTargetExistingView()
                }
            }
        })

        snapToTargetExistingView()
    }

    fun snapToTargetExistingView() {
        val layoutManager: RecyclerView.LayoutManager = recyclerView.layoutManager ?: return

        val snapView = findSnapView(layoutManager) ?: return
        val snapDistance = calculateDistanceToFinalSnap(layoutManager, snapView)
        if (snapDistance[0] != 0 || snapDistance[1] != 0) {
            recyclerView.smoothScrollBy(snapDistance[0], snapDistance[1])
        }
    }

    private fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        return findCenterView(layoutManager, this.getHorizontalHelper(layoutManager))
    }

    companion object {
        private const val TAG = "CalendarGestureDetector"
    }
}
