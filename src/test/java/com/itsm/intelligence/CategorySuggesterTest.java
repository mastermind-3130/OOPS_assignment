package com.itsm.intelligence;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CategorySuggesterTest {
    private final CategorySuggester suggester = new CategorySuggester();

    @Test void detectsNetwork() {
        assertEquals("NETWORK", suggester.suggestCode("VPN connection keeps dropping").orElseThrow());
    }
    @Test void detectsSecurity() {
        assertEquals("SECURITY", suggester.suggestCode("Phishing malware attack").orElseThrow());
    }
    @Test void detectsApplication() {
        assertEquals("APPLICATION", suggester.suggestCode("Payment website error").orElseThrow());
    }
    @Test void returnsEmptyForUnknownText() {
        assertTrue(suggester.suggestCode("Something strange happened").isEmpty());
    }
    @Test void returnsEmptyForBlankText() {
        assertTrue(suggester.suggestCode(" ").isEmpty());
    }
}
