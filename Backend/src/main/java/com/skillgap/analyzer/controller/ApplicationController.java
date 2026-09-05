package com.skillgap.analyzer.controller;

import com.skillgap.analyzer.dto.*;
import com.skillgap.analyzer.service.ApplicationService;
import jakarta.validation.constraints.Positive;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class ApplicationController {
    private final ApplicationService applications;
    public ApplicationController(ApplicationService applications) { this.applications = applications; }
    @PostMapping("/api/user/applications") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('USER')")
    public ApplicationResponse apply(Principal principal, @Valid @RequestBody ApplicationRequest request) {
        return applications.apply(principal.getName(), request);
    }
    @GetMapping("/api/user/applications") @PreAuthorize("hasRole('USER')")
    public List<ApplicationResponse> current(Principal principal) { return applications.current(principal.getName()); }
    @GetMapping("/api/admin/applications") @PreAuthorize("hasRole('ADMIN')")
    public List<ApplicationResponse> all() { return applications.all(); }
    @PutMapping("/api/admin/applications/{applicationId}/status") @PreAuthorize("hasRole('ADMIN')")
    public ApplicationResponse updateStatus(@PathVariable @Positive Long applicationId,
                                             @Valid @RequestBody ApplicationStatusRequest request) {
        return applications.updateStatus(applicationId, request);
    }
}
