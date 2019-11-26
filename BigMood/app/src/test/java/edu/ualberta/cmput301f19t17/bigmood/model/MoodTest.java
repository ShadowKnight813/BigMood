package edu.ualberta.cmput301f19t17.bigmood.model;

import com.google.firebase.firestore.GeoPoint;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Calendar;

public class MoodTest {
    private Mood mockMood(Calendar calendar, GeoPoint location) {
        return new Mood(EmotionalState.ANGER, calendar, SocialSituation.ALONE, "Angry cause alone",
                location, null);
    }

    @Test
    public void testGetters() {
        Calendar calendar = Calendar.getInstance();
        GeoPoint gp = new GeoPoint(53.34, 60.0);

        Mood mood = mockMood(calendar, gp);

        Assertions.assertEquals(EmotionalState.ANGER, mood.getState());
        Assertions.assertEquals(calendar, mood.getDatetime());
        Assertions.assertEquals(SocialSituation.ALONE, mood.getSituation());
        Assertions.assertEquals("Angry cause alone", mood.getReason());
        Assertions.assertEquals(gp, mood.getLocation());
        Assertions.assertNull(mood.getImage());
        Assertions.assertNull(mood.getFirestoreId());
    }

}
