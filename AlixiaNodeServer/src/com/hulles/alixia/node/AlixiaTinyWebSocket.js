/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of Alixia.
 *  
 * Alixia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *    
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-License-Identifer: GPL-3.0-or-later
 *******************************************************************************/
/**
 * http://usejsdoc.org/
 */
/*jshint esversion: 6 */

const http = require('http');
const WebSocket = require('/usr/local/node/lib/node_modules/ws');
const hostname = '127.0.0.1';
//const hostname = '10.0.0.22';
const port = 1337;
const myBody = 'What a nice body!';

const server = http.createServer((request, response) => {
	const { headers, method, url } = request;
	var sessionID;
	let body = [];
	request.on('error', (err) => {
		console.error(err);
	    response.statusCode = 400;
	    response.end();
	}).on('data', (chunk) => {
		body.push(chunk);
	}).on('end', () => {
		body = Buffer.concat(body).toString();
		
		if (request.method === 'POST') {
			if (request.url === '/echo') {
			    request.pipe(response);
				response.end();
			} else if (request.url === '/alixia/text') {
				sessionID = register();
				response.statusCode = 200;
				response.setHeader('Content-Type', 'text/plain');
				response.end(plainAnswer(sessionID, body));
			} else {
				response.statusCode = 404;
				response.end();
			}
		} else if (request.method === 'GET') {
			if (request.url === '/alixia/text') {
				sessionID = register();
				response.statusCode = 200;
				response.setHeader('Content-Type', 'text/plain');
				response.end(plainAnswer(sessionID, myBody));
			} else if (request.url === '/alixia') {
				sessionID = register();
				response.statusCode = 200;
				response.setHeader('Content-Type', 'text/html');
//				const responseBody = { headers, method, url, body };
				response.end(htmlAnswer(sessionID, myBody));
			} else {
				response.statusCode = 404;
				response.end();
			}
		} else {
			response.statusCode = 404;
			response.end();
		}

		response.on('error', (err) => {
			console.error(err);
		});

	});
});

const wss = new WebSocket.Server({ server });

wss.on('connection', function connection(ws) {
	const location = url.parse(req.url, true);
	const ip = req.connection.remoteAddress;
	// You might use location.query.access_token to authenticate or share sessions
	// or req.headers.cookie (see http://stackoverflow.com/a/16395220/151312)

	ws.on('message', function incoming(message) {
		console.log('received: %s', message);
	});
	ws.on('error', function error(err) {
		console.error(err);
	});
	
	ws.send('something');
});


server.listen(port, hostname, () => {
	console.log(`Server running at http://${hostname}:${port}/`);
	const ws = new WebSocket('ws://localhost:1337');

	ws.on('open', function open() {
		ws.send('All glory to WebSockets!');
	});
	ws.on('error', function error(err) {
		console.error(err);
	});
});
