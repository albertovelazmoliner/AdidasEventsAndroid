package com.albertovelaz.adidasevents.models;

import org.json.JSONArray;

/**
 * Fieldaware©
 * Created by albertovelazmoliner on 19/06/16.
 */
public class JSONModels {

    public static class ApiResponse {
        String error;
        String message;
        JSONArray errors;

        public ApiResponse(String error, String message, JSONArray errors) {
            this.error = error;
            this.errors = errors;
            this.message = message;
        }

        public JSONArray getErrors() {
            return errors;
        }

        public String getMessage() {
            return message;
        }

        public String getError() {
            return error;
        }
    }


}
