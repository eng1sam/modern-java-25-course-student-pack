# apps/

The reference order-processing platform — built to its **final** state, then "rewound" into
per-day checkpoints via Git tags (`checkpoint/day-08` … `checkpoint/day-10`; the app first
appears at day 8, so `checkpoint/day-01` … `day-07` are app-free).

See `apps/CLAUDE.md` for the conventions and the root `CLAUDE.md` for the checkpoint discipline.

Layout:

```
apps/
├─ kyc-service/            # Quarkus REST + Panache + Flyway + SmallRye Messaging (applicant onboarding)
└─ screening-service/      # Quarkus consumer — runs background checks on registered applicants
```
