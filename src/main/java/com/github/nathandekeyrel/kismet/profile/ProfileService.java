package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfileService {

    private final ProfileAnswerRepository profileAnswerRepository;
    private final PromptSectionRepository promptSectionRepository;
    private final PromptRepository promptRepository;

    public ProfileService(ProfileAnswerRepository profileAnswerRepository, PromptSectionRepository promptSectionRepository, PromptRepository promptRepository) {
        this.profileAnswerRepository = profileAnswerRepository;
        this.promptSectionRepository = promptSectionRepository;
        this.promptRepository = promptRepository;
    }

    public List<ProfileAnswer> getByUser(User user) {
        return profileAnswerRepository.findByUser(user);
    }

    public void save(ProfileAnswer answer) {
        profileAnswerRepository.save(answer);
    }

    public Prompt getPrompt(Long promptId) {
        Prompt prompt = promptRepository.findById(promptId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid prompt ID"));
        return prompt;
    }

    public ProfileAnswer getProfileAnswer(User currentUser, Prompt prompt) {
        ProfileAnswer answer = profileAnswerRepository.findByUserAndPrompt(currentUser, prompt)
                .orElse(new ProfileAnswer());
        return answer;
    }

    public List<PromptSection> getAll() {
        return promptSectionRepository.findAll();
    }
}
