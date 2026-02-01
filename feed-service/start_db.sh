#!/usr/bin/env bash
# ...existing code...
# Start Postgres (docker compose) and wait until healthy
set -euo pipefail

# Load .env if present (so DB_PORT etc are available for printing)
if [ -f .env ]; then
  # shellcheck disable=SC1091
  source .env
fi

DOCKER_COMPOSE_CMD="docker compose"

echo "Starting Postgres service via docker compose..."
$DOCKER_COMPOSE_CMD up -d db

echo "Waiting for DB container to become healthy (up to 60s)..."
for i in {1..60}; do
  status=$($DOCKER_COMPOSE_CMD ps -q db | xargs -r docker inspect --format='{{.State.Health.Status}}' 2>/dev/null || echo "")
  if [ "$status" = "healthy" ]; then
    echo "DB is healthy"
    break
  fi
  if [ -z "$status" ]; then
    echo -n "."
  else
    echo -n "."
  fi
  sleep 1
done

echo
$DOCKER_COMPOSE_CMD ps db

echo "Recent DB logs (last 200 lines):"
$DOCKER_COMPOSE_CMD logs --tail=200 db || true

echo "Done. To stop the DB use './stop_db.sh'."
