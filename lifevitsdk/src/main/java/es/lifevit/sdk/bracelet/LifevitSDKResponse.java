package es.lifevit.sdk.bracelet;

public class LifevitSDKResponse {
    private Integer error;
    private Integer action;
    private String message;
    private Object data;

    public static LifevitSDKResponse error(int action, int error, String message){
        LifevitSDKResponse r = new LifevitSDKResponse();
        r.setError(error);
        r.setMessage(message);
        r.setAction(action);

        return r;
    }


    public static LifevitSDKResponse success(int action, Object data){
        LifevitSDKResponse r = new LifevitSDKResponse();
        r.setData(data);
        r.setAction(action);
        return r;
    }

    public boolean isError(){
        return error!=null;
    }

    public Integer getError() {
        return error;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public Integer getAction() {
        return action;
    }

    public void setError(Integer error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
