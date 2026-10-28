package dev.ilionx.workshop.api.pet.repository;

import dev.ilionx.workshop.api.pet.model.Pet;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Pet} entities.
 */
@Repository
public interface PetRepository extends JpaRepository<Pet, Integer> {

    /**
     * Finds all pets belonging to the specified owner.
     *
     * @param ownerId the owner's ID
     * @return list of pets for the given owner
     */
    @EntityGraph(
        attributePaths = {
            "visits",
            "visits.vet"
        },
        type = EntityGraph.EntityGraphType.LOAD
    )
    List<Pet> findByOwnerId(Integer ownerId);

    /**
     * Finds pets whose names contain the given text, ignoring case.
     *
     * @param name the name substring to search for
     * @return list of matching pets
     */
    @EntityGraph(
        attributePaths = {
            "visits",
            "visits.vet"
        },
        type = EntityGraph.EntityGraphType.LOAD
    )
    List<Pet> findByNameContainingIgnoreCase(String name);

    @Override
    @EntityGraph(
        attributePaths = {
            "visits",
            "visits.vet"
        },
        type = EntityGraph.EntityGraphType.LOAD
    )
    List<Pet> findAll();

    @Override
    @EntityGraph(
        attributePaths = {
            "visits",
            "visits.vet"
        },
        type = EntityGraph.EntityGraphType.LOAD
    )
    Optional<Pet> findById(Integer id);
}
