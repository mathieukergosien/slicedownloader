package com.mathieukergosien.slicedownloader;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;

public class HttpClient {

    private static HttpClient instance;
    private AsyncHttpClient client;

    static {
        instance = new HttpClient();
    }

    public static AsyncHttpClient getInstance() {
        return instance.client;
    }

    public HttpClient() {
        client = Dsl.asyncHttpClient();
    }
}
