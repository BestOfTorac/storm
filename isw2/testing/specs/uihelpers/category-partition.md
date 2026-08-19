# UIHelpers - Category Partition Specification

## Purpose

This document defines the manual black-box test design for:

`org.apache.storm.daemon.ui.UIHelpers`

The test design is produced independently from the native Apache Storm test
suite. Native Storm tests are neither executed nor reused.

The initial manual suite is identified as `T_BB`.

## Method

The test design follows Category Partition.

For each externally observable functionality we identify:

1. functionality;
2. input categories;
3. choices for each category;
4. constraints among choices;
5. representative test frames;
6. expected externally observable behavior.

Boundary values are used where the input domain contains meaningful
boundaries.

At this stage the objective is NOT structural coverage. Control-flow
information will be considered later when evolving `T_BB` into `T_CF`.

## Functional decomposition

| ID | Functional area |
|---|---|
| F1 | Formatting |
| F2 | URL and logviewer links |
| F3 | JSON and HTTP responses |
| F4 | Jetty, SSL and servlet filters |
| F5 | Cluster and resource summaries |
| F6 | Supervisor and worker summaries |
| F7 | Topology summaries |
| F8 | Bolt, spout and executor statistics |
| F9 | Error representation |
| F10 | Topology visualization |
| F11 | Profiling |
| F12 | Topology operations |
| F13 | Nimbus and logging |


## F1 - Formatting

### F1.1 Uptime formatting - seconds

#### Categories

**Input magnitude**

- zero;
- seconds only;
- minutes;
- hours;
- days.

**Boundary relation**

- immediately below a unit boundary;
- exactly on a unit boundary;
- immediately above a unit boundary.

**Input representation**

- String overload;
- int overload.

#### Representative test frames

| ID | Input | Expected semantic result |
|---|---:|---|
| U-S-01 | 0 | empty formatted duration |
| U-S-02 | 1 | `1s` |
| U-S-03 | 59 | `59s` |
| U-S-04 | 60 | `1m` |
| U-S-05 | 61 | `1m 1s` |
| U-S-06 | 3599 | `59m 59s` |
| U-S-07 | 3600 | `1h` |
| U-S-08 | 3601 | `1h 1s` |
| U-S-09 | 86399 | `23h 59m 59s` |
| U-S-10 | 86400 | `1d` |
| U-S-11 | 90061 | `1d 1h 1m 1s` |

The same representative domain is exercised through both the String and int
overloads where meaningful.

### F1.2 Uptime formatting - milliseconds

Relevant boundaries:

- 1000 ms;
- 60000 ms;
- 3600000 ms;
- 86400000 ms.

Representative values:

- 0;
- 1;
- 999;
- 1000;
- 1001;
- 59999;
- 60000;
- 60001;
- 3600000;
- 86400000;
- 90061001.

### F1.3 Invalid uptime textual representation

Robustness categories:

- non-numeric string;
- empty string;
- null;
- negative numeric value.

These cases characterize the current externally observable behavior.
They are not interpreted as requirements that the API must accept malformed
uptime values.

### F1.4 Executor range formatting

Categories:

- single executor task: start = end;
- normal range: start < end;
- unusual range: start > end;
- zero-valued range.

Representative pairs:

- (1, 1);
- (1, 5);
- (5, 1);
- (0, 0).

### F1.5 Window hint

Partitions:

- special value `:all-time`;
- regular numeric window.

Representative values:

- `:all-time`;
- `0`;
- `59`;
- `60`;
- `3600`.

### F1.6 Stream-name sanitization

**First-character category**

- alphabetic;
- non-alphabetic;
- empty input.

**Remaining-character category**

- only allowed characters;
- at least one disallowed character.

Representative values:

- `stream`;
- `stream_1`;
- `stream-name`;
- `stream.name`;
- `stream name`;
- `stream@name`;
- `1stream`;
- `_stream`;
- `-stream`;
- `.stream`;
- empty string.


### F1.7 Explicit oracles for representative frames

The following expected values complete the black-box oracle for the
representative F1 frames.

#### Millisecond formatting

| ID | Input | Expected result |
|---|---:|---|
| U-MS-01 | 0 | empty formatted duration |
| U-MS-02 | 1 | `1ms` |
| U-MS-03 | 999 | `999ms` |
| U-MS-04 | 1000 | `1s` |
| U-MS-05 | 1001 | `1s 1ms` |
| U-MS-06 | 59999 | `59s 999ms` |
| U-MS-07 | 60000 | `1m` |
| U-MS-08 | 60001 | `1m 1ms` |
| U-MS-09 | 3600000 | `1h` |
| U-MS-10 | 86400000 | `1d` |
| U-MS-11 | 90061001 | `1d 1h 1m 1s 1ms` |

#### Executor formatting

| ID | Start | End | Expected result |
|---|---:|---:|---|
| EX-01 | 1 | 1 | `[1-1]` |
| EX-02 | 1 | 5 | `[1-5]` |
| EX-03 | 5 | 1 | `[5-1]` |
| EX-04 | 0 | 0 | `[0-0]` |

#### Window hints

| ID | Input | Expected result |
|---|---|---|
| WH-01 | `:all-time` | `All time` |
| WH-02 | `0` | empty formatted duration |
| WH-03 | `59` | `59s` |
| WH-04 | `60` | `1m` |
| WH-05 | `3600` | `1h` |

#### Stream-name sanitization

| ID | Input | Expected result |
|---|---|---|
| SN-01 | `stream` | `stream` |
| SN-02 | `stream_1` | `stream__` |
| SN-03 | `stream-name` | `stream-name` |
| SN-04 | `stream.name` | `stream.name` |
| SN-05 | `stream name` | `stream_name` |
| SN-06 | `stream@name` | `stream_name` |
| SN-07 | `1stream` | `_s_stream` |
| SN-08 | `_stream` | `_s_stream` |
| SN-09 | `-stream` | `_s-stream` |
| SN-10 | `.stream` | `_s.stream` |
| SN-11 | empty string | `_s` |


## F2 - URL and logviewer links

### F2.1 URL formatting

The functionality builds a formatted URL after applying URL encoding to each
argument.

#### Categories

**Number of formatted arguments**

- one argument;
- multiple arguments.

**Argument representation**

- String;
- numeric value;
- null value.

**Argument content**

- already URL-safe;
- contains characters requiring URL encoding.

