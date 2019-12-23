package es.lifevit.sdk.utils;


import java.util.List;
import java.util.UUID;

public class BLEAdvertisedData {
    private List<UUID> mUuids;
    private String mName;
    public BLEAdvertisedData(List<UUID> uuids, String name){
        mUuids = uuids;
        mName = name;
    }

    public List<UUID> getUuids(){
        return mUuids;
    }

    public String getName(){
        return mName;
    }
}