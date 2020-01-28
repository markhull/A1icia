
const express = require(urlFinder("express"));
const bodyParser = require(urlFinder("parser"));
const textParser = bodyParser.text();
const jsonParser = bodyParser.json();
const app = express();
const hostname = serverParams("host");
const port = serverParams("port");

// req.xhr === true means XHttpRequest

/*
*
*  /alixia/text
*
*/

app.get('/alixia/text', function (req, res) {
	let sessionID = req.query.sessionid;
	if ((sessionID == {}) || (sessionID == undefined) || (sessionID == null)) {
		console.error("No session ID parameter");
		res.sendStatus(400);
		return;
	}
	let message = textAnswer(sessionID);
    let msgText = `${sessionID}||${message}`;
	res.set('Content-Type', 'text/plain');
	res.send(msgText);
});

app.post('/alixia/text', textParser, function (req, res) {
    let sessionID = req.query.sessionid;
	if (sessionID == {}) {
		// for now, we allow a POST without a previous sessionid, so we generate one
        sessionID = register();
	}
	let message = textAnswer(sessionID, req.body);
    let msgText = `${sessionID}||${message}`;
	res.set('Content-Type', 'text/plain');
	res.send(msgText);
});

/*
*
*  /alixia/json -- we only support POST for JSON processing
*
*/

app.post('/alixia/json', jsonParser, function (req, res) {
	let requestObj = req.body;
    let sessionID = requestObj.sessionid;
	if ((sessionID == {}) || (sessionID == undefined) || (sessionID == null)) {
		console.error("No session ID parameter");
		res.sendStatus(400);
		return;
	}
	// TODO maybe figure out how to make JSON object a V8Object (just cast it?)
	let messageStr = jsonAnswer(sessionID, requestObj.text);
	// let messageObj = JSON.parse(messageStr);
	// for (var nm in messageObj) {
	// 	console.log("Container name is " + nm);
	// }
	res.set('Content-Type', 'application/json');
	res.send(messageStr);
});

/*
*
*  /alixia/media -- we only support GET for media processing
*
*/

app.get('/alixia/media', function (req, res) {
	let mediaKey = req.query.mmd;
	if ((mediaKey == {}) || (mediaKey == undefined) || (mediaKey == null)) {
		console.error("No media ID parameter");
		res.sendStatus(400);
		return;
	}
	let bufStr = mediaAnswer(mediaKey);
	let buf = Buffer.from(bufStr, 'base64');
	res.send(buf);
});

/*
*
*  /alixia/html
*
*/

app.get('/alixia', function (req, res) {
	let sessionID = register();
	let htmlPage = htmlAnswer();
	res.cookie("sessionid", sessionID);
	res.set('Content-Type', 'text/html');
	res.send(htmlPage);
});

/*
*
*  serve
*
*/

app.listen(port, hostname, () => {
	console.log(`Server running at http://${hostname}:${port}/`);
});


/*
// testing functions

var dummyHTML;

loadFile();

function register() {
    return "xyz123";
}

function textAnswer(sessionID) {
	console.log(`textAnswer: ${sessionID}`);
    return "hi there";
}
function textAnswer(sessionID, text) {
	console.log(`textAnswer: ${sessionID} : ${text}`);
    return "hi there";
}

function jsonAnswer(sessionID, text) {
    console.log(`jsonAnswer: ${sessionID} : ${text}`);
    return {'messages': ["hi there"], 'explanations': []};
}

function htmlAnswer() {
    console.log("htmlAnswer");
	var text = dummyHTML;
//    var text = "<html><head></head><body><p>Hello World!</p></body></html>";
    return text;
}

function loadFile() {
	fs.readFile('TinyPage.html', (err, data) => {
		if (err) throw err;
		dummyHTML = data;
	});
}
*/
