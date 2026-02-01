#!/usr/bin/env bash
# ...existing code...
# Stop and optionally remove Postgres containers and volumes
set -euo pipefail

# Load .env if present
if [ -f .env ]; then
  # shellcheck disable=SC1091
  source .env
fi

DOCKER_COMPOSE_CMD="docker compose"

usage(){
  cat <<EOF
Usage: $0 [--remove-volumes]

Options:
  --remove-volumes   Stop containers and remove associated volumes (will delete DB data)
  --help             Show this help
EOF
}

REMOVE_VOLUMES=false

if [ "${1:-}" = "--remove-volumes" ]; then
  REMOVE_VOLUMES=true
fi

echo "Stopping Postgres containers..."
$DOCKER_COMPOSE_CMD stop db || true

if [ "$REMOVE_VOLUMES" = true ]; then
  echo "Removing containers and volumes..."
  $DOCKER_COMPOSE_CMD down -v
  echo "Containers and volumes removed."
else
  echo "Removing containers (keep volumes)..."
  $DOCKER_COMPOSE_CMD rm -f db || true
  echo "Containers removed; volumes retained. Use --remove-volumes to delete volumes."
fi
