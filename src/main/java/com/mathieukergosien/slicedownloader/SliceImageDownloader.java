package com.mathieukergosien.slicedownloader;

import com.mathieukergosien.slicedownloader.exception.SliceImageDownloaderException;
import io.netty.handler.timeout.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.ConnectException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
public class SliceImageDownloader {

    private final static Integer RETRY_COUNT = 10;

    private AsyncHttpClient client;

    public SliceImageDownloader() {
        client = HttpClient.getInstance();
    }

    public CompletableFuture downloadWithRetry(String url, String fileName) {

        // We first check if file exists
        File file = new File(fileName);
        if (file.length() != 0) {
            log.debug("File {} already exists and is not empty, we don't download it", fileName);
            return CompletableFuture.completedFuture(null);
        }

        return downloadWithRetry(url, fileName, RETRY_COUNT);
    }

    private CompletableFuture downloadWithRetry(String url, String fileName, Integer retryCount) {
        final CompletableFuture future = new CompletableFuture();

        try {
            download(url, fileName)
                .toCompletableFuture()
                .whenCompleteAsync((response, t) -> {
                    if (t != null) {
                        if (retryCount > 0 && t instanceof ConnectException || t instanceof TimeoutException) {
                            log.debug("Attempt number {}: Failed to download the image \"{}\", launching new attempt...", (RETRY_COUNT - retryCount + 1), url);
                            downloadWithRetry(url, fileName, retryCount - 1).whenCompleteAsync((response2, t2) -> future.complete(response2));
                        } else {
                            log.warn("Attempt number {}: Failed to download the image \"{}\", aborting", (RETRY_COUNT - retryCount + 1), url);
                            future.completeExceptionally(t);
                        }
                    } else {
                        log.debug("Attempt number {}: Download the image \"{}\" was a success", (RETRY_COUNT - retryCount + 1), url);
                        future.complete(response);
                    }
                });
        } catch (SliceImageDownloaderException e) {
            throw new CompletionException(e);
        }

        return future;
    }

    private ListenableFuture<FileOutputStream> download(String url, String fileName) throws SliceImageDownloaderException {
        try {
            final FileOutputStream stream = new FileOutputStream(fileName);

            return client.prepareGet(url).execute(new AsyncCompletionHandler<FileOutputStream>() {

                @Override
                public State onBodyPartReceived(HttpResponseBodyPart bodyPart)
                        throws Exception {
                    stream.getChannel().write(bodyPart.getBodyByteBuffer());
                    return State.CONTINUE;
                }

                @Override
                public FileOutputStream onCompleted(Response response)
                        throws Exception {
                    stream.close();
                    return stream;
                }
            });

        } catch (FileNotFoundException e) {
            throw new SliceImageDownloaderException("Failure to create the file: " + fileName, e);
        }
    }
}