#### Representative test frames

| ID | Format / arguments | Expected semantic result |
|---|---|---|
| URL-01 | `/x/%s`, `abc` | `/x/abc` |
| URL-02 | `/x/%s/%s`, `host`, `8080` | both arguments inserted in order |
| URL-03 | `/x/%s`, value containing a space | argument is URL encoded |
| URL-04 | `/x/%s`, value containing URL metacharacters | argument is URL encoded |
| URL-05 | `/x/%s`, null | textual `null` value is formatted |

Exact special-character encoding is checked against the standard UTF-8 URL
encoding contract rather than against another invocation of UIHelpers.

### F2.2 Logviewer security mode

The observable functionality chooses the HTTP or HTTPS logviewer endpoint
according to the supplied configuration.

#### Categories

**HTTPS configuration**

- HTTPS port absent;
- HTTPS port negative;
- HTTPS port zero;
- HTTPS port positive.

**HTTP configuration**

- HTTP port present.

#### Representative test frames

| ID | HTTPS port | HTTP port | Expected mode |
|---|---:|---:|---|
| SEC-01 | absent | 8000 | HTTP |
| SEC-02 | -1 | 8000 | HTTP |
| SEC-03 | 0 | 8000 | HTTPS |
| SEC-04 | 8443 | 8000 | HTTPS |

The zero boundary is retained as a characterization case because it is
externally distinguishable from a negative HTTPS-port configuration.

### F2.3 Logviewer port selection

Representative frames:

| ID | Configuration | Expected result |
|---|---|---:|
| PORT-01 | HTTP=8000, HTTPS absent | 8000 |
| PORT-02 | HTTP=8000, HTTPS=-1 | 8000 |
| PORT-03 | HTTP=8000, HTTPS=8443 | 8443 |

### F2.4 Generic logviewer links

#### Categories

**Protocol**

- HTTP;
- HTTPS.

**Host**

- regular hostname.

**Filename**

- regular filename;
- filename requiring URL encoding.

#### Representative frames

| ID | Mode | Host | Filename | Expected semantic result |
|---|---|---|---|---|
| LOG-01 | HTTP | `worker.example` | `worker.log` | HTTP log endpoint using configured HTTP port |
| LOG-02 | HTTPS | `worker.example` | `worker.log` | HTTPS log endpoint using configured HTTPS port |
| LOG-03 | HTTP | `worker.example` | filename containing a space | filename is URL encoded |

### F2.5 Nimbus and Supervisor daemon-log links

Representative frames:

| ID | Function | Mode | Expected path |
|---|---|---|---|
| DAEMON-01 | Nimbus | HTTP | `/api/v1/daemonlog?file=nimbus.log` |
| DAEMON-02 | Nimbus | HTTPS | `/api/v1/daemonlog?file=nimbus.log` |
| DAEMON-03 | Supervisor | HTTP | `/api/v1/daemonlog?file=supervisor.log` |
| DAEMON-04 | Supervisor | HTTPS | `/api/v1/daemonlog?file=supervisor.log` |

The selected scheme and port must correspond to the supplied logviewer
configuration.

### F2.6 Worker log links

#### Categories

- topology identifier;
- worker port;
- HTTP or HTTPS logviewer mode.

Representative frames exercise:

- a regular topology identifier and worker port;
- both HTTP and HTTPS logviewer configurations;
- a topology identifier requiring URL-safe representation.

The expected result is a logviewer URL targeting the worker log filename
associated with the topology and worker port.

### F2.7 Worker dump links

#### Categories

**Protocol**

- HTTP;
- HTTPS.

**Worker identity**

- host;
- port.

**Topology identity**

- regular topology identifier.

Representative frames:

| ID | Mode | Host | Port | Topology |
|---|---|---|---:|---|
| DUMP-01 | HTTP | `worker.example` | 6700 | `topology-1` |
| DUMP-02 | HTTPS | `worker.example` | 6700 | `topology-1` |

The expected endpoint is `/api/v1/dumps/<topology>/<host:port>` using the
configured logviewer scheme and port.


## F3 - JSON and HTTP responses

### F3.1 Unauthorized-user representation

#### User category

- regular user name;
- empty user name;
- null user name.

Representative frames verify that the returned representation contains:

- error = `No Authorization`;
- an error message associated with the supplied user identity.

### F3.2 JSONP callback domain

The callback is externally observable through both response body and
Content-Type.

#### Categories

**Callback presence**

- null;
- present.

**Callback syntax**

- simple valid identifier;
- valid dotted identifier;
- starts with an invalid character;
- contains an invalid character.

**Callback length**

- below maximum;
- exactly 128 characters;
- 129 characters.

#### Representative test frames

| ID | Callback | Category | Expected interpretation |
|---|---|---|---|
| CB-01 | null | absent | regular JSON |
| CB-02 | `callback` | valid simple | JSONP |
| CB-03 | `app.callback` | valid dotted | JSONP |
| CB-04 | `1callback` | invalid start | regular JSON |
| CB-05 | `callback()` | invalid syntax | regular JSON |
| CB-06 | valid identifier of length 128 | boundary valid | JSONP |
| CB-07 | valid identifier of length 129 | over boundary | regular JSON |

### F3.3 JSON response headers

#### Callback category

- absent or invalid;
- valid.

#### Additional-header category

- null;
- custom header present;
- custom header overriding a default header.

#### Required observable defaults

Every generated header map contains the standard cache, CORS and
content-type-protection headers.

For an absent or invalid callback:

`Content-Type = application/json;charset=utf-8`

For a valid callback:

`Content-Type = application/javascript;charset=utf-8`

Representative frames:

| ID | Callback | Extra headers | Expected content type |
|---|---|---|---|
| HDR-01 | null | none | JSON |
| HDR-02 | valid | none | JavaScript |
| HDR-03 | invalid | none | JSON |
| HDR-04 | null | custom header | JSON plus custom header |
| HDR-05 | null | override of a default header | supplied override is externally visible |

### F3.4 JSON response body

#### Serialization category

- serialization requested;
- serialization not requested.

#### Callback category

- null;
- valid;
- invalid.

#### Data category

- structured object when serialization is requested;
- String when serialization is not requested.

Representative frames:

