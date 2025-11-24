package com.bitespeed.identity.service;

import com.bitespeed.identity.model.Contact;
import com.bitespeed.identity.model.LinkPrecedence;
import com.bitespeed.identity.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    @Transactional
    public Map<String, Object> identify(String email, String phoneNumber) {

        List<Contact> relatedContacts = contactRepository.findRelatedContacts(email, phoneNumber);

        if (relatedContacts.isEmpty()) {
            //New Customer
            Contact newPrimary = Contact.builder()
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .linkPrecedence(LinkPrecedence.primary)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            contactRepository.save(newPrimary);
            return response(newPrimary, Collections.emptyList());
        }

        //Existing Contacts Found.
        //Find all connected contacts
        Set<Contact> allConnected = new HashSet<>(relatedContacts);
        Queue<Contact> queue = new LinkedList<>(relatedContacts);

        while (!queue.isEmpty()) {
            Contact c = queue.poll();
            List<Contact> neighbors = contactRepository.findRelatedContacts(c.getEmail(), c.getPhoneNumber());
            for (Contact n : neighbors) {
                if (allConnected.add(n)) queue.add(n);
            }
        }

        // Find all primaries
        List<Contact> primaries = allConnected.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.primary)
                .sorted(Comparator.comparing(Contact::getCreatedAt))
                .toList();

        Contact mainPrimary = primaries.get(0);

        // Merge other primaries into this one (turn them into secondary)
        for (int i = 1; i < primaries.size(); i++) {
            Contact oldPrimary = primaries.get(i);
            oldPrimary.setLinkPrecedence(LinkPrecedence.secondary);
            oldPrimary.setLinkedId(mainPrimary.getId());
            oldPrimary.setUpdatedAt(LocalDateTime.now());
            contactRepository.save(oldPrimary);
        }

        // If new info (new email or phone), create secondary contact
        boolean newInfo = allConnected.stream()
                .noneMatch(c -> Objects.equals(c.getEmail(), email) && Objects.equals(c.getPhoneNumber(), phoneNumber));

        if (newInfo) {
            Contact secondary = Contact.builder()
                    .email(email)
                    .phoneNumber(phoneNumber)
                    .linkPrecedence(LinkPrecedence.secondary)
                    .linkedId(mainPrimary.getId())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            contactRepository.save(secondary);
            allConnected.add(secondary);
        }

        return buildResponse(mainPrimary, allConnected);
    }

    private Map<String, Object> buildResponse(Contact primary, Set<Contact> all) {
        List<String> emails = all.stream().map(Contact::getEmail)
                .filter(Objects::nonNull).distinct().toList();

        List<String> phones = all.stream().map(Contact::getPhoneNumber)
                .filter(Objects::nonNull).distinct().toList();

        List<Long> secondaryIds = all.stream()
                .filter(c -> c.getLinkPrecedence() == LinkPrecedence.secondary)
                .map(Contact::getId).toList();

        Map<String, Object> contact = new LinkedHashMap<>();
        contact.put("primaryContactId", primary.getId());
        contact.put("emails", emails);
        contact.put("phoneNumbers", phones);
        contact.put("secondaryContactIds", secondaryIds);

        return Map.of("contact", contact);
    }

    private Map<String, Object> response(Contact primary, List<Long> secondaryIds) {
        return Map.of("contact", Map.of(
                "primaryContactId", primary.getId(),
                "emails", List.of(primary.getEmail()),
                "phoneNumbers", List.of(primary.getPhoneNumber()),
                "secondaryContactIds", secondaryIds
        ));
    }
}