let timer;

function simplePutRequest(path, forceReload) {
    let xhr = new XMLHttpRequest();
    xhr.open('PUT', path, true);
    xhr.onloadend = function () {
        if (xhr.status !== 200) {
            if (xhr.responseText && xhr.responseText.length > 0) {
                alert(xhr.responseText);
            } else {
                alert("Error");
            }
            location.reload(true);
        } else {
            if (forceReload) {
                location.reload(true);
            } else {
                const savedHint = document.getElementById("savedHint");
                if (savedHint) {
                    savedHint.classList.remove("invisible");
                    if (timer) {
                        clearTimeout(timer);
                    }
                    timer = setTimeout(function () {
                        savedHint.classList.add("invisible");
                    }, 2000);
                }
            }
        }
    };
    xhr.send()
}

function onCheckedChanged(id) {
    const cb = document.getElementById(id);
    simplePutRequest("settings?" + id + "=" + cb.checked);
}

function onInputChanged(id, forceReload) {
    const input = document.getElementById(id);
    simplePutRequest("settings?" + id + "=" + input.value, forceReload);
}

let clientsCount = -1;

function connectToWebSocket() {
    const ws = new WebSocket("ws://" + location.host + "/song/ws?status=true");
    ws.onmessage = function (ev) {
        let data = JSON.parse(ev.data);
        if (clientsCount === -1) {
            clientsCount = data.clients
        } else if (clientsCount !== data.clients) {
            // listen for changes in clientsCount because the fullscreen window could have been closed
            // Reload the page to ensure the correct value is displayed in preference "showFullscreen"
            location.reload(true);
        }
    };
    ws.onclose = function (ev) {
        setTimeout(connectToWebSocket, 500);
        console.error("ws closed" + ev.reason);
    };
}