![Skrip Language](.github/assets/Cover.jpg)

---

# Skrip
**Skrip** is a Minecraft plugin for Paper/Spigot, which allows server owners and other people
to modify their servers without learning Java. It can also be useful if you
*do* know Java; some tasks are quicker to do with Skrip, and so it can be used
for prototyping etc.

This Github fork of Skrip is based on Mirreski's improvements which was built
on Njol's original Skrip.

## Requirements
Skrip requires **Spigot** to work. You heard it right, **CraftBukkit** does *not* work.
**Paper**, which is a fork of Spigot, is recommended; it is required for some
parts of Skrip to be available.

Skrip supports only the **latest** patch versions of Minecraft 1.19 and newer.
For example, this means that 1.19.4 is supported, but 1.19.3 is *not*.
Testing with all old patch versions is not feasible for us.

Minecraft 1.12 and earlier are not, and will not be supported. New Minecraft
versions will be supported as soon as possible.

## Download
You can find the downloads for each version with their release notes in the [releases page](https://github.com/SkripLang/Skrip/releases).

Two major feature updates are expected each year in January and July, with monthly patches occurring in between. For full details, please review our [release model](CLOCKWORK_RELEASE_MODEL.md).

## Documentation
Documentation is available [here](https://docs.Skriplang.org/) for the
latest version of Skrip.

## Reporting Issues
Please see our [contribution guidelines](https://github.com/SkripLang/Skrip/blob/master/.github/contributing.md)
before reporting issues.

## Help Us Test
Wanting to help test Skrip's new features and releases?
You can head on over to our [Official Testing Discord](https://discord.gg/ZPsZAg6ygu), and whenever we start testing new features/releases you will be the first to know.

Please note this is not a help Discord.
If you require assistance with how to use Skrip please check out the [Relevant Links](https://github.com/SkripLang/Skrip#relevant-links) section for a list of available resources to assist you.

## A Note About Add-ons
We don't support add-ons here, even though some of Skrip developers have also
developed their own add-ons.

## Compiling
Skrip uses Gradle for compilation. Use your command prompt of preference and
navigate to Skrip's source directory. Then you can just call Gradle to compile
and package Skrip for you:

```bash
./gradlew clean build # on UNIX-based systems (mac, linux)
gradlew clean build # on Windows
```

You can get source code from the [releases page](https://github.com/SkripLang/Skrip/releases).
You may also clone this repository, but that code may or may not be stable.

### Compiling Modules
Parts of Skrip are provided as Gradle subprojects. They require Skrip, so
they are compiled *after* it has been built. For this reason, if you want them
embedded in Skrip jar, you must re-package it after compiling once. For example:

```
./gradlew jar
```

Note that modules are not necessary for Skrip to work. Currently, they are
only used to provide compatibility with old WorldGuard versions.

### Testing
Skrip has some tests written in Skrip. Running them requires a Minecraft
server, but our build script will create one for you. Running the tests is easy:

```
./gradlew (quickTest|SkripTest|SkripTestJava17|SkripTestJava21)
```

<code>quickTest</code> runs the test suite on newest supported server version.
<code>SkripTestJava21</code> (1.20.6+) runs the tests on Java 21 supported versions.
<code>SkripTestJava17</code> (1.19.4-1.20.4) runs the tests on Java 17 supported versions.
<code>SkripTest</code> runs the tests on all versions.
That is, it runs SkripTestJava17, and SkripTestJava21.

By running the tests, you agree to Mojang's End User License Agreement.

### Importing to Eclipse
With new Eclipse versions, there is integrated Gradle support, and it actually works now.
So, first get latest Eclipse, then import Skrip as any Gradle project. Just
make sure to **keep** the configuration when the importer asks for that!

If you encounter strange issues, make sure you follow the instructions above and have
actually downloaded latest Eclipse or update your installation correctly. Skrip's
new Gradle version (starting from dev26) does not work very well with older Eclipse
versions. Also, do *not* use Gradle STS; it is outdated.

### Importing to IDEA
You'll need to make sure that nullness annotations are working correctly. Also,
when sending pull requests, make sure not to change IDEA configuration files
that may have been stored in the repository.

### Releasing
```
./gradlew clean build
./gradlew <flavor>Release
```
Available flavors are github and spigot. Please do not abuse flavors by
compiling your own test builds as releases.

## Contributing
Please review our [contribution guidelines](https://github.com/SkripLang/Skrip/blob/master/.github/contributing.md).
In addition to that, if you are contributing Java code, check our
[coding conventions](https://github.com/SkripLang/Skrip/blob/master/code-conventions.md).

## Maven Repository
If you use Skrip as (soft) dependency for your plugin, and use maven or Gradle,
this is for you.

First, you need to add the Maven repository at the **END** of all your repositories. Skrip is not available in Maven Central.
```gradle
repositories {
    maven {
        url 'https://repo.Skriplang.org/releases'
    }
}
```

Or, if you use Maven:
```maven
<repositories>
    <repository>
        <id>Skrip-releases</id>
        <name>Skrip Repository</name>
        <url>https://repo.Skriplang.org/releases</url>
    </repository>
</repositories>
```

For versions of Skrip after dev37 you might need to add the paper-api repository to prevent build issues.

```gradle
maven {
    url 'https://repo.destroystokyo.com/repository/maven-public/'
}
```

Or, if you use Maven:
```maven
<repository>
    <id>destroystokyo-repo</id>
    <url>https://repo.destroystokyo.com/content/repositories/snapshots/</url>
</repository>
```

Then you will also need to add Skrip as a dependency.
```gradle
dependencies {
    implementation 'com.github.SkripLang:Skrip:[versionTag]'
}
```

An example of the version tag would be ```2.8.5```.

> Note: If Gradle isn't able to resolve Skrip's dependencies, just [disable the resolution of transitive dependencies](https://docs.gradle.org/current/userguide/resolution_rules.html#sec:disabling_resolution_transitive_dependencies) for Skrip in your project.

Or, if you use Maven:
```
<dependency>
    <groupId>com.github.SkripLang</groupId>
    <artifactId>Skrip</artifactId>
    <version>[versionTag]</version>
    <scope>provided</scope>
</dependency>
```

## Relevant Links
* [skUnity forums](https://forums.skunity.com)
* [skUnity addon releases](https://forums.skunity.com/forums/addon-releases)
* [skUnity Discord invite](https://discord.gg/0l3WlzBPKX7WNjkf)
* [Skrip Chat Discord invite](https://discord.gg/0lx4QhQvwelCZbEX)
* [Skrip Hub](https://Skriphub.net)
* [Original Skrip at Bukkit](https://dev.bukkit.org/bukkit-plugins/Skrip) (inactive)

Note that these resources are not maintained by Skrip's developers. Don't
contact us about any problems you might have with them.

## Developers
You can find all contributors [here](https://github.com/SkripLang/Skrip/graphs/contributors).

All code is owned by its writer, licensed for others under GPLv3 (see LICENSE)
unless otherwise specified.
