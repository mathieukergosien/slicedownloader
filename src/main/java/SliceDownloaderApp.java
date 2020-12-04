import exception.SliceDownloaderManagerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SliceDownloaderApp {

    public static void main(String[] args) {
        if (args[0] == null || args[0] == "") {
            System.out.println("Vous devez passer le chemin où seront téléchargées les images Slice");
        }

        SliceDownloaderManager sliceDownloaderManager = new SliceDownloaderManager();

        try {
            sliceDownloaderManager.downloadSliceContent(args[0]);
        } catch (SliceDownloaderManagerException e) {
            log.error(e.getMessage(), e);
        }

        System.out.println("Images téléchargées avec succès dans le répertoire: \"" + args[0] + "\"");
    }
}
