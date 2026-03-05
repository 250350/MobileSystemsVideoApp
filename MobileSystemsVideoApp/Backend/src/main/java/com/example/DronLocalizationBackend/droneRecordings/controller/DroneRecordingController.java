package com.example.DronLocalizationBackend.droneRecordings.controller;

import com.example.DronLocalizationBackend.droneRecordings.DroneListEntry;
import com.example.DronLocalizationBackend.droneRecordings.entity.DroneRecording;
import com.example.DronLocalizationBackend.droneRecordings.service.DroneRecordingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.CurrentSecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collection;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/drone-recording")
public class DroneRecordingController {

    private final DroneRecordingService droneRecordingService;

    @GetMapping("/test")
    public ResponseEntity<String> getIngredient(@CurrentSecurityContext Authentication authentication) {
        return ResponseEntity.ok("test successful");
    }

    @PostMapping
    public ResponseEntity<String> uploadVideo(@RequestParam("file") MultipartFile file) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        DroneRecording saved = droneRecordingService.saveVideo(file, authentication.getName());
        return ResponseEntity.ok("Video saved with ID: " + saved.getId());
    }

@GetMapping(value = "/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
public ResponseEntity<byte[]> getVideo(@PathVariable Long id) {

    DroneRecording video = droneRecordingService.getVideo(id);

    return ResponseEntity.ok()
            .contentType(MediaType.valueOf("video/mp4"))
            .header("Content-Disposition", "inline; filename=\"" + video.getFilename() + "\"")
            .body(video.getData());
}


    @GetMapping("/all")
    public ResponseEntity<List<DroneListEntry>> getAllVideos() {
        List<DroneListEntry> videos = droneRecordingService.getVideos();
        return ResponseEntity.ok(videos);
    }

    @GetMapping()
    public ResponseEntity<List<DroneListEntry>> getVideos() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        List<DroneListEntry> videos = droneRecordingService.getVideosAssingnedToUser(username, authorities);
        return ResponseEntity.ok(videos);
    }

    @PostMapping("/{id}/share")
    public ResponseEntity<String> shareVideo(
            @CurrentSecurityContext Authentication authentication,
            @PathVariable Long id,
            @RequestParam String email) {

        DroneRecording video = droneRecordingService.getVideo(id);
        try {
            droneRecordingService.shareVideoWithUser(video, email);
            return ResponseEntity.ok("Udostępniono wideo użytkownikowi: " + email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

