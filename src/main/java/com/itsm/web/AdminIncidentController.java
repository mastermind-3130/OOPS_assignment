package com.itsm.web;

import com.itsm.domain.enums.IncidentStatus;
import com.itsm.domain.enums.Priority;
import com.itsm.dto.IncidentSearchCriteria;
import com.itsm.repository.CategoryRepository;
import com.itsm.repository.ItServiceRepository;
import com.itsm.repository.TechnicianRepository;
import com.itsm.service.IncidentSearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Admin list of all incidents, with search filters.
 */
@Controller
@RequestMapping("/admin/incidents")
public class AdminIncidentController {

    private final IncidentSearchService searchService;
    private final CategoryRepository categoryRepository;
    private final ItServiceRepository itServiceRepository;
    private final TechnicianRepository technicianRepository;

    public AdminIncidentController(IncidentSearchService searchService,
                                   CategoryRepository categoryRepository,
                                   ItServiceRepository itServiceRepository,
                                   TechnicianRepository technicianRepository) {
        this.searchService = searchService;
        this.categoryRepository = categoryRepository;
        this.itServiceRepository = itServiceRepository;
        this.technicianRepository = technicianRepository;
    }

    @GetMapping
    public String list(@ModelAttribute("criteria") IncidentSearchCriteria criteria, Model model) {
        model.addAttribute("incidents", searchService.search(criteria));
        model.addAttribute("statuses", IncidentStatus.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("categories", categoryRepository.findAll());
        model.addAttribute("services", itServiceRepository.findAll());
        model.addAttribute("technicians", technicianRepository.findAll());
        return "admin/incidents";
    }
}