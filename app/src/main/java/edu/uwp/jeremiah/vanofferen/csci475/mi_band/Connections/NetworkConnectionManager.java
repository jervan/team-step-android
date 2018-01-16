package edu.uwp.jeremiah.vanofferen.csci475.mi_band.Connections;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.ActivityData;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Group;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Leader;
import edu.uwp.jeremiah.vanofferen.csci475.mi_band.Models.Member;

/**
 * Created by Jeremiah on 10/28/16.
 */

public class NetworkConnectionManager<Params, Progress, Post> extends AsyncTask<Params, Progress, Post> {


    public enum RequestType {
        POST_MEMBER,
        POST_ACTIVITY_DATA,
        POST_GROUP,
        GET_GROUP,
        GET_GROUPS,
        GET_LOGIN,
        PUT_MEMBER,
        DELETE_MEMBER,
    }

    private static final String TAG = "Network Connection";
    private static final String API_KEY = "b8cabb605b097fa12e903b261db15f45";
    private static final String BASE_URL = "http://teamstep.cs.uwp.edu/api/";
    private static final String LEADER_POST_URL = BASE_URL + "leader";
    private static final String MEMBER_POST_URL = BASE_URL + "member";
    private static final String GROUP_POST_URL = BASE_URL + "groups";
    private static final String MEMBER_ACTIVITY_DATA_POST_URL = BASE_URL + "activityData";
    private static final String LEADER_ACTIVITY_DATA_POST_URL = BASE_URL + "leaderActivityData";
    private static final String MEMBER_PUT_URL = BASE_URL + "member/anything";
    private static final String LEADER_PUT_URL = BASE_URL + "leader/anything";
    private static final String GROUPS_GET_URL = BASE_URL + "groups";
    private static final String GROUP_GET_URL = BASE_URL + "groupView";
    private static final String MEMBER_DELETE_URL = BASE_URL + "member/edit";
    private static final String LOGIN_URL = BASE_URL + "leader/show";

    private RequestType mRequestType;

    private Gson gson = new GsonBuilder()
            .registerTypeAdapter(boolean.class, new BooleanTypeAdapter())
            .registerTypeAdapter(Boolean.class, new BooleanTypeAdapter())
            .create();

    public NetworkConnectionManager (RequestType requestType) {
        mRequestType = requestType;
    }

    @SafeVarargs
    @Override
    protected final Post doInBackground(Params... params) {
        if (params.length > 0)
            return getConnection(params[0]);
        else
            return getConnection(null);
    }


    private Post getConnection(Params param) {
        switch (mRequestType) {
            case POST_MEMBER:
                return memberPostRequest(param);

            case POST_ACTIVITY_DATA:
                return activityDataPostRequest(param);

            case POST_GROUP:
                return groupPostRequest(param);

            case GET_GROUP:
                return groupGetRequest(param);

            case GET_GROUPS:
                return groupsGetRequest();

            case GET_LOGIN:
                return loginGetRequest(param);

            case PUT_MEMBER:
                return memberPutRequest(param);

            case DELETE_MEMBER:
                return memberDeleteRequest(param);

        }
        return null;
    }

    // works for creating a member or a leader
    private Post memberPostRequest (Params param) {

        Member member;
        if (param instanceof Member) {
            member = (Member) param;
        } else {
            return null;
        }

        try {
            String url;
            String jsonObject;
            if (member instanceof Leader) {
                jsonObject = "{\"leader\":" + gson.toJson(member, new TypeToken<Leader>() {
                }.getType()) + "}";
                url = LEADER_POST_URL;
            } else {
                jsonObject = "{\"member\":" + gson.toJson(member, new TypeToken<Member>() {
                }.getType()) + "}";
                url = MEMBER_POST_URL;
            }
            member = null;

            Log.d(TAG, "Upload Data Sending: " + jsonObject);
            HttpURLConnection connection = getHttpURLConnection("POST", url, true, true);
            writeOutputStream(jsonObject, connection);

            // read input stream
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                member = parseMember(connection);
            }

            // log response code and message
            Log.d("Upload Data", "response code: " + connection.getResponseCode() + " response message: " + connection.getResponseMessage());
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return postCast(member);
    }

