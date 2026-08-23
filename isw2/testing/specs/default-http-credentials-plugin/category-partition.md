# DefaultHttpCredentialsPlugin - Category Partition

## Scope

Target:

`org.apache.storm.security.auth.DefaultHttpCredentialsPlugin`

Apache Storm baseline:

`3.0.0`

The target exposes three public operations:

- `prepare(Map<String, Object>)`
- `getUserName(HttpServletRequest)`
- `populateContext(ReqContext, HttpServletRequest)`

This specification defines the initial manual black-box suite `T_BB`.

Exactly 10 representative test frames are selected.

The suite size is intentionally smaller than the UIHelpers suite because the
target has a much smaller public behavioral surface.

## Design chronology

The class and its direct production dependencies were inspected during a
testability and dependency pre-flight before this Category Partition was
finalized.

Therefore this experiment does not claim that the production implementation
was unseen.

However:

- no project-native Storm tests were inspected or reused;
- no ISW2 test for this class existed before this specification;
- no JaCoCo measurement for this class was available;
- no PIT result for this class was available;
- no coverage target was used to select the test frames;
- no surviving mutant was used to select the test frames.

The categories are defined in terms of public inputs and observable results.

When the public documentation does not specify an exact result precisely
enough, the oracle is classified as `INFERRED_FROM_PRODUCTION`.

## Available observation points

The returned or mutated `ReqContext` can be observed through its public API:

- `subject()`
- `principal()`
- `realPrincipal()`
- `isImpersonating()`

Principal identity can be observed through:

- `Principal.getName()`

`HttpServletRequest` is an external interface and may be represented with
Mockito in the black-box tests.

Mockito is used only to provide request inputs.

Interaction verification such as `verify(request)...` is not an oracle for
the initial black-box suite.

---

# F1 - prepare

## Functional responsibility

`prepare` performs plugin initialization.

The target documents this implementation as a no-op.

## Category F1.C1 - configuration object

Choices:

- C1.1: non-null ordinary configuration;
- C1.2: null configuration.

## Selected choice

`C1.1`

A non-null map containing representative values is supplied.

Expected observable result:

- method completes normally;
- supplied map remains unchanged.

Oracle source:

`PUBLIC_TARGET_DOCUMENTATION`

## Deferred choice

`C1.2` is not selected for T_BB.

A null Storm configuration is not established as a normal operational input
by the public contract. It may be reconsidered later only if control-flow or
mutation analysis provides a justified reason.

---

# F2 - getUserName

## Functional responsibility

Return the authenticated user represented by an HTTP request.

The public interface states that the result is the authenticated user, or
null when none is authenticated.

## Category F2.C1 - request principal

Choices:

- C1.1: principal present with non-empty name;
- C1.2: principal absent;
- C1.3: principal present with empty name.

## Category F2.C2 - remote user

Choices:

- C2.1: non-empty remote user;
- C2.2: remote user absent;
- C2.3: empty remote user.

## Category F2.C3 - competing identity sources

Choices:

- C3.1: valid principal only;
- C3.2: remote user only;
- C3.3: valid principal and valid remote user;
- C3.4: unusable principal name and valid remote user;
- C3.5: no usable identity.

## Constraints

- A valid non-empty principal is the preferred authenticated identity.
- Remote user is the fallback when the principal does not provide a usable
  name.
- Empty remote-user behavior is not required by the public contract and is
  deferred.

## Selected frames

### DHCP-BB-02

Principal:

`alice`

Remote user:

`bob`

Expected:

`alice`

Purpose:

verify precedence when both public identity sources contain values.

Oracle source:

`INFERRED_FROM_PRODUCTION`

### DHCP-BB-03

Principal:

absent

Remote user:

`bob`

Expected:

`bob`

Purpose:

verify the documented authenticated-user result through the remote-user
fallback.

Oracle source:

`INFERRED_FROM_PRODUCTION`

### DHCP-BB-04

Principal name:

empty string

Remote user:

`bob`

Expected:

`bob`

Purpose:

represent the significant boundary between an empty and non-empty principal
name.

Oracle source:

`INFERRED_FROM_PRODUCTION`

### DHCP-BB-05

Principal:

absent

Remote user:

absent

Expected:

`null`

Purpose:

represent a request with no authenticated identity.

Oracle source:

`PUBLIC_INTERFACE_CONTRACT`

## Deferred choices

The following are deliberately not selected for T_BB:

- null request object;
- empty remote-user string.

A null request is outside the normal servlet-request operational profile.

The public contract does not establish an empty remote-user string as a
distinct authenticated identity state.

Both may be reconsidered in later adequacy-guided evolution if justified.

