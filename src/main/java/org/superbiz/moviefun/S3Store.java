package org.superbiz.moviefun;

import com.amazonaws.AmazonWebServiceClient;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.IOException;
import java.util.Optional;

public class S3Store implements BlobStore {
    AmazonS3Client amazonS3Client;
    String photoStorageBucket;


    public S3Store(AmazonS3Client amazonS3Client, String photoStorageBucket) {
        this.amazonS3Client = amazonS3Client;
        this.photoStorageBucket = photoStorageBucket;
    }

    @Override
    public void put(Blob blob) throws IOException {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(blob.contentType);
        amazonS3Client.putObject(photoStorageBucket,blob.name,blob.inputStream,objectMetadata);
    }

    @Override
    public Optional<Blob> get(String name) throws IOException {
        S3Object s3Object = amazonS3Client.getObject(photoStorageBucket,name);
        Blob blob = new Blob(name,s3Object.getObjectContent(),s3Object.getObjectMetadata().getContentType());
        return Optional.of(blob);
    }

    @Override
    public void deleteAll() {

    }
}
