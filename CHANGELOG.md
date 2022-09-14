# Change Logging

#### Version scheme follows loosely [Semantic versioning](https://semver.org/):  "MAJOR.MINOR.PATCH"
_(Loosely because: MAJOR does also increase by reaching Milestones!)_

+ MAJOR - The major version number, incremented for a major release/milestone and incompatible API changes
+ MINOR - The minor version number, incremented by adding functionality in a backwards compatible manner
+ PATCH - Bugfix for prior major/minor version

### Version:
- **0.4.0**
  - Major Update
  - Finished structure of Websites
    - Meaning: All websites exist now, I just need them to fill with life...
  - Added Category administration
  - Added possibility to upload/download files
  - Added Login/Logout system
  - Updated Bootstrap to v.5.2.0
  - Updated Bootstrap Icons to v.1.9.1
  - Removed jQuery
    - I'll try to make this only with Bootstrap. Let's see how this works...
  - Updated [Spring Security](src/main/java/com/acme/onlineshop/security/SecurityConfig.java) to new standard
  - Added several [new Database Models](src/main/java/com/acme/onlineshop/persistence)
- **0.3.0**
  - Added new Role: Staff
    - Has more permissions than "normal" User, but less than "admin"
  - Updated [readme](README.md)
- **0.2.0**
  - Added backend skeleton
- **0.1.0**
  - Initialization
