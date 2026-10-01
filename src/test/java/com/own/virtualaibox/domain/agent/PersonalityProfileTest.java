package com.own.virtualaibox.domain.agent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class PersonalityProfileTest {

    @Test
    void defaultProfilesAreDistinctForNamedResidents() {
        PersonalityProfile alice = PersonalityProfile.defaultFor("Alice");
        PersonalityProfile bob = PersonalityProfile.defaultFor("Bob");

        assertNotEquals(alice, bob);
        assertEquals("探索者", alice.getRole());
        assertEquals("社区居民", bob.getRole());
    }

    @Test
    void unknownResidentUsesStableGenericProfile() {
        PersonalityProfile first = PersonalityProfile.defaultFor("Clara");
        PersonalityProfile second = PersonalityProfile.defaultFor("Clara");

        assertEquals(first, second);
        assertEquals("居民", first.getRole());
    }

    @Test
    void promptContextContainsAllPersonalityBoundaries() {
        String context = PersonalityProfile.defaultFor("Alice").toPromptContext();

        assertTrue(context.contains("身份：探索者"));
        assertTrue(context.contains("当前动机：了解小镇并寻找新地点"));
        assertTrue(context.contains("知识边界：只知道已经感知到或记忆中的世界信息"));
    }
}
