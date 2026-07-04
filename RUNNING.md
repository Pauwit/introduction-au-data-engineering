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

### `iot-simulator` (component 1)

Publishes simulated drone readings to `drone-events`.

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

### `alert-detection` (component 2)

Spark Structured Streaming job: reads `drone-events`, detects anomalies within a 1-minute tumbling window, writes to `alerts`.

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

Because of the 1-minute window plus a 1-minute watermark, expect roughly 2 minutes of data flowing through `DRONE_EVENTS_TOPIC` before the first alerts appear on the `ALERTS_TOPIC`.

### `alert-service` (component 3)

Consumes `ALERTS_TOPIC`, enriches with owner/contact from the seeded contacts table, dispatches to console and to a WebSocket at `/alerts`; exposes `/health`.

```bash
cd alert-service
sbt run
```

| Env var | Default |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `ALERTS_TOPIC` | `alerts` |
| `CONTACTS_RESOURCE` | `contacts-seed.json` |
| `CONTACTS_REFRESH_SECONDS` | `300` |
| `GEOHASH_PRECISION` | `5` |
| `HTTP_HOST` | `0.0.0.0` |
| `HTTP_PORT` | `8080` |

Check it's up with `curl http://localhost:8080/health` (expects `ok`).

### `lake-ingestion` (component 4)

Consumes `drone-events`, writes raw JSON to the bronze layer of a local data lake, partitioned by date/hour.

```bash
cd lake-ingestion
DATA_LAKE_ROOT=/absolute/path/to/data-lake sbt run
```

| Env var | Default |
|---|---|
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` |
| `DRONE_EVENTS_TOPIC` | `drone-events` |
| `DATA_LAKE_ROOT` | `data-lake` (relative to the current directory) |
| `BATCH_SIZE` | `50` |
| `BATCH_INTERVAL_SECONDS` | `5` |

`DATA_LAKE_ROOT` must be the same absolute path used for `analytics` below, since one writes the data lake and the other reads it.

### `analytics` (component 5)

One-shot Spark batch job: reads the bronze layer, writes a cleaned/deduped silver layer, computes 4 gold aggregations, and prints a summary. Run this after `lake-ingestion` has had time to write some data.

```bash
cd analytics
DATA_LAKE_ROOT=/absolute/path/to/data-lake sbt run
```

| Env var | Default |
|---|---|
| `DATA_LAKE_ROOT` | `data-lake` |
| `SPARK_MASTER` | `local[*]` |

This exits on its own once done (it is not a long-running service).

## Suggested order

1. Start Kafka, create the topics.
2. Start `iot-simulator`.
3. Start `lake-ingestion`, `alert-detection`, `alert-service` (any order, each in its own terminal).
4. Let it run for a couple of minutes so alerts and bronze data accumulate.
5. Run `analytics` once to see the gold results.
