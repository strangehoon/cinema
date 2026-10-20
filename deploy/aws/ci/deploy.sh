#!/bin/bash
set -euo pipefail

sha="$1"
image_base="$2"
repository="$3"
[[ "$sha" =~ ^[0-9a-f]{40}$ ]] || { echo "Invalid commit SHA" >&2; exit 1; }
[[ "$image_base" =~ ^[a-zA-Z0-9._/-]+$ ]] || { echo "Invalid Docker image name" >&2; exit 1; }
[[ "$repository" =~ ^[a-zA-Z0-9_.-]+/[a-zA-Z0-9_.-]+$ ]] || { echo "Invalid GitHub repository" >&2; exit 1; }

declare -A instances
for role in mysql redis monitoring app; do
  id="$(aws ec2 describe-instances \
    --filters "Name=tag:Project,Values=Cinema" "Name=tag:Role,Values=$role" "Name=instance-state-name,Values=running,stopped" \
    --query 'Reservations[].Instances[].InstanceId' --output text)"
  [[ "$id" =~ ^i-[a-f0-9]+$ ]] || { echo "Expected exactly one $role instance, got: $id" >&2; exit 1; }
  instances[$role]="$id"
done

for role in mysql redis monitoring app; do
  id="${instances[$role]}"
  state="$(aws ec2 describe-instances --instance-ids "$id" \
    --query 'Reservations[0].Instances[0].State.Name' --output text)"
  if [[ "$state" == stopped ]]; then aws ec2 start-instances --instance-ids "$id" >/dev/null; fi
done
aws ec2 wait instance-running --instance-ids "${instances[@]}"

for role in mysql redis monitoring app; do
  id="${instances[$role]}"
  for attempt in $(seq 1 30); do
    ping="$(aws ssm describe-instance-information --filters "Key=InstanceIds,Values=$id" \
      --query 'InstanceInformationList[0].PingStatus' --output text)"
    if [[ "$ping" == Online ]]; then break; fi
    sleep 10
  done
  [[ "$ping" == Online ]] || { echo "SSM unavailable on $role" >&2; exit 1; }
done

for role in mysql redis monitoring app; do
  id="${instances[$role]}"
  command="set -e; export AWS_DEFAULT_REGION=ap-northeast-2; for attempt in \$(seq 1 120); do docker compose version >/dev/null 2>&1 && break; sleep 5; done; docker compose version; mkdir -p /opt/cinema/release; curl -fLSs --retry 3 --retry-delay 2 https://github.com/$repository/archive/$sha.tar.gz -o /tmp/cinema-source-$sha.tar.gz; mkdir -p /tmp/cinema-source-$sha; tar -xzf /tmp/cinema-source-$sha.tar.gz -C /tmp/cinema-source-$sha --strip-components=1; cp -a /tmp/cinema-source-$sha/deploy/aws/. /opt/cinema/release/; bash /opt/cinema/release/install.sh $role $sha $image_base"
  jq -n --arg command "$command" '{commands:[$command]}' >ssm-parameters.json
  command_id="$(aws ssm send-command --instance-ids "$id" --document-name AWS-RunShellScript \
    --parameters file://ssm-parameters.json --query 'Command.CommandId' --output text)"
  status=Pending
  for attempt in $(seq 1 90); do
    status="$(aws ssm get-command-invocation --command-id "$command_id" --instance-id "$id" \
      --query Status --output text 2>/dev/null || echo Pending)"
    case "$status" in
      Success|Failed|Cancelled|TimedOut) break ;;
    esac
    sleep 10
  done
  if [[ "$status" != Success ]]; then
    aws ssm get-command-invocation --command-id "$command_id" --instance-id "$id" \
      --query '[Status,StandardOutputContent,StandardErrorContent]' --output text
    exit 1
  fi
  aws ssm get-command-invocation --command-id "$command_id" --instance-id "$id" \
    --query '[Status,StandardOutputContent]' --output text
done
