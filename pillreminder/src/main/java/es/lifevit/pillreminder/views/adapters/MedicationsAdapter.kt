package es.lifevit.pillreminder.views.adapters


import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import es.lifevit.pillreminder.R
import es.lifevit.pillreminder.constants.AppConstants
import es.lifevit.pillreminder.model.PRMedication
import es.lifevit.pillreminder.utils.Utils
import es.lifevit.pillreminder.views.activities.BaseAppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs


class MedicationsAdapter(private val mActivity: BaseAppCompatActivity, private val menuButtonsIndexes: List<PRMedication>) : RecyclerView.Adapter<MedicationsAdapter.ViewHolder>() {

    private val TAG = MedicationsAdapter::class.java.simpleName

    private val mInflater: LayoutInflater = LayoutInflater.from(mActivity)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = mInflater.inflate(R.layout.item_medication, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val medication = menuButtonsIndexes[position]

        // Title
        holder.item_medication_title.text = Utils.getMedicationName(mActivity, medication.medicationId)

        // Quantity of pills
        holder.item_medication_dose_text.text = if (medication.quantity == 1) {
            mActivity.getString(R.string.number_tablets_singular, medication.quantity)
        } else {
            mActivity.getString(R.string.number_tablets_plural, medication.quantity)
        }

        // Date from and to
        val resultFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        holder.item_medication_dates_text.text = if (medication.endDate != null) {
            mActivity.getString(R.string.date_from_to, resultFormatter.format(medication.startDate), resultFormatter.format(medication.endDate))
        } else {
            mActivity.getString(R.string.date_from, resultFormatter.format(medication.startDate))
        }

        // Calculate time of next medication
        val nextTake = medication.getNextMedicationTakes()[0]

        val nextTakeCalendar = Calendar.getInstance()
        nextTakeCalendar.timeInMillis = nextTake
        nextTakeCalendar.set(Calendar.SECOND, 0)
        nextTakeCalendar.set(Calendar.MILLISECOND, 0)

        val isToday = Utils.isSameDay(Calendar.getInstance().timeInMillis, nextTake)

        val dayText = if (isToday) {
            mActivity.getString(R.string.today)
        } else {
            mActivity.getString(R.string.tomorrow)
        }

        val hour: String = if (nextTakeCalendar.get(Calendar.HOUR_OF_DAY) < 10) {
            "0"
        } else {
            ""
        } + nextTakeCalendar.get(Calendar.HOUR_OF_DAY)

        val minute: String = if (nextTakeCalendar.get(Calendar.MINUTE) < 10) {
            "0"
        } else {
            ""
        } + nextTakeCalendar.get(Calendar.MINUTE)

        val timeText = hour + ":" + minute

        holder.item_medication_time_text.text = mActivity.getString(R.string.next_medication_time, dayText, timeText)

        // Difference text
        val currentMoment = Calendar.getInstance()
        currentMoment.set(Calendar.SECOND, 0)
        currentMoment.set(Calendar.MILLISECOND, 0)

        val differenceInMinutes = abs(nextTakeCalendar.timeInMillis - currentMoment.timeInMillis) / (1000 * 60)
        val differenceString = "" + if (differenceInMinutes > 60) {
            "" + (differenceInMinutes / 60) + "h "
        } else {
            ""
        } + (differenceInMinutes % 60) + "'"

        holder.item_medication_time_remaining_text.text = mActivity.getString(R.string.remaining_time, differenceString)

        //        holder. item_medication_image
        // TODO: Versiones previas de Android?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            val colorId = when (medication.color) {
                AppConstants.TICARE_COLOR_YELLOW -> R.color.bracelet_yellow
                AppConstants.TICARE_COLOR_PURPLE -> R.color.bracelet_purple
                AppConstants.TICARE_COLOR_BLUE -> R.color.bracelet_blue
                AppConstants.TICARE_COLOR_GREEN -> R.color.bracelet_green
                AppConstants.TICARE_COLOR_RED -> R.color.bracelet_red
                else -> android.R.color.black
            }

            holder.item_medication_color.imageTintList = ContextCompat.getColorStateList(mActivity, colorId)
        }


//        holder.item_medication_parent_layout.setOnClickListener {
//
//
//        }
    }


    override fun getItemCount(): Int {
        return menuButtonsIndexes.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var item_medication_parent_layout: LinearLayout = itemView.findViewById(R.id.item_medication_parent_layout)

        var item_medication_image: ImageView = itemView.findViewById(R.id.item_medication_image)
        var item_medication_color: ImageView = itemView.findViewById(R.id.item_medication_color)

        var item_medication_title: TextView = itemView.findViewById(R.id.item_medication_title)
        var item_medication_dose_text: TextView = itemView.findViewById(R.id.item_medication_dose_text)
        var item_medication_dates_text: TextView = itemView.findViewById(R.id.item_medication_dates_text)
        var item_medication_time_text: TextView = itemView.findViewById(R.id.item_medication_time_text)
        var item_medication_time_remaining_text: TextView = itemView.findViewById(R.id.item_medication_time_remaining_text)

    }


}

