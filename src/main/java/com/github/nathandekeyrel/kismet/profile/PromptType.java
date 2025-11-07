package com.github.nathandekeyrel.kismet.profile;

import lombok.Getter;

@Getter
public enum PromptType {
    SPONTANEOUS_THING("The most spontaneous thing I've ever done"),
    PERFECT_DAY("My perfect day involves"),
    PASSIONATE_ABOUT("I'm passionate about"),
    LOVE_LANGUAGE("My love language is"),

    WEIRDEST_SKILL("A weird skill I have that surprises people"),
    STRANGEST_CELEBRITY_ENCOUNTER("Strangest celebrity interaction"),
    MOST_EMBARRASSING_MOMENT("Most embarrassing moment I can laugh about now"),
    CONSPIRACY_THEORY("Conspiracy theory I lowkey believe"),
    UNPOPULAR_OPINION("Unpopular opinion I'll defend"),

    TERRIBLE_AT("I'm embarrassingly terrible at"),
    GREEN_FLAG("A green flag for me is"),
    RED_FLAG("My biggest red flag is probably"),
    DATING_ME("Dating me is like"),

    DINNER_GUEST("If I could have dinner with anyone, alive or dead"),
    SUPERPOWER("If I had one superpower"),
    ZOMBIE_APOCALYPSE("My zombie apocalypse survival strategy"),

    CURRENT_OBSESSION("Currently geeking out on"),
    HIDDEN_TALENT("Hidden talent nobody knows about"),
    CHILDHOOD_DREAM("What I wanted to be when I grew up vs reality"),
    LAST_ADVENTURE("Last adventure I went on");

    private final String displayText;

    PromptType(String displayText) {
        this.displayText = displayText;
    }
}