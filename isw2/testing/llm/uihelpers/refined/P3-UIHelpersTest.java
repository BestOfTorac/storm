package org.apache.storm.daemon.ui;

import java.util.HashMap;
import java.util.Map;

import org.apache.storm.DaemonConfig;
import org.apache.storm.generated.ExecutorInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UIHelpersTest {

    /*
     * 1. Funzionalità: formattazione della durata in secondi.
     * Scenario: durata pari a zero.
     * Atteso: stringa vuota.
     */
    @Test
    void prettyUptimeSecWithZeroReturnsEmptyString() {
        assertEquals("", UIHelpers.prettyUptimeSec(0));
    }

    /*
     * 2. Funzionalità: formattazione della durata in secondi.
     * Scenario: durata inferiore a un minuto.
     * Atteso: visualizzazione dei soli secondi.
     */
    @Test
    void prettyUptimeSecBelowOneMinuteUsesSeconds() {
        assertEquals("59s", UIHelpers.prettyUptimeSec(59));
    }

    /*
     * 3. Funzionalità: formattazione della durata in secondi.
     * Scenario: confine esatto di un minuto.
     * Atteso: un minuto e zero secondi.
     */
    @Test
    void prettyUptimeSecAtOneMinuteIncludesZeroSeconds() {
        assertEquals("1m 0s", UIHelpers.prettyUptimeSec(60));
    }

    /*
     * 4. Funzionalità: formattazione di una durata composta.
     * Scenario: ore, minuti e secondi tutti non nulli.
     * Atteso: tutte le unità sono riportate nell'ordine corretto.
     */
    @Test
    void prettyUptimeSecFormatsHoursMinutesAndSeconds() {
        assertEquals("1h 1m 1s", UIHelpers.prettyUptimeSec(3661));
    }

    /*
     * 5. Funzionalità: formattazione di una durata superiore a un giorno.
     * Scenario: un giorno, un'ora, un minuto e un secondo.
     * Atteso: tutte le unità sono presenti.
     */
    @Test
    void prettyUptimeSecFormatsDaysHoursMinutesAndSeconds() {
        assertEquals("1d 1h 1m 1s", UIHelpers.prettyUptimeSec(90061));
    }

    /*
     * 6. Funzionalità: formattazione della durata in millisecondi.
     * Scenario: durata inferiore a un secondo.
     * Atteso: visualizzazione dei soli millisecondi.
     */
    @Test
    void prettyUptimeMsBelowOneSecondUsesMilliseconds() {
        assertEquals("999ms", UIHelpers.prettyUptimeMs(999));
    }

    /*
     * 7. Funzionalità: formattazione composta in millisecondi.
     * Scenario: un minuto, un secondo e un millisecondo.
     * Atteso: tutte le unità sono riportate.
     */
    @Test
    void prettyUptimeMsFormatsMinutesSecondsAndMilliseconds() {
        assertEquals("1m 1s 1ms", UIHelpers.prettyUptimeMs(61001));
    }

    /*
     * 8. Funzionalità: parsing dell'overload String.
     * Scenario: input non numerico.
     * Atteso: NumberFormatException.
     */
    @Test
    void prettyUptimeSecWithNonNumericStringThrowsException() {
        assertThrows(
                NumberFormatException.class,
                () -> UIHelpers.prettyUptimeSec("not-a-number")
        );
    }

    /*
     * 9. Funzionalità: interpolazione degli argomenti in un URL.
     * Scenario: più placeholder e argomenti già URL-safe.
     * Atteso: URL completo.
     */
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

    /*
     * 10. Funzionalità: encoding degli argomenti URL.
     * Scenario: argomento contenente uno slash.
     * Atteso: slash codificato come %2F.
     */
    @Test
    void urlFormatEncodesSlashInArgument() {
        String result = UIHelpers.urlFormat(
                "/component/%s",
                "bolt/A"
        );

        assertEquals("/component/bolt%2FA", result);
    }

    /*
     * 11. Funzionalità: formattazione di ExecutorInfo.
     * Scenario: intervallo di task da 3 a 7.
     * Atteso: rappresentazione "[3-7]".
     */
    @Test
    void prettyExecutorInfoFormatsTaskRange() {
        ExecutorInfo executorInfo = new ExecutorInfo();
        executorInfo.set_task_start(3);
        executorInfo.set_task_end(7);

        assertEquals(
                "[3-7]",
                UIHelpers.prettyExecutorInfo(executorInfo)
        );
    }

    /*
     * 12. Funzionalità: generazione del risultato per un utente non autorizzato.
     * Scenario: utente alice.
     * Atteso: tipo di errore e messaggio contenente il nome utente.
     */
    @Test
    void unauthorizedUserJsonContainsErrorAndUser() {
        Map<String, Object> result =
                UIHelpers.unauthorizedUserJson("alice");

        assertEquals("No Authorization", result.get("error"));
        assertEquals(
                "User alice is not authorized.",
                result.get("errorMessage")
        );
    }

    /*
     * 13. Funzionalità: trasformazione di un'eccezione in una mappa.
     * Scenario: eccezione con messaggio non vuoto e status 400.
     * Atteso: messaggio preservato e status rappresentato nel campo error.
     */
    @Test
    void exceptionToJsonPreservesNonEmptyMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(
                new IllegalArgumentException("invalid input"),
                400
        );

        assertEquals("invalid input", result.get("errorMessage"));
        assertTrue(String.valueOf(result.get("error")).contains("400"));
    }

    /*
     * 14. Funzionalità: fallback del messaggio di errore.
     * Scenario: eccezione con messaggio vuoto.
     * Atteso: nome completo della classe dell'eccezione.
     */
    @Test
    void exceptionToJsonUsesClassNameForEmptyMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(
                new IllegalStateException(""),
                500
        );

        assertEquals(
                IllegalStateException.class.getName(),
                result.get("errorMessage")
        );
        assertTrue(String.valueOf(result.get("error")).contains("500"));
    }

    /*
     * 15. Funzionalità: fallback del messaggio di errore.
     * Scenario: eccezione con messaggio null.
     * Atteso: nome completo della classe dell'eccezione.
     */
    @Test
    void exceptionToJsonUsesClassNameForNullMessage() {
        Map<String, Object> result = UIHelpers.exceptionToJson(
                new RuntimeException(),
                404
        );

        assertEquals(
                RuntimeException.class.getName(),
                result.get("errorMessage")
        );
        assertTrue(String.valueOf(result.get("error")).contains("404"));
    }

    /*
     * 16. Funzionalità: selezione del Content-Type.
     * Scenario: callback assente.
     * Atteso: risposta JSON e header di sicurezza.
     */
    @Test
    void jsonResponseHeadersUseJsonForNullCallback() {
        Map<?, ?> result =
                UIHelpers.getJsonResponseHeaders(null, null);

        assertEquals(
                "application/json;charset=utf-8",
                result.get("Content-Type")
        );
        assertEquals(
                "nosniff",
                result.get("X-Content-Type-Options")
        );
    }

    /*
     * 17. Funzionalità: riconoscimento di callback JSONP.
     * Scenario: identificatore JavaScript semplice.
     * Atteso: Content-Type JavaScript.
     */
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

    /*
     * 18. Funzionalità: riconoscimento di callback JSONP qualificate.
     * Scenario: identificatori separati da punti.
     * Atteso: Content-Type JavaScript.
     */
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

    /*
     * 19. Funzionalità: validazione della callback JSONP.
     * Scenario: callback che inizia con una cifra.
     * Atteso: callback rifiutata e fallback a JSON.
     */
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

    /*
     * 20. Funzionalità: limite di lunghezza della callback JSONP.
     * Scenario: callback valida lunga esattamente 128 caratteri.
     * Atteso: callback accettata.
     */
    @Test
    void jsonResponseHeadersAcceptCallbackWith128Characters() {
        String callback = "a".repeat(128);

        Map<?, ?> result =
                UIHelpers.getJsonResponseHeaders(callback, null);

        assertEquals(
                "application/javascript;charset=utf-8",
                result.get("Content-Type")
        );
    }

    /*
     * 21. Funzionalità: limite di lunghezza della callback JSONP.
     * Scenario: callback lunga 129 caratteri.
     * Atteso: callback rifiutata.
     */
    @Test
    void jsonResponseHeadersRejectCallbackLongerThan128Characters() {
        String callback = "a".repeat(129);

        Map<?, ?> result =
                UIHelpers.getJsonResponseHeaders(callback, null);

        assertEquals(
                "application/json;charset=utf-8",
                result.get("Content-Type")
        );
    }

    /*
     * 22. Funzionalità: composizione degli header HTTP.
     * Scenario: header personalizzati con Content-Type custom.
     * Atteso: gli header del chiamante sovrascrivono quelli standard.
     */
    @Test
    void jsonResponseHeadersAllowCustomHeaderOverride() {
        Map<String, Object> customHeaders = new HashMap<>();
        customHeaders.put(
                "Content-Type",
                "application/problem+json"
        );
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

    /*
     * 23. Funzionalità: serializzazione JSON.
     * Scenario: mappa con un valore numerico.
     * Atteso: oggetto JSON equivalente.
     */
    @Test
    void jsonResponseBodySerializesMap() {
        Map<String, Object> data = new HashMap<>();
        data.put("value", 7);

        String result = UIHelpers.getJsonResponseBody(
                data,
                null,
                true
        );

        assertEquals("{\"value\":7}", result);
    }

    /*
     * 24. Funzionalità: gestione di un corpo già serializzato.
     * Scenario: serializzazione disabilitata e callback assente.
     * Atteso: corpo invariato.
     */
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

    /*
     * 25. Funzionalità: produzione di JSONP.
     * Scenario: callback valida e corpo già serializzato.
     * Atteso: invocazione JavaScript terminata da punto e virgola.
     */
    @Test
    void jsonResponseBodyWrapsRawBodyInValidCallback() {
        String result = UIHelpers.getJsonResponseBody(
                "{\"status\":\"ok\"}",
                "done",
                false
        );

        assertEquals(
                "done({\"status\":\"ok\"});",
                result
        );
    }

    /*
     * 26. Funzionalità: protezione da callback JSONP non valide.
     * Scenario: callback contenente parentesi.
     * Atteso: il corpo non viene avvolto.
     */
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

    /*
     * 27. Funzionalità: rilevamento del logviewer sicuro.
     * Scenario: configurazione con la sola porta HTTP.
     * Atteso: modalità non sicura.
     */
    @Test
    void isSecureLogviewerReturnsFalseWhenHttpsPortIsAbsent() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);

        assertFalse(UIHelpers.isSecureLogviewer(config));
    }

    /*
     * 28. Funzionalità: rilevamento del logviewer sicuro.
     * Scenario: porta HTTPS negativa.
     * Atteso: modalità non sicura.
     */
    @Test
    void isSecureLogviewerReturnsFalseForNegativeHttpsPort() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, -1);

        assertFalse(UIHelpers.isSecureLogviewer(config));
    }

    /*
     * 29. Funzionalità: confine per l'abilitazione HTTPS.
     * Scenario: porta HTTPS uguale a zero.
     * Atteso: modalità sicura secondo il confronto >= 0.
     */
    @Test
    void isSecureLogviewerReturnsTrueForZeroHttpsPort() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, 0);

        assertTrue(UIHelpers.isSecureLogviewer(config));
    }

    /*
     * 30. Funzionalità: selezione della porta del logviewer.
     * Scenario: HTTPS disabilitato e porta HTTP configurata.
     * Atteso: porta HTTP.
     */
    @Test
    void getLogviewerPortReturnsHttpPortWhenHttpsIsDisabled() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, -1);
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);

        assertEquals(
                8000,
                UIHelpers.getLogviewerPort(config)
        );
    }

    /*
     * 31. Funzionalità: selezione della porta del logviewer.
     * Scenario: entrambe le porte configurate e HTTPS abilitato.
     * Atteso: porta HTTPS.
     */
    @Test
    void getLogviewerPortReturnsHttpsPortWhenHttpsIsEnabled() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, 8443);

        assertEquals(
                8443,
                UIHelpers.getLogviewerPort(config)
        );
    }

    /*
     * 32. Funzionalità: costruzione del link HTTP al logviewer.
     * Scenario: HTTP attivo e file contenente uno slash.
     * Atteso: protocollo e porta HTTP, con file URL-encoded.
     */
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
                "http://worker1:8000/api/v1/log"
                        + "?file=topology%2Fworker.log",
                result
        );
    }

    /*
     * 33. Funzionalità: costruzione del link HTTPS al logviewer.
     * Scenario: porta HTTPS configurata.
     * Atteso: protocollo HTTPS e porta HTTPS.
     */
    @Test
    void getLogviewerLinkBuildsHttpsUrlWhenHttpsIsEnabled() {
        Map<String, Object> config = new HashMap<>();
        config.put(DaemonConfig.LOGVIEWER_PORT, 8000);
        config.put(DaemonConfig.LOGVIEWER_HTTPS_PORT, 8443);

        String result = UIHelpers.getLogviewerLink(
                "worker1",
                "worker.log",
                config,
                6700
        );

        assertEquals(
                "https://worker1:8443/api/v1/log"
                        + "?file=worker.log",
                result
        );
    }

    /*
     * 34. Funzionalità: presentazione della finestra temporale.
     * Scenario: valore speciale :all-time.
     * Atteso: etichetta leggibile "All time".
     */
    @Test
    void getWindowHintFormatsAllTimeWindow() {
        assertEquals(
                "All time",
                UIHelpers.getWindowHint(":all-time")
        );
    }

    /*
     * 35. Funzionalità: sanitizzazione di un nome di stream vuoto.
     * Scenario: il nome non contiene alcun carattere e quindi non può
     * iniziare con una lettera.
     * Atteso: viene restituito il prefisso "_s", ottenendo comunque
     * un identificatore non vuoto utilizzabile nella visualizzazione.
     */
    @Test
    void sanitizeStreamNamePrefixesEmptyName() {
        assertEquals(
                "_s",
                UIHelpers.sanitizeStreamName("")
        );
    }
}
