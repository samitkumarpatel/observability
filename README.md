# observability

Observability with `lgmt` stack with alloy agent:

- docker
- [k8s](https://github.com/samitkumarpatel/k8s-local-infra)


## Grafana
- [api reference](https://grafana.com/docs/grafana/latest/developers/http_api/dashboard/)

**:: api Example ::**

```sh
# Generate a base64 basic auth (make sure you know the username & password)
BASICAUTH=$(echo -n "admin:admin" | base64)

# Create a service account
curl -X POST -H "Authorization: Basic $BASICAUTH" -H "Content-Type: application/json" http://localhost:3000/api/serviceaccounts -d '{"name": "mcp-server","role": "Writer","isDisabled": false}'

# Create a token for particular service account
curl -X POST -H "Authorization: Basic $BASICAUTH" -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:3000/api/serviceaccounts/3/tokens -d '{"name":"cursor-mcp-client", "secondsToLive": 0}'

#Output {"id":1,"name":"cursor-mcp-client","key":"xxxxxxxxxxxx"}

SERVICE_ACCOUNT_TOKEN=key_from_above_output

# Use the service account token to look for some other Grafana Information
curl -H "Authorization: Basic $SERVICE_ACCOUNT_TOKEN" -H "Content-Type: application/json" -H "Accept: application/json" http://localhost:3000/api/datasources
```

## Loki
- [api reference](https://grafana.com/docs/loki/latest/reference/loki-http-api/#query-logs-at-a-single-point-in-time)



## Mimir
- [api reference](https://grafana.com/docs/mimir/latest/references/http-api/)
