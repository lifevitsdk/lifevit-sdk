package es.lifevit.pillreminder.constants

object AppConstants {


    // region --- Medications

    const val MEDICATION_ADIRO = 1
    const val MEDICATION_ENANTYUM = 2
    const val MEDICATION_EUTIROX = 3
    const val MEDICATION_IBUPROFENO = 4
    const val MEDICATION_NOLOTIL = 5
    const val MEDICATION_ORFIDAL = 6
    const val MEDICATION_PARACETAMOL = 7
    const val MEDICATION_SINTROM = 8
    const val MEDICATION_TRANKIMAZIN = 9
    const val MEDICATION_VENTOLIN = 10

    // endregion


    // region (ti.care) Colors

    const val TICARE_COLOR_GREEN = 1
    const val TICARE_COLOR_PURPLE = 2
    const val TICARE_COLOR_RED = 3
    const val TICARE_COLOR_YELLOW = 4
    const val TICARE_COLOR_BLUE = 5

    // endregion


    // region Periodicities

    const val PERIODICITY_NEVER = 0
    const val PERIODICITY_24H = 1
    const val PERIODICITY_12H = 2
    const val PERIODICITY_8H = 3
    const val PERIODICITY_6H = 4
    const val PERIODICITY_4H = 5
    const val PERIODICITY_1min = 10

    // endregion


    // region --- Preferences

    const val PREF_OAUTH_ACCESS_TOKEN = "PREF_OAUTH_ACCESS_TOKEN"
    const val PREF_USER_LOGGED_ID = "PREF_USER_LOGGED_ID"

    // endregion


    // region --- Request codes

    const val REQUEST_CODE_LOCATION_PERMISSIONS = 100

    // endregion


    // region --- Request codes

    const val SCAN_PERIOD = 60000L

    // endregion


    // region --- WS Response codes

    const val ERROR_EXCEPTION = -999
    const val ERROR_UNKNOWN = -1000

    const val RESPONSE_OK = 200
    const val RESPONSE_GENERAL_ERROR = 400
    const val RESPONSE_ERROR_UNAUTHORIZED = 401

    // endregion


    // region WS timeout

    const val REST_TIMEOUT = 60000

    // endregion


    // region WS URLs

    const val REST_URL_TOKEN = "usuarios/token"
    const val REST_URL_LOGIN = "usuarios/login"
    const val REST_URL_LOGOUT = "usuarios/logout"
    const val REST_URL_GET_PRESCRIPTIONS = "formularios/0a0d5f69-87f2-4eeb-80a4-f330e2cde04e/submissions"
    const val REST_URL_SET_PRESCRIPTIONS = "entradas"

    // endregion


    // region WS Params

    const val REST_HEADER_TOKEN = "X-Csrf-Token"
    const val REST_PARAM_USERNAME = "username"
    const val REST_PARAM_PASSWORD = "password"

    const val REST_PARAM_WEBFORM = "webform"
    const val REST_PARAM_SUBMISSION = "submission"

    // endregion


    // region JSON parsing

    const val JSON_TOKEN = "token"

    const val JSON_USER = "user"
    const val JSON_UID = "uid"

    const val JSON_DATA = "data"
    const val JSON_FORM_KEY = "form_key"
    const val JSON_VALUES = "values"

    const val JSON_PATIENT = "paciente"
    const val JSON_MEDICATION = "medicacion"
    const val JSON_COLOR = "color"
    const val JSON_QUANTITY = "cantidad"
    const val JSON_INDICATIONS = "indicaciones"
    const val JSON_TAKE_PILL_TIME = "hora_de_la_toma"
    const val JSON_START_DATE = "fecha_de_inicio"
    const val JSON_END_DATE = "fecha_de_fin"
    const val JSON_REPETITION_PATTERN = "pauta_de_repeticion"

    // endregion


}