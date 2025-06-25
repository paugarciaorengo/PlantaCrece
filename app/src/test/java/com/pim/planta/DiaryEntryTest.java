package com.pim.planta;

import static org.junit.Assert.assertEquals;

import com.pim.planta.models.DiaryEntry;

import org.junit.Test;

import android.content.Context;

public class DiaryEntryTest {

    @Test
    public void testConstructorAndGetters() {
        DiaryEntry entry = new DiaryEntry("Test Highlight", "Test Annotation", 2, 1, 1672531200000L);
        assertEquals("Test Highlight", entry.getHighlight());
        assertEquals("Test Annotation", entry.getAnnotation());
        assertEquals(2, entry.getEmotion());
        assertEquals(1, entry.getUser_id());
        assertEquals(1672531200000L, entry.getDate());
    }

    @Test
    public void testSetters() {
        DiaryEntry entry = new DiaryEntry("Test Highlight", "Test Annotation", 2, 1, 1672531200000L);
        entry.setHighlight("Updated Highlight");
        entry.setAnnotation("Updated Annotation");
        entry.setEmotion(4);
        entry.setUser_id(2);
        entry.setDate(1675209600000L);
        entry.setId(1);

        assertEquals("Updated Highlight", entry.getHighlight());
        assertEquals("Updated Annotation", entry.getAnnotation());
        assertEquals(4, entry.getEmotion());
        assertEquals(2, entry.getUser_id());
        assertEquals(1675209600000L, entry.getDate());
        assertEquals(1, entry.getId());
    }

    @Test
    public void testEmotionToString(Context context) {
        DiaryEntry entry1 = new DiaryEntry("Test Highlight", "Test Annotation", 1, 1, 1672531200000L);
        assertEquals("Excited", entry1.emotionToString(context));

        DiaryEntry entry2 = new DiaryEntry("Test Highlight", "Test Annotation", 2, 1, 1672531200000L);
        assertEquals("Happy", entry2.emotionToString(context));

        DiaryEntry entry3 = new DiaryEntry("Test Highlight", "Test Annotation", 3, 1, 1672531200000L);
        assertEquals("Neutral", entry3.emotionToString(context));

        DiaryEntry entry4 = new DiaryEntry("Test Highlight", "Test Annotation", 4, 1, 1672531200000L);
        assertEquals("Sad", entry4.emotionToString(context));

        DiaryEntry entry5 = new DiaryEntry("Test Highlight", "Test Annotation", 5, 1, 1672531200000L);
        assertEquals("Very sad", entry5.emotionToString(context));

        DiaryEntry entry6 = new DiaryEntry("Test Highlight", "Test Annotation", 0, 1, 1672531200000L);
        assertEquals("No emotion", entry6.emotionToString(context));

        DiaryEntry entry7 = new DiaryEntry("Test Highlight", "Test Annotation", 6, 1, 1672531200000L);
        assertEquals("No emotion", entry7.emotionToString(context));
    }
    @Test
    public void testGetUserId() {
        DiaryEntry entry = new DiaryEntry("Test Highlight", "Test Annotation", 2, 1, 1672531200000L);
        assertEquals(1, entry.getUserId());
    }
    @Test
    public void testSetAnnotation() {
        DiaryEntry entry = new DiaryEntry("Test Highlight", "Test Annotation", 2, 1, 1672531200000L);
        entry.setAnnotation("New Annotation");
        assertEquals("New Annotation", entry.getAnnotation());
    }
}