| ID | Data | Serialize | Callback | Expected semantic result |
|---|---|---|---|---|
| BODY-01 | structured object | yes | null | serialized JSON |
| BODY-02 | structured object | yes | valid | callback-wrapped serialized JSON |
| BODY-03 | structured object | yes | invalid | serialized JSON without callback |
| BODY-04 | String | no | null | unchanged String |
| BODY-05 | String | no | valid | callback-wrapped String |
| BODY-06 | String | no | invalid | unchanged String |

### F3.5 Direct callback wrapping

Representative frame:

| ID | Callback | Response | Expected result |
|---|---|---|---|
| WRAP-01 | `callback` | `{"value":1}` | `callback({"value":1});` |

This function is specified for a callback that has already been validated.

### F3.6 Standard HTTP response construction

#### Status category

- default OK;
- explicit successful status;
- explicit non-success status.

#### Serialization category

- serialized;
- already serialized String.

#### Callback category

- absent;
- valid;
- invalid.

Representative frames verify jointly:

- HTTP status;
- response entity;
- Content-Type;
- standard response headers.

Example frames:

| ID | Status | Serialize | Callback |
|---|---|---|---|
| RESP-01 | default OK | yes | null |
| RESP-02 | explicit OK | yes | valid |
| RESP-03 | explicit non-success status | yes | null |
| RESP-04 | explicit OK | no | null |
| RESP-05 | explicit OK | no | valid |
| RESP-06 | explicit OK | yes | invalid |


## F4 - Jetty, SSL and servlet filters

### F4.1 HTTP server creation

The functionality creates and configures the HTTP side of a Jetty server.

#### Categories

**HTTP port**

- null;
- zero;
- positive.

**Host**

- null;
- explicit host.

**HTTPS state**

- absent or inactive;
- active.

**HTTP binding**

- enabled;
- disabled.

**Request-header size**

- default;
- explicitly configured.

#### Representative test frames

| ID | HTTP port | HTTPS port | Disable HTTP | Host | Header size | Expected behavior |
|---|---:|---:|---|---|---|---|
| JETTY-01 | 8080 | absent | false | null | default | HTTP connector on port 8080 |
| JETTY-02 | null | absent | false | null | default | HTTP connector using default port |
| JETTY-03 | 0 | absent | false | null | default | HTTP connector configured with port 0 |
| JETTY-04 | 8080 | 8443 | false | null | default | HTTP connector remains enabled |
| JETTY-05 | 8080 | 8443 | true | null | default | no HTTP connector |
| JETTY-06 | 8080 | 8443 | null | null | default | HTTP connector remains enabled |
| JETTY-07 | 8080 | absent | false | localhost | default | explicit host preserved |
| JETTY-08 | 8080 | absent | false | null | 16384 | explicit request-header size preserved |

### F4.2 SSL connector configuration

#### Categories

**SSL port**

- negative;
- zero;
- positive.

**Trust-store configuration**

- complete;
- absent;
- incomplete.

**Client-authentication mode**

- required;
- wanted;
- neither.

**Header-buffer configuration**

- default;
- explicit.

**SSL reload**

- disabled;
- enabled.

#### Representative test frames

| ID | SSL port | Need auth | Want auth | Header size | Expected behavior |
|---|---:|---|---|---|---|
| SSL-01 | -1 | false | false | default | no SSL connector |
| SSL-02 | 0 | false | false | default | no SSL connector |
| SSL-03 | 8443 | false | false | default | SSL connector on port 8443 |
| SSL-04 | 8443 | true | false | default | client authentication required |
| SSL-05 | 8443 | false | true | default | client authentication wanted |
| SSL-06 | 8443 | false | false | 16384 | custom request-header size preserved |
| SSL-07 | 8443 | true | true | default | required authentication takes precedence |

Trust-store characterization frames:

- TS-01: path, password and type all supplied;
- TS-02: trust-store information absent;
- TS-03: partially supplied trust-store information.

Both values of the SSL-reload option are retained as configuration choices.

### F4.3 CORS filter creation

The returned filter holder must represent the configured CORS filter.

Representative frame:

**CORS-01**

Expected externally observable configuration:

- filter implementation is `CrossOriginFilter`;
- allowed origins include `*`;
- allowed methods are `GET, POST, PUT`;
- configured request headers are present.

### F4.4 Access-logging filter creation

Representative frame:

**ACCESS-01**

The returned holder represents an `AccessLoggingFilter`.

### F4.5 Servlet-context filter configuration

#### Categories

**Configured-filter collection**

- empty;
- one filter;
- multiple filters.

**Filter class**

- present;
- absent.

**Filter name**

- explicit;
- absent.

**Initialization parameters**

- null;
- empty;
- non-empty.

#### Representative test frames

| ID | Collection | Class | Name | Parameters | Expected semantic result |
|---|---|---|---|---|---|
| FIL-01 | empty | - | - | - | standard CORS and access-logging filters configured |
| FIL-02 | one | present | explicit | null | custom filter added |
| FIL-03 | one | present | absent | null | filter class used as name |
| FIL-04 | one | present | explicit | non-empty | parameters preserved |
| FIL-05 | one | absent | explicit | non-empty | invalid custom filter omitted |
| FIL-06 | multiple | mixed | mixed | mixed | each valid custom filter configured |

### F4.6 Servlet and filter installation on a Server

#### Categories

**Filter configuration list**

- null;
- empty;
- non-empty.

**Servlet initialization parameters**

- null;
- empty;
- non-empty.

Representative frames:

- CFG-01: null filter configuration;
- CFG-02: empty filter configuration and no servlet parameters;
- CFG-03: one custom filter and no servlet parameters;
- CFG-04: one custom filter and custom servlet parameters.

The observable result is the handler/context installed on the supplied Jetty
server together with its servlet and filter configuration.

### F4.7 Jetty execution

This functionality starts a Jetty server after optional customization.

#### Categories

**Configurator**

- absent;
- present.

**Request-header size**

- default;
- explicit.

Representative frames:

- RUN-01: dynamic port (`0`) and no configurator;
- RUN-02: dynamic port (`0`) and a configurator.

A dynamic port is selected for testing so the specification does not depend
on a fixed local TCP port.

Tests that start a server must terminate it after the observation to avoid
leaking server resources.


## F5 - Cluster and resource summaries

### F5.1 Cluster summary

The functionality converts a Storm cluster summary and its configuration into
the information exposed to the UI.

#### Categories

**Supervisor population**

