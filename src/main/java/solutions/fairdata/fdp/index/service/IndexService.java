/**
 * The MIT License
 * Copyright © 2020 https://fairdata.solutions
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package solutions.fairdata.fdp.index.service;

import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import solutions.fairdata.fdp.index.api.dto.EntryDTO;
import solutions.fairdata.fdp.index.domain.IndexEntry;
import solutions.fairdata.fdp.index.storage.EntryRepository;

@Component
public class IndexService {
    private static final Logger logger = LoggerFactory.getLogger(IndexService.class);
    
    @Autowired
    private EntryRepository repository;
    
    public void storeEntry(String clientUrl) {
        var entity = repository.findById(clientUrl);
        var now = OffsetDateTime.now();
        
        final IndexEntry entry;
        if (entity.isPresent()) {
            logger.info("Updating timestamp of existing entry {}", clientUrl);
            entry = entity.orElseThrow();
        } else {
            logger.info("Storing new entry {}", clientUrl);
            entry = new IndexEntry();
            entry.setClientUrl(clientUrl);
            entry.setRegistrationTime(now.toString());
        }
        
        entry.setModificationTime(now.toString());
        repository.save(entry);
    }
    
    public Iterable<IndexEntry> getAllEntries() {
        return repository.findAll();
    }

    public Page<IndexEntry> getEntriesPage(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public EntryDTO toDTO(IndexEntry indexEntry) {
        EntryDTO dto = new EntryDTO();
        dto.setClientUrl(indexEntry.getClientUrl());
        dto.setRegistrationTime(OffsetDateTime.parse(indexEntry.getRegistrationTime()));
        dto.setModificationTime(OffsetDateTime.parse(indexEntry.getModificationTime()));
        return dto;
    }
}
