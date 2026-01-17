package com.ximad.prism.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfile {
    private List<String> interests;
    private float[] vector;
    private Set<String> negativeFeedback = new HashSet<>();
    private Set<String> intents = new HashSet<>();
    private String location; // e.g., for House recall

    public UserProfile(List<String> interests, Set<String> negativeFeedback) {
        this.interests = interests;
        this.negativeFeedback = negativeFeedback;
    }

    public boolean hasIntent(String intent) {
        return intents.contains(intent);
    }
}
