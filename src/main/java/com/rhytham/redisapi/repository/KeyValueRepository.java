package com.rhytham.redisapi.repository;

import com.rhytham.redisapi.model.KeyValueEntry;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;



@Repository
public interface KeyValueRepository extends JpaRepository<KeyValueEntry,String> {

    @Modifying
    @Transactional
    @Query("DELETE FROM KeyValueEntry e WHERE e.expiryTime IS NOT NULL AND e.expiryTime <= :now")
    void deleteAllExpired(@Param("now") Long now);

}
