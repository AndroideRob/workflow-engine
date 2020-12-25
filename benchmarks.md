# Performance benchmarks
Performance benchmarks for all supported databases.
Uses the `app` module with its predefined Workflows.

### Scenario 1: High throughput, lightweight processing
Creating 1000 workflows atomically with 2 activities: 
- `PerformanceInputActivity`
- `PerformanceTerminalActivity`

Measure the time between completion of the first and the last workflows.
Divide by the number of workflows (1000) to get throughput.

Engine configuration: 4 workers

#### Results
##### PostgreSQL
TODO
##### MySQL
TODO