- no supervisors;
- one supervisor;
- multiple supervisors.

**Topology population**

- no topologies;
- one topology;
- multiple topologies.

**Resource utilization**

- capacity available and unused;
- capacity partially used;
- capacity fully used;
- zero total capacity.

**Generic resources**

- absent;
- present.

**UI configuration**

- optional UI URLs absent;
- optional UI URLs present.

#### Representative test frames

| ID | Supervisors | Topologies | Resource state | Generic resources | Expected semantic result |
|---|---|---|---|---|---|
| CLUSTER-01 | none | none | no allocated capacity | absent | zero supervisors, topologies, slots, tasks and executors |
| CLUSTER-02 | one | none | unused capacity | absent | total resources reported as available |
| CLUSTER-03 | one | one | partially used | absent | used/free slots and available resources correctly summarized |
| CLUSTER-04 | multiple | multiple | mixed utilization | absent | totals equal the aggregation of all summaries |
| CLUSTER-05 | one | one | zero total CPU/memory | absent | utilization percentages represented as zero |
| CLUSTER-06 | one | one | partially used | present | generic resource totals and availability exposed |
| CLUSTER-07 | one | one | partially used | absent | supplied optional UI configuration values preserved |

For aggregation frames, task and executor counts are treated as externally
observable cluster totals.

### F5.2 Owner resource summary

The functionality exposes resource consumption and guarantees associated with
an owner.

#### Categories

**Guarantee availability**

- memory and CPU guarantees set;
- guarantees unset;
- isolated-node guarantee set;
- isolated-node guarantee unset.

**Remaining guarantees**

- set;
- unset.

**Usage**

- zero;
- positive.

#### Representative test frames

| ID | Guarantees | Remaining | Usage | Expected semantic result |
|---|---|---|---|---|
| OWNER-01 | all set | all set | positive | supplied values preserved |
| OWNER-02 | all unset | unset | zero | unavailable guarantees represented as `N/A` where specified |
| OWNER-03 | memory/CPU set | remaining unset | positive | guarantees present, remaining guarantees unavailable |
| OWNER-04 | isolated-node guarantee set | mixed | positive | isolated-node value preserved |

The output must also preserve owner identity and topology/executor/worker/task
totals supplied by the summary.

### F5.3 Collection of owner resource summaries

#### Categories

**Owner collection**

- empty;
- one owner;
- multiple owners.

**Scheduler display configuration**

- present;
- absent.

#### Representative test frames

| ID | Owners | Scheduler display setting | Expected semantic result |
|---|---|---|---|
| OWNERS-01 | empty | present | empty owner collection |
| OWNERS-02 | one | present | one unpacked owner |
| OWNERS-03 | multiple | present | all owners represented |
| OWNERS-04 | one | absent | owner data remains available and scheduler-display value is absent/null |

### F5.4 Specific owner summary

The functionality combines the selected owner's resource information with the
topologies owned by that user.

#### Categories

**Owner resource summary**

- no summary available;
- one summary available.

**Topology collection**

- empty;
- contains topologies owned by the selected owner;
- contains topologies owned by other users;
- mixed ownership.

#### Representative test frames

| ID | Owner summary | Topologies | Expected semantic result |
|---|---|---|---|
| OWNERPAGE-01 | absent | not required | default summary for requested owner |
| OWNERPAGE-02 | present | empty | owner information with empty topology collection |
| OWNERPAGE-03 | present | matching topology | matching topology exposed |
| OWNERPAGE-04 | present | unrelated topology | unrelated topology omitted |
| OWNERPAGE-05 | present | mixed ownership | only matching owner's topologies exposed |


## F6 - Supervisor and worker summaries

### F6.1 Supervisor representation

#### Categories

**Worker-slot utilization**

- no slots used;
- partially used;
- all slots used;
- reported used slots greater than total slots.

**Supervisor status**

- not blacklisted;
- blacklisted.

**Resource utilization**

- unused;
- partially used;
- fully used.

**Generic resources**

- absent;
- present.

**Logviewer mode**

- HTTP;
- HTTPS.

#### Representative test frames

| ID | Slots | Blacklisted | Resources | Generics | Log mode | Expected semantic result |
|---|---|---|---|---|---|---|
| SUP-01 | none used | no | unused | absent | HTTP | all slots and resources available |
| SUP-02 | partial | no | partial | absent | HTTP | correct used/free resources |
| SUP-03 | all used | no | full | absent | HTTP | zero free slots |
| SUP-04 | used > total | no | mixed | absent | HTTP | free slots never exposed as negative |
| SUP-05 | partial | yes | partial | absent | HTTP | blacklist state preserved |
| SUP-06 | partial | no | partial | present | HTTP | generic totals, usage and availability exposed |
| SUP-07 | partial | no | partial | absent | HTTPS | supervisor log link uses secure endpoint |

The supervisor identity, host, uptime and version supplied by the input are
also expected to remain observable.

### F6.2 Worker collection

#### Worker-summary availability

- worker summaries not set;
- empty collection;
- one worker;
- multiple workers.

#### Representative test frames

| ID | Worker state | Expected semantic result |
|---|---|---|
| WORKERS-01 | summaries not set | empty result |
| WORKERS-02 | empty collection | empty result |
| WORKERS-03 | one worker | one worker representation |
| WORKERS-04 | multiple workers | all worker representations exposed |

### F6.3 Worker representation

For each worker the observable information includes:

- supervisor identity;
- host;
- port;
- topology identity and name;
- executor count;
- assigned on-heap and off-heap memory;
- assigned CPU;
- component task counts;
- uptime;
- worker log link;
- owner.

#### Categories

**Assigned resources**

- zero;
- positive.

**Executor count**

- zero;
- positive.

**Logviewer mode**

- HTTP;
- HTTPS.

Representative frames:

| ID | Executors | Resources | Log mode | Expected semantic result |
|---|---:|---|---|---|
| WORKER-01 | 0 | zero | HTTP | zero values preserved |
| WORKER-02 | positive | positive | HTTP | supplied worker values preserved |
| WORKER-03 | positive | positive | HTTPS | worker link uses secure logviewer |

### F6.4 Supervisor summary collection

#### Categories

**Supervisor population**

- empty;
- one supervisor;
- multiple supervisors.

**Logviewer mode**

- HTTP;
- HTTPS.

Representative frames:

