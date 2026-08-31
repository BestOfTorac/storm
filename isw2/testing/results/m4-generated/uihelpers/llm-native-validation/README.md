# UIHelpers LLM raw native validation

Repository HEAD: 6fb84f954982999eb1f11393c58ef0a5028db284

No raw generated test was modified or repaired.
No coverage or mutation feedback was used.

## C1
- Raw tests: 35
- Production compilation: PASS
- Test compilation: FAIL
- JUnit: NOT RUN
- Cause: incompatible generic map types in generated test source.

## C2
- Raw tests: 35
- Production compilation: PASS
- Test compilation: PASS
- JUnit: 34 PASS / 1 FAIL
- Failing test: T14_urlFormatPercentEncodesReservedCharacters
- Expected: /component/a%2Fb%20c
- Observed: /component/a%2Fb+c

## C3
- Raw tests: 35
- Production compilation: PASS
- Test compilation: PASS
- JUnit: 32 PASS / 3 FAIL

Failures:
1. t34SanitizeStreamNameReplacesInvalidCharactersAndPrefixesNonLetter
   Expected: orders.main-v2
   Observed: orders.main-v_

2. t12UrlFormatEncodesSpacesAndUnicodeUtf8
   Expected: /api/a%20b/caf%C3%A9
   Observed: /api/a+b/caf%C3%A9

3. t32LogviewerLinkUsesHttpAndEncodesFileName
   Expected: http://worker%20one:8000/api/v1/log?file=a%20b.log
   Observed: http://worker+one:8000/api/v1/log?file=a+b.log

## Experimental policy
These failures are retained as part of the raw LLM generation quality.
No generated test is repaired, rewritten, removed, or regenerated using validation feedback.