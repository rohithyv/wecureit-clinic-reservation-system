package com.wecureit.doctor.controller;

import com.wecureit.doctor.model.request.ChamberRequest;
import com.wecureit.doctor.model.response.ChamberResponse;
import com.wecureit.doctor.service.ChamberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor/chambers")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChamberController {
    private final ChamberService chamberService;

    @GetMapping
    public List<ChamberResponse> getMyChambers(@CookieValue(name = "session_key", required = false) String sessionKey) {
        return chamberService.getMyChambers(sessionKey);
    }

    @PostMapping
    public ChamberResponse addChamber(
            @CookieValue(name = "session_key", required = false) String sessionKey,
            @RequestBody ChamberRequest request
    ) {
        return chamberService.addChamber(sessionKey, request);
    }

    @PutMapping("/{id}")
    public ChamberResponse updateChamber(
            @CookieValue(name = "session_key", required = false) String sessionKey,
            @PathVariable Long id,
            @RequestBody ChamberRequest request
    ) {
        return chamberService.updateChamber(sessionKey, id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChamber(
            @CookieValue(name = "session_key", required = false) String sessionKey,
            @PathVariable Long id
    ) {
        chamberService.deleteChamber(sessionKey, id);
        return ResponseEntity.noContent().build();
    }
}
