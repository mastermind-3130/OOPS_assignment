package com.itsm.intelligence;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TextSimilarityTest {
    @Test void similarPaymentDescriptions() {
        assertTrue(TextSimilarity.similarity("Payment application unavailable", "Payment application is unavailable") > 0.30);
    }
    @Test void unrelatedDescriptions() {
        assertTrue(TextSimilarity.similarity("Network VPN connection offline", "Printer toner cartridge empty") < 0.10);
    }
    @Test void emptyInput() {
        assertEquals(0.0, TextSimilarity.similarity("", "hello"));
        assertEquals(0.0, TextSimilarity.similarity(null, "hello"));
    }
    @Test void identicalDescriptions() {
        assertEquals(1.0, TextSimilarity.similarity("Payment gateway error", "Payment gateway error"));
    }
}
