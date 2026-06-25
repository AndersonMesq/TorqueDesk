package com.andersonmesq.TorqueDesk.shared.util;

import java.text.Normalizer;

public class SlugGenerator {
    public static String generate(String slug) {
        return Normalizer.normalize(slug, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
    }
}

