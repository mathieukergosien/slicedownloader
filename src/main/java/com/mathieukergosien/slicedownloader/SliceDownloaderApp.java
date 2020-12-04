package com.mathieukergosien.slicedownloader;

import com.mathieukergosien.slicedownloader.exception.SliceDownloaderManagerException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SliceDownloaderApp {

    public static void main(String[] args) {
        if (args == null || args.length == 0 || args[0] == null || args[0] == "") {
            System.out.println("Error: You must pas the folder in which you want to download the Slices images in argument.");
            return;
        }

        SliceDownloaderManager sliceDownloaderManager = new SliceDownloaderManager();

        try {
            sliceDownloaderManager.downloadSliceContent(args[0]);
        } catch (SliceDownloaderManagerException e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }

        System.out.println("Images downloaded with success in the folder: \"" + args[0] + "\"");

        System.exit(0);
    }
}
