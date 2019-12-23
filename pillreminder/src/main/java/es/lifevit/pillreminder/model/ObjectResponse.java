package es.lifevit.pillreminder.model;


import es.lifevit.pillreminder.constants.AppConstants;

/**
 * Represents a REST response (with error code and result object).
 */
public class ObjectResponse {

    private int error;
    private Object object;

    public ObjectResponse() {
        this.error = -1;
    }

    public ObjectResponse(int error, Object object) {
        super();
        this.error = error;
        this.object = object;
    }

    public final int getError() {
        return error;
    }

    public final void setError(int error) {
        this.error = error;
    }

    public final Object getObject() {
        return object;
    }

    public final void setObject(Object object) {
        this.object = object;
    }

    public static ObjectResponse getSimpleErrorResult() {
        return new ObjectResponse(AppConstants.ERROR_UNKNOWN, "Unknown Error");
    }

    public static ObjectResponse getResult(int result, Object object) {
        return new ObjectResponse(result, object);
    }

    public boolean isResponseOk() {
        return (error == AppConstants.RESPONSE_OK);
    }
}
