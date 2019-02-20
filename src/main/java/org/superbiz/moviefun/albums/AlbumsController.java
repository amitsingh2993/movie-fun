package org.superbiz.moviefun.albums;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    @Autowired
    private BlobStore blobStore;
    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
    }


    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {
        String name = "cover_"+albumId;
        Blob blob = new Blob(name, uploadedFile.getInputStream(),uploadedFile.getContentType());
        blobStore.put(blob);
        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {
        String name = getFileName(albumId);
        Optional<Blob> blob = blobStore.get(name);
        byte [] imageBytes = null;
        HttpHeaders headers = new HttpHeaders();
        if(blob.isPresent()) {
            imageBytes = IOUtils.toByteArray(blob.get().inputStream);
            headers.setContentType(MediaType.parseMediaType(blob.get().contentType));
            headers.setContentLength(imageBytes.length);

        }
        return new HttpEntity<>(imageBytes, headers);
    }

    private String getFileName(@PathVariable long albumId){
        return "cover_"+albumId;

    }

}
