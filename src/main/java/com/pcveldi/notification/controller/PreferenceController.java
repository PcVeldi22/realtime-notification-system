package com.pcveldi.notification.controller;

import com.pcveldi.notification.model.UserPreference;
import com.pcveldi.notification.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final UserPreferenceRepository userPreferenceRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<UserPreference> getPreferences(@PathVariable String userId) {
        return userPreferenceRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(UserPreference.builder().userId(userId).build()));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserPreference> updatePreferences(
            @PathVariable String userId,
            @RequestBody UserPreference preference) {
        preference.setUserId(userId);
        UserPreference saved = userPreferenceRepository.save(preference);
        return ResponseEntity.ok(saved);
    }
}
