package com.itsm.service;

import com.itsm.domain.catalog.Category;
import com.itsm.domain.catalog.ItService;
import com.itsm.domain.catalog.SlaPolicy;
import com.itsm.domain.enums.BusinessCriticality;
import com.itsm.exception.ItsmException;
import com.itsm.repository.CategoryRepository;
import com.itsm.repository.ItServiceRepository;
import com.itsm.repository.SlaPolicyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional
public class CatalogService {

    private final ItServiceRepository itServiceRepository;
    private final CategoryRepository categoryRepository;
    private final SlaPolicyRepository slaPolicyRepository;

    public CatalogService(ItServiceRepository itServiceRepository,
                          CategoryRepository categoryRepository,
                          SlaPolicyRepository slaPolicyRepository) {
        this.itServiceRepository = itServiceRepository;
        this.categoryRepository = categoryRepository;
        this.slaPolicyRepository = slaPolicyRepository;
    }

    // ---------- IT services ----------

    @Transactional(readOnly = true)
    public List<ItService> listServices() {
        List<ItService> list = new ArrayList<>(itServiceRepository.findAll());
        list.sort(Comparator.comparing(ItService::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public ItService createService(String name, String description, BusinessCriticality criticality) {
        requireText(name, "Service name");
        if (itServiceRepository.findByName(name.trim()).isPresent()) {
            throw new ItsmException("A service named '" + name.trim() + "' already exists");
        }
        return itServiceRepository.save(new ItService(name.trim(), description, criticality));
    }

    public void updateService(Long id, String name, String description,
                              BusinessCriticality criticality, boolean active) {
        requireText(name, "Service name");
        ItService service = itServiceRepository.findById(id)
                .orElseThrow(() -> new ItsmException("Service not found: " + id));
        boolean taken = itServiceRepository.findByName(name.trim())
                .filter(other -> !other.getId().equals(id)).isPresent();
        if (taken) {
            throw new ItsmException("A service named '" + name.trim() + "' already exists");
        }
        service.update(name.trim(), description, criticality);
        if (active) {
            service.activate();
        } else {
            service.deactivate();
        }
        itServiceRepository.save(service);
    }

    // ---------- Categories ----------

    @Transactional(readOnly = true)
    public List<Category> listCategories() {
        List<Category> list = new ArrayList<>(categoryRepository.findAll());
        list.sort(Comparator.comparing(Category::getName, String.CASE_INSENSITIVE_ORDER));
        return list;
    }

    public Category createCategory(String code, String name, String description) {
        requireText(code, "Category code");
        requireText(name, "Category name");
        String upperCode = code.trim().toUpperCase();
        if (categoryRepository.findByCode(upperCode).isPresent()) {
            throw new ItsmException("A category with code '" + upperCode + "' already exists");
        }
        return categoryRepository.save(new Category(upperCode, name.trim(), description));
    }

    public void updateCategory(Long id, String name, String description, boolean active) {
        requireText(name, "Category name");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ItsmException("Category not found: " + id));
        category.rename(name.trim(), description);
        if (active) {
            category.activate();
        } else {
            category.deactivate();
        }
        categoryRepository.save(category);
    }

    // ---------- SLA policies ----------

    @Transactional(readOnly = true)
    public List<SlaPolicy> listSlaPolicies() {
        List<SlaPolicy> list = new ArrayList<>(slaPolicyRepository.findAll());
        list.sort(Comparator.comparingInt((SlaPolicy p) -> p.getPriority().getLevel())
                .thenComparing(p -> p.getService() == null ? "" : p.getService().getName()));
        return list;
    }

    public void updateSlaPolicy(Long id, int responseMinutes, int resolutionMinutes) {
        SlaPolicy policy = slaPolicyRepository.findById(id)
                .orElseThrow(() -> new ItsmException("SLA policy not found: " + id));
        policy.changeTargets(responseMinutes, resolutionMinutes); // throws IllegalArgumentException if invalid
        slaPolicyRepository.save(policy);
    }

    private void requireText(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }
    }
}