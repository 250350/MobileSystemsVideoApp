package com.example.DronLocalizationBackend.droneRecordings.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DroneRecordingUploadRequest {

    private byte[] data;

}
