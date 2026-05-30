package com.technokratos.agona.model;

import com.technokratos.agona.enums.TranscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_answer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAnswer extends AbstractEntity {

    @ManyToOne
    @JoinColumn(name = "progress_id", nullable = false)
    private UserProgress progress;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "transcribed_text", columnDefinition = "TEXT")
    private String transcribedText;

    @Enumerated(EnumType.STRING)
    @Column(name = "transcription_status", nullable = false)
    @Builder.Default
    private TranscriptionStatus transcriptionStatus = TranscriptionStatus.PENDING;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "audio_file_id", unique = true)
    private AudioFile audioFile;

}
