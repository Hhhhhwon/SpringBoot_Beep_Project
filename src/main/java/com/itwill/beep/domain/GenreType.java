package com.itwill.beep.domain;

public enum GenreType {
    POINT_AND_CLICK(2), FIGHTING(4), SHOOTER(5), MUSIC(7), PLATFORM(8), PUZZLE(9), RACING(
            10), REAL_TIME_STRATEGY(11), ROLE_PLAYING(12), SIMULATOR(13), SPORT(14), MOBA(
                    15), TURN_BASED_STRATEGY(16), TACTICAL(24), HACK_AND_SLASH(25), QUIZ_TRIVIA(
                            26), PINBALL(30), ADVENTURE(31), INDIE(32), ARCADE(
                                    33), VISUAL_NOVEL(34), CARD_AND_BOARD_GAME(35), STRATEGY(36);

    private final int value;

    GenreType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static GenreType getByValue(int value) {
        for (GenreType genre : GenreType.values()) {
            if (genre.value == value) {
                return genre;
            }
        }
        throw new IllegalArgumentException("Invalid GenreType value: " + value);
    }
}
