# 🚀 Publishing Guide

Follow these steps to publish SafeLLMKit packages to public registries.

---

## 🐍 1. Python (PyPI)

Publish `safellmkit-python` to PyPI to make it installable via `pip install safellmkit`.

### Prerequisites
1.  Create an account on [PyPI.org](https://pypi.org/).
2.  Generate an **API Token** from Account Settings.

### Steps
1.  **Navigate to directory**:
    ```bash
    cd safellmkit-python
    ```

2.  **Install build tools**:
    ```bash
    pip install build twine
    ```

3.  **Build the package** (Wheel & Source):
    ```bash
    python -m build
    ```
    *This creates a `dist/` folder.*

4.  **Upload to PyPI**:
    ```bash
    twine upload dist/*
    ```
    *When prompted for username, use `__token__` and paste your API key as password.*

---

## 🌐 2. JavaScript (NPM)

Publish `safellmkit-js` to the NPM Registry to make it installable via `npm install safellmkit-js`.

### Prerequisites
1.  Create an account on [npmjs.com](https://www.npmjs.com/).

### Steps
1.  **Navigate to directory**:
    ```bash
    cd safellmkit-js
    ```

2.  **Login to NPM**:
    ```bash
    npm login
    ```
    *Follow the browser prompt.*

3.  **Build**:
    Ensure `dist/` is up to date (if you have a build script, otherwise source is ts).
    *Note: package.json points to `src/index.ts` or `dist/index.js`. Standard practice compiles TS first.*
    ```bash
    npm run build
    ```
    *(Ensure you added a build script to package.json: `"build": "tsc"`)*

4.  **Publish**:
    ```bash
    npm publish --access public
    ```

---

## ☕ 3. Kotlin (Maven Central)

Publish `safellmkit-core` to Maven Central (staging via Sonatype OSSRH).

### Prerequisites
1.  Create a JIRA account at [issues.sonatype.org](https://issues.sonatype.org/).
2.  Create a "New Project" ticket to claim your `GroupId` (e.g., `io.github.aryanbaglane`).
3.  Generate a **GPG Key** to sign artifacts (`gpg --gen-key`).

### Configuration (build.gradle.kts)
You usually need a plugin like `vanniktech/gradle-maven-publish-plugin`.

**Add to root `build.gradle.kts`:**
```kotlin
plugins {
    id("com.vanniktech.maven.publish") version "0.25.3"
}
```

**Configure in `gradle.properties`:**
```properties
signing.keyId=...
signing.password=...
signing.secretKeyRingFile=...
mavenCentralUsername=...
mavenCentralPassword=...
```

### Steps
1.  **Publish to Staging**:
    ```bash
    ./gradlew publishToMavenCentral
    ```

2.  **Release**:
    Log in to [s01.oss.sonatype.org](https://s01.oss.sonatype.org/), "Close" the staging repository, and "Release" it.
