package com.atomicnorth.hrm.tenant.repository.message;

import com.atomicnorth.hrm.tenant.domain.message.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findByMessageCode(String messageCode);

    List<Message> findByModuleIdAndModuleFunctionId(Integer moduleId, Integer functionId);

    List<Message> findByModuleId(Integer moduleId);

    List<Message> findByModuleFunctionId(Integer functionId);
}
