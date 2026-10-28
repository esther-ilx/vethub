package dev.ilionx.workshop.api.vet.model.mapper;

import dev.ilionx.workshop.api.vet.model.Vet;

import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
public class VetNameMapper {

    @Named("vetName")
    public String vetName(final Vet vet) {
        return vet == null ? null : (vet.getFirstName().trim() + " " + vet.getLastName().trim()).trim();
    }
}
