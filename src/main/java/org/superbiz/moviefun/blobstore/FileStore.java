package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import software.amazon.ion.IonException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        String coverFileName = coverFilePath(blob.name);
        File targetFile = new File(coverFileName);
        saveFileToDisk(targetFile, blob);
    }

    private void saveFileToDisk(File targetFile, Blob blob) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();
        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(IOUtils.toByteArray(blob.inputStream));
        }
    }

    private String coverFilePath(String name) {
        String coverFileName = format("covers/%d", name);
        return coverFileName;
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        Path coverFilePath = null;
        try {
            coverFilePath = getExistingCoverPath(name);
        } catch (URISyntaxException e) {
            throw new IonException(e);
        }
        byte[] imageBytes = readAllBytes(coverFilePath);
        String contentType = new Tika().detect(coverFilePath);
        Blob blob = new Blob(name, IOUtils.toInputStream(IOUtils.toString(imageBytes)), contentType);
        return Optional.of(blob);
    }

    private Path getExistingCoverPath(String name) throws URISyntaxException {
        File coverFile = new File(coverFilePath(name));
        Path coverFilePath;
        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }
        return coverFilePath;
    }

    @Override
    public void deleteAll() {
        // ...
    }
}