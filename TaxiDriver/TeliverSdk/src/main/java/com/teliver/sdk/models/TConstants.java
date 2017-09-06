package com.teliver.sdk.models;

public interface TConstants {

    int PERMISSION_REQ_CODE = 102, PERMISSION_ENABLE_CODE = 106;

    int TYPE_LOCATION = 1, TYPE_CMD = 2;

    String AUTH_TOKEN = "dots_auth_token", API_KEY = "dots_app_key",
            TRIP_DETAILS = "dots_trip_data",USER_ID="teliver_user_id";

    String T_PUSH_TOKEN = "t_push_token";

    String DOTS_OBJ = "dots_object";

    String CMD_TRIP_START = "teliver_start_trip",CMD_EVENT_PUSH="teliver_event_push";

    String PUSH_TITLE="title",PUSH_DATA="data",PUSH_IDS="ids";

}
