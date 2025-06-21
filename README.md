# Setup

- Setup Configuration through application.yml
    - Setup DB
  ```yaml
    spring:
      datasource:
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
        url: ${DB_URL}
  ```
    - Setup Thread Count
  ```yaml
    env.app:
      consumerThread: ${THREAD_COUNT}
  ```

    - Setup Report Location
  ```yaml
    env.app:
      location: ${REPORT_LOCATION}
  ```

# Run

- Run the application
  ```bash
  mvn spring-boot:run 
  ```

- Trigger the Crawl
  ```bash
  # Single Env Crawl
  curl -X POST {{SERVER_URL}}/process \
    -H "Content-Type: application/json" \
    -d '{
    "baseUrl": "{{BASE_URL}}"
    }'
  ```
  ```bash
  # Compare Two Env Crawl
    curl -X POST {{SERVER_URL}}/process \
      -H "Content-Type: application/json" \
      -d '{
      "baseUrl": "{{COMPARE_FROM_BASE_URL}}",
      "compareTo": "{{COMPARE_TO_BASE_URL}}"
      }'  
  ```
 
- Trigger the Report Generation
  ```bash
  curl --location '{{SERVER_URL}}/reports/generateReport' \
    --header 'Content-Type: application/json' \
    --data '{
    "crawlHeaderId": {{PROCESS_ID}}
    }' 
  ```
