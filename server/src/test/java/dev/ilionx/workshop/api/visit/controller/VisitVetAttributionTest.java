package dev.ilionx.workshop.api.visit.controller;

import dev.ilionx.workshop.api.owner.model.Owner;
import dev.ilionx.workshop.api.pet.model.Pet;
import dev.ilionx.workshop.api.vet.model.Vet;
import dev.ilionx.workshop.api.visit.model.Visit;
import dev.ilionx.workshop.api.visit.model.request.CreateVisitRequest;
import dev.ilionx.workshop.api.visit.model.response.VisitResponse;
import dev.ilionx.workshop.support.IntegrationTest;

import java.time.LocalDate;
import java.util.List;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;

import static dev.ilionx.workshop.api.Paths.*;
import static io.github.jframe.util.mapper.ObjectMappers.fromJson;
import static io.github.jframe.util.mapper.ObjectMappers.toJson;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class VisitVetAttributionTest extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    private Vet savedVet() {
        final Vet vet = new Vet();
        vet.setFirstName(" Alice ");
        vet.setLastName(" Brown ");
        return vetRepository.save(vet);
    }

    @Test
    void bothCreationRoutesValidateOptionalVetAndPreserveItOnUpdates() throws Exception {
        final Owner owner = aSavedOwner();
        final Pet pet = aSavedPet(owner);
        final Vet vet = savedVet();
        final CreateVisitRequest request = aCreateVisitRequestWithPet(pet.getId()).setVetId(vet.getId());

        final String globalBody = mockMvc.perform(post(VISITS).contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.vetName", is("Alice Brown")))
            .andReturn().getResponse().getContentAsString();
        final int globalId = fromJson(globalBody, VisitResponse.class).getId();
        final String nestedBody = mockMvc.perform(
            post(PET_VISITS, owner.getId(), pet.getId())
                .contentType(MediaType.APPLICATION_JSON).content(toJson(request))
        )
            .andExpect(status().isCreated()).andExpect(jsonPath("$.vetName", is("Alice Brown")))
            .andReturn().getResponse().getContentAsString();
        final int nestedId = fromJson(nestedBody, VisitResponse.class).getId();

        mockMvc.perform(put(VISIT_BY_ID, globalId).contentType(MediaType.APPLICATION_JSON).content(toJson(anUpdateVisitRequest())))
            .andExpect(status().isOk()).andExpect(jsonPath("$.vetName", is("Alice Brown")));
        mockMvc.perform(
            put(PET_VISIT_BY_ID, owner.getId(), pet.getId(), nestedId)
                .contentType(MediaType.APPLICATION_JSON).content(toJson(anUpdateVisitRequest()))
        )
            .andExpect(status().isOk()).andExpect(jsonPath("$.vetName", is("Alice Brown")));
        mockMvc.perform(get(VISIT_BY_ID, globalId)).andExpect(status().isOk()).andExpect(jsonPath("$.vetName", is("Alice Brown")));
        mockMvc.perform(get(PET_VISIT_BY_ID, owner.getId(), pet.getId(), nestedId))
            .andExpect(status().isOk()).andExpect(jsonPath("$.vetName", is("Alice Brown")));

        request.setVetId(null);
        mockMvc.perform(post(VISITS).contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.vetName").value(nullValue()));
        mockMvc.perform(
            post(PET_VISITS, owner.getId(), pet.getId())
                .contentType(MediaType.APPLICATION_JSON).content(toJson(aCreateVisitRequest()))
        )
            .andExpect(status().isCreated()).andExpect(jsonPath("$.vetName").value(nullValue()));
        final long count = visitRepository.count();
        request.setVetId(999999);
        mockMvc.perform(post(VISITS).contentType(MediaType.APPLICATION_JSON).content(toJson(request))).andExpect(status().isNotFound());
        mockMvc.perform(post(PET_VISITS, owner.getId(), pet.getId()).contentType(MediaType.APPLICATION_JSON).content(toJson(request)))
            .andExpect(status().isNotFound());
        assertEquals(count, visitRepository.count());
    }

    @Test
    void namesAppearInAllHistoriesAndReflectRenames() throws Exception {
        final Owner owner = aSavedOwner();
        final Pet pet = aSavedPet(owner);
        final Vet vet = savedVet();
        final Visit attributed = aSavedVisit(pet);
        attributed.setVet(vet);
        visitRepository.save(attributed);
        aSavedVisit(pet);

        vet.setFirstName("New");
        vet.setLastName("Name");
        vetRepository.save(vet);
        mockMvc.perform(get(VISITS)).andExpect(status().isOk())
            .andExpect(jsonPath("$[*].vetName", containsInAnyOrder("New Name", null)));
        mockMvc.perform(get(PET_VISITS, owner.getId(), pet.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$[*].vetName", containsInAnyOrder("New Name", null)));
        mockMvc.perform(get(PET_BY_ID, pet.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.visits[*].vetName", containsInAnyOrder("New Name", null)));
        mockMvc.perform(get(OWNER_PET_BY_ID, owner.getId(), pet.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$.visits[*].vetName", containsInAnyOrder("New Name", null)));
        mockMvc.perform(get(PETS).param("name", "Leo")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].visits[*].vetName", containsInAnyOrder("New Name", null)));
        mockMvc.perform(get(OWNER_PETS, owner.getId())).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].visits[*].vetName", containsInAnyOrder("New Name", null)));
    }

    @Test
    void referencedVetCannotBeDeletedEvenForFutureVisitsAndDatabaseEnforcesConstraint() throws Exception {
        final Owner owner = aSavedOwner();
        final Pet pet = aSavedPet(owner);
        final Vet vet = savedVet();
        final Visit visit = aSavedVisit(pet);
        visit.setVet(vet);
        visit.setDate(LocalDate.now().plusDays(2));
        visitRepository.save(visit);

        mockMvc.perform(delete(VET_BY_ID, vet.getId())).andExpect(status().isConflict())
            .andExpect(jsonPath("$.apiErrorCode", is("ERR-0008")))
            .andExpect(jsonPath("$.apiErrorReason", is("Cannot delete veterinarian because they are referenced by visits.")));
        assertThrows(
            DataIntegrityViolationException.class,
            () -> jdbcTemplate.update("delete from vets where id = ?", vet.getId())
        );
        assertTrue(vetRepository.existsById(vet.getId()));
        assertEquals(vet.getId(), visitRepository.findById(visit.getId()).orElseThrow().getVet().getId());

        final Vet unreferenced = savedVet();
        mockMvc.perform(delete(VET_BY_ID, unreferenced.getId())).andExpect(status().isNoContent());
        assertFalse(vetRepository.existsById(unreferenced.getId()));
    }

    @Test
    void historyReadsDoNotAddQueriesForEachVet() throws Exception {
        final Owner owner = aSavedOwner();
        final Pet pet = aSavedPet(owner);
        final List<Vet> vets = vetRepository.findAll();
        final Visit first = aSavedVisit(pet);
        first.setVet(vets.get(0));
        visitRepository.save(first);

        final Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
        stats.setStatisticsEnabled(true);
        try {
            stats.clear();
            mockMvc.perform(get(VISITS)).andExpect(status().isOk());
            final long globalQueries = stats.getPrepareStatementCount();
            stats.clear();
            mockMvc.perform(get(OWNER_PET_BY_ID, owner.getId(), pet.getId())).andExpect(status().isOk());
            final long petQueries = stats.getPrepareStatementCount();

            for (int index = 1; index < vets.size(); index++) {
                final Visit visit = aSavedVisit(pet);
                visit.setVet(vets.get(index));
                visitRepository.save(visit);
            }
            stats.clear();
            mockMvc.perform(get(VISITS)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(vets.size())));
            assertEquals(globalQueries, stats.getPrepareStatementCount());
            stats.clear();
            mockMvc.perform(get(OWNER_PET_BY_ID, owner.getId(), pet.getId())).andExpect(status().isOk())
                .andExpect(jsonPath("$.visits", hasSize(vets.size())));
            assertEquals(petQueries, stats.getPrepareStatementCount());
        } finally {
            stats.setStatisticsEnabled(false);
        }
    }
}
