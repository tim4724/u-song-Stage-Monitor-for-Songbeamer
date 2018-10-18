function main() {
    const titleElement = document.getElementById('title');
    const pages = document.getElementsByClassName('page');
    const errorElement = document.getElementById('errorBox');
    const clientsCountElement = document.getElementById('activeClients');
    const clockButton = document.getElementById('clock');

    let currentPage = undefined; // element
    let lastPageNumber = -2; // number

    function simplePostRequest(path) {
        let xhr = new XMLHttpRequest();
        xhr.open('POST', path, true);
        xhr.send();
    }

    function connectToWebSocket() {
        const songId = parseInt(titleElement.getAttribute('data-songId'));
        const ws = new WebSocket('ws://' + location.host + '/song/ws');
        ws.onopen = function () {
            errorElement.style.display = 'none';
        };
        ws.onmessage = function (ev) {
            let data = JSON.parse(ev.data);
            if (songId !== parseInt(data.songId)) {
                // reload page if new song is selected or beamer does show something that is not a song
                setTimeout(function () {
                    location.reload(true);
                }, 700);
                document.body.classList.add('bodyExit');
            } else {
                if (clockButton) {
                    const visible = data.page === -1 && data.songType === "SNG";
                    clockButton.style.opacity = visible ? "1" : "0";
                    clockButton.style.cursor = visible ? "pointer" : "default";
                    clockButton.onclick = visible ? backend.clock : "";
                }
                updatePageNumber(data.page);
            }
            if (clientsCountElement) {
                if (data.clients === 1) {
                    clientsCountElement.classList.add('negative');
                } else {
                    clientsCountElement.classList.remove('negative');
                }
                clientsCountElement.innerHTML = data.clients;
            }
            errorElement.style.display = 'none';
        };
        ws.onclose = function (ev) {
            setTimeout(connectToWebSocket, 500);
            console.error('ws closed' + ev.reason);
            errorElement.style.display = 'block';
        };
    }

    function updatePageNumber(newPageNumber) {
        const oldPage = currentPage;
        const newPage = pages[newPageNumber];

        if (oldPage) {
            oldPage.parentNode.classList.remove('currentSection');
        } else {
            // no page is set yet
            // song was loaded, but no page selected
            if (newPageNumber === -1) {
                scrollTo(0, 0); // scroll to the top of the page
                lastPageNumber = -1;
                return;
            }
        }

        if (newPage) {
            currentPage = newPage;
            lastPageNumber = newPageNumber;
            if (oldPage) {
                oldPage.classList.remove('currentPage');
            }
            currentPage.classList.add('currentPage');
            currentPage.parentElement.classList.add('currentSection');

            let offset = 10;
            let scrollTarget;
            if (newPageNumber === 0) {
                scrollTarget = titleElement;
            } else if (currentPage.parentElement.scrollHeight < (innerHeight * 0.8)) {
                scrollTarget = currentPage.parentNode;
            } else {
                scrollTarget = currentPage;
                if (currentPage.parentElement.getElementsByClassName('page')[0] !== currentPage) {
                    offset = innerHeight * 0.2;
                }
            }

            let duration = isVisible(currentPage) ? 2000 : 300;
            scroll(scrollTarget, offset, duration);
        }
    }

    return {
        setLang: function (newLang) {
            simplePostRequest('song/lang/' + newLang);
        },
        pageUp: function () {
            simplePostRequest('song/page/' + (lastPageNumber - 1));
        },
        pageDown: function () {
            simplePostRequest('song/page/' + (lastPageNumber + 1));
        },
        connectToWebSocket: connectToWebSocket,
        clock: function () {
            simplePostRequest('song/clock');
        }
    }
}

function scroll(e, offset, duration) {
    if (typeof zenscroll !== "undefined") {
        zenscroll.setup(duration, offset);
        zenscroll.to(e, duration);
        setTimeout(function () {
            if (!zenscroll.moving()) {
                zenscroll.to(e, duration);
            }
        }, 100); // sometimes its buggy and doesn't scroll right away
        // backup plan
        setTimeout(function () {
            if (!zenscroll.moving()) {
                e.scrollIntoView(true);
            }
        }, 200);
    } else {
        e.scrollIntoView(true);
    }
}

function isVisible(el) {
    const elemTop = el.getBoundingClientRect().top;
    const elemBottom = el.getBoundingClientRect().bottom;
    return (elemTop >= 0) && (elemBottom <= innerHeight);
}