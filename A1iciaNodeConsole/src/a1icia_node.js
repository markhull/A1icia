const repl = require('repl');
const redis = require('redis');
const host = "10.0.0.18";
const port = 6379;
const pubChannel = "a1icia:channel:text:to";
const subChannel = "a1icia:channel:text:from";
const broadcastID = "ALL";

var subClient = redis.createClient(port, host);
var pubClient = redis.createClient(port, host);
var a1icianID;
var stationID;

console.log();
console.log("A1icia Node.js CLI");
console.log("Daily greater with all horizon users!");
console.log();

pubClient.get("a1icia:jsonstation", function(err, reply) {
    var jsonAppKeys;
    var jsonObj;
    var objName;
    
    if (reply == null) {
        console.error("Error: can't find JSON station keys, make sure you have run A1iciaStationInstaller on this machine");
        process.exit(1);
    }
    jsonAppKeys = JSON.parse(reply);
    for (i in jsonAppKeys) {
        jsonObj = jsonAppKeys[i];
        objName = jsonObj.Name;
        if (objName == "STATIONID") {
            stationID = jsonObj.Value;
            break;
        }
    }
    if (stationID == null) {
        console.error("Error: can't find station ID, make sure you have run A1iciaStationInstaller on this machine");
        process.exit(1);
    }
    gotAsync();
});

subClient.on("error", function (err) {

    console.error("Error: " + err);
});

pubClient.on("error", function (err) {

    console.error("Error: " + err);
});

pubClient.incr("a1icia:a1ician:next_a1ician", function(err, reply) {

    a1icianID = reply;
    gotAsync();
});

subClient.on("subscribe", function (channel, count) {

    console.log("Subscribed to A1icia");
});
 
subClient.on("message", function (channel, message) {
    var msgArray;
    
    // change this to use same output stream as repl TODO
    msgArray = message.split("::");
    if ((msgArray[0] == a1icianID) || (msgArray[0] == broadcastID)) {
        console.log("A1icia: " + msgArray[1]);
        process.stdout.write("> "); // set up a new prompt
        if (msgArray[1].startsWith("***")) {
            // fatal error occurred in A1iciaCentral
            process.exit(1);
        }
        if (msgArray[1].startsWith("**")) {
            // server shutdown
            process.exit();
        }
    }
});

subClient.subscribe(subChannel);

function startApp() {
    var textToSend;
    
    textToSend = `${a1icianID}::${stationID}`;
    pubClient.publish(pubChannel, textToSend);

    const r = repl.start({ prompt: '> ', eval: textEval, writer: textEcho });
}

function gotAsync() {

    if ((a1icianID != null) && (stationID != null)) {
        startApp();
    }
}

function a1iciaSend(text) {
    var textToSend;
    
    textToSend = `${a1icianID}::${text}`;
    pubClient.publish(pubChannel, textToSend);
    return text;
}

function textEval(cmd, context, filename, callback) {

    callback(null, a1iciaSend(cmd));
}

function textEcho(output) {

    return "Me: " + output;
}

