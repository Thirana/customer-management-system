#!/usr/bin/env bash

set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CALLER_DIR="$(pwd)"

OUTPUT_PATH=""
ROW_COUNT="1000"
MODE="mixed"
INCLUDE_INVALID_ROW="false"

for ARG in "$@"; do
  case "$ARG" in
    --output=*)
      OUTPUT_PATH="${ARG#--output=}"
      ;;
    --rows=*)
      ROW_COUNT="${ARG#--rows=}"
      ;;
    --mode=*)
      MODE="${ARG#--mode=}"
      ;;
    --include-invalid-row=*)
      INCLUDE_INVALID_ROW="${ARG#--include-invalid-row=}"
      ;;
    *)
      echo "Unsupported argument: $ARG" >&2
      echo "Usage: $0 --output=<path> [--rows=<count>] [--mode=create-only|auto|mixed] [--include-invalid-row=true|false]" >&2
      exit 1
      ;;
  esac
done

if [[ -z "$OUTPUT_PATH" ]]; then
  echo "Missing required argument: --output=<path>" >&2
  exit 1
fi

if [[ "$OUTPUT_PATH" != /* ]]; then
  OUTPUT_PATH="${CALLER_DIR}/${OUTPUT_PATH}"
fi

python3 "$ROOT_DIR/scripts/generate_import_workbook.py" \
  --output="${OUTPUT_PATH}" \
  --rows="${ROW_COUNT}" \
  --mode="${MODE}" \
  --include-invalid-row="${INCLUDE_INVALID_ROW}"
