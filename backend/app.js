
let uri = 'ws://' + window.location.host + '/ws'
let ws = new WebSocket(uri)

ws.onmessage = e => {
	console.log(e.data)
}

let count = 0

setInterval(() => ws.send('Message ' + ++count), 24000)
