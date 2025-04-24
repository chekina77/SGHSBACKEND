package com.example.SGHS4.repository;

import com.example.SGHS4.entite.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorAdminRepository extends JpaRepository<Doctor, Long> {
    @Query("SELECT p FROM Doctor p WHERE " +
            "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "CAST(p.age AS string) LIKE CONCAT('%', :keyword, '%') OR " +
            "p.phoneNumber LIKE CONCAT('%', :keyword, '%')")
    static List<Doctor> searchByKeyword(@Param("keyword") String keyword) {
        return null;
    }

}
