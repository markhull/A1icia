---
layout: page
title: Release Notes for Reykjavik Version 4
---

* Well, a pretty big change is **changing the project name** (and all the accompanying programs and documents) from A1icia to Alixia. I just got really tired of trying to type the number in the middle of Alixia's name. Besides, she's l33t enough without the l4me 1 in her n4me.

* We **migrated from MySQL to MariaDB** in keeping with our move toward free-and-open-source-software. And Oracle is becoming a little scary lately.

* We **migrated from Oracle Java 8 to OpenJDK Java 8**, then **migrated again to OpenJDK Java 10**. Whew. But the Jigsaw modular structure introduced in Java 9 seemed custom-designed for Alixia, so we embraced it with open(JDK) arms. Also, see scary Oracle note above.

* We added a **variant of AlixiaPi called AlixiaPiAIY**. It uses the Google AIY Voice Kit to provide the hardware for Alixia to run on a Raspberry Pi Zero (or bigger).

* We finally **added Automated Speech Recognition to Alixia**, using the DeepSpeech engine developed by Mozilla. For the time being, we are running a standalone Python DeepSpeech server and accessing it via TCP/IP, but at least it's local, vs. *in the cloud* (shudder). It works pretty well, but we have lots of improvements to make, not the least being incorporating it directly into Alixia.

* On our primary Alixia development platform we **changed our OS from Linux Mint to Arch Linux**. This mostly won't affect the project, but in some peripheral ways it may. For instance, Arch Linux makes use of systemd, vs. not for Mint, so instructions for doing things Alixianic will first be written for systemd.

* We **changed the name of spark to sememe**. We were never really satisfied with the 'spark' name, and 'sememe' is at least evocative of what it represents in Alixia. Probably it will creep out linguists, but into every life a little rain must fall....

* We **added a new Swing-based console**, that has history arrows (*finally*) and will be the base of a "smart" console sub-project.

* We **added the Alixia Sierra module**, and wrote a tutorial about doing so. Sierra is tasked with communicating with IoT gizmos. Also, we note that with the move to Java 10, the tutorial is already somewhat out-of-date, but we'll fix it.

* We **changed project names and packages to remove spaces**. We also changed "AlixiaNode" to "AlixiaNodeServer", and "AlixiaWeb" to "AlixiaWebServer".

* We **changed the A1iciaCentral module (back) to A1icia**, and created a new A1iciaCentral to reify modules and run A1icia.

* We **changed the Mike library updater**, and fixed it to follow symlinks.

* We **removed the embedded Google Cloud Translate functionality** and switched to a REST interface (we're still using Google Translate for that). Anecdotally there has been no noticable performance hit; and the code-and-dependencies excision dropped about 50 kilos off of Alixia's thighs, for which she is eternally grateful.

* We **rearranged various utility functions** in Alixia and moved most of them to the API, where they seem to fit better.

* We mostly **replaced System.out.xxx and System.err.xxx** with defined functions.

* We **created a new Node.js console module**, called, unsurprisingly, AlixiaNodeConsole. It was mostly written as an exercise to check out
Node.js's **repl** functionality, but it grew like Topsy, whoever that is, and made us finally accomodate text-only remotes in StationServer.

So there you have Version 4 changes, so far at least. We're still working on the CLI and Swing consoles and fixing bugs as we encounter them. Ciao.

- hulles


