package edu.ualberta.cmput301f19t17.bigmood.model;

import org.junit.jupiter.api.Test;

import edu.ualberta.cmput301f19t17.bigmood.model.SocialSituation;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SocialSituationTest {

    private SocialSituation mockSocialSituation() {
        return SocialSituation.ALONE;
    }

    @Test
    void testGetSituationCode() {
        SocialSituation situation = mockSocialSituation();
        assertEquals(1, situation.getSituationCode());
    }

    @Test
    void testToString() {
        SocialSituation situation = mockSocialSituation();
        assertEquals("Alone", situation.toString());
    }

    @Test
    void testFindBySituationCode() {
        SocialSituation situation = mockSocialSituation();
        assertEquals(situation, situation.findBySituationCode(1));
    }
}