| ID | Supervisors | Mode | Expected semantic result |
|---|---|---|---|
| SUPLIST-01 | empty | HTTP | empty supervisor collection |
| SUPLIST-02 | one | HTTP | one transformed supervisor |
| SUPLIST-03 | multiple | HTTP | all supervisors represented |
| SUPLIST-04 | one | HTTPS | secure logviewer information exposed |

The scheduler-display-resource configuration must be preserved in the
result.

### F6.5 Supervisor page

The functionality combines supervisor and worker information into one
externally visible page representation.

#### Categories

**Supervisor population**

- empty;
- non-empty.

**Worker population**

- not set or empty;
- non-empty.

**Logviewer mode**

- HTTP;
- HTTPS.

Representative frames:

| ID | Supervisors | Workers | Mode | Expected semantic result |
|---|---|---|---|---|
| SUPPAGE-01 | empty | empty | HTTP | empty page collections with HTTP logviewer metadata |
| SUPPAGE-02 | present | empty | HTTP | supervisors exposed without workers |
| SUPPAGE-03 | present | present | HTTP | supervisors and workers exposed |
| SUPPAGE-04 | present | present | HTTPS | secure logviewer metadata exposed |


## F7 - Topology summaries

### F7.1 Basic topology representation

The functionality exposes the externally relevant identity, execution state,
resource information and version information of a topology.

#### Categories

**Topology identity**

- regular identifier;
- identifier containing characters requiring URL encoding.

**Topology execution state**

- active/running-like state;
- alternative state.

**Execution size**

- zero tasks/workers/executors;
- positive tasks/workers/executors.

**Resource values**

- zero;
- positive.

**Generic resources**

- absent or empty;
- present.

#### Representative test frames

| ID | Identity | Execution size | Resources | Generics | Expected semantic result |
|---|---|---|---|---|---|
| TOP-01 | regular | zero | zero | absent | identity and zero execution values preserved |
| TOP-02 | regular | positive | positive | absent | execution and resource values preserved |
| TOP-03 | encoded characters | positive | positive | absent | raw and URL-safe identity exposed consistently |
| TOP-04 | regular | positive | positive | present | requested and assigned generic resources represented |
| TOP-05 | regular | positive | zero | absent | zero resource values preserved |

The representation is expected to distinguish requested and assigned
resources and to expose their corresponding totals consistently.

### F7.2 Collection of topologies

#### Categories

**Topology collection**

- empty;
- one topology;
- multiple topologies.

**Scheduler display configuration**

- present;
- absent.

#### Representative test frames

| ID | Topologies | Scheduler setting | Expected semantic result |
|---|---|---|---|
| TOPLIST-01 | empty | present | empty topology list |
| TOPLIST-02 | one | present | one transformed topology |
| TOPLIST-03 | multiple | present | all topologies represented |
| TOPLIST-04 | one | absent | topology information remains available |

### F7.3 Detailed topology page

The functionality exposes a detailed topology view including configuration,
statistics, workers, components and UI metadata.

#### Categories

**Requested window**

- `:all-time`;
- regular numeric window.

**Remote user**

- present;
- null.

**Topology configuration**

- valid configuration containing message timeout;
- additional configuration values present.

**Workers**

- not set or empty;
- one;
- multiple.

**Spouts**

- empty;
- non-empty.

**Bolts**

- empty;
- non-empty.

**Debug state**

- debug options absent;
- debugging disabled;
- debugging enabled.

#### Representative test frames

| ID | Window | Workers | Spouts | Bolts | Debug | Expected semantic result |
|---|---|---|---|---|---|---|
| TOPPAGE-01 | `:all-time` | empty | empty | empty | absent | basic topology page with all-time hint |
| TOPPAGE-02 | numeric | one | empty | empty | disabled | worker and window information exposed |
| TOPPAGE-03 | numeric | multiple | present | present | full topology/component information exposed |
| TOPPAGE-04 | numeric | one | present | present | enabled | debugging state and sampling percentage exposed |

The result must also preserve:

- topology identity and owner;
- status;
- task, worker and executor counts;
- requested and assigned resources;
- message timeout from topology configuration;
- remote user;
- scheduler display setting;
- optional bug-tracker and central-log URLs.

### F7.4 Topology worker locations

The functionality exposes the distinct host/port locations associated with a
topology's executors.

#### Categories

**Executor collection**

- empty;
- one executor;
- multiple executors on distinct workers;
- multiple executors sharing the same worker.

#### Representative test frames

| ID | Executors | Expected semantic result |
|---|---|---|
| TOPWORK-01 | empty | empty host/port list |
| TOPWORK-02 | one | one host/port entry |
| TOPWORK-03 | multiple distinct | all distinct worker locations |
| TOPWORK-04 | repeated host/port | duplicate worker location represented once |

Logviewer metadata is also expected to correspond to the supplied
configuration.

### F7.5 Topology history

#### Categories

**History**

- empty;
- one topology identifier;
- multiple topology identifiers.

Representative frames verify that the supplied topology-history identifiers
are preserved in the externally visible representation.

### F7.6 Topology spout lag

#### Categories

**Lag monitoring configuration**

- enabled;
- disabled.

**Topology**

- contains spouts relevant to lag monitoring;
- no relevant spout data.

Representative frames:

| ID | Monitoring | Topology state | Expected semantic result |
|---|---|---|---|
| LAG-01 | disabled | any | empty lag information |
| LAG-02 | enabled | no relevant lag data | no relevant lag entries |
| LAG-03 | enabled | lag-capable spout data | computed lag information exposed |


## F8 - Bolt, spout and executor statistics

### F8.1 Statistical-window mapping

The functionality converts raw window identifiers into display-oriented
window names while preserving the corresponding statistical values.

#### Categories

**Window**

- `:all-time`;
- seconds-only window;
- minute/hour-scale window.

**Collection**

- empty;
- one entry;
- multiple entries.

Representative frames:

| ID | Input map | Expected semantic result |
|---|---|---|
| STATWIN-01 | empty | empty result |
| STATWIN-02 | `:all-time` only | value associated with `All time` |
| STATWIN-03 | numeric window only | value associated with formatted duration |
| STATWIN-04 | mixed windows | all values preserved under formatted keys |

### F8.2 Transferred-stat stream sanitization

#### Categories

**Outer statistics map**

- empty;
- one window;
- multiple windows.

**Stream identifiers**

