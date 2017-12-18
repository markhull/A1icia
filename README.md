
![A1icia Logo](docs/images/A1icia_100H.png)

A1icia is a professional incarnation of a personal assistant written in Java. She is modular, extensible, and damn fast.

## So Why Another Personal Assistant?

The essential nature of a "personal assistant" is that it uses **personal information** about you to help improve your life in some fashion ‒ it will remind you of appointments, keep tracking of your jogging, notify you of new emails. But who do you want to possess that personal information? Google? Amazon? Some ominous government agency? Vladimir Putin? Not that Google is evil, you understand, but still, it's personal information.

So, I really wanted a personal assistant, but one over which I could have complete control and one that would let me do all the things
I wanted to do, see below. So I looked at various efforts written in scripting languages of one sort or another, and while these scripting languages are great for some things, they're not great for massive projects like I knew this would become. Some of the other PAs were certainly large enough, but they were almost all web server creations and based themselves on exotic and questionable (IMO) technology.

So I decided to create a personal assistant that was written in Java by a professional programmer (me, duh) that was adaptable enough to do all the things I wanted to do:

* experiment with machine learning and AI, 
* run so-called **IoT** gizmos around the house (like a [Magic Mirror](https://github.com/MichMich/MagicMirror)), 
* drive my car, 
* and keep me company on these cold and lonely winter nights.

So far, A1icia has managed to do a pretty good job of everything except drive my car, but that's just because I haven't gotten around to that module yet, and I suppose I should learn about LIDAR and crap like that first so I don't kill anyone.

And as far as keeping me company, A1icia's currently about at the tropical fish stage, though I expect her to enter the Shih Tzu stage soon. Don't tell her I said that, by the way.

## N Interesting Things About A1icia, N Being A Small Integer Less Than 100 (Probably)

* A1icia can speak, and quite well. She uses a small and robust Text To Speech (TTS) service to do this.
* A1icia can currently speak 5.5 different languages; the .5 is because she speaks American and British English.
* A1icia can "understand" many languages, thanks to Google Cloud Translate, but she currently only uses the 5.5 referred to above.
* A1icia can run a remote Raspberry Pi, either stock with a Command Line Interface (CLI) or configured as a "Magic Mirror", and successfully communicate with the Pi hardware using Java (no Python).
* A1icia can classify images using the TensorFlow Inception engine (we have bigger plans for TensorFlow, heh heh).
* A1icia can respond to commands with multimedia output as well as text/voice.
* A1icia has a database of quotes from which she can select and read, because every project needs a quotes database, dammit.
* A1icia has, to a limited degree, "self-awareness", in the sense that she knows about her memory, file systems, operating temperature and the like, and can report on same.
* A1icia can communicate with a weather service and tell you the current weather and the forecast for wherever (mostly) you may be.
* A1icia can look up and answer queries about facts ("Who is Donald Trump?"), and has the beginnings of non-factoid query response ("Why is Donald Trump's hair that weird color?").
* A1icia has two built-in web servers and three command line interfaces with which to communicate with the outside world, i.e. you.
* A1icia is free software, licensed under GPL3.

## Design Goals

* A1icia should have little to no reliance on external services, preferably no reliance. Thus, no "cloud services". We'll use local TTS and ASR services instead of e.g. Google Voice Cloud Service. As of this writing, the only cloud service that A1icia uses is Google Cloud Translate, and that's just because we haven't found a suitable alternative yet.

* All Plain Old Java. Really. POJOs with no annotations or decorations or funky "enhancements". If you can program in Java, you should have zero learning curve with A1icia. Currently, all the code is Java 8 except for a little bit of Prolog and Node.js. And the only reason there's any Node.js at all is to attract the children with flashy toys, then we'll suck them in and make them code in Java.
