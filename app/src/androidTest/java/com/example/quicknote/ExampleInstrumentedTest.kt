package com.example.quicknote

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Przykładowy test instrumentowany, który wykonuje się na fizycznym urządzeniu lub emulatorze Androida.
 * Pozwala na testowanie funkcjonalności wymagających dostępu do API Androida (np. Context).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    /**
     * Test sprawdza, czy pakiet aplikacji jest poprawny w kontekście urządzenia.
     */
    @Test
    fun useAppContext() {
        // Kontekst aplikacji w trakcie testów
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.quicknote", appContext.packageName)
    }
}
