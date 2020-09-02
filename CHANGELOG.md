# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added

- Simple webhooks (from database) with possibility to select specific event(s) and/or
  specific entries
- Ability to trigger metadata retrieval using secured API endpoints (admin-only)

## [0.2.0]

### Added

- Metadata retrieval after receiving ping as async event with possibility to resume
- Info page for each entry with details including retrieved metadata
- States of entries with possible filtering
- Configurable rate limit per IP for receiving pings

## [0.1.1]

### Fixed

- Serving static files for the web index page correctly

## [0.1.0]

Initial version for simple list of FAIR Data Points.

### Added

- Endpoint for "call home" ping and storing entries in MongoDB
- REST API to retrieve entries list (both all and paged) documented using Swagger/OpenAPI
- Simple webpage with table to browse entries including sorting and pagination

[Unreleased]: /../../compare/v0.2.0...develop
[0.1.0]: /../../tree/v0.1.0
[0.1.1]: /../../tree/v0.1.1
[0.2.0]: /../../tree/v0.2.0
