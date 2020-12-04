import exception.SliceDownloaderManagerException;
import exception.SlicePageContentFetcherException;
import model.SliceDetail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SliceDownloaderManager {

    private Integer page = 1;
    private SliceImageDownloader sliceImageDownloader = new SliceImageDownloader();

    public void downloadSliceContent(String path) throws SliceDownloaderManagerException {

        List<String> imagesUrls = getSliceImagesUrls();

        downloadImages(imagesUrls, path);
    }

    private List<String> getSliceImagesUrls() throws SliceDownloaderManagerException {
        List<String> urls = new ArrayList<>();

        boolean continueToFetchPages = true;

        while(continueToFetchPages) {

            List<SliceDetail> slicePage;

            try {
                slicePage = SlicePageContentFetcher.fetch(page);
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

            String fullImageUrl = sliceDetail.getThumb().replace("430x764", "base");

            urls.add(fullImageUrl);
        }

        return urls;
    }

    private void downloadImages(List<String> urls, String path) {

        List<CompletableFuture> completableFutures = new ArrayList<>();

        for (final String url: urls) {

            completableFutures.add(sliceImageDownloader.downloadWithRetry(url, path + "/" + url.substring(url.lastIndexOf("/") + 1))
                .whenCompleteAsync((response, t) -> {
                    if (t != null) {
                        System.out.println("Erreur lors du téléchargement de l'image: " + url);
                    }
                })
            );
        }

        CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[completableFutures.size()]))
            .join();
    }
}
