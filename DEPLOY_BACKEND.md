# DEPLOY_BACKEND.md — latency-lab (Spring Boot → Cloud Run)

Spring Boot (Java 17) → Dockerfile → Artifact Registry → Cloud Run  
Windows + PowerShell friendly.

---

## 0) TL;DR (redeploy in two commands)

```powershell
# From C:\workspace\latency-lab (repo root with Dockerfile)
$REGION="europe-west2"
$PROJECT="YOUR_PROJECT_ID"                  # e.g. bench-marks-demo-123456
$REPO="apps"
$IMAGE="$REGION-docker.pkg.dev/$PROJECT/$REPO/latency-lab-backend:1.0.$(Get-Date -Format yyyyMMddHHmm)"

gcloud builds submit . --tag $IMAGE
gcloud run deploy latency-lab-backend --image $IMAGE --allow-unauthenticated --port 8080 --memory 512Mi --cpu 1 --min-instances 0 --max-instances 1 --concurrency 80
```

> Cloud Run service URL stays stable unless you change service/region.

---

## 1) Prerequisites (once per machine)

```powershell
gcloud auth login
gcloud config set project YOUR_PROJECT_ID
gcloud config set run/region europe-west2
```

Create Artifact Registry repo (one-time):
```powershell
$REGION="europe-west2"; $REPO="apps"
gcloud artifacts repositories create $REPO --repository-format=docker --location=$REGION
```

---

## 2) Dockerfile (Maven example) at repo root

```dockerfile
# --- Build stage ---
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/app.jar
ENV PORT=8080 JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=70"
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
```

---

## 3) App settings (keep these in your repo)

`src/main/resources/application.properties`
```properties
server.port=${PORT:8080}
```

**CORS/Security** (Netlify + localhost OK) — Spring Security 6 / Boot 3:
```java
package com.yourcompany.latencylab;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
    return http.build();
  }

  @Bean
  CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration cfg = new CorsConfiguration();
    cfg.setAllowedOriginPatterns(List.of(
      "https://*.netlify.app",
      "http://localhost:5173", "http://localhost:3000"
    ));
    cfg.setAllowedMethods(Arrays.asList("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    cfg.setAllowedHeaders(List.of("*"));
    cfg.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
    src.registerCorsConfiguration("/api/**", cfg);
    return src;
  }
}
```

> Later, tighten AllowedOriginPatterns to your exact Netlify/custom domains.

---

## 4) Full redeploy steps

```powershell
$REGION="europe-west2"
$PROJECT="YOUR_PROJECT_ID"
$REPO="apps"
$IMAGE="$REGION-docker.pkg.dev/$PROJECT/$REPO/latency-lab-backend:1.0.$(Get-Date -Format yyyyMMddHHmm)"

gcloud builds submit . --tag $IMAGE

gcloud run deploy latency-lab-backend `
  --image $IMAGE `
  --allow-unauthenticated `
  --port 8080 `
  --memory 512Mi --cpu 1 `
  --min-instances 0 --max-instances 1 `
  --concurrency 80
```

Get service URL:
```powershell
gcloud run services describe latency-lab-backend --format="value(status.url)"
```

---

## 5) Validate

- Direct: `https://latency-lab-backend-633887535514.europe-west2.run.app/api/health` → **200**

---

## 6) Logs / Rollback

```powershell
gcloud logs tail --project $PROJECT
gcloud run revisions list --service latency-lab-backend
gcloud run services update-traffic latency-lab-backend --to-revisions <REVISION-ID>=100
```

---

## 7) When domains/URLs change

- If the Cloud Run service/region changes, update the frontend proxy target and redeploy the frontend.
- If the Netlify domain changes, add it to AllowedOriginPatterns and redeploy the backend.

---

## 8) Troubleshooting

- **403 Invalid CORS** → ensure calls go via Netlify `/api/*`; allow your Netlify origin; include `OPTIONS`.
- **Jar path mismatch** → adjust `COPY --from=build ...` to your actual JAR.
- **Cold start** → first hit may be slow; use `--min-instances 1` if needed (may consume free tier).
