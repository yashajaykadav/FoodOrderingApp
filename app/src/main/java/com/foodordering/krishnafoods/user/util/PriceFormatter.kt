import android.content.Context
import android.graphics.Paint
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.foodordering.krishnafoods.R

// PriceFormatter.kt
object PriceFormatter {

    fun formatPrices(
        originalPrice: Int,
        offerPrice: Int?,
        originalPriceView: TextView,
        offerPriceView: TextView,
        context: Context
    ) {
        val hasValidOffer = offerPrice != null && offerPrice < originalPrice

        // Reset original price view
        originalPriceView.visibility = View.GONE
        originalPriceView.paintFlags =
            originalPriceView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        if (hasValidOffer) {
            // Show original price with strike-through
            originalPriceView.text = context.getString(R.string.price_format, originalPrice)
            originalPriceView.visibility = View.VISIBLE
            originalPriceView.paintFlags =
                originalPriceView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            // Show offer price highlighted
            offerPriceView.text = context.getString(R.string.price_format, offerPrice)
            offerPriceView.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        } else {
            // Show original price as regular price
            offerPriceView.text = context.getString(R.string.price_format, originalPrice)
            offerPriceView.setTextColor(ContextCompat.getColor(context, R.color.gray))
        }
    }
}