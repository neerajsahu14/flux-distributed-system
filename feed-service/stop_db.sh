#!/usr/bin/env bash
# Stop and remove Postgres and its volumes
set -euo pipefail

echo "Stopping Postgres and removing containers..."
docker compose down -v

echo "Done. Volumes removed. To keep data, use 'docker compose stop' instead."
