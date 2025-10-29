package com.urjcservice.Backend.Service;

import com.urjcservice.Backend.Entities.Software;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class SoftwareService {

    private final List<Software> softwareList = new ArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(1);

    public List<Software> findAll() {
        return new ArrayList<>(softwareList); // Devuelve una copia para evitar modificaciones externas
    }

    public Software save(Software software) {
        if (software.getId() == null) {
            software.setId(idCounter.getAndIncrement()); // Asigna un ID único
        }
        softwareList.add(software);
        return software;
    }

    public Optional<Software> findById(Long id) {
        return softwareList.stream().filter(software -> software.getId().equals(id)).findFirst(); // Busca por ID
    }

    public Optional<Software> deleteById(Long id) {
        Optional<Software> existing = findById(id);
        existing.ifPresent(softwareList::remove);
        return existing; // Elimina por ID y devuelve la entidad eliminada si existía
    }

    public Optional<Software> updateSoftware(Long id, Software updatedSoftware) {
        return findById(id).map(existingSoftware -> {
            existingSoftware.setName(updatedSoftware.getName());
            existingSoftware.setVersion(updatedSoftware.getVersion());
            return existingSoftware;
        });
    }

    public Optional<Software> patchSoftware(Long id, Software partialSoftware) {
        return findById(id).map(existingSoftware -> {
            if (partialSoftware.getName() != null) {
                existingSoftware.setName(partialSoftware.getName());
            }
            if (partialSoftware.getVersion() != null) {
                existingSoftware.setVersion(partialSoftware.getVersion());
            }
            return existingSoftware;
        });
    }
}
