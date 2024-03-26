package iit.uvip.psysuite.core.tests.ttc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import iit.uvip.psysuite.core.R
import kotlin.math.round

/*

        ____________________________________
        |                                   |
        |                                   |
        |                                   |
        |                                   |
        |                                   |
        |                                   |
        |                                   |
        |                                   |
        |___________________________________|

 */

class ScenarioView(context: Context?, attrs: AttributeSet?) : View(context, attrs) {

    companion object {
        @JvmStatic val TARGET_DIM:Int = 75
    }
    private lateinit var mTrial: TrialTTC

    private var isHorizontal: Boolean   = true
    private var isDownRight: Boolean    = true
    private var isLandscape: Boolean    = true
    private var canvas: Canvas?         = null
    private lateinit var drawPaint: Paint

    private lateinit var mLayout:ConstraintLayout
    private lateinit var mViewEnd:ImageView
    private lateinit var mViewFrame:ImageView
    private lateinit var mViewTarget:ImageView
    private lateinit var mRespButton:Button

    private var mTargetStartPos:Float = 0F   // starting position (either X or Y) of the target

    private var parent_layout_width:Int         = 0
    private var parent_layout_height:Int        = 0

    private var movementSamplingInterval:Long   = 10L      // pace when I move the target

    init {
        setupPaint()
    }

    // Setup paint with color and stroke styles
    private fun setupPaint() {
        drawPaint               = Paint()
        drawPaint.color         = Color.BLUE
        drawPaint.isAntiAlias   = true
        drawPaint.strokeWidth   = 5f
        drawPaint.style         = Paint.Style.FILL_AND_STROKE
        drawPaint.strokeJoin    = Paint.Join.ROUND
        drawPaint.strokeCap     = Paint.Cap.ROUND
    }

    override fun onDraw(canv:Canvas) {
        super.onDraw(canv)
        canvas = canv
    }

    // =============================================================================================
    //region PUBLIC

    fun createScenario(layout:ConstraintLayout, trial:TrialTTC, islandscape:Boolean, mov_sampl_int:Long, onPress:() -> Unit){

        mLayout                 = layout
        mTrial                  = trial
        isHorizontal            = trial.isHoriz
        isDownRight             = trial.isDownRight
        isLandscape             = islandscape
        movementSamplingInterval= mov_sampl_int

        parent_layout_width     = layout.width
        parent_layout_height    = layout.height

        createResponseButton("press", layout, onPress)
        createEndPoint()
        createTarget(trial.imageId, trial.distance)

        Log.d("PARAMS", "ishoriz=${isHorizontal}, dist=${trial.distance}, speed=${trial.pxPerMs}, targetx=${mViewTarget.x}, targety=${mViewTarget.y}, endx=${mViewEnd.x}, endy=${mViewEnd.y}")
//        createFrame()
    }

    fun clearScenario() {
        mLayout.removeView(mViewEnd)
        mLayout.removeView(mRespButton)
        mLayout.removeView(mViewTarget)
        //        mLayout.removeView(mViewFrame)
    }

    fun hidePoint() {
        mViewTarget.visibility = INVISIBLE
        Log.d("PARAMS", "HIDEPOINT:targetx=${mViewTarget.x}, targety=${mViewTarget.y}, endx=${mViewEnd.x}, endy=${mViewEnd.y}")
    }

    fun setToEnd() {
        if(isHorizontal)
            if(isDownRight)
                mViewTarget.x = mViewEnd.x - mViewTarget.width
            else
                mViewTarget.x = mViewEnd.x + mViewEnd.width
        else
            if(isDownRight)
                mViewTarget.y = mViewEnd.y - mViewTarget.height
            else
                mViewTarget.y = mViewEnd.y + mViewEnd.height

        Log.d("PARAMS", "SET2END:targetx=${mViewTarget.x}, targety=${mViewTarget.y}, endx=${mViewEnd.x}, endy=${mViewEnd.y}")
    }

    fun movePoint(elapsed:Long) {

        val step =  if(isDownRight) round(mTargetStartPos + mTrial.pxPerMs*elapsed).toFloat()
                    else            round(mTargetStartPos - mTrial.pxPerMs*elapsed).toFloat()

        if(isHorizontal)    mViewTarget.x = step
        else                mViewTarget.y = step
    }
    //endregion

