package com.mathieukergosien.slicedownloader;

import com.mathieukergosien.slicedownloader.exception.SliceDownloaderManagerException;
import com.mathieukergosien.slicedownloader.exception.SlicePageContentFetcherException;
import com.mathieukergosien.slicedownloader.model.SliceDetail;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class SliceDownloaderManager {

    private static final String THUMBNAIL_URL_PATH = "430x764";
    private static final String BASE_IMAGE_URL_PATH = "base";
    private static final int DOWNLOAD_IMAGES_BATCHES_SIZE = 200;

    private SliceImageDownloader sliceImageDownloader;
    private SlicePageContentFetcher slicePageContentFetcher;

    public SliceDownloaderManager() {
        sliceImageDownloader = new SliceImageDownloader();
        slicePageContentFetcher = new SlicePageContentFetcher();
    }

    public void downloadSliceContent(String path) throws SliceDownloaderManagerException {

        List<String> imagesUrls = getSliceImagesUrls();

        downloadImages(imagesUrls, path);
    }

    private List<String> getSliceImagesUrls() throws SliceDownloaderManagerException {
        List<String> urls = new ArrayList<>();

        boolean continueToFetchPages = true;

        Integer page = 1;

        while(continueToFetchPages) {

            List<SliceDetail> slicePage;

            try {
                slicePage = slicePageContentFetcher.fetch(page).join();
            } catch (SlicePageContentFetcherException e) {
                throw new SliceDownloaderManagerException(e);
            }

            urls.addAll(getImagesUrlsFromPage(slicePage));

            continueToFetchPages = slicePage != null && slicePage.size() > 0;
            page++;
        }

        return urls;
    }

    private List<String> getImagesUrlsFromPage(List<SliceDetail> slicePage) {

        List<String> urls = new ArrayList<>();

        for (SliceDetail sliceDetail: slicePage) {

            if (null == sliceDetail || null == sliceDetail.getThumb()) {
                continue;
            }

            String fullImageUrl = sliceDetail.getThumb().replace(THUMBNAIL_URL_PATH, BASE_IMAGE_URL_PATH);

            urls.add(fullImageUrl);
        }

        return urls;
    }

    private void downloadImages(List<String> urls, String path) {

        List<CompletableFuture> completableFutures = new ArrayList<>();

        int i = 0;
        for (final String url: urls) {

            i++;

            if (i > 1 && i % DOWNLOAD_IMAGES_BATCHES_SIZE == 0) {
                waitForBatchDownloadIsFinished(completableFutures);
                completableFutures.clear();
            }

            completableFutures.add(sliceImageDownloader.downloadWithRetry(url, path + "/" + url.substring(url.lastIndexOf("/") + 1))
                .whenCompleteAsync((response, t) -> {
                    if (t != null) {
                        log.warn("Failure when downloading the image: \"" + url + "\"");
                    }
                })
            );
        }

        waitForBatchDownloadIsFinished(completableFutures);
    }

    private void waitForBatchDownloadIsFinished(List<CompletableFuture> completableFutures) {
        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
            .join();
    }
}
