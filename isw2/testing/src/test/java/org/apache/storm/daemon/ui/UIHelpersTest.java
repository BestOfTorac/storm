package org.apache.storm.daemon.ui;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.DaemonConfig;
import org.apache.storm.generated.ExecutorInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersTest {

    // 1. Verifica la durata nulla.
    // Scenario: zero secondi.
    // Atteso: stringa vuota, perché nessuna unità viene aggiunta.
    @Test
    void prettyUptimeSecWithZeroReturnsEmptyString() {
        assertEquals("", UIHelpers.prettyUptimeSec(0));
    }

    // 2. Verifica una durata inferiore a un minuto.
    // Scenario: 59 secondi.
    // Atteso: "59s".
    @Test
    void prettyUptimeSecBelowOneMinuteUsesSeconds() {
        assertEquals("59s", UIHelpers.prettyUptimeSec(59));
    }

    // 3. Verifica il confine esatto del minuto.
    // Scenario: 60 secondi.
    // Atteso: "1m 0s".
    @Test
    void prettyUptimeSecAtOneMinuteIncludesZeroSeconds() {
        assertEquals("1m 0s", UIHelpers.prettyUptimeSec(60));
    }

    // 4. Verifica una durata composta da ore, minuti e secondi.
    // Scenario: 3661 secondi.
    // Atteso: "1h 1m 1s".
    @Test
    void prettyUptimeSecFormatsHoursMinutesAndSeconds() {
        assertEquals("1h 1m 1s", UIHelpers.prettyUptimeSec(3661));
    }

    // 5. Verifica una durata composta che supera un giorno.
    // Scenario: 90061 secondi.
    // Atteso: "1d 1h 1m 1s".
    @Test
    void prettyUptimeSecFormatsDaysHoursMinutesAndSeconds() {
        assertEquals("1d 1h 1m 1s", UIHelpers.prettyUptimeSec(90061));
    }

    // 6. Verifica la coerenza tra i due overload.
    // Scenario: lo stesso valore fornito come String e come int.
    // Atteso: risultato identico.
    @Test
    void prettyUptimeSecStringAndIntOverloadsAreConsistent() {
        assertEquals(
                UIHelpers.prettyUptimeSec("125"),
                UIHelpers.prettyUptimeSec(125)
        );
    }

    // 7. Verifica una durata in millisecondi inferiore a un secondo.
    // Scenario: 999 millisecondi.
    // Atteso: "999ms".
    @Test
    void prettyUptimeMsBelowOneSecondUsesMilliseconds() {
        assertEquals("999ms", UIHelpers.prettyUptimeMs(999));
    }

    // 8. Verifica una durata composta in millisecondi.
    // Scenario: 61001 millisecondi.
    // Atteso: "1m 1s 1ms".
    @Test
    void prettyUptimeMsFormatsMinutesSecondsAndMilliseconds() {
        assertEquals("1m 1s 1ms", UIHelpers.prettyUptimeMs(61001));
    }

    // 9. Verifica il comportamento con un valore negativo.
    // Scenario: -1 secondo.
    // Atteso: stringa vuota.
    @Test
    void prettyUptimeSecWithNegativeValueReturnsEmptyString() {
        assertEquals("", UIHelpers.prettyUptimeSec(-1));
    }

    // 10. Verifica la gestione di input non numerico.
    // Scenario: stringa "not-a-number".
    // Atteso: NumberFormatException.
    @Test
    void prettyUptimeSecWithNonNumericStringThrowsException() {
        assertThrows(
                NumberFormatException.class,
                () -> UIHelpers.prettyUptimeSec("not-a-number")
        );
    }

    // 11. Verifica la sostituzione di più placeholder URL.
    // Scenario: host, porta e file con caratteri già sicuri.
    // Atteso: URL completo.
    @Test
    void urlFormatReplacesMultiplePlaceholders() {
        String result = UIHelpers.urlFormat(
                "http://%s:%s/log?file=%s",
                "worker1",
                8000,
                "worker.log"
        );

        assertEquals(
                "http://worker1:8000/log?file=worker.log",
                result
        );
    }

    // 12. Verifica la codifica URL di un carattere riservato.
    // Scenario: l'argomento contiene uno slash.
    // Atteso: lo slash è codificato come %2F.
    @Test
    void urlFormatEncodesSlashInArgument() {
        String result = UIHelpers.urlFormat("/component/%s", "bolt/A");

        assertEquals("/component/bolt%2FA", result);
    }

    // 13. Verifica la conversione di un argomento null.
    // Scenario: singolo argomento null.
    // Atteso: la stringa "null".
    @Test
    void urlFormatConvertsNullArgumentToLiteralNull() {
        String result = UIHelpers.urlFormat("/value/%s", (Object) null);

        assertEquals("/value/null", result);
    }

    // 14. Verifica la formattazione dell'identificatore di executor.
    // Scenario: task iniziale 3 e task finale 7.
    // Atteso: "[3-7]".
    @Test
    void prettyExecutorInfoFormatsTaskRange() {
        ExecutorInfo executorInfo = new ExecutorInfo();
        executorInfo.set_task_start(3);
        executorInfo.set_task_end(7);

        assertEquals("[3-7]", UIHelpers.prettyExecutorInfo(executorInfo));
    }

    // 15. Verifica il JSON logico per un utente non autorizzato.
    // Scenario: utente "alice".
    // Atteso: codice simbolico e messaggio contenente l'utente.
    @Test
    void unauthorizedUserJsonContainsErrorAndUser() {
        Map<String, Object> result = UIHelpers.unauthorizedUserJson("alice");

        assertEquals("No Authorization", result.get("error"));
        assertEquals(
                "User alice is not authorized.",
                result.get("errorMessage")
        );
    }

    // 16. Verifica la conversione di un'eccezione con messaggio.
    // Scenario: IllegalArgumentException con messaggio "invalid input".
    // Atteso: stato HTTP testuale e messaggio originale.
    @Test
    void exceptionToJsonPreservesNonEmptyMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(
                new IllegalArgumentException("invalid input"),
                400
        );

        assertEquals("400 Bad Request", result.get("error"));
        assertEquals("invalid input", result.get("errorMessage"));
    }

    // 17. Verifica il fallback per un messaggio vuoto.
    // Scenario: IllegalStateException con stringa vuota.
    // Atteso: nome completo della classe dell'eccezione.
    @Test
    void exceptionToJsonUsesClassNameForEmptyMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(
                new IllegalStateException(""),
                500
        );

        assertEquals("500 Server Error", result.get("error"));
        assertEquals(
                IllegalStateException.class.getName(),
                result.get("errorMessage")
        );
    }

    // 18. Verifica il fallback per un messaggio null.
    // Scenario: RuntimeException senza messaggio.
    // Atteso: nome completo della classe dell'eccezione.
    @Test
    void exceptionToJsonUsesClassNameForNullMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(
                new RuntimeException(),
                404
        );

        assertEquals("404 Not Found", result.get("error"));
        assertEquals(
                RuntimeException.class.getName(),
                result.get("errorMessage")
        );
    }

    // 19. Verifica gli header senza callback JSONP.
    // Scenario: callback null.
    // Atteso: Content-Type JSON.
    @Test
    void jsonResponseHeadersUseJsonForNullCallback() {
        Map<?, ?> result = UIHelpers.getJsonResponseHeaders(null, null);

        assertEquals(
                "application/json;charset=utf-8",
                result.get("Content-Type")
        );
        assertEquals("nosniff", result.get("X-Content-Type-Options"));
    }

    // 20. Verifica gli header con callback semplice valida.
    // Scenario: callback "handleResponse".
    // Atteso: Content-Type JavaScript.
    @Test
    void jsonResponseHeadersUseJavaScriptForSimpleValidCallback() {
        Map<?, ?> result = UIHelpers.getJsonResponseHeaders(
                "handleResponse",
                null
        );

        assertEquals(
                "application/javascript;charset=utf-8",
                result.get("Content-Type")
        );
    }

    // 21. Verifica gli header con callback qualificata valida.
    // Scenario: callback composta da più identificatori separati da punti.
    // Atteso: Content-Type JavaScript.
    @Test
    void jsonResponseHeadersUseJavaScriptForQualifiedCallback() {
        Map<?, ?> result = UIHelpers.getJsonResponseHeaders(
                "app.callbacks.done",
                null
        );

        assertEquals(
                "application/javascript;charset=utf-8",
                result.get("Content-Type")
        );
    }

    // 22. Verifica il rifiuto di una callback che inizia con una cifra.
    // Scenario: callback "1callback".
    // Atteso: fallback a Content-Type JSON.
    @Test
    void jsonResponseHeadersRejectCallbackStartingWithDigit() {
        Map<?, ?> result = UIHelpers.getJsonResponseHeaders(
                "1callback",
                null
        );

        assertEquals(
                "application/json;charset=utf-8",
                result.get("Content-Type")
        );
    }

    // 23. Verifica il limite massimo della callback.
    // Scenario: callback di 129 caratteri.
    // Atteso: callback ignorata e Content-Type JSON.
    @Test
    void jsonResponseHeadersRejectCallbackLongerThan128Characters() {
        String callback = "a".repeat(129);

        Map<?, ?> result = UIHelpers.getJsonResponseHeaders(callback, null);

        assertEquals(
                "application/json;charset=utf-8",
                result.get("Content-Type")
        );
    }

    // 24. Verifica la precedenza degli header personalizzati.
    // Scenario: il chiamante fornisce un Content-Type custom.
    // Atteso: il valore custom sovrascrive quello standard.
    @Test
    void jsonResponseHeadersAllowCustomHeaderOverride() {
        Map<String, Object> customHeaders = new HashMap<>();
        customHeaders.put("Content-Type", "application/problem+json");
        customHeaders.put("X-Custom", "value");

        Map<?, ?> result = UIHelpers.getJsonResponseHeaders(
                null,
                customHeaders
        );

        assertEquals(
                "application/problem+json",
                result.get("Content-Type")
        );
        assertEquals("value", result.get("X-Custom"));
    }

    // 25. Verifica la serializzazione JSON di una mappa.
    // Scenario: mappa con una sola coppia chiave-valore.
    // Atteso: oggetto JSON equivalente.
    @Test
    void jsonResponseBodySerializesMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("value", 7);

        String result = UIHelpers.getJsonResponseBody(data, null, true);

        assertEquals("{\"value\":7}", result);
    }

    // 26. Verifica l'utilizzo diretto di una stringa già serializzata.
    // Scenario: needSerialize false e callback null.
    // Atteso: stringa invariata.
    @Test
    void jsonResponseBodyReturnsRawStringWhenSerializationIsDisabled() {
        String json = "{\"status\":\"ok\"}";

        String result = UIHelpers.getJsonResponseBody(
                json,
                null,
                false
        );

        assertEquals(json, result);
    }

    // 27. Verifica l'avvolgimento JSONP.
    // Scenario: callback valida e corpo già serializzato.
    // Atteso: callback(body);
    @Test
    void jsonResponseBodyWrapsRawBodyInValidCallback() {
        String result = UIHelpers.getJsonResponseBody(
                "{\"status\":\"ok\"}",
                "done",
                false
        );

        assertEquals("done({\"status\":\"ok\"});", result);
    }

    // 28. Verifica che una callback non valida sia ignorata.
    // Scenario: callback contenente parentesi.
    // Atteso: corpo non avvolto.
    @Test
    void jsonResponseBodyDoesNotWrapBodyInInvalidCallback() {
        String json = "{\"status\":\"ok\"}";

        String result = UIHelpers.getJsonResponseBody(
                json,
                "done()",
                false
        );

        assertEquals(json, result);
    }

    // 29. Verifica la serializzazione di un valore String.
    // Scenario: needSerialize true.
    // Atteso: stringa JSON racchiusa tra virgolette.
    @Test
    void jsonResponseBodySerializesStringAsJsonString() {
        String result = UIHelpers.getJsonResponseBody(
                "hello",
                null,
                true
        );

        assertEquals("\"hello\"", result);
    }

    // 30. Verifica la configurazione senza HTTPS.
    // Scenario: è presente soltanto la porta HTTP.
    // Atteso: logviewer non sicuro.
    @Test
    void isSecureLogviewerReturnsFalseWhenHttpsPortIsAbsent() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);

        assertEquals(false, UIHelpers.isSecureLogviewer(config));
    }

    // 31. Verifica una porta HTTPS negativa.
    // Scenario: HTTPS configurato a -1.
    // Atteso: logviewer non sicuro.
    @Test
    void isSecureLogviewerReturnsFalseForNegativeHttpsPort() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, -1);

        assertEquals(false, UIHelpers.isSecureLogviewer(config));
    }

    // 32. Verifica il confine usato dal codice per HTTPS.
    // Scenario: porta HTTPS uguale a zero.
    // Atteso: true, perché il codice usa il confronto >= 0.
    @Test
    void isSecureLogviewerReturnsTrueForZeroHttpsPort() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, 0);

        assertTrue(UIHelpers.isSecureLogviewer(config));
    }

    // 33. Verifica la selezione della porta HTTP.
    // Scenario: porta HTTPS negativa e porta HTTP positiva.
    // Atteso: viene restituita la porta HTTP.
    @Test
    void getLogviewerPortReturnsHttpPortWhenHttpsIsDisabled() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, -1);
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);

        assertEquals(8000, UIHelpers.getLogviewerPort(config));
    }

    // 34. Verifica la costruzione del collegamento al logviewer.
    // Scenario: logviewer HTTP e nome file contenente uno slash.
    // Atteso: protocollo e porta HTTP, file codificato.
    @Test
    void getLogviewerLinkBuildsHttpUrlAndEncodesFileName() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);

        String result = UIHelpers.getLogviewerLink(
                "worker1",
                "topology/worker.log",
                config,
                6700
        );

        assertEquals(
                "http://worker1:8000/api/v1/log?file=topology%2Fworker.log",
                result
        );
    }

    // 35. Verifica due trasformazioni elementari usate nella presentazione UI.
    // Scenario: finestra all-time e stream con caratteri non ammessi.
    // Atteso: etichetta leggibile e nome stream sanitizzato.
    @Test
    void presentationHelpersFormatAllTimeAndSanitizeInvalidStreamCharacters() {
        assertEquals("All time", UIHelpers.getWindowHint(":all-time"));
        assertEquals(
                "orders_eu_",
                UIHelpers.sanitizeStreamName("orders/eu!")
        );
    }
}
