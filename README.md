# Dromedary Drones

![build](https://github.com/Team-Cobalt/DromedaryDrones/workflows/build%20&%20test/badge.svg?branch=master)
![GitHub release](https://img.shields.io/github/v/release/Team-Cobalt/DromedaryDrones?include_prereleases)

This README covers the installation, startup, and use of team Cobalt's simulated drone delivery service.

# Contents

- [Dromedary Drones](#dromedary-drones)
- [Contents](#contents)
- [Pre-Installation](#pre-installation)
- [Installation](#installation)
- [Running](#running)
  - [Linux](#linux)
  - [Windows](#windows)

# Pre-Installation



# Installation

1. [Download](https://github.com/Team-Cobalt/DromedaryDrones/archive/master.zip) the project's ZIP file from the repository.
2. Extract the zip file at the location you want it to be installed to.

# Running

## Linux

1. Launch a terminal window and navigate into the top level project folder containing the gradlew file.

```bash
cd ~/path/to/DromedaryDrones-master/
```

2. Run the gradlew file with `run` as the argument.

```bash
./gradlew run
```

### PATH Variable

If you get a build failure like the following when trying to run the gradlew file, this means you either do not jave Java 13 installed or Java 13 is not properly set within the environment PATH.

```bash
FAILURE: Build failed with an exception.

* What went wrong:
java.lang.UnsupportedClassVersionError: org/openjfx/gradle/JavaFXPlugin has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class file versions up to xx.0)
```

---

## Windows

1. Launch the Command Prompt (`WindowsKey + R`, `cmd`, `Enter`) and navigate into the top level project folder containing the gradlew file.

```bash
cd C:\path\to\DromedaryDrones-master
```

2. Run the gradlew.bat file with `run` as the argument.

```bash
gradlew.bat run
```

### Environment Variable

If you get a build failure like the following when trying to run the gradlew file, this means you either do not jave Java 13 installed or Java 13 is not properly set within the environment variable JAVA_HOME.

```bash
FAILURE: Build failed with an exception.

* What went wrong:
java.lang.UnsupportedClassVersionError: org/openjfx/gradle/JavaFXPlugin has been compiled by a more recent version of the Java Runtime (class file version 55.0), this version of the Java Runtime only recognizes class file versions up to xx.0)
```
