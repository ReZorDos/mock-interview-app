package com.technokratos.agona.repository;

import com.technokratos.agona.model.AudioFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AudioFileRepository extends JpaRepository<AudioFile, UUID> {
}
