package com.mathieukergosien.slicedownloader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mathieukergosien.slicedownloader.exception.SlicePageContentFetcherException;
import com.mathieukergosien.slicedownloader.model.SliceDetail;
import org.asynchttpclient.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SlicePageContentFetcher {

    private static final String URL = "https://www.factornews.com/xhr/loadmoreslices/";
    private static final String PAGE_FORMPARAM = "loadpage";

    private AsyncHttpClient client;

    public SlicePageContentFetcher() {
        client = HttpClient.getInstance();
    }

    public CompletableFuture<List<SliceDetail>> fetch(Integer page) throws SlicePageContentFetcherException {

        ObjectMapper mapper = new ObjectMapper();

        return client.preparePost(URL)
            .addFormParam(PAGE_FORMPARAM, page.toString())
            .execute(new AsyncCompletionHandler<List<SliceDetail>>() {
                @Override
                public List<SliceDetail> onCompleted(Response response) throws Exception {
                    return mapper.readValue(response.getResponseBody(), mapper.getTypeFactory().constructCollectionType(List.class, SliceDetail.class));
                }
            })
            .toCompletableFuture();
    }
}
