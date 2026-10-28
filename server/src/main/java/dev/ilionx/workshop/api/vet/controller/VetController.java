package dev.ilionx.workshop.api.vet.controller;

import dev.ilionx.workshop.api.vet.model.Vet;
import dev.ilionx.workshop.api.vet.model.mapper.VetMapper;
import dev.ilionx.workshop.api.vet.model.request.CreateVetRequest;
import dev.ilionx.workshop.api.vet.model.request.UpdateVetRequest;
import dev.ilionx.workshop.api.vet.model.response.VetResponse;
import dev.ilionx.workshop.api.vet.service.VetService;
import io.github.jframe.exception.resource.ApiErrorResponseResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static dev.ilionx.workshop.api.Paths.VETS;
import static dev.ilionx.workshop.api.Paths.VET_BY_ID;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * REST controller for managing veterinarians.
 */
@Tag(
    name = "Vet",
    description = "Vet management endpoints"
)
@RestController
@RequiredArgsConstructor
public class VetController {

    private final VetService vetService;
    private final VetMapper vetMapper;

    @ResponseStatus(OK)
    @Operation(
        summary = "Get all vets",
        description = "Returns a list of all veterinarians"
    )
    @GetMapping(
        path = VETS,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<List<VetResponse>> getAllVets() {
        final List<Vet> vets = vetService.findAll();
        return ResponseEntity.status(OK).body(vetMapper.toResponseList(vets));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Get vet by ID",
        description = "Returns a single veterinarian by their unique identifier"
    )
    @GetMapping(
        path = VET_BY_ID,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VetResponse> getVetById(@PathVariable final Integer id) {
        final Vet vet = vetService.findById(id);
        return ResponseEntity.status(OK).body(vetMapper.toResponse(vet));
    }

    @ResponseStatus(CREATED)
    @Operation(
        summary = "Create vet",
        description = "Creates a new veterinarian"
    )
    @PostMapping(
        path = VETS,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VetResponse> createVet(@RequestBody final CreateVetRequest request) {
        final Vet vet = vetService.create(request);
        return ResponseEntity.status(CREATED).body(vetMapper.toResponse(vet));
    }

    @ResponseStatus(OK)
    @Operation(
        summary = "Update vet",
        description = "Updates an existing veterinarian"
    )
    @PutMapping(
        path = VET_BY_ID,
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<VetResponse> updateVet(@PathVariable final Integer id, @RequestBody final UpdateVetRequest request) {
        final Vet vet = vetService.update(id, request);
        return ResponseEntity.status(OK).body(vetMapper.toResponse(vet));
    }

    @ResponseStatus(NO_CONTENT)
    @Operation(
        summary = "Delete vet",
        description = "Deletes a veterinarian by their unique identifier"
    )
    @ApiResponse(
        responseCode = "409",
        description = "Veterinarian is referenced by visits",
        content = @Content(schema = @Schema(implementation = ApiErrorResponseResource.class))
    )
    @DeleteMapping(path = VET_BY_ID)
    public ResponseEntity<Void> deleteVet(@PathVariable final Integer id) {
        vetService.delete(id);
        return ResponseEntity.status(NO_CONTENT).build();
    }
}
