package dev.ilionx.workshop.api.visit.repository;

import dev.ilionx.workshop.api.visit.model.Visit;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Visit} entities.
 */
@Repository
public interface VisitRepository extends JpaRepository<Visit, Integer> {

    /**
     * Finds all visits for the specified pet.
     *
     * @param petId the pet's ID
     * @return list of visits for the given pet
     */
    @EntityGraph(attributePaths = "vet")
    List<Visit> findByPetId(Integer petId);

    @Override
    @EntityGraph(attributePaths = "vet")
    List<Visit> findAll();

    @Override
    @EntityGraph(attributePaths = "vet")
    Optional<Visit> findById(Integer id);

    boolean existsByVetId(Integer vetId);
}
