package com.example.DronLocalizationBackend.droneRecordings;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DroneListEntry {

    private long id;
    private double version;
    private String filename;
    private LocalDateTime timestamp;


}
