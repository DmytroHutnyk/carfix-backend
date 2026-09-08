package com.hutnyk.carfix.employee.controller;

import com.hutnyk.carfix.employee.dto.request.CreateEmployeeRequest;
import com.hutnyk.carfix.employee.dto.request.UpdateEmployeeRequest;
import com.hutnyk.carfix.employee.dto.response.EmployeeResponse;
import com.hutnyk.carfix.employee.mapper.EmployeeCommandMapper;
import com.hutnyk.carfix.employee.mapper.EmployeeResponseMapper;
import com.hutnyk.carfix.in.employee.OwnerEmployeePortIn;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/owner/branches/{branchId}/employees")
public class OwnerEmployeeController {

    private final OwnerEmployeePortIn ownerEmployeePortIn;

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<List<EmployeeResponse>> getEmployees(
            @PathVariable(name = "branchId") UUID branchId,
            @AuthenticationPrincipal UserDetails principal) {
        List<EmployeeResponse> response = ownerEmployeePortIn.getEmployees(principal.getUsername(), branchId).stream()
                .map(EmployeeResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @PathVariable(name = "branchId") UUID branchId,
            @Valid @RequestBody CreateEmployeeRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        EmployeeResponse response = EmployeeResponseMapper.toResponse(
                ownerEmployeePortIn.createEmployee(principal.getUsername(), branchId,
                        EmployeeCommandMapper.toCreateCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable(name = "branchId") UUID branchId,
            @PathVariable(name = "employeeId") UUID employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        EmployeeResponse response = EmployeeResponseMapper.toResponse(
                ownerEmployeePortIn.updateEmployee(principal.getUsername(), branchId, employeeId,
                        EmployeeCommandMapper.toUpdateCommand(request)));
        return ResponseEntity.ok(response);
    }
}
