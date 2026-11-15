package com.itsm.web;

import com.itsm.repository.AuditLogRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminAuditController {

    private final AuditLogRepository auditLogRepository;

    public AdminAuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping("/admin/audit")
    public String page(Model model) {
        model.addAttribute("entries",
                auditLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")));
        return "admin/audit";
    }
}
