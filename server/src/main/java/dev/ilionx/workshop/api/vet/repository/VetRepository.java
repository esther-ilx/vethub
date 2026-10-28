package dev.ilionx.workshop.api.vet.repository;

import dev.ilionx.workshop.api.vet.model.Vet;

import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Vet} entities.
 */
@Repository
public interface VetRepository extends JpaRepository<Vet, Integer> {

    @Override
    @EntityGraph(attributePaths = "specialties")
    List<Vet> findAll();

    @Override
    @EntityGraph(attributePaths = "specialties")
    Optional<Vet> findById(Integer id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Vet v where v.id = :id")
    Optional<Vet> findLockedById(@Param("id") Integer id);
}
