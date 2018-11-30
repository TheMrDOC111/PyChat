package com.doc.pychat;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;


class JsonParser {

    static String jsonEncrypt(String[] jsonString) {

        JSONObject jsonObject = new JSONObject();
        String message;

        try {
            String type = jsonString[0];
            jsonObject.put("type", type);
            if (type.equals("auth")) {
                jsonObject.put("login", jsonString[1]);
                jsonObject.put("password", jsonString[2]);
            } else if (type.equals("registration")) {
                jsonObject.put("login", jsonString[1]);
                jsonObject.put("password", jsonString[2]);
            } else if (type.equals("message")) {
                jsonObject.put("text", jsonString[1]);
                jsonObject.put("token", jsonString[2]);
            }

            message = jsonObject.toString();
        } catch (JSONException e) {
            e.getStackTrace();
            return null;
        }
        return message;
    }

    static Message jsonDecrypt(String jsonString) {

        Message message = new Message();
        try {
            JSONObject jo = new JSONObject(jsonString);
            message.setType(jo.getString("type"));

            switch (message.getType()) {
                case "auth":
                    message.setStatus(jo.getBoolean("status"));
                    message.setText(jo.getString("text"));
                    message.setToken(jo.getString("token"));
                    break;
                case "registration":
                    message.setStatus(jo.getBoolean("status"));
                    message.setText(jo.getString("text"));
                    message.setToken(jo.getString("token"));
                    break;
                case "message":
                    message.setStatus(jo.getBoolean("status"));
                    message.setUserName(jo.getString("user"));
                    message.setText(jo.getString("text"));
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            Log.d("Client socket", e.toString());
        }
        return message;
    }
}
