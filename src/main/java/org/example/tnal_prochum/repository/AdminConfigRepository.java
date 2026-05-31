package org.example.tnal_prochum.repository;



import org.example.tnal_prochum.model.AdminConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminConfigRepository extends JpaRepository<AdminConfig, Long> {
}
