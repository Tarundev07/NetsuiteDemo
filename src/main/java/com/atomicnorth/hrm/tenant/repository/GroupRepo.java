package com.atomicnorth.hrm.tenant.repository;

import com.atomicnorth.hrm.tenant.domain.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GroupRepo extends JpaRepository<Group, Long> {

    Group findByGroupNameIgnoreCase(String name);

}
