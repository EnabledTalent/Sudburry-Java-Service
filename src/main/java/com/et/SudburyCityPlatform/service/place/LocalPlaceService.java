package com.et.SudburyCityPlatform.service.place;

import com.et.SudburyCityPlatform.exception.ResourceNotFoundException;
import com.et.SudburyCityPlatform.models.places.LocalPlace;
import com.et.SudburyCityPlatform.models.places.LocalPlaceRequest;
import com.et.SudburyCityPlatform.repository.place.LocalPlaceRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LocalPlaceService {

    private final LocalPlaceRepository repository;

    public LocalPlaceService(LocalPlaceRepository repository) {
        this.repository = repository;
    }

    // CREATE
    public LocalPlace create(LocalPlaceRequest request) {
        LocalPlace place = new LocalPlace();
        mapRequestToEntity(request, place);
        return repository.save(place);
    }

    // UPDATE
    public LocalPlace update(Long id, LocalPlaceRequest request) {
        LocalPlace place = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Local place not found"));

        mapRequestToEntity(request, place);
        return repository.save(place);
    }

    // READ ALL
    public List<LocalPlace> getAll() {
        return repository.findAll();
    }

    // READ BY ID
    public LocalPlace getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Local place not found"));
    }

    // DELETE
    public void delete(Long id) {
        repository.deleteById(id);
    }

    private void mapRequestToEntity(LocalPlaceRequest request, LocalPlace place) {
        place.setName(request.getName());
        place.setCategory(request.getCategory());
        place.setAddress(request.getAddress());
        place.setOpenNow(request.isOpenNow());
        place.setWeekdayHours(request.getWeekdayHours());
        place.setWeekendHours(request.getWeekendHours());
        place.setPhone(request.getPhone());
        place.setWebsite(request.getWebsite());
        place.setDescription(request.getDescription());
    }
}

