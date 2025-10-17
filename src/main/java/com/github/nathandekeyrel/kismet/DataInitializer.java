package com.github.nathandekeyrel.kismet;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final PromptSectionRepository sectionRepository;
    private final PromptRepository promptRepository;

    public DataInitializer(PromptSectionRepository sectionRepository, PromptRepository promptRepository) {
        this.sectionRepository = sectionRepository;
        this.promptRepository = promptRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("--- Initializing Prompt Data ---");

        if (sectionRepository.count() == 0) {
            PromptSection aboutMe = new PromptSection();
            aboutMe.setTitle("About Me");
            sectionRepository.save(aboutMe);

            Prompt p1 = new Prompt();
            p1.setText("The most spontaneous thing I've ever done is...");
            p1.setSection(aboutMe);
            promptRepository.save(p1);

            Prompt p2 = new Prompt();
            p2.setText("A goal I'm working towards is...");
            p2.setSection(aboutMe);
            promptRepository.save(p2);

            Prompt p3 = new Prompt();
            p3.setText("Let's see how changing affects the app...");
            p3.setSection(aboutMe);
            promptRepository.save(p3);

            System.out.println("--- Prompt Data Initialized ---");
        } else {
            System.out.println("--- Prompt Data already exists, skipping initialization ---");
        }
    }
}
