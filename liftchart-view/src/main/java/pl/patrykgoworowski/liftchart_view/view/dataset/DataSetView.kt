package pl.patrykgoworowski.liftchart_view.view.dataset

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.view.ViewCompat
import pl.patrykgoworowski.liftchart_common.axis.*
import pl.patrykgoworowski.liftchart_common.constants.DEF_CHART_WIDTH
import pl.patrykgoworowski.liftchart_common.extension.set
import pl.patrykgoworowski.liftchart_view.common.UpdateRequestListener
import pl.patrykgoworowski.liftchart_view.data_set.DataSetRendererWithModel
import pl.patrykgoworowski.liftchart_view.data_set.layout.ViewVirtualLayout
import pl.patrykgoworowski.liftchart_view.extension.*
import java.util.*
import kotlin.properties.Delegates.observable

class DataSetView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val contentBounds = RectF()

    private val updateRequestListener: UpdateRequestListener = object : UpdateRequestListener {

        private var previousDataSetWidth = 0

        override fun invoke() {
            if (ViewCompat.isAttachedToWindow(this@DataSetView)) {
                val contentWidth = getMeasuredContentWidth()
                val parentOrOwnWidth = parentOrOwnWidth
                if (widthIsWrapContent &&
                    width != contentWidth &&
                    contentWidth <= parentOrOwnWidth &&
                    previousDataSetWidth <= parentOrOwnWidth
                ) {
                    requestLayout()
                } else {
                    updateBounds()
                    invalidate()
                }
            }
        }
    }

    private val virtualLayout = ViewVirtualLayout(isLTR)

    var dataSet: DataSetRendererWithModel<*>? by observable(null) { _, oldValue, newValue ->
        oldValue?.removeListener(updateRequestListener)
        newValue?.addListener(updateRequestListener)
        updateBounds()
    }

    var axisManager = AxisManager()

    override fun onDraw(canvas: Canvas) {
        val dataSet = dataSet ?: return
        dataSet.getAxisModel().let { model ->
            axisManager.draw(canvas, model)
        }
        dataSet.draw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.UNSPECIFIED,
            MeasureSpec.AT_MOST -> getMeasuredContentWidth()
            else -> measureDimension(widthMeasureSpec.specSize, widthMeasureSpec)
        }

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.UNSPECIFIED -> DEF_CHART_WIDTH.dpInt + verticalPadding
            MeasureSpec.AT_MOST -> minOf(
                DEF_CHART_WIDTH.dpInt + verticalPadding,
                heightMeasureSpec.specSize
            )
            else -> measureDimension(heightMeasureSpec.specSize, heightMeasureSpec)
        }
        setMeasuredDimension(width, height)

        contentBounds.set(
            paddingLeft,
            paddingTop,
            width - paddingRight,
            height - paddingBottom
        )
        updateBounds()
    }

    private fun getMeasuredContentWidth(): Int {
        val dataSet = dataSet ?: return horizontalPadding
        return virtualLayout.getMeasuredWidth(dataSet, axisManager) + horizontalPadding
    }

    private fun updateBounds() {
        val dataSet = dataSet ?: return
        virtualLayout.setBounds(contentBounds, dataSet, axisManager)
    }

    override fun onRtlPropertiesChanged(layoutDirection: Int) {
        val isLTR = layoutDirection == ViewCompat.LAYOUT_DIRECTION_LTR
        virtualLayout.isLTR = isLTR
    }
}