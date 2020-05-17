package solutions.fairdata.fdp.index.api.controller;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import solutions.fairdata.fdp.index.api.dto.EntryDTO;
import solutions.fairdata.fdp.index.service.IndexService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Tag(name = "Entries")
@RestController
@RequestMapping("/entries")
public class EntriesController {

    @Autowired
    private IndexService service;

    @GetMapping("")
    public Page<EntryDTO> getEntriesPage(Pageable pageable) {
        return service.getEntriesPage(pageable).map(service::toDTO);
    }

    @GetMapping("/all")
    public List<EntryDTO> getEntriesAll() {
        return StreamSupport.stream(service.getAllEntries().spliterator(), true).map(service::toDTO).collect(Collectors.toList());
    }
}
