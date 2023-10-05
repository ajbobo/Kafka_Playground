kafka-consumer-groups `
    --bootstrap-server localhost:9092 `
    --group KStreams-test `
    --describe

kafka-consumer-groups `
    --bootstrap-server localhost:9092 `
    --group KStreams-test `
    --reset-offsets `
    --execute `
    --to-earliest `
    --all-topics
