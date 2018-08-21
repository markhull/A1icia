/*******************************************************************************
 * Copyright © 2017, 2018 Hulles Industries LLC
 * All rights reserved
 *  
 * This file is part of A1icia.
 *  
 * A1icia is free software: you can redistribute it and/or modify
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
/*jshint esversion: 6 */
const fs = require('fs');
const path = require('path');
const http2 = require('http2');
const server = http2.createSecureServer({
  cert: fs.readFileSync(path.join(__dirname, './ssl/cert.pem')),
  key: fs.readFileSync(path.join(__dirname, './ssl/key.pem'))
}, onRequest);

// Request handler
function onRequest (request, response) {
	const { headers, method, url } = request;
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
			} else if (request.url === '/a1icia/text') {
				response.statusCode = 200;
				response.setHeader('Content-Type', 'text/plain');
				response.end(plainAnswer(body));
			} else {
				response.statusCode = 404;
				response.end();
			}
		} else if (request.method === 'GET') {
			if (request.url === '/a1icia/text') {
				response.statusCode = 200;
				response.setHeader('Content-Type', 'text/plain');
				response.end(plainAnswer(myBody));
			} else if (request.url === '/a1icia') {
				response.statusCode = 200;
				response.setHeader('Content-Type', 'text/html');
				const responseBody = { headers, method, url, body };

				response.write('<html>');
				response.write('<body>');
				response.write('<h1>Hello, It\'s Secure A1icia!</h1>');
				response.write('<h2><i>daily greater with all horizon users!</i></h2>');
				response.write('</body>');
				response.write('</html>');
				response.end();
				
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
}

server.listen(port, hostname, () => {
	console.log(`Secure server running at http://${hostname}:${port}/`);
});

