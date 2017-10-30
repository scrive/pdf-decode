package com.scrive.decode;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.event.S3EventNotification;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.itextpdf.text.DocumentException;
import org.apache.commons.io.FilenameUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Lambda implements RequestHandler<S3Event, String> {
    @Override
    public String handleRequest(S3Event event, Context context) {
        System.out.println("Handling request");

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();

        for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {
            String bucket = record.getS3().getBucket().getName();
            String key = record.getS3().getObject().getUrlDecodedKey();
            S3Object obj = client.getObject(new GetObjectRequest(bucket, key));
            System.out.printf("Processing %s\n", key);

            try {
                System.out.printf("Decoding %s\n", key);
                Pdf pdf = new Pdf(obj.getObjectContent());
                byte[] content = gson.toJson(pdf).getBytes(StandardCharsets.UTF_8);
                System.out.printf("Decoded %s\n", key);

                ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(content.length);
                InputStream stream = new ByteArrayInputStream(content);

                String jsonFile = String.format("%s.json", FilenameUtils.getBaseName(key));
                String jsonKey = FilenameUtils.concat("decoded/", jsonFile);

                System.out.printf("Uploading %s\n", jsonKey);
                client.putObject(bucket, jsonKey, stream, meta);
                System.out.printf("Uploaded %s\n", jsonKey);
            } catch (IOException|DocumentException e) {
                System.out.println(e.toString());
                return e.toString();
            }

            System.out.printf("Processed %s\n", key);
        }

        System.out.println("Handled request");

        return "Ok";
    }
}
