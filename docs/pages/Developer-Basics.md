# Developer Basics

The following provides basic guidance on how to set up command-line use of nshmp-haz.

## Required Software

* Java 11 JDK: [Oracle](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html) or
  [Amazon Corretto](https://docs.aws.amazon.com/corretto/latest/corretto-11-ug/downloads-list.html)
* [Git](https://git-scm.com/downloads)  
  * Git is a distributed version control system. The USGS uses a [GitLab](https://docs.gitlab.com)
    [instance](https://code.usgs.gov/) to host projects and facilitate sharing and collaborative
    development of code. Git is included in the macOS
    [developer tools](https://developer.apple.com/xcode/).  
  * Windows users may want to consider [Git for Windows](https://git-for-windows.github.io) or
    [GitHub Desktop](https://desktop.github.com), both of which include a linux-like terminal
    (Git BASH) in which subsequent commands listed here will work.  

Other project dependencies are managed with [Gradle](https://gradle.org/), which does not
require a separate installation. Gradle is clever about finding Java, but some users may have to
explicitly define a `JAVA_HOME` environment variable. For example, on Unix-like systems with
`bash` as the default shell, one might add the following to `~/.bash_profile`:

```bash
# macOS
export JAVA_HOME="$(/usr/libexec/java_home -v 11)"
# Linux
export JAVA_HOME=/usr/lib/jvm/jdk-11.0.6.jdk
```

On Windows systems, environment variables are set through the `System Properties > Advanced >
Environment Variables...` control panel. Depending on where Java is installed, `JAVA_HOME`
might be:

```bash
JAVA_HOME     C:\Program Files\Java\jdk-11.0.6.jdk
```

## Set Up Git

Follow the [GitLab instructions](https://docs.gitlab.com/ee/topics/git/). Some users may find it
easier to use [Git for Windows](https://git-for-windows.github.io) or
[GitHub Desktop](https://desktop.github.com). These desktop applications install required system
components and are helpful for managing communication between local and remote repositories and
viewing file diffs as one makes changes.

## Get the Code

```bash
cd /directory/for/code
git clone https://code.usgs.gov/ghsc/nshmp/nshmp-haz.git
```

## Eclipse Integration (Optional)

Eclipse provides automatic compilation, syntax highlighting, and integration with Git, among
other useful features. To build or modify *nshmp-haz* using [Eclipse](http://www.eclipse.org/),
install the [Eclipse IDE for Java Developers](https://www.eclipse.org/downloads/packages/) or
[Eclipse IDE for Enterprise Java and Web Developers](https://www.eclipse.org/downloads/packages/),
if you plan on developing web services. Import the project into Eclipse: `File > Import >
Gradle > Existing Gradle Project`

---

[**Documentation Index**](docs/README.md)

---
![USGS logo](images/usgs-icon.png) &nbsp;[U.S. Geological Survey](https://www.usgs.gov)
National Seismic Hazard Mapping Project ([NSHMP](https://earthquake.usgs.gov/hazards/))
