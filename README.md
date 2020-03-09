# fair-metadata-index
Index of FAIR Data Points.

[![Build Status](https://travis-ci.com/FAIRDataTeam/fair-metadata-index.svg?branch=develop)](https://travis-ci.com/FAIRDataTeam/fair-metadata-index)

# Introduction
The index serves as a registry for [FAIR Data Point](https://github.com/FAIRDataTeam/FAIRDataPoint) deployments.

# Quickstart
- start redis by running `docker-compose up -d`
- start the app by running `mvn spring-boot:run`
- post something using `curl http://localhost:8080/ping?endpoint=http://example.com`
- see the results in your browser at `http://localhost:8080/`

# License
This project is licensed under the [MIT License](LICENSE).
