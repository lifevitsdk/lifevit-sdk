package es.lifevit.pillreminder.model

import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import es.lifevit.pillreminder.utils.Utils
import java.text.SimpleDateFormat
import java.util.*


data class PRMedication(
        var patientId: Int = -1,
        var medicationId: Int = -1,
        var color: Int = -1,
        var quantity: Int? = null,
        var indications: String? = null,
        var startTimeInMinutes: Int = 0,
        var startDate: Long = 0,
        var endDate: Long? = null,
        var repeatPattern: Int = 0) : Parcelable {


    private val TAG = PRMedication::class.java.simpleName


    constructor(parcel: Parcel) : this() {
        patientId = parcel.readInt()
        medicationId = parcel.readInt()
        color = parcel.readInt()
        quantity = parcel.readValue(Int::class.java.classLoader) as? Int
        indications = parcel.readString()
        startTimeInMinutes = parcel.readInt()
        startDate = parcel.readLong()
        endDate = parcel.readValue(Long::class.java.classLoader) as? Long
        repeatPattern = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(patientId)
        parcel.writeInt(medicationId)
        parcel.writeInt(color)
        parcel.writeValue(quantity)
        parcel.writeString(indications)
        parcel.writeInt(startTimeInMinutes)
        parcel.writeLong(startDate)
        parcel.writeValue(endDate)
        parcel.writeInt(repeatPattern)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PRMedication> {
        override fun createFromParcel(parcel: Parcel): PRMedication {
            return PRMedication(parcel)
        }

        override fun newArray(size: Int): Array<PRMedication?> {
            return arrayOfNulls(size)
        }
    }

    fun getLifeVitColor(): Int {
        return Utils.getLifeVitColor(color)
    }


    fun getNextMedicationTakes(): List<Long> {
        val result = arrayListOf<Long>()

        // Start previous day (24h before today's first take)
        val startCalculationTime = Calendar.getInstance()
        startCalculationTime.set(Calendar.HOUR, startTimeInMinutes / 60)
        startCalculationTime.set(Calendar.MINUTE, startTimeInMinutes % 60)
        startCalculationTime.set(Calendar.SECOND, 0)
        startCalculationTime.set(Calendar.MILLISECOND, 0)
        startCalculationTime.add(Calendar.DAY_OF_YEAR, -1)

        // End next day (24h after current moment)
        val endCalculationTime = Calendar.getInstance()
        endCalculationTime.add(Calendar.DAY_OF_YEAR, 1)

        val myStep: Long = Utils.getTimeinMinutesForPattern(repeatPattern).toLong() * 60L * 1000L

        Log.d(TAG, "getNextMedicationTakes: [" + medicationId + "]")
        val df = SimpleDateFormat("dd/MM/yyyy HH:mm")

        for (time in startCalculationTime.timeInMillis until endCalculationTime.timeInMillis step myStep) {
            if (time > Calendar.getInstance().timeInMillis) {
                result.add(time)
                Log.d(TAG, "___ Added: " + df.format(time))
            } else {
                Log.d(TAG, "___ NOT Added: " + df.format(time))
            }
        }

        return result
    }


}
