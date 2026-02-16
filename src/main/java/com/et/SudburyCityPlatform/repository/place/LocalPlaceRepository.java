package com.et.SudburyCityPlatform.repository.place;

import com.et.SudburyCityPlatform.models.places.LocalPlace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocalPlaceRepository extends JpaRepository<LocalPlace, Long> {
}
