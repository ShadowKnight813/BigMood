package edu.ualberta.cmput301f19t17.bigmood.model;

import org.junit.jupiter.api.Test;

import edu.ualberta.cmput301f19t17.bigmood.R;
import static org.junit.jupiter.api.Assertions.assertEquals;


class EmotionalStateTest {

    private EmotionalState mockEmotionalState() {
        return EmotionalState.DISGUST;
    }

    @Test
    void testGetStateCode() {
        EmotionalState state = mockEmotionalState();
        assertEquals(3, state.getStateCode());
    }

    @Test
    void testGetDrawableId() {
        EmotionalState state = mockEmotionalState();
        assertEquals(R.drawable.ic_emoticon_disgust, state.getDrawableId());
    }

    @Test
    void testToString() {
        EmotionalState state = mockEmotionalState();
        assertEquals("Disgusted", state.toString());
    }

    @Test
    void testFindByStateCode() {
        EmotionalState state = mockEmotionalState();
        assertEquals(state, state.findByStateCode(3));
    }
}
