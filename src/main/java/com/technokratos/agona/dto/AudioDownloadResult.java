package com.technokratos.agona.dto;

public record AudioDownloadResult(
        byte[] data,
        String contentType
) {}
