package es.lifevit.sdk;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

import es.lifevit.sdk.utils.HexUtils;
import es.lifevit.sdk.utils.LogUtils;
import es.lifevit.sdk.utils.Utils;
import es.lifevit.sdk.weightscale.LifevitSDKWeightScaleData;


/**
 * Created by aescanuela on 26/1/16.
 */
public class LifevitSDKBleDeviceWeightScale extends LifevitSDKBleDevice {

    private final static String TAG = LifevitSDKBleDeviceWeightScale.class.getSimpleName();

    private static final String DEVICE_NAME = "Chipsea-BLE";

    private final static byte HEADER = (byte) 0xCA;
    private final static byte VERSION_NUMBER = (byte) 0x10;
    private final static byte COMMAND_SYNC_DATA = (byte) 0x01;


    private Double lastWeight = null;
    private double lastFat;
    private double userId;


    /**
     * Service Descriptors
     */


    // UUIDs
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String ISSC_SERVICE_UUID = "0000fff0-0000-1000-8000-00805f9b34fb";
    public static String ISSC_CHAR_RX_UUID = "0000fff1-0000-1000-8000-00805f9b34fb";
    public static String ISSC_CHAR_TX_UUID = "0000fff2-0000-1000-8000-00805f9b34fb";
    public static String ISSC_CHAR_BODY_UUID = "00002a9c-0000-1000-8000-00805f9b34fb";
    private boolean isNewWeightScale = false;


    protected LifevitSDKBleDeviceWeightScale(BluetoothDevice dev, LifevitSDKManager ch) {
        this.mBluetoothDevice = dev;
        this.mLifevitSDKManager = ch;
    }

    @Override
    protected int getType() {
        return LifevitSDKConstants.DEVICE_WEIGHT_SCALE;
    }

    /**
     * Receivers
     */

    protected void connectGatt(Context context, boolean firstTime) {
        mBluetoothGatt = mBluetoothDevice.connectGatt(context, false, this.mGattCallback);
        mContext = context;
        mFirstTime = firstTime;
    }


    protected void startReceiver(String action, Intent intent) {
    }


    /**
     * Other methods
     */

    protected static boolean isWeightScaleDevice(String name) {
        return DEVICE_NAME.equalsIgnoreCase(name);
    }


    protected static UUID[] getUUIDs() {
        UUID[] uuidArray = new UUID[1];
        uuidArray[0] = UUID.fromString(ISSC_SERVICE_UUID);
        return uuidArray;
    }


    protected void sendDeviceStatus() {
        mLifevitSDKManager.deviceOnConnectionChanged(LifevitSDKConstants.DEVICE_WEIGHT_SCALE, mDeviceStatus, true);
        if (mDeviceStatus == LifevitSDKConstants.STATUS_CONNECTED) {
            // Send instruction after services discovered...
            checkAndSendWeight();
        } else if (mDeviceStatus == LifevitSDKConstants.STATUS_DISCONNECTED) {
            LogUtils.log(Log.DEBUG, TAG, "Checking is in measurement on disconnection");
            /*if(inMeasuring){
                LogUtils.log(Log.DEBUG, TAG, "Sending weight: " + lastWeight);
                //Si no se ha enviado el peso lo enviamos al desconectar...
                mLifevitSDKManager.getWeightScaleListener().onScaleMeasurementAllValues(lastWeight,
                        PreferenceUtil.getWeightScaleUnit(mContext), lastFat, 0,0,0,0,0);
                inMeasuring = false;
            }*/
        }
    }

    public void checkAndSendWeight() {
        if (!isNewWeightScale) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothGatt != null) {
                        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(ISSC_SERVICE_UUID));
                        Log.d(TAG, "mBluetoothGatt: " + mBluetoothGatt);
                        if (RxService == null) {
                            Log.e(TAG, "Rx service not found!");
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            checkAndSendWeight();
                            return;
                        }

