# FAIRDataPoint-index
Index of FAIR Data Points

[![Build Status](https://travis-ci.com/FAIRDataTeam/FAIRDataPoint-index.svg?branch=develop)](https://travis-ci.com/FAIRDataTeam/FAIRDataPoint-index)

# Introduction
The index serves as a registry for [FAIR Data Point](https://github.com/FAIRDataTeam/FAIRDataPoint) deployments.

# Quickstart
- build the app by running `mvn package`
- start redis by running `docker-compose up -d`
- post something using `curl -H "Content-Type: application/json" -d '{"clientUrl":"http://example.com/"}' http://localhost:8080/`
- see the results in your browser at `http://localhost:8080/`

# License
This project is licensed under the [MIT License](LICENSE).
