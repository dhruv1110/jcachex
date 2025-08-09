#!/usr/bin/env bash
set -euo pipefail

PORT="${1:-8085}"
RUNS="${2:-6}"
ROOT_DIR="$(cd "$(dirname "$0")"/../../.. && pwd)"
LOG_FILE="$ROOT_DIR/example/springboot/build/smoke-run.log"

echo "Starting Spring example on port $PORT (runs per endpoint: $RUNS) ..."

pushd "$ROOT_DIR" >/dev/null

# Start app in background (use --no-daemon for isolation)
./gradlew --no-daemon :example:springboot:bootRun --args="--server.port=$PORT" -x javadoc -x dokkaJavadoc -x dokkaHtml >"$LOG_FILE" 2>&1 &
APP_PID=$!

cleanup() {
  echo "Stopping example (pid=$APP_PID) ..."
  kill "$APP_PID" >/dev/null 2>&1 || true
}
trap cleanup EXIT

# Wait for readiness
echo -n "Waiting for app to be ready"
for i in {1..60}; do
  if curl -sf "http://localhost:$PORT/actuator/health" >/dev/null 2>&1 || curl -sf "http://localhost:$PORT/actuator" >/dev/null 2>&1; then
    echo " - ready"
    break
  fi
  echo -n "."
  sleep 1
  if ! kill -0 "$APP_PID" >/dev/null 2>&1; then
    echo "\nApp process died. See log: $LOG_FILE"
    exit 1
  fi
done

BASE="http://localhost:$PORT"

time_one() {
  local url="$1"
  curl -s -o /dev/null -w "%{time_total}" "$url"
}

measure_endpoint() {
  local url="$1"; shift
  local title="$1"; shift
  local runs="${1:-$RUNS}"

  echo "\n=== $title ($runs calls) ==="
  declare -a times=()
  for i in $(seq 1 "$runs"); do
    t=$(time_one "$url")
    times+=("$t")
    printf "call %d:%26s %8.3fs\n" "$i" "$url" "$t"
  done

  first="${times[0]}"
  # build hot list (2..runs)
  hot_list=""
  if [ "$runs" -gt 1 ]; then
    for i in $(seq 2 "$runs"); do
      hot_list+="${times[$((i-1))]}\n"
    done
  fi
  hot_avg=$(echo -e "$hot_list" | awk '{sum+=$1} END { if (NR>0) printf "%.6f", sum/NR; else print "0" }')
  speedup=$(awk -v f="$first" -v a="$hot_avg" 'BEGIN { if (a>0) printf "%.2f", f/a; else print "inf" }')

  # Print a safe summary line (avoid leading dash to keep printf portable)
  printf "%s\n" "first: $(printf '%0.3fs' "$first"), hot avg: $(printf '%0.3fs' "$hot_avg"), speedup: ${speedup}x"

  verdict=$(awk -v f="$first" -v a="$hot_avg" 'BEGIN { if (a>0 && f/a >= 5) print "OK (strong caching)"; else if (a>0 && f/a >= 2) print "OK (moderate caching)"; else print "WARN (weak caching)" }')
  echo "verdict: $verdict"
}

measure_endpoint "$BASE/users/demo" "Properties-based cache (@Cacheable -> users)" "$RUNS"

measure_endpoint "$BASE/users/demo/profile" "JCacheX annotation (@JCacheXCacheable -> apiResponses)" "$RUNS"

measure_endpoint "$BASE/users/demo/bean" "Bean-defined cache (userCache bean)" "$RUNS"

echo "\n=== Diagnostics ==="
curl -s "$BASE/cache/stats" | sed 's/{/{\n  /;s/,/,\n  /g;s/}/\n}/'

echo "\nSmoke test complete. App log: $LOG_FILE"

popd >/dev/null


