## Case Study - XLSX ZIP to TXT ZIP Conversion

<p align="center">
    <img src="screenshots/main-image.png" alt="Main Information" width="800" height="550">
</p>

### 📖 Information

### Project Definition (XLSX ZIP to TXT ZIP Conversion)

<p> A Spring Boot service that processes a user-uploaded **ZIP file** containing one or more **Mockaroo-style XLSX files**. The service extracts each XLSX, converts its content into a **column-aligned TXT file**, and returns all generated TXT files wrapped in a **new ZIP archive**. A separate logging endpoint allows for retrieval of application logs with **pagination and sorting** capabilities. </p>

#### End-to-end flow (Convert ZIP API):

* Client sends a multipart request with a single file part: `file` (the uploaded ZIP).
* Service extracts the ZIP content, validates the file, and processes each internal `.xlsx` file.
* Each XLSX file is converted to a column-aligned TXT file.
* All generated TXT files are compressed into a new ZIP archive.
* Returns:
    * `200 OK` with the converted **ZIP file** (`application/zip`) as the body.

#### Logs API flow (List Logs API):

* Client sends a `POST` request to `/api/logs/list` with a **JSON request body** (`CustomPagingRequest`) containing pagination and optional sorting parameters (page number, size, sort field, direction).
* Service loads a paged list of application logs (`LogDto`) using the parameters.
* Returns:
    * `200 OK` **JSON** model (`CustomResponse<CustomPagingResponse<LogResponse>>`).

#### Error semantics:

* `400 Bad Request` — Empty uploaded ZIP file or invalid pagination parameters (for logs API).
* `404 Not Found` — Resource not found (not explicitly mentioned in controller but typical).
* `500 Internal Server Error` — Unexpected failures (e.g., error during output ZIP creation).
* **Custom Exceptions**:
    * `InvalidZipContentException`: Uploaded ZIP is empty or contains no XLSX entries.
    * `ZipProcessingException`: Error during the creation of the output TXT ZIP.

-----

### Explore Rest APIs

Endpoints Summary

| Method | URL | Description | Request Body | Headers/Path | Response | Status Codes |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| POST | `/api/upload-zip` | Upload a ZIP of XLSX files and download a ZIP of aligned TXT files. | **Multipart:** \<ul\>\<li\>`file`: ZIP file (required)\</li\>\</ul\> | Content-Type: `multipart/form-data`, Accept: `application/zip` | `application/zip` file | 200, 400, 500 (via exceptions) |
| POST | `/api/logs/list` | List application logs with pagination and optional sorting. | **JSON:** `CustomPagingRequest` (page number, size, sort field, direction) | Content-Type: `application/json`, Accept: `application/json` | `CustomResponse<CustomPagingResponse<LogResponse>>` | 200, 400 |

-----

### Technologies

* Java 25
* Spring Boot 3.0
* Restful API
* Open Api (Swagger)
* Maven
* Junit5
* Mockito
* Integration Tests
* Mapstruct
* Docker
* Docker Compose
* CI/CD (Github Actions)
* Postman
* Prometheus
* Grafana
* Kubernetes
* JaCoCo (Test Report)
* AOP
* Sonarqube
* Jenkins

-----

### Prerequisites

#### Define Variable in .env file

```
# Note: Database settings are not directly relevant to the core logic shown but may be required for LogController
ZIP_DB_IP=localhost
ZIP_DB_PORT=3306
DATABASE_USERNAME={MY_SQL_DATABASE_USERNAME}
DATABASE_PASSWORD={MY_SQL_DATABASE_PASSWORD}
```

-----

### Open Api (Swagger)

Explore the API and schemas at: `http://localhost:1929/swagger-ui/index.html` (port configurable).

-----

### Maven, Docker and Kubernetes Running Process

**(The instructions for Maven, Docker, and Kubernetes running processes, including observability tools like Prometheus/Grafana, remain the same as the example README.md.)**

### Maven Run

To build and run the application with `Maven`, please follow the directions shown below;

```sh
$ git clone https://github.com/Rapter1990/xlsxziptotxtzip.git # (Assuming a new repo name)
$ cd xlsxziptotxtzip
$ mvn clean install
$ mvn spring-boot:run
```

-----

### Docker Run

The application can be built and run by the `Docker` engine. The `Dockerfile` has multistage build, so you do not need to build and run separately.

Please follow directions shown below in order to build and run the application with Docker Compose file;

```sh
$ cd xlsxziptotxtzip
$ docker-compose up -d
```

If you change anything in the project and run it on Docker, you can also use this command shown below

```sh
$ cd xlsxziptotxtzip
$ docker-compose up --build
```

To monitor the application, you can use the following tools:

