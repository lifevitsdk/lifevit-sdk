package es.lifevit.pillreminder.utils


import android.content.Context
import es.lifevit.pillreminder.R
import es.lifevit.pillreminder.constants.AppConstants
import es.lifevit.sdk.LifevitSDKConstants
import java.util.*


object Utils {


    fun getTimeinMinutesForPattern(repeatPattern: Int): Int {
        return when (repeatPattern) {
            AppConstants.PERIODICITY_4H -> 4 * 60
            AppConstants.PERIODICITY_6H -> 6 * 60
            AppConstants.PERIODICITY_8H -> 8 * 60
            AppConstants.PERIODICITY_12H -> 12 * 60
            AppConstants.PERIODICITY_24H -> 24 * 60
            AppConstants.PERIODICITY_1min -> 1
            else -> 24 * 60
        }
    }


    fun getMedicationName(context: Context, medicationId: Int): String {
        return when (medicationId) {
            AppConstants.MEDICATION_ADIRO -> context.getString(R.string.adiro)
            AppConstants.MEDICATION_ENANTYUM -> context.getString(R.string.enantyum)
            AppConstants.MEDICATION_EUTIROX -> context.getString(R.string.eutirox)
            AppConstants.MEDICATION_IBUPROFENO -> context.getString(R.string.ibuprofeno)
            AppConstants.MEDICATION_NOLOTIL -> context.getString(R.string.nolotil)
            AppConstants.MEDICATION_ORFIDAL -> context.getString(R.string.orfidal)
            AppConstants.MEDICATION_PARACETAMOL -> context.getString(R.string.paracetamol)
            AppConstants.MEDICATION_SINTROM -> context.getString(R.string.sintrom)
            AppConstants.MEDICATION_TRANKIMAZIN -> context.getString(R.string.trankimazin)
            AppConstants.MEDICATION_VENTOLIN -> context.getString(R.string.ventolin)
            else -> ""
        }
    }


    fun getLifeVitColor(color: Int): Int {
        return when (color) {
            AppConstants.TICARE_COLOR_GREEN -> LifevitSDKConstants.PILLREMINDER_COLOR_GREEN
            AppConstants.TICARE_COLOR_PURPLE -> LifevitSDKConstants.PILLREMINDER_COLOR_PURPLE
            AppConstants.TICARE_COLOR_RED -> LifevitSDKConstants.PILLREMINDER_COLOR_RED
            AppConstants.TICARE_COLOR_YELLOW -> LifevitSDKConstants.PILLREMINDER_COLOR_YELLOW
            AppConstants.TICARE_COLOR_BLUE -> LifevitSDKConstants.PILLREMINDER_COLOR_BLUE
            else -> LifevitSDKConstants.PILLREMINDER_COLOR_RED
        }
    }


    fun isSameDay(date1: Long, date2: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance()
        cal1.time = Date(date1)
        cal2.time = Date(date2)
        return cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR) && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
    }


}