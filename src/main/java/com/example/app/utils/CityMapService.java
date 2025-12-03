package com.example.app.utils;

import android.util.Log;


import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CityMapService {

    private static final String TAG = "CityMapService";

    private static final String BASE_URL = "http://cutmap-api.azurewebsites.net/ServiceCityMap";

    private static final String NAMESPACE = "http://citymapsoap.com/service/";
    private static final int TIMEOUT_MS = 60_000;
    private static CityMapService instance;


    public static synchronized CityMapService getInstance() {
        if (instance == null) {
            instance = new CityMapService();
        }
        return instance;
    }


    public String getFragmentOfMap(int x1, int y1, int x2, int y2) {
        HttpTransportSE http = new HttpTransportSE(BASE_URL, TIMEOUT_MS);
        http.debug = true;

        try {
            SoapObject request = new SoapObject(NAMESPACE, "GetFragmentOfMap");

            request.addProperty(makeIntProp("X1", x1));
            request.addProperty(makeIntProp("Y1", y1));
            request.addProperty(makeIntProp("X2", x2));
            request.addProperty(makeIntProp("Y2", y2));

            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.dotNet = false;
            envelope.implicitTypes = true;
            envelope.setAddAdornments(false);
            envelope.setOutputSoapObject(request);

            http.call(null, envelope);

            String xml = http.responseDump;
            Log.d(TAG, "RESPONSE:\n" + xml);

            String startTag = "<ImageInBase64>";
            String endTag = "</ImageInBase64>";

            int start = xml.indexOf(startTag);
            int end = xml.indexOf(endTag);

            if (start == -1 || end == -1 || end <= start) {
                return "ERROR: tag ImageInBase64 not found";
            }

            return xml.substring(start + startTag.length(), end).trim();

        } catch (Exception e) {
            Log.e(TAG, "SOAP ERROR", e);
            Log.d(TAG, "REQUEST DUMP:\n" + http.requestDump);
            Log.d(TAG, "RESPONSE DUMP:\n" + http.responseDump);

            return "ERROR: " + e;
        }
    }

    private PropertyInfo makeIntProp(String name, int value) {
        PropertyInfo pi = new PropertyInfo();
        pi.setName(name);
        pi.setNamespace(NAMESPACE);
        pi.setValue(value);
        pi.setType(Integer.class);
        return pi;
    }

    public String getInitialMap() {
        HttpTransportSE http = new HttpTransportSE(BASE_URL, TIMEOUT_MS);
        http.debug = true;

        try {
            SoapObject request = new SoapObject(NAMESPACE, "GetInitialMap");

            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.dotNet = false;
            envelope.implicitTypes = true;
            envelope.setAddAdornments(false);
            envelope.setOutputSoapObject(request);

            http.call(null, envelope);

            String xml = http.responseDump;
            Log.d(TAG, "RESPONSE (GetInitialMap):\n" + xml);

            String startTag = "<ImageInBase64>";
            String endTag = "</ImageInBase64>";

            int start = xml.indexOf(startTag);
            int end = xml.indexOf(endTag);

            if (start == -1 || end == -1 || end <= start) {
                return "ERROR: tag ImageInBase64 not found";
            }

            return xml.substring(start + startTag.length(), end).trim();

        } catch (Exception e) {
            Log.e(TAG, "SOAP ERROR (GetInitialMap)", e);
            Log.d(TAG, "REQUEST DUMP:\n" + http.requestDump);
            Log.d(TAG, "RESPONSE DUMP:\n" + http.responseDump);

            return "ERROR: " + e;
        }
    }

    private String readResponse(HttpURLConnection conn) throws IOException {
        int code = conn.getResponseCode();
        BufferedReader br;
        if (code >= 200 && code < 300) {
            br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        } else {
            br = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
        }

        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line).append('\n');
        }
        br.close();

        Log.d(TAG, "HTTP " + code + " RESPONSE:\n" + sb.toString());
        return sb.toString();
    }

    public String getFragmentOfMapByCoordinate(Double rLat, Double rLon, Double lLat, Double lLon) {
        HttpTransportSE http = new HttpTransportSE(BASE_URL, TIMEOUT_MS);
        http.debug = true;

        try {
            SoapObject request = new SoapObject(NAMESPACE, "GetFragmentOfMapUsingGeoCoordinates");

            request.addProperty(makeStringProp("Lat1", String.valueOf(rLat)));
            request.addProperty(makeStringProp("Lon1", String.valueOf(rLon)));
            request.addProperty(makeStringProp("Lat2", String.valueOf(lLat)));
            request.addProperty(makeStringProp("Lon2", String.valueOf(lLon)));

            SoapSerializationEnvelope envelope =
                    new SoapSerializationEnvelope(SoapEnvelope.VER11);

            envelope.dotNet = false;
            envelope.implicitTypes = true;
            envelope.setAddAdornments(false);
            envelope.setOutputSoapObject(request);

            http.call(null, envelope);

            String xml = http.responseDump;
            Log.d(TAG, "RESPONSE:\n" + xml);

            String startTag = "<ImageInBase64>";
            String endTag = "</ImageInBase64>";

            int start = xml.indexOf(startTag);
            int end = xml.indexOf(endTag);

            if (start == -1 || end == -1 || end <= start) {
                return "ERROR: tag ImageInBase64 not found";
            }

            return xml.substring(start + startTag.length(), end).trim();

        } catch (Exception e) {
            Log.e(TAG, "SOAP ERROR", e);
            Log.d(TAG, "REQUEST DUMP:\n" + http.requestDump);
            Log.d(TAG, "RESPONSE DUMP:\n" + http.responseDump);

            return "ERROR: " + e;
        }
    }

    private PropertyInfo makeStringProp(String name, String value) {
        PropertyInfo pi = new PropertyInfo();
        pi.setName(name);
        pi.setNamespace(NAMESPACE);
        pi.setValue(value);
        pi.setType(String.class);
        return pi;
    }
}
