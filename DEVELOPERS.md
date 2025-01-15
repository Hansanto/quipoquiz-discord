# Overview

| Element                                                                | Usage                                      |
|------------------------------------------------------------------------|--------------------------------------------|
| [![](https://img.shields.io/badge/Gradle-blue?logo=gradle)](#gradle)   | Build tool                                 |
| [![](https://img.shields.io/badge/Kotlin-orange?logo=kotlin)](#kotlin) | Development language                       |
| [![](https://img.shields.io/badge/Docker-blue?logo=docker)](#docker)   | Containerization for application execution |

## Getting started

### Gradle

![](https://img.shields.io/badge/require-black)

> [!TIP]
> We provide a [Gradle wrapper](gradlew) to build the project.
> You can use it to avoid installing Gradle on your machine.

To check if the Gradle wrapper is available:

```shell
./gradlew -v
```

All the dependencies are defined in [settings.gradle.kts](settings.gradle.kts) file and used
in [build.gradle.kts](build.gradle.kts) file.

### Kotlin

![](https://img.shields.io/badge/require-black)

We use [Kotlin](https://kotlinlang.org/) to create this multiplatform library with great performance by the use
of [coroutines](https://kotlinlang.org/docs/coroutines-overview.html).

The version of Kotlin is defined in [settings.gradle.kts](settings.gradle.kts) file.

### Docker

![](https://img.shields.io/badge/optional-black)
[![](https://img.shields.io/badge/docker-install-blue?logo=docker)](https://www.docker.com/)

Docker can be used to build and run the project.
However, it's also possible to do it without Docker.

## Commands

### Generate GraphQL Kotlin files

You need to set the [QuipoQuiz](https://quipoquiz.com) token to retrieve the models from their API.

The schema will be downloaded and placed
at [src/commonMain/graphql/quipoquiz.graphqls](src/commonMain/graphql/quipoquiz.graphqls).

The Kotlin files will be generated in
the [build/generated/source/apollo/quipoquiz](build/generated/source/apollo/quipoquiz) directory.

| Environment variable | Description     | Required | Default value                 |
|----------------------|-----------------|----------|-------------------------------|
| QUIPOQUIZ_TOKEN      | QuipoQuiz token | ✅        |                               |
| QUIPOQUIZ_URL        | QuipoQuiz URL   | ❌        | https://cms.quipoquiz.com/api |

_Unix_

````shell
# Token
QUIPOQUIZ_TOKEN="YOUR_TOKEN" ./gradlew generateSources
# Token and URL
QUIPOQUIZ_TOKEN="YOUR_TOKEN" QUIPOQUIZ_URL="YOUR_URL" ./gradlew generateSources
````

_Windows_

````shell
# Token
$env:QUIPOQUIZ_TOKEN="YOUR_TOKEN"; ./gradlew generateSources
# Token and URL
$env:QUIPOQUIZ_TOKEN="YOUR_TOKEN"; $env:QUIPOQUIZ_URL="YOUR_URL"; ./gradlew generateSources
````

### Build

> [!IMPORTANT]
> To build, you need to generate the GraphQL Kotlin files.
> Check the step [Generate GraphQL Kotlin files](#generate-graphql-kotlin-files).

#### Without Docker

> [!NOTE]
> You need to have [JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
> (or higher) installed on your machine whatever the final executable you want to build.

##### JVM

```shell
./gradlew app:jvm:assembleDist app:jvm:installDist
```

The executable will be placed in the [app/jvm/build/install/jvm/bin](app/jvm/build/install/jvm/bin)
directory.

##### NodeJS

> [!NOTE]
> In addition to the JDK, you need to have [NodeJS](https://nodejs.org/en/download/) installed on your machine.

```shell
./gradlew compileProductionExecutableKotlinJs jsPackageJson
mv .\build\js\packages\quipoquiz-discord\package.json .\build\compileSync\js\main\productionExecutable\kotlin
cd build\compileSync\js\main\productionExecutable\kotlin
npm i
```

The executable will be placed in
the [build/compileSync/js/main/productionExecutable/kotlin](build/compileSync/js/main/productionExecutable/kotlin)
directory.

##### Apple

> [!NOTE]
> In addition to the JDK, you need to have [Xcode](https://developer.apple.com/xcode/) installed on your machine.

| Platform              | Command                                                | Executable directory                                                                                   |
|-----------------------|--------------------------------------------------------|--------------------------------------------------------------------------------------------------------|
| IosArm64              | `./gradlew linkReleaseExecutableIosArm64`              | [build/bin/iosArm64/releaseExecutable](build/bin/iosArm64/releaseExecutable)                           |
| IosSimulatorArm64     | `./gradlew linkReleaseExecutableIosSimulatorArm64`     | [build/bin/iosSimulatorArm64/releaseExecutable](build/bin/iosSimulatorArm64/releaseExecutable)         |
| IosX64                | `./gradlew linkReleaseExecutableIosX64`                | [build/bin/iosX64/releaseExecutable](build/bin/iosX64/releaseExecutable)                               |
| MacosArm64            | `./gradlew linkReleaseExecutableMacosArm64`            | [build/bin/macosArm64/releaseExecutable](build/bin/macosArm64/releaseExecutable)                       |
| MacosX64              | `./gradlew linkReleaseExecutableMacosX64`              | [build/bin/macosX64/releaseExecutable](build/bin/macosX64/releaseExecutable)                           |
| TvosArm64             | `./gradlew linkReleaseExecutableTvosArm64`             | [build/bin/tvosArm64/releaseExecutable](build/bin/tvosArm64/releaseExecutable)                         |
| TvosSimulatorArm64    | `./gradlew linkReleaseExecutableTvosSimulatorArm64`    | [build/bin/tvosSimulatorArm64/releaseExecutable](build/bin/tvosSimulatorArm64/releaseExecutable)       |
| TvosX64               | `./gradlew linkReleaseExecutableTvosX64`               | [build/bin/tvosX64/releaseExecutable](build/bin/tvosX64/releaseExecutable)                             |
| WatchosArm64          | `./gradlew linkReleaseExecutableWatchosArm64`          | [build/bin/watchosArm64/releaseExecutable](build/bin/watchosArm64/releaseExecutable)                   |
| WatchosSimulatorArm64 | `./gradlew linkReleaseExecutableWatchosSimulatorArm64` | [build/bin/watchosSimulatorArm64/releaseExecutable](build/bin/watchosSimulatorArm64/releaseExecutable) |

#### With Docker

> [!NOTE]
> You need to have [Docker](#docker) installed and started on your machine.

##### JVM

_Unix_

````shell
QUIPOQUIZ_TOKEN="YOUR_TOKEN" docker build --secret id=QUIPOQUIZ_TOKEN -t quipoquiz-bot-jvm -f DockerfileJvm .
````

_Windows_

```shell
$env:QUIPOQUIZ_TOKEN="YOUR_TOKEN"; docker build --secret id=QUIPOQUIZ_TOKEN -t quipoquiz-bot-jvm -f DockerfileJvm .
```

A new Docker image will be created with the name `quipoquiz-bot-jvm`.

##### NodeJS

_Unix_

````shell
QUIPOQUIZ_TOKEN="YOUR_TOKEN" docker build --secret id=QUIPOQUIZ_TOKEN -t quipoquiz-bot-node -f DockerfileNode .
````

_Windows_

```shell
$env:QUIPOQUIZ_TOKEN="YOUR_TOKEN"; docker build --secret id=QUIPOQUIZ_TOKEN -t quipoquiz-bot-node -f DockerfileNode .
```

A new Docker image will be created with the name `quipoquiz-bot-node`.

### Run

> [!IMPORTANT]
> To run, you need to build the project first. Check the step [Build](#build).

| Environment variable                    | Description                                                                           | Required | Default value                  | Limit                                                                          | Example               | 
|-----------------------------------------|---------------------------------------------------------------------------------------|----------|--------------------------------|--------------------------------------------------------------------------------|-----------------------|
| BOT_TOKEN                               | Discord bot token.                                                                    | ✅        |                                |                                                                                | token                 |
| QUIPOQUIZ_TOKEN                         | The token to retrieve data from with QuipoQuiz API.                                   | ✅        |                                |                                                                                | token                 |
| QUIPOQUIZ_REQUEST_PAGE_SIZE             | Page size to retrieve data from QuipoQuiz API.                                        | ❌        | 10                             | greater than 0                                                                 | 10                    |
| QUIPOQUIZ_DEFAULT_CATEGORY_COLOR        | Default color for the category embed.                                                 | ❌        | #2A75FF                        |                                                                                | #2A75FF               |
| QUIPOQUIZ_CACHE_EXPIRATION              | Cache expiration time in seconds.                                                     | ❌        | 1 day                          | greater than 0 seconds                                                         | 86400                 |
| QUIPOQUIZ_CACHE_DIRECTORY               | Cache directory.                                                                      | ❌        | Temporary folder of the system |                                                                                | /tmp/quipoquiz        |
| QUIPOQUIZ_INCLUDE_QUIZ_WITHOUT_CATEGORY | Include quizzes without category.                                                     | ❌        | false                          | true/false                                                                     | true                  |
| QUIPOQUIZ_URL                           | QuipoQuiz URL.                                                                        | ❌        | https://cms.quipoquiz.com/api  |                                                                                | http://localhost:8080 |
| BOT_LANGUAGE_DEFAULT                    | Default language to define commands and display error.                                | ❌        | English                        | french/fr/english/en                                                           | English               |
| BOT_REFRESH_INTERVAL                    | Refresh interval in seconds to update the embeds and buttons to display changes.      | ❌        | 1 second                       | greater than 0 seconds                                                         | 1                     |
| BOT_DEV_GUILD                           | Discord guild ID for development.                                                     | ❌        |                                | greater than 0                                                                 | 123456789123456789    |
| GAME_SCORE_PLAYER_PER_PAGE              | Number of players per page in the game score.                                         | ❌        | 10                             | greater than 0                                                                 | 10                    |
| GAME_DEFAULT_COLOR                      | Default color for the game embed.                                                     | ❌        | #3B72F0                        | [hexadecimal color](https://en.wikipedia.org/wiki/Web_colors#HTML_color_names) | #3B72F0               |
| GAME_TIMEOUT_ALIVE_PER_QUESTION         | Timeout in seconds between each interaction with a question before stopping the game. | ❌        | 180                            | greater than 0 seconds                                                         | 180                   |
| GAME_TIMEOUT_IDLE                       | Timeout in seconds before stopping the game if no interaction.                        | ❌        | 3600                           | greater than 0 seconds                                                         | 3600                  |
| GAME_TIMER_VOTE_UPDATE_EVERY            | Timer in seconds to update the remaining time to vote.                                | ❌        | 10                             | greater than 0 seconds                                                         | 10                    |
| COMMAND_AUTO_COMPLETE_MAX_SIZE          | Maximum number of elements to display in the auto-complete command.                   | ❌        | 25                             | greater than 0                                                                 | 25                    |
| COMMAND_LIFE_MIN                        | Minimum life to play a game.                                                          | ❌        | 1                              | greater than 0                                                                 | 1                     |
| COMMAND_LIFE_MAX                        | Maximum life to play a game.                                                          | ❌        | 2147483647                     | greater than 0 and `COMMAND_LIFE_MIN`                                          | 10                    |
| COMMAND_TIME_VOTE_MIN                   | Minimum time to vote in seconds.                                                      | ❌        | 5                              | greater than 0                                                                 | 60                    |
| COMMAND_TIME_VOTE_MAX                   | Maximum time to vote in seconds.                                                      | ❌        | 120                            | greater than 0 and `COMMAND_TIME_VOTE_MIN`                                     | 120                   |
| COMMAND_TIME_VOTE_DUEL_DEFAULT          | Default time to vote in seconds for a duel.                                           | ❌        | 60                             | greater than 0 seconds                                                         | 20                    |
| COMMAND_LIMIT_QUIZ_MIN                  | Minimum number of quizzes to play a game.                                             | ❌        | 1                              | greater than 0                                                                 | 1                     |
| COMMAND_LIMIT_QUIZ_MAX                  | Maximum number of quizzes to play a game.                                             | ❌        | 100                            | greater than 0 and `COMMAND_LIMIT_QUIZ_MIN`                                    | 10                    |
| COMMAND_LIMIT_QUIZ_DEFAULT_NORMAL_MODE  | Default number of quizzes to play a game in normal mode.                              | ❌        | 10                             | greater than 0                                                                 | 10                    |
| COMMAND_LIMIT_QUIZ_DEFAULT_DUEL_MODE    | Default number of quizzes to play a game in duel mode.                                | ❌        | 10                             | greater than 0                                                                 | 5                     |
| COMMAND_HIDE_VOTE_DEFAULT               | Default value to hide the vote in the game.                                           | ❌        | false                          | true/false                                                                     | false                 |
| COMMAND_CATEGORY_PAGE_SIZE              | Page size to display the quizzes of a category.                                       | ❌        | 10                             | greater than 0                                                                 | 10                    |

#### Without Docker

##### JVM

> [!NOTE]
> You need to have [JDK 21](https://www.oracle.com/java/technologies/downloads/#java21)
> (or higher) installed on your machine.

_Unix_

````shell
QUIPOQUIZ_TOKEN="YOUR_TOKEN" ./app/jvm/build/install/jvm/bin/jvm
````

_Windows_

```shell
$env:QUIPOQUIZ_TOKEN="YOUR_TOKEN"; ./app/jvm/build/install/jvm/bin/jvm.bat
```

##### NodeJS

> [!NOTE]
> You need to have [NodeJS](https://nodejs.org/en/download/) installed on your machine.

_Unix_

```shell
QUIPOQUIZ_TOKEN="YOUR_TOKEN" node build/compileSync/js/main/productionExecutable/kotlin/quipoquiz-discord.js
```

_Windows_

```shell
$env:QUIPOQUIZ_TOKEN="YOUR_TOKEN"; node build/compileSync/js/main/productionExecutable/kotlin/quipoquiz-discord.js
```

##### Apple

```shell
# Example for macosX64
QUIPOQUIZ_TOKEN="YOUR_TOKEN" ./build/bin/macosX64/releaseExecutable/quipoquiz-discord.kexe
```

#### With Docker

Replace `my_storage_folder` with the path of the cache directory you want to use.

##### JVM

````shell
docker run -v ./quipoquiz-cache:/my_storage_folder -e QUIPOQUIZ_TOKEN="YOUR_TOKEN" quipoquiz-bot-jvm
````

##### NodeJS

````shell
docker run -v ./quipoquiz-cache:/my_storage_folder -e QUIPOQUIZ_TOKEN="YOUR_TOKEN" quipoquiz-bot-node
````

### Test

> [!IMPORTANT]
> To run unit tests, you need to generate the GraphQL Kotlin files.
> Check the step [Generate GraphQL Kotlin files](#generate-graphql-kotlin-files).

The tests are located in the [src/commonTest](src/commonTest) directory.

To run the tests:

```shell
./gradlew allTests
```

### Linter

We use [Ktlint](https://github.com/JLLeitschuh/ktlint-gradle) to format the code
and the configuration is defined in [.editorconfig](.editorconfig) file.

To format the code:

```shell
./gradlew ktlintFormat
```

To check the formatting:

```shell
./gradlew ktlintCheck
```

### Format

We use [Detekt](https://github.com/detekt/detekt) to analyze the code
and apply the rules defined in the [detekt.yml](detekt.yml) file.

To analyze the code:

```shell
./gradlew detektAll
```