- **Prometheus**:  
  Open in your browser at [http://localhost:9090](http://localhost:9090)  
  Prometheus collects and stores application metrics.

- **Grafana**:  
  Open in your browser at [http://localhost:3000](http://localhost:3000)  
  Grafana provides a dashboard for visualizing the metrics.  
  **Default credentials**:
  - Username: `admin`
  - Password: `admin`

Define prometheus data source url, use this link shown below

```
http://prometheus:9090
```

-----

### Kubernetes Run
To run the application, please follow the directions shown below;

- Start Minikube

```sh
$ minikube start
```

- Open Minikube Dashboard

```sh
$ minikube dashboard
```

- To deploy the application on Kubernetes, apply the Kubernetes configuration file underneath k8s folder

```sh
$ kubectl apply -f k8s
```

- To open Prometheus, click tunnel url link provided by the command shown below to reach out Prometheus

```sh
minikube service prometheus-service
```

- To open Grafana, click tunnel url link provided by the command shown below to reach out Prometheus

```sh
minikube service grafana-service
```

- Define prometheus data source url, use this link shown below

```
http://prometheus-service.default.svc.cluster.local:9090
```

---
### Docker Image Location

```
https://hub.docker.com/repository/docker/noyandocker/xlsxziptotxtzip/general
https://hub.docker.com/repository/docker/noyandocker/xlsxziptotxtzip-jenkins/general
```

### Sonarqube

- Go to `localhost:9000` for Docker and Go there through `minikube service sonarqube` for Kubernetes
- Enter username and password as `admin`
- Change password
- Click `Create Local Project`
- Choose the baseline for this code for the project as `Use the global setting`
- Click `Locally` in Analyze Method
- Define Token
- Click `Continue`
- Copy `sonar.host.url` and `sonar.token` (`sonar.login`) in the `properties` part in  `pom.xml`
- Run `mvn sonar:sonar` to show code analysis

-----

### Jenkins

- Go to `jenkins` folder
- Run `docker-compose up -d`
- Open Jenkins in the browser via `localhost:8080`
- Define `credentials` for `Github General token` used by  `GIT_REPO_ID` and `docker-hub-credentials` for `Docker` `Username` and `Password`
- Go to pipeline named `cryptoexchangeapi`
- Run Pipeline
- Show `Pipeline Step` to verify if it succeeded or failed

-----

### 📸 Screenshots

<details>
<summary>Click here to show the screenshots of project</summary>
    <p> Figure 1 </p>
    <img src ="screenshots/1.PNG">
    <p> Figure 2 </p>
    <img src ="screenshots/2.PNG">
    <p> Figure 3 </p>
    <img src ="screenshots/3.PNG">
    <p> Figure 4 </p>
    <img src ="screenshots/4.PNG">
    <p> Figure 5 </p>
    <img src ="screenshots/5.PNG">
    <p> Figure 6 </p>
    <img src ="screenshots/6.PNG">
    <p> Figure 7 </p>
    <img src ="screenshots/7.PNG">
    <p> Figure 8 </p>
    <img src ="screenshots/8.PNG">
    <p> Figure 9 </p>
    <img src ="screenshots/9.PNG">
    <p> Figure 10 </p>
    <img src ="screenshots/10.PNG">
    <p> Figure 11 </p>
    <img src ="screenshots/11.PNG">
    <p> Figure 12 </p>
    <img src ="screenshots/12.PNG">
    <p> Figure 13 </p>
    <img src ="screenshots/13.PNG">
    <p> Figure 14 </p>
    <img src ="screenshots/14.PNG">
    <p> Figure 15 </p>
    <img src ="screenshots/15.PNG">
    <p> Figure 16 </p>
    <img src ="screenshots/16.PNG">
    <p> Figure 17 </p>
    <img src ="screenshots/17.PNG">
    <p> Figure 18 </p>
    <img src ="screenshots/18.PNG">
    <p> Figure 19 </p>
    <img src ="screenshots/19.PNG">
    <p> Figure 20 </p>
    <img src ="screenshots/20.PNG">
    <p> Figure 21 </p>
    <img src ="screenshots/21.PNG">
    <p> Figure 22 </p>
    <img src ="screenshots/22.PNG">
    <p> Figure 23 </p>
    <img src ="screenshots/23.PNG">
    <p> Figure 24 </p>
    <img src ="screenshots/24.PNG">
    <p> Figure 25 </p>
    <img src ="screenshots/25.PNG">
    <p> Figure 26 </p>
    <img src ="screenshots/26.PNG">
    <p> Figure 27 </p>
    <img src ="screenshots/27.PNG">
    <p> Figure 28 </p>
    <img src ="screenshots/28.PNG">
    <p> Figure 29 </p>
    <img src ="screenshots/29.PNG">
    <p> Figure 30 </p>
    <img src ="screenshots/30.PNG">
    <p> Figure 31 </p>
    <img src ="screenshots/31.PNG">
    <p> Figure 32 </p>
    <img src ="screenshots/32.PNG">
    <p> Figure 33 </p>
    <img src ="screenshots/33.PNG">
    <p> Figure 34 </p>
    <img src ="screenshots/34.PNG">
    <p> Figure 35 </p>
    <img src ="screenshots/35.PNG">
    <p> Figure 36 </p>
    <img src ="screenshots/31.PNG">
    <p> Figure 37 </p>
    <img src ="screenshots/37.PNG">
    <p> Figure 38 </p>
    <img src ="screenshots/38.PNG">
    <p> Figure 39 </p>
    <img src ="screenshots/39.PNG">
    <p> Figure 40 </p>
    <img src ="screenshots/40.PNG">
</details>

-----

### Contributors

- [Sercan Noyan Germiyanoğlu](https://github.com/Rapter1990)
