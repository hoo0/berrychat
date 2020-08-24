/* websocket = new WebSocket()
 * websocket.onopen = handleConnOpen
 * 
 * peerConnection = new RTCPeerConnection()
 * peerConnection.createDataChannel()
 * peerConnection.onicecandidate = connSend
 */

//connecting to signaling server - 처음에 연결, 하지만 언젠가 끊어지겠지
var wssServer = 'wss://berrychat-suopm.run.goorm.io/socket';
var peerConnection;
var dataChannel;
var input = document.getElementById("messageInput");
var websocket;

createWebsocket();

function createWebsocket() {
	websocket = new WebSocket(wssServer);
	websocket.onopen = handleWebsocketOpen; // 연결되면 바로 open event가 발생, 초기화 진행
}

function websocketSend(message) {
	_log("> websocketSend: websocket:", websocket);
	
    websocket.send(JSON.stringify(message));
}

function handleWebsocketOpen() {
	_log("WebsocketOpen");

	websocket.onclose = function(event) { _log('websocket.onclose:', event); };
	websocket.onerror = function(event) { _log('websocket.onerror:', event); };
	
	websocket.onmessage = function(msg) {
		var content = JSON.parse(msg.data);
		_log(">>> websocket.onmessage: content.event:", content.event);

		switch (content.event) {
		case "offer":
			handleOffer(content.data);
			break;
		case "answer":
			handleAnswer(content.data);
			break;
		case "candidate":
			handleCandidate(content.data);
			break;
		default:
			break;
		}
	};

	createPeerConnection();
}

function createPeerConnection() {
	_log('createPeerConnection');
	
	// peerConnection init
    var configuration = null;

    peerConnection = new RTCPeerConnection(configuration, {
        optional : [ {
            RtpDataChannels : true
        } ]
    });

    // Setup ice handling - STUN서버를 통해 서로의 네트워크정보를 주고받는 icecandidate event 발생
    peerConnection.onicecandidate = function(event) {
		_log('peerConnection.onicecandidate:', event);
        if (event.candidate) {
            websocketSend({
                event : "candidate",
                data : event.candidate
            });
        }
    };
	
	createDataChannel();
}

function createDataChannel() {
	_log('createDataChannel');
	
	// TURN서버 : 네트워크정보를 기반으로 데이터를 중계해주는 Relay서버
    dataChannel = peerConnection.createDataChannel("dataChannel", {
        reliable : true
    });

	dataChannel.onopen = function(event) { _log("dataChannel.onopen:", event); };
    dataChannel.onclose = function(event) { _log("dataChannel.onclose:", event); };
    dataChannel.onerror = function(event) { _log("dataChannel.onerror:", event); };
    dataChannel.onmessage = function(event) { _log("dataChannel.onmessage:", event.data); };
}

function closeAll() {
	closeChannel();
	closePeerConnect();
	websocket.close();
}

function closePeerConnect() {
	_log('closePeerConnect');
	peerConnection.close();
}

function closeChannel() {
	_log('closeChannel');
	dataChannel.close();
}

// server
function createOffer() {
	_log('createOffer');

    peerConnection.createOffer(function(offer) {
        websocketSend({ 
            event : "offer",
            data : offer
        });
		
		_log('setLocal(server)');
        peerConnection.setLocalDescription(offer);
    }, function(error) {
        alert("Error creating an offer");
    });
}

// client
function handleOffer(offer) {
	_log('>>> handleOffer');

	_log('setRemote(client)');
    peerConnection.setRemoteDescription(new RTCSessionDescription(offer));

   	_log('createAnswer');
    peerConnection.createAnswer(function(answer) {
		_log('setLocal(client)');
        peerConnection.setLocalDescription(answer);

        websocketSend({
            event : "answer",
            data : answer
        });
    }, function(error) {
        alert("Error creating an answer");
    });
}

// server, client
function handleCandidate(candidate) {
	_log('handleCandidate');
	
    peerConnection.addIceCandidate(new RTCIceCandidate(candidate));
}

// server
function handleAnswer(answer) {
	_log('handleAnswer');
	
	_log('setRemote(server)');
    peerConnection.setRemoteDescription(new RTCSessionDescription(answer));
    _log("handleAnswer: connection established successfully!!");
	
	// if (dataChannel.readyState != 'open') {
	// 	console.warn('handleAnswer: dataChannel.readyState=', dataChannel.readyState);
	// 	interval = setInterval(function() {
	// 		if (dataChannel.readyState == 'open') {
	// 			clearInterval(interval);
	// 		}
	// 		_log('dataChannel.readyState=', dataChannel.readyState);
	// 	}, 100);
	// }
}

function sendMessage() {
	_log('sendMessage: ', input.value);
	
    dataChannel.send(input.value);
    input.value = "";
}

var C_LOGBOX = 'Y';
function _log() {
	if (typeof console != 'undefined' && typeof console.log == 'function') {
		try {
			console.log.apply(console, arguments);

			var logbox = $('#logTextarea');
			if (C_LOGBOX == 'Y' && logbox.length > 0) {
				if (logbox.css('display') == 'none') logbox.show();

				var logboxCont = logbox.val();
				var count = logboxCont.split(/\r|\r\n|\n/).length;

				logbox.val(logboxCont+'\n'+arguments[0]);
			}
		} catch (e) {}
	}
}

function clearLog() {
    $('#logTextarea').val('');
}
