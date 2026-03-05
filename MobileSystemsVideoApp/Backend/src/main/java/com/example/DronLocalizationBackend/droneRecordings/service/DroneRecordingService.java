package com.example.DronLocalizationBackend.droneRecordings.service;

import com.example.DronLocalizationBackend.droneRecordings.DroneListEntry;
import com.example.DronLocalizationBackend.droneRecordings.entity.DroneRecording;
import com.example.DronLocalizationBackend.droneRecordings.repository.DroneRecordingRepository;
import com.example.DronLocalizationBackend.user.Role;
import com.example.DronLocalizationBackend.user.UserEntity;
import com.example.DronLocalizationBackend.user.repository.UserEntityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DroneRecordingService {

    private final DroneRecordingRepository droneRecordingRepository;
    private final UserEntityRepository userEntityRepository;



public DroneRecording saveVideo(MultipartFile file, String email) throws IOException {

    UserEntity user = userEntityRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new RuntimeException("User not found: " + email));

    DroneRecording droneRecording = droneRecordingRepository
            .getLatestRecordingByFilename(file.getOriginalFilename());

    double currentVersion = droneRecording != null ? droneRecording.getVersion() : 0.0;
    currentVersion += 0.1;

    DroneRecording video = DroneRecording.builder()
            .filename(file.getOriginalFilename())
            .contentType("video/mp4")
            .timestamp(LocalDateTime.now())
            .data(file.getBytes())
            .version(currentVersion)
            .sharedWith(new HashSet<>(Set.of(user)))
            .build();

    return droneRecordingRepository.save(video);
}

    public DroneRecording saveVideo(MultipartFile file, double version) throws IOException {

        DroneRecording droneRecording = droneRecordingRepository.getLatestRecordingByFilenameAndVersion(file.getOriginalFilename(), version);

        if (droneRecording != null) {
            droneRecording.setContentType(file.getContentType());
            droneRecording.setData(file.getBytes());
            droneRecording.setFilename(file.getOriginalFilename());
        } else {
            droneRecording = DroneRecording.builder()
                    .filename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .data(file.getBytes())
                    .version(version)
                    .build();
        }

        return droneRecordingRepository.save(droneRecording);
    }

    public DroneRecording getVideo(Long id) {
        return droneRecordingRepository.findById(id).orElseThrow(() -> new RuntimeException("Video not found"));
    }
    public DroneRecording getVideo(String filename) {
        return droneRecordingRepository.getLatestRecordingByFilename(filename);
    }
    public List<DroneListEntry> getVideos() {
        return droneRecordingRepository.findAllDroneListEntries();
    }

    public void shareVideoWithUser(DroneRecording video, String email) {

        UserEntity user = userEntityRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
        video.getSharedWith().add(user);
        droneRecordingRepository.save(video);
    }

    public List<DroneListEntry> getVideosAssingnedToUser(String username, Collection<? extends GrantedAuthority> authorities) {
        UserEntity user = userEntityRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        if (authorities.contains(new SimpleGrantedAuthority(Role.ADMIN.toString()))) {
            return  droneRecordingRepository.findAllDroneListEntries();
        }
        return droneRecordingRepository.findBySharedWith_Id(user.getId());
    }
}

