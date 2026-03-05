package com.example.DronLocalizationBackend.droneRecordings.repository;

import com.example.DronLocalizationBackend.droneRecordings.DroneListEntry;
import com.example.DronLocalizationBackend.droneRecordings.entity.DroneRecording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DroneRecordingRepository extends JpaRepository<DroneRecording, Long> {

    @Query("SELECT new com.example.DronLocalizationBackend.droneRecordings.DroneListEntry(d.id, d.version, d.filename, d.timestamp) " +
            "FROM DroneRecording d ORDER BY d.timestamp DESC")
    List<DroneListEntry> findAllDroneListEntries();

    @Query("SELECT new com.example.DronLocalizationBackend.droneRecordings.DroneListEntry(d.id, d.version, d.filename, d.timestamp) " +
            "FROM DroneRecording d JOIN d.sharedWith u WHERE u.id = :userId")
    List<DroneListEntry> findBySharedWith_Id(@Param("userId") Long userId);

    @Query("SELECT d FROM DroneRecording d WHERE d.filename = :originalFilename ORDER BY d.version DESC LIMIT 1")
    DroneRecording getLatestRecordingByFilename(String originalFilename);

    @Query("SELECT d FROM DroneRecording d WHERE d.filename = :originalFilename AND d.version = :version ORDER BY d.timestamp DESC LIMIT 1")
    DroneRecording getLatestRecordingByFilenameAndVersion(String originalFilename, double version);
}
