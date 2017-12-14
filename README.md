
![A1icia Logo](docs/images/A1icia_100H.png)

A1icia is a professional incarnation of a personal assistant written in Java. She is modular, extensible, and damn fast.

# So Why Another Personal Assistant?

I wanted to create a "personal assistant" that was written in Java by a professional programmer (me, duh) that was adaptable enough to do all the things I wanted to do:

* experiment with machine learning and AI, 
* run so-called **IoT** gizmos around the house (like a Magic Mirror), 
* drive my car, 
* and keep me company on these cold and lonely winter nights.

So far, A1icia has managed to do a pretty good job of everything except drive my car, but that's just because I haven't gotten around to that module yet, and I suppose I should learn about LIDAR and crap like that first so I don't kill anyone.

And as far as keeping me company, A1icia's currently about at the tropical fish stage, though I expect her to enter the house cat stage soon.

# Design Goals

* A1icia should have little to no reliance on external services, preferably no reliance. Thus, no "cloud services". We'll use local TTS and ASR services instead of e.g. Google Voice Cloud Service. As of this writing, the only cloud service that A1icia uses is Google Cloud Translate, and that's just because we haven't found a suitable alternative yet.

* All Plain Old Java. Really. POJOs with no annotations or decorations or funky "enhancements". If you can program in Java, you should have zero learning curve with A1icia. Currently, all the code is Java 8 except for a little bit of Prolog and Node.js. And the only reason there's any Node.js at all is to attract the children with flashy toys, then we'll suck them in and make them code in Java.
