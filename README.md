# A1icia
![A1icia Logo](fixme/A1icia_100H.png)

A1icia is a professional incarnation of a personal assistant written in Java. She is modular, extensible, and damn fast.

# Design Goals

* A1icia should have little to no reliance on external services, preferably no reliance. Thus, no "cloud services". We'll use local TTS and ASR services instead of e.g. Google Voice Cloud Service. As of this writing, the only cloud service that A1icia uses is Google Cloud Translate, and that's just because we haven't found a suitable alternative yet.

* All Plain Old Java. Really. POJOs with no annotations or decorations or funky "enhancements". If you can program in Java, you should have zero learning curve with A1icia. Currently, all the code is Java 8 except for a little bit of Prolog and Node.js. And the only reason there's any Node.js at all is to attract the children with flashy toys, then we'll suck them in and make them code in Java.