- already valid;
- require sanitization;
- mixture of valid and invalid characters.

Representative frames verify that:

- window keys are preserved;
- stream names are sanitized consistently with the F1 stream-name contract;
- transferred values are unchanged.

### F8.3 Executor-summary statistics

#### Categories

**Executor statistics**

- statistics absent;
- statistics present.

**Transferred map**

- empty;
- non-empty.

Representative frames:

| ID | Stats | Expected semantic result |
|---|---|---|
| EXECSTAT-01 | absent | host, port and uptime exposed; transferred value absent/null |
| EXECSTAT-02 | present but empty | empty transferred-stat representation |
| EXECSTAT-03 | present and non-empty | sanitized transferred statistics exposed |

### F8.4 Component input representation

A topology input is represented by its source component, stream identifier,
sanitized stream identifier and grouping type.

#### Stream-name category

- already valid;
- requires sanitization.

#### Grouping category

- representative valid grouping type.

Representative frames verify preservation of component and stream identity,
stream sanitization and externally visible grouping information.

### F8.5 Bolt executor grouping

#### Categories

**Executor collection**

- empty;
- one Bolt executor;
- multiple executors belonging to one Bolt;
- executors belonging to multiple Bolts;
- mixture of Bolt and non-Bolt components.

**System-component handling**

- system components excluded;
- system components included.

Representative frames:

| ID | Executors | System mode | Expected semantic result |
|---|---|---|---|
| BEXEC-01 | empty | exclude | empty result |
| BEXEC-02 | one Bolt | exclude | Bolt grouped by component identifier |
| BEXEC-03 | several same Bolt | exclude | all executors grouped under same component |
| BEXEC-04 | multiple Bolts | exclude | independent groups |
| BEXEC-05 | Bolt plus non-Bolt | exclude | only Bolt executors represented |
| BEXEC-06 | system Bolt present | exclude/include | system visibility follows requested mode |

### F8.6 Spout executor grouping

#### Categories

- empty executor collection;
- one Spout executor;
- several executors for one Spout;
- several Spouts;
- mixture of Spout and non-Spout components.

Representative frames verify that only Spout executors are grouped and that
group membership preserves component identity.

### F8.7 Bolt page statistics

#### Categories

**Statistical windows**

- empty;
- one;
- multiple.

**Input streams**

- empty;
- non-empty.

**Output streams**

- empty;
- non-empty.

**Executors**

- empty;
- non-empty.

**Errors**

- absent;
- present.

#### Representative test frames

| ID | Windows | Inputs | Outputs | Executors | Errors | Expected semantic result |
|---|---|---|---|---|---|---|
| BOLT-01 | empty | empty | empty | empty | none | all Bolt collections empty |
| BOLT-02 | one | empty | empty | empty | none | aggregate Bolt statistics exposed |
| BOLT-03 | one | present | present | present | none | complete Bolt statistical view exposed |
| BOLT-04 | multiple | present | present | present | present | statistics and component errors both exposed |

Observable Bolt statistics include, where supplied:

- emitted;
- transferred;
- acknowledged;
- failed;
- execute latency;
- process latency;
- executed count;
- capacity.

### F8.8 Spout page statistics

#### Categories

**Statistical windows**

- empty;
- one;
- multiple.

**Output streams**

- empty;
- non-empty.

**Executors**

- empty;
- non-empty.

**Errors**

- absent;
- present.

#### Representative test frames

| ID | Windows | Outputs | Executors | Errors | Expected semantic result |
|---|---|---|---|---|---|
| SPOUT-01 | empty | empty | empty | none | all Spout collections empty |
| SPOUT-02 | one | empty | empty | none | aggregate Spout statistics exposed |
| SPOUT-03 | one | present | present | none | complete Spout statistical view exposed |
| SPOUT-04 | multiple | present | present | present | statistics and component errors both exposed |

Observable Spout statistics include, where supplied:

- emitted;
- transferred;
- acknowledged;
- failed;
- complete latency.

### F8.9 Component page

The functionality obtains and exposes the page representation for a selected
topology component.

#### Component type

- Bolt;
- Spout.

#### Component state

- no executors/tasks;
- positive executors/tasks.

#### Resource map

- requested CPU/memory only;
- requested CPU/memory plus generic resources.

#### Debug configuration

- absent;
- disabled;
- enabled.

#### Event-log information

- absent;
- present.

Representative frames:

| ID | Type | Executors/tasks | Debug | Event log | Expected semantic result |
|---|---|---|---|---|---|
| COMP-01 | Bolt | zero | absent | absent | common Bolt page fields plus Bolt-specific collections |
| COMP-02 | Bolt | positive | enabled | present | full Bolt page with debug/event metadata |
| COMP-03 | Spout | zero | absent | absent | common Spout page fields plus Spout-specific collections |
| COMP-04 | Spout | positive | disabled | present | full Spout page with event metadata |

For all component pages the externally visible result must preserve:

- component identity;
- topology identity and status;
- topology name;
- executor and task counts;
- requested resources;
- selected window and its display hint;
- component type;
- scheduler-display configuration;
- profiling/debugging metadata.


## F9 - Error representation

### F9.1 Exception-to-HTTP representation

The functionality converts an exception and an HTTP status code into an
externally visible error representation.

#### Categories

**Exception message**

- non-empty;
- empty;
- null.

**HTTP status**

- representative client-error status;
- representative server-error status.

#### Representative test frames

| ID | Exception message | Status category | Expected semantic result |
|---|---|---|---|
| ERRJSON-01 | non-empty | client error | supplied exception message exposed |
| ERRJSON-02 | non-empty | server error | supplied exception message exposed |
| ERRJSON-03 | empty | client error | exception type used as fallback message |
| ERRJSON-04 | null | server error | exception type used as fallback message |

The error field must identify the supplied HTTP status and its standard
status description.

### F9.2 Component error collection

#### Error population

- empty;
- one error;
- multiple errors.

#### Error attributes

- regular message;
- long message;
- different timestamps;
- different host/port locations.

Representative frames:

| ID | Errors | Expected semantic result |
|---|---|---|
| COMPERR-01 | none | empty component-error collection |
| COMPERR-02 | one | error details exposed |
| COMPERR-03 | multiple | all errors exposed in the externally defined ordering |
| COMPERR-04 | errors on different workers | each error retains its host/port and worker-log information |

