---
layout: page
title: Installing Alixia Central
---

This document explains how to install Alixia Central on a computer. To date we have only installed Alixia Central on Linux machines, but Alixia should be (mostly) platform-agnostic.

## Hardware Requirements

The hardware requirements have not as yet been codified, but Alixia Central has been run successfully on antiquated POS desktop PCs and on a Raspberry Pi 3 Model B.

A Local Area Network (LAN) is required for remote stations to communicate with Alixia Central.

An Internet connection is required to access (e.g.) OpenWeatherMap, WikiData, and Wolfram\|Alpha.

## Software Requirements

The following software is required for the installation of Alixia Central:

* [OpenJDK Java 8+](http://openjdk.java.net/) (preferred), or [Oracle Java SE 8+](http://www.oracle.com/)
* [MariaDb 10.1.34+](https://mariadb.org/) (preferred) or [MySQL 5.7+](https://www.mysql.com/)
* [Redis 4.0.10+](https://redis.io/)

* **AlixiaExec.jar**, an uber-jar with all Java dependencies included
* **AlixiaCentralInstaller.jar**, the means by which Alixia Central is configured
* **appkeys.txt**, Alixia's configuration file with physical file locations etc., used by AlixiaCentralInstaller
* **purdahkeys.txt**, Alixia's encrypted configuration file with private keys, used by AlixiaCentralInstaller
* **a1icia_aes**, An encryption key for the Purdah

Note: If you need to install any of the above packages, you should probably use your distro's repositories to do so, but as always, YMMV.

Note: Actually, since we use [Apache Cayenne](https://cayenne.apache.org/) for RDBMS management, you can use any of [Cayenne's supported database management systems](https://cayenne.apache.org/database-support.html) in place of MariaDb / MySQL, like e.g. [PostgreSQL](https://www.postgresql.org/).

Note: Be sure to read [Notes on Alixia Central's File Descriptor Usage](https://github.com/markhull/Alixia/wiki/Notes-on-Alixia-Central's-File-Descriptor-Usage) in the Wiki.
