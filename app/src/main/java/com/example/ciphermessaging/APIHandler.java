package com.example.ciphermessaging;

import android.net.Uri;
import android.util.JsonReader;
import android.util.Log;

import androidx.annotation.NonNull;

//import com.apollographql.apollo3.ApolloCall;
//import com.apollographql.apollo3.ApolloClient;
//import com.apollographql.apollo3.api.Adapter;
//import com.apollographql.apollo3.api.CompiledField;
//import com.apollographql.apollo3.api.CustomScalarAdapters;
//import com.apollographql.apollo3.api.Query;
//import com.apollographql.apollo3.api.json.JsonWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class APIHandler {
    private static final String TAG = "API_Handler";

    private static final String API_USER_LINK = "https://testing-prject.can.canonic.dev/api";
    private static final String API_KEY = "62478e48bcbe2400092182b5-20cdb8a3-4ee4-4a7d-a11d-cd399fa3baee";
    private static final String GET_SINGLE_TEXT = "/texts/:_id";
    private static final String API_ID = "authorization";
    private static final String CONTENT = "Content-type";
    private static final String FILE_TYPE = "application/json";

    public static void makeTextRequest()
    {
        String id = "6247c1eaaa234f000914c649";
        try
        {

            Uri uri =  Uri.parse(API_USER_LINK + GET_SINGLE_TEXT).buildUpon()
//                    .appendQueryParameter("_id", id)
                    .build();
            URL url = new URL(uri.toString());
//            ApolloClient apolloClient = new ApolloClient.Builder()
//                    .serverUrl("https://apollo-fullstack-tutorial.herokuapp.com/graphql")
//                    .build();

//
              HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty(API_ID, API_KEY);
            connection.setRequestProperty(CONTENT, FILE_TYPE);
            connection.setDoOutput(true);
            JSONObject testObject = new JSONObject();
            testObject.put("id", id);
//            String test = "{\"_id\": 6240bd89342cfd0009845f1f}";
            connection.getOutputStream().write(testObject.toString().getBytes());
//            connection.addRequestProperty();
//            connection.sey
            Log.d(TAG, testObject.toString());
                InputStream results = connection.getInputStream();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK)
            {
                Log.d(TAG, "Failed to process.");
            }
            JsonReader reader = new JsonReader(new InputStreamReader(results));
//            ByteArrayOutputStream output = new ByteArrayOutputStream();
//            int numBytesRead = 0;
//            byte[] dataLine = new byte[1024];
//            numBytesRead = results.read(dataLine);
//            while (numBytesRead > 0)
//            {
//                output.write(dataLine, 0, numBytesRead);
//                numBytesRead = results.read(dataLine);
//            }
//            output.close();
            reader.beginObject();
//            reader.setLenient(true);
            while (reader.hasNext())
            {
                Log.d(TAG, reader.nextName());
                Log.d(TAG, reader.nextBoolean() + "");
                Log.d(TAG, reader.nextName());
                Log.d(TAG, reader.nextString());
//                Log.d(TAG, reader.nextString());
            }
//            Log.d(TAG,);
//            Log.d(TAG, results.toString());
//            Log.d(TAG, "returned values " + data.toString());
        }
        catch (MalformedURLException mue)
        {
            Log.d(TAG, mue.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static void createText(String content)
    {
        try
        {
            JSONObject test = new JSONObject();
            test.put("content", content);
            Uri uri =  Uri.parse(API_USER_LINK).buildUpon()
                    .appendQueryParameter("input", test.toString())
                    .build();
            URL url = new URL(uri.toString());
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty(API_ID, API_KEY);
            connection.setRequestProperty(CONTENT, FILE_TYPE);
            InputStream results = connection.getInputStream();

            if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK)
            {
                Log.d(TAG, "Failed to process.");
            }

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            int numBytesRead = 0;
            byte[] dataLine = new byte[1024];
            numBytesRead = results.read(dataLine);
            while (numBytesRead > 0)
            {
                output.write(dataLine, 0, numBytesRead);
                numBytesRead = results.read(dataLine);
            }
            output.close();
            Log.d(TAG, new String(output.toByteArray()));
        }
        catch (MalformedURLException mue)
        {
            Log.d(TAG, mue.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}