    // =============================================================================================
    //region CREATE ELEMENTS
    private fun createResponseButton(txt:String, parent_layout:ConstraintLayout, onPress:() -> Unit): Button {

        if(this::mRespButton.isInitialized)
            mLayout.removeView(mRespButton)

        mRespButton = AppCompatButton(context).apply {
            id              = generateViewId()
            text            = txt
            textAlignment   = TextView.TEXT_ALIGNMENT_CENTER
            gravity         = Gravity.CENTER
            visibility      = VISIBLE

            parent_layout.addView(this)

            if(isLandscape) {
                x = (parent_layout_width*0.8).toFloat()
                y = (parent_layout_height*0.1).toFloat()

                layoutParams.width = (parent_layout_width*0.15).toInt()
                layoutParams.height = (parent_layout_height*0.8).toInt()
            }
            else{
                x = (parent_layout_width*0.1).toFloat()
                y = (parent_layout_height*0.8).toFloat()

                layoutParams.width = (parent_layout_width*0.8).toInt()
                layoutParams.height = (parent_layout_height*0.15).toInt()
            }

            setBackgroundColor(context.resources.getColor(R.color.colorPrimary))
            setTextAppearance(androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_Button_Colored)
            setLinkTextColor(context.resources.getColor(R.color.colorPrimary))
        }
        mRespButton.setOnClickListener {
            onPress()
        }
        return mRespButton
    }

    private fun createEndPoint(): ImageView {

        if(this::mViewEnd.isInitialized)
            mLayout.removeView(mViewEnd)

        mViewEnd = ImageView(context).apply {
            mLayout.addView(this)

            if(isLandscape) {
                if (isHorizontal) {
                    x = if(isDownRight)   (parent_layout_width * 0.75).toFloat()      // ****************************
                        else              (parent_layout_width * 0.10).toFloat()

                    y = (parent_layout_height * 0.1).toFloat()

                    layoutParams.width  = round(parent_layout_width * 0.025).toInt()
                    layoutParams.height = round(parent_layout_height * 0.8).toInt()

                } else {
                    x = (parent_layout_width * 0.10).toFloat()
                    y = if(isDownRight)   (parent_layout_height* 0.875).toFloat()     // ****************************
                        else              (parent_layout_height* 0.10).toFloat()

                    layoutParams.width  = round(parent_layout_width * 0.6).toInt()
                    layoutParams.height = round(parent_layout_height* 0.025).toInt()

                }
            }else{
                // always vertical
                x = (parent_layout_width * 0.10).toFloat()

                y = if(isDownRight)   (parent_layout_height* 0.75).toFloat()          // ****************************
                    else              (parent_layout_height* 0.10).toFloat()

                layoutParams.width  = round(parent_layout_width * 0.8).toInt()
                layoutParams.height = round(parent_layout_height* 0.025).toInt()
            }
            setBackgroundColor(Color.BLACK)
        }
        return mViewEnd
    }

    private fun createTarget(resid:Int, dist:Int):ImageView{

        if(this::mViewTarget.isInitialized)
            mLayout.removeView(mViewTarget)

        mViewTarget = ImageView(context).apply {
            mLayout.addView(this)

            layoutParams.width  = TARGET_DIM
            layoutParams.height = TARGET_DIM

            if(isLandscape) {
                if (isHorizontal) {
                    x = if(isDownRight)   (parent_layout_width * 0.75 - dist - TARGET_DIM).toFloat()      // ****************************
                        else              (parent_layout_width * 0.125 + dist).toFloat()

                    y = (parent_layout_height * 0.5 - TARGET_DIM/2).toFloat()
                } else {
                    x = (parent_layout_width * 0.40 - TARGET_DIM/2).toFloat()
                    y = if(isDownRight)   (parent_layout_height* 0.875 - dist - TARGET_DIM).toFloat()      // ****************************
                        else              (parent_layout_height* 0.125 + dist).toFloat()
                }
            }else{
                // always vertical
                x = (parent_layout_width * 0.50 - TARGET_DIM/2).toFloat()
                y = if(isDownRight)   (parent_layout_height* 0.75 - dist - TARGET_DIM).toFloat()          // ****************************
                    else              (parent_layout_height* 0.125 + dist).toFloat()
            }
            setImageResource(resid)
        }

        mTargetStartPos =   if(isHorizontal)    mViewTarget.x
                            else                mViewTarget.y

        return mViewTarget
    }


    private fun createFrame(): ImageView {

        if(this::mViewEnd.isInitialized)
            mLayout.removeView(mViewEnd)

        mViewFrame = ImageView(context).apply {
            mLayout.addView(this)

            layoutParams.width  = round(parent_layout_height * 0.8).toInt()
            layoutParams.height = round(parent_layout_height * 0.8).toInt()

            x = (parent_layout_height * 0.10).toFloat()
            y = (parent_layout_height * 0.10).toFloat()

//            setBorder
            setBackgroundColor(Color.BLACK)
        }
        return mViewEnd
    }
    //endregion

    // =============================================================================================
}