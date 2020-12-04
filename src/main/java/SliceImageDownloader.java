import exception.SliceImageDownloaderException;
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

    private AsyncHttpClient client = Dsl.asyncHttpClient();

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
                            System.out.println("Essai n°" + (RETRY_COUNT - retryCount + 1) + " Echec du téléchargement de l'image \"" + url + "\"");
                            downloadWithRetry(url, fileName, retryCount - 1);
                        } else {
                            future.completeExceptionally(t);
                        }
                    } else {
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
                    return stream;
                }
            });

        } catch (FileNotFoundException e) {
            throw new SliceImageDownloaderException("Impossible de créer le fichier: " + fileName, e);
        }
    }
}
