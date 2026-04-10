package com.wecureit.admin.repository;

import com.wecureit.admin.entity.FacilityRoom;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FacilityRoomRepository extends JpaRepository<FacilityRoom, Long> {
    List<FacilityRoom> findByFacilityId(Long facilityId);

    @Modifying
    void deleteByFacilityId(Long facilityId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM wecureit.facility_rooms WHERE facility_id = ?1 AND id NOT IN (?2)", nativeQuery = true)
    void deleteByFacilityIdAndIdNotIn(Long facilityId, List<Long> roomIds);
}