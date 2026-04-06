package com.example.backend_labo2.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class AttestationVerificationService {

    @Value("${app.upload.dir:uploads/attestations}")
    private String uploadDir;

    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );

    public record StoredAttestation(
            String url,
            String originalName,
            String mimeType,
            long size,
            boolean verified
    ) {}

    public StoredAttestation verifyAndStore(
            MultipartFile file,
            String cin,
            String nom,
            String prenom,
            String departementNom,
            String niveau,
            String classe
    ) {
        // L'attestation est optionnelle : pas de fichier fourni
        if (file == null || file.isEmpty()) {
            return new StoredAttestation(null, null, null, 0, false);
        }

        // Vérification taille
        if (file.getSize() > MAX_SIZE) {
            throw new RuntimeException("Fichier trop volumineux (max 10 MB)");
        }

        // Vérification type MIME
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_TYPES.contains(mimeType.toLowerCase())) {
            throw new RuntimeException("Format non supporté. Utilisez PDF, JPG ou PNG");
        }

        // Vérification extension
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "attestation";
        String ext = getExtension(originalName);
        if (!List.of("pdf", "jpg", "jpeg", "png").contains(ext.toLowerCase())) {
            throw new RuntimeException("Extension non autorisée. Utilisez .pdf, .jpg ou .png");
        }

        // Stockage du fichier
        String storedName = UUID.randomUUID() + "_" + sanitizeFilename(originalName);
        try {
            Path dir = Paths.get(uploadDir);
            Files.createDirectories(dir);
            Path dest = dir.resolve(storedName);
            file.transferTo(dest);
        } catch (IOException e) {
            throw new RuntimeException("Erreur lors du stockage de l'attestation : " + e.getMessage());
        }

        String url = "/uploads/attestations/" + storedName;
        return new StoredAttestation(url, originalName, mimeType, file.getSize(), true);
    }

    private String getExtension(String filename) {
        int dot = filename.lastIndexOf('.');
        return (dot >= 0) ? filename.substring(dot + 1) : "";
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}