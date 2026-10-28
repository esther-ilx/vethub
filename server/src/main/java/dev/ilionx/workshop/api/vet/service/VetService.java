package dev.ilionx.workshop.api.vet.service;

import dev.ilionx.workshop.api.vet.model.Specialty;
import dev.ilionx.workshop.api.vet.model.Vet;
import dev.ilionx.workshop.api.vet.model.request.CreateVetRequest;
import dev.ilionx.workshop.api.vet.model.request.UpdateVetRequest;
import dev.ilionx.workshop.api.vet.repository.SpecialtyRepository;
import dev.ilionx.workshop.api.vet.repository.VetRepository;
import dev.ilionx.workshop.api.visit.repository.VisitRepository;
import dev.ilionx.workshop.common.exception.VetReferencedException;
import io.github.jframe.exception.core.DataNotFoundException;
import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static dev.ilionx.workshop.common.exception.ApiErrorCode.VET_NOT_FOUND;

/**
 * Service for managing veterinarian operations.
 */
@Service
@RequiredArgsConstructor
public class VetService {

    private final VetRepository vetRepository;
    private final SpecialtyRepository specialtyRepository;
    private final VisitRepository visitRepository;

    /**
     * Retrieves all veterinarians.
     *
     * @return list of all vets
     */
    @Transactional(readOnly = true)
    public List<Vet> findAll() {
        return vetRepository.findAll();
    }

    /**
     * Finds a veterinarian by ID.
     *
     * @param id the vet ID
     * @return the found vet
     */
    @Transactional(readOnly = true)
    public Vet findById(final Integer id) {
        return vetRepository.findById(id)
            .orElseThrow(() -> new DataNotFoundException(VET_NOT_FOUND));
    }

    /**
     * Creates a new veterinarian.
     *
     * @param request the create request
     * @return the created vet
     */
    @Transactional
    public Vet create(final CreateVetRequest request) {
        final Vet vet = new Vet();
        vet.setFirstName(request.getFirstName());
        vet.setLastName(request.getLastName());

        final List<Specialty> specialties = specialtyRepository.findAllById(request.getSpecialtyIds());
        vet.setSpecialties(new HashSet<>(specialties));

        return vetRepository.save(vet);
    }

    /**
     * Updates an existing veterinarian.
     *
     * @param id      the vet ID
     * @param request the update request
     * @return the updated vet
     */
    @Transactional
    public Vet update(final Integer id, final UpdateVetRequest request) {
        final Vet vet = findById(id);
        vet.setFirstName(request.getFirstName());
        vet.setLastName(request.getLastName());

        final List<Specialty> specialties = specialtyRepository.findAllById(request.getSpecialtyIds());
        vet.setSpecialties(new HashSet<>(specialties));

        return vetRepository.save(vet);
    }

    /**
     * Deletes a veterinarian by ID.
     *
     * @param id the vet ID
     */
    @Transactional
    public void delete(final Integer id) {
        final Vet vet = vetRepository.findLockedById(id)
            .orElseThrow(() -> new DataNotFoundException(VET_NOT_FOUND));
        if (visitRepository.existsByVetId(id)) {
            throw new VetReferencedException();
        }
        try {
            vetRepository.delete(vet);
            vetRepository.flush();
        } catch (DataIntegrityViolationException exception) {
            if (exception.getMostSpecificCause().getMessage().toLowerCase(java.util.Locale.ROOT).contains("fk_visits_vet")) {
                throw new VetReferencedException();
            }
            throw exception;
        }
    }
}
