
![Alixia Logo](docs/images/Alixia_100H.png)

Alixia is a professional incarnation of a personal assistant written in Java. She is modular, extensible, and damn fast.

### So Why Another Personal Assistant?

The essential nature of a "personal assistant" is that it uses **personal information** about you to help improve your life in some fashion ‒ it will remind you of appointments, keep track of your jogging, notify you of new emails. But who do you want to possess that personal information? Google? Amazon? Some ominous government agency? Vladimir Putin? Not that Google is evil, you understand, not sure about the others, but still, it's personal information.

### Why Not Use An Existing Project?

I really wanted a personal assistant, but one over which I could have complete control and one that would let me do all the things I wanted to do, see below. So I looked at various efforts written in scripting languages of one sort or another, and while these scripting languages are great for some things, they're not great for massive projects like I knew this would become. Some of the other PAs were certainly large enough, but they were almost all web server creations and based themselves on exotic and questionable (IMO) technology.

### DIY Punkware

So, since I listened to a [Black Flag song](https://en.wikipedia.org/wiki/DIY_ethic) once, I decided to write a personal assistant myself in Java that was adaptable enough to do all the things I wanted to do:

* experiment with machine learning and AI, 
* run so-called **IoT** gizmos around the house (like a [Magic Mirror](https://github.com/MichMich/MagicMirror)), 
* drive my car, 
* and keep me company on these cold and lonely winter nights.

So far, Alixia has managed to do a pretty good job of everything except drive my car, but that's just because I haven't gotten around to that module yet, and I suppose I should learn about LIDAR and crap like that first so I don't kill anyone.

And as far as keeping me company, Alixia's currently about at the tropical fish stage¹, though I expect her to enter the Shih Tzu stage soon. Don't tell her I said that, by the way.

### N Interesting Things About Alixia, N Being A Small Integer Less Than 100 (Probably)

* Alixia can speak, and quite well. She uses a small and robust Text To Speech (TTS) service to do this.
* Alixia can currently speak 5.5 different languages; the .5 is because she speaks American and British English.
* Alixia can "understand" many languages, thanks to Google Cloud Translate, but she currently only uses the 5.5 referred to above.
* Alixia can run a remote Raspberry Pi, either stock with a Command Line Interface (CLI) or configured as a "Magic Mirror", and successfully communicate with the Pi hardware using Java (no Python).
* Alixia can classify images using the TensorFlow Inception engine (we have bigger plans for TensorFlow, heh heh).
* Alixia can respond to commands with multimedia output as well as text/voice.
* Alixia has a database of quotes from which she can select and read, because every project needs a quotes database, dammit.
* Alixia has, to a limited degree, "self-awareness", in the sense that she knows about her memory, file systems, operating temperature and the like, and can report on same.
* Alixia can communicate with a weather service and tell you the current weather and the forecast for wherever (mostly) you may be.
* Alixia can look up and answer queries about facts ("Who is Donald Trump?"), and has the beginnings of non-factoid query response ("Why is Donald Trump's hair that weird color?").
* Alixia has two built-in web servers and three command line interfaces with which to communicate with the outside world, i.e. you.
* Alixia is free software, licensed under GPL3.

### Directory

* **[Alixia GitHub Project](https://github.com/markhull/Alixia)**

* **[Alixia Documentation](https://markhull.github.io/Alixia)**

* **[Alixia Wiki](https://github.com/markhull/Alixia/wiki)**

---

¹ Albeit a tropical fish that can play Black Flag and ask me if I've lost weight....
