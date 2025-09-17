#!/bin/bash
while true; do
    echo "$(date): CPU: $(top -l 1 | grep "CPU usage" | awk '{print $3}'), Memory: $(top -l 1 | grep "PhysMem" | awk '{print $2}')" >> system_metrics.log
    sleep 10
done
