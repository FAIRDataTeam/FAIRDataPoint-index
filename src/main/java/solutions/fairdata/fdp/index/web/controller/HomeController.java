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
package solutions.fairdata.fdp.index.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import solutions.fairdata.fdp.index.entity.IndexEntryState;
import solutions.fairdata.fdp.index.entity.config.EventsConfig;
import solutions.fairdata.fdp.index.service.IndexEntryService;

@Controller
@RequestMapping("/")
public class HomeController {
    @Autowired
    private IndexEntryService indexEntryService;

    @Autowired
    private EventsConfig eventsConfig;

    @GetMapping
    public String home(Model model, @SortDefault(sort = "modificationTime", direction = Sort.Direction.DESC) Pageable pageable, @RequestParam(defaultValue = "reachable") String state) {
        var sort = pageable.getSort().stream()
            .findFirst()
            .map(o -> o.getProperty() + "," + o.getDirection().name().toLowerCase())
            .orElse("");

        model.addAttribute("entries", indexEntryService.getEntriesPage(pageable, state));
        model.addAttribute("pingValidDuration", eventsConfig.getPingValidDuration());
        model.addAttribute("IndexEntryState", IndexEntryState.class);

        model.addAttribute("countAll", indexEntryService.countAllEntries());
        model.addAttribute("countActive", indexEntryService.countActiveEntries());
        model.addAttribute("countInactive", indexEntryService.countInactiveEntries());
        model.addAttribute("countUnreachable", indexEntryService.countUnreachableEntries());
        model.addAttribute("countInvalid", indexEntryService.countInvalidEntries());
        model.addAttribute("countUnknown", indexEntryService.countUnknownEntries());

        model.addAttribute("sort", sort);
        model.addAttribute("state", state);
        return "home";
    }
}