    private Post activityDataPostRequest (Params param) {

        ActivityData activityData;
        Integer stepCount = null;
        if (param instanceof ActivityData) {
            activityData = (ActivityData) param;
        } else {
            return null;
        }

        try {
            String url;
            String jsonObject;
            System.out.println(activityData.toString());
            jsonObject = "{\"activityData\":" + gson.toJson(activityData, new TypeToken<ActivityData>() {}.getType()) + "}";
            Log.d(TAG, jsonObject);

            if (activityData.isLeader()) {
                url = LEADER_ACTIVITY_DATA_POST_URL;
            } else {
                url = MEMBER_ACTIVITY_DATA_POST_URL;
            }

            HttpURLConnection connection = getHttpURLConnection("POST", url, true, true);
            writeOutputStream(jsonObject, connection);

            // read input stream
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {

                String response = getResponseString(connection);
                Log.d(TAG, "Post response: " + response);
                JSONObject responseJSON = new JSONObject(response);

                if (responseJSON.has("stepCount")) {
                    stepCount = responseJSON.getInt("stepCount");
                }
            }

            // log response code and message
            Log.d("Upload Data", "response code: " + connection.getResponseCode() + " response message: " + connection.getResponseMessage());
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return postCast(stepCount);
    }

    private Post groupPostRequest (Params param) {

        Group group;
        if (param instanceof Group) {
            group = (Group) param;
        } else {
            return null;
        }

        try {
            String url = GROUP_POST_URL;
            String jsonObject;
            jsonObject = "{\"group\":" + gson.toJson(group, new TypeToken<Group>(){}.getType()) + "}";
            group = null;

            Log.d(TAG, "Upload Data Sending: " + jsonObject);
            HttpURLConnection connection = getHttpURLConnection("POST", url, true, true);
            writeOutputStream(jsonObject, connection);

            // read input stream
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                group = parseGroup(connection);
            }

            // log response code and message
            Log.d("Upload Data", "response code: " + connection.getResponseCode() + " response message: " + connection.getResponseMessage());
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return postCast(group);
    }

    private Post groupGetRequest (Params param) {

        Leader leader;
        Group group = null;
        if (param instanceof Leader) {
            leader = (Leader) param;
        } else {
            return null;
        }

        try {
            String url = GROUP_GET_URL + "/" + leader.getDefaultGroup();
            HttpURLConnection connection = getHttpURLConnection("GET", url, false, true);

            // read input stream
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                group = parseGroup(connection);
            }

            // log response code and message
            Log.d("Upload Data", "response code: " + connection.getResponseCode() + " response message: " + connection.getResponseMessage());
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (group != null) {
            group.setLeader(leader);
        }

        return postCast(group);
    }

    private Post groupsGetRequest () {
        List<Group> groups = null;
        try {

            String url = GROUPS_GET_URL;
            HttpURLConnection connection = getHttpURLConnection("GET", url, false, true);

            // read input stream
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                groups = parseGroups(connection);
            }

            // log response code and message
            Log.d("Upload Data", "response code: " + connection.getResponseCode() + " response message: " + connection.getResponseMessage());
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return postCast(groups);
    }

    private Post loginGetRequest(Params param) {

        Leader leader;
        if (param instanceof Leader) {
            leader = (Leader) param;
        } else {
            return null;
        }

        try {

            String url = LOGIN_URL + "/username=" + leader.getUsername() + "&password=" +leader.getPassword();
            leader = null;
            HttpURLConnection connection = getHttpURLConnection("GET", url, false, true);

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                leader = parseLeader(connection);
                Log.d(TAG, "Leader object: " + (leader != null ? leader.toString() : "null"));
            }

            // log response code and message
            Log.d("Upload Data", "response code: " + connection.getResponseCode() + " response message: " + connection.getResponseMessage());
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return postCast(leader);
    }

    private Post memberPutRequest (Params param) {

        Member member;
        if (param instanceof Member) {
            member = (Member) param;
        } else {
            return null;
        }

        try {

            String url;
            String jsonObject;
            if (member instanceof Leader) {
                jsonObject = "{\"leader\":" + gson.toJson(member, new TypeToken<Leader>() {
                }.getType()) + "}";
                url = LEADER_PUT_URL;
            } else {
                jsonObject = "{\"member\":" + gson.toJson(member, new TypeToken<Member>() {
                }.getType()) + "}";
                url = MEMBER_PUT_URL;
            }

            Log.d(TAG, "Upload Data Sending: " + jsonObject);
            HttpURLConnection connection = getHttpURLConnection("PUT", url, true, false);
            writeOutputStream(jsonObject, connection);

            // log response code and message
            Log.d("Upload Data", "response code: " + connection.getResponseCode() + " response message: " + connection.getResponseMessage());

            // read input stream
            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                connection.disconnect();
                return postCast(member);
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Post memberDeleteRequest(Params param) {

        Member member;
        if (param instanceof Member) {
            member = (Member) param;
        } else {
            return null;
        }

        try {

            String url = MEMBER_DELETE_URL + "/id=" + member.getId();
            HttpURLConnection connection = getHttpURLConnection("DELETE", url, false, false);

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                return postCast(true);
            }

            // log response code and message
            Log.d("Upload Data", "response code: " + connection.getResponseCode() + " response message: " + connection.getResponseMessage());
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return postCast(false);
    }

    @SuppressWarnings("unchecked")
    private Post postCast(Object o) {
        Post post;
        try {
            post = (Post) o;
        } catch (ClassCastException e) {
            e.printStackTrace();
            post = null;
        }
        return post;
    }

    private HttpURLConnection getHttpURLConnection(String requestMethod, String url, boolean doesOutput, boolean doesInput) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setDoOutput(doesOutput);
        connection.setDoInput(doesInput);
        connection.setConnectTimeout(20000);
        connection.setRequestMethod(requestMethod);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Authorization", API_KEY);
        return connection;
    }

    private void writeOutputStream(String jsonObject, HttpURLConnection connection) throws IOException {
        OutputStreamWriter streamWriter = new OutputStreamWriter(connection.getOutputStream());
        streamWriter.write(jsonObject);
        streamWriter.close();
    }

    private String getResponseString(HttpURLConnection connection) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder stringBuilder = new StringBuilder();

        String line;
        while((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line);
        }
        bufferedReader.close();
        return stringBuilder.toString();
    }

    private Leader parseLeader(HttpURLConnection connection) throws IOException, JSONException {
        String response = getResponseString(connection);
        Log.d(TAG, "Post response: " + response);
        JSONObject responseJSON = new JSONObject(response);

        if (responseJSON.has("leader")) {
            return gson.fromJson(responseJSON.getJSONObject("leader").toString(),
                    new TypeToken<Leader>() {}.getType());
        } else {
            return null;
        }
    }

    private Member parseMember(HttpURLConnection connection) throws IOException, JSONException  {
        String response = getResponseString(connection);
        Log.d(TAG, "Post response: " + response);
        JSONObject responseJSON = new JSONObject(response);

        if (responseJSON.has("leader")) {
            return gson.fromJson(responseJSON.getJSONObject("leader").toString(),
                    new TypeToken<Leader>() {}.getType());
        } else if (responseJSON.has("member")) {
            return gson.fromJson(responseJSON.getJSONObject("member").toString(),
                    new TypeToken<Member>() {}.getType());
        } else {
            return null;
        }
    }

    private Group parseGroup(HttpURLConnection connection) throws IOException, JSONException {
        String response = getResponseString(connection);
        Log.d(TAG, "Post response: " + response);
        JSONObject responseJSON = new JSONObject(response);

        if (responseJSON.has("group")) {
            return gson.fromJson(responseJSON.getJSONObject("group").toString(),
                    new TypeToken<Group>() {}.getType());
        } else {
            return null;
        }
    }

    private ArrayList<Group> parseGroups(HttpURLConnection connection) throws IOException, JSONException {
        String response = getResponseString(connection);
        Log.d(TAG, "Post response: " + response);
        JSONObject responseJSON = new JSONObject(response);

        if (responseJSON.has("groups")) {
            return gson.fromJson(responseJSON.getJSONArray("groups").toString(),
                    new TypeToken<ArrayList<Group>>() {}.getType());
        } else {
            return null;
        }
    }

    private class BooleanTypeAdapter implements JsonDeserializer<Boolean>, JsonSerializer<Boolean> {

        public Boolean deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String bool = json.getAsString();
            if (bool.equalsIgnoreCase("Yes"))
                return true;
            else
                return false;
        }

        @Override
        public JsonElement serialize(Boolean src, Type typeOfSrc, JsonSerializationContext context) {
            if (src)
                return new JsonPrimitive("Yes");
            else
                return new JsonPrimitive("No");
        }
    }

}
