package com.shani.caradvisor.api;

import com.shani.caradvisor.data.ReferenceData;
import com.shani.caradvisor.workflow.Views.CatalogItem;
import com.shani.caradvisor.workflow.Views.OccupationItem;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Read-only reference data for pickers in the UI. */
@RestController
@RequestMapping("/api")
public class ReferenceController {

    private final ReferenceData data;

    public ReferenceController(ReferenceData data) {
        this.data = data;
    }

    @GetMapping("/catalog")
    public List<CatalogItem> catalog() {
        return data.catalog().vehicles().stream()
                .map(v -> new CatalogItem(v.id(), v.group(), v.maker(), v.commercialName(), v.key().registrationYear(),
                        v.drive().hebrew(), v.monthlyUseValue(), v.defaultParticipation(), v.officialUrl()))
                .toList();
    }

    @GetMapping("/occupations")
    public List<OccupationItem> occupations() {
        return data.occupations().all().stream()
                .map(o -> new OccupationItem(o.name(), o.group(), o.carRequiredByRole(), o.waiverAllowed(),
                        o.waiverMaintenance()))
                .toList();
    }
}
