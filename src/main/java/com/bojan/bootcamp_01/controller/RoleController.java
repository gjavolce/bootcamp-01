package com.bojan.bootcamp_01.controller;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bojan.bootcamp_01.dto.RoleDto;
import com.bojan.bootcamp_01.entity.Role;
import com.bojan.bootcamp_01.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {
    private final RoleService roleService;

    @Autowired
    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @GetMapping
    public List<RoleDto> getAllRoles() {
        return roleService.getAllRoles().stream()
                .map(role -> new RoleDto(role.getId(), role.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable UUID id) {
        return roleService.getRoleById(id)
                .map(role -> ResponseEntity.ok(new RoleDto(role.getId(), role.getName())))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RoleDto createRole(@RequestBody RoleDto roleDto) {
        Role role = new Role(roleDto.getName());
        Role savedRole = roleService.saveRole(role);
        return new RoleDto(savedRole.getId(), savedRole.getName());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}
