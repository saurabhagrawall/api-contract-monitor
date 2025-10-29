package com.contractmonitor.contractmonitor.repository;

import com.contractmonitor.contractmonitor.entity.ApiSpec;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiSpecRepository extends JpaRepository<ApiSpec, Long> {
    
    List<ApiSpec> findByServiceName(String serviceName);
    
    List<ApiSpec> findByServiceNameOrderByFetchedAtDesc(String serviceName);
    
    Optional<ApiSpec> findByServiceNameAndVersion(String serviceName, String version);
    
    Optional<ApiSpec> findTopByServiceNameOrderByFetchedAtDesc(String serviceName);

    @Query("SELECT DISTINCT a.serviceName FROM ApiSpec a")
    List<String> findDistinctServiceNames();
}