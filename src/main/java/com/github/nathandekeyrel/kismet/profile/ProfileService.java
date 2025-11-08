package com.github.nathandekeyrel.kismet.profile;

import com.github.nathandekeyrel.kismet.user.User;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfileAnswerRepository profileAnswerRepository;

    public ProfileService(ProfileAnswerRepository profileAnswerRepository, ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
        this.profileAnswerRepository = profileAnswerRepository;
    }

    public Profile getByUser(User user) {
        return profileRepository.findByUser(user).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    public List<ProfileAnswer> getAnswersByProfile(Profile profile) {
        List<ProfileAnswer> answers = profileAnswerRepository.findByProfile(profile);
        answers.sort(Comparator.comparing(answer -> answer.getPromptType().ordinal()));
        return answers;
    }

    public ProfileAnswer getProfileAnswer(Profile profile, PromptType promptType) {
        return profileAnswerRepository.findByProfileAndPromptType(profile, promptType).orElse(new ProfileAnswer());
    }

    public void saveProfile(Profile profile) {
        profileRepository.save(profile);
    }

    public void saveAnswer(ProfileAnswer profileAnswer) {
        profileAnswerRepository.save(profileAnswer);
    }

    public ProfileEditForm buildEditForm(Profile profile) {
        ProfileEditForm form = new ProfileEditForm();
        form.setBio(profile.getBio());

        List<ProfileAnswer> answers = getAnswersByProfile(profile);
        for (ProfileAnswer answer : answers) {
            form.getAnswers().put(answer.getPromptType(), answer.getAnswerText());
        }

        return form;
    }

    public void updateProfile(Profile profile, String bio, Map<PromptType, String> answers) {
        profile.setBio(bio);
        saveProfile(profile);

        for (Map.Entry<PromptType, String> entry : answers.entrySet()) {
            saveOrUpdateAnswer(profile, entry.getKey(), entry.getValue());
        }
    }

    private void saveOrUpdateAnswer(Profile profile, PromptType promptType, String answerText) {
        if (answerText == null || answerText.trim().isEmpty()) {
            return;
        }

        ProfileAnswer answer = getProfileAnswer(profile, promptType);
        answer.setProfile(profile);
        answer.setPromptType(promptType);
        answer.setAnswerText(answerText);

        saveAnswer(answer);
    }

}
