package es.lifevit.sdk.bracelet;

import es.lifevit.sdk.LifevitSDKConstants;

public class LifevitSDKResponse {
    private LifevitSDKConstants.BraceletVitalCommand command;
    private LifevitSDKConstants.BraceletVitalDataType type;
    private Object data;

    public LifevitSDKResponse(LifevitSDKConstants.BraceletVitalCommand command, Object data){
        setData(data);
        setCommand(command);
    }

    public LifevitSDKResponse(LifevitSDKConstants.BraceletVitalCommand command,  LifevitSDKConstants.BraceletVitalDataType type, Object data){
        setData(data);
       setCommand(command);
        setType(type);
    }

    public void setType(LifevitSDKConstants.BraceletVitalDataType type) {
        this.type = type;
    }

    public LifevitSDKConstants.BraceletVitalDataType getType() {
        return type;
    }

    public LifevitSDKConstants.BraceletVitalCommand getCommand() {
        return command;
    }

    public void setCommand(LifevitSDKConstants.BraceletVitalCommand command) {
        this.command = command;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LifevitSDKResponse{" +
                "action=" + command +
                ", type=" + (type!=null?type.value:"")+
                ", data=" +  (data!=null?data.toString():"") +
                '}';
    }
}
