package com.technokratos.agona.model;

import com.technokratos.agona.enums.CompressionStatus;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "audio_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AudioFile extends AbstractEntity {

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "original_filename")
    private String originalFileName;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "duration_seconds")
    private Double durationSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "compression_status")
    private CompressionStatus compressionStatus;

    @OneToOne(mappedBy = "audioFile")
    private UserAnswer answer;

}
