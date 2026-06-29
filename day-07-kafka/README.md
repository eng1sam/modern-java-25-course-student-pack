# Day 07 — Drive Kafka from the CLI

**Goal:** operate Kafka entirely from its CLI, run from a shell **inside the broker's container**
(the same move as `psql` on Day 6). Create a partitioned topic, produce keyed messages, consume
and replay them, read a consumer group's offsets and lag, and trigger a rebalance. No app code.

> No starter. You practise against the **real broker**. Start it with
> `docker compose -f infra/compose.yaml up -d kafka kafka-ui`.

## The command pattern

The CLI scripts aren't on `PATH`; call them by full path inside the `kafka` container, addressing
the broker as `localhost:9092`:

```bash
docker compose -f infra/compose.yaml exec kafka \
  /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 --list
```

## Do this

1. Start the broker (`docker compose -f infra/compose.yaml up -d kafka kafka-ui`), then confirm
   the `--list` command above answers (empty = success).
2. Create `order-created` with `--partitions 3 --replication-factor 1`; `--describe` it and
   confirm partitions 0, 1, 2.
3. Produce keyed messages with `kafka-console-producer.sh --property parse.key=true
   --property key.separator=:`. Send key `1001` twice.
4. Consume `--from-beginning` with `--property print.partition=true --property print.key=true`.
   Both `1001` messages share a partition. Run it **again** — every message reappears (a log,
   not a queue).
5. Consume with `--group notifications`, stop, then `kafka-consumer-groups.sh --describe
   --group notifications` — read `CURRENT-OFFSET`, `LOG-END-OFFSET`, `LAG`. Produce more, watch
   `LAG` rise, restart the consumer, watch it drain to 0.
6. Start two consumers in the **same** group `workers`, produce, confirm each message goes to one
   consumer; kill one and watch the other take over all partitions (a rebalance).
7. Cross-check in Kafka UI (<http://localhost:8081>).

## Done when

- [ ] `order-created` `--describe` shows 3 partitions.
- [ ] Both `1001` messages land in the **same** partition.
- [ ] A replay proves messages aren't removed by reading.
- [ ] A consumer group `LAG` you watched drain to 0.
- [ ] A rebalance you caused by adding then removing a consumer.

## Going further

Reset offsets `--to-earliest` · a short-`retention.ms` topic · a `cleanup.policy=compact` topic.

> The `order-created` topic you drive by hand here is the one the **Day 10** code produces to.

Full brief: `labs/day-07/README.md` · Concepts: `docs/content/day-07/`.
