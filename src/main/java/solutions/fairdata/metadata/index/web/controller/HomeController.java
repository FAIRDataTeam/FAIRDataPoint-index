package solutions.fairdata.metadata.index.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import solutions.fairdata.metadata.index.service.IndexService;

@Controller
@RequestMapping("/")
public class HomeController {
    @Autowired
    private IndexService service;
    
    @GetMapping
    public String home(Model model) {
        model.addAttribute("entries", service.getAllEntries());
        return "home";
    }
}