                        getWeight();
                    }
                }
            }).start();
        }
    }


    /**
     * Enable Device Notification
     *
     * @return
     */
    protected void enableDeviceNotification() {

        BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(ISSC_SERVICE_UUID));
        if (RxService == null) {
            LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(UUID.fromString(ISSC_CHAR_RX_UUID));
        if (TxChar == null) {
            LogUtils.log(Log.ERROR, TAG, "Tx charateristic not found!");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);

        // This is specific to Heart Rate Measurement.
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        write(descriptor);


        for (BluetoothGattService s : mBluetoothGatt.getServices()) {
            for (BluetoothGattCharacteristic c : s.getCharacteristics()) {
                if (UUID.fromString(ISSC_CHAR_BODY_UUID).equals(c.getUuid())) {
                    //Habilitamos notificaciones de la característica determinada
                    mBluetoothGatt.setCharacteristicNotification(c, true);
                    isNewWeightScale = true;
                    if (mLifevitSDKManager.getWeightScaleListener() != null) {
                        mLifevitSDKManager.getWeightScaleListener().onScaleTypeDetected(LifevitSDKConstants.WeightScale.TYPE2);
                    }

                    for (BluetoothGattDescriptor d : c.getDescriptors()
                    ) {
                        d.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

                        write(d);
                    }
                }
            }
        }

        if (!isNewWeightScale) {
            if (mLifevitSDKManager.getWeightScaleListener() != null) {
                mLifevitSDKManager.getWeightScaleListener().onScaleTypeDetected(LifevitSDKConstants.WeightScale.TYPE1);
            }

        }
    }


    protected synchronized void sendMessage(byte[] data) {

        if (mBluetoothGatt != null) {
            BluetoothGattService RxService = mBluetoothGatt.getService(UUID.fromString(ISSC_SERVICE_UUID));
            LogUtils.log(Log.DEBUG, TAG, "mBluetoothGatt: " + mBluetoothGatt);
            if (RxService == null) {
                LogUtils.log(Log.ERROR, TAG, "Rx service not found!");
                return;
            }
            BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(UUID.fromString(ISSC_CHAR_TX_UUID));
            if (RxChar == null) {
                LogUtils.log(Log.ERROR, TAG, "Rx charateristic not found!");
                return;
            }

            dividePacketsAndSendMessage(RxChar, data, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }
    }


    protected synchronized void characteristicReadProcessData(BluetoothGattCharacteristic characteristic) {

        final byte[] bytes = characteristic.getValue();

        LogUtils.log(Log.DEBUG, TAG, "[RECEIVED]: " + HexUtils.getStringToPrint(bytes) + " [" + characteristic.getUuid() + "]");

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.

        if (UUID.fromString(ISSC_CHAR_BODY_UUID).equals(characteristic.getUuid())) {


            if (bytes[0] == 0x06) {
                //Vamos a ver que devuelve la báscula...

                double bia = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[10], bytes[9]}) / 10.0;
                double weight = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[12], bytes[11]}) / 10.0;
                // ALL data
                double fatPercentage = this.getPercentageFatWithWeight(mContext, weight, bia);
                double waterPercentage = this.getPercentageWaterWithWeight(mContext, weight, bia);
                double musclePercentage = this.getPercentageMuscleWithWeight(mContext, weight, bia);
                double visceralPercent = this.getVisceralWithWeight(mContext, weight, bia);
                double boneKg = this.getBoneWithWeight(mContext, weight, bia);
                double bmrKcal = this.getBMRWithWeight(mContext, weight, bia);

                if (mLifevitSDKManager.getWeightScaleListener() != null) {
                    LifevitSDKWeightScaleData data = new LifevitSDKWeightScaleData();
                    data.setWeight(weight);
                    data.setUnit("kg");
                    data.setImc(getBMIWithWeight(mContext, weight));
                    data.setFatRawValue(fatPercentage * weight / 100);
                    data.setFatPercentage(fatPercentage);
                    data.setWaterRawValue(waterPercentage * weight / 100);
                    data.setWaterPercentage(waterPercentage);
                    data.setMuscleRawValue(musclePercentage * weight / 100);
                    data.setMusclePercentage(musclePercentage);
                    data.setVisceralRawValue(visceralPercent * weight / 100);
                    data.setVisceralPercentage(visceralPercent);
                    data.setBoneRawValue(boneKg);
                    data.setBonePercentage(boneKg / weight * 100);
                    data.setBmr(bmrKcal);
                    data.setBodyAge(this.getBodyAge(mContext, data.getImc()));
                    data.setIdealWeight(this.getIdealBodyWeight(mContext));
                    data.setProteinPercentage(this.getProteinPercentage(data.getMusclePercentage()));
                    data.setObesityPercentage(this.getObesityPercentage(mContext, weight));
                    data.setBia(bia);
                    mLifevitSDKManager.getWeightScaleListener().onScaleMeasurementAllValues(data);
                }
            }
        } else if (UUID.fromString(ISSC_CHAR_RX_UUID).equals(characteristic.getUuid())) {
            if (bytes != null) {
                if (bytes[0] == 0x06) {
                    //Vamos a ver que devuelve la báscula...

                    //double bia = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[10], bytes[9]}) / 10.0;
                    double weight = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[12], bytes[11]}) / 10.0;

                    if (mLifevitSDKManager.getWeightScaleListener() != null) {
                        mLifevitSDKManager.getWeightScaleListener().onScaleMeasurementOnlyWeight(weight, LifevitSDKConstants.WEIGHT_UNIT_KG);
                    }
                } else if (bytes[0] == HEADER) {

                    int protocolVersion = bytes[1];
                    int dataLength = bytes[2];
                    int command = bytes[3];

                    if (protocolVersion == VERSION_NUMBER) {

                        LogUtils.log(Log.DEBUG, TAG, "[Old Weight Scale]");

                        // OLD WEIGHT SCALE
                        if (command == COMMAND_SYNC_DATA) {

                            // SYNC DATA OPERATION
                            byte options = bytes[4];
                            int mode = options & 0b00000001;
                            int decimalPlacesCode = options & 0b00000110;
                            int unit = options & 0b00011000;

                            int weightRaw = Utils.bytesToInt(new byte[]{0, 0, bytes[5], bytes[6]});

                            int decimalPlaces = 0;
                            switch (decimalPlacesCode) {
                                case 0:
                                    decimalPlaces = 1;
                                    break;
                                case 1:
                                    decimalPlaces = 0;
                                    break;
                                case 2:
                                    decimalPlaces = 2;
                                    break;
                            }

                            double weightDouble = (double) weightRaw / 10.0 * decimalPlaces;

                            if (mode == 0) {

                                // Only weight

                                if (mLifevitSDKManager.getWeightScaleListener() != null) {
                                    mLifevitSDKManager.getWeightScaleListener().onScaleMeasurementOnlyWeight(weightDouble, unit);
                                }

                            } else if (mode == 1) {

                                // ALL data
                                double fatPercentage = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[7], bytes[8]}) / 10.0 * decimalPlaces;
                                double waterPercentage = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[9], bytes[10]}) / 10.0 * decimalPlaces;
                                double muscleKg = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[11], bytes[12]}) / 10.0 * decimalPlaces;
                                double visceralPercent = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[15], bytes[16]}) / 10.0 * decimalPlaces;
                                double boneKg = (double) Utils.bytesToInt(new byte[]{0, 0, 0, bytes[17]}) / 10.0 * decimalPlaces;
                                double bonePercentage = boneKg / weightDouble * 100;
                                double bmrKcal = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[13], bytes[14]});

                                if (mLifevitSDKManager.getWeightScaleListener() != null) {

                                    LifevitSDKWeightScaleData data = new LifevitSDKWeightScaleData();
                                    data.setBmr(bmrKcal);
                                    data.setBoneRawValue(boneKg);
                                    data.setBonePercentage(bonePercentage);
                                    data.setFatRawValue(fatPercentage * weightDouble * 100);
                                    data.setFatPercentage(fatPercentage);
                                    data.setImc(getBMIWithWeight(mContext, weightDouble));
                                    data.setUnit(PreferenceUtil.getWeightScaleUnit(mContext) == LifevitSDKConstants.WEIGHT_UNIT_KG ? "kg" : "lb");
                                    data.setMuscleRawValue(muscleKg);
                                    data.setMusclePercentage(muscleKg / weightDouble * 100);
                                    data.setVisceralRawValue(visceralPercent * weightDouble / 100);
                                    data.setVisceralPercentage(visceralPercent);
                                    data.setWaterRawValue(waterPercentage * weightDouble * 100);
                                    data.setWaterPercentage(waterPercentage);
                                    data.setWeight(weightDouble);
                                    data.setBodyAge(this.getBodyAge(mContext, data.getImc()));
                                    data.setIdealWeight(this.getIdealBodyWeight(mContext));
                                    data.setProteinPercentage(this.getProteinPercentage(data.getMusclePercentage()));
                                    data.setObesityPercentage(this.getObesityPercentage(mContext, weightDouble));
                                    mLifevitSDKManager.getWeightScaleListener().onScaleMeasurementAllValues(data);
                                }
                            }
                        }
                    } else if (protocolVersion == (byte) 0x11) {

                        // NEW WEIGHT SCALE
                        LogUtils.log(Log.DEBUG, TAG, "[New Weight Scale!]");

                        if (command == 0x00) {

                            //inMeasuring = true;

                            // Only weight data
                            double weight = Utils.bytesToInt(new byte[]{0, 0, bytes[5], bytes[6]}) / 10.0;
                            LogUtils.log(Log.DEBUG, TAG, "Only weight packet" + weight);

                            if (PreferenceUtil.getWeightScaleUnit(mContext) == LifevitSDKConstants.WEIGHT_UNIT_LB) {
                                weight = Utils.kgToLb(weight);
                            }
                            /*lastWeight = weight;
                            lastFat = 0;*/

                            if (mLifevitSDKManager.getWeightScaleListener() != null) {
                                mLifevitSDKManager.getWeightScaleListener().onScaleMeasurementOnlyWeight(weight, PreferenceUtil.getWeightScaleUnit(mContext));
                            }
                            // Ask for weight again
                            getWeight();

                        } else if (command == 0x12) {

                            // ALL data - First packet

                            int packetNumber = bytes[4] >> 4;
                            int totalPackets = bytes[4] & 0b00001111;

                            if (packetNumber == 1) {

                                userId = Utils.bytesToInt(new byte[]{bytes[11], bytes[12], bytes[13], bytes[14]});

                                lastWeight = Utils.bytesToInt(new byte[]{0, 0, bytes[15], bytes[16]}) / 10.0;
                                lastFat = Utils.bytesToInt(new byte[]{0, 0, bytes[17], bytes[18]}) / 10.0;

                                LogUtils.log(Log.DEBUG, TAG, "[Sync Data Command] Packet: " + packetNumber + "/" + totalPackets
                                        + ", User Number: " + userId
                                        + ", Weight: " + lastWeight
                                        + ", Fat: " + lastFat);
                            } else {

                                //inMeasuring = false;
                                // ALL data - Last packet

                                double waterPercentage, waterKg, muscleKg, musclePercentage, bmrKcal, visceralPercent, visceralKg, boneKg, bonePercentage, fatPercentage;
                                double weightToSend = lastWeight;

                                if (PreferenceUtil.getWeightScaleUnit(mContext) == LifevitSDKConstants.WEIGHT_UNIT_LB) {
                                    weightToSend = Utils.kgToLb(lastWeight);

                                }
                                /*if (userId == 0) {

                                    waterPercentage = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[6], bytes[7]}) / 10.0;
                                    muscleKg = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[8], bytes[9]}) / 10.0;
                                    bmrKcal = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[10], bytes[11]});
                                    visceralPercent = (double) Utils.bytesToInt(new byte[]{0, 0, 0, bytes[13]}) / 10.0;
                                    boneKg = (double) Utils.bytesToInt(new byte[]{0, 0, 0, bytes[5]}) / 10.0;

                                    if (BuildConfig.DEBUG_MESSAGES) {
                                        Log.d(TAG, "[Sync Data Command OLD] Packet: " + packetNumber + "/" + totalPackets
                                                + ", waterPercentage: " + waterPercentage
                                                + ", muscleKg: " + muscleKg
                                                + ", bmrKcal: " + bmrKcal
                                                + ", visceralPercent: " + visceralPercent
                                                + ", boneKg: " + boneKg);
                                    }
                                } else {*/
                                waterPercentage = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[5], bytes[6]}) / 10.0;
                                waterKg = waterPercentage * weightToSend / 100;

                                muscleKg = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[7], bytes[8]}) / 10.0;
                                musclePercentage = muscleKg / weightToSend * 100;
                                bmrKcal = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[9], bytes[10]});
                                visceralPercent = (double) Utils.bytesToInt(new byte[]{0, 0, bytes[11], bytes[12]}) / 10.0;
                                visceralKg = waterPercentage * weightToSend / 100;
                                boneKg = (double) Utils.bytesToInt(new byte[]{0, 0, 0, bytes[13]}) / 10.0;
                                double boneToSend = boneKg;
                                if (PreferenceUtil.getWeightScaleUnit(mContext) == LifevitSDKConstants.WEIGHT_UNIT_LB) {
                                    boneToSend = Utils.kgToLb(boneKg);
                                }
                                bonePercentage = boneToSend / weightToSend * 100;

                                LogUtils.log(Log.DEBUG, TAG, "[Sync Data Command NEW] Packet: " + packetNumber + "/" + totalPackets
                                        + ", waterPercentage: " + waterPercentage
                                        + ", muscleKg: " + muscleKg
                                        + ", bmrKcal: " + bmrKcal
                                        + ", visceralPercent: " + visceralPercent
                                        + ", boneKg: " + boneKg);

                                // It returns result in Kg always
                                if (mLifevitSDKManager.getWeightScaleListener() != null) {
                                    LifevitSDKWeightScaleData data = new LifevitSDKWeightScaleData();
                                    data.setBmr(bmrKcal);
                                    data.setBoneRawValue(boneKg);
                                    data.setBonePercentage(bonePercentage);
                                    data.setFatRawValue(lastFat);
                                    data.setFatPercentage(lastFat / weightToSend * 100);
                                    data.setImc(getBMIWithWeight(mContext, weightToSend));
                                    data.setUnit(PreferenceUtil.getWeightScaleUnit(mContext) == LifevitSDKConstants.WEIGHT_UNIT_KG ? "kg" : "lb");
                                    data.setMuscleRawValue(muscleKg);
                                    data.setMusclePercentage(musclePercentage);
                                    data.setVisceralRawValue(visceralKg);
                                    data.setVisceralPercentage(visceralPercent);
                                    data.setWaterRawValue(waterKg);
                                    data.setWaterPercentage(waterPercentage);
                                    data.setWeight(weightToSend);
                                    data.setBodyAge(this.getBodyAge(mContext, data.getImc()));
                                    data.setIdealWeight(this.getIdealBodyWeight(mContext));
                                    data.setProteinPercentage(this.getProteinPercentage(data.getMusclePercentage()));
                                    data.setObesityPercentage(this.getObesityPercentage(mContext, weightToSend));
                                    mLifevitSDKManager.getWeightScaleListener().onScaleMeasurementAllValues(data);
                                }

                            }
                        }
                    }
                }
            }
        }
    }


    public void getWeight() {
        if (!isNewWeightScale) {
            byte[] values = syncUserInformation2();
            sendMessage(values);
            checkReceivedWeight();
        }
    }

    private void checkReceivedWeight() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {

                }
                if (lastWeight == null && mBluetoothGatt != null) {
                    getWeight();
                }
            }
        }).start();
    }


    public byte[] syncUserInformation2() {
        byte[] bArr = new byte[20];

        bArr[0] = (byte) 0xCA; // header
        bArr[1] = (byte) 0x0A; // length
        bArr[2] = (byte) 0x10; // command
        bArr[3] = (byte) 0x01; // user number

        int unixTime = (int) (System.currentTimeMillis() / 1000);
        bArr[4] = (byte) (unixTime >> 24); // time
        bArr[5] = (byte) (unixTime >> 16); //time
        bArr[6] = (byte) (unixTime >> 8); //time
        bArr[7] = (byte) unixTime; //time

        //bArr[4] = (byte) (0); // time
        //bArr[5] = (byte) (0); //time
        //bArr[6] = (byte) (0); //time
        //bArr[7] = (byte) 0; //time

//        DGUser user = DagaApplication.getInstance().getActualUser();

        // gender
        if (PreferenceUtil.getWeightScaleUserGender(mContext) == LifevitSDKConstants.WEIGHT_SCALE_GENDER_MALE) {
            bArr[8] = (byte) 0b10000000;
        } else {
            bArr[8] = (byte) 0x00;
        }

        // age
        bArr[9] = (byte) PreferenceUtil.getWeightScaleUserAge(mContext).intValue();

        // height cm
        bArr[10] = (byte) PreferenceUtil.getWeightScaleUserHeight(mContext).intValue();

        // weight unit
        if (PreferenceUtil.getWeightScaleUnit(mContext) == LifevitSDKConstants.WEIGHT_UNIT_LB) {
            // Lb
            bArr[11] = (byte) 0x01;
        } else {
            // Kg
            bArr[11] = (byte) 0x00;
        }

        bArr[12] = getDatasXor(bArr, 0, 12); // xor byte

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        return bArr;
    }

    public void getHistoryData() {
        byte[] bArr = new byte[6];

        bArr[0] = (byte) 0xCA; // header
        bArr[1] = (byte) 0x11; // version
        bArr[2] = (byte) 0x02; // length
        bArr[3] = (byte) 0x11; // command
        bArr[4] = (byte) 0x01; // type
        bArr[5] = (byte) 0x00; // check code

        LogUtils.log(Log.DEBUG, TAG, "Sending: " + HexUtils.getStringToPrint(bArr));

        sendMessage(bArr);
    }


    public static byte getDatasXor(byte[] bArr, int i, int i2) {
        byte b = bArr[i];
        for (int i3 = (byte) (i + 1); i3 <= i2; i3 = (byte) (i3 + 1)) {
            b = (byte) (b ^ bArr[i3]);
        }
        return b;
    }

    //region New Weight scale by bia


    public double getPercentageFatWithWeight(Context context, double weight, double bia) {
        if (bia == 0) {
            return 0;
        }
        double userHeight = PreferenceUtil.getWeightScaleUserHeight(context).doubleValue();
        int userAge = PreferenceUtil.getWeightScaleUserAge(context).intValue();
        int userGender = PreferenceUtil.getWeightScaleUserGender(context).intValue();

        LogUtils.log(Log.WARN, TAG, String.format("userHeight: %.2f, userAge: %d, userGender: %d, bia: %.2f", userHeight, userAge, userGender, bia));

        if (userGender == LifevitSDKConstants.GENDER_MALE) {
            return 24.1911 + (0.0463 * userAge) - (0.460888 * userHeight) + (0.6341581 * weight) + (0.0566524 * bia);
        }
        return 43.1912 + (0.0443 * userAge) - (0.5008 * userHeight) + (0.7042 * weight) + (0.0449 * bia);
    }

    public double getPercentageWaterWithWeight(Context context, double weight, double bia) {
        if (bia == 0) {
            return 0;
        }
        int userGender = PreferenceUtil.getWeightScaleUserGender(context).intValue();

        if (userGender == LifevitSDKConstants.GENDER_MALE) {
            return 80 - this.getPercentageFatWithWeight(context, weight, bia);
        }
        return (80 - this.getPercentageFatWithWeight(context, weight, bia)) * 0.96;
    }

    public double getPercentageMuscleWithWeight(Context context, double weight, double bia) {
        if (bia == 0) {
            return 0;
        }
        double userHeight = PreferenceUtil.getWeightScaleUserHeight(context).doubleValue();
        int userAge = PreferenceUtil.getWeightScaleUserAge(context).intValue();
        int userGender = PreferenceUtil.getWeightScaleUserGender(context).intValue();

        if (userGender == LifevitSDKConstants.GENDER_MALE) {
            return 66.4907 - (0.1919 * userAge) + (0.2279 * userHeight) - (0.402 * weight) - (0.0514 * bia);
        }
        return 58.4907 - (0.1919 * userAge) + (0.2278 * userHeight) - (0.402 * weight) - (0.0514 * bia);
    }

    public double getBMRWithWeight(Context context, double weight, double bia) {
        if (bia == 0) {
            return 0;
        }
        double userHeight = PreferenceUtil.getWeightScaleUserHeight(context).doubleValue();
        int userAge = PreferenceUtil.getWeightScaleUserAge(context).intValue();
        int userGender = PreferenceUtil.getWeightScaleUserGender(context).intValue();

        if (userGender == LifevitSDKConstants.GENDER_MALE) {
            return 180.3347 - 2.4414 * userAge + 6.7997 * userHeight + 12.5974 * weight - 1.0073 * bia;
        }
        return 382.3347 - 2.441487 * userAge + 4.5998 * userHeight + 14.5974 * weight - 1.197371 * bia;
    }

    public double getBoneWithWeight(Context context, double weight, double bia) {
        if (bia == 0) {
            return 0;
        }
        double userHeight = PreferenceUtil.getWeightScaleUserHeight(context).doubleValue();
        int userAge = PreferenceUtil.getWeightScaleUserAge(context).intValue();
        int userGender = PreferenceUtil.getWeightScaleUserGender(context).intValue();

        if (userGender == LifevitSDKConstants.GENDER_MALE) {
            return 1.3991 - 0.00213 * userAge + 0.0105 * userHeight + 0.0205 * weight - 0.0026 * bia;
        }
        return 2.1191 - 0.00213 * userAge + 0.0059 * userHeight + 0.010501 * weight - 0.001599 * bia;
    }

    public double getVisceralWithWeight(Context context, double weight, double bia) {

        if (bia == 0) {
            return 0;
        }
        int userAge = PreferenceUtil.getWeightScaleUserAge(context).intValue();
        int userGender = PreferenceUtil.getWeightScaleUserGender(context).intValue();

        if (userGender == LifevitSDKConstants.GENDER_MALE) {
            return ((578.09 * this.getBMIWithWeight(context, weight) * 10 + 16.90 * userAge + 934.18 * 1 - 328.7) / 10000 - 6.0);
        }
        return (578.09 * this.getBMIWithWeight(context, weight) * 10 + 16.90 * userAge + 934.18 * 2 - 328.7) / 10000 / 3;
    }

    public double getBMIWithWeight(Context context, double weight) {
        double userHeight = PreferenceUtil.getWeightScaleUserHeight(context).doubleValue();
        double h = userHeight / 100.0;
        double factor = 2.0;
        double imc = weight / Math.pow(h, factor);

        return imc;
    }

    public double getProteinPercentage(double musclePercent) {
        if (musclePercent > 0) {
            return musclePercent / 2 - 3;
        }
        return 0;
    }

    public double getIdealBodyWeight(Context context) {
        double userHeight = PreferenceUtil.getWeightScaleUserHeight(context).doubleValue();
        double h = userHeight / 100.0;
        double factor = 2.0;
        return 21.8 * Math.pow(h, factor);
    }

    public double getObesityPercentage(Context context, double weight) {
        double ideal = this.getIdealBodyWeight(context);
        return (weight - ideal) / ideal * 100;
    }

    public double getBodyAge(Context context, double bmi) {
        int userAge = PreferenceUtil.getWeightScaleUserAge(context).intValue();
        return bmi * 10 * userAge / 240;
    }

    // endregion

}