# FAIRDataPoint-index
Index of FAIR Data Points

[![FAIRDataPoint-index CI](https://github.com/FAIRDataTeam/FAIRDataPoint-index/workflows/FAIRDataPoint-index%20CI/badge.svg?branch=master)](https://github.com/FAIRDataTeam/FAIRDataPoint-index/actions)
[![License](https://img.shields.io/github/license/FAIRDataTeam/FAIRDataPoint-index)](LICENSE)

# Introduction
The index serves as a registry for [FAIR Data Point](https://github.com/FAIRDataTeam/FAIRDataPoint) deployments.

# Quickstart
- build the app by running `mvn package`
- start redis by running `docker-compose up -d`
- post something using `curl -H "Content-Type: application/json" -d '{"clientUrl":"http://example.com/"}' http://localhost:8080/`
- see the results in your browser at `http://localhost:8080/`

# License
This project is licensed under the [MIT License](LICENSE).
