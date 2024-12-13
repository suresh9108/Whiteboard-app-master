package com.kotlin.whiteboard_app_master

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.annotation.IntRange

class DrawingView(context: Context, attrs: AttributeSet) : View(context,attrs){

    private var mDrawPath:CustomPath?=null
    private var mCanvasBitmap:Bitmap?=null
    private var mDrawPaint:Paint?=null
    private var mCanvasPaint:Paint?=null
    private var mBrushSize:Int = 0
    private var currentColor = Color.BLACK
    private var canvas: Canvas?=null
    private var mAlpha:Int=255
    var onTextSelected: ((Float, Float) -> Unit)? = null


    private var selectionRect: RectF? = null
    internal var isSelecting = false
    private var isDragging = false
    private var startX = 0f
    private var startY = 0f
    private var selectedText: String? = null


    private var mPaths = ArrayList<CustomPath>()
    private var mUndoPath = ArrayList<CustomPath>()
    private var backgroundColor = Color.WHITE
    private var isDrawingEnabled = false


    init {
        setUpDrawing()
    }

    private fun setUpDrawing() {
        mDrawPaint= Paint()
        mDrawPath=CustomPath(currentColor,mBrushSize,mAlpha)
        mDrawPaint!!.color = currentColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.alpha = mAlpha
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize =20
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(backgroundColor)  // Draw background color

        mCanvasBitmap?.let {
            canvas.drawBitmap(it, 0f, 0f, mCanvasPaint)
        }

        for (path in mPaths) {
            mDrawPaint!!.apply {
                strokeWidth = path.brushThickness.toFloat()
                color = path.color
                alpha = path.alpha
            }
            canvas.drawPath(path, mDrawPaint!!)
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.apply {
                strokeWidth = mDrawPath!!.brushThickness.toFloat()
                color = mDrawPath!!.color
                alpha = mDrawPath!!.alpha
            }
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }

        // Draw selection rectangle
        selectionRect?.let {
            val paint = Paint().apply {
                style = Paint.Style.STROKE
                color = Color.BLUE
                strokeWidth = 5f
            }
            canvas.drawRect(it, paint)
        }

    }




    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = currentColor
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.alpha = mAlpha
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        mDrawPath!!.moveTo(touchX,touchY)
                    }
                }
                startX = event.x
                startY = event.y
                if (isSelecting) {
                    // Start selection
                    selectionRect = RectF(startX, startY, startX, startY)
                }

            }

            MotionEvent.ACTION_MOVE ->{


                if (isSelecting && selectionRect != null) {
                    // Update selection rectangle
                    selectionRect?.set(
                        Math.min(startX, event.x),
                        Math.min(startY, event.y),
                        Math.max(startX, event.x),
                        Math.max(startY, event.y)
                    )
                    invalidate()
                }else{
                    if (touchX != null) {
                        if (touchY != null) {
                            mDrawPath!!.lineTo(touchX,touchY)
                        }
                    }
                }

            }

            MotionEvent.ACTION_UP ->{
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(currentColor,mBrushSize,mAlpha)
                if (isSelecting) {
                    // Finalize selection
                    onTextSelected?.invoke(selectionRect!!.left, selectionRect!!.top)

                    isDragging = true
                    //isSelecting = false
                }

            }

            else-> return false
        }

        invalidate()

        return true

    }
    /**
     * Helps setting the thickness of brush stroke
     * @param newSize Int 0-200
     */
    @SuppressLint("SupportAnnotationUsage")
    @IntRange(from = 0, to = 200)
    fun setSizeForBrush(newSize: Int){
        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
            newSize.toFloat(),resources.displayMetrics).toInt()
        mDrawPaint!!.strokeWidth = mBrushSize.toFloat()
    }

    fun getBrushSize(): Int {
        return mBrushSize
    }
    /**
     * Helps setting the transparency of brush stroke
     * @param newAlpha Int 0-255
     */
    @SuppressLint("SupportAnnotationUsage")
    @IntRange(from = 0,to = 255)
    fun setBrushAlpha(newAlpha:Int){
        mAlpha = newAlpha
        mDrawPaint!!.alpha = newAlpha
    }

    fun getBrushAlpha(): Int {
        return mAlpha
    }

    /**
     * Helps to set the color of brush
     * @param color Int
     */
    fun setBrushColor(color: Int){
        currentColor = color
        mDrawPaint!!.color = color
    }
    fun setBrush() {
        mDrawPaint!!.xfermode = null
        mDrawPaint!!.alpha = mAlpha
    }

    /**
     * Helps to set the color of brush
     * @return color Int
     */
    fun getBrushColor(): Int {
        return currentColor
    }

    // you can pass a color according to your background and the default is white
    /**
     * Helps to set the color of brush
     * @param color Int default white
     */
    fun erase(colorBackground: Int= Color.WHITE){
        mAlpha = 255
        mDrawPaint!!.alpha = 255
        currentColor = colorBackground
        mDrawPaint!!.color = colorBackground
    }
    fun erase() {
        mDrawPaint!!.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        mDrawPaint!!.alpha = 0
    }


    /**
     * will undo strokes, can be changed by redo()
     */
    fun undo(){
        if (mPaths.size > 0){
            mUndoPath.add(mPaths[mPaths.size -1])
            mPaths.removeAt(mPaths.size -1)
            invalidate()
        }
    }

    /**
     * will redo the undo-ed strokes
     */
    fun redo(){
        if (mUndoPath.size >0){
            mPaths.add(mUndoPath[mUndoPath.size -1])
            mUndoPath.removeAt(mUndoPath.size -1)
            invalidate()
        }

    }
    /**
     * will remove all the stores but not those saved in redo()
     */
    fun clearDrawingBoard(){
        mPaths.clear()
        invalidate()

    }

    fun getDrawing(): ArrayList<CustomPath> {
        return mPaths
    }



    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
        mCanvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
        canvas?.drawColor(backgroundColor)  // Fill with the new color
        invalidate()
    }







    inner class CustomPath(var color:Int , var brushThickness:Int, var alpha:Int) : Path() {


    }
}