Observable error information includes, where applicable:

- error text;
- error timestamp;
- host;
- port;
- elapsed time;
- worker-log link.

### F9.3 Last-error representation

#### Last-error state

- absent;
- present.

#### Error-message size

- short;
- longer than the UI last-error display limit.

Representative frames:

| ID | Last error | Message | Expected semantic result |
|---|---|---|---|
| LASTERR-01 | absent | - | empty/default last-error representation |
| LASTERR-02 | present | short | supplied error information exposed |
| LASTERR-03 | present | long | display-oriented last-error text is bounded while error metadata is preserved |

This last-error behavior is observed through public topology/component
representations rather than by directly invoking private helper methods.

### F9.4 Error-log endpoint mode

When an error is associated with a worker, its generated log link must remain
consistent with the configured logviewer mode.

Representative choices:

- HTTP logviewer;
- HTTPS logviewer.


## F10 - Topology visualization

### F10.1 Visualization data

The functionality exposes visualization-oriented information for the
components of a topology.

#### Categories

**Topology composition**

- no components;
- Spout only;
- Bolt only;
- Spout and Bolt components.

**Executor population**

- no executors;
- executors associated with the defined components.

**System-component mode**

- system components excluded;
- system components included.

**Requested statistics window**

- `:all-time`;
- regular numeric window.

#### Representative test frames

| ID | Components | Executors | System mode | Expected semantic result |
|---|---|---|---|---|
| VIS-01 | none | none | exclude | empty visualization data |
| VIS-02 | Spout only | present | exclude | Spout visualization entry exposed |
| VIS-03 | Bolt only | present | exclude | Bolt visualization entry exposed |
| VIS-04 | Spout + Bolt | present | exclude | both component types represented |
| VIS-05 | topology containing system component | present | exclude | system visibility follows exclusion mode |
| VIS-06 | topology containing system component | present | include | requested system component can be represented |

For each represented component, externally meaningful information includes,
where available:

- component type;
- component identity;
- relevant statistics for the requested window;
- component link;
- executor statistics;
- input-stream information.

### F10.2 Stream-box representation

A stream box represents one visualization input stream.

#### Categories

**Stream identifier**

- regular user stream;
- system stream;
- stream requiring sanitization.

Representative frames:

| ID | Stream | Expected semantic result |
|---|---|---|
| STREAMBOX-01 | regular stream | stream identity preserved and marked as selectable/checked |
| STREAMBOX-02 | system stream | system nature reflected by the checked state |
| STREAMBOX-03 | stream requiring sanitization | sanitized identifier preserved consistently |

The returned representation must contain:

- original stream identity;
- sanitized stream identity;
- checked/system-selection state.

### F10.3 Visualization table construction

The functionality converts visualization stream information into a table
layout.

#### Categories

**Number of stream boxes**

- zero;
- one;
- fewer than four;
- exactly four;
- five or more.

Representative frames:

| ID | Number of stream boxes | Expected semantic result |
|---|---:|---|
| VISTABLE-01 | 0 | empty visualization table |
| VISTABLE-02 | 1 | one row containing one stream box |
| VISTABLE-03 | 3 | one row containing three stream boxes |
| VISTABLE-04 | 4 | one complete row of four |
| VISTABLE-05 | 5 | first row of four and second row of one |

The observable contract is successful construction of a
`visualizationTable`; an unexpected runtime failure is treated as a defect
candidate rather than as an expected test outcome.


## F11 - Profiling

### F11.1 Active profiling action representation

#### Categories

**Worker identity**

- regular host and port.

**Action timestamp**

- recent;
- older.

**Logviewer mode**

- HTTP;
- HTTPS.

Representative frames:

| ID | Timestamp | Mode | Expected semantic result |
|---|---|---|---|
| PROF-ACT-01 | recent | HTTP | host, port, dump link and elapsed time exposed |
| PROF-ACT-02 | older | HTTP | larger non-negative elapsed time exposed |
| PROF-ACT-03 | recent | HTTPS | dump link uses secure logviewer |

Elapsed-time assertions must allow a small execution-time tolerance and must
not depend on an exact wall-clock millisecond value.

### F11.2 Collection of active profiling actions

#### Categories

**Pending action collection**

- empty;
- one action;
- multiple actions.

Representative frames:

| ID | Pending actions | Expected semantic result |
|---|---|---|
| PROF-LIST-01 | empty | empty active-action list |
| PROF-LIST-02 | one | one active-action representation |
| PROF-LIST-03 | multiple | all pending actions represented |

### F11.3 Profiling start

#### Categories

**Worker target**

- valid `host:port`.

**Timeout**

- zero minutes;
- positive minutes.

**Logviewer mode**

- HTTP;
- HTTPS.

Representative frames:

| ID | Timeout | Mode | Expected semantic result |
|---|---:|---|---|
| PROF-START-01 | 0 | HTTP | profiling request submitted and `status=ok` returned |
| PROF-START-02 | positive | HTTP | stop timestamp scheduled according to timeout |
| PROF-START-03 | positive | HTTPS | secure dump link returned |

The request sent to Nimbus must target the supplied topology and worker and
must represent the corresponding profiling action.

### F11.4 Profiling stop

Representative frame:

**PROF-STOP-01**

Expected observable behavior:

- a profiling request is sent for the selected topology/worker;
- stop action is requested immediately;
- returned status is `ok`;
- returned id equals the supplied `host:port`.

### F11.5 Profiling disabled response

Representative frame:

**PROF-DISABLED-01**

Expected result:

- `status = disabled`;
- an explanatory profiling-disabled message is exposed.

### F11.6 On-demand profiling actions

The supported externally visible actions include:

- Java profiler dump;
- JStack dump;
- heap/JMap dump;
- worker JVM restart.

Representative frames:

| ID | Operation | Expected Nimbus action | Expected response |
|---|---|---|---|
| PROF-DUMP-01 | profiler dump | JPROFILE_DUMP | status `ok`, target id preserved |
| PROF-JSTACK-01 | JStack | JSTACK_DUMP | status `ok`, target id preserved |
| PROF-HEAP-01 | heap dump | JMAP_DUMP | status `ok`, target id preserved |
| PROF-RESTART-01 | worker restart | JVM_RESTART | status `ok`, target id preserved |

### F11.7 Worker profiling request construction

#### Categories

**Profile action**

