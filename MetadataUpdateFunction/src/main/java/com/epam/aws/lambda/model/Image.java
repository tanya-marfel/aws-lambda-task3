package com.epam.aws.lambda.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {

    private UUID id;
    private LocalDateTime dateCreated;
    private URL imageLocation;
    private String imageName;

}
