# Running the project

## Prerequisites

- JDK 17
- sbt 1.9+
- A local Kafka broker (3.x or 4.x), running in KRaft mode

Each of the 5 components (`iot-simulator/`, `alert-detection/`, `alert-service/`, `lake-ingestion/`, `analytics/`) is an independent sbt project with its own `build.sbt`.

## 1. Start a local Kafka broker

Download a Kafka release (e.g. from `https://archive.apache.org/dist/kafka/`), extract it, then from the extracted directory:

```bash
# one-time setup
CLUSTER_ID=$(bin/kafka-storage.sh random-uuid)
bin/kafka-storage.sh format -t "$CLUSTER_ID" -c config/server.properties --standalone

# start the broker (foreground; use another terminal, or run in the background)
bin/kafka-server-start.sh config/server.properties
```

## 2. Create the topics

```bash
bin/kafka-topics.sh --create --topic drone-events --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
bin/kafka-topics.sh --create --topic alerts --bootstrap-server localhost:9092 --partitions 3 --replication-factor 1
```

## 3. Run the components

Every component reads its configuration from environment variables, all with defaults. From each component's own directory:

```bash
sbt run
```

`run` is forked in every `build.sbt`, so this stays running as a real service instead of exiting immediately. Press `Ctrl+C` to stop it.

### Component 1: `iot-simulator`

Publishes simulated drone readings to `DRONE_EVENTS_TOPIC`.

```bash
cd iot-simulator
DEVICE_COUNT=30 TICK_INTERVAL_SECONDS=2 sbt run
```

| Env var | Default |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `DRONE_EVENTS_TOPIC` | `drone-events` |
| `DEVICE_COUNT` | `5` |
| `TICK_INTERVAL_SECONDS` | `30` |

### Component 2: `alert-detection`

Spark Structured Streaming job: reads `DRONE_EVENTS_TOPIC`, detects anomalies within a tumbling window, writes to `ALERTS_TOPIC`.

```bash
cd alert-detection
sbt run
```

| Env var | Default |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `DRONE_EVENTS_TOPIC` | `drone-events` |
| `ALERTS_TOPIC` | `alerts` |
| `CHECKPOINT_LOCATION` | `checkpoints/alert-detection` |
| `SPARK_MASTER` | `local[*]` |
| `WINDOW_DURATION_SECONDS` | `60` |

The watermark matches the window duration. Because of that, expect roughly two window durations of data flowing through `DRONE_EVENTS_TOPIC` before the first alerts appear on the `ALERTS_TOPIC`; lowering `WINDOW_DURATION_SECONDS` shortens that wait.

### Component 3: `alert-service`

Consumes `ALERTS_TOPIC`, enriches each alert with owner/contact from the contacts table in the data lake (`$DATA_LAKE_ROOT/contacts/contacts-seed.json`, reloaded every `CONTACTS_REFRESH_SECONDS` without a restart), and dispatches to console and to a WebSocket at `/alerts`. It also exposes `/health`, serves the `dashboard.html` monitoring page at `/`, and a read-only `/gold` endpoint that returns the batch `gold-summary.json` for the dashboard.

```bash
cd alert-service
sbt run
```

| Env var | Default |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `ALERTS_TOPIC` | `alerts` |
| `DATA_LAKE_ROOT` | `../data-lake` |
| `CONTACTS_RESOURCE` | `contacts/contacts-seed.json` (path within `DATA_LAKE_ROOT`) |
| `CONTACTS_REFRESH_SECONDS` | `300` |
| `GEOHASH_PRECISION` | `5` |
| `HTTP_HOST` | `0.0.0.0` |
| `HTTP_PORT` | `8080` |

Check it's up with `curl http://localhost:8080/health` (expects `ok`), and open `http://localhost:8080/` for the live dashboard.

For owner/contact enrichment and the dashboard's gold panel to work, point `DATA_LAKE_ROOT` at the same lake as `lake-ingestion`/`analytics`. The default `../data-lake` already does this when each component is run from its own directory.

### Component 4: `lake-ingestion`

Consumes `DRONE_EVENTS_TOPIC`, writes raw JSON to the bronze layer of a local data lake, partitioned by date/hour.

```bash
cd lake-ingestion
DATA_LAKE_ROOT=/absolute/path/to/data-lake sbt run
```

| Env var | Default |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `DRONE_EVENTS_TOPIC` | `drone-events` |
| `DATA_LAKE_ROOT` | `../data-lake` (relative to the current directory) |
| `BATCH_SIZE` | `50` |
| `BATCH_INTERVAL_SECONDS` | `5` |

`DATA_LAKE_ROOT` must point at the same lake as `analytics` (and `alert-service`), since one writes it and the others read it. The default `../data-lake` already resolves to the repo-root lake for all of them when each component is run from its own directory; override it with a shared absolute path for other layouts.

### Component 5: `analytics`

One-shot Spark batch job: reads the bronze layer, writes a cleaned/deduped silver layer, computes 4 gold aggregations, and prints a summary. Run this after `lake-ingestion` has had time to write some data.

```bash
cd analytics
DATA_LAKE_ROOT=/absolute/path/to/data-lake sbt run
```

| Env var | Default |
|---|---|
| `DATA_LAKE_ROOT` | `../data-lake` |
| `SPARK_MASTER` | `local[*]` |

This exits on its own once done (it is not a long-running service).

## Suggested order

1. Start Kafka, create the topics.
2. Start `iot-simulator`.
3. Start `lake-ingestion`, `alert-detection`, `alert-service` (any order, each in its own terminal).
4. Let it run for a couple of minutes so alerts and bronze data accumulate.
5. Open `http://localhost:8080/` to watch the live alert dashboard.
6. Run `analytics` once to see the gold results (the dashboard's gold panel refreshes from `/gold`).