- JPROFILE_STOP;
- JPROFILE_DUMP;
- JSTACK_DUMP;
- JMAP_DUMP;
- JVM_RESTART.

**Timestamp**

- zero;
- current/recent timestamp;
- future timestamp.

For each representative action the request sent to Nimbus must preserve:

- topology identifier;
- worker host;
- worker port;
- requested profile action;
- supplied timestamp.

### F11.8 Malformed profiling target characterization

Robustness choices:

- missing port separator;
- non-numeric port;
- missing host.

These cases characterize externally observable error behavior and are not
treated as valid profiling requests.


## F12 - Topology operations

### F12.1 Generic topology-operation response

The standard operation response exposes:

- topology identifier;
- requested operation;
- success status.

Representative frames:

| ID | Operation | Expected result |
|---|---|---|
| OPRSP-01 | activate | topology id + `activate` + `success` |
| OPRSP-02 | deactivate | topology id + `deactivate` + `success` |
| OPRSP-03 | rebalance | topology id + `rebalance` + `success` |
| OPRSP-04 | kill | topology id + `kill` + `success` |
| OPRSP-05 | debug/enable | topology id + operation + `success` |

### F12.2 Topology activation

Representative frame:

**ACTIVATE-01**

Expected externally observable behavior:

1. the selected topology is resolved;
2. Nimbus receives an activation request for that topology;
3. the response reports successful `activate`.

### F12.3 Topology deactivation

Representative frame:

**DEACTIVATE-01**

Expected externally observable behavior:

1. the selected topology is resolved;
2. Nimbus receives a deactivation request;
3. the response reports successful `deactivate`.

### F12.4 Topology debug action

#### Categories

**Action**

- enable;
- disable.

**Sampling percentage**

- zero;
- positive representative value;
- upper representative value.

**Component**

- named component;
- topology-wide/null component where accepted by the service contract.

Representative frames:

| ID | Action | Sampling | Expected semantic result |
|---|---|---:|---|
| DEBUG-01 | enable | 10 | Nimbus debugging enabled at requested sampling percentage |
| DEBUG-02 | disable | 10 | Nimbus debugging disabled |
| DEBUG-03 | enable | 0 | zero sampling value preserved |
| DEBUG-04 | enable | 100 | upper representative percentage preserved |

The operation response identifies `debug/<action>`.

### F12.5 Topology rebalance

#### Wait-time category

- zero;
- positive.

Representative frames:

| ID | Wait time | Expected semantic result |
|---|---:|---|
| REBALANCE-01 | 0 | rebalance requested with zero wait |
| REBALANCE-02 | positive | supplied wait seconds preserved |

The response reports successful `rebalance`.

### F12.6 Topology kill

#### Wait-time category

- zero;
- positive.

Representative frames:

| ID | Wait time | Expected semantic result |
|---|---:|---|
| KILL-01 | 0 | kill requested with zero wait |
| KILL-02 | positive | supplied wait seconds preserved |

The response reports successful `kill`.

### F12.7 Invalid textual numeric parameters

Robustness choices:

- non-numeric debug sampling percentage;
- non-numeric rebalance wait time;
- non-numeric kill wait time.

These frames characterize error behavior and are not valid service
operations.

### F12.8 Nimbus-operation failure propagation

For representative topology operations, a Nimbus/Thrift failure is treated as
an unsuccessful execution and must not be mistaken for a normal successful
operation response.

This frame is used as robustness characterization of the public API boundary.

## F13 - Nimbus and logging

### F13.1 Topology logger configuration representation

#### Categories

**Named logger collection**

- not set;
- empty;
- one logger;
- multiple loggers.

**Logger configuration**

- target level present;
- reset information present;
- timeout values present.

Representative frames:

| ID | Logger state | Expected semantic result |
|---|---|---|
| LOGCFG-01 | not set | empty named logger-level map |
| LOGCFG-02 | empty | empty named logger-level map |
| LOGCFG-03 | one logger | logger configuration exposed |
| LOGCFG-04 | multiple loggers | every named logger represented |

For each logger, where supplied, the representation preserves:

- target level;
- reset level;
- reset timeout;
- timeout epoch.

### F13.2 Update topology log level

#### Categories

**Requested action**

- update level;
- remove level.

**Logger population**

- one logger;
- multiple loggers.

**Timeout**

- zero;
- positive.

Representative frames:

| ID | Request | Loggers | Expected semantic result |
|---|---|---|---|
| LOGPUT-01 | update | one | Nimbus receives update for named logger |
| LOGPUT-02 | remove | one | Nimbus receives removal for named logger |
| LOGPUT-03 | update | multiple | requested logger updates are submitted |
| LOGPUT-04 | update | one with positive timeout | timeout preserved |

After applying the request, the returned representation corresponds to the
log configuration reported by Nimbus.

### F13.3 Nimbus summary population

#### Categories

**Active Nimbus collection**

- empty;
- one;
- multiple.

**Leadership**

- leader;
- non-leader.

**Configured Nimbus seeds**

- all represented by active Nimbus instances;
- contains an absent/offline seed;
- contains localhost-style seed;
- multiple seeds.

**Logviewer mode**

- HTTP;
- HTTPS.

Representative frames:

| ID | Active Nimbus | Seeds | Expected semantic result |
|---|---|---|---|
| NIMBUS-01 | none | one remote seed | seed represented as Offline |
| NIMBUS-02 | one leader | matching seed | Nimbus represented as Leader |
| NIMBUS-03 | one non-leader | matching seed | Nimbus represented as Not a Leader |
| NIMBUS-04 | one active | one additional remote seed | active Nimbus plus Offline seed |
| NIMBUS-05 | multiple | matching seeds | all active Nimbus instances represented |
| NIMBUS-06 | active | localhost-style seed also configured | localhost seed is not reported as an offline remote Nimbus |
| NIMBUS-07 | one active | matching seed | HTTPS logviewer link uses secure endpoint |

### F13.4 Active Nimbus details

For each active Nimbus instance the externally visible representation
preserves:

- host;
- port;
- leader status;
- Storm version;
- uptime in seconds;
- formatted uptime;
- Nimbus log link.

### F13.5 Offline Nimbus details

For a configured remote seed that is not present among active Nimbus
instances, the representation identifies:

- host;
- configured port;
- `Offline` status;
- version and uptime as not applicable;
- a Nimbus log link using the configured logviewer mode.


