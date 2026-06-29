# Data Bytes Java 25 — Student Pack

Everything you need to follow the two-week course: **one Maven module per day** and the
**slide decks as PDF**. Open the whole folder in your IDE as a single Maven project — every
day shows up as a module, including the days that have no Java.

> **Self-contained.** Everything the hands-on days need is in this repo: the Compose stack
> (`infra/compose.yaml`), and the reference application at each day's `checkpoint/day-NN` Git
> tag (`git checkout checkpoint/day-09`). You do **not** need to clone anything else to do the
> labs. The full course repository,
> [`githubmo/modern-java-25-course`](https://github.com/githubmo/modern-java-25-course), is the
> upstream home of the Slidev slide sources and the deeper `docs/` write-ups, but it is optional.

## What's in here

```
modern-java-25-course-student-pack/
├─ pom.xml                     # aggregator — imports all 10 day modules
├─ infra/                      # the Compose stack (Postgres + Kafka + observability)
│  └─ compose.yaml
├─ slides/                     # day-01.pdf … day-10.pdf  (generated in the course repo)
├─ skeletons/                  # compiling hello-world Quarkus apps — safe start for days 08–10
│  ├─ kyc-service/
│  └─ screening-service/
├─ apps/                       # appears when you `git checkout checkpoint/day-NN` (days 08–10)
└─ day-NN-topic/
   ├─ pom.xml                  # the module (pom-packaging; days are materials, not a build)
   ├─ README.md                # the day's brief: goal, run commands, acceptance checklist
   ├─ starter/                 # only on the coding days (03, 04, 05) — single-file programs
   └─ solution/                # worked answers for the coding days (03, 04, 05)
```

> **Building days (08–10):** you normally `quarkus create app` from scratch, but if scaffolding
> fails or you fall behind, `skeletons/` has two known-good, already-compiling starting points
> with a green test. See `skeletons/README.md`.

| Day | Module | Topic | You run… |
|-----|--------|-------|----------|
| 01 | `day-01-git` | Git & the development workflow | `git` on this repo's history |
| 02 | `day-02-docker` | Docker & Compose | `docker compose -f infra/compose.yaml …` |
| 03 | `day-03-java-rosetta` | Java for polyglots | `java starter/RosettaStone.java` |
| 04 | `day-04-concurrency-i` | Cheap threads & a visible race | `java starter/RaceLab.java` |
| 05 | `day-05-concurrency-ii` | Safe & structured concurrency | `java --enable-preview --source 25 starter/RaceLab.java` |
| 06 | `day-06-postgresql` | PostgreSQL & `psql` | `psql` inside the Postgres container |
| 07 | `day-07-kafka` | Kafka from the CLI | the Kafka CLI inside the broker container |
| 08 | `day-08-quarkus-fundamentals` | Quarkus fundamentals | `quarkus create app` + `quarkus dev` |
| 09 | `day-09-quarkus-persistence` | Quarkus & persistence | the KYC Service from `checkpoint/day-08` |
| 10 | `day-10-messaging-capstone` | Messaging & capstone | the platform from `checkpoint/day-09` |

## Why some modules have no Java

Days 1, 2, 6 and 7 are about tools — Git, Docker, `psql`, the Kafka CLI — so their modules
carry the brief and assets, not source. Days 8–10 are the Quarkus reference app, which you
**generate** (day 8) or **check out at a Git tag** (days 9–10) rather than copy. Every day is
still a module so the structure is uniform and the whole course opens as one project.

## Prerequisites

This pack pins the toolchain with [`mise`](https://mise.jdx.dev) (see `mise.toml`): **JDK 25**,
plus Node and Python. Docker (Compose V2), the **`quarkus`** CLI, and PostgreSQL client tools
are also expected. From the repo root, `mise install` gets you the pinned runtimes. The exercises use the **real commands**
(`docker compose …`, `./mvnw …`, `quarkus …`, `psql`, the Kafka CLI) — no `task` wrapper.

## Slides (PDF)

The decks are exported from the Slidev sources in the [course repo](https://github.com/githubmo/modern-java-25-course)'s
`slides/`. To (re)generate them into this pack's `slides/` and build a distributable zip, run
this **from the course repo**:

```bash
task pack
```

That writes `slides/day-01.pdf … day-10.pdf` and `dist/databytes-java25-student-pack.zip`.

## Where the solutions live

- **Coding days (03–05):** a worked answer sits in each module's `solution/` folder.
- **App-building days (08–10):** the finished reference app is the Git tag `checkpoint/day-NN`:

  ```bash
  git checkout checkpoint/day-09      # the KYC Service with persistence, fully working
  ```
- **Tool days (01, 02, 06, 07):** no code to "solve" — the brief's commands and acceptance
  checklist are the answer key.

Checking out `checkpoint/day-NN` materialises the running reference implementation under
`apps/` (kyc-service + screening-service) at that day's state. The deeper concept
write-ups live in the [course repo](https://github.com/githubmo/modern-java-25-course)'s
`docs/content/day-NN/`.
