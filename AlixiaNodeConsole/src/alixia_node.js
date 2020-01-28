const repl = require('repl');
const redis = require('redis');
const host = "localhost";
const port = 6379;
const pubChannel = "alixia:channel:text:to";
const subChannel = "alixia:channel:text:from";
const broadcastID = "ALL";

var subClient = redis.createClient(port, host);
var pubClient = redis.createClient(port, host);
var alixianID;
var stationID;

console.log();
console.log("Alixia Node.js CLI");
console.log("Daily greater with all horizon users!");
console.log();

subClient.on("error", function (err) {

    console.error("Error: " + err);
});

pubClient.on("error", function (err) {

    console.error("Error: " + err);
});

pubClient.get("alixia:jsonstation", function(err, reply) {
    var jsonAppKeys;
    var jsonObj;
    var objName;
    
    if (reply == null) {
        console.error("Error: can't find JSON station keys, make sure you have run AlixiaStationInstaller on this machine");
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
        console.error("Error: can't find station ID, make sure you have run AlixiaStationInstaller on this machine");
        process.exit(1);
    }
    gotAsync();
});

pubClient.incr("alixia:alixian:next_alixian", function(err, reply) {

    alixianID = reply;
    gotAsync();
});

subClient.on("subscribe", function (channel, count) {

    console.log("Subscribed to Alixia");
});
 
subClient.on("message", function (channel, message) {
    var msgArray;
    
    // change this to use same output stream as repl TODO
    
    msgArray = message.split("::");
    if ((msgArray[0] == alixianID) || (msgArray[0] == broadcastID)) {
        console.log("Alixia: " + msgArray[1]);
        process.stdout.write("> "); // set up a new prompt
        if (msgArray[1].startsWith("***")) {
            // fatal error occurred in AlixiaCentral
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
    
    textToSend = `${alixianID}::${stationID}`;
    pubClient.publish(pubChannel, textToSend);

    const r = repl.start({ prompt: '> ', eval: textEval, writer: textEcho });
}

function gotAsync() {

    if ((alixianID != null) && (stationID != null)) {
        startApp();
    }
}

function alixiaSend(text) {
    var textToSend;
    
    textToSend = `${alixianID}::${text}`;
    pubClient.publish(pubChannel, textToSend);
    return text;
}

function textEval(cmd, context, filename, callback) {

    callback(null, alixiaSend(cmd));
}

function textEcho(output) {

    return "Me: " + output;
}

