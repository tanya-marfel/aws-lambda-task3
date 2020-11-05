package com.epam.aws.lambda;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.epam.aws.lambda.model.Image;
import com.epam.aws.lambda.util.DbConnection;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public class App {

    private final ObjectMapper mapper = new ObjectMapper();

    public Image handleRequest(SNSEvent input) {
        SNSEvent.SNSRecord record = input.getRecords().get(0);
        Image image = getImage(record);
        createImage(Optional.ofNullable(image).orElseThrow());
        return image;
    }

    private AmazonS3 getAmazonS3Client() {
        return AmazonS3ClientBuilder.standard()
                                    .withRegion(Regions.EU_CENTRAL_1)
                                    .build();
    }

    private Image getImage(SNSEvent.SNSRecord record) {
        try {
            JsonNode records = mapper.readTree(record.getSNS().getMessage());
            String bucketName = records.get("Records").get(0).get("s3").get("bucket").get("name").asText();
            String objectKey = records.get("Records").get(0).get("s3").get("object").get("key").asText();
            String normalizedFilename = objectKey.replace(" ", "_");

            return Image.builder()
                        .imageLocation(new URL(((AmazonS3Client) getAmazonS3Client())
                                .getResourceUrl(bucketName, normalizedFilename)))
                        .dateCreated(LocalDateTime.now())
                        .imageName(normalizedFilename)
                        .build();
        } catch (MalformedURLException e) {
            return new Image();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createImage(Image image) {
        DbConnection conn = new DbConnection();
        String query = "insert into images (id, image_location, date_created, image_name)"
                + " values (?, ?, ?, ?)";
        PreparedStatement stmt = null;
        try {
            stmt = conn.connect().prepareStatement(query);
            stmt.setObject(1, UUID.randomUUID());
            stmt.setString(2, image.getImageLocation().toString());
            stmt.setObject(3, image.getDateCreated());
            stmt.setString(4, image.getImageName());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