---

# F3 - populateContext

## Functional responsibility

Populate the supplied `ReqContext` using credentials from the HTTP request.

The observable state includes:

- effective subject principal;
- real principal;
- impersonation state;
- returned context identity.

## Category F3.C1 - base authenticated identity

Choices:

- C1.1: authenticated non-empty user;
- C1.2: no authenticated user.

## Category F3.C2 - doAsUser header

Choices:

- C2.1: absent;
- C2.2: present with non-empty value;
- C2.3: present with empty value.

## Category F3.C3 - doAsUser parameter

Choices:

- C3.1: absent;
- C3.2: present with non-empty value;
- C3.3: present with empty value.

## Category F3.C4 - impersonation source

Choices:

- C4.1: no impersonation value;
- C4.2: header value;
- C4.3: parameter value;
- C4.4: both header and parameter values.

## Constraints

- The parameter is the fallback source when the header is absent.
- When both sources are present, the header has precedence.
- Empty doAsUser values are not included in the initial functional baseline
  because their intended external semantics are not explicitly documented.
- Impersonation without an authenticated base identity is deferred because
  its security semantics depend on the surrounding authentication/
  authorization pipeline and are not established by this target's public
  contract.

## Selected frames

### DHCP-BB-06

Authenticated user:

`alice`

doAsUser:

absent from both sources

Expected:

- the same supplied context is returned;
- effective subject principal is `alice`;
- real principal is null;
- context is not impersonating.

Oracle source:

`INFERRED_FROM_PRODUCTION`

### DHCP-BB-07

Authenticated user:

absent

doAsUser:

absent from both sources

Expected:

- the same supplied context is returned;
- subject exists but exposes no primary principal;
- real principal is null;
- context is not impersonating.

Oracle source:

`INFERRED_FROM_PRODUCTION`

### DHCP-BB-08

Authenticated user:

`alice`

Header:

`doAsUser=bob`

Parameter:

absent

Expected:

- the same supplied context is returned;
- effective subject principal is `bob`;
- real principal is `alice`;
- context is impersonating.

Oracle source:

`INFERRED_FROM_PRODUCTION`

### DHCP-BB-09

Authenticated user:

`alice`

Header:

absent

Parameter:

`doAsUser=carol`

Expected:

- the same supplied context is returned;
- effective subject principal is `carol`;
- real principal is `alice`;
- context is impersonating.

Oracle source:

`INFERRED_FROM_PRODUCTION`

### DHCP-BB-10

Authenticated user:

`alice`

Header:

`doAsUser=bob`

Parameter:

`doAsUser=carol`

Expected:

- the same supplied context is returned;
- effective subject principal is `bob`;
- real principal is `alice`;
- context is impersonating.

Purpose:

verify the externally observable precedence of the two impersonation inputs.

Oracle source:

`INFERRED_FROM_PRODUCTION`

## Deferred choices

The following are not part of initial T_BB:

- empty `doAsUser` header;
- empty `doAsUser` parameter;
- null `ReqContext`;
- null request passed to `populateContext`;
- impersonation value with no authenticated base user.

These cases are either outside the documented operational input assumptions
or have insufficiently specified functional semantics.

They remain candidate inputs for later control-flow or mutation-guided
analysis when a concrete adequacy gap justifies them.

---

# Selected T_BB frame inventory

The initial suite consists of exactly ten frames:

1. `DHCP-BB-01` - prepare preserves an ordinary configuration
2. `DHCP-BB-02` - valid principal has precedence over remote user
3. `DHCP-BB-03` - remote user is used when principal is absent
4. `DHCP-BB-04` - empty principal name falls back to remote user
5. `DHCP-BB-05` - no authenticated identity returns null
6. `DHCP-BB-06` - authenticated user populates subject without impersonation
7. `DHCP-BB-07` - unauthenticated request produces no effective principal
8. `DHCP-BB-08` - doAsUser header establishes impersonation
9. `DHCP-BB-09` - doAsUser parameter establishes impersonation
10. `DHCP-BB-10` - doAsUser header has precedence over parameter

No additional frame is included merely to increase test count.

# Planned test technology

- Java 25
- JUnit Jupiter
- Mockito only for `HttpServletRequest`
- no network
- no server startup
- no sleeps
- no native Storm tests
- no production-code modification

# Adequacy chronology

After T_BB is implemented and frozen:

1. execute exactly the selected ten tests;
2. measure Line Coverage;
3. measure Branch Coverage;
4. use those measurements to guide the separate T_CF evolution.

Mutation testing is not used to design T_BB.

The initial T_BB suite remains frozen as the manual black-box baseline.
