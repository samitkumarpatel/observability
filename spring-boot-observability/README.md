# spring-boot-observability

```shell
docker run --rm --name spring-app \
  -v $(pwd):/var -p 8080:8080 \
  -w /var \
  --network observability_lgtm \
  ghcr.io/graalvm/jdk-community:21 java -jar target/*.jar
```